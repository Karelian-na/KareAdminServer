package cn.karelian.kas.services;

import java.io.FileInputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.ReflectionKit;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import cn.karelian.kas.KasApplication;
import cn.karelian.kas.Result;
import cn.karelian.kas.codes.FieldErrors;
import cn.karelian.kas.dtos.IndexParam;
import cn.karelian.kas.dtos.IndexParam.IndexType;
import cn.karelian.kas.exceptions.NullRequestException;
import cn.karelian.kas.exceptions.IllegalAccessException;
import cn.karelian.kas.exceptions.PermissionNotFoundException;
import cn.karelian.kas.mappers.MenusMapper;
import cn.karelian.kas.mappers.KasMapper;
import cn.karelian.kas.mappers.PermissionsMapper;
import cn.karelian.kas.mappers.TableFieldsInfoMapper;
import cn.karelian.kas.mappers.ViewsInfoMapper;
import cn.karelian.kas.services.interfaces.IKasService;
import cn.karelian.kas.utils.HttpUtil;
import cn.karelian.kas.utils.PageData;
import cn.karelian.kas.utils.SpringContextUtil;
import cn.karelian.kas.utils.WebPageInfo;
import cn.karelian.kas.views.FieldsInfoView;
import cn.karelian.kas.views.MenusView;
import cn.karelian.kas.views.Views;
import jakarta.servlet.http.HttpServletRequest;

public class KasService<M extends KasMapper<E, V>, E, V> extends ServiceImpl<M, E> implements IKasService<E, V> {
	private static TableFieldsInfoMapper tableFieldsInfoMapper;
	private static PermissionsMapper permissionsMapper;
	private static MenusMapper menusMapper;
	private static ViewsInfoMapper viewsInfoMapper;

	private static void getBeans() {
		tableFieldsInfoMapper = SpringContextUtil.getBean(TableFieldsInfoMapper.class);
		permissionsMapper = SpringContextUtil.getBean(PermissionsMapper.class);
		viewsInfoMapper = SpringContextUtil.getBean(ViewsInfoMapper.class);
		menusMapper = SpringContextUtil.getBean(MenusMapper.class);
	}

	public static <T> List<FieldsInfoView> getFields(Class<T> clszz) throws IllegalAccessException {
		return KasService.getFields(clszz, null);
	}

	public static <T> List<FieldsInfoView> getFields(Class<T> clszz, Wrapper<T> qw) throws IllegalAccessException {
		if (tableFieldsInfoMapper == null) {
			getBeans();
		}

		String viewName = TableInfoHelper.getTableInfo(clszz).getTableName();
		LambdaQueryWrapper<FieldsInfoView> lqw = new LambdaQueryWrapper<>();
		lqw.eq(FieldsInfoView::getTable_name, viewName);
		if (qw != null && qw.getSqlSelect() != null) {
			lqw.in(FieldsInfoView::getField_name, List.of(qw.getSqlSelect().split(",")));
		}
		List<FieldsInfoView> fields = tableFieldsInfoMapper.selectViewList(lqw);
		if (fields.size() == 0) {
			throw new IllegalAccessException("无可访问的字段！");
		}
		return fields;
	}

	public static <V> WebPageInfo<V> getWebPageInfo(Class<V> clszz)
			throws IllegalAccessException, NullRequestException, PermissionNotFoundException {
		return KasService.getWebPageInfo(clszz, null, true);
	}

	public static <V> WebPageInfo<V> getWebPageInfo(Class<V> clszz, boolean checkOperAuths)
			throws IllegalAccessException, NullRequestException, PermissionNotFoundException {
		return KasService.getWebPageInfo(clszz, null, checkOperAuths);
	}

	public static <V> WebPageInfo<V> getWebPageInfo(Class<V> clszz, Wrapper<V> qw, boolean checkOperAuths)
			throws IllegalAccessException, NullRequestException, PermissionNotFoundException {
		if (permissionsMapper == null) {
			getBeans();
		}
		HttpServletRequest request = HttpUtil.getRequest();

		Long uid = (Long) request.getSession().getAttribute("id");
		MenusView menu = null;

		if (checkOperAuths) {
			String url = request.getRequestURI();
			menu = menusMapper.getByUrl(url);
			if (null == menu) {
				menu = menusMapper.getByUrl(request.getRequestURI());
				if (null == menu) {
					throw new PermissionNotFoundException();
				}
			}
		}

		WebPageInfo<V> info = new WebPageInfo<>();

		// 获取字段配置
		String viewName = TableInfoHelper.getTableInfo(clszz).getTableName();
		LambdaQueryWrapper<Views> lqw = Wrappers
				.lambdaQuery(Views.class)
				.eq(Views::getView_name, viewName)
				.select(Views::getFields_config);

		Views viewsInfo = viewsInfoMapper.selectViewOne(lqw);
		if (null != viewsInfo) {
			Path viewPath = KasApplication.currentPath.resolve("data/configs").resolve(viewsInfo.getFields_config());
			if (Files.exists(viewPath)) {
				try (FileInputStream fileInputStream = new FileInputStream(viewPath.toString())) {
					info.fieldsConfig = new String(fileInputStream.readAllBytes(), "utf-8");
				} catch (Exception e) {
				}
			}
		}

		// 获取展示的字段
		info.fields = KasService.getFields(clszz, qw);

		// 获取操作权限
		if (checkOperAuths) {
			info.operButtons = menusMapper.getAuthorizedOperationPermissions(uid, menu);
		}
		return info;
	}

	public static <V> WebPageInfo<V> getWebPageInfo(Class<V> clszz, Wrapper<V> qw)
			throws IllegalAccessException, NullRequestException, PermissionNotFoundException {
		return KasService.getWebPageInfo(clszz, qw, true);
	}

	public static <E, V> Result getPageData(KasMapper<E, V> mapper, IndexParam params, Wrapper<V> qw) {
		if (null == qw) {
			qw = new QueryWrapper<>();
		}

		if (!ObjectUtils.isEmpty(params.searchKey) && ObjectUtils.isEmpty(params.searchField)) {
			return Result.fieldError("searchField", FieldErrors.EMPTY);
		} else if (!ObjectUtils.isEmpty(params.searchKey)) {
			((QueryWrapper<V>) qw).like(params.searchField, params.searchKey);
		}

		if (params.pageIdx == null) {
			params.pageIdx = 1L;
		}
		if (params.pageSize == null) {
			params.pageSize = 20L;
		}

		if (params.type == IndexType.One) {
			return new Result(true, mapper.selectViewOne(qw));
		} else if (params.type == IndexType.All) {
			PageData<V> pageData = new PageData<>();
			pageData.data = mapper.selectViewList(qw);
			return new Result(true, pageData);
		} else {
			Long nTotal = mapper.selectViewCount(qw);
			PageData<V> pageData = new PageData<>();
			pageData.totalCount = nTotal;

			Long pageAmount = Double.valueOf(Math.ceil(pageData.totalCount.doubleValue() / params.pageSize))
					.longValue();
			if (params.pageIdx > pageAmount) {
				params.pageIdx = pageAmount;
			}

			Page<V> page = new Page<>();
			page.setSearchCount(true);
			page.setCurrent(params.pageIdx);
			page.setSize(params.pageSize);
			mapper.selectViewPage(page, qw);

			pageData.totalCount = page.getTotal();
			pageData.curPageIdx = page.getCurrent();
			pageData.data = page.getRecords();

			return new Result(true, pageData);
		}
	}

	public static <E, V> Result index(KasMapper<E, V> mapper, IndexParam params)
			throws IllegalAccessException, NullRequestException, PermissionNotFoundException {
		return KasService.index(mapper, params, null);
	}

	@SuppressWarnings("unchecked")
	public static <E, V> Result index(KasMapper<E, V> mapper, IndexParam params, Wrapper<V> qw, boolean checkOperAuths)
			throws IllegalAccessException, NullRequestException, PermissionNotFoundException {
		if (params.initPageSize != null) {
			Class<?>[] clszzes = AopProxyUtils.proxiedUserInterfaces(mapper);
			Type[] type = clszzes[0].getGenericInterfaces();
			Class<V> clszz = (Class<V>) ((ParameterizedType) (type[0])).getActualTypeArguments()[1];

			WebPageInfo<V> info = KasService.getWebPageInfo(clszz, qw, checkOperAuths);

			params.pageSize = params.initPageSize;
			Result result = KasService.getPageData(mapper, params, qw);
			info.pageData = PageData.class.cast(result.getData());

			result.setData(info);
			return result;
		} else {
			return KasService.getPageData(mapper, params, qw);
		}
	}

	public static <E, V> Result index(KasMapper<E, V> mapper, IndexParam params, Wrapper<V> qw)
			throws IllegalAccessException, NullRequestException, PermissionNotFoundException {
		return KasService.index(mapper, params, qw, true);
	}

	//
	// 非静态属性和方法
	//
	@Autowired
	protected M baseMapper;

	protected Class<V> viewClass;

	@SuppressWarnings("unchecked")
	KasService() {
		this.viewClass = (Class<V>) ReflectionKit.getSuperClassGenericType(this.getClass(), KasService.class, 2);
	}

	@Override
	public Class<V> getViewClass() {
		return viewClass;
	}

	@Override
	public List<FieldsInfoView> getFields() throws IllegalAccessException {
		return KasService.getFields(this.viewClass, null);
	}

	@Override
	public List<FieldsInfoView> getFields(Wrapper<V> qw) throws IllegalAccessException {
		return KasService.getFields(this.viewClass, qw);
	}

	@Override
	public WebPageInfo<V> getWebPageInfo()
			throws IllegalAccessException, NullRequestException, PermissionNotFoundException {
		return KasService.getWebPageInfo(this.viewClass);
	}

	@Override
	public WebPageInfo<V> getWebPageInfo(Wrapper<V> qw)
			throws IllegalAccessException, NullRequestException, PermissionNotFoundException {
		return KasService.getWebPageInfo(this.viewClass, qw, true);
	}

	@Override
	public Result getPageData(IndexParam params, Wrapper<V> qw) {
		return KasService.getPageData(this.baseMapper, params, qw);
	}

	@Override
	public Result index(IndexParam params)
			throws IllegalAccessException, NullRequestException, PermissionNotFoundException {
		return KasService.index(this.baseMapper, params);
	}

	@Override
	public Result index(IndexParam params, Wrapper<V> qw)
			throws IllegalAccessException, NullRequestException, PermissionNotFoundException {
		return KasService.index(this.baseMapper, params, qw);
	}

	@Override
	public List<V> listViews(Wrapper<V> ew) {
		return baseMapper.selectViewList(ew);
	}

	@Override
	public List<Map<String, Object>> listViewMaps(Wrapper<V> ew) {
		return baseMapper.selectViewMaps(ew);
	}

	@Override
	public V getViewOne(Wrapper<V> qw) {
		return baseMapper.selectViewOne(qw);
	}

	@Override
	public boolean existsView(Wrapper<V> qw) {
		return baseMapper.existsView(qw);
	}
}

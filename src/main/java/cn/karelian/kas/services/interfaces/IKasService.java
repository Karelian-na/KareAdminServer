package cn.karelian.kas.services.interfaces;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.IService;

import cn.karelian.kas.Result;
import cn.karelian.kas.dtos.IndexParam;
import cn.karelian.kas.exceptions.NullRequestException;
import cn.karelian.kas.exceptions.IllegalAccessException;
import cn.karelian.kas.exceptions.PermissionNotFoundException;
import cn.karelian.kas.utils.WebPageInfo;
import cn.karelian.kas.views.FieldsInfoView;

public interface IKasService<T, V> extends IService<T> {
	public Class<V> getViewClass();

	public List<FieldsInfoView> getFields() throws IllegalAccessException;

	public List<FieldsInfoView> getFields(Wrapper<V> qw) throws IllegalAccessException;

	public WebPageInfo<V> getWebPageInfo()
			throws IllegalAccessException, NullRequestException,
			PermissionNotFoundException;

	public WebPageInfo<V> getWebPageInfo(Wrapper<V> qw)
			throws IllegalAccessException, NullRequestException,
			PermissionNotFoundException;

	public Result getPageData(IndexParam params, Wrapper<V> qw);

	public Result index(IndexParam params)
			throws IllegalAccessException, NullRequestException, PermissionNotFoundException;

	public Result index(IndexParam params, Wrapper<V> qw)
			throws IllegalAccessException, NullRequestException,
			PermissionNotFoundException;

	public List<V> listViews(Wrapper<V> qw);

	public List<Map<String, Object>> listViewMaps(Wrapper<V> qw);

	public V getViewOne(Wrapper<V> qw);

	public boolean existsView(Wrapper<V> qw);
}

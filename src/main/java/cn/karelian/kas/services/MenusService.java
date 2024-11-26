package cn.karelian.kas.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import cn.karelian.kas.Result;
import cn.karelian.kas.dtos.IndexParam;
import cn.karelian.kas.dtos.IndexParam.IndexType;
import cn.karelian.kas.entities.Menus;
import cn.karelian.kas.entities.Permissions;
import cn.karelian.kas.entities.Menus.MenuType;
import cn.karelian.kas.entities.Permissions.OperType;
import cn.karelian.kas.exceptions.NullRequestException;
import cn.karelian.kas.exceptions.IllegalAccessException;
import cn.karelian.kas.exceptions.PermissionNotFoundException;
import cn.karelian.kas.mappers.MenusMapper;
import cn.karelian.kas.mappers.PermissionsMapper;
import cn.karelian.kas.mappers.RolePermAssocMapper;
import cn.karelian.kas.mappers.UserPermAssocMapper;
import cn.karelian.kas.mappers.UserRoleAssocMapper;
import cn.karelian.kas.services.interfaces.IMenusService;
import cn.karelian.kas.utils.EntityUtil;
import cn.karelian.kas.utils.LoginInfomationUtil;
import cn.karelian.kas.utils.OperButton;
import cn.karelian.kas.utils.WebPageInfo;
import cn.karelian.kas.views.MenusView;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author Karelian_na
 * @since 2023-11-09
 */
@Service
public class MenusService extends KasService<MenusMapper, Menus, MenusView> implements IMenusService {
	@Autowired
	private UserPermAssocMapper userPermAssocMapper;
	@Autowired
	private UserRoleAssocMapper userRoleAssocMapper;
	@Autowired
	private RolePermAssocMapper rolePermAssocMapper;
	@Autowired
	private PermissionsMapper permissionsMapper;

	private static Consumer<Menus> func = null;

	@Override
	public MenusView getByUrl(String url) {
		return baseMapper.getByUrl(url);
	}

	@Override
	public MenuType getTypeById(int id) {
		LambdaQueryWrapper<Menus> lqw = new LambdaQueryWrapper<Menus>();
		lqw.select(Menus::getType).eq(Menus::getId, id);
		List<Object> objs = this.baseMapper.selectObjs(lqw);
		if (objs.size() != 0) {
			Integer typeNum = (Integer) objs.get(0);
			var enums = MenuType.class.getEnumConstants();
			return typeNum > enums.length ? null : enums[typeNum];
		}
		return null;
	}

	@Override
	public Integer getPidById(Integer id) {
		LambdaQueryWrapper<Menus> lqw = new LambdaQueryWrapper<Menus>();
		lqw.select(Menus::getPid).eq(Menus::getId, id);
		List<Object> objs = this.baseMapper.selectObjs(lqw);
		if (objs.size() != 0) {
			return (Integer) objs.get(0);
		}
		return null;
	}

	@Override
	public boolean checkTypeAssoc(MenuType pType, MenuType curType) {
		switch (pType) {
			case NONE:
			case MENU:
				if (curType == MenuType.MENU || curType == MenuType.ITEM
						|| curType == MenuType.OPER) { /// change
					return true;
				}
				break;
			case ITEM:
				if (curType == MenuType.PAGE || curType == MenuType.OPER) {
					return true;
				}
				break;
			case PAGE:
				if (curType == MenuType.OPER) {
					return true;
				}
				break;
			default:
				return false;
		}
		return false;
	}

	@Override
	public Byte getLevel(Integer pid) {
		Byte level = 1;

		if (pid != null) {
			while ((pid = this.getPidById(pid)) != null) {
				++level;
			}
		}
		return level;
	}

	@Override
	public boolean hasCircularRelationship(Integer id, Integer pid) {
		while (pid != null) {
			pid = this.getPidById(pid);
			if (pid == id) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isAuthorized(Menus menu) throws NullRequestException {
		if (!menu.getStatus()) {
			return false;
		}

		RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
		if (null == attributes) {
			throw new NullRequestException();
		}

		Integer mid = menu.getId();
		Long uid = LoginInfomationUtil.getUserId();
		Boolean authorize = userPermAssocMapper.isAuthorized(uid, mid);
		if (null != authorize) {
			return authorize;
		}

		List<Byte> roles = userRoleAssocMapper.getRoles(uid);
		for (Byte rid : roles) {
			if (rolePermAssocMapper.isAuthorized(rid, mid)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public List<OperButton> getAuthorizedOperationPermissions(Long uid, MenusView menu) {
		return this.baseMapper.getAuthorizedOperationPermissions(uid, menu);
	}

	@Override
	public Result index(IndexParam params)
			throws IllegalAccessException, NullRequestException, PermissionNotFoundException {
		var lmqw = Wrappers.lambdaQuery(MenusView.class);
		lmqw.orderByAsc(MenusView::getPid).orderByAsc(MenusView::getType);

		params.type = IndexType.All;
		Result result = super.index(params, lmqw);
		if (!result.isSuccess() || params.initPageSize == null) {
			return result;
		}

		@SuppressWarnings("unchecked")
		WebPageInfo<MenusView> info = result.getData(WebPageInfo.class);

		LambdaQueryWrapper<Permissions> lqw = Wrappers
				.lambdaQuery(Permissions.class)
				.select(Permissions::getName, Permissions::getId, Permissions::getGuid);
		info.extraData = new HashMap<>();
		info.extraData.put("permissions", permissionsMapper.selectMaps(lqw));
		return result;
	}

	@Override
	public Result update(MenusView param) {
		Menus menu = new Menus();
		BeanUtils.copyProperties(param, menu);

		Result result = new Result();
		if (EntityUtil.IsNonOrEmpty(menu, "id")) {
			result.setMsg("修改内容为空！");
			return result;
		}

		Menus oldMenu = this.getById(menu.getId());
		if (null == oldMenu) {
			result.setMsg("权限不存在!");
			return result;
		}

		// set oper_type
		if (menu.getOper_type() != null) {
			if (oldMenu.getType() != MenuType.OPER) {
				result.setMsg("非操作类型的菜单不能设置操作方式！");
				return result;
			}
			// unset associated permission, pmid = 0
			if (menu.getPmid() != null && menu.getPmid() == 0) {
			}
			// pmid != 0
			else if (menu.getPmid() != null || oldMenu.getPmid() != null) {
				result.setMsg("关联权限时，不能设置操作类型！");
				return result;
			}
		} else {
			menu.setOper_type(menu.getPmid() != null ? null : oldMenu.getOper_type());
		}

		if (menu.getPmid() == null || menu.getPmid() == 0) {
			menu.setPmid(menu.getPmid() != null ? null : oldMenu.getPmid());
		}
		if (menu.getPid() != null && menu.getPid() == 0) {
			menu.setPid(null);
		}

		MenuType pType = MenuType.NONE;
		Integer pid = menu.getPid() == null ? oldMenu.getPid() : menu.getPid();
		if (pid != null) {
			MenuType tempType = this.getTypeById(pid);
			if (tempType == null) {
				result.setMsg("关联父权限不存在！");
				return result;
			}
			pType = tempType;
		}

		MenuType curType = menu.getType() != null ? menu.getType() : oldMenu.getType();
		result.setSuccess(this.checkTypeAssoc(pType, curType));
		if (!result.isSuccess()) {
			result.setMsg("权限类型关联错误!");
			return result;
		}

		result.setSuccess(!this.hasCircularRelationship(menu.getId(), pid));
		if (!result.isSuccess()) {
			result.setMsg("父子权限关联错误!");
			return result;
		}

		if (menu.getUrl() != null) {
			if (!menu.getUrl().equals("")) {
				Integer refMenuId = this.getObj(this.lambdaQuery()
						.select(Menus::getId)
						.eq(Menus::getUrl, menu.getUrl()).getWrapper(), v -> (Integer) v);
				if (refMenuId != null) {
					menu.setUrl(null);
					menu.setRef_id(refMenuId);
				} else {
					menu.setRef_id(null);
				}
			} else {
				menu.setUrl(null);
				if (oldMenu.getRef_id() != null) {
					menu.setRef_id(menu.getRef_id());
				}
			}
		} else {
			menu.setUrl(oldMenu.getUrl());
			menu.setRef_id(oldMenu.getRef_id());
		}

		if (menu.getStatus() != null) {
			LambdaQueryWrapper<Menus> qw = new LambdaQueryWrapper<>();
			qw.select(Menus::getId, Menus::getPid)
					.eq(Menus::getPid, menu.getId());

			List<Menus> revising = new ArrayList<>();
			revising.add(menu);

			func = (Menus child) -> {
				child.setStatus(menu.getStatus());
				revising.add(child);

				qw.clear();
				qw.select(Menus::getId, Menus::getPid)
						.eq(Menus::getPid, child.getId());

				List<Menus> children = this.list(qw);

				if (children.size() != 0) {
					children.forEach(func);
				}
			};
			this.list(qw).forEach(func);

			Menus temp = menu;
			if (menu.getStatus()) {
				while (true) {
					qw.clear();
					qw.select(Menus::getId, Menus::getPid, Menus::getStatus)
							.eq(Menus::getId, temp.getPid());

					temp = this.getOne(qw);
					if (temp == null || temp.getStatus()) {
						break;
					}
					temp.setStatus(true);
					revising.add(temp);
				}
			}

			result.setSuccess(this.updateBatchById(revising));
		} else {
			result.setSuccess(this.updateById(menu));
		}
		return result;
	}

	@Override
	public Result add(MenusView param) {
		Menus menu = new Menus();
		BeanUtils.copyProperties(param, menu);

		Result result = new Result();
		if (menu.getUrl() != null) {
			if (menu.getUrl().equals("")) {
				menu.setUrl(null);
			} else {
				LambdaQueryWrapper<Menus> lqw = Wrappers.lambdaQuery(Menus.class);
				lqw.select(Menus::getId).eq(Menus::getUrl, menu.getUrl());

				Integer refMenuId = this.getObj(lqw, v -> (Integer) v);
				if (refMenuId != null) {
					menu.setUrl(null);
					menu.setRef_id(refMenuId);
				}
			}
		}

		if (menu.getPid() != null && menu.getPid() == 0) {
			menu.setPid(null);
		}

		Integer id = menu.getId();
		boolean isSameIdMenuExists = this.lambdaQuery().eq(Menus::getId, id).exists();
		if (isSameIdMenuExists) {
			result.setMsg("权限已存在!");
			return result;
		}

		boolean isSiblingSameNameMenuExists = this.lambdaQuery()
				.eq(menu.getPid() != null, Menus::getPid, menu.getPid())
				.isNull(menu.getPid() == null, Menus::getPid)
				.eq(Menus::getName, menu.getName())
				.exists();
		if (isSiblingSameNameMenuExists) {
			result.setMsg("具有相同名称的同级菜单已存在，无法重复添加！");
			return result;
		}

		MenuType type = menu.getType();
		MenuType pType = null == menu.getPid() ? MenuType.NONE : this.getTypeById(menu.getPid());
		result.setSuccess(this.checkTypeAssoc(pType, type));
		if (!result.isSuccess()) {
			result.setMsg("权限关联错误!");
			return result;
		}

		if (menu.getOper_type() != null && menu.getOper_type() == OperType.NONE) {
			menu.setOper_type(null);
		}
		if (menu.getPmid() != null && menu.getPmid() == 0) {
			menu.setPmid(null);
		}
		result.setSuccess(this.save(menu));
		if (result.isSuccess()) {
			result.setData(menu);
		}
		return result;

	}

	@Override
	public Result delete(Long id) {
		Result result = new Result();

		if (id < 10000) {
			result.fail("无法删除内置菜单！");
			return result;
		}

		boolean exists = this.lambdaQuery().eq(Menus::getId, id).exists();
		if (!exists) {
			result.fail("菜单不存在！");
			return result;
		}

		result.setSuccess(this.removeById(id));
		if (!result.isSuccess()) {
			result.setMsg("删除菜单失败！");
		}
		return result;
	}
}

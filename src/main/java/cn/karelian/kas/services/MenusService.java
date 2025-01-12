package cn.karelian.kas.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
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
import cn.karelian.kas.utils.MybatisPlusUtil;
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
				.select(Permissions::getName, Permissions::getId, Permissions::getOper_id);
		info.extraData = new HashMap<>();
		info.extraData.put("permissions", permissionsMapper.selectMaps(lqw));
		return result;
	}

	@Override
	public Result update(MenusView param) {
		Menus menu = new Menus();
		BeanUtils.copyProperties(param, menu, "type", "ref_id");

		Result result = new Result();
		if (EntityUtil.IsNonOrEmpty(menu, "id")) {
			result.setMsg("修改内容为空！");
			return result;
		}

		Menus oldMenu = this.getById(menu.getId());
		if (null == oldMenu) {
			result.setMsg("菜单不存在!");
			return result;
		}

		if (menu.getPmid() != null && oldMenu.getType() == MenuType.MENU) {
			result.setMsg("非操作类型的菜单不能设置关联权限！");
			return result;
		}

		// update target menu
		var uw = Wrappers.<Menus>lambdaUpdate().eq(Menus::getId, menu.getId());

		// if target menu type is MENU, then pmid and oper_type must be null
		// the reason why ITEM and PAGE can set pmid and oper_type is that it can be a
		// single menu that specify a url to display and query
		if (menu.getPmid() != null) {
			if (menu.getPmid() == 0) {
				menu.setPmid(null);
				if (oldMenu.getPmid() != null) {
					uw.set(Menus::getPmid, null);
				}
			} else if (oldMenu.getOper_type() != null) {
				param.setOper_type(OperType.NONE); // reutrn to client, to partial update
			}

			if (menu.getOper_type() != null && menu.getOper_type() != OperType.NONE) {
				result.fail("关联权限时，不能设置操作类型！");
				return result;
			}
		}
		if (menu.getOper_type() != null) {
			if (menu.getOper_type() == OperType.NONE) {
				menu.setOper_type(null);
				if (oldMenu.getOper_type() != null) {
					uw.set(Menus::getOper_type, null);
				}
			}

			if (oldMenu.getType() != MenuType.OPER) {
				result.fail("非操作类型的菜单不能设置操作方式！");
				return result;
			}

			if (menu.getPmid() == null && oldMenu.getPmid() != null) {
				result.fail("关联权限时，不能设置操作类型！");
				return result;
			}
		}

		// check the target menu and its parent menu type association;
		// check the target menu and its parent menu circular relationship
		if (menu.getPid() != null) {
			MenuType pType = MenuType.NONE;
			if (menu.getPid() != 0) {
				MenuType tempType = this.getTypeById(menu.getPid());
				if (tempType == null) {
					result.setMsg("关联父权限不存在！");
					return result;
				}
				pType = tempType;
			} else {
				uw.set(Menus::getPid, null);
				menu.setPid(null);
			}
			MenuType curType = menu.getType() != null ? menu.getType() : oldMenu.getType();
			result.setSuccess(this.checkTypeAssoc(pType, curType));
			if (!result.isSuccess()) {
				result.setMsg("权限类型关联错误!");
				return result;
			}

			// circular relationship check
			result.setSuccess(!this.hasCircularRelationship(menu.getId(), menu.getPid()));
			if (!result.isSuccess()) {
				result.setMsg("父子权限关联错误!");
				return result;
			}
		}

		// if update url isn't empty, first find whether the url exists
		// if exists, set ref_id to the url's id and set url to null
		// if not exists, set ref_id to null and set url to the target menu
		while (menu.getUrl() != null) {
			if (!menu.getUrl().equals("")) {
				Integer refMenuId = this.getObj(this.lambdaQuery()
						.select(Menus::getId)
						.eq(Menus::getUrl, menu.getUrl()).getWrapper(), v -> (Integer) v);
				if (refMenuId != null) {
					menu.setUrl(null);
					menu.setRef_id(refMenuId);
					if (oldMenu.getUrl() != null) {
						uw.set(Menus::getUrl, null);
					}

					// reutrn to client, to partial update
					param.setUrl("");
					param.setRef_id(refMenuId);
					break;
				}
			} else {
				menu.setUrl(null);
				if (oldMenu.getUrl() != null) {
					uw.set(Menus::getUrl, null);
				}
			}

			if (oldMenu.getRef_id() != null) {
				uw.set(Menus::getRef_id, null);
				param.setRef_id(0); // reutrn to client, to partial update
			}
			break;
		}

		// if update status, update all children status, if status is true, update its
		// all parent status
		if (menu.getStatus() != null) {
			// whether update status is true or false, all children status should be
			// updated, so first find all children and add its id to updateMenuStatusIds
			LambdaQueryWrapper<Menus> qw = new LambdaQueryWrapper<>();
			qw.select(Menus::getId, Menus::getPid)
					.eq(Menus::getStatus, !menu.getStatus())
					.eq(Menus::getPid, menu.getId());

			List<Integer> updateMenuStatusIds = new ArrayList<>();
			updateMenuStatusIds.add(menu.getId());

			// recursive find all children and add its id to updateMenuStatusIds
			func = (Menus child) -> {
				updateMenuStatusIds.add(child.getId());

				qw.clear();
				qw.select(Menus::getId, Menus::getPid)
						.eq(Menus::getPid, child.getId());

				this.list(qw).forEach(func);
			};
			this.list(qw).forEach(func);

			// if update status is true, find all parent and add its id to
			// updateMenuStatusIds until find a parent which status is true
			Menus temp = menu;
			temp.setPid(menu.getPid() != null ? menu.getPid() : oldMenu.getPid());
			while (temp != null && temp.getStatus()) {
				qw.clear();
				qw.select(Menus::getId, Menus::getPid, Menus::getStatus)
						.eq(Menus::getStatus, false)
						.eq(Menus::getId, temp.getPid());

				temp = this.getOne(qw);
				if (temp != null) {
					updateMenuStatusIds.add(temp.getId());
				}
			}

			// update all menus' status
			var bluw = Wrappers.lambdaUpdate(Menus.class)
					.set(Menus::getStatus, menu.getStatus())
					.set(Menus::getUpdate_time, LocalDateTime.now())
					.in(Menus::getId, updateMenuStatusIds);
			menu.setStatus(null);
			result.setSuccess(this.update(bluw));
		}

		// update target menu
		if (MybatisPlusUtil.applyNonNullUpdateFields(menu, uw, Menus::getId) || !ObjectUtils.isEmpty(uw.getSqlSet())) {
			result.setSuccess(this.update(uw));
		}

		result.setData(param);
		return result;
	}

	@Override
	public Result add(MenusView param) {
		Result result = new Result();
		Menus menu = new Menus();
		BeanUtils.copyProperties(param, menu, "id");

		// if update url isn't empty, first find whether the url exists
		// if exists, set ref_id to the url's id and set url to null
		// if not exists, set ref_id to null and set url to the target menu
		if (menu.getUrl() != null) {
			if (!menu.getUrl().equals("")) {
				Integer refMenuId = this.getObj(this.lambdaQuery()
						.select(Menus::getId)
						.eq(Menus::getUrl, menu.getUrl()).getWrapper(), v -> (Integer) v);
				if (refMenuId != null) {
					menu.setUrl(null);
					menu.setRef_id(refMenuId);
				}
			} else {
				menu.setUrl(null);
			}
		}

		if (menu.getPid() != null && menu.getPid() == 0) {
			menu.setPid(null);
		}

		boolean isSiblingSameNameMenuExists = this.lambdaQuery()
				.eq(menu.getPid() != null, Menus::getPid, menu.getPid())
				.isNull(menu.getPid() == null, Menus::getPid)
				.eq(Menus::getName, menu.getName())
				.exists();
		if (isSiblingSameNameMenuExists) {
			result.fail("具有相同名称的同级菜单已存在，无法重复添加！");
			return result;
		}

		Menus parentMenu = null;
		if (menu.getPid() != null) {
			parentMenu = this.getById(menu.getPid());
			if (parentMenu == null) {
				result.fail("指定添加的父菜单不存在！");
				return result;
			}
		}

		// check the target menu and its parent menu type association
		MenuType type = menu.getType();
		MenuType pType = parentMenu == null ? MenuType.NONE : parentMenu.getType();
		result.setSuccess(this.checkTypeAssoc(pType, type));
		if (!result.isSuccess()) {
			result.setMsg("权限关联错误!");
			return result;
		}

		// if target menu type is MENU, then pmid and oper_type must be null
		// the reason why ITEM and PAGE can set pmid and oper_type is that it can be a
		// single menu that specify a url to display and query
		if (menu.getPmid() != null) {
			if (menu.getPmid() == 0) {
				menu.setPmid(null);
			} else if (menu.getType() == MenuType.MENU) {
				result.fail("菜单类型的菜单不能设置关联权限！");
				return result;
			}

			if (menu.getOper_type() != null && menu.getOper_type() != OperType.NONE) {
				result.fail("关联权限时，不能设置操作类型！");
				return result;
			}
		}

		if (menu.getOper_type() != null) {
			if (menu.getOper_type() == OperType.NONE) {
				menu.setOper_type(null);
			} else if (menu.getType() == MenuType.OPER) {
				result.fail("非操作类型的菜单不能设置操作方式！");
				return result;
			}
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

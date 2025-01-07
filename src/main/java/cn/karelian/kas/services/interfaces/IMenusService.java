package cn.karelian.kas.services.interfaces;

import java.util.List;

import cn.karelian.kas.Result;
import cn.karelian.kas.entities.Menus;
import cn.karelian.kas.entities.Menus.MenuType;
import cn.karelian.kas.exceptions.NullRequestException;
import cn.karelian.kas.exceptions.UnLoginException;
import cn.karelian.kas.utils.OperButton;
import cn.karelian.kas.views.MenusView;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author Karelian_na
 * @since 2023-11-09
 */
public interface IMenusService extends IKasService<Menus, MenusView> {
	/**
	 * 获取指定url的菜单
	 * 
	 * @param url 将要获取的url的菜单
	 * @return
	 */
	public MenusView getByUrl(String url);

	/**
	 * @description: 获取指定权限id的权限的类型
	 * @param {int} id
	 * @return {MenuType}
	 */
	public MenuType getTypeById(int id);

	// /**
	// * @description: 获取指定权限id的父权限的类型
	// * @param {Integer} id
	// * @return {Byte}
	// */
	// public Byte getPTypeById(Integer id);

	/**
	 * @description: 获取指定权限id的父权限id
	 * @param {Integer} id
	 * @return {Interger}
	 */
	public Integer getPidById(Integer id);

	/**
	 * check the target menu and its parent menu type association
	 * 
	 * <p> rules are as follows:
	 * <p> if parent menu is null, then target menu type can be MENU, ITEM;
	 * <p> if parent menu is MENU, then target menu type can be MENU, ITEM, OPER;
	 * <p> if parent menu is ITEM, then target menu type can be PAGE, OPER;
	 * <p> if parent menu is PAGE, then target menu type can be OPER;
	 * <p> OPER menu type can not have children;
	 * 
	 * @param {MenuType} pType the parent menu's type
	 * @param {MenuType} curType the target menu's type
	 * @return {Boolean}
	 */
	public boolean checkTypeAssoc(MenuType pType, MenuType curType);

	/**
	 * @description: 获取给定权限id的权限的层级
	 * @param {Integer} pid 检测权限的id
	 * @return {Boolean}
	 */
	public Byte getLevel(Integer pid);

	/**
	 * @description: 检测给定权限id与父id是否有循环关系
	 * @param {Integer} id 检测权限的id
	 * @param {Integer} pid 检测权限的pid
	 * @return {Boolean}
	 */
	public boolean hasCircularRelationship(Integer id, Integer pid);

	/**
	 * 用给定变更来修改菜单
	 * 
	 * @param menu 将要修改的相关属性
	 * @return
	 */
	public Result update(MenusView menu);

	/**
	 * 用给定变更来添加菜单
	 * 
	 * @param menu 将要添加的菜单
	 * @return
	 */
	public Result add(MenusView menu) throws NullRequestException;

	/**
	 * 删除指定Id的菜单
	 * 
	 * @param id
	 * @return
	 */
	public Result delete(Long id);

	/**
	 * @description: 检测当前用户是否已被授权给定id的权限
	 * @param {Integer} id 检测权限的id
	 * @return {Boolean}
	 * @throws NullRequestException
	 * @throws UnLoginException
	 */
	public boolean isAuthorized(Menus menu) throws NullRequestException, UnLoginException;

	/**
	 * 获取 指定菜单id的 授权的操作权限
	 * 
	 * @param uid
	 * @param permission
	 * @return
	 */
	public List<OperButton> getAuthorizedOperationPermissions(Long uid, MenusView menu);
}

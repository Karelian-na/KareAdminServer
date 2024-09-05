package cn.karelian.kas.services.interfaces;

import java.util.List;

import cn.karelian.kas.Result;
import cn.karelian.kas.entities.Menus;
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
	 * @return {byte}
	 */
	public Byte getTypeById(int id);

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
	 * @description: 检测权限类型关联是否正确
	 * @param {byte} pType 父权限的类型
	 * @param {byte} curType 子权限的类型
	 * @return {Boolean}
	 */
	public boolean checkTypeAssoc(byte pType, byte curType);

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
	public Result update(Menus menu);

	/**
	 * 用给定变更来添加菜单
	 * 
	 * @param menu 将要添加的菜单
	 * @return
	 */
	public Result add(Menus menu) throws NullRequestException;

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

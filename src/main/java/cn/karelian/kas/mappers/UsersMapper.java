package cn.karelian.kas.mappers;

import java.util.List;

import cn.karelian.kas.entities.Users;
import cn.karelian.kas.views.MenusView;
import cn.karelian.kas.views.UsermsgsView;

/**
 * <p>
 * 管理用户的表 Mapper 接口
 * </p>
 *
 * @author Karelian_na
 * @since 2023-08-28
 */
public interface UsersMapper extends KasMapper<Users, UsermsgsView> {
	/**
	 * 获取已授权的菜单Id
	 * 
	 * @param uid 将要获取的用户的id
	 * @return
	 */
	public List<Integer> getAuthorizedMenuIds(Long uid);

	/**
	 * 获取已授权的菜单（不包含详细信息）用于授权
	 * 
	 * @param uid
	 * @return
	 */
	public List<MenusView> getAuthorizedMenusWithoutInfo(Long uid);

	/**
	 * 获取已授权的菜单（包含详细信息）用于菜单构建
	 * 
	 * @param uid
	 * @return
	 */
	public List<MenusView> getAuthorizedMenus(Long uid);
}

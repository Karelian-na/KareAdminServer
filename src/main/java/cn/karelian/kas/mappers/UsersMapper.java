package cn.karelian.kas.mappers;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.conditions.Wrapper;

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
	 * 获取给定Id的用户，包含逻辑删除的用户
	 * 
	 * @param id 将要获取的用户的Id
	 * @return
	 */
	public Users getUserWithLogicDelete(long id, @Param("ew") Wrapper<Users> ew);

	/**
	 * 删除给定用户Id的用户
	 * 
	 * @param ids 将要删除的用户的Id
	 * @return
	 */
	public boolean delete(List<Long> ids, int delete_type);

	/**
	 * 恢复删除/注销的用户
	 * 
	 * @param ids 将要恢复的用户的Id
	 * @return
	 */
	public boolean restoreDeletedUsers(List<Long> ids);

	/**
	 * 持久删除给定的用户（数据库删除）
	 * 
	 * @param ids 将要删除的用户的Id
	 * @return
	 */
	public boolean deleteUsersPermanently(List<Long> ids);

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

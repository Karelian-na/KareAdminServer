package cn.karelian.kas.mappers;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.conditions.Wrapper;

import cn.karelian.kas.entities.Roles;
import cn.karelian.kas.views.MenusView;

/**
 * <p>
 * 管理角色的表 Mapper 接口
 * </p>
 *
 * @author Karelian_na
 * @since 2023-08-28
 */
public interface RolesMapper extends KasMapper<Roles, Roles> {
	@Select("SELECT mid FROM role_perm_assoc WHERE rid = #{rid}")
	List<Integer> getAuthorizedMenuIds(Integer rid);

	List<MenusView> getUserAssociatedAuthorizedMenus(Long uid);

	/**
	 * get roles info for map, only include id and name
	 * 
	 * @param uid the user id
	 * @return
	 */
	public List<Roles> getUserAssocRolesForMap(Long uid);

	/**
	 * get specify user's assignable roles for map, can specify select condition by
	 * wrapper
	 * 
	 * @param uid     the user id
	 * @param wrapper the select condition wrapper
	 * @return
	 */
	public List<Roles> getUserAssignableRolesForMap(Long uid, @Param("ew") Wrapper<Roles> wrapper);

	/**
	 * get specify user's max associated roles' max level
	 * 
	 * @param uid the user id
	 * @return
	 */
	public Byte getUserAssocRolesTopLevel(Long uid);
}

package cn.karelian.kas.mappers;

import java.util.List;

import org.apache.ibatis.annotations.Select;

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
}

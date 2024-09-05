package cn.karelian.kas.mappers;

import java.util.List;

import org.apache.ibatis.annotations.Select;

import cn.karelian.kas.entities.Menus;
import cn.karelian.kas.utils.OperButton;
import cn.karelian.kas.views.MenusView;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author Karelian_na
 * @since 2023-11-09
 */
public interface MenusMapper extends KasMapper<Menus, MenusView> {
	/**
	 * @description: 获取指定url的菜单
	 * @param url
	 * @return {Permissions}
	 */
	@Select("SELECT * FROM menus_view WHERE url = #{url}")
	public MenusView getByUrl(String url);

	/**
	 * 获取 指定菜单id的 授权的操作权限
	 * 
	 * @param uid
	 * @param permission
	 * @return
	 */
	public List<OperButton> getAuthorizedOperationPermissions(Long uid, MenusView menu);
}

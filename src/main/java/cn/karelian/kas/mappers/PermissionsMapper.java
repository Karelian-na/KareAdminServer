package cn.karelian.kas.mappers;

import org.apache.ibatis.annotations.Select;

import cn.karelian.kas.entities.Permissions;
import cn.karelian.kas.views.PermissionsView;

/**
 * <p>
 * 管理权限目录的表 Mapper 接口
 * </p>
 *
 * @author Karelian_na
 * @since 2023-08-28
 */
public interface PermissionsMapper extends KasMapper<Permissions, PermissionsView> {
	/**
	 * @description: 获取指定url的权限
	 * @param {String} url
	 * @return {Permissions}
	 */
	@Select("SELECT * FROM permissions WHERE url = '${url}'")
	public Permissions getByUrl(String url);

	public Byte getTypeById(long id);

}

package cn.karelian.kas.mappers;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

import cn.karelian.kas.entities.RolePermAssoc;

/**
 * <p>
 * 角色权限关联表 Mapper 接口
 * </p>
 *
 * @author Karelian_na
 * @since 2023-08-28
 */
public interface RolePermAssocMapper extends KasMapper<RolePermAssoc, RolePermAssoc> {
	@Select("SELECT COUNT(*) FROM role_perm_assoc WHERE rid = #{rid} AND mid = #{mid}")
	boolean isAuthorized(byte rid, int mid);

	@Insert("INSERT INTO role_perm_assoc(rid, mid) VALUES(#{rid}, #{mid})")
	boolean insertByUnionKey(byte rid, int mid);

	@Delete("DELETE FROM role_perm_assoc WHERE (rid = #{rid} AND mid = #{mid})")
	boolean deleteByUnionKey(byte rid, int mid);
}

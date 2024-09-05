package cn.karelian.kas.mappers;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

import cn.karelian.kas.entities.UserRoleAssoc;

/**
 * <p>
 * 用户角色关联表 Mapper 接口
 * </p>
 *
 * @author Karelian_na
 * @since 2023-08-28
 */
public interface UserRoleAssocMapper extends KasMapper<UserRoleAssoc, UserRoleAssoc> {
	@Delete("DELETE FROM user_role_assoc WHERE uid = #{uid} AND rid = #{rid}")
	public boolean deleteByUnionKey(Long uid, Byte rid);

	@Insert("INSERT INTO user_role_assoc VALUES(#{uid}, #{rid})")
	public boolean insertByUnionKey(Long uid, Byte rid);

	public boolean insertBatchByUnionKey(Map<String, List<?>> params);

	@Select("SELECT rid FROM user_role_assoc WHERE uid = #{uid}")
	public List<Byte> getRoles(Long uid);
}

package cn.karelian.kas.mappers;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

import cn.karelian.kas.entities.UserPermAssoc;

/**
 * <p>
 * 用户权限关联表 Mapper 接口
 * </p>
 *
 * @author Karelian_na
 * @since 2023-08-28
 */
public interface UserPermAssocMapper extends KasMapper<UserPermAssoc, UserPermAssoc> {
	// void removeBatchByUnionKey(List<Map<String, Object>> data);
	@Delete("DELETE FROM user_perm_assoc WHERE (uid = #{uid} AND mid = #{mid})")
	boolean deleteByUnionKey(long uid, int mid);

	@Insert("INSERT INTO user_perm_assoc(uid, mid, authorize) VALUES(#{uid}, #{mid}, #{authorize}) ON DUPLICATE KEY UPDATE authorize = #{authorize}")
	boolean insertOrUpdateByUnionKey(long uid, int mid, Byte authorize);

	@Select("SELECT authorize FROM user_perm_assoc WHERE uid = #{uid} AND mid = #{mid}")
	Boolean isAuthorized(long uid, int mid);
}

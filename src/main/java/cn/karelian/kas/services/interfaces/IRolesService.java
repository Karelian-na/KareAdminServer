package cn.karelian.kas.services.interfaces;

import cn.karelian.kas.Result;
import cn.karelian.kas.dtos.AuthorizeParam;
import cn.karelian.kas.entities.Roles;
import cn.karelian.kas.exceptions.TransactionFailedException;

/**
 * <p>
 * 管理角色的表 服务类
 * </p>
 *
 * @author Karelian_na
 * @since 2023-08-28
 */
public interface IRolesService extends IKasService<Roles, Roles> {
	public Result add(Roles role);

	public Result edit(Roles role);

	public Result authorizeindex(Integer id, Boolean all);

	public Result authorize(AuthorizeParam params) throws TransactionFailedException;

	public Result delete(Integer id);
}

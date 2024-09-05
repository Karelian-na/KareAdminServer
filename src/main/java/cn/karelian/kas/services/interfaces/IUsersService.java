package cn.karelian.kas.services.interfaces;

import java.util.List;

import cn.karelian.kas.Result;
import cn.karelian.kas.dtos.AssignRoleParam;
import cn.karelian.kas.dtos.AuthorizeParam;
import cn.karelian.kas.dtos.RevisePasswordParam;
import cn.karelian.kas.entities.Users;
import cn.karelian.kas.exceptions.NullRequestException;
import cn.karelian.kas.exceptions.TransactionFailedException;
import cn.karelian.kas.exceptions.UnLoginException;
import cn.karelian.kas.views.UsermsgsView;

/**
 * <p>
 * 管理用户的表 服务类
 * </p>
 *
 * @author Karelian_na
 * @since 2023-08-28
 */
public interface IUsersService extends IKasService<Users, UsermsgsView> {
	public Result selfindex() throws NullRequestException;

	public Result edit(UsermsgsView usermsg, boolean self) throws TransactionFailedException;

	public Result add(UsermsgsView usermsg) throws TransactionFailedException;

	public Result reset(List<Long> ids);

	public Result authorize(AuthorizeParam params) throws TransactionFailedException;

	public Result authorizeindex(Long id, Boolean all) throws NullRequestException;

	public Result selfreset(RevisePasswordParam params) throws NullRequestException, UnLoginException;

	public Result asignindex(List<Long> ids);

	public Result asign(AssignRoleParam params) throws NullRequestException, TransactionFailedException;

	public Result getverifies(String account) throws NullRequestException;
}

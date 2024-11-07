package cn.karelian.kas.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import cn.karelian.kas.KasApplication;
import cn.karelian.kas.Result;
import cn.karelian.kas.codes.FieldErrors;
import cn.karelian.kas.dtos.AuthorizeParam;
import cn.karelian.kas.dtos.IndexParam;
import cn.karelian.kas.entities.Roles;
import cn.karelian.kas.exceptions.NullRequestException;
import cn.karelian.kas.exceptions.IllegalAccessException;
import cn.karelian.kas.exceptions.PermissionNotFoundException;
import cn.karelian.kas.exceptions.TransactionFailedException;
import cn.karelian.kas.mappers.RolePermAssocMapper;
import cn.karelian.kas.mappers.RolesMapper;
import cn.karelian.kas.services.interfaces.IRolesService;
import cn.karelian.kas.utils.EntityUtil;
import cn.karelian.kas.utils.LoginInfomationUtil;

/**
 * <p>
 * 管理角色的表 服务实现类
 * </p>
 *
 * @author Karelian_na
 * @since 2023-08-28
 */
@Service
public class RolesService extends KasService<RolesMapper, Roles, Roles> implements IRolesService {
	@Autowired
	private RolePermAssocMapper rolePermAssocMapper;

	@Override
	public Result index(IndexParam params)
			throws IllegalAccessException, NullRequestException, PermissionNotFoundException {
		Long uid = LoginInfomationUtil.getUserId();
		var lqw = Wrappers.lambdaQuery(Roles.class).gt(Roles::getLevel, baseMapper.getUserAssocRolesTopLevel(uid));
		Result result = super.index(params, lqw);
		return result;
	}

	@Override
	public Result add(Roles role) {
		Result result = new Result();

		Long uid = LoginInfomationUtil.getUserId();
		Byte currentUsersRoleTopLevel = baseMapper.getUserAssocRolesTopLevel(uid);

		if (role.getLevel() <= currentUsersRoleTopLevel) {
			result.setMsg("无法添加比自己角色级别更高的角色！");
			return result;
		}

		result.setSuccess(super.save(role));
		if (result.isSuccess()) {
			result.setData(role);
		}

		return result;
	}

	@Override
	public Result edit(Roles role) {
		Result result = new Result();

		Roles targetRole = this.lambdaQuery().select(Roles::getId, Roles::getLevel).eq(Roles::getId, role.getId())
				.one();
		if (null == targetRole) {
			result.setMsg("角色不存在!");
			return result;
		}

		Byte level = ObjectUtils.isEmpty(role.getLevel()) ? targetRole.getLevel() : role.getLevel();
		Long uid = LoginInfomationUtil.getUserId();
		Byte currentUsersRoleTopLevel = baseMapper.getUserAssocRolesTopLevel(uid);
		if (level <= currentUsersRoleTopLevel) {
			result.setMsg("无法提升该角色级别！");
			return result;
		}

		result.setSuccess(super.updateById(role));
		if (result.isSuccess()) {
			result.setData(EntityUtil.ToMap(role));
		}
		return result;
	}

	@Override
	public Result authorizeindex(Integer id, Boolean all) {
		Result result = new Result();
		if (id == KasApplication.superAdminRoleId || id == KasApplication.commonUserRoleId) {
			result.setMsg("不能授权此角色!");
			return result;
		}

		Roles targetRole = this.lambdaQuery().select(Roles::getLevel).eq(Roles::getId, id).one();
		if (null == targetRole) {
			result.setMsg("角色不存在!");
			return result;
		}

		Long uid = LoginInfomationUtil.getUserId();
		Byte topRoleLevel = baseMapper.getUserAssocRolesTopLevel(uid);
		if (null != targetRole.getLevel() && targetRole.getLevel() <= topRoleLevel) {
			result.setMsg("无法授权比自己角色级别更高的角色！");
			return result;
		}

		AuthorizeData authorizeData = new AuthorizeData();
		authorizeData.auth = baseMapper.getAuthorizedMenuIds(id);
		if (all) {
			authorizeData.all = baseMapper.getUserAssociatedAuthorizedMenus(uid);
		}

		return new Result(true, authorizeData);
	}

	@Override
	@Transactional(rollbackFor = TransactionFailedException.class)
	public Result authorize(AuthorizeParam params) throws TransactionFailedException {
		Result result = new Result();
		if (params.id == KasApplication.superAdminRoleId || params.id == KasApplication.commonUserRoleId) {
			result.setMsg("无法授权该角色!");
			return result;
		}

		Roles targetRole = this.lambdaQuery().select(Roles::getLevel).eq(Roles::getId, params.id).one();
		if (null == targetRole) {
			result.setMsg("角色不存在!");
			return result;
		}

		Long uid = LoginInfomationUtil.getUserId();
		Byte topRoleLevel = baseMapper.getUserAssocRolesTopLevel(uid);
		if (null != targetRole.getLevel() && targetRole.getLevel() <= topRoleLevel) {
			result.setMsg("无法授权比自己角色级别更高的角色！");
			return result;
		}

		if (params.auths.size() == 0) {
			return Result.fieldError("auths", FieldErrors.EMPTY);
		}

		for (Integer pid : params.auths.keySet()) {
			Byte auth = params.auths.get(pid);
			switch (auth) {
				case 0:
					if (!rolePermAssocMapper.deleteByUnionKey(params.id.byteValue(), pid)) {
						throw new TransactionFailedException(result);
					}
					break;
				case 1:
					if (!rolePermAssocMapper.insertByUnionKey(params.id.byteValue(), pid)) {
						throw new TransactionFailedException(result);
					}
					break;
				default:
					break;
			}
		}

		result.setSuccess(true);
		return result;
	}

	@Override
	public Result delete(Byte id) {
		Result result = new Result();
		if (id == KasApplication.superAdminRoleId || id == KasApplication.adminRoleId
				|| id == KasApplication.commonUserRoleId) {
			result.setMsg("不能删除该角色!");
			return result;
		}

		result.setSuccess(super.removeById(id));
		return result;
	}
}

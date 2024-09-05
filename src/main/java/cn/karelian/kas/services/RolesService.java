package cn.karelian.kas.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.karelian.kas.Result;
import cn.karelian.kas.codes.FieldErrors;
import cn.karelian.kas.dtos.AuthorizeParam;
import cn.karelian.kas.entities.Roles;
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

	private Result checkFields(Roles role, boolean add) {
		FieldErrors err = EntityUtil.CheckStringField(role.getName(), 1, 20, add);
		if (err != null) {
			return Result.fieldError("name", err);
		}

		err = EntityUtil.CheckNumberField(role.getLevel(), (byte) 2, (byte) 100, add);
		if (err != null) {
			return Result.fieldError("level", err);
		}

		err = EntityUtil.CheckStringField(role.getDescrip(), 0, 100, false);
		if (err != null) {
			return Result.fieldError("descrip", err);
		}

		return null;
	}

	@Override
	public Result add(Roles role) {
		Result result = this.checkFields(role, true);
		if (result != null) {
			return result;
		}

		role.setUpdate_time(null);

		result = new Result(super.save(role));
		if (result.isSuccess()) {
			result.setData(EntityUtil.ToMap(role));
		}

		return result;
	}

	@Override
	public Result edit(Roles role) {
		if (role.getId() == null) {
			return Result.fieldError("id", FieldErrors.EMPTY);
		}

		Result result = this.checkFields(role, false);
		if (result != null) {
			return result;
		}

		result = new Result();
		if (role.getId() <= 4 && (role.getName() != null || role.getLevel() != null)) {
			result.setMsg("不能修改给定字段!");
			return result;
		}

		role.setAdd_time(null);
		role.setAdd_user(null);

		result.setSuccess(super.updateById(role));
		if (result.isSuccess()) {
			result.setData(EntityUtil.ToMap(role));
		}
		return result;
	}

	@Override
	public Result authorizeindex(Integer id, Boolean all) {
		Result result = new Result();
		if (id == 1) {
			result.setMsg("不能授权此角色!");
			return result;
		}

		if (!super.lambdaQuery().eq(Roles::getId, id).exists()) {
			result.setMsg("角色不存在!");
			return result;
		}

		AuthorizeData authorizeData = new AuthorizeData();
		authorizeData.auth = baseMapper.getAuthorizedMenuIds(id);
		if (all) {
			Long uid = LoginInfomationUtil.getUserId();
			authorizeData.all = baseMapper.getUserAssociatedAuthorizedMenus(uid);
		}

		return new Result(true, authorizeData);
	}

	@Override
	@Transactional(rollbackFor = TransactionFailedException.class)
	public Result authorize(AuthorizeParam params) throws TransactionFailedException {
		FieldErrors err = EntityUtil.CheckNumberField(params.id, 2L, 255L, true);
		if (err != null) {
			return Result.fieldError("id", err);
		}

		if (params.auths.size() == 0) {
			return Result.fieldError("auths", FieldErrors.EMPTY);
		}

		Result result = new Result();
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
	public Result delete(Integer id) {
		Result result = new Result();
		if (id <= 2) {
			result.setMsg("不能删除该角色!");
			return result;
		}

		return new Result(super.removeById(id));
	}
}

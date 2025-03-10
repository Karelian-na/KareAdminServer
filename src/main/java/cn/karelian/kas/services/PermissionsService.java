package cn.karelian.kas.services;

import java.util.List;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import cn.karelian.kas.Result;
import cn.karelian.kas.dtos.IndexParam;
import cn.karelian.kas.dtos.IndexParam.IndexType;
import cn.karelian.kas.entities.Permissions;
import cn.karelian.kas.exceptions.NullRequestException;
import cn.karelian.kas.exceptions.IllegalAccessException;
import cn.karelian.kas.exceptions.PermissionNotFoundException;
import cn.karelian.kas.mappers.PermissionsMapper;
import cn.karelian.kas.mappers.RolePermAssocMapper;
import cn.karelian.kas.mappers.UserPermAssocMapper;
import cn.karelian.kas.mappers.UserRoleAssocMapper;
import cn.karelian.kas.services.interfaces.IPermissionsService;
import cn.karelian.kas.utils.EntityUtil;
import cn.karelian.kas.utils.LoginInfomationUtil;
import cn.karelian.kas.views.MenusView;
import cn.karelian.kas.views.PermissionsView;

/**
 * <p>
 * 管理权限目录的表 服务实现类
 * </p>
 *
 * @author Karelian_na
 * @since 2023-08-28
 */
@Service
public class PermissionsService extends KasService<PermissionsMapper, Permissions, PermissionsView>
		implements IPermissionsService {
	@Autowired
	UserPermAssocMapper userPermAssocMapper;
	@Autowired
	UserRoleAssocMapper userRoleAssocMapper;
	@Autowired
	RolePermAssocMapper rolePermAssocMapper;

	@Override
	public Permissions getByUrl(String url) {
		return super.baseMapper.getByUrl(url);
	}

	@Override
	public boolean isAuthorized(MenusView menu) throws NullRequestException {
		if (!menu.getStatus()) {
			return false;
		}

		RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
		if (null == attributes) {
			throw new NullRequestException();
		}

		Long uid = LoginInfomationUtil.getUserId();
		Boolean authorize = userPermAssocMapper.isAuthorized(uid, menu.getId());
		if (null != authorize) {
			return authorize;
		}

		List<Byte> roles = userRoleAssocMapper.getRoles(uid);
		for (Byte rid : roles) {
			if (rolePermAssocMapper.isAuthorized(rid, menu.getId())) {
				return true;
			}
		}

		return false;
	}

	@Override
	public Result index(IndexParam params)
			throws IllegalAccessException, NullRequestException, PermissionNotFoundException {
		params.type = IndexType.All;
		return super.index(params);
	}

	@Override
	public Result add(PermissionsView param) throws NullRequestException {
		Result result = new Result();

		boolean exists = this.lambdaQuery().eq(Permissions::getName, param.getName()).exists();
		if (exists) {
			result.fail("具有该名称的权限已存在，无法重复添加！");
			return result;
		}

		Permissions permission = new Permissions();
		BeanUtils.copyProperties(param, permission);
		result.setSuccess(this.save(permission));
		if (!result.isSuccess()) {
			result.setMsg("添加权限失败！");
		}

		BeanUtils.copyProperties(permission, param);
		param.setAdd_user(LoginInfomationUtil.getUserName());
		result.setData(param);
		return result;
	}

	@Override
	public Result update(PermissionsView param) {
		Result result = new Result();
		boolean exists = this.lambdaQuery().eq(Permissions::getId, param.getId()).exists();
		if (!exists) {
			result.fail("权限不存在！");
			return result;
		}

		Permissions permission = new Permissions();
		BeanUtils.copyProperties(param, permission);
		if (EntityUtil.IsNonOrEmpty(permission, "id")) {
			result.fail("修改内容为空！");
			return result;
		}

		result.setSuccess(this.updateById(permission));
		if (!result.isSuccess()) {
			result.setMsg("更新权限失败！");
		}

		result.setData(permission);
		return result;
	}

	@Override
	public Result delete(Integer[] ids) {
		Result result = new Result();
		if (ids.length == 0) {
			result.setMsg("删除权限为空！");
			return result;
		}

		if (ids.length == 1 && ids[0] < 100) {
			result.setMsg("无法删除内置权限！");
			return result;
		}

		if (!Stream.of(ids).filter(v -> v < 100).findFirst().isEmpty()) {
			result.setMsg("无法完成此操作，集合中包含不可删除的权限！");
			return result;
		}

		result.setSuccess(this.removeBatchByIds(Stream.of(ids).toList()));
		if (!result.isSuccess()) {
			result.setMsg("删除权限失败！");
		}
		return result;
	}
}

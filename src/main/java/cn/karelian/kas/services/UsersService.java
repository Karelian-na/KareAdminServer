package cn.karelian.kas.services;

import jakarta.servlet.http.HttpSession;
import lombok.Getter;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.tomcat.util.buf.HexUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import cn.karelian.kas.KasApplication;
import cn.karelian.kas.Result;
import cn.karelian.kas.codes.CommonErrors;
import cn.karelian.kas.codes.FieldErrors;
import cn.karelian.kas.dtos.AssignRoleParam;
import cn.karelian.kas.dtos.AuthorizeParam;
import cn.karelian.kas.dtos.IndexParam;
import cn.karelian.kas.dtos.RevisePasswordParam;
import cn.karelian.kas.entities.Roles;
import cn.karelian.kas.entities.UserPermAssoc;
import cn.karelian.kas.entities.UserRoleAssoc;
import cn.karelian.kas.entities.Usermsgs;
import cn.karelian.kas.entities.Users;
import cn.karelian.kas.exceptions.NullRequestException;
import cn.karelian.kas.exceptions.IllegalAccessException;
import cn.karelian.kas.exceptions.PermissionNotFoundException;
import cn.karelian.kas.exceptions.TransactionFailedException;
import cn.karelian.kas.mappers.DeletedUsersMapper;
import cn.karelian.kas.mappers.RolesMapper;
import cn.karelian.kas.mappers.UserPermAssocMapper;
import cn.karelian.kas.mappers.UserRoleAssocMapper;
import cn.karelian.kas.mappers.UsermsgsMapper;
import cn.karelian.kas.mappers.UsersMapper;
import cn.karelian.kas.services.interfaces.IUsersService;
import cn.karelian.kas.utils.EntityUtil;
import cn.karelian.kas.utils.LocalStorageUtil;
import cn.karelian.kas.utils.LoginInfomationUtil;
import cn.karelian.kas.utils.WebPageInfo;
import cn.karelian.kas.utils.LocalStorageUtil.AtomicMoveFileHandle;
import cn.karelian.kas.views.MenusView;
import cn.karelian.kas.views.UsermsgsView;

/**
 * <p>
 * 管理用户的表 服务实现类
 * </p>
 *
 * @author Karelian_na
 * @since 2023-08-28
 */
@Service
@Getter
public class UsersService extends KasService<UsersMapper, Users, UsermsgsView> implements IUsersService {
	public static final String BindVerifyUrl = "/Users/bind/verify/send";
	public static final String ReviseVerifyUrl = "/Users/revisepwd/verify/send";

	public enum UserDeleteType {
		DELETE("删除"),
		CANCEL("注销"),;

		private int value;
		private String description;

		UserDeleteType(String description) {
			this.value = super.ordinal() + 1;
			this.description = description;
		}
	}

	@Autowired
	private UsermsgsMapper usermsgsMapper;
	@Autowired
	private MenusService menusService;
	@Autowired
	private UserPermAssocMapper userPermAssocMapper;
	@Autowired
	private UserRoleAssocMapper userRoleAssocMapper;
	@Autowired
	private RolesMapper rolesMapper;
	@Autowired
	private DeletedUsersMapper deletedUsersMapper;
	@Autowired
	DatabasesService databasesService;

	@Override
	@Transactional(rollbackFor = TransactionFailedException.class)
	public Result edit(UsermsgsView usermsgView, boolean self) throws TransactionFailedException {
		Result result = new Result();
		if (self) {
			Long uid = LoginInfomationUtil.getUserId();
			if (!uid.equals(usermsgView.getId())) {
				result.fail("无法修改该用户！");
				return result;
			}
		} else if (!this.lambdaQuery().eq(Users::getId, usermsgView.getId()).exists()) {
			result.setMsg("用户不存在！");
			return result;
		}

		Users user = new Users();
		BeanUtils.copyProperties(usermsgView, user);
		if (!EntityUtil.IsNonOrEmpty(user, "id")) {
			result.setSuccess(baseMapper.updateById(user) == 1);
			if (!result.isSuccess()) {
				result.setMsg("更新用户失败！");
				throw new TransactionFailedException(result);
			}
		}

		Usermsgs usermsg = new Usermsgs();
		BeanUtils.copyProperties(usermsgView, usermsg);
		if (!EntityUtil.IsNonOrEmpty(usermsg, "id")) {
			String avatarName = usermsg.getAvatar();
			if (!ObjectUtils.isEmpty(avatarName)) {
				AtomicMoveFileHandle handle = LocalStorageUtil.atomicMoveTempFiles(avatarName, "image");
				if (!handle.isSuccess()) {
					result.fail("头像保存失败!");
					throw new TransactionFailedException(result);
				}

				usermsg.setAvatar(handle.getPublicPaths()[0]);
			}
			result.setSuccess(usermsgsMapper.updateById(usermsg) == 1);
			if (!result.isSuccess()) {
				result.setMsg("更新用户信息失败！");
				throw new TransactionFailedException(result);
			}
		}

		BeanUtils.copyProperties(usermsg, usermsgView);
		BeanUtils.copyProperties(user, usermsgView);
		result.setData(usermsgView);
		return result;
	}

	@Override
	public Result index(IndexParam params)
			throws IllegalAccessException, NullRequestException, PermissionNotFoundException {

		var rlqw = Wrappers.lambdaQuery(Roles.class);
		if (params.initPageSize != null) {
			rlqw.select(Roles::getId, Roles::getName, Roles::getLevel);
		} else {
			rlqw.select(Roles::getLevel);
		}

		Long uid = LoginInfomationUtil.getUserId();
		var assignableRoles = rolesMapper.getUserAssignableRolesForMap(uid, rlqw);

		Byte topLevel = assignableRoles.stream().map(Roles::getLevel).min(Byte::compareTo)
				.orElse((byte) KasApplication.commonUserRoleId);

		var lqw = Wrappers.lambdaQuery(UsermsgsView.class)
				.notIn(UsermsgsView::getId, LoginInfomationUtil.getUserId(), KasApplication.superAdminId)
				.ge(UsermsgsView::getMax_role_level, topLevel);

		Result result = super.index(params, lqw);
		if (null != result && result.isSuccess() && params.initPageSize != null) {
			@SuppressWarnings("unchecked")
			WebPageInfo<UsermsgsView> info = (WebPageInfo<UsermsgsView>) result.getData();
			info.extraData = Map.of("assignableRoles", assignableRoles);
		}

		return result;
	}

	@Override
	public Result selfindex() throws NullRequestException {
		Result result = new Result();
		UsermsgsView msg = baseMapper.selectViewById(LoginInfomationUtil.getUserId());
		if (null == msg) {
			result.setMsg("获取用户信息失败！");
			return result;
		}

		String fieldsConfig = databasesService.getFieldsConfig(UsermsgsView.class);
		if (null == fieldsConfig) {
			result.setMsg("获取配置信息失败！");
			return result;
		}

		Map<String, Object> data = Map.of("data", msg, "fieldsConfig", fieldsConfig);
		result.setData(data);
		result.setSuccess(true);
		return result;
	}

	@Override
	@Transactional(rollbackFor = TransactionFailedException.class)
	public Result add(UsermsgsView usermsgView) throws TransactionFailedException {
		Result result = new Result();
		Users old = this.baseMapper.getUserWithLogicDelete(usermsgView.getId(), usermsgView.getUid(),
				this.lambdaQuery().select(Users::getDeleted).getWrapper());

		if (null != old) {
			if (old.getDeleted()) {
				result.setMsg("无法添加该用户，具有该Id的用户已被标记为删除的！");
			} else {
				result.setMsg("具有该用户ID或用户名的用户已存在！");
			}
			return result;
		}

		Users user = new Users();
		BeanUtils.copyProperties(usermsgView, user);
		result.setSuccess(baseMapper.insert(user) == 1);
		if (!result.isSuccess()) {
			result.setMsg("添加用户失败！");
			return result;
		}

		Usermsgs usermsg = new Usermsgs();
		BeanUtils.copyProperties(usermsgView, usermsg);
		if (null == usermsg.getAvatar()) {
			usermsg.setAvatar(KasApplication.configs.defaultAvatar);
		} else {
			AtomicMoveFileHandle handle = LocalStorageUtil.atomicMoveTempFiles(usermsg.getAvatar(), "image");
			if (!handle.isSuccess()) {
				result.fail("头像保存失败!");
				throw new TransactionFailedException(result);
			}

			usermsg.setAvatar(handle.getPublicPaths()[0]);
		}
		result.setSuccess(usermsgsMapper.updateById(usermsg) == 1);
		if (!result.isSuccess()) {
			result.setMsg("添加用户信息失败！");
			throw new TransactionFailedException(result);
		}

		if (usermsgView.getRoles_id() != null && usermsgView.getRoles_id().length != 0) {
			Long uid = LoginInfomationUtil.getUserId();
			var rlqw = Wrappers.lambdaQuery(Roles.class).select(Roles::getId);
			var assignableRoles = rolesMapper.getUserAssignableRolesForMap(uid, rlqw)
					.stream().map(Roles::getId).toList();

			if (Stream.of(usermsgView.getRoles_id()).anyMatch(v -> !assignableRoles.contains(v))) {
				result.setMsg("初始指定的角色中包含了不允许分配的角色，无法完成添加！");
				throw new TransactionFailedException(result);
			}

			Map<String, List<?>> paramMap = Map.of("uids", List.of(usermsgView.getId()), "rids",
					List.of(usermsgView.getRoles_id()));
			result.setSuccess(userRoleAssocMapper.insertBatchByUnionKey(paramMap));
			if (!result.isSuccess()) {
				result.setMsg("关联用户角色失败！");
				throw new TransactionFailedException(result);
			}
		}

		BeanUtils.copyProperties(user, usermsgView);
		BeanUtils.copyProperties(usermsg, usermsgView);
		result.setData(usermsgView);
		return result;
	}

	@Override
	public Result delete(List<Long> ids, UserDeleteType type) {
		Result result = new Result();
		if (ids.size() == 0) {
			result.setMsg("删除用户的Id为空！");
			return result;
		}

		if (ids.contains(KasApplication.superAdminId)) {
			result.setMsg("无法删除该用户！");
			return result;
		}

		result.setSuccess(this.baseMapper.delete(ids, type.value));
		if (!result.isSuccess()) {
			result.setMsg(type.description + "用户失败！");
			return result;
		}
		return result;
	}

	@Override
	@Transactional(rollbackFor = TransactionFailedException.class)
	public Result authorize(AuthorizeParam params) throws TransactionFailedException {
		if (params.id == null || params.auths.size() == 0) {
			return Result.fieldError("id", FieldErrors.EMPTY);
		}

		Long loginedId = LoginInfomationUtil.getUserId();
		if (params.id.equals(loginedId)) {
			return Result.fieldError("params.id", "不能授权自己!");
		} else if (params.id.equals(KasApplication.superAdminId)) {
			return Result.fieldError("params.id", "不能授权该用户!");
		}

		Boolean success;
		for (Integer mid : params.auths.keySet()) {
			success = false;
			Byte auth = params.auths.get(mid);
			switch (auth) {
				case 0:
				case 1:
					UserPermAssoc upa = new UserPermAssoc();
					upa.setUid(params.id);
					upa.setMid(mid);
					success = userPermAssocMapper.insertOrUpdateByUnionKey(params.id, mid, auth);
					break;
				case 2:
					success = userPermAssocMapper.deleteByUnionKey(params.id, mid);
					break;
				default:
					break;
			}

			if (!success) {
				throw new TransactionFailedException(null);
			}
		}

		return new Result(true);
	}

	@Override
	public Result reset(List<Long> uids) {
		String hashPwd = null;
		try {
			MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
			messageDigest.update("123456".getBytes());
			hashPwd = HexUtils.toHexString(messageDigest.digest());
		} catch (Exception e) {
			return Result.internalError(null);
		}

		LambdaUpdateWrapper<Users> luw = new LambdaUpdateWrapper<>();
		luw.set(Users::getPwd, hashPwd);
		luw.in(Users::getId, uids);

		return new Result(super.update(luw));
	}

	@Override
	public Result selfreset(RevisePasswordParam params) throws NullRequestException {
		FieldErrors err = EntityUtil.CheckStringField(params.pwd, 64, true);
		if (err != null) {
			return Result.fieldError("pwd", err);
		}

		HttpSession session = LoginInfomationUtil.getSession();
		if (params.account == null && (params.account = String.valueOf(session.getAttribute("id"))) == null) {
			return Result.fieldError("account", FieldErrors.EMPTY);
		}

		Result result = new Result();
		Users user = this.lambdaQuery().select(Users::getPwd).eq(Users::getId, params.account).one();
		if (null == user) {
			result.setMsg("用户不存在！");
			return result;
		}

		if (!params.old.equals(user.getPwd())) {
			result.setMsg("当前密码错误！");
			return result;
		}

		if (params.old.equals(params.pwd)) {
			result.setMsg("新旧密码不能一致！");
			return result;
		}

		LambdaUpdateWrapper<Users> luw = new LambdaUpdateWrapper<>();
		luw.eq(Users::getId, params.account).func(t -> {
			t.eq(Users::getPwd, params.old)
					.isNull(Users::getBind_email)
					.isNull(Users::getBind_phone);
		}).set(Users::getPwd, params.pwd);

		result.setSuccess(super.update(luw));
		if (result.isSuccess()) {
			result.setCode(CommonErrors.UN_LOGIN.getValue());
			session.invalidate();
		}

		return result;
	}

	@Override
	public Result asignindex(List<Long> ids) {
		LambdaQueryWrapper<UserRoleAssoc> ulqw = Wrappers.lambdaQuery();
		ulqw.select(UserRoleAssoc::getRid)
				.in(UserRoleAssoc::getUid, ids)
				.groupBy(UserRoleAssoc::getRid)
				.having("COUNT(DISTINCT uid) = {0}", ids.size());

		Map<String, Object> data = new HashMap<>();
		data.put("common", userRoleAssocMapper.selectObjs(ulqw));

		Long uid = LoginInfomationUtil.getUserId();
		List<Roles> userAssocRoles = rolesMapper.getUserAssocRolesForMap(uid);
		int maxRoleLevelIdx = -1;
		byte curMaxLevel = 99;
		for (int idx = 0; idx < userAssocRoles.size(); idx++) {
			Roles role = userAssocRoles.get(idx);
			if (role.getLevel() < curMaxLevel) {
				curMaxLevel = role.getLevel();
				maxRoleLevelIdx = idx;
			}
		}
		userAssocRoles.remove(maxRoleLevelIdx);
		data.put("roles", userAssocRoles);

		Result result = new Result(true, data);
		return result;
	}

	@Override
	@Transactional(rollbackFor = TransactionFailedException.class)
	public Result asign(AssignRoleParam params) throws NullRequestException, TransactionFailedException {
		List<Byte> assignRids = new ArrayList<>();
		List<Byte> deAssignedRids = new ArrayList<>();
		for (Byte rid : params.auths.keySet()) {
			Byte auth = params.auths.get(rid);
			switch (auth) {
				case 0:
					deAssignedRids.add(rid);
					break;
				case 1:
					assignRids.add(rid);
					break;
				default:
					break;
			}
		}
		if (deAssignedRids.size() != 0) {
			LambdaQueryWrapper<UserRoleAssoc> lqw = Wrappers.lambdaQuery();
			lqw.in(UserRoleAssoc::getRid, deAssignedRids)
					.in(UserRoleAssoc::getUid, params.ids);
			if (userRoleAssocMapper.delete(lqw) == 0) {
				throw new TransactionFailedException(null);
			}
		}

		if (assignRids.size() != 0) {
			if (!userRoleAssocMapper.insertBatchByUnionKey(Map.of("uids", params.ids, "rids", assignRids))) {
				throw new TransactionFailedException(null);
			}
		}
		return new Result(true);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Result authorizeindex(Long id, Boolean all) throws NullRequestException {
		AuthorizeData authorizeData = new AuthorizeData();
		Long loginedId = LoginInfomationUtil.getUserId();
		if (id.equals(loginedId)) {
			return Result.fieldError("id", "不能授权自己!");
		} else if (id.equals(KasApplication.superAdminId)) {
			return Result.fieldError("id", "不能授权该用户!");
		}

		if (all) {
			List<MenusView> allPermissions = baseMapper.getAuthorizedMenusWithoutInfo(loginedId);
			authorizeData.all = allPermissions;
		}

		List<Integer> authPermis = baseMapper.getAuthorizedMenuIds(id);
		authorizeData.auth = authPermis;
		LambdaQueryWrapper<UserPermAssoc> lqw = new LambdaQueryWrapper<>();
		lqw.select(UserPermAssoc::getMid).eq(UserPermAssoc::getUid, id);

		List<Integer> independentPermis = List.class.cast(userPermAssocMapper.selectObjs(lqw));
		authorizeData.independent = independentPermis;

		return new Result(true, authorizeData);
	}

	@Override
	public Result getverifies(String account) throws NullRequestException {
		if (account == null && (account = String.valueOf(LoginInfomationUtil.getUserId())) == null) {
			return Result.fieldError("account", FieldErrors.EMPTY);
		}

		Result result = new Result();
		LambdaQueryWrapper<UsermsgsView> lqw = new LambdaQueryWrapper<>();
		lqw.select(UsermsgsView::getId, UsermsgsView::getBind_email, UsermsgsView::getBind_phone)
				.eq(UsermsgsView::getId, account);

		UsermsgsView usermsg = baseMapper.selectViewOne(lqw);
		if (usermsg == null) {
			result.setMsg("未找到该账户!");
			return result;
		}

		usermsg.setId(null);
		result.setSuccess(true);
		result.setData(EntityUtil.ToMap(usermsg));
		return result;
	}

	@Override
	public Result getDeletedUsers(IndexParam param)
			throws IllegalAccessException, NullRequestException, PermissionNotFoundException {
		return super.index(deletedUsersMapper, param);
	}

	@Override
	public Result restoreDeletedUsers(List<Long> ids) {
		Result result = new Result();
		if (ids.size() == 0) {
			result.setMsg("恢复用户的Id为空！");
			return result;
		}

		result.setSuccess(baseMapper.restoreDeletedUsers(ids));

		return result;
	}

	@Override
	public Result deleteUsersPermanently(List<Long> ids) {
		Result result = new Result();

		if (ids.size() == 0) {
			result.setMsg("删除用户的Id为空！");
			return result;
		}

		if (ids.contains(KasApplication.superAdminId)) {
			result.setMsg("无法删除该用户！");
			return result;
		}

		result.setSuccess(baseMapper.deleteUsersPermanently(ids));
		if (!result.isSuccess()) {
			result.setMsg("删除失败！");
		}

		return result;
	}
}

class AuthorizeData {
	public List<MenusView> all;
	public List<Integer> auth;
	public List<Integer> independent;
}
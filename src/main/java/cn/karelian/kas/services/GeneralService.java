package cn.karelian.kas.services;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.apache.tomcat.util.buf.HexUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;

import cn.karelian.kas.KasApplication;
import cn.karelian.kas.Result;
import cn.karelian.kas.codes.FieldErrors;
import cn.karelian.kas.dtos.LoginParam;
import cn.karelian.kas.entities.Users;
import cn.karelian.kas.exceptions.NullRequestException;
import cn.karelian.kas.services.interfaces.IGeneralService;
import cn.karelian.kas.utils.Constants;
import cn.karelian.kas.utils.HttpUtil;
import cn.karelian.kas.utils.LoginInfomationUtil;
import cn.karelian.kas.views.MenusView;
import cn.karelian.kas.views.UsermsgsView;

@Service
public class GeneralService implements IGeneralService {
	@Autowired
	private UsersService usersService;

	@Override
	public Result login(LoginParam params) {
		Result result = new Result();
		HttpServletRequest request;
		try {
			request = HttpUtil.getRequest();
		} catch (Exception e) {
			result.setMsg("非法的空请求!");
			return result;
		}
		HttpSession session = request.getSession();

		LambdaQueryChainWrapper<Users> lqcw = usersService.lambdaQuery()
				.select(Users::getId, Users::getPwd);
		// 使用用户id登录
		if (params.account.matches(KasApplication.userIdRegex)) {
			lqcw.eq(Users::getId, params.account);

			// 使用用户名登录
		} else if (params.account.matches(KasApplication.userUIdRegex)) {
			lqcw.eq(Users::getUid, params.account);

			// 使用邮箱登录
		} else if (params.account.matches(Constants.emailRegex)) {
			lqcw.eq(Users::getBind_email, params.account);

			// 使用手机号登录
		} else if (params.account.matches(Constants.phoneRegex)) {
			lqcw.eq(Users::getBind_phone, params.account);

		} else {
			return Result.fieldError("account", FieldErrors.FORMAT);
		}

		List<Users> users = lqcw.list();
		if (ObjectUtils.isEmpty(users)) {
			result.setMsg("账号不存在!");
			return result;
		}

		Users user = users.get(0);
		if (!params.pwd.equals(user.getPwd())) {
			result.setMsg("密码错误!");
			return result;
		}

		if (session.getAttribute("id") != null) {
			result.setSuccess(true);
			result.setMsg("您已登陆, 无需重复登陆!");
			return result;
		}

		LambdaQueryWrapper<UsermsgsView> lqw = new LambdaQueryWrapper<>();
		lqw.select(UsermsgsView::getName)
				.eq(UsermsgsView::getId, user.getId());
		UsermsgsView userMsg = usersService.getViewOne(lqw);

		session.setAttribute("id", user.getId());
		session.setAttribute("name", userMsg.getName());

		if (params.remember) {
			session.setMaxInactiveInterval(3600 * 24 * 7);
		}

		user.setPwd(null);
		user.setLast_login_ip(HttpUtil.getRemoteIp(request));
		user.setLast_login_time(LocalDateTime.now());

		result.setSuccess(usersService.updateById(user));
		return result;
	}

	@Override
	public void logout() {
		try {
			HttpSession session = LoginInfomationUtil.getSession();
			session.invalidate();
		} catch (Exception e) {
		}
	}

	@Override
	public Result index() throws NullRequestException {
		HttpServletRequest request = HttpUtil.getRequest();
		request.changeSessionId();
		Long uid = (Long) request.getSession().getAttribute("id");

		IndexInfo info = new IndexInfo();
		info.menus = usersService.getBaseMapper().getAuthorizedMenus(uid);

		LambdaQueryWrapper<UsermsgsView> lqw = new LambdaQueryWrapper<>();
		lqw.select(UsermsgsView::getName, UsermsgsView::getAvatar);
		lqw.eq(UsermsgsView::getId, uid);
		info.userMsg = usersService.listViewMaps(lqw).get(0);

		Result result = new Result();
		Path commonFieldsConfigPath = KasApplication.currentPath.resolve("data/configs/fields/common.js");
		try (FileInputStream fileInputStream = new FileInputStream(commonFieldsConfigPath.toFile())) {
			byte[] contents = fileInputStream.readAllBytes();
			info.fieldsConfig = new String(contents, "utf-8");
		} catch (Exception e) {
			result.setMsg("加载配置失败！");
			return result;
		}

		result.setSuccess(true);
		result.setData(info);
		return result;
	}

	@Override
	public Result upload(MultipartFile[] files) {
		MessageDigest digest;
		List<String> paths = new ArrayList<>();

		try {
			digest = MessageDigest.getInstance("SHA-512");
		} catch (Exception e) {
			return Result.internalError("获取实例失败!");
		}

		for (MultipartFile file : files) {
			try (InputStream is = file.getInputStream()) {
				byte[] buffer = new byte[1024];
				while (true) {
					int len = is.read(buffer, 0, 1024);
					if (len == -1) {
						break;
					}

					digest.update(buffer, 0, len);
				}

				// digest.update(HttpUtil.getRequest().getRequestURI().getBytes());
			} catch (Exception e) {
				return Result.internalError("实例计算失败!");
			}

			String originName = file.getOriginalFilename();
			if (originName == null) {
				return new Result("文件名不合法!");
			}

			StringBuilder hashWithFileExt = new StringBuilder(Base64.getEncoder().encodeToString(digest.digest()));

			String fileExtension = "";
			int idx = originName.lastIndexOf(".");
			if (idx != -1) {
				fileExtension = originName.substring(idx);
				hashWithFileExt.append(fileExtension);
			}

			try {
				digest = MessageDigest.getInstance("MD5");
			} catch (Exception e) {
				return Result.internalError("获取实例失败!");
			}

			String hashNameHash = HexUtils.toHexString(digest.digest(hashWithFileExt.toString().getBytes()));

			if (null != KasApplication.configs.localStorageConfig) {
				Path storePath = KasApplication.configs.localStorageConfig.tempPath.resolve(
						hashNameHash + fileExtension);
				if (null != storePath && !Files.exists(storePath)) {
					try {
						file.transferTo(storePath);
					} catch (Exception e) {
						continue;
					}
				}
			}
			paths.add(hashWithFileExt.toString());
		}
		if (paths.size() == 0) {
			return new Result(false);
		}

		return new Result(true, String.join(";", paths));
	}
}

class IndexInfo {
	public String fieldsConfig;
	public List<MenusView> menus;
	public Map<String, Object> userMsg;
}

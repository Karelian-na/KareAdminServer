package cn.karelian.kas.aspects;

import jakarta.servlet.http.HttpServletRequest;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cn.karelian.kas.exceptions.InvalidArgumentException;
import cn.karelian.kas.exceptions.NullRequestException;
import cn.karelian.kas.exceptions.OperationNotAllowedException;
import cn.karelian.kas.exceptions.PermissionNotFoundException;
import cn.karelian.kas.exceptions.UnAuthorizedAccessException;
import cn.karelian.kas.exceptions.UnLoginException;
import cn.karelian.kas.services.MenusService;
import cn.karelian.kas.services.PermissionsService;
import cn.karelian.kas.utils.HttpUtil;
import cn.karelian.kas.utils.LoginInfomationUtil;
import cn.karelian.kas.views.MenusView;

@Aspect
@Component
public class AuthorizeAspect {
	@Autowired
	private LogAspect logAspect;
	@Autowired
	private MenusService menusService;
	@Autowired
	private PermissionsService permissionsService;

	@Pointcut("@annotation(cn.karelian.kas.annotations.Authorize)")
	private void authorizePointcutFilter() {
	}

	@Before("authorizePointcutFilter()")
	public void canAccess(JoinPoint joinPoint) throws NullRequestException, UnLoginException,
			PermissionNotFoundException, UnAuthorizedAccessException, InvalidArgumentException,
			OperationNotAllowedException {
		HttpServletRequest request = HttpUtil.getRequest();
		if (LoginInfomationUtil.getUserId() == null) {
			throw new UnLoginException();
		}

		String url = request.getRequestURI();
		MenusView menu = menusService.getByUrl(url);
		while (menu == null) {
			if (url.endsWith("/bulkdelete")) {
				url = url.replace("/bulkdelete", "/delete");
			}
			menu = menusService.getByUrl(url);
			if (menu != null) {
				break;
			}

			url = url.replaceFirst("(.*)/.*/([a-zA-Z]*)$", "$1/$2");
			menu = menusService.getByUrl(url);
			if (null != menu) {
				break;
			}
			throw new PermissionNotFoundException();
		}

		if (!permissionsService.isAuthorized(menu)) {
			throw new UnAuthorizedAccessException();
		}

		logAspect.log(request, menu.getName(), joinPoint);
	}

}

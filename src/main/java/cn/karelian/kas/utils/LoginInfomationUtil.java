package cn.karelian.kas.utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import cn.karelian.kas.exceptions.NullRequestException;

public class LoginInfomationUtil {

	public static String getUserName() {
		RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
		if (null == attributes) {
			return null;
		}
		HttpServletRequest request = ((ServletRequestAttributes) attributes).getRequest();
		return (String) request.getSession().getAttribute("name");
	}

	public static Long getUserId() {
		RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
		if (null == attributes) {
			return null;
		}
		HttpServletRequest request = ((ServletRequestAttributes) attributes).getRequest();
		return (Long) request.getSession().getAttribute("id");
	}

	public static HttpSession getSession() throws NullRequestException {
		RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
		if (null == attributes) {
			throw new NullRequestException();
		}
		HttpServletRequest request = ((ServletRequestAttributes) attributes).getRequest();
		return request.getSession();
	}
}

package cn.karelian.kas.utils;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.util.ObjectUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import cn.karelian.kas.exceptions.NullRequestException;

public class HttpUtil {

	public static HttpServletRequest getRequest() throws NullRequestException {
		RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
		if (null == attributes) {
			throw new NullRequestException();
		}
		return ((ServletRequestAttributes) attributes).getRequest();
	}

	public static String getRemoteIp() throws NullRequestException {
		return HttpUtil.getRemoteIp(HttpUtil.getRequest());
	}

	public static String getRemoteIp(HttpServletRequest request) {
		String ip = request.getHeader("X-Real-IP");
		if (ObjectUtils.isEmpty(ip)) {
			ip = request.getHeader("X-Forwarded-For");
			if (ObjectUtils.isEmpty(ip)) {
				ip = request.getRemoteAddr();
			}
		}

		return ip;
	}

	public static String getRequestBody(HttpServletRequest request) {
		try {
			if (!(request instanceof CachedBodyHttpServletRequestWrapper)) {
				return null;
			}

			CachedBodyHttpServletRequestWrapper requestWrapper = (CachedBodyHttpServletRequestWrapper) request;
			return requestWrapper.body;
		} catch (Exception e) {
			return "";
		}
	}
}

package cn.karelian.kas.aspects;

import java.util.HashMap;
import java.util.Map;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import cn.karelian.kas.KasApplication;
import cn.karelian.kas.annotations.Log;
import cn.karelian.kas.entities.Logs;
import cn.karelian.kas.exceptions.InvalidArgumentException;
import cn.karelian.kas.exceptions.NullRequestException;
import cn.karelian.kas.exceptions.OperationNotAllowedException;
import cn.karelian.kas.exceptions.UnLoginException;
import cn.karelian.kas.services.LogsService;
import cn.karelian.kas.utils.HttpUtil;
import cn.karelian.kas.utils.LoginInfomationUtil;
import jakarta.servlet.http.HttpServletRequest;

@Aspect
@Component
public class LogAspect {
	@Autowired
	private LogsService logsService;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private ValidateAspect validateAspect;

	@Pointcut("@annotation(cn.karelian.kas.annotations.Log)")
	private void logPointcutFilter() {
	}

	public void log(HttpServletRequest request, String name, JoinPoint joinPoint)
			throws InvalidArgumentException, OperationNotAllowedException {
		String param = null;
		Logs log = new Logs();
		log.setUid(LoginInfomationUtil.getUserId());
		log.setType(request.getMethod());
		log.setUrl(request.getRequestURI());
		try {
			Map<String, Object> args = new HashMap<>();
			KasApplication.configs.traceFields.forEach(field -> {
				args.put(field, request.getHeader(field));
			});

			request.getParameterMap().forEach((k, v) -> {
				if (v.length == 1) {
					args.put(k, v[0]);
				} else if (v.length == 0) {
					args.put(k, "");
				} else {
					args.put(k, v);
				}
			});

			if (!request.getMethod().equals("GET") && !request.getMethod().equals("DELETE")) {
				String body = HttpUtil.getRequestBody(request);
				// if content-type is `multipart/form-data`, the body was empty
				if (body != null) {
					try {
						args.put("$body", objectMapper.readValue(body, Map.class));
					} catch (Exception e) {
						args.put("$body", body);
					}
				}
			}

			param = objectMapper.writeValueAsString(args);
		} catch (Exception e) {
			param = "error occured";
		}
		log.setParams(param);
		log.setIp(HttpUtil.getRemoteIp(request));
		log.setTitle(name);
		logsService.save(log);

		if (joinPoint.getArgs().length != 0) {
			validateAspect.validate(joinPoint);
		}
	}

	@Before("logPointcutFilter() && @annotation(log)")
	public void log(JoinPoint joinPoint, Log log)
			throws NullRequestException, UnLoginException, InvalidArgumentException, OperationNotAllowedException {
		HttpServletRequest request = HttpUtil.getRequest();
		if (log.checkLogin()) {
			if (request.getSession().getAttribute("id") == null) {
				throw new UnLoginException();
			}
		}
		this.log(request, log.value(), joinPoint);
	}
}

package cn.karelian.kas.configs;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.BeanWrapperImpl;
import org.springframework.core.MethodParameter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolverComposite;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest;

import cn.karelian.kas.annotations.RequestParamWithExtras;
import jakarta.servlet.http.HttpServletRequest;

public class RequestParamWithExtrasResolver extends HandlerMethodArgumentResolverComposite {
	@Override
	public boolean supportsParameter(@NonNull MethodParameter parameter) {
		return parameter.hasParameterAnnotation(RequestParamWithExtras.class);
	}

	@Override
	public Object resolveArgument(@NonNull MethodParameter parameter, @Nullable ModelAndViewContainer mavContainer,
			@NonNull NativeWebRequest webRequest, @Nullable WebDataBinderFactory binderFactory) throws Exception {

		RequestParamWithExtras annotation = parameter.getParameterAnnotation(RequestParamWithExtras.class);
		if (annotation == null) {
			throw new IllegalArgumentException(
					"RequestParamWithExtras annotation is required for ConvertWithExtraParamResolver.");
		}

		final HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
		if (request == null) {
			throw new IllegalArgumentException("HttpServletRequest is required for AoEncryptableParamResolver.");
		}

		Map<String, Object> extras = null;

		Class<?> clazz = parameter.getParameterType();
		Object target = clazz.getDeclaredConstructor().newInstance();

		Field field = clazz.getDeclaredField(annotation.value());
		if (field != null) {
			field.setAccessible(true);
			if (field.getType() != Map.class) {
				throw new IllegalArgumentException(
						"Field '" + annotation.value() + "' must be of type Map<String, Object>.");
			}
			extras = new HashMap<>();
			field.set(target, extras);
		}

		Map<String, String[]> paramMap = request.getParameterMap();
		BeanWrapperImpl wrapper = new BeanWrapperImpl(target);
		for (Map.Entry<String, String[]> entry : paramMap.entrySet()) {
			String key = entry.getKey();
			String[] valueArray = entry.getValue();
			String value = valueArray != null && valueArray.length > 0 ? valueArray[0] : null;

			if (wrapper.isWritableProperty(key)) {
				wrapper.setPropertyValue(key, value);
			} else if (extras != null) {
				extras.put(key, value);
			}
		}

		field = clazz.getDeclaredField(annotation.files());
		if (field != null && request instanceof StandardMultipartHttpServletRequest) {
			if (field.getType() != Map.class) {
				throw new IllegalArgumentException(
						"Field '" + annotation.files() + "' must be of type MultiValueMap<String, MultipartFile>.");
			}

			field.setAccessible(true);
			field.set(target, ((StandardMultipartHttpServletRequest) request).getMultiFileMap());
		}
		return target;
	}
}
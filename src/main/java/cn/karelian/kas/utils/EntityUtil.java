package cn.karelian.kas.utils;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.reflection.property.PropertyNamer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.karelian.kas.codes.FieldErrors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EntityUtil {
	public static boolean IsNonOrEmpty(Object obj, String... ignores) {
		if (obj == null) {
			return true;
		}

		// 获取对象的所有属性
		var fields = EntityUtil.getFieldsIncludeSuperClasses(obj);
		for (Field field : fields) {
			if (Modifier.isStatic(field.getModifiers()) || Arrays.asList(ignores).indexOf(field.getName()) != -1) {
				continue;
			}

			field.setAccessible(true);
			try {
				Object value = field.get(obj);

				// 判断属性是否为 null 或者空字符串
				if (value != null && (!(value instanceof Array) || Array.getLength(value) != 0)) {
					return false;
				}
			} catch (Exception e) {
				Logger logger = LoggerFactory.getLogger(EntityUtil.class);
				logger.error("Judge object empty error, reason: " + e.getMessage());
			}
		}
		return true;
	}

	public static Map<String, Object> ToMap(Serializable entity) {
		if (entity == null)
			return null;

		Map<String, Object> map = new HashMap<>();

		for (Field field : entity.getClass().getDeclaredFields()) {
			if (Modifier.isStatic(field.getModifiers())) {
				continue;
			}
			Boolean access = field.canAccess(entity);
			if (!access)
				field.setAccessible(true);

			Object value;
			try {
				value = field.get(entity);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				continue;
			}
			if (value != null) {
				map.put(field.getName(), value);
			}

			if (!access) {
				field.setAccessible(access);
			}
		}

		return map;
	}

	public static FieldErrors CheckStringField(String value, String regex, boolean noEmpty) {

		if (value == null) {
			return noEmpty ? FieldErrors.EMPTY : null;
		}

		if (!value.matches(regex)) {
			return FieldErrors.FORMAT;
		}

		return null;
	}

	public static List<Field> getFieldsIncludeSuperClasses(Object model) {
		Class<?> clazz = model.getClass();
		List<Field> fields = new ArrayList<>();
		while (clazz != null) {
			fields.addAll(0, new ArrayList<>(Arrays.asList(clazz.getDeclaredFields())));
			clazz = clazz.getSuperclass();
		}
		return fields;
	}

	public static Field getFieldIncludeSuperClasses(Object model, String fieldName) {
		Class<?> clszz = model.getClass();
		Field field = null;
		while (clszz != null) {
			try {
				field = clszz.getDeclaredField(fieldName);
			} catch (NoSuchFieldException e) {
			}
			clszz = clszz.getSuperclass();
		}
		return field;
	}

	public static FieldErrors CheckStringField(String value, int len, boolean noEmpty) {
		if (value == null) {
			return noEmpty ? FieldErrors.EMPTY : null;
		}

		if (value.length() != len) {
			return FieldErrors.FORMAT;
		}

		return null;
	}

	public static FieldErrors CheckStringField(String value, int minLen, int maxLen, boolean noEmpty) {
		if (value == null) {
			return noEmpty ? FieldErrors.EMPTY : null;
		}

		if (value.length() > maxLen) {
			return FieldErrors.TOO_LONG;
		}

		if (value.length() < minLen) {
			return FieldErrors.TOO_SHORT;
		}

		return null;
	}

	public static <T extends Comparable<T>> FieldErrors CheckNumberField(T value, T min, T max, boolean noEmpty) {
		FieldErrors err = null;
		if (value == null) {
			if (noEmpty) {
				err = FieldErrors.EMPTY;
			}
		} else if (max != null && value.compareTo(max) > 0) {
			err = FieldErrors.TOO_LARGE;
		} else if (min != null && value.compareTo(min) < 0) {
			err = FieldErrors.TOO_SMALL;
		}

		return err;
	}

	/**
	 * Create a runtime annotation
	 * 
	 * @param <T>    the annotation type
	 * @param <R>    ignored
	 * @param clszz  the annotation class
	 * @param values the annotation's init value
	 * @return
	 */
	public static <T extends Annotation, R> T createAnnatation(Class<T> clszz,
			Map<SerializableFunction<T, R>, Object> values) {
		return new AnnotationInvocationHandler<T, R>(clszz, values).target();
	}

	/**
	 * Set an annotation's member value, the annotation must created by function
	 * {@code createAnnatation}
	 * 
	 * @param <T>the     annotation type
	 * @param <R>the     ignored
	 * @param annotation the annotation instance
	 * @param column     the member need to be changed
	 * @param value      changed value
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Annotation, R> void setAnnotationValue(T annotation, SerializableFunction<T, R> column,
			Object value) {
		InvocationHandler handler = Proxy.getInvocationHandler(annotation);

		if (!(handler instanceof AnnotationInvocationHandler)) {
			throw new UnsupportedOperationException(
					"The function `setAnnotationValue` only worked when the annotation was created by `createAnnatation`!");
		}

		Class<?> clszz = handler.getClass();
		try {
			Field field = clszz.getDeclaredField("memberValues");
			field.setAccessible(true);

			var memberValues = (Map<String, Object>) field.get(handler);

			String key = getFieldName(column);
			memberValues.put(key, value);
		} catch (Exception e) {
		}
	}

	/**
	 * Get field name through the field's getter accessor
	 * 
	 * @param <T>      the type of the field
	 * @param <R>      ignored
	 * @param function the field's getter accessor
	 * @return
	 */
	public static <T, R> String getFieldName(SerializableFunction<T, R> function) {
		try {
			Method writeReplace = function.getClass().getDeclaredMethod("writeReplace");
			writeReplace.setAccessible(true);

			SerializedLambda serializedLambda = (SerializedLambda) writeReplace.invoke(function);

			String name = PropertyNamer.methodToProperty(serializedLambda.getImplMethodName());
			return name;
		} catch (Exception e) {
			throw new RuntimeException("Failed to extract field name from function", e);
		}
	}

	public static <T> T enumValueOf(Class<T> clszz, String value) {
		int idx = Integer.valueOf(value);

		var constants = clszz.getEnumConstants();
		if (idx < 0 || idx > constants.length) {
			throw new IllegalArgumentException(
					"Failed to convert value `" + value + "` to enum type `" + clszz.getName() + "`!");
		}

		return constants[idx];
	}

	public static <T> T copyProperties(T source, T target, String... ignores) {
		if (source == null || target == null) {
			return target;
		}

		var ignoreFields = Arrays.asList(ignores);
		var fields = getFieldsIncludeSuperClasses(target);
		for (Field field : fields) {
			if (Modifier.isStatic(field.getModifiers())) {
				continue;
			}

			if (ignoreFields.indexOf(field.getName()) != -1) {
				continue;
			}

			field.setAccessible(true);
			try {
				Object value = field.get(source);
				if (value != null) {
					field.set(target, value);
				}
			} catch (Exception e) {
				String msg = "Error occurred when copy properties, class: " + source.getClass().getName() + ", field: "
						+ field.getName() + ", reason: " + e.getMessage();
				throw new RuntimeException(msg);
			}
		}

		return target;
	}
}

class AnnotationInvocationHandler<T extends Annotation, R> implements InvocationHandler {
	private final Map<String, Object> memberValues;
	private final Class<T> type;

	public AnnotationInvocationHandler(Class<T> clszz, Map<SerializableFunction<T, R>, Object> values) {
		this.memberValues = new HashMap<>();

		values.forEach((column, value) -> {
			String key = EntityUtil.getFieldName(column);
			memberValues.put(key, value);
		});

		var memberMethods = clszz.getDeclaredMethods();
		for (var method : memberMethods) {
			String key = method.getName();

			if (!memberValues.containsKey(key)) {
				memberValues.put(key, method.getDefaultValue());
			}
		}

		type = clszz;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		String valueName = method.getName();

		return memberValues.get(valueName);
	}

	@SuppressWarnings("unchecked")
	public T target() {
		return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class[] { type }, this);
	}
}
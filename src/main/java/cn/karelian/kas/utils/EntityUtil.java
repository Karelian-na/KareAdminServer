package cn.karelian.kas.utils;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.karelian.kas.codes.FieldErrors;

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
			fields.addAll(new ArrayList<>(Arrays.asList(clazz.getDeclaredFields())));
			clazz = clazz.getSuperclass();
		}
		return fields;
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
}

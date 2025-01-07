package cn.karelian.kas.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.apache.ibatis.reflection.property.PropertyNamer;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.UnknownTypeHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.core.toolkit.LambdaUtils;
import com.baomidou.mybatisplus.core.conditions.AbstractLambdaWrapper;
import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.support.ColumnCache;
import com.baomidou.mybatisplus.core.toolkit.support.LambdaMeta;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MybatisPlusUtil {
	@Autowired
	private static Configuration configuration;

	private static Method formatParamMethod;
	private static Method columnToStringMethod;

	static {
		SqlSessionFactory sqlSessionFactory = SpringContextUtil.getBean(SqlSessionFactory.class);
		configuration = sqlSessionFactory.getConfiguration();

		try {
			formatParamMethod = AbstractWrapper.class.getDeclaredMethod("formatParam", String.class,
					Object.class);
			columnToStringMethod = AbstractLambdaWrapper.class.getDeclaredMethod("columnToString", SFunction.class,
					boolean.class);
		} catch (Exception e) {
			log.error("Failed to initialize internal methods, reason: " + e.getMessage());
			System.exit(1);
		}
	}

	private static final <T> String formatParams(LambdaUpdateWrapper<T> luw, String mapping, Object value) {
		try {
			formatParamMethod.setAccessible(true);
			String sql = (String) formatParamMethod.invoke(luw, mapping, value);
			formatParamMethod.setAccessible(false);
			return sql;
		} catch (Exception e) {
			String message = "Failed to invoke `formatParamMethod`, reason: " + ExceptionUtil.getMessage(e);
			throw new RuntimeException(message);
		}
	}

	public static final <T> String columnToString(SFunction<T, ?> column, boolean onlyColumn) {
		LambdaMeta meta = LambdaUtils.extract(column);
		String fieldName = PropertyNamer.methodToProperty(meta.getImplMethodName());
		Class<?> instantiatedClass = meta.getInstantiatedClass();
		var columnMap = LambdaUtils.getColumnMap(instantiatedClass);
		ColumnCache columnCache = (ColumnCache) columnMap.get(LambdaUtils.formatKey(fieldName));
		if (columnCache == null) {
			throw new RuntimeException("Failed to get column cache for field: " + fieldName);
		}
		return onlyColumn ? columnCache.getColumn() : columnCache.getColumnSelect();
	}

	public static final <T> String columnToString(SFunction<T, ?> column) {
		return columnToString(column, true);
	}

	private static final <T> String columnToString(LambdaUpdateWrapper<T> luw, SFunction<T, ?> column,
			boolean onlyColumn) {
		try {
			columnToStringMethod.setAccessible(true);
			String name = (String) columnToStringMethod.invoke(luw, column, onlyColumn);
			columnToStringMethod.setAccessible(false);
			return name;
		} catch (Exception e) {
			String message = "Failed to invoke `columnToStringMethod`, reason: " + ExceptionUtil.getMessage(e);
			throw new RuntimeException(message);
		}
	}

	// apply non-null fields to the update wrapper
	private static <T> boolean applyNonNullUpdateFields(T entity, Wrapper<T> ew, String... ignores) {
		if (entity == null) {
			return false;
		}

		boolean hasValueSet = false;
		var ignoreFields = Arrays.asList(ignores);
		var fields = EntityUtil.getFieldsIncludeSuperClasses(entity);
		for (Field field : fields) {
			if (Modifier.isStatic(field.getModifiers()) || ignoreFields.contains(field.getName())) {
				continue;
			}

			Object value = null;
			field.setAccessible(true);

			try {
				value = field.get(entity);
			} catch (Exception e) {
				log.error("Judge object empty error, reason: " + e.getMessage());
				continue;
			}

			// 跳过空值字段
			if (value == null) {
				continue;
			}

			// 跳过非数据库字段
			TableField annotation = field.getAnnotation(TableField.class);
			if (annotation != null && !annotation.exist()) {
				continue;
			}

			String columnName = annotation == null ? null : annotation.value();
			if (ObjectUtils.isEmpty(columnName)) {
				if (configuration.isMapUnderscoreToCamelCase()) {
					columnName = StringUtils.underlineToCamel(field.getName());
				} else {
					columnName = field.getName();
				}
			}

			final String mapping = annotation != null && annotation.typeHandler() != UnknownTypeHandler.class
					? "typeHandler=" + annotation.typeHandler().getName()
					: null;

			if (ew instanceof UpdateWrapper<T> uw) {
				uw.set(columnName, value, mapping);
			} else if (ew instanceof LambdaUpdateWrapper<T> luw) {
				String sql = formatParams(luw, mapping, value);
				luw.setSql(columnName + Constants.EQUALS + sql);
			} else {
				String msg = "Invalid update wrapper type: " + ew.getClass().getName();
				throw new RuntimeException(msg);
			}

			hasValueSet = true;
		}

		return hasValueSet;
	}

	@SafeVarargs
	public final static <T> boolean applyNonNullUpdateFields(T entity, LambdaUpdateWrapper<T> ew,
			SFunction<T, ?>... ignores) {
		var columns = Stream.of(ignores).map(column -> columnToString(ew, column, true)).toArray(String[]::new);
		return applyNonNullUpdateFields(entity, (Wrapper<T>) ew, columns);
	}

	public final static <T> boolean applyNonNullUpdateFields(T entity, UpdateWrapper<T> ew, String... ignores) {
		return applyNonNullUpdateFields(entity, (Wrapper<T>) ew, ignores);
	}

	// judge whether the update wrapper is empty
	@SuppressWarnings("unchecked")
	private static final <T> boolean isUpdateWrapperEmpty(Wrapper<T> updateWrapper) {
		if (updateWrapper == null) {
			return true;
		}

		try {
			Field field = updateWrapper.getClass().getDeclaredField("sqlSet");
			field.setAccessible(true);
			var sqlSet = (List<String>) field.get(updateWrapper);
			field.setAccessible(false);
			return sqlSet.isEmpty();
		} catch (Exception e) {
			return true;
		}
	}

	public static final <T> boolean isUpdateWrapperEmpty(LambdaUpdateWrapper<T> updateWrapper) {
		return isUpdateWrapperEmpty((Wrapper<T>) updateWrapper);
	}

	public static final <T> boolean isUpdateWrapperEmpty(UpdateWrapper<T> updateWrapper) {
		return isUpdateWrapperEmpty((Wrapper<T>) updateWrapper);
	}
}

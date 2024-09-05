package cn.karelian.kas.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.UnknownTypeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;

public class MybatisPlusUtil {
	private static Logger logger = LoggerFactory.getLogger(MybatisPlusUtil.class);

	@Autowired
	private static Configuration configuration;

	static {
		SqlSessionFactory sqlSessionFactory = SpringContextUtil.getBean(SqlSessionFactory.class);
		configuration = sqlSessionFactory.getConfiguration();
	}

	public static <T> boolean applyNonNullUpdateFields(T entity, UpdateWrapper<T> luw, String... ignores) {
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
				logger.error("Judge object empty error, reason: " + e.getMessage());
				continue;
			}

			// 跳过空值字段
			if (value == null) {
				continue;
			}

			// 跳过非数据库字段
			TableField tableFieldAnnotation = field.getAnnotation(TableField.class);
			if (tableFieldAnnotation != null && !tableFieldAnnotation.exist()) {
				continue;
			}

			String columnName = tableFieldAnnotation == null ? null : tableFieldAnnotation.value();
			if (ObjectUtils.isEmpty(columnName)) {
				if (configuration.isMapUnderscoreToCamelCase()) {
					columnName = StringUtils.underlineToCamel(field.getName());
				} else {
					columnName = field.getName();
				}
			}

			if (tableFieldAnnotation == null || tableFieldAnnotation.typeHandler() == UnknownTypeHandler.class) {
				luw.set(columnName, value);
			} else {
				luw.set(columnName, value, "typeHandler=" + tableFieldAnnotation.typeHandler().getName());
			}

			hasValueSet = true;
		}

		return hasValueSet;
	}

	@SuppressWarnings("unchecked")
	public static <T> boolean isUpdateWrapperEmpty(UpdateWrapper<T> updateWrapper) {
		if (updateWrapper == null) {
			return true;
		}

		try {
			Field field = updateWrapper.getClass().getDeclaredField("sqlSet");

			field.setAccessible(true);

			var sqlSet = (List<String>) field.get(updateWrapper);

			return sqlSet.isEmpty();
		} catch (Exception e) {
			return true;
		}
	}
}

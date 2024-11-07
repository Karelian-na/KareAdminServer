package cn.karelian.kas.aspects;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import cn.karelian.kas.annotations.ComparableValidate;
import cn.karelian.kas.annotations.GeneralValidate;
import cn.karelian.kas.annotations.StringValidate;
import cn.karelian.kas.annotations.Validate;
import cn.karelian.kas.codes.FieldErrors;
import cn.karelian.kas.exceptions.InvalidArgumentException;
import cn.karelian.kas.exceptions.OperationNotAllowedException;
import cn.karelian.kas.services.TableFieldsInfoService;
import cn.karelian.kas.utils.EntityUtil;
import cn.karelian.kas.utils.NonEmptyStrategy;

class ValidatorContext<T> {
	String validatingFieldName;
	T validator;
}

@Aspect
@Component
public class ValidateAspect {
	@Autowired
	TableFieldsInfoService tableFieldsInfoService;

	Logger logger = LoggerFactory.getLogger(ValidateAspect.class);

	@Pointcut("@annotation(cn.karelian.kas.annotations.Validate)")
	private void validatePointcutFilter() {
	}

	@SuppressWarnings("unchecked")
	private <T, V> T getCommonAnnotationMember(String name, V validator) throws InvalidArgumentException {
		Method method = null;
		try {
			method = validator.getClass().getMethod(name);
			if (method == null) {
				var err = new InvalidArgumentException("validator has no nonEmptyStrategy policy!");
				logger.error(err.getMessage());
				throw err;
			}
			return (T) method.invoke(validator);
		} catch (Exception e) {
			var err = new InvalidArgumentException("validator has no nonEmptyStrategy policy!");
			logger.error(err.getMessage());
			throw err;
		}
	}

	private int compare(Object l, Long r) throws InternalError {
		if (l instanceof Byte) {
			return ((Byte) l).compareTo(r.byteValue());
		}

		if (l instanceof Short) {
			return ((Short) l).compareTo(r.shortValue());
		}

		if (l instanceof Integer) {
			return ((Integer) l).compareTo(r.intValue());
		}

		if (l instanceof Long) {
			return ((Long) l).compareTo(r);
		}

		if (l instanceof Float) {
			return ((Float) l).compareTo(r.floatValue());
		}

		if (l instanceof Double) {
			return ((Double) l).compareTo(r.doubleValue());
		}

		throw new InternalError("Incorrect type to compare when validating!");
	}

	private <T extends Annotation> ValidatorContext<T> commonValiate(Object descriptor, Object value,
			Object argContainer, Validate rule, Class<T> validateClszz) throws InvalidArgumentException {
		ValidatorContext<T> context = new ValidatorContext<>();

		if (descriptor instanceof Field) {
			Field field = (Field) descriptor;
			context.validatingFieldName = field.getName();
			context.validator = field.getAnnotation(validateClszz);
		} else if (descriptor instanceof Parameter) {
			Parameter param = (Parameter) descriptor;
			context.validatingFieldName = param.getName();
			context.validator = param.getAnnotation(validateClszz);
		} else {
			var err = new InvalidArgumentException("validate an unsupported target!");
			logger.error(err.getMessage());
			throw err;
		}

		// validate non empty strategy
		Integer nonEmptyStrategy = this.getCommonAnnotationMember("nonEmptyStrategy", context.validator);
		if ((rule.nonEmptyStrategy() & nonEmptyStrategy) != 0) {
			if (value == null) {
				throw new InvalidArgumentException(context.validatingFieldName, FieldErrors.EMPTY);
			}
		}

		// validate non empty when depends on's value is not null
		String dependsOn = this.getCommonAnnotationMember("dependsOn", context.validator);
		if (!dependsOn.equals("")) {
			Object dependsFieldValue = null;
			if (descriptor instanceof Field) {
				try {
					Field dependsField = argContainer.getClass().getDeclaredField(dependsOn);
					dependsField.setAccessible(true);
					dependsFieldValue = dependsField.get(argContainer);
				} catch (NoSuchFieldException e) {
				} catch (Exception e) {
					throw new InternalError("Failed to get field value");
				}
			}

			if (!ObjectUtils.isEmpty(dependsFieldValue) && ObjectUtils.isEmpty(value)) {
				throw new InvalidArgumentException(context.validatingFieldName,
						FieldErrors.EMPTY);
			}
		}

		return context;
	}

	private void validateField(Field field, Object obj, Validate rule, Map<String, Boolean> fieldsEditableMap,
			int depth)
			throws InternalError, InvalidArgumentException, OperationNotAllowedException {
		Boolean access = field.canAccess(obj);
		if (!access) {
			field.setAccessible(true);
		}

		Object value = null;
		try {
			value = field.get(obj);
		} catch (Exception e) {
			throw new InternalError("Failed to get field value when validating!");
		}

		boolean isAdd = rule.nonEmptyStrategy() == NonEmptyStrategy.ADD;
		boolean isOperation = (rule.nonEmptyStrategy() & NonEmptyStrategy.OPERATION) != 0;
		if (isOperation && fieldsEditableMap != null) {
			String fieldName = field.getName();

			if (fieldName.equals(rule.uniqueKey())) {
				if (!isAdd && ObjectUtils.isEmpty(value)) {
					throw new InvalidArgumentException(fieldName, FieldErrors.EMPTY);
				}
			} else if (fieldsEditableMap.get(fieldName) == null) {
			} else if (null != value && !fieldsEditableMap.get(fieldName)) {
				throw new OperationNotAllowedException(fieldName);
			}
		}

		if (field.isAnnotationPresent(StringValidate.class)) {
			validateString(field, value, obj, rule);
		} else if (field.isAnnotationPresent(ComparableValidate.class)) {
			validateComparable(field, value, obj, rule);
		} else if (field.isAnnotationPresent(GeneralValidate.class)) {
			validateGeneral(field, value, obj, rule, depth);
		}
	}

	@SuppressWarnings("rawtypes")
	public void validateObject(Object obj, Validate rule, int depth, String... validateFields)
			throws InvalidArgumentException, OperationNotAllowedException {
		if (obj == null) {
			return;
		}

		var clszz = obj.getClass();
		// validate Array, like Permissions[]
		if (clszz.isArray()) {
			for (int idx = 0; idx < Array.getLength(obj); idx++) {
				try {
					validateObject(Array.get(obj, idx), rule, depth);
				} catch (IndexOutOfBoundsException e) {
				}
			}
			return;
		}

		// validate Map, like Map<String, Permissions>
		if (obj instanceof Map) {
			var mapValue = (Map) obj;
			for (Object item : mapValue.values()) {
				validateObject(item, rule, depth);
			}
			return;
		}

		// validate Map, like Set<Permissions>
		if (obj instanceof Set) {
			var setValue = (Set) obj;
			for (Object item : setValue) {
				validateObject(item, rule, depth);
			}
			return;
		}

		boolean isAdd = rule.nonEmptyStrategy() == NonEmptyStrategy.ADD;
		boolean isOperation = (rule.nonEmptyStrategy() & NonEmptyStrategy.OPERATION) != 0;
		var fieldsEditableMap = isOperation ? tableFieldsInfoService.getFieldsEditableMap(clszz, isAdd) : null;

		if (validateFields.length != 0) {
			for (String fieldName : List.of(validateFields)) {
				Field field = EntityUtil.getFieldIncludeSuperClasses(obj, fieldName);
				if (field == null) {
					continue;
				}
				logger.info("Validating field " + "    ".repeat(depth) + "`" + fieldName + "` of object `"
						+ clszz.getSimpleName() + "`");
				validateField(field, obj, rule, fieldsEditableMap, depth + 1);
			}
			return;
		}

		var fields = EntityUtil.getFieldsIncludeSuperClasses(obj);
		for (Field field : fields) {
			if (Modifier.isStatic(field.getModifiers())) {
				continue;
			}

			logger.info("Validating field " + "    ".repeat(depth) + "`" + field.getName() + "` of object `"
					+ clszz.getSimpleName() + "`");
			validateField(field, obj, rule, fieldsEditableMap, depth + 1);
		}
	}

	private void validateGeneral(Object descriptor, Object arg, Object argContainer, Validate rule, int depth)
			throws InvalidArgumentException, OperationNotAllowedException {
		if (descriptor instanceof Field) {
			var context = this.commonValiate(descriptor, arg, argContainer, rule, GeneralValidate.class);

			if (!context.validator.validateComponentType()) {
				return;
			}

			Field field = (Field) descriptor;
			if (field.isAnnotationPresent(Validate.class)) {
				rule = field.getAnnotation(Validate.class);
			}
		}

		validateObject(arg, rule, depth);
	}

	private void validateString(Object descriptor, Object value, Object argContainer, Validate rule)
			throws InvalidArgumentException {
		var context = this.commonValiate(descriptor, value, argContainer, rule, StringValidate.class);
		if (value == null) {
			return;
		}

		var validator = context.validator;
		var validatingFieldName = context.validatingFieldName;
		if (descriptor instanceof Field && validator.trim()) {
			Field field = (Field) descriptor;
			do {
				if (!(value instanceof String)) {
					return;
				}

				value = ((String) value).trim();
				try {
					field.set(argContainer, value);
				} catch (Exception e) {
					String msg = "Failed to set field value when validate trimable!";
					logger.error(msg + " reason: " + e.getMessage());
					throw new InternalError(msg);
				}
			} while (false);
		}
		String val = String.valueOf(value);

		// 格式不匹配
		if (!ObjectUtils.isEmpty(validator.regex())) {
			if (validator.minLen() == 0 && val.length() == 0) {
				return;
			} else if (!val.matches(validator.regex())) {
				throw new InvalidArgumentException(validatingFieldName, FieldErrors.FORMAT);
			}
		}

		// 长度不匹配
		if ((-1 != validator.len() && val.length() != validator.len())) {
			throw new InvalidArgumentException(validatingFieldName, FieldErrors.FORMAT);
		}

		// 长度小于最小长度
		if (-1 != validator.minLen() && val.length() < validator.minLen()) {
			throw new InvalidArgumentException(validatingFieldName, FieldErrors.TOO_SHORT);
		}

		// 长度大于最大长度
		if (-1 != validator.maxLen() && val.length() > validator.maxLen()) {
			throw new InvalidArgumentException(validatingFieldName, FieldErrors.TOO_LONG);
		}
	}

	private void validateComparable(Object descriptor, Object value, Object argContainer, Validate rule)
			throws InvalidArgumentException {
		var context = this.commonValiate(descriptor, value, argContainer, rule, ComparableValidate.class);
		if (value == null) {
			return;
		}

		var validator = context.validator;
		var validatingFieldName = context.validatingFieldName;

		if (value instanceof Enum) {
			try {
				Method method = value.getClass().getMethod(validator.enumKey());
				value = method.invoke(value);
			} catch (Exception e) {
				String msg = "Invalid configuration for `Comparable` annotation, the method specified by `enumKey` was not exists in type `"
						+ value.getClass().getSimpleName() + "`";

				throw new InternalError(msg);
			}
		}

		// validate value
		if (validator.value() != Long.MIN_VALUE) {
			if (compare(value, validator.value()) != 0) {
				throw new InvalidArgumentException(validatingFieldName, FieldErrors.INCORRECT);
			}
			return;
		}

		// validate value range
		if (validator.min() != Long.MIN_VALUE) {
			if (compare(value, validator.min()) < 0) {
				throw new InvalidArgumentException(validatingFieldName, FieldErrors.TOO_SMALL);
			}
		}
		if (validator.max() != Long.MIN_VALUE) {
			if (compare(value, validator.max()) > 0) {
				throw new InvalidArgumentException(validatingFieldName, FieldErrors.TOO_LARGE);
			}
		}
	}

	@Before("validatePointcutFilter()")
	public void validate(JoinPoint joinPoint) throws InvalidArgumentException, OperationNotAllowedException {
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();

		Object[] args = joinPoint.getArgs();
		Parameter[] params = signature.getMethod().getParameters();

		for (int idx = 0; idx < params.length; idx++) {
			Object curArg = args[idx];
			Parameter curParam = params[idx];

			// 参数为String类型时，检查是否有 StringValidate 验证
			if (curParam.getClass().equals(String.class)) {
				if (curParam.isAnnotationPresent(StringValidate.class)) {
					validateString(curParam, curArg, params, null);
				}
				continue;
			} else if (curParam.isAnnotationPresent(ComparableValidate.class)) {
				validateComparable(curParam, curArg, params, null);
			}

			// 参数为非对象类型时，直接跳过验证
			if (!(curArg instanceof Object)) {
				continue;
			}

			Validate rule = params[idx].getAnnotation(Validate.class);
			if (null == rule) {
				continue;
			}
			validateGeneral(curParam, curArg, params, rule, 0);
		}
	}
}

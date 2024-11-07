package cn.karelian.kas.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import cn.karelian.kas.utils.NonEmptyStrategy;

@Target({ ElementType.PARAMETER, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
/**
 * Dto对象字段校验，校验对象字段的可空性，可编辑性与字段值的规范性
 * <p>
 * 与 {@link StringValidate}, {@link ComparableValidate} 搭配使用于指定对象的字段上
 * </p>
 * 
 */
public @interface Validate {
	/**
	 * 对象字段可空策略，取自于 {@link NonEmptyStrategy}
	 */
	int nonEmptyStrategy() default NonEmptyStrategy.NONE;

	/**
	 * 对象更新时主键名称，校验更新时主键是否传递
	 */
	String uniqueKey() default "id";
}

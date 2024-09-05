package cn.karelian.kas.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import cn.karelian.kas.utils.NonEmptyStrategy;

@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Documented
/**
 * 字符串类型字段验证
 */
public @interface StringValidate {
	/**
	 * 校验字符串长度
	 */
	int len() default -1;

	/**
	 * 校验字符串长度不得小于该值
	 */
	int minLen() default -1;

	/**
	 * 校验字符串长度不得大于该值
	 */
	int maxLen() default -1;

	/**
	 * 校验字符串必须匹配给定正则
	 */
	String regex() default "";

	/**
	 * 校验可空性，取自于 {@link NonEmptyStrategy} 的逻辑或组合；
	 * 与 {@link Validate} 指定的 {@code nonEmptyStrategy}
	 * 的值进行二进制或运算，如果结果非零则字段不可为空。
	 */
	int nonEmptyStrategy() default NonEmptyStrategy.NONE;

	/**
	 * 依赖字段，当依赖字段不为空时，该字段必须不为空
	 * @return
	 */
	String dependsOn() default "";
}

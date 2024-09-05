package cn.karelian.kas.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import cn.karelian.kas.utils.NonEmptyStrategy;

@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
/**
 * 可比较类型字段验证
 */
public @interface ComparableValidate {
	/**
	 * 校验值必须大于该值，可与max结合使用，默认不校验。
	 */
	long min() default Long.MIN_VALUE;

	/**
	 * 验证值必须小于该值，可与min结合使用，默认不校验。
	 */
	long max() default Long.MIN_VALUE;

	/**
	 * 验证值必须等于该值，忽略 {@code min} 与 {@code max}，默认不校验。
	 */
	long value() default Long.MIN_VALUE;

	/**
	 * 校验可空性，取自于 {@link NonEmptyStrategy} 的逻辑或组合；
	 * 与 {@link Validate} 指定的 {@code nonEmptyStrategy}
	 * 的值进行二进制或运算，如果结果非零则字段不可为空。
	 */
	int nonEmptyStrategy() default NonEmptyStrategy.NONE;

	/**
	 * 依赖字段，当依赖字段不为空时，该字段必须不为空
	 * 
	 * @return
	 */
	String dependsOn() default "";
}

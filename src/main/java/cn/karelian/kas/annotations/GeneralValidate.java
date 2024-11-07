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
public @interface GeneralValidate {
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

	/**
	 * 当类型为 Array 或 Map 或 Set 时，是否校验元素类型
	 * 
	 * @return
	 */
	boolean validateComponentType() default false;
}

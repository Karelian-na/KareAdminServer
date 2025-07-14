package cn.karelian.kas.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestParamWithExtras {
	/**
	 * the name of the field to store the extra parameters in the request.
	 * 
	 * the target object's extras field must be a
	 * {@code java.util.Map<String, Object>}
	 * 
	 */
	String value() default "extras";

	/**
	 * the name of the field to store the files in the request.
	 * 
	 * the target object's files field must be a
	 * {@code org.springframework.util.MultiValueMap<String, MultipartFile>}
	 * 
	 */
	String files() default "files";
}

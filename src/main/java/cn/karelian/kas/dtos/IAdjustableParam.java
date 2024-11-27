package cn.karelian.kas.dtos;

import cn.karelian.kas.exceptions.InvalidArgumentException;

/**
 * param that can be regulared after been validated(annotation with
 * {@link cn.karelian.kas.annotations.Validate})
 * 
 * <p>
 * the member function {@code validateAndRegular} will be called after the param
 * been validated
 * </p>
 */
public interface IAdjustableParam {
	/**
	 * called after the param been validated
	 * 
	 * @throws InvalidArgumentException
	 */
	public void validateAndRegular() throws InvalidArgumentException;
}

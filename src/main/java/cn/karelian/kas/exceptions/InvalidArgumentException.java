/*
 * @Author: Karelian_na
 */
package cn.karelian.kas.exceptions;

import cn.karelian.kas.Result;
import cn.karelian.kas.codes.CommonErrors;
import cn.karelian.kas.codes.FieldErrors;
import lombok.Getter;

@Getter
public class InvalidArgumentException extends KasException {
	private Result result;

	public InvalidArgumentException(String argName) {
		super(CommonErrors.INVALID_ARGUMENT, argName);

		this.result = Result.fieldError(argName, FieldErrors.INVALID);
	}

	public InvalidArgumentException(String argName, String msg) {
		super(CommonErrors.INVALID_ARGUMENT, argName + "!" + msg);

		this.result = Result.fieldError(argName, msg);
	}

	public InvalidArgumentException(String argName, FieldErrors err) {
		super(CommonErrors.INVALID_ARGUMENT);

		this.result = Result.invalidArgument(argName, err);
	}
}

/*
 * @Author: Karelian_na
 */
package cn.karelian.kas.exceptions;

import cn.karelian.kas.codes.CommonErrors;

public class IllegalAccessException extends KasException {

	public IllegalAccessException() {
		super(CommonErrors.ILLEGAL_ACCESS);
	}
}

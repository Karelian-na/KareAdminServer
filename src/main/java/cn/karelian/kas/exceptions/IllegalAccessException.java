/*
 * @Author: Karelian_na
 */
package cn.karelian.kas.exceptions;

import cn.karelian.kas.codes.CommonErrors;

public class IllegalAccessException extends KasException {

	public IllegalAccessException(String msg) {
		super(CommonErrors.ILLEGAL_ACCESS, msg);
	}
}

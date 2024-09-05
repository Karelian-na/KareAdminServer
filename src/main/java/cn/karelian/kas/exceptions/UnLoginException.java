/*
 * @Author: Karelian_na
 */
package cn.karelian.kas.exceptions;

import cn.karelian.kas.codes.CommonErrors;

public class UnLoginException extends KasException {
	public UnLoginException() {
		super(CommonErrors.UN_LOGIN);
	}
}

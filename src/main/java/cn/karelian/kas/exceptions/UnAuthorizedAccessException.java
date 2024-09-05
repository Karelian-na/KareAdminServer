/*
 * @Author: Karelian_na
 */
package cn.karelian.kas.exceptions;

import cn.karelian.kas.codes.CommonErrors;

public class UnAuthorizedAccessException extends KasException {
	public UnAuthorizedAccessException() {
		super(CommonErrors.UN_AUTHORIZED);
	}
}

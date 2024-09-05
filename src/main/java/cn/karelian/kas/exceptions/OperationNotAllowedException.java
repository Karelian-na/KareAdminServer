/*
 * @Author: Karelian_na
 */
package cn.karelian.kas.exceptions;

import cn.karelian.kas.codes.CommonErrors;

public class OperationNotAllowedException extends KasException {

	public OperationNotAllowedException() {
		super(CommonErrors.OPERATION_NOT_ALLOWED);
	}

	public OperationNotAllowedException(String fieldName) {
		super(CommonErrors.OPERATION_NOT_ALLOWED, "不允许修改字段：" + fieldName);
	}
}

/*
 * @Author: Karelian_na
 */
package cn.karelian.kas.exceptions;

import cn.karelian.kas.codes.CommonErrors;

public class PermissionNotFoundException extends KasException {

	public PermissionNotFoundException() {
		super(CommonErrors.ILLEGAL_ACCESS);
	}
}

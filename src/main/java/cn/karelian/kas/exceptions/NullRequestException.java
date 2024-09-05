package cn.karelian.kas.exceptions;

import cn.karelian.kas.codes.CommonErrors;

/*
 * @Author: Karelian_na
 */

public class NullRequestException extends KasException {

	public NullRequestException() {
		super(CommonErrors.EMPTY_REQUEST);
	}

}

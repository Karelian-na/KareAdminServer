package cn.karelian.kas.exceptions;

import cn.karelian.kas.Result;

public class TransactionFailedException extends KasException {
	private Result result;

	public TransactionFailedException(Result result) {
		super(result.getCode(), result.getMsg());
		this.result = result;
	}

	public Result getResult() {
		return result;
	}
}

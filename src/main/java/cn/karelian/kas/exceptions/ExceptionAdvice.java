/*
 * @Author: Karelian_na
 */
package cn.karelian.kas.exceptions;

import java.lang.reflect.UndeclaredThrowableException;

import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import cn.karelian.kas.Result;
import cn.karelian.kas.codes.CommonErrors;

@RestControllerAdvice
public class ExceptionAdvice {
	@Order(0)
	@ExceptionHandler(Exception.class)
	public Result doException(Exception ex) {
		if (ex instanceof UndeclaredThrowableException && ex.getCause() instanceof Exception) {
			ex = (Exception) ex.getCause();
		}

		if (ex instanceof TransactionFailedException) {
			return ((TransactionFailedException) ex).getResult();
		}

		if (ex instanceof InvalidArgumentException) {
			return ((InvalidArgumentException) ex).getResult();
		}

		Result result = new Result();
		if (ex instanceof KasException) {
			result.setMsg(ex.getMessage());
			result.setCode(((KasException) ex).getCode());
		} else if (ex instanceof UndeclaredThrowableException) {
			KasException throwable = (KasException) ((UndeclaredThrowableException) ex).getUndeclaredThrowable();
			result.setMsg(throwable.getMessage());
			result.setCode(throwable.getCode());
		} else if (ex instanceof MaxUploadSizeExceededException) {
			result.setCode(CommonErrors.INVALID_ARGUMENT.getValue());
			result.setMsg("上传文件大小超过最大限制！");
		} else {
			result.setCode(CommonErrors.INTERNAL.getValue());
			result.setMsg(ex.getMessage());
		}
		return result;
	}
}

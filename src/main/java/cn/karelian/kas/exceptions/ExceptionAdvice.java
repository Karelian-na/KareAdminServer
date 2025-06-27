/*
 * @Author: Karelian_na
 */
package cn.karelian.kas.exceptions;

import java.lang.reflect.UndeclaredThrowableException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

import com.fasterxml.jackson.databind.JsonMappingException;

import cn.karelian.kas.Result;
import cn.karelian.kas.codes.CommonErrors;
import cn.karelian.kas.codes.FieldErrors;
import cn.karelian.kas.utils.ExceptionUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class ExceptionAdvice {
	private String writeUniqueErrorLog(Exception ex) {
		String ts = "[" + String.valueOf(LocalDateTime.now().toInstant(ZoneOffset.of("+8")).toEpochMilli()) + "]";
		ExceptionAdvice.log.error(ExceptionUtil.getMessage(ex) + ts);
		return ts;
	}

	@Order(1)
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public Result doHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
		Throwable cause = ex.getCause();
		if (cause instanceof JsonMappingException) {
			JsonMappingException jsonMappingException = (JsonMappingException) cause;

			String fieldName = null;
			var exPath = jsonMappingException.getPath();
			if (exPath != null && exPath.size() != 0) {
				fieldName = exPath.get(exPath.size() - 1).getFieldName();
			}

			if (fieldName != null) {
				return Result.fieldError(fieldName, FieldErrors.INVALID);
			}
		}

		Result result = new Result();
		result.error(CommonErrors.INVALID_ARGUMENT);
		return result;
	}

	@Order(1)
	@ExceptionHandler(MissingServletRequestParameterException.class)
	public Result doMissingServletRequestParameterException(MissingServletRequestParameterException ex) {
		String paramName = ex.getParameterName();
		return Result.fieldError(paramName, FieldErrors.EMPTY);
	}

	@Order(5)
	@ExceptionHandler(Exception.class)
	public Result doException(Exception ex) {
		if (ex instanceof UndeclaredThrowableException && ex.getCause() instanceof Exception) {
			ex = (Exception) ex.getCause();
		}

		// exceptions include result member or kasException
		if (ex instanceof TransactionFailedException) {
			return ((TransactionFailedException) ex).getResult();
		} else if (ex instanceof InvalidArgumentException) {
			return ((InvalidArgumentException) ex).getResult();
		} else if (ex instanceof KasException) {
			KasException kasException = (KasException) ex;
			return new Result(kasException.getCode(), kasException.getMessage());
		}

		Result result = new Result();
		// database operation exception
		if (ex instanceof DataAccessException || ex instanceof SQLException) {
			String ts = this.writeUniqueErrorLog(ex);
			result.error(CommonErrors.DATA_OPERATION, ts);
			return result;
		}

		// upload exceeded exception
		if (ex instanceof MultipartException) {
			result.error(CommonErrors.INVALID_ARGUMENT, "无效的文件！");
			return result;
		} else if (ex instanceof MaxUploadSizeExceededException) {
			result.error(CommonErrors.INVALID_ARGUMENT, "上传文件大小超过最大限制！");
			return result;
		}

		// undeclared kasException
		if (ex instanceof UndeclaredThrowableException) {
			KasException throwable = (KasException) ((UndeclaredThrowableException) ex).getUndeclaredThrowable();
			result.setMsg(throwable.getMessage());
			result.setCode(throwable.getCode());
			return result;
		}

		// unknown exception
		String ts = this.writeUniqueErrorLog(ex);
		result.error(CommonErrors.INTERNAL, ts);
		return result;
	}
}

/*
 * @Author: Karelian_na
 */
package cn.karelian.kas;

import lombok.Data;

import java.lang.reflect.Field;

import org.springframework.util.ObjectUtils;

import cn.karelian.kas.codes.CommonErrors;
import cn.karelian.kas.codes.FieldErrors;

@Data
public class Result {

	/**
	 * 指示当前请求是否成功!
	 */
	private int code;

	/**
	 * 指示当前请求是否成功!
	 */
	private boolean success;

	/**
	 * 指示当前请求附带消息!
	 */
	private String msg;

	/**
	 * 指示当前请求返回的数据!
	 */
	private Object data;

	public Result() {
		this.success = false;
		this.data = null;
		this.msg = "";
	}

	public Result(boolean success) {
		this.success = success;
		this.data = null;
		this.msg = null;
	}

	public Result(String msg) {
		this.success = false;
		this.data = null;
		this.msg = msg;
	}

	public Result(int code, String msg) {
		this.success = false;
		this.code = code;
		this.data = null;
		this.msg = msg;
	}

	public Result(Enum<?> val) {
		this.error(val);
	}

	public Result(boolean success, Object data) {
		this.success = success;
		this.data = success ? data : null;
		this.msg = null;
	}

	public Result(boolean success, String msg, Object data) {
		this.success = success;
		this.msg = msg;
		this.data = data;
	}

	public Result(boolean success, int code, Object data) {
		this.success = success;
		this.code = code;
		this.data = data;
	}

	public void setSuccess(Boolean value) {
		this.success = value != null && value;
	}

	@SuppressWarnings("unchecked")
	public <T> T getData(Class<T> clszz) {
		return (T) this.data;
	}

	public void fail(String msg) {
		this.success = false;
		this.msg = msg;
	}

	public void error(Enum<?> val, String msg) {
		this.success = false;
		this.data = null;
		Class<?> clszz = val.getDeclaringClass();
		try {
			Field field = clszz.getDeclaredField("value");
			field.setAccessible(true);

			this.code = field.getInt(val);

			field = clszz.getDeclaredField("description");
			if (null != field) {
				field.setAccessible(true);
				this.msg = (String) field.get(val);
				if (msg != null) {
					this.msg += "," + msg;
				}
			}
		} catch (Exception e) {
		}
	}

	public void error(Enum<?> val) {
		this.error(val, null);
	}

	public static Result internalError(String msg) {
		Result result = new Result(CommonErrors.INTERNAL);
		if (!ObjectUtils.isEmpty(msg)) {
			result.setMsg(result.getMsg() + ": " + msg);
		}
		return result;
	}

	public static Result fieldError(String field, FieldErrors err) {
		Result result = new Result(err);
		result.setMsg(result.getMsg() + ": " + field);
		return result;
	}

	public static Result fieldError(String field, String msg) {
		Result result = new Result(CommonErrors.INVALID_ARGUMENT);
		result.setMsg(result.getMsg() + ": " + field + ", " + msg);
		return result;
	}

	public static Result invalidArgument(String argName, FieldErrors err) {
		return Result.fieldError(argName, err);
	}
}

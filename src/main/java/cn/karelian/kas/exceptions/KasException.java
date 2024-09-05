package cn.karelian.kas.exceptions;

import java.lang.reflect.Field;

import cn.karelian.kas.codes.CommonErrors;

public class KasException extends Exception {
	protected int Code;

	private static String GetErrorMsgAndSetCode_(Enum<?> val) {
		Class<?> clszz = val.getDeclaringClass();
		try {
			Field field = clszz.getDeclaredField("description");
			if (null != field) {
				field.setAccessible(true);
				return (String) field.get(val);
			}
		} catch (Exception e) {
		}
		return null;
	}

	public int getCode() {
		return this.Code;
	}

	public KasException(String msg) {
		super(msg);
	}

	public KasException(Enum<?> val) {
		super(GetErrorMsgAndSetCode_(val));
		Class<?> clszz = val.getDeclaringClass();
		try {
			Field field = clszz.getDeclaredField("value");
			field.setAccessible(true);

			this.Code = field.getInt(val);
		} catch (Exception e) {
			this.Code = CommonErrors.INTERNAL.getValue();
		}
	}

	public KasException(Enum<?> val, String msg) {
		super(msg);
		Class<?> clszz = val.getDeclaringClass();
		try {
			Field field = clszz.getDeclaredField("value");
			field.setAccessible(true);

			this.Code = field.getInt(val);
		} catch (Exception e) {
		}
	}

	public KasException(int code, String msg) {
		super(msg);
		this.Code = code;
	}
}

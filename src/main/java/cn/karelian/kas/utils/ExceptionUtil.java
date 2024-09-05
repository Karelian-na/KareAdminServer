package cn.karelian.kas.utils;

public class ExceptionUtil {
	public static String getMessage(Throwable e) {
		while (e != null && e.getMessage() == null) {
			e = e.getCause();
		}

		return e == null ? null : e.getMessage();
	}
}

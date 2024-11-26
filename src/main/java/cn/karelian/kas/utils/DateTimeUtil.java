package cn.karelian.kas.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateTimeUtil {
	public static final String datePattern = "yyyy-MM-dd";
	public static final String timePattern = "HH:mm:ss";
	public static final String dateTimePattern = datePattern + " " + timePattern;

	public static final String dbDatePattern = "%Y-%m-%d";
	public static final String dbTimePattern = "%H:%i:%s";
	public static final String dbDateTimePattern = dbDatePattern + " " + dbTimePattern;

	public static LocalDate parseDate(String date) {
		if (date == null) {
			return null;
		}

		String trimedDate = date.trim();
		try {
			return LocalDate.parse(trimedDate, DateTimeFormatter.ofPattern("yyyy/MM/dd"));
		} catch (DateTimeParseException e) {
			return LocalDate.parse(trimedDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		}
	}

	public static LocalDateTime parseDateTime(String date) {
		if (date == null) {
			return null;
		}

		String trimedDate = date.trim();
		try {
			return LocalDateTime.parse(trimedDate, DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"));
		} catch (DateTimeParseException e) {
			return LocalDateTime.parse(trimedDate, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
		}
	}
}

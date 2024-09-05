package cn.karelian.kas.codes;

import lombok.Getter;

@Getter
public enum FieldErrors {
	/**
	 * 空字段
	 */
	EMPTY("缺少字段"),
	/**
	 * 字段值无效
	 */
	INVALID("字段值无效"),
	/**
	 * 字段值过长
	 */
	TOO_LONG("字段值过长"),
	/**
	 * 字段值过短
	 */
	TOO_SHORT("字段值过短"),
	/**
	 * 字段值过大
	 */
	TOO_LARGE("字段值过大"),
	/**
	 * 字段值过小
	 */
	TOO_SMALL("字段值过小"),
	/**
	 * 字段格式错误
	 */
	FORMAT("字段格式错误"),
	/**
	 * 字段值不正确
	 */
	INCORRECT("字段值不正确");

	private static final int START = 0x6000001;

	private int value;
	private String description;

	FieldErrors(String description) {
		this.value = ordinal() + START;
		this.description = description;
	}
}

package cn.karelian.kas.codes;

import lombok.Getter;

@Getter
public enum CommonErrors {
	/**
	 * 无
	 */
	NONE(""),
	/**
	 * 内部错误
	 */
	INTERNAL("服务器内部错误"),
	/**
	 * 空请求错误（未遇到）
	 */
	EMPTY_REQUEST("空请求"),
	/**
	 * 非法的访问
	 */
	ILLEGAL_ACCESS("非法访问"),
	/**
	 * 用户未登录
	 */
	UN_LOGIN("登陆状态失效或未登录"),
	/**
	 * 未经授权的访问
	 */
	UN_AUTHORIZED("未经授权的访问"),
	/**
	 * 无效的参数
	 */
	INVALID_ARGUMENT("无效的参数"),
	/**
	 * 文件不存在
	 */
	FILE_NOT_FOUND("指定文件不存在"),
	/**
	 * 事务错误
	 */
	TRANSACTION("事务错误"),
	/**
	 * 事务错误
	 */
	OPERATION_NOT_ALLOWED("操作不允许");

	private static final int START = 0x5000001;

	private int value;
	private String description;

	CommonErrors(String description) {
		this.value = ordinal() + START;
		this.description = description;
	}

}

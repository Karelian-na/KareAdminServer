package cn.karelian.kas.codes;

import lombok.Getter;

@Getter
public enum LocalStorageErrors {
	/**
	 * 无
	 */
	NONE(""),
	/**
	 * 无效的文件参数
	 */
	INVALID_FILE_ATTR("无效的文件参数！"),
	/**
	 * 未配置本地存储
	 */
	INVALID_CONFIG("内部存储不支持！"),
	/**
	 * 临时文件不存在
	 */
	TEMP_FILE_NOT_FOUND("文件不存在，尝试重新上传文件！"),
	/**
	 * 创建分组存储文件夹失败
	 */
	CREATE_CATEGORY_DIR("创建存储分区失败！"),
	/**
	 * 临时文件复制失败
	 */
	COPY("保存文件失败！"),
	;

	private static final int START = 0x5000001;

	private int value;
	private String description;

	LocalStorageErrors(String description) {
		this.value = ordinal() + START;
		this.description = description;
	}
}

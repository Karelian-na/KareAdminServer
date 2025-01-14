package cn.karelian.kas.configs;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.springframework.util.ObjectUtils;

interface BaseConfig {
	/**
	 * 检查配置有效性并规整配置
	 * 
	 * @return 错误信息
	 */
	public String checkValidationAndRegularization(Object base);
}

// 资源存储模式
enum StorageMode {
	// 使用本地存储
	Local,

	// 使用 CDN 服务存储
	Cdn
}

public class KasConfig implements BaseConfig {
	// 服务主机名
	public String host;

	// 是否记录日志
	public boolean log;

	// 留痕字段
	public List<String> traceFields;

	// -------------------------
	// 资源存储模式
	public StorageMode storageMode;

	// 默认头像
	public String defaultAvatar;

	// 本地存储配置
	public LocalStorageConfig localStorageConfig;

	// Cdn存储配置
	public CdnStorageConfig cdnStorageConfig;
	// -------------------------

	// 检查配置有效性并规整默认配置
	@Override
	public String checkValidationAndRegularization(Object base) {
		if (ObjectUtils.isEmpty(this.host)) {
			return "The server config `host` shouldn't be empty!";
		}

		if (null == this.traceFields) {
			this.traceFields = new ArrayList<>();
		}

		// 本地存储时，规整并校验存储配置
		if (storageMode == StorageMode.Local) {
			if (ObjectUtils.isEmpty(localStorageConfig)) {
				return "The server config `localStorageConfig` shouldn't be empty when `storageMode` is `local`!";
			}

			String errorMessage = localStorageConfig.checkValidationAndRegularization(this);
			if (!ObjectUtils.isEmpty(errorMessage)) {
				return errorMessage;
			}
		}

		if (!ObjectUtils.isEmpty(defaultAvatar)) {
			URI uri = URI.create(defaultAvatar);
			if (!uri.isAbsolute()) {
				return "defaultAvatar should be an internet absolute uri!";
			}
		}

		return null;
	}
}

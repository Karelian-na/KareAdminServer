package cn.karelian.kas.configs;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.springframework.util.ObjectUtils;

import cn.karelian.kas.KasApplication;

public class LocalStorageConfig implements BaseConfig {
	/* 上传文件的临时目录 */
	public Path tempPath;

	/* 通用文件存储路径 */
	public Path commonFilePath;

	/* 资源分类路径映射 */
	public Map<String, Path> resourceCategoriesPathMap;

	/* 资源分类公网路径映射 */
	public Map<String, String> resourceCategoriesUriPrefixMap;

	/* 对外公网访问的 uri 前缀 */
	public URI publicUriPrefix;

	/* 外网资源路径映射 */
	public Map<String, String> publicResourcesPathMap;

	/**
	 * 规整单一路径
	 * 
	 * @param path                路径
	 * @param defaultRelativePath 默认的 相对服务器启动路径
	 * @return
	 */
	private Path regularSinglePath(Path path, String defaultRelativePath) throws Exception {
		if (Path.of(defaultRelativePath).isAbsolute()) {
			throw new Exception("The path `" + defaultRelativePath + "` was not a relative path!");
		}

		if (ObjectUtils.isEmpty(path)) {
			path = KasApplication.currentPath.resolve(defaultRelativePath);
		} else if (!path.isAbsolute()) {
			path = KasApplication.currentPath.resolve(path);
		}

		if (!Files.exists(path)) {
			Files.createDirectories(path);
		}

		return path;
	}

	// 检查有效性并规整化参数
	@Override
	public String checkValidationAndRegularization(Object base) {
		KasConfig baseConfig = (KasConfig) base;

		try {
			// 临时文件路径
			tempPath = this.regularSinglePath(tempPath, "data/temp");
			// 通用文件路径
			commonFilePath = this.regularSinglePath(commonFilePath, "data/files");
			// 其他资源分类路径映射
			if (null != this.resourceCategoriesPathMap) {
				for (var entry : resourceCategoriesPathMap.entrySet()) {
					resourceCategoriesPathMap.replace(entry.getKey(),
							this.regularSinglePath(entry.getValue(), "data/" + entry.getKey()));
				}
			}
		} catch (Exception e) {
			return e.getMessage();
		}

		// 对外公网访问 url 前缀
		if (ObjectUtils.isEmpty(publicUriPrefix)) {
			try {
				publicUriPrefix = new URI(baseConfig.host);
			} catch (Exception e) {
				return e.getMessage();
			}
		}

		return null;
	}
}

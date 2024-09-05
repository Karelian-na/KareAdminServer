package cn.karelian.kas.utils;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.BiFunction;

import org.apache.tomcat.util.buf.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ObjectUtils;

import cn.karelian.kas.KasApplication;
import cn.karelian.kas.codes.LocalStorageErrors;
import cn.karelian.kas.configs.LocalStorageConfig;
import cn.karelian.kas.exceptions.KasException;
import lombok.Getter;

public final class LocalStorageUtil {
	private static class TempFileCopyContext {
		LocalStorageErrors errorCode;
		String publicPath;
	}

	private static Logger logger = LoggerFactory.getLogger(LocalStorageUtil.class);
	private static MessageDigest md5Digest = null;

	static {
		try {
			md5Digest = MessageDigest.getInstance("MD5");
		} catch (Exception e) {
			logger.error("Failed to init md5Digest instance!");
			System.exit(0);
		}
	}

	public static Path getLocalFilePath(String path, BiFunction<Path, String, Path> resolvePath) {
		LocalStorageConfig config = KasApplication.configs.localStorageConfig;
		if (null == path || null == config) {
			return null;
		}

		String prefixStr = config.publicUriPrefix.toString();
		if (!path.startsWith(prefixStr, 0)) {
			return null;
		}

		String pathWithoutHost = path.substring(prefixStr.length());
		if (pathWithoutHost.startsWith("/")) {
			pathWithoutHost = pathWithoutHost.substring(1);
		}

		String category = "";
		String lefPath = pathWithoutHost;
		if (null != config.resourceCategoriesUriPrefixMap) {
			for (var entry : config.resourceCategoriesUriPrefixMap.entrySet()) {
				final String uriPrefix = entry.getValue();
				if (pathWithoutHost.startsWith(entry.getValue())) {
					category = entry.getKey();
					if (pathWithoutHost.length() == uriPrefix.length()) {
						lefPath = "";
					} else {
						lefPath = pathWithoutHost.substring(uriPrefix.length() + 1);
					}
					break;
				}
			}
		}

		Path localPath = config.resourceCategoriesPathMap.get(category);

		if (null == localPath) {
			localPath = config.commonFilePath;
		}

		if (null == resolvePath) {
			return localPath.resolve(lefPath);
		}

		return resolvePath.apply(localPath, lefPath);
	}

	public static Path getLocalFilePath(String path) {
		return getLocalFilePath(path, null);
	}

	private static TempFileCopyContext CopyTempFileToSpecifiedCategory(String category, String fileName,
			boolean deleteOld) {
		TempFileCopyContext context = new TempFileCopyContext();

		LocalStorageConfig config = KasApplication.configs.localStorageConfig;
		if (null == fileName || null == config) {
			context.errorCode = LocalStorageErrors.INVALID_CONFIG;
			return context;
		}

		Path tempPath = config.tempPath.resolve(fileName.toString());
		if (!Files.exists(tempPath)) {
			context.errorCode = LocalStorageErrors.TEMP_FILE_NOT_FOUND;
			return context;
		}

		URI publicUrlPrefix = config.publicUriPrefix.resolve("files");
		Path localCategoryPath = config.commonFilePath;
		if (!ObjectUtils.isEmpty(category) && null != config.resourceCategoriesPathMap) {
			Path categoryPath = config.resourceCategoriesPathMap.get(category);
			if (null != categoryPath) {
				localCategoryPath = categoryPath;
			}

			if (null != config.resourceCategoriesUriPrefixMap) {
				String urlPrefix = config.resourceCategoriesUriPrefixMap.get(category);
				if (!ObjectUtils.isEmpty(urlPrefix)) {
					publicUrlPrefix = config.publicUriPrefix.resolve(urlPrefix);
				}
			}
		}

		if (!Files.exists(localCategoryPath)) {
			try {
				Files.createDirectories(localCategoryPath);
			} catch (Exception e) {
				logger.error("Failed to create local category stroage directory, details " + e.getMessage());
				context.errorCode = LocalStorageErrors.CREATE_CATEGORY_DIR;
				return context;
			}
		}

		String date = "";
		String finalFileName = fileName;

		String fileNameWithoutExt = LocalStorageUtil.getFileNameWithoutExtension(finalFileName);
		String fileExtension = LocalStorageUtil.getFileExtension(fileName);

		Path newPath = localCategoryPath.resolve(finalFileName);
		while (Files.exists(newPath)) {
			date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
			finalFileName = fileNameWithoutExt + date + fileExtension;
			newPath = localCategoryPath.resolve(finalFileName);
		}

		try {
			if (deleteOld) {
				Files.move(tempPath, newPath);
			} else {
				Files.copy(tempPath, newPath);
			}
			context.publicPath = publicUrlPrefix.toString() + "/" + finalFileName;
		} catch (Exception e) {
			context.errorCode = LocalStorageErrors.COPY;
		}

		return context;
	}

	/**
	 * judge if the temp file exists
	 * 
	 * @param fileName the actual temp file name, handled by method
	 *                 {@code getTempFileName}
	 * @return true if the file exists, or else
	 */
	public static boolean isTempFileExists(String fileName) {
		LocalStorageConfig config = KasApplication.configs.localStorageConfig;
		if (null == fileName || null == config) {
			return false;
		}

		Path path = config.tempPath.resolve(fileName);
		return Files.exists(path);
	}

	/**
	 * get the temp file's size
	 * 
	 * @param fileName the actual temp file name, handled by method
	 *                 {@code getTempFileName}
	 * @return -1 when the file not exists, else the temp file's size
	 */
	public static long getTempFileSize(String fileName) {
		LocalStorageConfig config = KasApplication.configs.localStorageConfig;
		if (null == fileName || null == config) {
			return -1;
		}

		Path path = config.tempPath.resolve(fileName);
		if (!Files.exists(path)) {
			return -1;
		}

		try {
			return Files.size(path);
		} catch (IOException e) {
			logger.error("Failed to get temp file size, details: " + e.getMessage());
		}

		return -1;
	}

	/**
	 * get the uploaded files's corresponding temp file name
	 * 
	 * @param uploadedPath the uploaded name, handled by
	 *                     {@code GeneralService's upload method}, format with
	 *                     `${fileSHA512}${extension}`
	 * @return the tempFile's actual name, formart with
	 *         `${fileHash}${fileExtension}`, `${fileExtension}` includes char `.`
	 * @throws KasException throws when get the file size failed
	 */
	public static String getTempFileName(String uploadedPath) {
		if (null == uploadedPath) {
			return null;
		}

		String fileExtension = "";

		int dotIdx = uploadedPath.lastIndexOf(".");
		if (-1 != dotIdx) {
			fileExtension = uploadedPath.substring(dotIdx);
		}

		// 该摘要值加上文件扩展名 为存放在临时文件目录的实际文件名
		md5Digest.reset();
		String hashNameHash = HexUtils.toHexString(md5Digest.digest(uploadedPath.getBytes()));

		return hashNameHash + fileExtension;
	}

	/**
	 * get the uploaded files's corresponding temp file attribute
	 * 
	 * @param uploadedPath the uploaded name, handled by
	 *                     {@code GeneralService's upload method}, format with
	 *                     `${fileSHA512}${extension}`
	 * @return the temp file attribute, includes name, size, extension, hash
	 * @throws KasException throws when get the file size failed
	 */
	public static TempFileAttributes getTempFileAttribute(String uploadedPath) throws KasException {
		if (null == uploadedPath) {
			return null;
		}
		String actualTempFileName = LocalStorageUtil.getTempFileName(uploadedPath);

		long size = LocalStorageUtil.getTempFileSize(actualTempFileName);
		if (size == -1) {
			return null;
		}

		int dotIdx = actualTempFileName.lastIndexOf(".");
		String fileExtension = dotIdx == -1 ? "" : actualTempFileName.substring(dotIdx);
		String fileHash = dotIdx == -1 ? actualTempFileName : actualTempFileName.substring(0, dotIdx);

		TempFileAttributes tempFileAttribute = new TempFileAttributes();
		tempFileAttribute.name = actualTempFileName;
		tempFileAttribute.size = size;
		tempFileAttribute.extension = fileExtension;
		tempFileAttribute.hash = fileHash;

		return tempFileAttribute;
	}

	/**
	 * 原子操作将 临时文件 移动至 指定的资源分组的路径
	 * 
	 * @param files    将要移动的临时文件的名称
	 * @param category 将要移动的文件的分组，对应
	 *                 {@code KasConfig.localStorageConfig.resourceCatogoriesPathMap}
	 *                 的键值
	 * @return
	 */
	public static AtomicMoveFileHandle atomicMoveTempFiles(String[] files, String category) {
		AtomicMoveFileHandle handle = new AtomicMoveFileHandle(files);
		handle.execute(category);
		return handle;
	}

	/**
	 * 原子操作将 临时文件 移动至 默认资源分组的路径
	 * 
	 * @param file     将要移动的临时文件的名称
	 * @param category 将要移动的文件的分组，对应
	 *                 {@code KasConfig.localStorageConfig.resourceCatogoriesPathMap}
	 *                 的键值
	 * @return
	 */
	public static AtomicMoveFileHandle atomicMoveTempFiles(String[] files) {
		return atomicMoveTempFiles(files, "");
	}

	/**
	 * 原子操作将 临时文件 移动至 指定的资源分组的路径
	 * 
	 * @param file     将要移动的临时文件的名称
	 * @param category 将要移动的文件的分组，对应
	 *                 {@code KasConfig.localStorageConfig.resourceCatogoriesPathMap}
	 *                 的键值
	 * @return
	 */
	public static AtomicMoveFileHandle atomicMoveTempFiles(String file, String category) {
		AtomicMoveFileHandle handle = new AtomicMoveFileHandle(new String[] { file });
		handle.execute(category);
		return handle;
	}

	public static String getFileExtension(String fileName) {
		if (fileName == null || fileName.isEmpty()) {
			return ""; // 空文件名返回空字符串
		}

		int lastDotIndex = fileName.lastIndexOf('.');
		if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
			return ""; // 没有扩展名或扩展名为空
		}

		return fileName.substring(lastDotIndex);
	}

	public static String getFileNameWithoutExtension(String fileName) {
		if (fileName == null || fileName.isEmpty()) {
			return ""; // 空文件名返回空字符串
		}

		int lastDotIndex = fileName.lastIndexOf('.');
		if (lastDotIndex == -1) {
			return fileName; // 没有扩展名时，返回整个文件名
		}

		return fileName.substring(0, lastDotIndex);
	}

	public static class TempFileAttributes {
		public String name;
		public String extension;
		public String hash;
		public long size;
	}

	public static class AtomicMoveFileHandle {
		private static Logger logger = LoggerFactory.getLogger(AtomicMoveFileHandle.class);

		@Getter
		private boolean success;
		@Getter
		private String[] publicPaths;
		@Getter
		private LocalStorageErrors errorCode;

		private int succeededCount;
		private String[] tempFiles;

		public AtomicMoveFileHandle(String[] tempFiles) {
			this.errorCode = LocalStorageErrors.NONE;
			this.tempFiles = tempFiles;
			this.succeededCount = 0;
			this.publicPaths = new String[tempFiles.length];
		}

		public void rollback() {
			if (null == KasApplication.configs.localStorageConfig) {
				return;
			}

			var localStorageConfig = KasApplication.configs.localStorageConfig;
			for (int subidx = 0; subidx < this.succeededCount; ++subidx) {
				String path = publicPaths[subidx];
				Path localPath = LocalStorageUtil.getLocalFilePath(path);

				if (!Files.exists(localPath)) {
					continue;
				}

				String localFileName = localPath.getFileName().toString();
				String fileName = LocalStorageUtil.getFileNameWithoutExtension(localFileName);
				String fileExtension = LocalStorageUtil.getFileExtension(localFileName);

				String fileNameWithoutDateSalt = (fileName.length() > 32 ? fileName.substring(0, 32) : fileName)
						+ fileExtension;

				Path tempPath = localStorageConfig.tempPath.resolve(fileNameWithoutDateSalt);

				try {
					if (Files.exists(tempPath)) {
						Files.delete(localPath);
					} else {
						Files.move(localPath, tempPath);
					}
				} catch (Exception e) {
					logger.error("Failed to rollback file `" + localPath + "`, details: " + e.getMessage());
				}
			}
		}

		public void execute(String category) {
			for (int idx = 0; idx < tempFiles.length; idx++) {
				String tempFileName = LocalStorageUtil.getTempFileName(tempFiles[idx]);

				// 期间如果有一个文件移动失败则删除所有已经移除的文件, 并尝试将其移动至临时目录
				if (tempFileName == null) {
					this.errorCode = LocalStorageErrors.INVALID_FILE_ATTR;
					this.success = false;
					this.rollback();
					return;
				}

				// 期间如果有一个文件移动失败则删除所有已经移除的文件, 并尝试将其移动至临时目录
				TempFileCopyContext context = LocalStorageUtil.CopyTempFileToSpecifiedCategory(category, tempFileName,
						true);
				if (context.publicPath == null) {
					this.errorCode = context.errorCode;
					this.success = false;
					this.rollback();
					return;
				}

				this.succeededCount = idx + 1;
				publicPaths[idx] = context.publicPath;
			}

			this.success = true;
		}
	}
}

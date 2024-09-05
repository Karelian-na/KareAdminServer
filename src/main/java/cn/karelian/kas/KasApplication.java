package cn.karelian.kas;

import java.io.FileInputStream;
import java.nio.file.Path;

import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.util.ObjectUtils;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.fasterxml.jackson.databind.ObjectMapper;

import cn.karelian.kas.configs.KasConfig;
import cn.karelian.kas.views.FieldsInfoView;
import cn.karelian.kas.views.MenusView;
import cn.karelian.kas.views.PermissionsView;
import cn.karelian.kas.views.UsermsgsView;
import cn.karelian.kas.views.Views;

@SpringBootApplication
@MapperScan({ "cn.karelian.kas.mappers.**" })
public class KasApplication {
	public static final Path currentPath;

	public static KasConfig configs;

	static {
		Logger logger = LoggerFactory.getLogger(KasApplication.class);

		// 初始化服务器运行目录
		String path = new ApplicationHome(KasApplication.class).getSource().getParentFile().getPath();
		logger.info("KasServer is running at: " + path);
		if (path.endsWith("target")) {
			path = path.substring(0, path.length() - "/target".length());
		}
		currentPath = Path.of(path);

		// 读取服务器配置
		try {
			Path serverConfigPath = KasApplication.currentPath.resolve("data/configs/server.json");
			FileInputStream fileInputStream = new FileInputStream(serverConfigPath.toFile());
			ObjectMapper objectMapper = new ObjectMapper();
			KasApplication.configs = objectMapper.readValue(fileInputStream, KasConfig.class);
			if (null == KasApplication.configs) {
				KasApplication.configs = new KasConfig();
			}
		} catch (Exception e) {
			logger.error("Error parse server config: " + e.getMessage());
			System.exit(0);
		}

		// 检查服务器配置有效性并规整配置
		String errorMessage = KasApplication.configs.checkValidationAndRegularization(null);
		if (!ObjectUtils.isEmpty(errorMessage)) {
			logger.error(errorMessage);
			System.exit(0);
		}

		MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(), "");
		TableInfoHelper.initTableInfo(assistant, FieldsInfoView.class);
		TableInfoHelper.initTableInfo(assistant, MenusView.class);
		TableInfoHelper.initTableInfo(assistant, UsermsgsView.class);
		TableInfoHelper.initTableInfo(assistant, Views.class);
		TableInfoHelper.initTableInfo(assistant, PermissionsView.class);
	}

	public static void main(String[] args) {
		SpringApplication.run(KasApplication.class, args);
	}
}

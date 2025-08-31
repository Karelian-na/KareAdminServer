package cn.karelian.kas;

import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.ibatis.jdbc.ScriptRunner;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import cn.karelian.kas.utils.DateTimeUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class DataInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
	private String databaseUrl;
	private String databaseUserName;
	private String databasePassword;
	private String extraSchema;

	private String databaseInitUser;
	private String databaseInitPassword;

	private void readConfigurations(ConfigurableApplicationContext applicationContext) {
		ConfigurableEnvironment environment = applicationContext.getEnvironment();
		this.databaseUrl = environment.getProperty("spring.datasource.url");
		this.databaseUserName = environment.getProperty("spring.datasource.username");
		this.databasePassword = environment.getProperty("spring.datasource.password");
		this.extraSchema = environment.getProperty("kas.datasource.extraSchema");

		this.databaseInitUser = environment.getProperty("kas.datasource.dbInitUser");
		this.databaseInitPassword = environment.getProperty("kas.datasource.dbInitPassword");
	}

	private void initializeDatabase(String databaseName, Connection connection) throws Exception {
		ResourceLoader resourceLoader = new DefaultResourceLoader();
		// read kas database schema SQL file
		String placeholderSql = null;
		Resource resource = resourceLoader.getResource("classpath:/Database.sql");
		try (InputStream inputStream = resource.getInputStream()) {
			placeholderSql = new String(inputStream.readAllBytes(), "UTF-8");
		} catch (Exception e) {
			log.error("Failed to read basic SQL file: " + resource.getFilename(), e);
			System.exit(1);
			return;
		}
		if (placeholderSql == null || ObjectUtils.isEmpty(placeholderSql)) {
			log.error("Invalid SQL initialization file: " + resource.getFilename());
			System.exit(1);
			return;
		}

		// Replace placeholders in the SQL script
		Map<String, String> replacements = Map.of(
				"${dbName}", databaseName //
				, "${dbUser}", databaseUserName //
				, "${dbPwd}", databasePassword //
				, "${defaultAvatar}", KasApplication.configs.defaultAvatar //
				, "${superAdminId}", String.valueOf(KasApplication.superAdminId) //
				, "${superAdminRoleId}", String.valueOf(KasApplication.superAdminRoleId) //
				, "${adminRoleId}", String.valueOf(KasApplication.adminRoleId) //
				, "${commonUserRoleId}", String.valueOf(KasApplication.commonUserRoleId) //
		);

		String placeholders = replacements.keySet().stream().map(item -> {
			return item.substring(2, item.length() - 1); // remove ${ and }
		}).collect(Collectors.joining("|"));
		Pattern pattern = Pattern.compile("(\\$\\{(?:" + placeholders + ")\\})");
		Matcher matcher = pattern.matcher(placeholderSql);

		StringBuilder resSql = new StringBuilder();
		while (matcher.find()) {
			String placeholder = matcher.group(1);
			String replacement = replacements.get(placeholder);
			if (replacement == null) {
				log.error("Placeholder not found in replacements: " + placeholder);
				System.exit(1);
				return;
			}

			matcher.appendReplacement(resSql, replacement);
		}
		matcher.appendTail(resSql);

		// create replaced SQL script file for execution
		Path actualScriptFile = Path.of(System.getProperty("java.io.tmpdir"),
				"kas_temp_sql_execution_" + DateTimeUtil.formatDateTimeNoSeparator(LocalDateTime.now()) + ".sql");
		try {
			Files.writeString(actualScriptFile, resSql.toString());
		} catch (Exception e) {
			log.error("Failed to write replaced basic SQL schema script to file: "
					+ actualScriptFile + ", reason: " + e.getMessage());
			System.exit(1);
			return;
		}

		ScriptRunner scriptRunner = new ScriptRunner(connection);
		scriptRunner.setAutoCommit(false);
		scriptRunner.setLogWriter(null);
		scriptRunner.setStopOnError(true);

		// execute the basic SQL schema script to create database and built-in tables
		log.info("Executing basic SQL initialization script, details see file: " + actualScriptFile);
		try (FileReader reader = new FileReader(actualScriptFile.toFile(), Charset.forName("UTF-8"))) {
			scriptRunner.runScript(reader);
			log.info("Successfully executed basic SQL initialization script!");
		} catch (Exception e) {
			log.error("Failed to execute basic SQL initialization script: " + actualScriptFile + ", reason: "
					+ e.getMessage());
			throw e;
		}

		// execute extra SQL schema script if configured
		if (ObjectUtils.isEmpty(extraSchema)) {
			return;
		}

		resource = resourceLoader.getResource(extraSchema);
		if (!resource.exists()) {
			return;
		}

		try (InputStreamReader reader = new InputStreamReader(resource.getInputStream(), Charset.forName("UTF-8"))) {
			log.info("Executing extra SQL schema script: " + resource.getFilename());
			scriptRunner.runScript(reader);
			log.info("Successfully executed extra SQL schema script: " + resource.getFilename());
		} catch (Exception e) {
			log.error("Failed to execute extra SQL schema script: " + resource.getFilename() + ", reason: "
					+ e.getMessage());
			throw e;
		}
	}

	@Override
	@SuppressWarnings("null")
	public void initialize(ConfigurableApplicationContext applicationContext) {
		this.readConfigurations(applicationContext);

		// get database name from configuration `spring.datasource.url`
		int questionMarkPos = databaseUrl.indexOf("?");
		int lastSlashPos = databaseUrl.lastIndexOf("/");
		if (questionMarkPos == -1 || lastSlashPos == -1 || lastSlashPos > questionMarkPos) {
			log.error("Couldn't get database name from configuration `spring.datasource.url`: " + databaseUrl);
			System.exit(1);
		}

		String dbName = databaseUrl.substring(lastSlashPos + 1, questionMarkPos);
		String dataUrlWithDefaultDb = databaseUrl.substring(0, lastSlashPos + 1) + "information_schema"
				+ databaseUrl.substring(questionMarkPos);

		boolean isDatabaseExists = false;
		Connection connection;
		try {
			connection = DriverManager.getConnection(dataUrlWithDefaultDb, databaseInitUser, databaseInitPassword);
			ResultSet rs = connection.getMetaData().getCatalogs();
			while (rs.next()) {
				if (rs.getString(1).equalsIgnoreCase(dbName)) {
					isDatabaseExists = true;
					break;
				}
			}
		} catch (Exception e) {
			log.error("Failed to connect to basic database: mysql, reason: " + e.getMessage());
			System.exit(1);
			return;
		}

		Exception dbInitException = null;
		if (!isDatabaseExists) {
			log.info("Initializing database: " + dbName);
			try {
				this.initializeDatabase(dbName, connection);
				connection.commit();
			} catch (Exception e) {
				dbInitException = e;
			}
		}

		if (dbInitException != null) {
			try {
				connection.rollback();
				connection.close();
			} catch (Exception ex) {
			}
			System.exit(1);
			return;
		}

		// close the connection
		try {
			connection.close();
		} catch (Exception e) {
		}
	}
}

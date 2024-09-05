package cn.karelian.kas;

import java.util.Collections;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.fill.Column;

import cn.karelian.kas.mappers.KasMapper;
import cn.karelian.kas.services.KasService;
import cn.karelian.kas.services.interfaces.IKasService;

public class TableGenerator {
	public static void main(String[] args) {
		FastAutoGenerator generator = FastAutoGenerator
				.create("jdbc:mysql://127.0.0.1:3306/kas?serverTimeZone=UTC", "kas_user", "123456789")
				.globalConfig(builder -> {
					builder
							.outputDir("src/main/java")
							.disableOpenDir()
							.author("Karelian_na");
				}).packageConfig(builder -> {
					builder.parent("cn.karelian.kas")
							.service("services.interfaces")
							.serviceImpl("services")
							.controller("controllers")
							.entity("entities")
							.mapper("mappers")
							.pathInfo(Collections.singletonMap(OutputFile.xml, "src/main/resources/mappers"));
				}).strategyConfig(builder -> {
					builder.entityBuilder()
							.addTableFills(new Column[] {
									new Column("add_uid", FieldFill.INSERT),
									new Column("add_user", FieldFill.INSERT),
									new Column("add_time", FieldFill.INSERT),
									new Column("update_time", FieldFill.UPDATE),
									new Column("update_user", FieldFill.UPDATE),
									new Column("update_uid", FieldFill.UPDATE),
							})
							.logicDeleteColumnName("deleted")
							.columnNaming(NamingStrategy.no_change)
							.enableLombok();

					builder.controllerBuilder()
							.enableHyphenStyle()
							.enableRestStyle();

					builder.serviceBuilder()
							.superServiceClass(IKasService.class)
							.superServiceImplClass(KasService.class)
							.formatServiceFileName("I%sService")
							.formatServiceImplFileName("%sService");

					builder.mapperBuilder()
							.superClass(KasMapper.class)
							.superClass(KasMapper.class);

					builder.addInclude(new String[] {
							// "logs",
							// "permissions",
							// "menus",
							// "role_perm_assoc",
							// "roles",
							// "table_fields_info",
							// "user_perm_assoc",
							// "user_role_assoc",
							// "usermsgs",
							// "users",
					});
				});

		generator.execute();
	}
}

package cn.karelian.kas;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.TemplateType;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;

import cn.karelian.kas.mappers.KasMapper;

public class ViewGenerator {
	public static void main(String[] args) {
		FastAutoGenerator generator = FastAutoGenerator
				.create("jdbc:mysql://127.0.0.1:3306/kas?serverTimeZone=UTC", "kas_user", "123456789")
				.globalConfig(builder -> {
					builder
							.outputDir("src/main/java")
							.disableOpenDir()
							.author("Karelian_na");
				}).templateConfig(builder -> {
					builder.disable(TemplateType.CONTROLLER)
							.disable(TemplateType.SERVICE)
							.disable(TemplateType.SERVICE_IMPL)
							.disable(TemplateType.MAPPER)
							.disable(TemplateType.XML);
				}).packageConfig(builder -> {
					builder.parent("cn.karelian.kas")
							.entity("views");
				}).strategyConfig(builder -> {
					builder.entityBuilder()
							.columnNaming(NamingStrategy.no_change)
							.enableLombok();

					builder.mapperBuilder()
							.superClass(KasMapper.class);

					builder.addInclude(new String[] {
							// "views_info", // table

							// "views",
							// "menus_view",
							// "fields_info_view",
							// "usermsgs_view",
							// "deleted_usermsgs_view",
							// "permissions_view",
					});
				});
		generator.execute();
	}
}

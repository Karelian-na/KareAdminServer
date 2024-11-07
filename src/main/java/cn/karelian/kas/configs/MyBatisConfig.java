package cn.karelian.kas.configs;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.servlet.http.HttpSession;

import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.injector.DefaultSqlInjector;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.fasterxml.jackson.databind.ObjectMapper;

import cn.karelian.kas.exceptions.NullRequestException;
import cn.karelian.kas.mappers.methods.SelectViewById;
import cn.karelian.kas.mappers.methods.SelectViewCount;
import cn.karelian.kas.mappers.methods.SelectViewList;
import cn.karelian.kas.mappers.methods.SelectViewMaps;
import cn.karelian.kas.mappers.methods.SelectViewOne;
import cn.karelian.kas.mappers.methods.SelectViewPage;
import cn.karelian.kas.utils.LoginInfomationUtil;

@Configuration
public class MyBatisConfig {

	MyBatisConfig(ObjectMapper objectMapper) {
		JacksonTypeHandler.setObjectMapper(objectMapper);
	}

	@Bean
	MybatisPlusInterceptor getInterceptor() {
		MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
		interceptor.addInnerInterceptor(new PaginationInnerInterceptor());
		return interceptor;
	}

	@Bean
	MetaObjectHandler metaObjectHandler() {
		return new MetaObjectHandler() {
			@Override
			public void insertFill(MetaObject metaObject) {
				this.strictInsertFill(metaObject, "add_time", LocalDate.class, LocalDate.now());
				this.strictInsertFill(metaObject, "add_time", LocalDateTime.class, LocalDateTime.now());
				this.strictInsertFill(metaObject, "deleted", Boolean.class, false);
				try {
					HttpSession session = LoginInfomationUtil.getSession();
					this.strictInsertFill(metaObject, "add_uid", Long.class, (Long) session.getAttribute("id"));
					this.strictInsertFill(metaObject, "add_user", String.class, (String) session.getAttribute("name"));
				} catch (NullRequestException e) {
				}
			}

			@Override
			public void updateFill(MetaObject metaObject) {
				this.strictUpdateFill(metaObject, "update_time", LocalDateTime.class, LocalDateTime.now());
				try {
					HttpSession session = LoginInfomationUtil.getSession();
					this.strictUpdateFill(metaObject, "update_uid", Long.class, (Long) session.getAttribute("id"));
					this.strictUpdateFill(metaObject, "update_user", String.class,
							(String) session.getAttribute("name"));
				} catch (NullRequestException e) {
				}
			}

		};
	}

	@Bean
	DefaultSqlInjector defaultSqlInjector() {
		return new DefaultSqlInjector() {
			@Override
			public List<AbstractMethod> getMethodList(Class<?> mapperClass, TableInfo tableInfo) {
				List<AbstractMethod> methods = super.getMethodList(mapperClass, tableInfo);

				methods.add(new SelectViewList());
				methods.add(new SelectViewMaps());
				methods.add(new SelectViewCount());
				methods.add(new SelectViewOne());
				methods.add(new SelectViewPage());
				methods.add(new SelectViewById());
				return methods;
			}
		};
	}
}

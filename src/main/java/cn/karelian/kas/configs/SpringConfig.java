package cn.karelian.kas.configs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import cn.karelian.kas.KasApplication;
import cn.karelian.kas.utils.CachedBodyHttpServletRequestWrapper;
import cn.karelian.kas.utils.DateTimeUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.filter.OncePerRequestFilter;
// import org.springframework.web.cors.CorsConfiguration;
// import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
// import org.springframework.web.filter.CorsFilter;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SpringConfig {
	@Scheduled(cron = "0 0 0 1/3 * ?")
	private void clearTask() {
		if (null != KasApplication.configs.localStorageConfig) {
			File[] files = KasApplication.configs.localStorageConfig.tempPath.toFile().listFiles();

			Calendar calendar = Calendar.getInstance();
			calendar.setTime(new Date());
			calendar.add(Calendar.DAY_OF_MONTH, -3);

			Long deadline = calendar.getTime().getTime();
			for (File file : files) {
				if (file.lastModified() < deadline) {
					file.delete();
				}
			}
		}
	}

	@Bean
	Converter<String, LocalDateTime> localDateConverter() {
		return new Converter<String, LocalDateTime>() {
			@Override
			@SuppressWarnings("null")
			public LocalDateTime convert(String str) {
				return DateTimeUtil.parseDateTime(str);
			}
		};
	}

	@Bean
	Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer(
			LocalDateTimeSerializer localDateTimeSerializer,
			LocalDateTimeDeserializer localDateTimeDeserializer,
			MultipartFileSerializer multipartFileSerializer) {
		return builder -> builder
				.serializationInclusion(JsonInclude.Include.NON_NULL)
				.serializerByType(LocalDateTime.class, localDateTimeSerializer)
				.serializerByType(MultipartFile.class, multipartFileSerializer)
				.deserializerByType(LocalDateTime.class, localDateTimeDeserializer);
	}

	@Bean
	LocalDateTimeSerializer localDateTimeSerializer() {
		return new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DateTimeUtil.dateTimePattern));
	}

	@Bean
	LocalDateTimeDeserializer localDateTimeDeserializer() {
		return new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(DateTimeUtil.dateTimePattern));
	}

	@Bean
	MultipartFileSerializer multipartFileSerializer() {
		return new MultipartFileSerializer();
	}

	private class MultipartFileSerializer extends JsonSerializer<MultipartFile> {
		@Override
		public void serialize(MultipartFile file, JsonGenerator generator, SerializerProvider provider)
				throws IOException {
			generator.writeString(file.getOriginalFilename() + ":" + file.getSize());
		}
	}

	@Bean
	WebMvcConfigurer webMvcConfigurer() {
		return new WebMvcConfigurer() {

			@Autowired
			StringToEnumConverterFactory stringToEnumConverterFactory;

			@Override
			@SuppressWarnings("null")
			public void addFormatters(FormatterRegistry registry) {
				WebMvcConfigurer.super.addFormatters(registry);
				registry.addConverterFactory(stringToEnumConverterFactory);
			}

			@Override
			@SuppressWarnings("null")
			public void addResourceHandlers(ResourceHandlerRegistry registry) {
				WebMvcConfigurer.super.addResourceHandlers(registry);
				if (null == KasApplication.configs.localStorageConfig) {
					return;
				}

				if (KasApplication.configs.localStorageConfig.publicResourcesPathMap != null) {
					KasApplication.configs.localStorageConfig.publicResourcesPathMap.forEach((k, v) -> {
						Path path = Path.of(v);
						if (!path.isAbsolute()) {
							path = KasApplication.currentPath.resolve(path);
						}
						String location = "file:" + path.toString();
						if (v.endsWith("/") || v.endsWith("\\")) {
							location += v.charAt(v.length() - 1);
						}
						registry.addResourceHandler(k).addResourceLocations(location);
					});
				}
			}
		};
	}

	@Bean
	OncePerRequestFilter oncePerRequestFilter() {
		return new OncePerRequestFilter() {
			@Override
			@SuppressWarnings("null")
			protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
				if (null != request.getContentType() && request.getContentType().startsWith("multipart/form-data")) {
					return true;
				}

				return super.shouldNotFilter(request);
			}

			@Override
			@SuppressWarnings("null")
			protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
					FilterChain filterChain) throws ServletException, IOException {
				CachedBodyHttpServletRequestWrapper requestWrapper = new CachedBodyHttpServletRequestWrapper(request);
				filterChain.doFilter(requestWrapper, response);
			}
		};
	}

}

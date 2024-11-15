package cn.karelian.kas.configs;

import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.stereotype.Component;

import cn.karelian.kas.utils.EntityUtil;

@Component
class StringToEnumConverterFactory implements ConverterFactory<String, Enum<?>> {

	@SuppressWarnings({ "null", "unchecked", "rawtypes" })
	@Override
	public <T extends Enum<?>> Converter<String, T> getConverter(Class<T> targetType) {
		return new StringToEnum(targetType);
	}

	private static class StringToEnum<T extends Enum<T>> implements Converter<String, Enum<T>> {
		private final Class<T> type;

		public StringToEnum(Class<T> type) {
			this.type = type;
		}

		@Override
		@SuppressWarnings("null")
		public Enum<T> convert(String source) {
			if (source.isEmpty()) {
				return null;
			}

			try {
				return EntityUtil.enumValueOf(type, source);
			} catch (Exception e) {
				return Enum.valueOf(this.type, source);
			}
		}

	}
}
package cn.karelian.kas.utils;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class EnumOrdinalSerializer extends JsonSerializer<Enum<?>> {
	@Override
	public void serialize(Enum<?> value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
		gen.writeNumber(value.ordinal());
	}
}

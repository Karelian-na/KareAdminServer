package cn.karelian.kas.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializable;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;

@JsonDeserialize(using = BiMapDeserializer.class)
public class BiMap<K, V> implements JsonSerializable {
	private final Map<K, V> keyToValue = new HashMap<>();
	private final Map<V, K> valueToKey = new HashMap<>();

	public void put(K key, V value) {
		if (keyToValue.containsKey(key)) {
			throw new IllegalArgumentException("Duplicate key: " + key);
		}
		if (valueToKey.containsKey(value)) {
			throw new IllegalArgumentException("Duplicate value: " + value);
		}

		keyToValue.put(key, value);
		valueToKey.put(value, key);
	}

	public V getValue(K key) {
		return keyToValue.get(key);
	}

	public K getKey(V value) {
		return valueToKey.get(value);
	}

	public boolean containsKey(K key) {
		return keyToValue.containsKey(key);
	}

	public boolean containsValue(V value) {
		return valueToKey.containsKey(value);
	}

	public V removeByKey(K key) {
		V value = keyToValue.remove(key);
		if (value != null) {
			valueToKey.remove(value);
		}
		return value;
	}

	public K removeByValue(V value) {
		K key = valueToKey.remove(value);
		if (key != null) {
			keyToValue.remove(key);
		}
		return key;
	}

	public int size() {
		return keyToValue.size();
	}

	public void clear() {
		keyToValue.clear();
		valueToKey.clear();
	}

	@Override
	public void serialize(JsonGenerator gen, SerializerProvider serializers) throws IOException {
		gen.writeObject(keyToValue);
	}

	@Override
	public void serializeWithType(JsonGenerator gen, SerializerProvider serializers, TypeSerializer typeSer)
			throws IOException {
		serialize(gen, serializers);
	}
}

class BiMapDeserializer extends JsonDeserializer<BiMap<String, String>> {
	@Override
	public BiMap<String, String> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
		JsonNode node = p.getCodec().readTree(p);
		BiMap<String, String> biMap = new BiMap<>();
		node.fields().forEachRemaining(entry -> {
			biMap.put(entry.getKey(), entry.getValue().asText());
		});
		return biMap;
	}
}
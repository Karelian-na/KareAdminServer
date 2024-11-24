package cn.karelian.kas.configs.mybatis;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NumericArrayTypeHandler<T extends Number> extends BaseTypeHandler<T[]> {
	private static final List<Class<?>> numericClasses = Arrays.asList(Byte.class, Short.class, Integer.class,
			Long.class, Double.class, Float.class);

	private Class<?> type;

	public NumericArrayTypeHandler(Class<T[]> type) {
		if (type == null) {
			throw new IllegalArgumentException("Type argument cannot be null");
		}

		this.type = type.getComponentType();

		if (!numericClasses.contains(this.type)) {
			throw new IllegalArgumentException(
					"Non primitive type argument cannot be annotated with `NumericArrayTypeHandler` to handler resultset!");
		}
	}

	@SuppressWarnings({ "unchecked" })
	private T[] parseFromString(String value) {
		if (null == value) {
			return null;
		}

		try {
			final Method method = type.getMethod("valueOf", String.class);
			return Stream.of(value.split(",")).map(v -> {
				try {
					return method.invoke(null, v);
				} catch (Exception e) {
					return null;
				}
			}).toArray(size -> (T[]) Array.newInstance(type, size));
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, T[] parameter, JdbcType jdbcType)
			throws SQLException {
		ps.setString(i, String.join(",", List.of(parameter).stream().map(String::valueOf).toList()));
	}

	@Override
	public T[] getNullableResult(ResultSet rs, String columnName) throws SQLException {
		String value = rs.getString(columnName);
		return this.parseFromString(value);
	}

	@Override
	public T[] getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		String value = rs.getString(columnIndex);
		return this.parseFromString(value);
	}

	@Override
	public T[] getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		String value = cs.getString(columnIndex);
		return this.parseFromString(value);
	}
}

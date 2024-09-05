package cn.karelian.kas.configs.mybatis;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Stream;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

public abstract class NumericArrayTypeHandler<T extends Number> extends BaseTypeHandler<T[]> {

	public static class ByteArray extends NumericArrayTypeHandler<Byte> {
		@Override
		protected Byte[] parseFrom(String[] str) {
			return Stream.of(str).map(Byte::valueOf).toArray(Byte[]::new);
		}
	}

	public static class ShortArray extends NumericArrayTypeHandler<Short> {
		@Override
		protected Short[] parseFrom(String[] str) {
			return Stream.of(str).map(Short::valueOf).toArray(Short[]::new);
		}
	}

	public static class IntegerArray extends NumericArrayTypeHandler<Integer> {
		@Override
		protected Integer[] parseFrom(String[] str) {
			return Stream.of(str).map(Integer::valueOf).toArray(Integer[]::new);
		}
	}

	public static class LongArray extends NumericArrayTypeHandler<Long> {
		@Override
		protected Long[] parseFrom(String[] str) {
			return Stream.of(str).map(Long::valueOf).toArray(Long[]::new);
		}
	}

	protected abstract T[] parseFrom(String[] str);

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, T[] parameter, JdbcType jdbcType)
			throws SQLException {
		ps.setString(i, String.join(",", List.of(parameter).toArray(String[]::new)));
	}

	@Override
	public T[] getNullableResult(ResultSet rs, String columnName) throws SQLException {
		String value = rs.getString(columnName);
		return value == null ? null : parseFrom(value.split(","));
	}

	@Override
	public T[] getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		String value = rs.getString(columnIndex);
		return value == null ? null : parseFrom(value.split(","));
	}

	@Override
	public T[] getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		String value = cs.getString(columnIndex);
		return value == null ? null : parseFrom(value.split(","));
	}
}

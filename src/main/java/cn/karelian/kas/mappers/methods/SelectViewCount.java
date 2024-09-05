package cn.karelian.kas.mappers.methods;

import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;

import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.ReflectionKit;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;

import cn.karelian.kas.mappers.KasMapper;

public class SelectViewCount extends AbstractMethod {

	public SelectViewCount() {
		this(StringUtils.firstToLowerCase(SelectViewCount.class.getSimpleName()));
	}

	public SelectViewCount(String name) {
		super(name);
	}

	public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
		Class<?> viewClass = ReflectionKit.getSuperClassGenericType(mapperClass, KasMapper.class, 1);
		tableInfo = TableInfoHelper.getTableInfo(viewClass);

		SqlMethod sqlMethod = SqlMethod.SELECT_COUNT;
		String sql = String.format(sqlMethod.getSql(), this.sqlFirst(), this.sqlCount(), tableInfo.getTableName(),
				this.sqlWhereEntityWrapper(true, tableInfo), this.sqlComment());
		SqlSource sqlSource = this.languageDriver.createSqlSource(this.configuration, sql, modelClass);
		return this.addSelectMappedStatementForOther(mapperClass, this.methodName, sqlSource, Long.class);
	}
}

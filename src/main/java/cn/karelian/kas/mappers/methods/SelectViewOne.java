package cn.karelian.kas.mappers.methods;

import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.SqlSource;

import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.ReflectionKit;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;

import cn.karelian.kas.mappers.KasMapper;

public class SelectViewOne extends AbstractMethod {

	public SelectViewOne() {
		this(StringUtils.firstToLowerCase(SelectViewOne.class.getSimpleName()));
	}

	public SelectViewOne(String name) {
		super(name);
	}

	@Override
	public void inject(MapperBuilderAssistant builderAssistant, Class<?> mapperClass, Class<?> modelClass,
			TableInfo tableInfo) {
		Class<?> viewClass = ReflectionKit.getSuperClassGenericType(mapperClass, KasMapper.class, 1);
		TableInfo viewTableInfo = TableInfoHelper.getTableInfo(viewClass);

		String viewResultName = viewTableInfo.getResultMap();
		if (modelClass != viewClass && !builderAssistant.getConfiguration().hasResultMap(viewResultName)
				&& viewTableInfo.getConfiguration().hasResultMap(viewResultName)) {
			ResultMap resultMap = viewTableInfo.getConfiguration().getResultMap(viewResultName);
			builderAssistant.getConfiguration().addResultMap(resultMap);
		}

		super.inject(builderAssistant, mapperClass, modelClass, tableInfo);
	}

	@Override
	public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
		Class<?> viewClass = ReflectionKit.getSuperClassGenericType(mapperClass, KasMapper.class, 1);
		tableInfo = TableInfoHelper.getTableInfo(viewClass);

		SqlMethod sqlMethod = SqlMethod.SELECT_LIST;
		String sql = String.format(sqlMethod.getSql(), this.sqlFirst(), this.sqlSelectColumns(tableInfo, true),
				tableInfo.getTableName(), this.sqlWhereEntityWrapper(true, tableInfo), this.sqlOrderBy(tableInfo),
				this.sqlComment());
		SqlSource sqlSource = this.languageDriver.createSqlSource(this.configuration, sql, modelClass);
		return this.addSelectMappedStatementForTable(mapperClass, this.methodName, sqlSource, tableInfo);
	}
}

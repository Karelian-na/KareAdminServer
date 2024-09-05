package cn.karelian.kas.mappers.methods;

import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.scripting.defaults.RawSqlSource;

import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.ReflectionKit;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;

import cn.karelian.kas.mappers.KasMapper;

public class SelectViewById extends AbstractMethod {

	public SelectViewById() {
		this(StringUtils.firstToLowerCase(SelectViewById.class.getSimpleName()));
	}

	public SelectViewById(String name) {
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

	public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
		Class<?> viewClass = ReflectionKit.getSuperClassGenericType(mapperClass, KasMapper.class, 1);
		tableInfo = TableInfoHelper.getTableInfo(viewClass);

		SqlMethod sqlMethod = SqlMethod.SELECT_BY_ID;
		SqlSource sqlSource = new RawSqlSource(this.configuration,
				String.format(sqlMethod.getSql(), this.sqlSelectColumns(tableInfo, false), tableInfo.getTableName(),
						tableInfo.getKeyColumn(), tableInfo.getKeyProperty(), tableInfo.getLogicDeleteSql(true, true)),
				Object.class);
		return this.addSelectMappedStatementForTable(mapperClass, this.methodName, sqlSource, tableInfo);
	}
}

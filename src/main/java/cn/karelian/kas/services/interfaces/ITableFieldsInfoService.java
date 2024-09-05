package cn.karelian.kas.services.interfaces;

import java.util.Map;
import java.util.Set;

import cn.karelian.kas.entities.TableFieldsInfo;
import cn.karelian.kas.views.FieldsInfoView;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author Karelian_na
 * @since 2023-08-28
 */
public interface ITableFieldsInfoService extends IKasService<TableFieldsInfo, FieldsInfoView> {
	public Map<String, Boolean> getFieldsEditableMap(Class<?> entityOrViewClszz, boolean isAdd);

	public Set<String> getSearchableFields(Class<?> entityOrViewClszz);
}

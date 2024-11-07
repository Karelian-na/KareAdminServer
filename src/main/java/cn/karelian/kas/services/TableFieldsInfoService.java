package cn.karelian.kas.services;

import jakarta.annotation.PostConstruct;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;

import cn.karelian.kas.entities.TableFieldsInfo;
import cn.karelian.kas.mappers.TableFieldsInfoMapper;
import cn.karelian.kas.services.interfaces.ITableFieldsInfoService;
import cn.karelian.kas.views.FieldsInfoView;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author Karelian_na
 * @since 2023-08-28
 */
@Service
public class TableFieldsInfoService extends KasService<TableFieldsInfoMapper, TableFieldsInfo, FieldsInfoView>
		implements ITableFieldsInfoService {
	private static ConcurrentHashMap<String, List<FieldsInfoView>> allFieldsConfigs;

	@PostConstruct
	public void reloadFieldsConfigs() {
		allFieldsConfigs = new ConcurrentHashMap<>();
		List<FieldsInfoView> allConfigs = this.baseMapper.selectViewList(null);
		allConfigs.stream().forEach(item -> {
			List<FieldsInfoView> curViewConfigs = null;
			if (!allFieldsConfigs.containsKey(item.getTable_name())) {
				curViewConfigs = new ArrayList<>();
				allFieldsConfigs.put(item.getTable_name(), curViewConfigs);
			} else {
				curViewConfigs = allFieldsConfigs.get(item.getTable_name());
			}

			curViewConfigs.add(item);
		});
	}

	@Override
	public Map<String, Boolean> getFieldsEditableMap(Class<?> entityOrViewClszz, boolean isAdd) {
		var tableInfo = TableInfoHelper.getTableInfo(entityOrViewClszz);
		if (null == tableInfo) {
			return null;
		}

		String viewName = tableInfo.getTableName();

		if (!allFieldsConfigs.containsKey(viewName)) {
			return null;
		}

		return allFieldsConfigs.get(viewName).stream().reduce(new HashMap<String, Boolean>(), (prev, item) -> {
			final boolean editable = item.getEditable() == null ? false : item.getEditable().booleanValue();
			final Boolean editableWhenAdd = item.getEditable_when_add();
			if ((!isAdd && editable) // 不为添加时，可编辑
					|| (isAdd && ((editableWhenAdd != null && editableWhenAdd) // 添加时可自定义为 true
							|| (editable && editableWhenAdd == null))) // 可编辑且添加时可自定义未定义
			) {
				prev.put(item.getField_name(), true);
			} else {
				prev.put(item.getField_name(), false);
			}

			return prev;
		}, (l, r) -> l);
	}

	@Override
	public Set<String> getSearchableFields(Class<?> entityOrViewClszz) {
		String viewName = TableInfoHelper.getTableInfo(entityOrViewClszz).getTableName();

		if (!allFieldsConfigs.containsKey(viewName)) {
			return null;
		}

		return allFieldsConfigs.get(viewName).stream().reduce(new HashSet<String>(), (prev, item) -> {
			if (null != item.getSearchable() && item.getSearchable()) {
				prev.add(item.getField_name());
			}

			return prev;
		}, (l, r) -> {
			l.addAll(r);
			return l;
		});
	}
}

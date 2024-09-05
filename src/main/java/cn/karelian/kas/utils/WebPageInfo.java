package cn.karelian.kas.utils;

import java.util.List;
import java.util.Map;

import cn.karelian.kas.views.FieldsInfoView;

public class WebPageInfo<T> {
	public String fieldsConfig;
	public List<FieldsInfoView> fields;
	public List<OperButton> operButtons;
	public PageData<T> pageData;
	public Map<String, Object> extraData;
}

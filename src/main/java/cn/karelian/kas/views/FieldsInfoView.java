package cn.karelian.kas.views;

import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * VIEW
 * </p>
 *
 * @author Karelian_na
 * @since 2023-08-28
 */
@Getter
@Setter
@TableName("fields_info_view")
public class FieldsInfoView implements Serializable {

	private static final long serialVersionUID = 1L;

	private String table_name;

	private String field_name;

	private String display_name;

	/**
	 * 次序
	 */
	private Byte display_order;

	/**
	 * 是否展示
	 */
	private Boolean display;

	/**
	 * 是否可检索
	 */
	private Boolean searchable;

	/**
	 * 是否可编辑
	 */
	private Boolean editable;

	/**
	 * 是否添加时可自定义
	 */
	private Boolean editable_when_add;
}

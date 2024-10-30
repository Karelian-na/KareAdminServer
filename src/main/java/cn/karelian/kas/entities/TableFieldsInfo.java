package cn.karelian.kas.entities;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 
 * </p>
 *
 * @author Karelian_na
 * @since 2023-08-28
 */
@Getter
@Setter
@TableName("table_fields_info")
public class TableFieldsInfo implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * ID
	 */
	@TableId(value = "id", type = IdType.AUTO)
	private Integer id;

	/**
	 * 表名或视图名
	 */
	private String table_name;

	/**
	 * 字段名
	 */
	private String field_name;

	/**
	 * 展示名
	 */
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

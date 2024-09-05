package cn.karelian.kas.entities;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 管理权限目录的表
 * </p>
 *
 * @author Karelian_na
 * @since 2023-08-28
 */
@Getter
@Setter
public class Permissions implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * ID
	 */
	@TableId(value = "id", type = IdType.AUTO)
	private Integer id;

	/**
	 * 名称
	 */
	private String name;

	/**
	 * 状态
	 */
	private Boolean status;

	/**
	 * 唯一标识
	 */
	private String guid;

	/**
	 * 备注
	 */
	private String descrip;

	/**
	 * 操作方式
	 */
	private Byte oper_type;

	/**
	 * 创建人
	 */
	@TableField(fill = FieldFill.INSERT)
	private Long add_uid;

	/**
	 * 创建时间
	 */
	@TableField(fill = FieldFill.INSERT)
	private LocalDateTime add_time;

	/**
	 * 更新时间
	 */
	@TableField(fill = FieldFill.UPDATE)
	private LocalDateTime update_time;
}

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
 * 
 * </p>
 *
 * @author Karelian_na
 * @since 2023-11-09
 */
@Getter
@Setter
public class Menus implements Serializable {

	private static final long serialVersionUID = 1L;
	public static final byte TYPE_MENU = 1;
	public static final byte TYPE_ITEM = 2;
	public static final byte TYPE_PAGE = 3;
	public static final byte TYPE_OPER = 4;

	/**
	 * 菜单ID
	 */
	@TableId(value = "id", type = IdType.AUTO)
	// private Short id;
	private Integer id;

	/**
	 * 名称
	 */
	private String name;

	/**
	 * 图标
	 */
	private String icon;

	/**
	 * 类型
	 */
	private Byte type;

	/**
	 * 操作方式，仅未关联权限时有效
	 */
	@TableField(fill = FieldFill.INSERT_UPDATE)
	private Byte oper_type;

	/**
	 * 状态
	 */
	private Boolean status;

	/**
	 * 地址
	 */
	@TableField(fill = FieldFill.INSERT_UPDATE)
	private String url;

	/**
	 * 关联权限ID
	 */
	@TableField(fill = FieldFill.INSERT_UPDATE)
	private Short pmid;

	/**
	 * 备注
	 */
	private String descrip;

	/**
	 * 父权限ID
	 */
	@TableField(fill = FieldFill.INSERT_UPDATE)
	private Integer pid;

	/**
	 * 默认页面id
	 */
	@TableField(fill = FieldFill.INSERT_UPDATE)
	private Integer ref_id;

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

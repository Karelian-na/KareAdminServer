package cn.karelian.kas.views;

import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * VIEW
 * </p>
 *
 * @author Karelian_na
 * @since 2023-11-10
 */
@Getter
@Setter
@TableName("menus_view")
public class MenusView implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 菜单ID
	 */
	private Short id;

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

	private Integer status;

	/**
	 * 关联权限ID
	 */
	private Short pmid;

	private String url;

	/**
	 * 备注
	 */
	private String descrip;

	/**
	 * 父权限ID
	 */
	private Short pid;

	/**
	 * 引用页面ID
	 */
	private Short ref_id;

	/**
	 * 操作方式
	 */
	private Byte oper_type;

	/**
	 * 姓名
	 */
	private String add_user;

	/**
	 * 创建时间
	 */
	private LocalDateTime add_time;

	/**
	 * 更新时间
	 */
	private LocalDateTime update_time;
}

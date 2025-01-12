package cn.karelian.kas.views;

import com.baomidou.mybatisplus.annotation.TableName;

import cn.karelian.kas.annotations.GeneralValidate;
import cn.karelian.kas.annotations.StringValidate;
import cn.karelian.kas.entities.Menus.MenuType;
import cn.karelian.kas.entities.Permissions.OperType;
import cn.karelian.kas.utils.NonEmptyStrategy;

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
	// private Short id;
	private Integer id;

	/**
	 * 名称
	 */
	@StringValidate(nonEmptyStrategy = NonEmptyStrategy.ADD, minLen = 1, maxLen = 50)
	private String name;

	/**
	 * 图标
	 */
	@StringValidate(minLen = 0, maxLen = 255)
	private String icon;

	/**
	 * 类型
	 */
	@GeneralValidate(nonEmptyStrategy = NonEmptyStrategy.ADD)
	private MenuType type;

	/**
	 * 操作方式，仅未关联权限时有效
	 */
	private OperType oper_type;

	/**
	 * 操作标识，仅未关联权限时有效
	 */
	private String oper_id;

	/**
	 * 状态
	 */
	private Boolean status;

	/**
	 * 地址
	 */
	@StringValidate(minLen = 0, maxLen = 255)
	private String url;

	/**
	 * 关联权限ID
	 */
	private Short pmid;

	/**
	 * 备注
	 */
	@StringValidate(minLen = 0, maxLen = 100)
	private String descrip;

	/**
	 * 父权限ID
	 */
	private Integer pid;

	/**
	 * 默认页面id
	 */
	private Integer ref_id;

	/**
	 * 创建人
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

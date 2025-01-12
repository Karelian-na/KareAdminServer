package cn.karelian.kas.views;

import com.baomidou.mybatisplus.annotation.TableName;

import cn.karelian.kas.annotations.GeneralValidate;
import cn.karelian.kas.annotations.StringValidate;
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
 * @since 2024-09-08
 */
@Getter
@Setter
@TableName("permissions_view")
public class PermissionsView implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * ID
	 */
	private Integer id;

	/**
	 * 名称
	 */
	@StringValidate(nonEmptyStrategy = NonEmptyStrategy.ADD, minLen = 2, maxLen = 50)
	private String name;

	/**
	 * 状态
	 */
	private Boolean status;

	/**
	 * 唯一标识
	 */
	@StringValidate(nonEmptyStrategy = NonEmptyStrategy.ADD, minLen = 2, maxLen = 128)
	private String oper_id;

	/**
	 * 备注
	 */
	@StringValidate(minLen = 0, maxLen = 128)
	private String descrip;

	/**
	 * 操作方式
	 */
	@GeneralValidate(nonEmptyStrategy = NonEmptyStrategy.ADD)
	private OperType oper_type;

	/**
	 * 创建人姓名
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

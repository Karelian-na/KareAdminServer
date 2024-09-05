package cn.karelian.kas.views;

import com.baomidou.mybatisplus.annotation.TableName;

import cn.karelian.kas.annotations.ComparableValidate;
import cn.karelian.kas.annotations.StringValidate;
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
	private String guid;

	/**
	 * 备注
	 */
	@StringValidate(minLen = 0, maxLen = 100)
	private String descrip;

	/**
	 * 操作方式
	 */
	@ComparableValidate(nonEmptyStrategy = NonEmptyStrategy.ADD, min = 0, max = 3)
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

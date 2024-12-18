package cn.karelian.kas.entities;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;

import cn.karelian.kas.annotations.ComparableValidate;
import cn.karelian.kas.annotations.StringValidate;
import cn.karelian.kas.utils.NonEmptyStrategy;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 管理角色的表
 * </p>
 *
 * @author Karelian_na
 * @since 2023-08-28
 */
@Getter
@Setter
public class Roles implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 角色ID
	 */
	@TableId(value = "id", type = IdType.AUTO)
	private Byte id;

	/**
	 * 角色名称
	 */
	@StringValidate(nonEmptyStrategy = NonEmptyStrategy.ADD, minLen = 1, maxLen = 20)
	private String name;

	/**
	 * 创建人
	 */
	@TableField(fill = FieldFill.INSERT)
	private String add_user;

	/**
	 * 角色级别
	 */
	@ComparableValidate(nonEmptyStrategy = NonEmptyStrategy.ADD, min = 3, max = 126)
	private Byte level;

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

	/**
	 * 备注
	 */
	@StringValidate(maxLen = 100)
	private String descrip;

	/**
	 * 是否删除
	 */
	@TableLogic
	private Byte deleted;
}

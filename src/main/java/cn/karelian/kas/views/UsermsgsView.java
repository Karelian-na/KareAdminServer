package cn.karelian.kas.views;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;

import cn.karelian.kas.KasApplication;
import cn.karelian.kas.annotations.ComparableValidate;
import cn.karelian.kas.annotations.StringValidate;
import cn.karelian.kas.utils.Constants;
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
 * @since 2023-08-28
 */
@Getter
@Setter
@TableName("usermsgs_view")
public class UsermsgsView implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 用户名
	 */
	@StringValidate(regex = KasApplication.userUIdRegex)
	private String uid;

	/**
	 * ID
	 */
	@StringValidate(regex = KasApplication.userIdRegex)
	private Long id;

	/**
	 * 姓名
	 */
	@StringValidate(nonEmptyStrategy = NonEmptyStrategy.ADD, minLen = 1, maxLen = 20)
	private String name;

	/**
	 * 性别
	 */
	@ComparableValidate(min = 1, max = 3)
	private Byte gender;

	/**
	 * 年龄
	 */
	@ComparableValidate(min = 10, max = 125)
	private Byte age;

	/**
	 * 头像
	 */
	@StringValidate(minLen = 1, maxLen = 255)
	private String avatar;

	/**
	 * 电子邮箱
	 */
	@StringValidate(regex = Constants.emailRegex)
	private String email;

	/**
	 * 联系方式
	 */
	@StringValidate(regex = Constants.phoneRegex)
	private String phone;

	/**
	 * 政治面貌
	 */
	@ComparableValidate(min = 1, max = 13)
	private Byte political_status;

	/**
	 * 民族
	 */
	@ComparableValidate(min = 1, max = 56)
	private Byte clan;

	/**
	 * 个人简介
	 */
	@StringValidate(minLen = 0, maxLen = 100)
	private String profile;

	/**
	 * 角色
	 */
	private String roles;

	/**
	 * 最大角色级别
	 */
	@JsonIgnore
	private Byte max_role_level;

	/**
	 * 注册时间
	 */
	private LocalDateTime add_time;

	/**
	 * 绑定邮箱
	 */
	private String bind_email;

	/**
	 * 绑定手机号
	 */
	private String bind_phone;
}

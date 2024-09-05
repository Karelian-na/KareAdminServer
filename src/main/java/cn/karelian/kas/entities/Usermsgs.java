package cn.karelian.kas.entities;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 管理用户基本信息的表
 * </p>
 *
 * @author Karelian_na
 * @since 2023-08-28
 */
@Getter
@Setter
public class Usermsgs implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * ID
	 */
	private Long id;

	/**
	 * 姓名
	 */
	private String name;

	/**
	 * 性别
	 */
	private Byte gender;

	/**
	 * 年龄
	 */
	private Byte age;

	/**
	 * 头像
	 */
	private String avatar;

	/**
	 * 电子邮箱
	 */
	private String email;

	/**
	 * 联系方式
	 */
	private String phone;

	/**
	 * 政治面貌
	 */
	private Byte political_status;

	/**
	 * 民族
	 */
	private Byte clan;

	/**
	 * 个人简介
	 */
	private String profile;
}

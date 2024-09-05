package cn.karelian.kas.entities;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 管理用户的表
 * </p>
 *
 * @author Karelian_na
 * @since 2023-08-28
 */
@Getter
@Setter
public class Users implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * ID
	 */
	@TableId(value = "id")
	private Long id;

	/**
	 * 用户名
	 */
	private String uid;

	/**
	 * 密码
	 */
	private String pwd;

	/**
	 * 最后登录IP
	 */
	private String last_login_ip;

	/**
	 * 最后登录时间
	 */
	private LocalDateTime last_login_time;

	/**
	 * 注册时间
	 */
	@TableField(fill = FieldFill.INSERT)
	private LocalDateTime add_time;

	/**
	 * 绑定邮箱
	 */
	private String bind_email;

	/**
	 * 绑定手机
	 */
	private String bind_phone;

	/**
	 * 是否初始化
	 */
	private Boolean is_init;

	/**
	 * 是否删除
	 */
	@TableLogic
	private Boolean deleted;
}

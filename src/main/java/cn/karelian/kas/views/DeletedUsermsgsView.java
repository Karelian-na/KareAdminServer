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
 * @since 2024-10-18
 */
@Getter
@Setter
@TableName("deleted_usermsgs_view")
public class DeletedUsermsgsView implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * ID
	 */
	private Long id;

	/**
	 * 用户名
	 */
	private String uid;

	/**
	 * 姓名
	 */
	private String name;

	/**
	 * 头像
	 */
	private String avatar;

	/**
	 * 绑定邮箱
	 */
	private String bind_email;

	/**
	 * 绑定手机
	 */
	private String bind_phone;

	/**
	 * 最后登录时间
	 */
	private LocalDateTime last_login_time;

	/**
	 * 注册时间
	 */
	private LocalDateTime add_time;

	/**
	 * 删除人
	 */
	private String delete_user;

	/**
	 * 删除/注销时间
	 */
	private LocalDateTime delete_time;

	/**
	 * 删除类型
	 */
	private Boolean delete_type;

	/**
	 * 电子邮箱
	 */
	private String email;

	/**
	 * 联系方式
	 */
	private String phone;
}

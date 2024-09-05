package cn.karelian.kas.entities;

import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 用户角色关联表
 * </p>
 *
 * @author Karelian_na
 * @since 2023-08-28
 */
@Getter
@Setter
@TableName("user_role_assoc")
public class UserRoleAssoc implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long uid;

	private Byte rid;
}

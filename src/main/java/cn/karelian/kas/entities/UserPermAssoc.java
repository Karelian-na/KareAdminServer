package cn.karelian.kas.entities;

import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 用户权限关联表
 * </p>
 *
 * @author Karelian_na
 * @since 2023-08-28
 */
@Getter
@Setter
@TableName("user_perm_assoc")
public class UserPermAssoc implements Serializable {

	private static final long serialVersionUID = 1L;

	private Integer id;

	private Long uid;

	private Integer mid;

	private Boolean authorize;
}

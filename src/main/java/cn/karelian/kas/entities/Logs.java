package cn.karelian.kas.entities;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 
 * </p>
 *
 * @author Karelian_na
 * @since 2023-08-28
 */
@Getter
@Setter
public class Logs implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * ID
	 */
	@TableId(value = "id", type = IdType.AUTO)
	private Integer id;

	/**
	 * 请求用户ID
	 */
	private Long uid;

	/**
	 * 请求类型
	 */
	private String type;

	/**
	 * 名称
	 */
	private String title;

	/**
	 * 地址
	 */
	private String url;

	/**
	 * 参数
	 */
	private String params;

	/**
	 * 请求IP
	 */
	private String ip;

	/**
	 * 请求时间
	 */
	private LocalDateTime date;
}

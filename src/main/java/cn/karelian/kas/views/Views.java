package cn.karelian.kas.views;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

import com.baomidou.mybatisplus.annotation.TableField;

import cn.karelian.kas.annotations.StringValidate;
import cn.karelian.kas.entities.TableFieldsInfo;
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
public class Views implements Serializable {
	private static final long serialVersionUID = 1L;

	@StringValidate(minLen = 1, maxLen = 30)
	private String view_name;

	/**
	 * 字段配置
	 */
	@StringValidate(minLen = 0, maxLen = 10240)
	private String fields_config;

	/**
	 * 备注
	 */
	@StringValidate(minLen = 0, maxLen = 100)
	private String comment;

	@TableField(exist = false)
	public Map<String, TableFieldsInfo> fields;

	/**
	 * 更新时间
	 */
	private LocalDateTime update_time;

	/**
	 * 更新人
	 */
	private String update_user;
}

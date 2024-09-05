package cn.karelian.kas.dtos;

import cn.karelian.kas.annotations.StringValidate;
import cn.karelian.kas.utils.NonEmptyStrategy;
import lombok.Data;

@Data
public class LoginParam {
	/**
	 * 登录账户, 具体校验在服务内
	 */
	@StringValidate(nonEmptyStrategy = NonEmptyStrategy.QUERY, minLen = 6, maxLen = 20)
	public String account;

	/**
	 * 登录密码 MD5
	 */
	@StringValidate(nonEmptyStrategy = NonEmptyStrategy.QUERY, len = 64)
	public String pwd;

	public boolean remember;
}

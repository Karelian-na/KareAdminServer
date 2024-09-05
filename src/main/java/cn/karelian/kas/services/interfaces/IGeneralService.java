package cn.karelian.kas.services.interfaces;

import org.springframework.web.multipart.MultipartFile;

import cn.karelian.kas.Result;
import cn.karelian.kas.dtos.LoginParam;
import cn.karelian.kas.exceptions.NullRequestException;

public interface IGeneralService {
	public Result login(LoginParam params);

	public void logout();

	public Result index() throws NullRequestException;

	public Result upload(MultipartFile[] files) throws NullRequestException;
}

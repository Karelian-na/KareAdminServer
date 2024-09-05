/*
 * @Author: Karelian_na
 */
package cn.karelian.kas.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import cn.karelian.kas.Result;
import cn.karelian.kas.annotations.Log;
import cn.karelian.kas.annotations.Validate;
import cn.karelian.kas.dtos.LoginParam;
import cn.karelian.kas.exceptions.NullRequestException;
import cn.karelian.kas.services.GeneralService;
import cn.karelian.kas.utils.NonEmptyStrategy;

@RestController
@RequestMapping("")
public class GeneralController {
	@Autowired
	private GeneralService generalService;

	@Log(value = "登陆", checkLogin = false)
	@PostMapping("/login")
	public Result login(@Validate(nonEmptyStrategy = NonEmptyStrategy.QUERY) @RequestBody LoginParam params) {
		return generalService.login(params);
	}

	@Log(value = "登出", checkLogin = false)
	@PostMapping("/logout")
	public void logout() {
		generalService.logout();
	}

	@Log(value = "首页信息")
	@GetMapping("/index")
	public Result index() throws NullRequestException {
		return generalService.index();
	}

	@Log(value = "上传", checkLogin = false)
	@PostMapping("/upload")
	public Result upload(@RequestPart MultipartFile[] files) {
		return generalService.upload(files);
	}
}
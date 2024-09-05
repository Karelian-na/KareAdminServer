package cn.karelian.kas.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cn.karelian.kas.Result;
import cn.karelian.kas.annotations.Authorize;
import cn.karelian.kas.annotations.Log;
import cn.karelian.kas.annotations.Validate;
import cn.karelian.kas.dtos.AssignRoleParam;
import cn.karelian.kas.dtos.AuthorizeParam;
import cn.karelian.kas.dtos.IndexParam;
import cn.karelian.kas.dtos.RevisePasswordParam;
import cn.karelian.kas.exceptions.NullRequestException;
import cn.karelian.kas.exceptions.KasException;
import cn.karelian.kas.exceptions.TransactionFailedException;
import cn.karelian.kas.services.UsersService;
import cn.karelian.kas.utils.NonEmptyStrategy;
import cn.karelian.kas.views.UsermsgsView;

/**
 * <p>
 * 管理用户的表 前端控制器
 * </p>
 *
 * @author Karelian_na
 * @since 2023-08-28
 */
@RestController
@RequestMapping("")
public class UsersController {
	@Autowired
	private UsersService usersService;

	@Authorize
	@GetMapping("/users/index")
	public Result index(@Validate @ModelAttribute IndexParam params) throws Exception {
		return usersService.index(params);
	}

	@Authorize
	@PutMapping("/users/edit")
	public Result edit(@Validate(nonEmptyStrategy = NonEmptyStrategy.EDIT) @RequestBody UsermsgsView params)
			throws KasException {
		return usersService.edit(params, false);
	}

	@Authorize
	@PostMapping("/users/add")
	public Result add(@Validate(nonEmptyStrategy = NonEmptyStrategy.ADD) @RequestBody UsermsgsView params)
			throws TransactionFailedException {
		return usersService.add(params);
	}

	@Log("我的信息")
	@GetMapping("/self/index")
	public Result selfindex() throws NullRequestException {
		return usersService.selfindex();
	}

	@Log("我的信息修改")
	@PutMapping("/self/edit")
	public Result selfedit(@Validate(nonEmptyStrategy = NonEmptyStrategy.EDIT) @RequestBody UsermsgsView params)
			throws KasException {
		return usersService.edit(params, true);
	}

	@Authorize
	@DeleteMapping("/users/delete")
	public Result delete(@RequestParam long id) {
		return new Result(usersService.removeById(id));
	}

	@Authorize
	@GetMapping("/users/authorize")
	public Result authorizeindex(@RequestParam Long id,
			@RequestParam(required = false, defaultValue = "false") Boolean all)
			throws NullRequestException {
		return usersService.authorizeindex(id, all);
	}

	@Authorize
	@PutMapping("/users/authorize")
	public Result authorize(@RequestBody AuthorizeParam params) throws TransactionFailedException {
		return usersService.authorize(params);
	}

	@Authorize
	@PutMapping("/users/reset")
	public Result reset(@RequestBody List<Long> uids) {
		return usersService.reset(uids);
	}

	@Log(value = "修改密码", checkLogin = false)
	@PutMapping("/users/revisepwd")
	public Result selfreset(@RequestBody RevisePasswordParam params) throws NullRequestException {
		return usersService.selfreset(params);
	}

	@Authorize
	@GetMapping("/users/assign")
	public Result asignindex(@RequestParam List<Long> ids) throws NullRequestException {
		return usersService.asignindex(ids);
	}

	@Authorize
	@PutMapping("/users/assign")
	public Result asign(@RequestBody AssignRoleParam params) throws NullRequestException, TransactionFailedException {
		return usersService.asign(params);
	}

	@Log(value = "获取验证方式", checkLogin = false)
	@GetMapping("/users/verifies")
	public Result getverifies(@RequestParam(required = false) String account) throws NullRequestException {
		return usersService.getverifies(account);
	}
}

package cn.karelian.kas.controllers;

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
import cn.karelian.kas.annotations.Validate;
import cn.karelian.kas.dtos.AuthorizeParam;
import cn.karelian.kas.dtos.IndexParam;
import cn.karelian.kas.entities.Roles;
import cn.karelian.kas.exceptions.NullRequestException;
import cn.karelian.kas.exceptions.IllegalAccessException;
import cn.karelian.kas.exceptions.PermissionNotFoundException;
import cn.karelian.kas.exceptions.TransactionFailedException;
import cn.karelian.kas.services.RolesService;
import cn.karelian.kas.utils.NonEmptyStrategy;

/**
 * <p>
 * 管理角色的表 前端控制器
 * </p>
 *
 * @author Karelian_na
 * @since 2023-08-28
 */
@RestController
@RequestMapping("/admin/roles")
public class RolesController {
	@Autowired
	private RolesService rolesService;

	@Authorize
	@GetMapping("/index")
	public Result index(@ModelAttribute IndexParam params)
			throws IllegalAccessException, NullRequestException, PermissionNotFoundException {
		return rolesService.index(params);
	}

	@Authorize
	@GetMapping("/authorize")
	public Result getAuthorized(@RequestParam Integer id,
			@RequestParam(required = false, defaultValue = "false") Boolean all) {
		return rolesService.authorizeindex(id, all);
	}

	@Authorize
	@PutMapping("/authorize")
	public Result authorize(@RequestBody AuthorizeParam params) throws TransactionFailedException {
		return rolesService.authorize(params);
	}

	@Authorize
	@PostMapping("/add")
	public Result add(@Validate(nonEmptyStrategy = NonEmptyStrategy.ADD) @RequestBody Roles role) {
		return rolesService.add(role);
	}

	@Authorize
	@PutMapping("/edit")
	public Result edit(@RequestBody Roles role) {
		return rolesService.edit(role);
	}

	@Authorize
	@DeleteMapping("/delete")
	public Result delete(@RequestParam Byte id) {
		return rolesService.delete(id);
	}
}

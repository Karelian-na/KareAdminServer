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
import cn.karelian.kas.dtos.IndexParam;
import cn.karelian.kas.exceptions.NullRequestException;
import cn.karelian.kas.exceptions.IllegalAccessException;
import cn.karelian.kas.exceptions.PermissionNotFoundException;
import cn.karelian.kas.services.PermissionsService;
import cn.karelian.kas.utils.NonEmptyStrategy;
import cn.karelian.kas.views.PermissionsView;

/**
 * <p>
 * 管理权限目录的表 前端控制器
 * </p>
 *
 * @author Karelian_na
 * @since 2023-08-28
 */
@RestController
@RequestMapping("/admin/permissions")
public class PermissionsController {
	@Autowired
	private PermissionsService permissionsService;

	@Authorize
	@GetMapping("/index")
	public Result index(@Validate @ModelAttribute IndexParam params)
			throws IllegalAccessException, NullRequestException, PermissionNotFoundException {
		return permissionsService.index(params);
	}

	@Authorize
	@PutMapping("/edit")
	public Result update(@Validate(nonEmptyStrategy = NonEmptyStrategy.EDIT) @RequestBody PermissionsView permission)
			throws Exception {
		return permissionsService.update(permission);
	}

	@Authorize
	@DeleteMapping("/delete")
	public Result delete(@RequestParam Integer id) throws Exception {
		return permissionsService.delete(new Integer[] { id });
	}

	@Authorize
	@DeleteMapping("/bulkdelete")
	public Result bulkdelete(@RequestParam Integer[] ids) {
		return permissionsService.delete(ids);
	}

	@Authorize
	@PostMapping("/add")
	public Result add(@Validate(nonEmptyStrategy = NonEmptyStrategy.ADD) @RequestBody PermissionsView permission)
			throws Exception {
		return permissionsService.add(permission);
	}
}

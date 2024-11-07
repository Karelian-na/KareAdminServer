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
import cn.karelian.kas.dtos.IndexParam;
import cn.karelian.kas.entities.Menus;
import cn.karelian.kas.exceptions.NullRequestException;
import cn.karelian.kas.exceptions.IllegalAccessException;
import cn.karelian.kas.exceptions.PermissionNotFoundException;
import cn.karelian.kas.services.MenusService;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author Karelian_na
 * @since 2023-11-09
 */
@RestController
@RequestMapping("/menus")
public class MenusController {
	@Autowired
	private MenusService menus;

	@Authorize
	@GetMapping("/index")
	public Result index(@ModelAttribute IndexParam params)
			throws IllegalAccessException, NullRequestException, PermissionNotFoundException {
		return menus.index(params);
	}

	@Authorize
	@PutMapping("/edit")
	public Result update(@RequestBody Menus menu) throws Exception {
		return menus.update(menu);
	}

	@Authorize
	@DeleteMapping("/delete")
	public Result delete(@RequestParam Long id) throws Exception {
		return menus.delete(id);
	}

	@Authorize
	@PostMapping("/add")
	public Result add(@RequestBody Menus menu) throws Exception {
		return menus.add(menu);
	}
}

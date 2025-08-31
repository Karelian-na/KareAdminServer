package cn.karelian.kas.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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
import cn.karelian.kas.exceptions.TransactionFailedException;
import cn.karelian.kas.services.DatabasesService;
import cn.karelian.kas.views.Views;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
class DatabaseIndexParam extends IndexParam {
	private String viewName;
}

@RestController
@RequestMapping("/admin/databases")
public class DatabasesController {
	@Autowired
	private DatabasesService databasesService;

	@Authorize
	@GetMapping("/index")
	public Result index(@ModelAttribute DatabaseIndexParam params)
			throws IllegalAccessException, NullRequestException, PermissionNotFoundException {
		if (params.getViewName() != null) {
			return databasesService.editindex(params.getViewName());
		} else {
			return databasesService.index(params);
		}
	}

	@Authorize
	@GetMapping("/edit")
	public Result editindex(@RequestParam String viewName) {
		return databasesService.editindex(viewName);
	}

	@Authorize
	@PutMapping("/edit")
	public Result edit(@Validate(uniqueKey = "view_name") @RequestBody Views params)
			throws TransactionFailedException {
		return databasesService.edit(params);
	}

	@Authorize
	@GetMapping("/configs/fields/reload")
	public Result reloadFieldsInfo() throws TransactionFailedException {
		return databasesService.reloadFieldsInfo();
	}

}

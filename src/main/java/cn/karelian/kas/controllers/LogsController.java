package cn.karelian.kas.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import cn.karelian.kas.Result;
import cn.karelian.kas.annotations.Authorize;
import cn.karelian.kas.dtos.IndexParam;
import cn.karelian.kas.entities.Logs;
import cn.karelian.kas.services.LogsService;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author Karelian_na
 * @since 2023-08-28
 */
@RestController
@RequestMapping("/logs")
public class LogsController {
	@Autowired
	private LogsService logsService;

	@Authorize
	@GetMapping("/index")
	public Result index(@ModelAttribute IndexParam params) throws Exception {

		QueryWrapper<Logs> qw = new QueryWrapper<>();
		qw.orderBy(true, false, "date");

		return logsService.index(params, qw);
	}

	@Authorize
	@DeleteMapping("/delete")
	public Result delete(@RequestParam Integer id) {
		return logsService.delete(new Integer[] { id });
	}

	@Authorize
	@DeleteMapping("/bulkdelete")
	public Result delete(@RequestParam Integer[] ids) {
		return logsService.delete(ids);
	}
}

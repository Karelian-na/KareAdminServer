package cn.karelian.kas.services;

import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import cn.karelian.kas.Result;
import cn.karelian.kas.dtos.IndexParam;
import cn.karelian.kas.entities.Logs;
import cn.karelian.kas.exceptions.NullRequestException;
import cn.karelian.kas.exceptions.IllegalAccessException;
import cn.karelian.kas.exceptions.PermissionNotFoundException;
import cn.karelian.kas.mappers.LogsMapper;
import cn.karelian.kas.services.interfaces.ILogsService;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author Karelian_na
 * @since 2023-08-28
 */
@Service
public class LogsService extends KasService<LogsMapper, Logs, Logs> implements ILogsService {
	@Override
	public Result index(IndexParam params)
			throws IllegalAccessException, NullRequestException, PermissionNotFoundException {

		var lqw = Wrappers.lambdaQuery(Logs.class);
		lqw.orderBy(true, false, Logs::getDate);

		return super.index(params, lqw);
	}

	@Override
	public Result delete(Integer[] ids) {
		Result result = new Result();
		if (ids.length == 0) {
			result.setMsg("删除日志为空！");
			return result;
		}

		result.setSuccess(this.removeBatchByIds(Stream.of(ids).toList()));
		if (!result.isSuccess()) {
			result.setMsg("删除失败！");
		}

		return result;
	}
}

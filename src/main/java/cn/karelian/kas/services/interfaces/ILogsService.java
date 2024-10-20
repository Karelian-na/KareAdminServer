package cn.karelian.kas.services.interfaces;

import cn.karelian.kas.Result;
import cn.karelian.kas.entities.Logs;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author Karelian_na
 * @since 2023-08-28
 */
public interface ILogsService extends IKasService<Logs, Logs> {
	public Result delete(Integer[] ids);
}

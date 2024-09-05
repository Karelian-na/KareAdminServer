package cn.karelian.kas.services;

import org.springframework.stereotype.Service;

import cn.karelian.kas.entities.Logs;
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

}

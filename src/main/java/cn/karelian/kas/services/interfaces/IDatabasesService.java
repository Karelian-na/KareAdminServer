package cn.karelian.kas.services.interfaces;

import com.baomidou.mybatisplus.extension.service.IService;

import cn.karelian.kas.Result;
import cn.karelian.kas.entities.ViewsInfo;
import cn.karelian.kas.exceptions.TransactionFailedException;
import cn.karelian.kas.views.Views;

/**
 * <p>
 * 学生参见本领域国内外重要学术会议表(研究生) 服务类
 * </p>
 *
 * @author baomidou
 * @since 2023-01-15
 */
public interface IDatabasesService extends IService<ViewsInfo> {

	public Result editindex(String viewName);

	public Result edit(Views params) throws TransactionFailedException;

	public Result reloadFieldsInfo();

	public String getFieldsConfig(Class<?> entityClszz);
}

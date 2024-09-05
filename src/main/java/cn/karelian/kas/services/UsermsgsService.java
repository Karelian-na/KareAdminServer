package cn.karelian.kas.services;

import org.springframework.stereotype.Service;

import cn.karelian.kas.entities.Usermsgs;
import cn.karelian.kas.mappers.UsermsgsMapper;
import cn.karelian.kas.services.interfaces.IUsermsgsService;
import cn.karelian.kas.views.UsermsgsView;

/**
 * <p>
 * 管理用户基本信息的表 服务实现类
 * </p>
 *
 * @author Karelian_na
 * @since 2023-08-28
 */
@Service
public class UsermsgsService extends KasService<UsermsgsMapper, Usermsgs, UsermsgsView> implements IUsermsgsService {

}

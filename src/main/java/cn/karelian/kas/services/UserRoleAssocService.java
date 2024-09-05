package cn.karelian.kas.services;

import org.springframework.stereotype.Service;

import cn.karelian.kas.entities.UserRoleAssoc;
import cn.karelian.kas.mappers.UserRoleAssocMapper;
import cn.karelian.kas.services.interfaces.IUserRoleAssocService;

/**
 * <p>
 * 用户角色关联表 服务实现类
 * </p>
 *
 * @author Karelian_na
 * @since 2023-08-28
 */
@Service
public class UserRoleAssocService extends KasService<UserRoleAssocMapper, UserRoleAssoc, UserRoleAssoc>
		implements IUserRoleAssocService {

}

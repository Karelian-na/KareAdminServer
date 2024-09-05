package cn.karelian.kas.services;

import org.springframework.stereotype.Service;

import cn.karelian.kas.entities.UserPermAssoc;
import cn.karelian.kas.mappers.UserPermAssocMapper;
import cn.karelian.kas.services.interfaces.IUserPermAssocService;

/**
 * <p>
 * 用户权限关联表 服务实现类
 * </p>
 *
 * @author Karelian_na
 * @since 2023-08-28
 */
@Service
public class UserPermAssocService extends KasService<UserPermAssocMapper, UserPermAssoc, UserPermAssoc>
		implements IUserPermAssocService {

}

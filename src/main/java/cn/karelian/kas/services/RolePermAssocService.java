package cn.karelian.kas.services;

import org.springframework.stereotype.Service;

import cn.karelian.kas.entities.RolePermAssoc;
import cn.karelian.kas.mappers.RolePermAssocMapper;
import cn.karelian.kas.services.interfaces.IRolePermAssocService;

/**
 * <p>
 * 角色权限关联表 服务实现类
 * </p>
 *
 * @author Karelian_na
 * @since 2023-08-28
 */
@Service
public class RolePermAssocService extends KasService<RolePermAssocMapper, RolePermAssoc, RolePermAssoc>
		implements IRolePermAssocService {

}

package cn.karelian.kas.services.interfaces;

import cn.karelian.kas.Result;
import cn.karelian.kas.entities.Permissions;
import cn.karelian.kas.exceptions.NullRequestException;
import cn.karelian.kas.views.MenusView;
import cn.karelian.kas.views.PermissionsView;

/**
 * <p>
 * 管理权限目录的表 服务类
 * </p>
 *
 * @author Karelian_na
 * @since 2023-08-28
 */
public interface IPermissionsService extends IKasService<Permissions, PermissionsView> {
	/**
	 * @description: 获取指定url的权限
	 * @param {String} url
	 * @return {Permissions}
	 */
	public Permissions getByUrl(String url);

	/**
	 * 判断当前访问是否被授权
	 * 
	 * @param menu 将要判断的访问的菜单
	 * @throws NullRequestException
	 */
	public boolean isAuthorized(MenusView menu) throws NullRequestException;

	public Result add(PermissionsView permission) throws NullRequestException;

	public Result update(PermissionsView permission);

	public Result delete(Integer id);
}

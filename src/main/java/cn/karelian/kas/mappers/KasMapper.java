package cn.karelian.kas.mappers;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;

public interface KasMapper<E, V> extends BaseMapper<E> {
	/**
	 * 获取实体对应的视图数据
	 * 
	 * @param ew 过滤和组织条件
	 * @return
	 */
	public List<V> selectViewList(@Param("ew") Wrapper<V> ew);

	/**
	 * 获取实体对应的视图数据
	 * 
	 * @param ew 过滤和组织条件
	 * @return
	 */
	public List<Map<String, Object>> selectViewMaps(@Param("ew") Wrapper<V> ew);

	/**
	 * 获取指定条件的 在视图中的数据
	 * 
	 * @param ew
	 * @return
	 */
	public V selectViewOne(@Param("ew") Wrapper<V> ew);

	/**
	 * 获取指定条件的视图的分页数据
	 * 
	 * @param <P>  分页结构
	 * @param page 分页属性
	 * @param ew   过滤和组织条件
	 * @return
	 */
	public <P extends IPage<V>> P selectViewPage(P page, @Param(value = "ew") Wrapper<V> ew);

	/**
	 * 获取指定主键值的视图数据
	 * 
	 * @param id
	 * @return
	 */
	public V selectViewById(Serializable id);

	/**
	 * 获取视图中数据数量
	 * 
	 * @param ew
	 * @return
	 */
	public Long selectViewCount(@Param("ew") Wrapper<V> ew);

	/**
	 * 判断指定条件的数据是否在视图中存在
	 * 
	 * @param ew
	 * @return
	 */
	default boolean existsView(@Param("ew") Wrapper<V> ew) {
		Long count = this.selectViewCount(ew);
		return null != count && count > 0L;
	}

}

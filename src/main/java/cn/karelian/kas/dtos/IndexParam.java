package cn.karelian.kas.dtos;

import cn.karelian.kas.annotations.ComparableValidate;
import cn.karelian.kas.annotations.StringValidate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IndexParam {
	@ComparableValidate(min = 20, max = 200)
	public Long initPageSize;

	@ComparableValidate(min = 1, max = 1000)
	public Long pageIdx;

	@ComparableValidate(min = 20, max = 200)
	public Long pageSize;

	@StringValidate(minLen = 0, maxLen = 25)
	public String searchKey;

	public String searchField;

	public boolean one;
}
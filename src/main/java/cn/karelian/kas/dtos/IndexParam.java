package cn.karelian.kas.dtos;

import com.fasterxml.jackson.annotation.JsonIgnore;

import cn.karelian.kas.annotations.ComparableValidate;
import cn.karelian.kas.annotations.StringValidate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IndexParam {
	public enum IndexType {
		Page,
		One,
		All
	};

	@ComparableValidate(min = 20, max = 200)
	public Long initPageSize;

	@ComparableValidate(min = 1, max = 1000)
	public Long pageIdx;

	@ComparableValidate(min = 20, max = 200)
	public Long pageSize;

	@StringValidate(minLen = 0, maxLen = 25)
	public String searchKey;

	public String searchField;

	@JsonIgnore
	public IndexType type = IndexType.Page;
}
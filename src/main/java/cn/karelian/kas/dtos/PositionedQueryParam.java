package cn.karelian.kas.dtos;

import cn.karelian.kas.annotations.ComparableValidate;
import cn.karelian.kas.annotations.StringValidate;
import cn.karelian.kas.exceptions.InvalidArgumentException;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PositionedQueryParam implements IAdjustableParam {
	private static enum PageDirection {
		PREV,
		NEXT,
	}

	private String page_pos;

	@ComparableValidate(min = 20, max = 200)
	private Long pageSize;

	@ComparableValidate(min = 20)
	private Long initPageSize;

	private PageDirection direction;

	@StringValidate(minLen = 0, maxLen = 25)
	public String searchKey;

	@StringValidate(regex = "[a-zA-Z_]{2,}", dependsOn = "searchKey")
	public String searchField;

	@Override
	public void validateAndRegular() throws InvalidArgumentException {
		if (pageSize == null) {
			pageSize = 20L;
		}

		if (direction == null) {
			direction = PageDirection.NEXT;
		}
	}

	public Long getPageSize() {
		return this.pageSize == null ? (this.initPageSize == null ? 20L : this.initPageSize) : this.pageSize;
	}
}

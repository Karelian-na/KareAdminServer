package cn.karelian.kas.utils;

import java.util.List;

public class PageData<T> {
	public Long totalCount;
	public Long curPageIdx;
	public List<T> data;
}

package com.eiff.framework.common.biz.pkg.page;

import com.eiff.framework.common.biz.pkg.BaseRequest;

/**
 * @author bohan
 * 
 *         分页请求基类
 *
 */
public class PageableRequest extends BaseRequest {

	private static final long serialVersionUID = 1L;

	private int pageNum;
	private int pageSize;
	private Sort sort;

	public int getPageNum() {
		return pageNum;
	}

	public void setPageNum(int pageNum) {
		this.pageNum = pageNum;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public Sort getSort() {
		return sort;
	}

	public void setSort(Sort sort) {
		this.sort = sort;
	}
}

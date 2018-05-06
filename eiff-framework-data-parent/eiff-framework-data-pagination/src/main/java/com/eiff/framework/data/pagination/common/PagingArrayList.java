package com.eiff.framework.data.pagination.common;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

public class PagingArrayList<T> extends ArrayList<T> {

	private static final long serialVersionUID = 6938303223560962410L;
	public PagingArrayList() {
	}
	public PagingArrayList(List<T> list, Pageable pageable, Long total) {
		super(list);
		this.pageable = pageable;
		this.total = total;
		this.page = new PageImpl<T>(list, pageable, total);
	}
	
	private Page<T> page;
	private Long total;
	private Pageable pageable;

	public Long getTotal() {
		return total;
	}

	public Pageable getPageable() {
		return pageable;
	}

	public Page<T> getPage() {
		return page;
	}
	
}

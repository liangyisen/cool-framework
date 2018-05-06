package com.eiff.framework.data.pagination.common;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;

import com.eiff.framework.common.biz.pkg.page.PageableRequest;
import com.eiff.framework.common.biz.pkg.page.PageableResponse;
import com.eiff.framework.common.biz.pkg.page.Sort;
import com.eiff.framework.common.biz.pkg.page.Sort.Order;

public class ConversionPluging {

	public static <T> PageableResponse<T> map(PagingArrayList<T> pageList, PageableResponse<T> response) {
		Page<T> page = pageList.getPage();
		response.init(page.getNumber(), page.getNumberOfElements(), page.getSize(), page.getTotalPages(),
				page.getTotalElements(), page.getContent());
		return response;
	}

	public static PageRequest map(PageableRequest request){
		Sort sort = request.getSort();
		if(sort != null){
			List<org.springframework.data.domain.Sort.Order> springOrders = new ArrayList<>();
			for (Order order : sort) {
				springOrders.add(new org.springframework.data.domain.Sort.Order(Direction.valueOf(order.toString()), order.getProperty()));
			}
			return new PageRequest(request.getPageNum(), request.getPageSize(), new org.springframework.data.domain.Sort(springOrders));
		}else{
			return new PageRequest(request.getPageNum(), request.getPageSize());
		}
	}
}

package com.eiff.framework.common.biz.pkg.page;

import java.util.List;

import com.eiff.framework.common.biz.pkg.BaseResponse;

/**
 * @author bohan
 *
 * @param <T>
 * 
 *            分页应答基类
 * 
 */
public class PageableResponse<T> extends BaseResponse {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// 当前页
	private int currentPageNum;
	// 当前页的数量
	private int currentPagesize;
	// 每页的数量
	private int pageSize;
	// 总记录数
	private long total;
	// 总页数
	private int pages;
	// 结果集
	private List<T> list;

	// 第一页
	private int firstPage;
	// 前一页
	private int prePage;
	// 下一页
	private int nextPage;
	// 最后一页
	private int lastPage;

	// 是否为第一页
	private boolean isFirstPage = false;
	// 是否为最后一页
	private boolean isLastPage = false;
	// 是否有前一页
	private boolean hasPreviousPage = false;
	// 是否有下一页
	private boolean hasNextPage = false;

	// 导航页码数
	private int navigatePages;
	private int[] navigatepageNums;

	public PageableResponse() {
	}
	/**
	 * 包装Page对象
	 *
	 * @param list
	 */
	public PageableResponse(List<T> list) {
		this(list, 8);
	}

	/**
	 * 包装Page对象
	 *
	 * @param list
	 *            page结果
	 * @param navigatePages
	 *            页码数量
	 */
	public PageableResponse(List<T> list, int navigatePages) {
		this.currentPageNum = 1;
		this.pageSize = list.size();

		this.pages = 1;
		this.list = list;
		this.currentPagesize = list.size();
		this.total = list.size();
		this.navigatePages = navigatePages;
		// 计算导航页
		calcNavigatepageNums();
		// 计算前后页，第一页，最后一页
		calcPage();
		// 判断页面边界
		judgePageBoudary();
	}

	public PageableResponse<T> init(int currentPageNum, int currentPageSize, int pageSize,int totalPages,long totalElements,
			List<T> list) {
		this.currentPageNum = currentPageNum;
		this.currentPagesize = currentPageSize;
		
		
		this.pageSize = pageSize;
		this.pages = totalPages;
		this.list = list;
		this.total = totalElements	;

		this.navigatePages = 8;
		// 计算导航页
		calcNavigatepageNums();
		// 计算前后页，第一页，最后一页
		calcPage();
		// 判断页面边界
		judgePageBoudary();
		return this;
	}

	/**
	 * 计算导航页
	 */
	private void calcNavigatepageNums() {
		// 当总页数小于或等于导航页码数时
		if (pages <= navigatePages) {
			navigatepageNums = new int[pages];
			for (int i = 0; i < pages; i++) {
				navigatepageNums[i] = i + 1;
			}
		} else { // 当总页数大于导航页码数时
			navigatepageNums = new int[navigatePages];
			int startNum = currentPageNum - navigatePages / 2;
			int endNum = currentPageNum + navigatePages / 2;

			if (startNum < 1) {
				startNum = 1;
				// (最前navigatePages页
				for (int i = 0; i < navigatePages; i++) {
					navigatepageNums[i] = startNum++;
				}
			} else if (endNum > pages) {
				endNum = pages;
				// 最后navigatePages页
				for (int i = navigatePages - 1; i >= 0; i--) {
					navigatepageNums[i] = endNum--;
				}
			} else {
				// 所有中间页
				for (int i = 0; i < navigatePages; i++) {
					navigatepageNums[i] = startNum++;
				}
			}
		}
	}

	/**
	 * 计算前后页，第一页，最后一页
	 */
	private void calcPage() {
		if (navigatepageNums != null && navigatepageNums.length > 0) {
			firstPage = navigatepageNums[0];
			lastPage = navigatepageNums[navigatepageNums.length - 1];
			if (currentPageNum > 1) {
				prePage = currentPageNum - 1;
			}
			if (currentPageNum < pages) {
				nextPage = currentPageNum + 1;
			}
		}
	}

	/**
	 * 判定页面边界
	 */
	private void judgePageBoudary() {
		isFirstPage = currentPageNum == 1;
		isLastPage = currentPageNum == pages;
		hasPreviousPage = currentPageNum > 1;
		hasNextPage = currentPageNum < pages;
	}

	public int getCurrentPageNum() {
		return currentPageNum;
	}

	public void setCurrentPageNum(int currentPageNum) {
		this.currentPageNum = currentPageNum;
	}

	public int getCurrentPagesize() {
		return currentPagesize;
	}

	public void setCurrentPagesize(int currentPagesize) {
		this.currentPagesize = currentPagesize;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public long getTotal() {
		return total;
	}

	public void setTotal(long total) {
		this.total = total;
	}

	public int getPages() {
		return pages;
	}

	public void setPages(int pages) {
		this.pages = pages;
	}

	public List<T> getList() {
		return list;
	}

	public void setList(List<T> list) {
		this.list = list;
	}

	public int getFirstPage() {
		return firstPage;
	}

	public void setFirstPage(int firstPage) {
		this.firstPage = firstPage;
	}

	public int getPrePage() {
		return prePage;
	}

	public void setPrePage(int prePage) {
		this.prePage = prePage;
	}

	public int getNextPage() {
		return nextPage;
	}

	public void setNextPage(int nextPage) {
		this.nextPage = nextPage;
	}

	public int getLastPage() {
		return lastPage;
	}

	public void setLastPage(int lastPage) {
		this.lastPage = lastPage;
	}

	public boolean isFirstPage() {
		return isFirstPage;
	}

	public void setFirstPage(boolean isFirstPage) {
		this.isFirstPage = isFirstPage;
	}

	public boolean isLastPage() {
		return isLastPage;
	}

	public void setLastPage(boolean isLastPage) {
		this.isLastPage = isLastPage;
	}

	public boolean isHasPreviousPage() {
		return hasPreviousPage;
	}

	public void setHasPreviousPage(boolean hasPreviousPage) {
		this.hasPreviousPage = hasPreviousPage;
	}

	public boolean isHasNextPage() {
		return hasNextPage;
	}

	public void setHasNextPage(boolean hasNextPage) {
		this.hasNextPage = hasNextPage;
	}
}

package cn.coder.struts.view;

import cn.coder.struts.util.StringUtils;

public class PageResult {

	private long total;
	private int current;
	private int pageSize;
	private int startRow;
	private int endRow;
	private int pages;

	public PageResult(String page, int pageSize) {
		if(StringUtils.isEmpty(page))
			page = "1";
		this.current = Integer.parseInt(page);
		this.pageSize = pageSize;
		this.startRow = (current - 1) * pageSize;
		this.endRow = current * pageSize;
	}

	public long getTotal() {
		return total;
	}

	public int getPageSize() {
		return pageSize;
	}

	public int getPages() {
		return pages;
	}

	public int getStartRow() {
		return startRow;
	}

	public int getEndRow() {
		return endRow;
	}

	public int getCurrent() {
		return current;
	}

	public void setTotal(Long total){
		this.total = total;
		this.pages = (int) Math.ceil(total / Double.parseDouble(pageSize + ""));
	}

}

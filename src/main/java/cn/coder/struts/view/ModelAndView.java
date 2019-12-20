package cn.coder.struts.view;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import cn.coder.struts.support.ServletWebRequest;

public class ModelAndView implements View {

	private String viewName;
	private Map<String, Object> data = new HashMap<>();

	public ModelAndView(String name) {
		this.viewName = name;
	}

	public String getViewName() {
		if (!this.viewName.startsWith("/"))
			this.viewName = "/" + this.viewName;
		return this.viewName;
	}

	public ModelAndView addObject(String name, Object obj) {
		this.data.put(name, obj);
		return this;
	}

	public Map<String, Object> getData() {
		return this.data;
	}

	@Override
	public boolean supports(Object result) {
		return (result instanceof ModelAndView);
	}

	@Override
	public void render(ServletWebRequest req, Object result) throws Exception {
		ModelAndView mav = (ModelAndView) result;
		Map<String, Object> data = mav.getData();
		for (Entry<String, Object> entry : data.entrySet()) {
			req.getRequest().setAttribute(entry.getKey(), entry.getValue());
		}
		req.forward(mav.getViewName());
	}

}

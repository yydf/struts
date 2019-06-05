package cn.coder.struts.view;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.coder.struts.util.BeanUtils;

public final class ModelAndView {
	private static final Logger logger = LoggerFactory.getLogger(ModelAndView.class);
	private String viewName;
	private final HashMap<String, Object> temp;

	public ModelAndView() {
		this(null);
	}

	public ModelAndView(String view) {
		if (view != null)
			this.viewName = view;
		temp = new HashMap<>(32);
	}

	public static ModelAndView view(String view) {
		return new ModelAndView(view);
	}

	public String getViewName() {
		return this.viewName;
	}

	public ModelAndView setViewName(String view) {
		this.viewName = view;
		return this;
	}

	public ModelAndView addObject(Object obj) {
		if (obj == null)
			return this;
		Set<Field> fields = BeanUtils.getDeclaredFields(obj.getClass());
		if (!fields.isEmpty()) {
			for (Field field : fields) {
				if ("serialVersionUID".equals(field.getName()))
					continue;
				try {
					field.setAccessible(true);
					addObject(field.getName(), field.get(obj));
				} catch (IllegalArgumentException | IllegalAccessException e) {
					logger.error("Add object faild", e);
				}
			}
		}
		return this;
	}

	public ModelAndView addObject(String attr, Object obj) {
		this.temp.put(attr, obj);
		logger.debug("Add object [{}]{}", attr, obj);
		return this;
	}

	public void fillRequest(HttpServletRequest req) {
		if (!temp.isEmpty()) {
			Set<Entry<String, Object>> entries = temp.entrySet();
			for (Entry<String, Object> entry : entries) {
				req.setAttribute(entry.getKey(), entry.getValue());
			}
			logger.debug("Fill request:{}", entries.size());
		}
	}
}

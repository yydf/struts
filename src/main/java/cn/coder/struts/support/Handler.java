package cn.coder.struts.support;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.coder.struts.core.Handlers;

public interface Handler {

	void handle(HttpServletRequest req, HttpServletResponse res, Handlers stack);
}

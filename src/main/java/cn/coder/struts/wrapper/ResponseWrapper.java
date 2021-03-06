package cn.coder.struts.wrapper;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.coder.struts.util.StringUtils;
import cn.coder.struts.view.JSONMap;
import cn.coder.struts.view.ModelAndView;

public final class ResponseWrapper {
	private static final Logger logger = LoggerFactory.getLogger(ResponseWrapper.class);
	
	private static final int LOG_LIMIIT = 1024;
	private static final String CONTENT_TYPE_JSON = "application/json;charset=UTF-8";
	private static final String CONTENT_TYPE_TEXT = "text/plain;charset=UTF-8";

	public void doResponse(Object result, HttpServletRequest req, HttpServletResponse res)
			throws IOException, ServletException {
		boolean supportGzip = isSupportGZip(req);
		if (result instanceof JSONMap) {
			res.setContentType(CONTENT_TYPE_JSON);
			String json = result.toString();
			String callback = req.getParameter("callback");
			if (!StringUtils.isEmpty(callback))
				json = callback + "(" + json + ")";
			renderText(json, supportGzip, res);
			((JSONMap) result).clear();
			if (logger.isDebugEnabled()) {
				if (json.length() > LOG_LIMIIT)
					logger.debug("[JSON]" + json.substring(0, LOG_LIMIIT) + "...");
				else
					logger.debug("[JSON]" + json);
			}
		} else if (result instanceof String) {
			res.setContentType(CONTENT_TYPE_TEXT);
			String text = result.toString();
			renderText(text, supportGzip, res);
			if (logger.isDebugEnabled()) {
				if (text.length() > LOG_LIMIIT)
					logger.debug("[TEXT]" + text.substring(0, LOG_LIMIIT) + "...");
				else
					logger.debug("[TEXT]" + text);
			}
		} else if (result instanceof ModelAndView) {
			renderView((ModelAndView) result, req, res);
		} else
			throw new ServletException("Unsupported return type " + result.getClass());
	}

	private static boolean isSupportGZip(HttpServletRequest req) {
		String encoding = req.getHeader("Accept-Encoding");
		return encoding != null && encoding.indexOf("gzip") > -1;
	}

	private static void renderView(ModelAndView mav, HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		String view = mav.getViewName();
		view = view.startsWith("/") ? view : ("/" + view);
		if (!view.endsWith(".html") && !view.endsWith(".htm")) {
			view = view.endsWith(".jsp") ? view : (view + ".jsp");
			mav.fillRequest(req);
		}
		req.getRequestDispatcher(view).forward(req, res);
		if (logger.isDebugEnabled())
			logger.debug("Forward to " + view);
	}

	private static void renderText(String text, boolean supportGzip, HttpServletResponse res) throws IOException {
		int len = text.length();
		if (supportGzip && len > 128) {
			long start = System.nanoTime();
			res.addHeader("Content-Encoding", "gzip");
			GZIPOutputStream output = new GZIPOutputStream(res.getOutputStream());
			output.write(text.getBytes("utf-8"));
			output.close();
			output.finish();
			if (logger.isDebugEnabled())
				logger.debug("Compress gzip from {} to {} in {} ns", len, res.getHeader("Content-Length"),
						(System.nanoTime() - start));
		} else {
			PrintWriter pw = res.getWriter();
			pw.write(text);
			pw.close();
		}
	}
}

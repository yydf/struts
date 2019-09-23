package cn.coder.struts.core;

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

public final class ViewHandler {
	private static final Logger logger = LoggerFactory.getLogger(ViewHandler.class);

	private final String encoding;
	private final String CONTENT_TYPE_JSON;
	private final String CONTENT_TYPE_TEXT;

	private static final int LOG_LIMIIT = 1024;

	public ViewHandler(String encoding) {
		this.encoding = encoding;
		CONTENT_TYPE_JSON = String.format("application/json;charset=%s", encoding);
		CONTENT_TYPE_TEXT = String.format("text/plain;charset=%s", encoding);
	}

	public void handle(Object result, HttpServletRequest req, HttpServletResponse res)
			throws IOException, ServletException {
		boolean supportGzip = isSupportGZip(req);
		if (result instanceof JSONMap) {
			res.setContentType(CONTENT_TYPE_JSON);
			String json = result.toString();
			String callback = req.getParameter("callback");
			if (!StringUtils.isEmpty(callback))
				json = callback + "(" + json + ")";
			renderText(json, this.encoding, supportGzip, res);
			if (logger.isDebugEnabled()) {
				if (json.length() > LOG_LIMIIT)
					logger.debug("[JSON]" + json.substring(0, LOG_LIMIIT) + "...");
				else
					logger.debug("[JSON]" + json);
			}
		} else if (result instanceof String) {
			res.setContentType(CONTENT_TYPE_TEXT);
			String text = result.toString();
			renderText(text, this.encoding, supportGzip, res);
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
		String accept = req.getHeader("Accept-Encoding");
		return accept != null && accept.indexOf("gzip") > -1;
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

	private static void renderText(String text, String encoding, boolean supportGzip, HttpServletResponse res)
			throws IOException {
		int len = text.length();
		if (supportGzip && len > 512) {
			long start = System.nanoTime();
			res.addHeader("Content-Encoding", "gzip");
			GZIPOutputStream output = new GZIPOutputStream(res.getOutputStream());
			output.write(text.getBytes(encoding));
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

package cn.coder.struts.wrapper;

import java.io.IOException;
import java.io.PrintWriter;
import java.rmi.ServerException;
import java.util.zip.GZIPOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.coder.struts.core.ContextUtils;
import cn.coder.struts.view.JSONMap;

public class ResponseWrapper {
	private static final Logger logger = LoggerFactory.getLogger(ResponseWrapper.class);

	public void doResponse(Object result, HttpServletRequest req, HttpServletResponse res)
			throws IOException, ServerException {
		boolean supportGzip = ContextUtils.isSupportGZip(req);
		if (result instanceof JSONMap) {
			res.setContentType("application/json;charset=UTF-8");
			String json = result.toString();
			renderText(json, supportGzip, res);
			if (logger.isDebugEnabled())
				logger.debug("[JSON]" + json);
		} else if (result instanceof String) {
			res.setContentType("text/plain;charset=UTF-8");
			String text = result.toString();
			renderText(text, supportGzip, res);
			if (logger.isDebugEnabled())
				logger.debug("[TEXT]" + text);
		} else
			throw new ServerException("Unsupported return type " + result.getClass().getTypeName());
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
				logger.debug("Compress gzip from " + len + " to " + res.getHeader("Content-Length") + " in "
						+ (System.nanoTime() - start) + " ns");
		} else {
			PrintWriter pw = res.getWriter();
			pw.write(text);
			pw.close();
		}
	}
}

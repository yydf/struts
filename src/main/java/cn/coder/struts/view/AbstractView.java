package cn.coder.struts.view;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.zip.GZIPOutputStream;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractView implements View {
	private static final Logger logger = LoggerFactory.getLogger(AbstractView.class);

	private static final String ENCODING = "UTF-8";
	private static final int LOG_LIMIIT = 1024;

	/**
	 * 响应文本，如果长度大于512，则根据客户端判断是否压缩
	 * 
	 * @param str
	 * @param supportGzip
	 * @param res
	 * @throws IOException
	 */
	protected static void renderText(String str, boolean supportGzip, HttpServletResponse res) throws IOException {
		int len = str.length();
		if (supportGzip && len > 512) {
			long start = System.nanoTime();
			res.addHeader("Content-Encoding", "gzip");
			GZIPOutputStream output = new GZIPOutputStream(res.getOutputStream());
			output.write(str.getBytes(ENCODING));
			output.close();
			if (logger.isDebugEnabled())
				logger.debug("Compress gzip from {} to {} in {} ns", len, res.getHeader("Content-Length"),
						(System.nanoTime() - start));
		} else {
			PrintWriter pw = res.getWriter();
			pw.write(str);
			pw.close();
		}
		if (logger.isDebugEnabled()) {
			if (len > LOG_LIMIIT)
				logger.debug("[RENDER]{}...", str.substring(0, LOG_LIMIIT));
			else
				logger.debug("[RENDER]{}", str);
		}
	}
}

package cn.coder.struts.wrapper;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.coder.struts.util.Streams;
import cn.coder.struts.util.multipart.PartIterator;
import cn.coder.struts.util.multipart.FileItemStream;
import cn.coder.struts.view.MultipartFile;

public final class MultipartRequestWrapper {
	private static final Logger logger = LoggerFactory.getLogger(MultipartRequestWrapper.class);

	private HttpServletRequest request;
	private HashMap<String, String> paras = new HashMap<>();
	private HashMap<String, MultipartFile> multipartFiles = new HashMap<>();

	public MultipartRequestWrapper(HttpServletRequest req, processFile process) {
		this.request = req;
		wrapperRequest(process);
		if (logger.isDebugEnabled())
			logger.debug("Wrapper multipart request");
	}

	private void wrapperRequest(processFile process) {
		try {
			PartIterator items = new PartIterator(request);
			FileItemStream stream;
			while (items.hasNext()) {
				stream = items.next();
				if (stream.isFormField())
					paras.put(stream.getFieldName(), Streams.asString(stream.openStream(), "utf-8"));
				else {
					MultipartFile file = new MultipartFile(stream);
					paras.put(file.getFieldName(), process.processMultipartFile(file));
					multipartFiles.put(file.getFieldName(), file);
				}
			}
		} catch (IOException e) {
			logger.error("Process request faild", e);
		}
	}
	
	public static final boolean isMultipartContent(HttpServletRequest request) {
		if (!"POST".equalsIgnoreCase(request.getMethod())) {
			return false;
		}
		String contentType = request.getContentType();
		if (contentType == null) {
			return false;
		}
		return contentType.toLowerCase().startsWith("multipart/");
	}

	public String getField(String name, String temp) {
		String str = paras.get(name);
		if (str == null)
			return temp;
		return str;
	}

	public MultipartFile getMultipartFile(String name) {
		return multipartFiles.get(name);
	}

	public void clear() {
		paras.clear();
		paras = null;
		for (MultipartFile file : multipartFiles.values()) {
			file.clear();
		}
		multipartFiles.clear();
		multipartFiles = null;
		request = null;
	}

	public interface processFile {
		String processMultipartFile(MultipartFile file);
	}
}

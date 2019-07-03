package cn.coder.struts.wrapper;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.tomcat.util.http.fileupload.FileItemIterator;
import org.apache.tomcat.util.http.fileupload.FileItemStream;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.apache.tomcat.util.http.fileupload.util.Streams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.coder.struts.view.MultipartFile;

public final class MultipartRequestWrapper {
	private static final Logger logger = LoggerFactory.getLogger(MultipartRequestWrapper.class);

	private HttpServletRequest request;
	private HashMap<String, String> paras = new HashMap<>();
	private HashMap<String, MultipartFile> multipartFiles = new HashMap<>();
	private static ServletFileUpload upload;

	public MultipartRequestWrapper(HttpServletRequest req, processFile process) {
		this.request = req;
		// 只初始化一次
		if (upload == null) {
			upload = new ServletFileUpload();
		}
		wrapperRequest(process);
	}

	private void wrapperRequest(processFile process) {
		try {
			FileItemIterator items = upload.getItemIterator(request);
			if (items != null) {
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
			}
		} catch (FileUploadException | IOException e) {
			logger.error("Process request faild", e);
		}
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

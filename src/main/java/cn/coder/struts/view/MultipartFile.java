package cn.coder.struts.view;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.coder.struts.util.Streams;
import cn.coder.struts.util.StringUtils;
import cn.coder.struts.wrapper.MultipartRequestWrapper;
import cn.coder.struts.wrapper.MultipartStream;

public final class MultipartFile {
	private static final Logger logger = LoggerFactory.getLogger(MultipartFile.class);

	/**
	 * HTTP content type header name.
	 */
	public static final String CONTENT_TYPE = "Content-Type";

	private static final String CONTENT_DISPOSITION = MultipartRequestWrapper.CONTENT_DISPOSITION;
	private static final String FORM_DATA = MultipartRequestWrapper.FORM_DATA;

	private String fileName;
	private String fieldName;
	private InputStream inputStream;
	private long size;
	private String contentType;
	private String extension;

	public MultipartFile(MultipartStream stream) throws IOException {

	}

	public MultipartFile(Map<String, String> headers, InputStream input) {
		this.fileName = getFileName(headers);
		this.fieldName = getFieldName(headers);
		this.inputStream = input;
		try {
			this.size = (long) input.available();
		} catch (IOException e) {
			this.size = 0;
		}
		this.contentType = headers.get(CONTENT_TYPE);
		if (!StringUtils.isEmpty(fileName)) {
			int last = fileName.lastIndexOf(".");
			if (last > 0)
				this.extension = fileName.substring(last).toLowerCase();
			else
				this.extension = "";
		}
	}

	private static String getFileName(Map<String, String> headers) {
		String contentDisposition = headers.get(CONTENT_DISPOSITION);
		if (contentDisposition != null)
			return Streams.parseValue(contentDisposition, "filename");
		return null;
	}

	private static String getFieldName(Map<String, String> headers) {
		String contentDisposition = headers.get(CONTENT_DISPOSITION);
		if (contentDisposition != null && contentDisposition.toLowerCase().startsWith(FORM_DATA))
			return Streams.parseValue(contentDisposition, "name");
		return null;
	}

	public long getSize() {
		return size;
	}

	public String getContentType() {
		return contentType;
	}

	public String getFieldName() {
		return this.fieldName;
	}

	public String getFileName() {
		return this.fileName;
	}

	public String getExtension() {
		return this.extension;
	}

	public boolean transferTo(File dest) {
		if (this.inputStream == null)
			return false;
		FileOutputStream fos = null;
		try {
			if (!dest.getParentFile().exists())
				dest.getParentFile().mkdirs();
			fos = new FileOutputStream(dest);
			byte[] buffer = new byte[102400];
			int n = 0;
			while ((n = inputStream.read(buffer)) > 0) {
				fos.write(buffer, 0, n);
			}
			return true;
		} catch (IOException e) {
			if (logger.isErrorEnabled())
				logger.error("Transfer file faild", e);
			return false;
		} finally {
			// 关闭输入输出流
			Streams.close(inputStream);
			Streams.close(fos);
		}
	}

	public void clear() {
		this.fileName = null;
		this.extension = null;
		this.inputStream = null;
		this.fieldName = null;
		this.contentType = null;
	}
}

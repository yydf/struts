package cn.coder.struts.mvc;

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

public class MultipartFile {
	private static final Logger logger = LoggerFactory.getLogger(MultipartFile.class);
	private static final String CONTENT_TYPE = "Content-Type";
	private static final String CONTENT_DISPOSITION = MultipartRequestWrapper.CONTENT_DISPOSITION;

	private String fileName;
	private String fieldName;
	private InputStream stream;
	private long size;
	private String contentType;
	private String extension;

	public MultipartFile(Map<String, String> headers, InputStream inputStream) throws IOException {
		this.fileName = getFileName(headers);
		this.fieldName = getFieldName(headers);
		this.stream = inputStream;
		this.size = (long) inputStream.available();
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
		return Streams.parseValue(headers.get(CONTENT_DISPOSITION), "filename");
	}

	private static String getFieldName(Map<String, String> headers) {
		return Streams.parseValue(headers.get(CONTENT_DISPOSITION), "name");
	}

	public long getSize() {
		return this.size;
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
		if (this.stream == null || dest == null)
			return false;
		if (!dest.getParentFile().exists())
			dest.getParentFile().mkdirs();
		try {
			return Streams.copy(this.stream, new FileOutputStream(dest), true) > 0;
		} catch (IOException e) {
			logger.error("Transfer file faild", e);
			return false;
		}
	}

	public void clear() {
		this.fileName = null;
		this.extension = null;
		this.stream = null;
		this.fieldName = null;
		this.contentType = null;
	}

}

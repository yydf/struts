package cn.coder.struts.view;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.tomcat.util.http.fileupload.FileItemStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.coder.struts.util.StringUtils;

public final class MultipartFile {
	private static final Logger logger = LoggerFactory.getLogger(MultipartFile.class);

	private String fileName;
	private String fieldName;
	private InputStream inputStream;
	private long size;
	private String contentType;
	private String extension;

	public MultipartFile(FileItemStream stream) throws IOException {
		this.fileName = stream.getName();
		this.fieldName = stream.getFieldName();
		this.inputStream = stream.openStream();
		this.size = (long) this.inputStream.available();
		this.contentType = stream.getContentType();
		if (!StringUtils.isEmpty(fileName)) {
			int last = fileName.lastIndexOf(".");
			if (last > 0)
				this.extension = fileName.substring(last).toLowerCase();
			else
				this.extension = "";
		}
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
				logger.error("Save file faild", e);
			return false;
		} finally {
			// 关闭输输出流
			if (fos != null) {
				try {
					inputStream.close();
					fos.close();
				} catch (IOException e) {
					if (logger.isErrorEnabled())
						logger.error("FileOutputStream close faild", e);
				}
			}
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

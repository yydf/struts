package cn.coder.struts.view;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.tomcat.util.http.fileupload.FileItemStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.coder.struts.util.StringUtils;

public class MultipartFile {
	private static final Logger logger = LoggerFactory.getLogger(MultipartFile.class);

	private String fileName;
	private String extension;
	private Long size;
	private InputStream inputStream;
	private String fieldName;
	private String contentType;

	public MultipartFile(FileItemStream stream) throws IOException {
		this.fileName = stream.getName();
		this.fieldName = stream.getFieldName();
		this.inputStream = stream.openStream();
		this.size = (long) this.inputStream.available();
		this.contentType = stream.getContentType();
		if (StringUtils.isNotBlank(fileName)) {
			int last = fileName.lastIndexOf(".");
			if (last > 0)
				this.extension = fileName.substring(last).toLowerCase();
			else
				this.extension = "";
		}
	}

	/**
	 * 将流保存到文件
	 * 
	 * @param dest
	 *            文件目录
	 * @return true/false
	 */
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

	public String getFieldName() {
		return fieldName;
	}

	public Long getSize() {
		return size;
	}

	public String getContentType() {
		return contentType;
	}

	public InputStream getInputStream() {
		return inputStream;
	}

	public String getFileName() {
		return fileName;
	}

	public String getExtension() {
		return extension;
	}

	public boolean isPic() {
		if (StringUtils.isEmpty(extension))
			return false;
		return ".jpg".equals(extension) || ".png".equals(extension) || ".bmp".equals(extension)
				|| ".gif".equals(extension) || ".jpeg".equals(extension);
	}

	public boolean isVedio() {
		if (StringUtils.isEmpty(extension))
			return false;
		return ".avi".equals(extension) || ".mp4".equals(extension) || ".wmv".equals(extension)
				|| ".flv".equals(extension) || ".mov".equals(extension);
	}

	public void clear() {
		this.fileName = null;
		this.extension = null;
		this.size = null;
		this.inputStream = null;
		this.fieldName = null;
		this.contentType = null;
	}

}

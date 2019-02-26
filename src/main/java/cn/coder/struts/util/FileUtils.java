package cn.coder.struts.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileUtils {
	private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);
	private static final int BYTE_LEN = 10240;

	public static boolean saveFile(File dest, InputStream input) {
		if (input == null)
			return false;
		try {
			if (!dest.getParentFile().exists())
				dest.getParentFile().mkdirs();
			FileOutputStream fos = new FileOutputStream(dest);
			byte[] buffer = new byte[BYTE_LEN];
			int n = 0;
			while ((n = input.read(buffer)) > 0) {
				fos.write(buffer, 0, n);
			}
			// 关闭输入流等（略）
			fos.close();
			return true;
		} catch (IOException e) {
			logger.error("Save file faild", e);
			return false;
		}
	}

}

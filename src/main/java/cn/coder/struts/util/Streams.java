package cn.coder.struts.util;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class Streams {
	private static final byte[] BYTE_BUFFER = new byte[8192];
	private static final char[] CHAR_BUFFER = new char[8192];

	public static String asString(InputStream inputStream) throws IOException {
		final char[] temp = CHAR_BUFFER.clone();
		InputStreamReader reader = new InputStreamReader(inputStream, "utf-8");
		int len;
		StringBuilder sb = new StringBuilder();
		while ((len = reader.read(temp)) > 0) {
			sb.append(new String(temp, 0, len));
		}
		reader.close();
		// 释放资源
		inputStream.close();
		return sb.toString();
	}

	public static long copy(InputStream in, OutputStream out, boolean closeOut) throws IOException {
		final byte[] temp = BYTE_BUFFER.clone();
		try {
			long total = 0;
			int res;
			while ((res = in.read(temp)) > 0) {
				total += res;
				if (out != null) {
					out.write(temp, 0, res);
				}
			}
			return total;
		} finally {
			close(in);
			if (closeOut) {
				close(out);
			} else {
				if (out != null) {
					out.flush();
					out = null;
				}
			}
		}
	}


	public static void close(Closeable closeable) {
		if (closeable != null) {
			try {
				closeable.close();
				closeable = null;
			} catch (IOException e) {
				// Nothing
			}
		}
	}

	public static String parseValue(String str, String name) {
		if (str == null || str.length() == 0)
			return null;
		String[] arr = str.split("; ");
		for (String p : arr) {
			int index = p.indexOf("=");
			if (index != -1) {
				String field = p.substring(0, index);
				if (name.equals(field)) {
					String val = p.substring(index + 1);
					if (val.startsWith("\""))
						val = val.substring(1);
					if (val.endsWith("\""))
						val = val.substring(0, val.length() - 1);
					return val;
				}
			}
		}
		return null;
	}
}

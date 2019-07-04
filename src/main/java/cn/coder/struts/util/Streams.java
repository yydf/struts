/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.coder.struts.util;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

/**
 * Utility class for working with streams.
 */
public final class Streams {

	/**
	 * Default buffer size for use in
	 * {@link #copy(InputStream, OutputStream, boolean)}.
	 */
	private static final int DEFAULT_BUFFER_SIZE = 8192;

	/**
	 * Copies the contents of the given {@link InputStream} to the given
	 * {@link OutputStream}. Shortcut for
	 * 
	 * <pre>
	 * copy(pInputStream, pOutputStream, new byte[8192]);
	 * </pre>
	 *
	 * @param inputStream
	 *            The input stream, which is being read. It is guaranteed, that
	 *            {@link InputStream#close()} is called on the stream.
	 * @param outputStream
	 *            The output stream, to which data should be written. May be
	 *            null, in which case the input streams contents are simply
	 *            discarded.
	 * @param closeOutputStream
	 *            True guarantees, that {@link OutputStream#close()} is called
	 *            on the stream. False indicates, that only
	 *            {@link OutputStream#flush()} should be called finally.
	 *
	 * @return Number of bytes, which have been copied.
	 * @throws IOException
	 *             An I/O error occurred.
	 */
	public static long copy(InputStream inputStream, OutputStream outputStream, boolean closeOutputStream)
			throws IOException {
		return copy(inputStream, outputStream, closeOutputStream, new byte[DEFAULT_BUFFER_SIZE]);
	}

	/**
	 * Copies the contents of the given {@link InputStream} to the given
	 * {@link OutputStream}.
	 *
	 * @param inputStream
	 *            The input stream, which is being read. It is guaranteed, that
	 *            {@link InputStream#close()} is called on the stream.
	 * @param outputStream
	 *            The output stream, to which data should be written. May be
	 *            null, in which case the input streams contents are simply
	 *            discarded.
	 * @param closeOutputStream
	 *            True guarantees, that {@link OutputStream#close()} is called
	 *            on the stream. False indicates, that only
	 *            {@link OutputStream#flush()} should be called finally.
	 * @param buffer
	 *            Temporary buffer, which is to be used for copying data.
	 * @return Number of bytes, which have been copied.
	 * @throws IOException
	 *             An I/O error occurred.
	 */
	public static long copy(InputStream inputStream, OutputStream outputStream, boolean closeOutputStream,
			byte[] buffer) throws IOException {
		OutputStream out = outputStream;
		InputStream in = inputStream;
		try {
			long total = 0;
			for (;;) {
				int res = in.read(buffer);
				if (res == -1) {
					break;
				}
				if (res > 0) {
					total += res;
					if (out != null) {
						out.write(buffer, 0, res);
					}
				}
			}
			if (out != null) {
				if (closeOutputStream) {
					out.close();
				} else {
					out.flush();
				}
				out = null;
			}
			in.close();
			in = null;
			return total;
		} finally {
			close(in);
			if (closeOutputStream) {
				close(out);
			}
		}
	}

	public static String asString(InputStream inputStream) throws IOException {
		final char[] temp = new char[DEFAULT_BUFFER_SIZE];
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

	public static void close(Closeable closeable) {
		if (closeable != null) {
			try {
				closeable.close();
			} catch (IOException e) {
				// Nothing
			}
		}
	}

}

package cn.coder.struts.wrapper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.coder.struts.event.FileUploadListener;
import cn.coder.struts.mvc.MultipartFile;
import cn.coder.struts.util.Streams;

public final class MultipartRequestWrapper {
	private static final Logger logger = LoggerFactory.getLogger(MultipartRequestWrapper.class);

	public static final String CONTENT_DISPOSITION = "Content-Disposition";
	public static final String FORM_DATA = "form-data";

	private final HashMap<String, String> paras = new HashMap<>();
	private final HashMap<String, MultipartFile> multipartFiles = new HashMap<>();

	public MultipartRequestWrapper(HttpServletRequest req, FileUploadListener upload) {
		wrapperRequest(req, (upload == null ? new DefaultFileUpload() : upload));
		if (logger.isDebugEnabled())
			logger.debug("Wrapper multipart request");
	}

	private void wrapperRequest(HttpServletRequest req, FileUploadListener upload) {
		try {
			final InputStream input = req.getInputStream();

			final byte[] boundary = getBoundary(req.getContentType());
			if (boundary == null) {
				Streams.close(input);
				throw new IOException("the request has no multipart boundary");
			}

			MultipartStream stream = new MultipartStream(input, boundary);
			boolean nextPart = stream.skipPreamble();
			while (nextPart) {
				Map<String, String> headers = getParsedHeaders(stream.readHeaders());
				if (getFileName(headers) == null)
					paras.put(getFieldName(headers), Streams.asString(stream.getInputStream()));
				else {
					MultipartFile file = new MultipartFile(headers, stream.getInputStream());
					if (file.getSize() > 0)
						paras.put(file.getFieldName(), upload.uploadMultipartFile(file));
					multipartFiles.put(file.getFieldName(), file);
				}
				nextPart = stream.readBoundary();
			}
		} catch (IOException e) {
			// a read or write error occurred
		}

	}

	private static byte[] getBoundary(String contentType) {
		String boundaryStr = Streams.parseValue(contentType, "boundary");
		if (boundaryStr != null)
			return boundaryStr.getBytes();
		return null;
	}

	/**
	 * <p>
	 * Parses the <code>header-part</code> and returns as key/value pairs.
	 *
	 * <p>
	 * If there are multiple headers of the same names, the name will map to a
	 * comma-separated list containing the values.
	 *
	 * @param headerPart
	 *            The <code>header-part</code> of the current
	 *            <code>encapsulation</code>.
	 *
	 * @return A <code>Map</code> containing the parsed HTTP request headers.
	 */
	private static Map<String, String> getParsedHeaders(String headerPart) {
		final int len = headerPart.length();
		Map<String, String> headers = new HashMap<>();
		int start = 0;
		for (;;) {
			int end = parseEndOfLine(headerPart, start);
			if (start == end) {
				break;
			}
			StringBuilder header = new StringBuilder(headerPart.substring(start, end));
			start = end + 2;
			while (start < len) {
				int nonWs = start;
				while (nonWs < len) {
					char c = headerPart.charAt(nonWs);
					if (c != ' ' && c != '\t') {
						break;
					}
					++nonWs;
				}
				if (nonWs == start) {
					break;
				}
				// Continuation line found
				end = parseEndOfLine(headerPart, nonWs);
				header.append(" ").append(headerPart.substring(nonWs, end));
				start = end + 2;
			}
			parseHeaderLine(headers, header.toString());
		}
		return headers;
	}

	/**
	 * Skips bytes until the end of the current line.
	 * 
	 * @param headerPart
	 *            The headers, which are being parsed.
	 * @param end
	 *            Index of the last byte, which has yet been processed.
	 * @return Index of the \r\n sequence, which indicates end of line.
	 */
	private static int parseEndOfLine(String headerPart, int end) {
		int index = end;
		for (;;) {
			int offset = headerPart.indexOf('\r', index);
			if (offset == -1 || offset + 1 >= headerPart.length()) {
				throw new IllegalStateException("Expected headers to be terminated by an empty line.");
			}
			if (headerPart.charAt(offset + 1) == '\n') {
				return offset;
			}
			index = offset + 1;
		}
	}

	/**
	 * Reads the next header line.
	 * 
	 * @param headers
	 *            String with all headers.
	 * @param header
	 *            Map where to store the current header.
	 */
	private static void parseHeaderLine(Map<String, String> headers, String header) {
		final int colonOffset = header.indexOf(':');
		if (colonOffset == -1) {
			// This header line is malformed, skip it.
			return;
		}
		String headerName = header.substring(0, colonOffset).trim();
		String headerValue = header.substring(header.indexOf(':') + 1).trim();
		headers.put(headerName, headerValue);
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

	public static final boolean isMultipartContent(HttpServletRequest request) {
		if (!"POST".equalsIgnoreCase(request.getMethod()))
			return false;
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

	public Iterator<MultipartFile> getMultipartFiles() {
		return multipartFiles.values().iterator();
	}

	public void clear() {
		paras.clear();
		for (MultipartFile file : multipartFiles.values()) {
			file.clear();
		}
		multipartFiles.clear();
	}

	private final class DefaultFileUpload implements FileUploadListener {

		@Override
		public String uploadMultipartFile(MultipartFile file) {
			String fileName = System.currentTimeMillis() + file.getExtension();
			file.transferTo(new File("/" + fileName));
			return fileName;
		}

	}
}

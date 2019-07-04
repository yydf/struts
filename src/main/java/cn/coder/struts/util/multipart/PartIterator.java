package cn.coder.struts.util.multipart;

import java.io.IOException;
import java.io.InputStream;
import java.util.NoSuchElementException;

import javax.servlet.http.HttpServletRequest;

import cn.coder.struts.util.Streams;

public final class PartIterator {
	/**
	 * HTTP content type header name.
	 */
	public static final String CONTENT_TYPE = "Content-type";

	/**
	 * HTTP content disposition header name.
	 */
	public static final String CONTENT_DISPOSITION = "Content-disposition";

	/**
	 * HTTP content length header name.
	 */
	public static final String CONTENT_LENGTH = "Content-length";

	/**
	 * Content-disposition value for form data.
	 */
	public static final String FORM_DATA = "form-data";

	/**
	 * Content-disposition value for file attachment.
	 */
	public static final String ATTACHMENT = "attachment";

	/**
	 * HTTP content type header for multiple uploads.
	 */
	public static final String MULTIPART_MIXED = "multipart/mixed";

	/**
	 * The multi part stream to process.
	 */
	private final MultipartStream multi;

	/**
	 * The boundary, which separates the various parts.
	 */
	private final byte[] boundary;

	/**
	 * The item, which we currently process.
	 */
	private FileItemStream currentItem;

	/**
	 * The current items field name.
	 */
	private String currentFieldName;

	/**
	 * Whether we are currently skipping the preamble.
	 */
	private boolean skipPreamble;

	/**
	 * Whether the current item may still be read.
	 */
	private boolean itemValid;

	/**
	 * Whether we have seen the end of the file.
	 */
	private boolean eof;

	/**
	 * Creates a new instance.
	 *
	 * @param ctx
	 *            The request context.
	 * @throws FileUploadException
	 *             An error occurred while parsing the request.
	 * @throws IOException
	 *             An I/O error occurred.
	 */
	public PartIterator(HttpServletRequest ctx) throws IOException {
		if (ctx == null) {
			throw new NullPointerException("ctx parameter");
		}

		String contentType = ctx.getContentType();
		if (contentType == null) {
			throw new NullPointerException("Multipart request Content-Type attribute cannot be null");
		}

		InputStream input = ctx.getInputStream();

		boundary = getBoundary(contentType);
		if (boundary == null) {
			Streams.close(input); // avoid possible resource leak
			throw new IOException("the request was rejected because no multipart boundary was found");
		}

		String encoding = ctx.getCharacterEncoding();
		
		try {
			multi = new MultipartStream(input, boundary);
		} catch (IllegalArgumentException iae) {
			Streams.close(input); // avoid possible resource leak
			throw new IOException(String.format("The boundary specified in the %s header is too long", CONTENT_TYPE),
					iae);
		}
		multi.setHeaderEncoding(encoding);

		skipPreamble = true;
		findNextItem();
	}

	/**
	 * Retrieves the boundary from the <code>Content-type</code> header.
	 *
	 * @param contentType
	 *            The value of the content type header from which to extract the
	 *            boundary value.
	 *
	 * @return The boundary, as a byte array.
	 */
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
	private static PartHeaders getParsedHeaders(String headerPart) {
		final int len = headerPart.length();
		PartHeaders headers = new PartHeaders();
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
	private static void parseHeaderLine(PartHeaders headers, String header) {
		final int colonOffset = header.indexOf(':');
		if (colonOffset == -1) {
			// This header line is malformed, skip it.
			return;
		}
		String headerName = header.substring(0, colonOffset).trim();
		String headerValue = header.substring(header.indexOf(':') + 1).trim();
		headers.addHeader(headerName, headerValue);
	}

	/**
	 * Retrieves the file name from the <code>Content-disposition</code> header.
	 *
	 * @param headers
	 *            The HTTP headers object.
	 *
	 * @return The file name for the current <code>encapsulation</code>.
	 */
	protected static String getFileName(PartHeaders headers) {
		return getFileName(headers.getHeader(CONTENT_DISPOSITION));
	}

	/**
	 * Returns the given content-disposition headers file name.
	 * 
	 * @param pContentDisposition
	 *            The content-disposition headers value.
	 * @return The file name
	 */
	private static String getFileName(String pContentDisposition) {
		if (pContentDisposition != null)
			return Streams.parseValue(pContentDisposition, "filename");
		return null;
	}

	/**
	 * Retrieves the field name from the <code>Content-disposition</code>
	 * header.
	 *
	 * @param headers
	 *            A <code>Map</code> containing the HTTP request headers.
	 *
	 * @return The field name for the current <code>encapsulation</code>.
	 */
	protected static String getFieldName(PartHeaders headers) {
		return getFieldName(headers.getHeader(CONTENT_DISPOSITION));
	}

	/**
	 * Returns the field name, which is given by the content-disposition header.
	 * 
	 * @param pContentDisposition
	 *            The content-dispositions header value.
	 * @return The field jake
	 */
	private static String getFieldName(String pContentDisposition) {
		if (pContentDisposition != null && pContentDisposition.toLowerCase().startsWith(FORM_DATA))
			return Streams.parseValue(pContentDisposition, "name");
		return null;
	}

	private static long getContentLength(PartHeaders pHeaders) {
		try {
			return Long.parseLong(pHeaders.getHeader(CONTENT_LENGTH));
		} catch (Exception e) {
			return -1;
		}
	}

	/**
	 * Called for finding the next item, if any.
	 *
	 * @return True, if an next item was found, otherwise false.
	 * @throws IOException
	 *             An I/O error occurred.
	 */
	private boolean findNextItem() throws IOException {
		if (eof) {
			return false;
		}
		if (currentItem != null) {
			currentItem.close();
			currentItem = null;
		}
		for (;;) {
			boolean nextPart;
			if (skipPreamble) {
				nextPart = multi.skipPreamble();
			} else {
				nextPart = multi.readBoundary();
			}
			if (!nextPart) {
				if (currentFieldName == null) {
					// Outer multipart terminated -> No more data
					eof = true;
					return false;
				}
				// Inner multipart terminated -> Return to parsing the outer
				multi.setBoundary(boundary);
				currentFieldName = null;
				continue;
			}
			PartHeaders headers = getParsedHeaders(multi.readHeaders());
			if (currentFieldName == null) {
				// We're parsing the outer multipart
				String fieldName = getFieldName(headers);
				if (fieldName != null) {
					String subContentType = headers.getHeader(CONTENT_TYPE);
					if (subContentType != null && subContentType.toLowerCase().startsWith(MULTIPART_MIXED)) {
						currentFieldName = fieldName;
						// Multiple files associated with this field name
						byte[] subBoundary = getBoundary(subContentType);
						multi.setBoundary(subBoundary);
						skipPreamble = true;
						continue;
					}
					String fileName = getFileName(headers);
					currentItem = new FileItemStream(multi, fileName, fieldName, headers.getHeader(CONTENT_TYPE),
							fileName == null, getContentLength(headers));
					currentItem.setHeaders(headers);
					itemValid = true;
					return true;
				}
			} else {
				String fileName = getFileName(headers);
				if (fileName != null) {
					currentItem = new FileItemStream(multi, fileName, currentFieldName, headers.getHeader(CONTENT_TYPE),
							false, getContentLength(headers));
					currentItem.setHeaders(headers);
					itemValid = true;
					return true;
				}
			}
			multi.discardBodyData();
		}
	}

	/**
	 * Returns, whether another instance of {@link FileItemStream} is available.
	 *
	 * @throws FileUploadException
	 *             Parsing or processing the file item failed.
	 * @throws IOException
	 *             Reading the file item failed.
	 * @return True, if one or more additional file items are available,
	 *         otherwise false.
	 */
	public boolean hasNext() throws IOException {
		if (eof) {
			return false;
		}
		if (itemValid) {
			return true;
		}
		try {
			return findNextItem();
		} catch (IOException e) {
			// unwrap encapsulated SizeException
			throw e;
		}
	}

	/**
	 * Returns the next available {@link FileItemStream}.
	 *
	 * @throws java.util.NoSuchElementException
	 *             No more items are available. Use {@link #hasNext()} to
	 *             prevent this exception.
	 * @throws FileUploadException
	 *             Parsing or processing the file item failed.
	 * @throws IOException
	 *             Reading the file item failed.
	 * @return FileItemStream instance, which provides access to the next file
	 *         item.
	 */
	public FileItemStream next() throws IOException {
		if (eof || (!itemValid && !hasNext())) {
			throw new NoSuchElementException();
		}
		itemValid = false;
		return currentItem;
	}

}

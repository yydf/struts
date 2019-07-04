package cn.coder.struts.util.multipart;

import java.io.IOException;
import java.io.InputStream;

import cn.coder.struts.util.Streams;
import cn.coder.struts.util.multipart.MultipartStream.ItemInputStream;

public final class FileItemStream {
	/**
	 * The file items content type.
	 */
	private final String contentType;

	/**
	 * The file items field name.
	 */
	private final String fieldName;

	/**
	 * The file items input stream.
	 */
	private final InputStream stream;

	/**
	 * The file items file name.
	 */
	private final String name;

	/**
	 * Whether the file item is a form field.
	 */
	private final boolean formField;

	/**
	 * The headers, if any.
	 */
	private PartHeaders headers;

	/**
	 * Creates a new instance.
	 * 
	 * @param multi
	 *
	 * @param pName
	 *            The items file name, or null.
	 * @param pFieldName
	 *            The items field name.
	 * @param pContentType
	 *            The items content type, or null.
	 * @param pFormField
	 *            Whether the item is a form field.
	 * @param pContentLength
	 *            The items content length, if known, or -1
	 * @throws IOException
	 *             Creating the file item failed.
	 */
	public FileItemStream(MultipartStream multi, String pName, String pFieldName, String pContentType,
			boolean pFormField, long pContentLength) throws IOException {
		name = pName;
		fieldName = pFieldName;
		contentType = pContentType;
		formField = pFormField;
		stream = multi.newInputStream();
	}

	/**
	 * Returns the items file name.
	 *
	 * @return File name, if known, or null.
	 * @throws InvalidFileNameException
	 *             The file name contains a NUL character, which might be an
	 *             indicator of a security attack. If you intend to use the file
	 *             name anyways, catch the exception and use
	 *             InvalidFileNameException#getName().
	 */
	public String getName() {
		return Streams.checkFileName(name);
	}

	/**
	 * Sets the file item headers.
	 *
	 * @param pHeaders
	 *            The items header object
	 */
	public void setHeaders(PartHeaders pHeaders) {
		headers = pHeaders;
	}

	/**
	 * Returns the file item headers.
	 *
	 * @return The items header object
	 */
	public PartHeaders getHeaders() {
		return headers;
	}

	/**
	 * Returns the items content type, or null.
	 *
	 * @return Content type, if known, or null.
	 */
	public String getContentType() {
		return contentType;
	}

	/**
	 * Returns the items field name.
	 *
	 * @return Field name.
	 */
	public String getFieldName() {
		return fieldName;
	}

	/**
	 * Returns, whether this is a form field.
	 *
	 * @return True, if the item is a form field, otherwise false.
	 */
	public boolean isFormField() {
		return formField;
	}

	/**
	 * Returns an input stream, which may be used to read the items contents.
	 *
	 * @return Opened input stream.
	 * @throws IOException
	 *             An I/O error occurred.
	 */
	public InputStream openStream() throws IOException {
		if (((ItemInputStream)stream).isClosed()) {
			throw new IOException("Stream has been closed");
		}
		return stream;
	}

	/**
	 * Closes the file item.
	 *
	 * @throws IOException
	 *             An I/O error occurred.
	 */
	void close() throws IOException {
		stream.close();
	}
}

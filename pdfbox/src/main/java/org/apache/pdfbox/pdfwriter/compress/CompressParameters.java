package org.apache.pdfbox.pdfwriter.compress;

/**
 * An instance of this class centralizes and provides the configuration for a PDF compression.
 * @author Christian Appl
 */
public class CompressParameters {

	public static final int DEFAULT_OBJECT_STREAM_SIZE = 200;

	private int objectStreamSize = DEFAULT_OBJECT_STREAM_SIZE;

	/**
	 * Sets the number of objects, that can be contained in compressed object streams. Higher object stream sizes may
	 * cause PDF readers to slow down during the rendering of PDF documents, therefore a reasonable value should be
	 * selected.
	 *
	 * @param objectStreamSize The number of objects, that can be contained in compressed object streams.
	 * @return The current instance, to allow method chaining.
	 */
	public CompressParameters setObjectStreamSize(int objectStreamSize) {
		this.objectStreamSize = objectStreamSize <= 0 ? DEFAULT_OBJECT_STREAM_SIZE : objectStreamSize;
		return this;
	}

	/**
	 * Returns the number of objects, that can be contained in compressed object streams. Higher object stream sizes may
	 * cause PDF readers to slow down during the rendering of PDF documents, therefore a reasonable value should be
	 * selected.
	 *
	 * @return The number of objects, that can be contained in compressed object streams.
	 */
	public int getObjectStreamSize() {
		return objectStreamSize;
	}

}

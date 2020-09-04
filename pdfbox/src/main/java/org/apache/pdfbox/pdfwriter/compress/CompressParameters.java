package org.apache.pdfbox.pdfwriter.compress;

import org.apache.pdfbox.pdfwriter.compress.content.ContentCompressor;
import org.apache.pdfbox.pdfwriter.compress.content.ImageCompressor;
import org.apache.pdfbox.pdfwriter.compress.content.UnencodedStreamCompressor;

import java.util.ArrayList;
import java.util.List;

/**
 * An instance of this class centralizes and provides the configuration for a PDF compression.
 * @author Christian Appl
 */
public class CompressParameters {

	public static final int DEFAULT_OBJECT_STREAM_SIZE = 200;

	private int objectStreamSize = DEFAULT_OBJECT_STREAM_SIZE;
	private boolean compressImages = false;
	private int imageQuality = 100;
	private boolean compressUncompressedStreams = true;

	/**
	 * The current list of {@link ContentCompressor}s, that result from the chosen parameters.
	 */
	private List<ContentCompressor> compressors;

	/**
	 * Set to true, to activate the image compression. This will search for simple images in the PDF document, that can
	 * be compressed, by applying a DCT - filter (converting them to jpeg images). Use
	 * {@link CompressParameters#setImageQuality(int)} to configure the quality of the resulting jpegs.
	 *
	 * @param compressImages True to activate the image compression.
	 * @return The current instance, to allow method chaining.
	 */
	public CompressParameters setCompressImages(boolean compressImages) {
		this.compressImages = compressImages;
		resetCompressors();
		return this;
	}

	/**
	 * Returns true, if the image compression has been activated. This will search for simple images in the PDF document,
	 * that can be compressed, by applying a DCT - filter (converting them to jpeg images).
	 *
	 * @return True, if the image compression has been activated.
	 */
	public boolean isCompressImages() {
		return compressImages;
	}

	/**
	 * Set the jpeg quality for the image compression, when applying a DCT - filter. A lower quality will result in a
	 * higher compression rate. (Calling this method, will automatically activate the image compression - see
	 * {@link CompressParameters#setCompressImages(boolean)} for further information.)
	 *
	 * @param imageQuality A percentage (a value in between 0 and 100) for the jpeg quality of compressed images.
	 * @return The current instance, to allow method chaining.
	 */
	public CompressParameters setImageQuality(int imageQuality) {
		this.imageQuality = Math.max(Math.min(100, imageQuality), 0);
		setCompressImages(true);
		resetCompressors();
		return this;
	}

	/**
	 * Returns the jpeg quality for the image compression, when applying a DCT - filter.
	 *
	 * @return A percentage (a value in between 0 and 100) for the jpeg quality of compressed images.
	 */
	public int getImageQuality() {
		return imageQuality;
	}

	/**
	 * Set to true, to activate the compression of uncompressed streams. This will search for uncompressed streams in
	 * the PDF document, that can be compressed, by applying a Flate - filter.
	 *
	 * @param compressUncompressedStreams True to activate the compression of uncompressed streams.
	 * @return The current instance, to allow method chaining.
	 */
	public CompressParameters setCompressUncompressedStreams(boolean compressUncompressedStreams) {
		this.compressUncompressedStreams = compressUncompressedStreams;
		resetCompressors();
		return this;
	}

	/**
	 * Returns true, if the compression of uncompressed streams has been activated. This will search for uncompressed
	 * streams in the PDF document, that can be compressed, by applying a Flate - filter.
	 *
	 * @return True, if the compression of uncompressed streams has been activated.
	 */
	public boolean isCompressUncompressedStreams() {
		return compressUncompressedStreams;
	}

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
		resetCompressors();
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

	/**
	 * Resets the currently prepared {@link ContentCompressor}s - this should be called, whenever an option of this
	 * configuration container is changed, otherwise the altered parameter may not affect the compression.
	 */
	private void resetCompressors() {
		this.compressors = null;
	}

	/**
	 * Returns a collection of {@link ContentCompressor}s, that reflect the parameters, that have been selected for this
	 * configuration container. Those compressors shall be used to compress specific contents of the document, that
	 * shall be compressed.
	 *
	 * @return A a collection of {@link ContentCompressor}s, that shall be used, to compress contents of a PDF document.
	 */
	public List<ContentCompressor> getContentCompressors() {
		if (this.compressors != null) {
			return this.compressors;
		}

		this.compressors = new ArrayList<ContentCompressor>();
		if (isCompressUncompressedStreams()) {
			this.compressors.add(new UnencodedStreamCompressor());
		}
		if (isCompressImages()) {
			this.compressors.add(new ImageCompressor(getImageQuality()));
		}

		return this.compressors;
	}
}

package org.apache.pdfbox.pdfwriter.compress.content;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.pdfwriter.compress.TraversedCOSElement;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.IOException;

/**
 * An extending class shall provide the means to filter for {@link COSObject}s, that can be compressed by the
 * implementing content compressor and to apply the compression to the found objects.
 *
 * @author Christian Appl
 */
public abstract class AbstractContentCompressor implements ContentCompressor {

	/**
	 * Attempts to compress the given {@link COSObject} for the given {@link PDDocument}, will do nothing, if such an
	 * object can not be compressed by this content compressor.
	 *
	 * @param document The document a {@link COSObject} shall be compressed for.
	 * @param currentState     The current state of the object, that shall be compressed.
	 * @param traversedElement The traversed COSentry.
	 * @return The resulting object.
	 * @throws IOException Shall be thrown, if an error occurred during compression.
	 */
	@Override
	public COSBase compress(PDDocument document, COSBase currentState, TraversedCOSElement traversedElement)
			throws IOException {
		if (isCompressible(currentState, traversedElement)) {
			return doCompress(document, currentState, traversedElement);
		}
		return currentState;
	}

	/**
	 * Actually compress the object, without further checks for validity. Whether an object can be compressed shall
	 * be determined by {@link ContentCompressor#isCompressible(COSBase, TraversedCOSElement)}, which is automatically
	 * executed prior to calls to this method.
	 *
	 * @param document The document a {@link COSBase} shall be compressed for.
	 * @param currentState     The current state of the object, that shall be compressed.
	 * @param traversedElement The traversed COSentry.
	 * @return The resulting object.
	 * @throws IOException Shall be thrown, if an error occurred during compression.
	 */
	protected abstract COSBase doCompress(PDDocument document, COSBase currentState, TraversedCOSElement traversedElement)
			throws IOException;
}

package org.apache.pdfbox.pdfwriter.compress.content;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.pdfwriter.compress.TraversedCOSElement;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.IOException;

/**
 * An implementing class shall provide the means to filter for {@link COSObject}s, that can be compressed by the
 * implementing content compressor and to apply the compression to the found objects.
 *
 * @author Christian Appl
 */
public interface ContentCompressor {

	/**
	 * Checks whether the given {@link COSBase} can be a compressed by this content compressor.
	 *
	 * @param currentState     The current state of the object, that shall be compressed.
	 * @param traversedElement The traversed COSentry.
	 * @return True, if the object can be compressed.
	 */
	boolean isCompressible(COSBase currentState, TraversedCOSElement traversedElement);

	/**
	 * Attempts to compress the given {@link COSBase} for the given {@link PDDocument}, will do nothing, if such an
	 * object can not be compressed by this content compressor.
	 *
	 * @param document         The document a {@link COSBase} shall be compressed for.
	 * @param currentState     The current state of the object, that shall be compressed.
	 * @param traversedElement The traversed COSentry.
	 * @return The resulting object.
	 * @throws IOException Shall be thrown, if an error occurred during compression.
	 */
	COSBase compress(PDDocument document, COSBase currentState, TraversedCOSElement traversedElement) throws IOException;
}

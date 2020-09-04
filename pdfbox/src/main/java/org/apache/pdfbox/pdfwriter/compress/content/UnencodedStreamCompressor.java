package org.apache.pdfbox.pdfwriter.compress.content;

import org.apache.pdfbox.cos.*;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdfwriter.compress.TraversedCOSElement;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * An instance of this class shall provide the means to filter for uncompressed {@link COSStream}s, that can be
 * compressed by applying a Flate - Filter.
 * This compressor shall deny compressing already compressed streams.
 * @author Christian Appl
 */
public class UnencodedStreamCompressor extends AbstractContentCompressor {

	/**
	 * Shall return false for all objects, that are not valid uncompressed {@link COSStream}s.
	 *
	 * @param currentState     The current state of the object, that shall be compressed.
	 * @param traversedElement The traversed COSentry.
	 * @return True, if the object is a compressible {@link COSStream}.
	 */
	@Override
	public boolean isCompressible(COSBase currentState, TraversedCOSElement traversedElement) {
		COSBase base = currentState instanceof COSObject ? ((COSObject)currentState).getObject() : currentState;
		if (!(base instanceof COSStream)) {
			return false;
		}
		COSStream stream = (COSStream) base;
		return (stream.getFilters() == null ||
				(stream.getFilters() instanceof COSArray && ((COSArray) stream.getFilters()).size() == 0)) &&
				!stream.containsKey(COSName.F);
	}

	/**
	 * The object can be assumed, to contain a compressible {@link COSStream}. A Flate compression shall be applied
	 * to the stream data, without further checks for validity.
	 *
	 * @param document The document a {@link COSBase} shall be compressed for.
	 * @param currentState     The current state of the object, that shall be compressed.
	 * @param traversedElement The traversed COSentry.
	 * @return The resulting object.
	 * @throws IOException Shall be thrown, if an error occurred during compression.
	 */
	@Override
	protected COSBase doCompress(PDDocument document, COSBase currentState, TraversedCOSElement traversedElement)
			throws IOException {
		COSBase base = currentState instanceof COSObject ? ((COSObject)currentState).getObject() : currentState;
		COSStream stream = (COSStream) base;
		COSStream encodedStream = new COSStream();
		InputStream inputStream = null;
		OutputStream outputStream = null;
		try {
			inputStream = stream.createInputStream();
			outputStream = encodedStream.createOutputStream(COSName.FLATE_DECODE);
			IOUtils.copy(inputStream, outputStream);
		} finally {
			if (inputStream != null) {
				inputStream.close();
			}
			if (outputStream != null) {
				outputStream.close();
			}
		}
		for (COSName key : stream.keySet()) {
			if (Arrays.asList(
					COSName.FILTER, COSName.LENGTH, COSName.DECODE_PARMS
			).contains(key)) {
				continue;
			}
			encodedStream.setItem(key, stream.getItem(key));
		}

		if (currentState instanceof COSObject) {
			COSObject cosObject = (COSObject) currentState;
			cosObject.setObject(encodedStream);
			return cosObject;
		}
		return encodedStream;
	}
}

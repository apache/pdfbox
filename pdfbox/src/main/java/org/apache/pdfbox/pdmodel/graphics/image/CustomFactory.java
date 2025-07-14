package org.apache.pdfbox.pdmodel.graphics.image;

import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;

/**
 * A functional interface that allows users to define a custom strategy for converting
 * image data as a byte array into a {@link PDImageXObject}.
 */
@FunctionalInterface
public interface CustomFactory {
	/**
	 * Creates a {@link PDImageXObject} from the given image byte array and document context.
	 *
	 * @param document the document that shall use this PDImageXObject.
	 * @param byteArray the image data as a byte array
	 * @return a PDImageXObject.
	 * @throws IOException if there is an error when creating the PDImageXObject.
	 */
	PDImageXObject createFromByteArray(PDDocument document, byte[] byteArray) throws IOException;
}

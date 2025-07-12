package org.apache.pdfbox.pdmodel.graphics.image;

import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;

@FunctionalInterface
public interface DefaultFactory {
	PDImageXObject createFromByteArray(PDDocument document, byte[] byteArray) throws IOException;
}

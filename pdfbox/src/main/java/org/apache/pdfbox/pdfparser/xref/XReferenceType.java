package org.apache.pdfbox.pdfparser.xref;

import org.apache.pdfbox.pdfparser.PDFXRefStream;

/**
 * An instance of this class represents a type for a {@link XReferenceEntry}, as it can be found in a PDF's
 * {@link PDFXRefStream}.
 * @author Christian Appl
 */
public enum XReferenceType {

	FREE(0),
	NORMAL(1),
	OBJECT_STREAM_ENTRY(2);

	private final int numericValue;

	/**
	 * Represents a type for a {@link XReferenceEntry}, as it can be found in a PDF's {@link PDFXRefStream}.
	 *
	 * @param numericValue The numeric representation of this type.
	 */
	XReferenceType(int numericValue) {
		this.numericValue = numericValue;
	}

	/**
	 * Returns the numeric representation of this type.
	 *
	 * @return The numeric representation of this type.
	 */
	public int getNumericValue() {
		return numericValue;
	}
}

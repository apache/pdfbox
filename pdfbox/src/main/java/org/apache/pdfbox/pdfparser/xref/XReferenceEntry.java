package org.apache.pdfbox.pdfparser.xref;

import org.apache.pdfbox.cos.COSObjectKey;
import org.apache.pdfbox.pdfparser.PDFXRefStream;

/**
 * An implementing class represents an entry, as it can be found in a PDF's crossreference stream
 * ({@link PDFXRefStream}). Such an entry shall locate a PDF object/resource in a PDF document.
 * @author Christian Appl
 */
public interface XReferenceEntry extends Comparable<XReferenceEntry> {

	/**
	 * Returns the {@link XReferenceType} of this crossreference stream entry.
	 *
	 * @return The {@link XReferenceType} of this crossreference stream entry.
	 */
	XReferenceType getType();

	/**
	 * Returns the {@link COSObjectKey} of the object, that is described by this crossreference stream entry.
	 *
	 * @return The {@link COSObjectKey} of the object, that is described by this crossreference stream entry.
	 */
	COSObjectKey getReferencedKey();

	/**
	 * Returns the value for the first column of the crossreference stream entry.
	 * (The numeric representation of this entry's {@link XReferenceType}.)
	 *
	 * @return The value for the first column of the crossreference stream entry.
	 */
	long getFirstColumnValue();

	/**
	 * Returns the value for the second column of the crossreference stream entry. (It's meaning depends on the
	 * {@link XReferenceType} of this entry.)
	 *
	 * @return The value for the second column of the crossreference stream entry.
	 */
	long getSecondColumnValue();

	/**
	 * Returns the value for the third column of the crossreference stream entry. (It's meaning depends on the
	 * {@link XReferenceType} of this entry.)
	 *
	 * @return The value for the third column of the crossreference stream entry.
	 */
	long getThirdColumnValue();
}

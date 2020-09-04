package org.apache.pdfbox.pdfparser.xref;

import org.apache.pdfbox.cos.COSObjectKey;
import org.apache.pdfbox.pdfparser.PDFXRefStream;

/**
 * A class representing a free reference in a PDF's crossreference stream ({@link PDFXRefStream}).
 * @author Christian Appl
 */
public class FreeXReference extends AbstractXReference {

	public static final FreeXReference NULL_ENTRY = new FreeXReference(new COSObjectKey(0, 65535));
	private final COSObjectKey nextFreeKey;

	/**
	 * Sets the given {@link COSObjectKey} as a free reference in a PDF's crossreference stream ({@link PDFXRefStream}).
	 *
	 * @param nextFreeKey The key, that shall be set as the free reference of the document.
	 */
	public FreeXReference(COSObjectKey nextFreeKey) {
		super(XReferenceType.FREE);
		this.nextFreeKey = nextFreeKey;
	}

	/**
	 * Returns the {@link COSObjectKey} of the object, that is described by this crossreference stream entry.
	 *
	 * @return The {@link COSObjectKey} of the object, that is described by this crossreference stream entry.
	 */
	@Override
	public COSObjectKey getReferencedKey() {
		return nextFreeKey;
	}

	/**
	 * Returns the value for the second column of the crossreference stream entry. (This is the object number of the set
	 * next free {@link COSObjectKey} - for entries of this type.)
	 *
	 * @return The value for the second column of the crossreference stream entry.
	 */
	@Override
	public long getSecondColumnValue() {
		return getReferencedKey().getNumber();
	}

	/**
	 * Returns the value for the third column of the crossreference stream entry. (This is the generation number of the set
	 * next free {@link COSObjectKey} - for entries of this type.)
	 *
	 * @return The value for the third column of the crossreference stream entry.
	 */
	@Override
	public long getThirdColumnValue() {
		return getReferencedKey().getGeneration();
	}

	/**
	 * Returns a string representation of this crossreference stream entry.
	 *
	 * @return A string representation of this crossreference stream entry.
	 */
	@Override
	public String toString() {
		return "FreeReference{" +
				"nextFreeKey=" + nextFreeKey +
				", type=" + getType().getNumericValue() +
				" }";
	}
}

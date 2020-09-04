package org.apache.pdfbox.pdfparser.xref;

import org.apache.pdfbox.cos.*;
import org.apache.pdfbox.pdfparser.PDFXRefStream;

/**
 * A class representing a normal reference in a PDF's crossreference stream ({@link PDFXRefStream}).
 * @author Christian Appl
 */
public class NormalXReference extends AbstractXReference {

	private final long byteOffset;
	private final COSObjectKey key;
	private final COSBase object;
	private final boolean objectStream;

	/**
	 * Prepares a normal reference for the given {@link COSObject} in a PDF's crossreference stream ({@link PDFXRefStream}).
	 *
	 * @param byteOffset The byte offset of the {@link COSObject} in the PDF file.
	 * @param key        The {@link COSObjectKey}, that is represented by this entry.
	 * @param object     The {@link COSObject}, that is represented by this entry.
	 */
	public NormalXReference(long byteOffset, COSObjectKey key, COSBase object) {
		super(XReferenceType.NORMAL);
		this.byteOffset = byteOffset;
		this.key = key;
		this.object = object;
		COSBase base = object;
		if (base != null && (base instanceof COSStream ||
				(base instanceof COSObject && (base = ((COSObject) base).getObject()) instanceof COSStream))) {
			this.objectStream = COSName.OBJ_STM.equals(((COSStream) base).getCOSName(COSName.TYPE));
		} else {
			this.objectStream = false;
		}
	}

	/**
	 * Returns the byte offset of the {@link COSObject} in the PDF file.
	 *
	 * @return The byte offset of the {@link COSObject} in the PDF file.
	 */
	public long getByteOffset() {
		return byteOffset;
	}

	/**
	 * Returns the {@link COSObjectKey} of the object, that is described by this crossreference stream entry.
	 *
	 * @return The {@link COSObjectKey} of the object, that is described by this crossreference stream entry.
	 */
	@Override
	public COSObjectKey getReferencedKey() {
		return key;
	}

	/**
	 * Returns the {@link COSObject}, that is described by this crossreference stream entry.
	 *
	 * @return The {@link COSObject}, that is described by this crossreference stream entry.
	 */
	public COSBase getObject() {
		return object;
	}

	/**
	 * Returns true, if the referenced object is an object stream.
	 *
	 * @return True, if the referenced object is an object stream.
	 */
	public boolean isObjectStream() {
		return objectStream;
	}

	/**
	 * Returns the value for the second column of the crossreference stream entry. (This is byte offset of the
	 * {@link COSObject} in the PDF file - for entries of this type.)
	 *
	 * @return The value for the second column of the crossreference stream entry.
	 */
	@Override
	public long getSecondColumnValue() {
		return getByteOffset();
	}

	/**
	 * Returns the value for the third column of the crossreference stream entry. (This is the generation number of the
	 * set {@link COSObjectKey} - for entries of this type.)
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
		return (isObjectStream() ? "ObjectStreamParent{" : "NormalReference{") +
				" key=" + key +
				", type=" + getType().getNumericValue() +
				", byteOffset=" + byteOffset +
				" }";
	}
}

package org.apache.pdfbox.pdfwriter.compress;

import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.contentstream.operator.OperatorName;
import org.apache.pdfbox.cos.*;
import org.apache.pdfbox.pdfparser.PDFXRefStream;
import org.apache.pdfbox.pdfwriter.COSWriter;
import org.apache.pdfbox.util.Charsets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * An instance of this class represents an object stream, that compresses a number of {@link COSObject}s in a stream.
 * It may be added to the top level container of a written PDF document in place of the compressed objects. The
 * document's {@link PDFXRefStream} must be adapted accordingly.
 *
 * @author Christian Appl
 */
public class COSWriterObjectStream extends COSStream {

	private final COSWriterCompressionPool compressionPool;
	private final List<COSObjectKey> preparedKeys = new ArrayList<COSObjectKey>();
	private final List<COSBase> preparedObjects = new ArrayList<COSBase>();

	/**
	 * Creates an object stream for compressible objects from the given {@link COSWriterCompressionPool}. The objects
	 * must first be prepared for this object stream, by adding them via calling
	 * {@link COSWriterObjectStream#prepareStreamObject(COSObjectKey, COSBase)} and will be written to this
	 * {@link COSStream}, when {@link COSWriterObjectStream#update()} is called.
	 *
	 * @param compressionPool The compression pool an object stream shall be created for.
	 */
	public COSWriterObjectStream(COSWriterCompressionPool compressionPool) {
		this.compressionPool = compressionPool;
		setItem(COSName.TYPE, COSName.OBJ_STM);
	}

	/**
	 * Returns the number of objects, that have been written to this object stream. ({@link COSName#N})
	 *
	 * @return The number of objects, that have been written to this object stream.
	 */
	public int getObjectCount() {
		return getInt(COSName.N, 0);
	}

	/**
	 * Sets the number of objects, that have been written to this object stream. ({@link COSName#N})
	 *
	 * @param size The number of objects, that have been written to this object stream.
	 */
	public void setObjectCount(int size) {
		setInt(COSName.N, size);
	}

	/**
	 * Returns the byte offset of the first object contained in this object stream. ({@link COSName#FIRST})
	 *
	 * @return The byte offset of the first object contained in this object stream.
	 */
	public int getFirstEntryOffset() {
		return getInt(COSName.FIRST, 0);
	}

	/**
	 * Sets the byte offset of the first object contained in this object stream. ({@link COSName#FIRST})
	 *
	 * @param firstEntryOffset The byte offset of the first object contained in this object stream.
	 */
	public void setFirstEntryOffset(int firstEntryOffset) {
		setInt(COSName.FIRST, firstEntryOffset);
	}

	/**
	 * Prepares the given {@link COSObject} to be written to this object stream, using the given {@link COSObjectKey} as
	 * it's ID for indirect references.
	 *
	 * @param key    The {@link COSObjectKey}, that shall be used for indirect references to the {@link COSObject}.
	 * @param object The {@link COSObject}, that shall be written to this object stream.
	 */
	public void prepareStreamObject(COSObjectKey key, COSBase object) {
		if (key != null && object != null) {
			this.preparedKeys.add(key);
			this.preparedObjects.add(object instanceof COSObject ? ((COSObject) object).getObject() : object);
		}
	}

	/**
	 * Returns all {@link COSObjectKey}s, that shall be added to the object stream, when
	 * {@link COSWriterObjectStream#update()} is called.
	 *
	 * @return All {@link COSObjectKey}s, that shall be added to the object stream.
	 */
	public List<COSObjectKey> getPreparedKeys() {
		return preparedKeys;
	}

	/**
	 * Returns all {@link COSObject}s, that shall be added to the object stream, when
	 * {@link COSWriterObjectStream#update()} is called.
	 *
	 * @return All {@link COSObject}s, that shall be added to the object stream.
	 */
	public List<COSBase> getPreparedObjects() {
		return preparedObjects;
	}

	/**
	 * Updates the underlying {@link COSStream} by writing all prepared {@link COSObject}s to this object stream.
	 *
	 * @return The underlying {@link COSStream} dictionary of this object stream.
	 * @throws IOException Shall be thrown, if writing the object stream failed.
	 */
	public COSStream update() throws IOException {
		setObjectCount(preparedKeys.size());

		// Prepare the compressible objects for writing.
		List<Long> objectNumbers = new ArrayList<Long>();
		List<byte[]> objectsBuffer = new ArrayList<byte[]>();
		for (int i = 0; i < getObjectCount(); i++) {
			ByteArrayOutputStream partialOutput = null;
			try {
				partialOutput = new ByteArrayOutputStream();
				objectNumbers.add(preparedKeys.get(i).getNumber());
				COSBase base = preparedObjects.get(i);
				writeObject(partialOutput, base, true);
				objectsBuffer.add(partialOutput.toByteArray());
			} finally {
				if (partialOutput != null) {
					partialOutput.close();
				}
			}
		}

		// Deduce the object stream byte offset map.
		byte[] offsetsMapBuffer;
		ByteArrayOutputStream partialOutput = null;
		long nextObjectOffset = 0;
		try {
			partialOutput = new ByteArrayOutputStream();
			for (int i = 0; i < objectNumbers.size(); i++) {
				partialOutput.write(String.valueOf(objectNumbers.get(i)).getBytes(Charsets.ISO_8859_1));
				partialOutput.write(COSWriter.SPACE);

				partialOutput.write(String.valueOf(nextObjectOffset).getBytes(Charsets.ISO_8859_1));
				partialOutput.write(COSWriter.SPACE);

				nextObjectOffset += objectsBuffer.get(i).length;
			}
			offsetsMapBuffer = partialOutput.toByteArray();
		} finally {
			if (partialOutput != null) {
				partialOutput.close();
			}
		}

		// Write Flate compressed object stream data.
		OutputStream output = null;
		try {
			output = createOutputStream(COSName.FLATE_DECODE);
			output.write(offsetsMapBuffer);
			setFirstEntryOffset(offsetsMapBuffer.length);
			for (byte[] rawObject : objectsBuffer) {
				output.write(rawObject);
			}
		} finally {
			if (output != null) {
				output.close();
			}
		}

		return this;
	}

	/**
	 * This method prepares and writes COS data to the object stream by selecting appropriate specialized methods for
	 * the content.
	 *
	 * @param output   The stream, that shall be written to.
	 * @param object   The content, that shall be written.
	 * @param topLevel True, if the currently written object is a top level entry of this object stream.
	 * @throws IOException Shall be thrown, when an exception occurred for the write operation.
	 */
	private void writeObject(OutputStream output, Object object, boolean topLevel)
			throws IOException {
		if(object == null){
			return;
		}

		if (object instanceof Operator) {
			writeOperator(output, (Operator) object);
			return;
		}
		if(!(object instanceof COSBase)){
			throw new IOException("Error: Unknown type in object stream:" + object);
		}

		COSBase base = object instanceof COSObject ? ((COSObject) object).getObject() : (COSBase) object;
		if(base == null){
			return;
		}
		if(!topLevel && this.compressionPool.contains(base)){
			COSObjectKey key = this.compressionPool.getKey(base);
			if (key == null) {
				throw new IOException("Error: Adding unknown object reference to object stream:" + object);
			}
			writeObjectReference(output, key);
		} else if (base instanceof COSString) {
			writeCOSString(output, (COSString) base);
		} else if (base instanceof COSFloat) {
			writeCOSFloat(output, (COSFloat) base);
		} else if (base instanceof COSInteger) {
			writeCOSInteger(output, (COSInteger) base);
		} else if (base instanceof COSBoolean) {
			writeCOSBoolean(output, (COSBoolean) base);
		} else if (base instanceof COSName) {
			writeCOSName(output, (COSName) base);
		} else if (base instanceof COSArray) {
			writeCOSArray(output, (COSArray) base);
		} else if (base instanceof COSDictionary) {
			writeCOSDictionary(output, (COSDictionary) base);
		} else if (base instanceof COSNull) {
			writeCOSNull(output);
		} else {
			throw new IOException("Error: Unknown type in object stream:" + object);
		}
	}

	/**
	 * Write the given {@link COSString} to the given stream.
	 *
	 * @param output    The stream, that shall be written to.
	 * @param cosString The content, that shall be written.
	 */
	private void writeCOSString(OutputStream output, COSString cosString)
			throws IOException {
		COSWriter.writeString(cosString, output);
		output.write(COSWriter.SPACE);
	}

	/**
	 * Write the given {@link COSFloat} to the given stream.
	 *
	 * @param output   The stream, that shall be written to.
	 * @param cosFloat The content, that shall be written.
	 */
	private void writeCOSFloat(OutputStream output, COSFloat cosFloat)
			throws IOException {
		cosFloat.writePDF(output);
		output.write(COSWriter.SPACE);
	}

	/**
	 * Write the given {@link COSInteger} to the given stream.
	 *
	 * @param output     The stream, that shall be written to.
	 * @param cosInteger The content, that shall be written.
	 */
	private void writeCOSInteger(OutputStream output, COSInteger cosInteger)
			throws IOException {
		cosInteger.writePDF(output);
		output.write(COSWriter.SPACE);
	}

	/**
	 * Write the given {@link COSBoolean} to the given stream.
	 *
	 * @param output     The stream, that shall be written to.
	 * @param cosBoolean The content, that shall be written.
	 */
	private void writeCOSBoolean(OutputStream output, COSBoolean cosBoolean)
			throws IOException {
		cosBoolean.writePDF(output);
		output.write(COSWriter.SPACE);
	}

	/**
	 * Write the given {@link COSName} to the given stream.
	 *
	 * @param output  The stream, that shall be written to.
	 * @param cosName The content, that shall be written.
	 */
	private void writeCOSName(OutputStream output, COSName cosName)
			throws IOException {
		cosName.writePDF(output);
		output.write(COSWriter.SPACE);
	}

	/**
	 * Write the given {@link COSArray} to the given stream.
	 *
	 * @param output   The stream, that shall be written to.
	 * @param cosArray The content, that shall be written.
	 */
	private void writeCOSArray(OutputStream output, COSArray cosArray)
			throws IOException {
		output.write(COSWriter.ARRAY_OPEN);
		for (COSBase value : cosArray.toList()) {
			if(value == null){
				writeCOSNull(output);
			} else {
				writeObject(output, value, false);
			}
		}
		output.write(COSWriter.ARRAY_CLOSE);
		output.write(COSWriter.SPACE);
	}

	/**
	 * Write the given {@link COSDictionary} to the given stream.
	 *
	 * @param output        The stream, that shall be written to.
	 * @param cosDictionary The content, that shall be written.
	 */
	private void writeCOSDictionary(OutputStream output, COSDictionary cosDictionary)
			throws IOException {
		output.write(COSWriter.DICT_OPEN);
		for (Map.Entry<COSName, COSBase> entry : cosDictionary.entrySet()) {
			if (entry.getValue() != null) {
				writeObject(output, entry.getKey(), false);
				writeObject(output, entry.getValue(), false);
			}
		}
		output.write(COSWriter.DICT_CLOSE);
		output.write(COSWriter.SPACE);
	}

	/**
	 * Write the given {@link COSObjectKey} to the given stream.
	 *
	 * @param output            The stream, that shall be written to.
	 * @param indirectReference The content, that shall be written.
	 */
	private void writeObjectReference(OutputStream output, COSObjectKey indirectReference)
			throws IOException {
		output.write(String.valueOf(indirectReference.getNumber()).getBytes(Charsets.ISO_8859_1));
		output.write(COSWriter.SPACE);
		output.write(String.valueOf(indirectReference.getGeneration()).getBytes(Charsets.ISO_8859_1));
		output.write(COSWriter.SPACE);
		output.write(COSWriter.REFERENCE);
		output.write(COSWriter.SPACE);
	}

	/**
	 * Write {@link COSNull} to the given stream.
	 *
	 * @param output The stream, that shall be written to.
	 */
	private void writeCOSNull(OutputStream output)
			throws IOException {
		output.write("null".getBytes(Charsets.ISO_8859_1));
		output.write(COSWriter.SPACE);
	}

	/**
	 * Write the given {@link Operator} to the given stream.
	 *
	 * @param output   The stream, that shall be written to.
	 * @param operator The content, that shall be written.
	 */
	private void writeOperator(OutputStream output, Operator operator)
			throws IOException {
		if (operator.getName().equals(OperatorName.BEGIN_INLINE_IMAGE)) {
			output.write(OperatorName.BEGIN_INLINE_IMAGE.getBytes(Charsets.ISO_8859_1));
			COSDictionary dic = operator.getImageParameters();
			for (COSName key : dic.keySet()) {
				Object value = dic.getDictionaryObject(key);
				key.writePDF(output);
				output.write(COSWriter.SPACE);
				writeObject(output, value, false);
			}
			output.write(OperatorName.BEGIN_INLINE_IMAGE_DATA.getBytes(Charsets.ISO_8859_1));
			output.write(operator.getImageData());
			output.write(OperatorName.END_INLINE_IMAGE.getBytes(Charsets.ISO_8859_1));
		} else {
			output.write(operator.getName().getBytes(Charsets.ISO_8859_1));
		}
	}
}

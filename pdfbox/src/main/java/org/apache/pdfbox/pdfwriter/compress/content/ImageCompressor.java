package org.apache.pdfbox.pdfwriter.compress.content;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdfwriter.compress.TraversedCOSElement;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * An instance of this class shall provide the means to filter for {@link PDImageXObject}s, that can be compressed by
 * applying a DCT - Filter (converting them to jpegs).
 * This compressor shall deny compressing images, that it can not guarantee to compress without being able to regenerate
 * valid dictionary entries.
 *
 * @author Christian Appl
 */
public class ImageCompressor extends AbstractContentCompressor {

	private final int jpegQuality;

	/**
	 * Creates an object, that provide the means to filter for {@link PDImageXObject}s, that can be compressed by
	 * applying a DCT - Filter (converting them to jpegs).
	 * This compressor shall deny compressing images, that it can not guarantee to compress without being able to
	 * regenerate valid dictionary entries.
	 *
	 * @param jpegQuality The quality of the resulting JPEG images - a lower quality leads to a higher compression rate.
	 */
	public ImageCompressor(int jpegQuality) {
		this.jpegQuality = jpegQuality;
	}

	/**
	 * Shall return false for all objects, that are not valid image {@link COSStream}s. Shall also return false for all
	 * image {@link COSStream}s, that contain dictionary entries, that can not be transferred to a compressed image
	 * stream.
	 *
	 * @param currentState     The current state of the object, that shall be compressed.
	 * @param traversedElement The traversed COSentry.
	 * @return True, if the object is a compressible image {@link COSStream}.
	 */
	@Override
	public boolean isCompressible(COSBase currentState, TraversedCOSElement traversedElement) {
		COSBase base = currentState instanceof COSObject ? ((COSObject) currentState).getObject() : currentState;
		if (!(base instanceof COSStream)) {
			return false;
		}
		COSStream stream = (COSStream) base;
		return COSName.XOBJECT.equals(((COSStream) base).getCOSName(COSName.TYPE)) &&
				COSName.IMAGE.equals(((COSStream) base).getCOSName(COSName.SUBTYPE)) &&
				!traversedElement.isPartOfStreamDictionary() &&
				!stream.containsKey(COSName.IMAGE_MASK) &&
				!stream.containsKey(COSName.MASK) &&
				!stream.containsKey(COSName.INTERPOLATE) &&
				!stream.containsKey(COSName.SMASK);
	}

	/**
	 * The object can be assumed, to contain a compressible image {@link COSStream}. A DCT compression shall be applied
	 * to the stream data, without further checks for validity.
	 *
	 * @param document         The document a {@link COSObject} shall be compressed for.
	 * @param currentState     The current state of the object, that shall be compressed.
	 * @param traversedElement The traversed COSentry.
	 * @return The resulting object.
	 * @throws IOException Shall be thrown, if an error occurred during compression.
	 */
	@Override
	protected COSBase doCompress(PDDocument document, COSBase currentState, TraversedCOSElement traversedElement) throws IOException {
		COSBase base = currentState instanceof COSObject ? ((COSObject) currentState).getObject() : currentState;
		COSStream stream = (COSStream) base;
		InputStream inputStream = null;
		PDImageXObject xObject;
		try {
			inputStream = stream.createRawInputStream();
			xObject = new PDImageXObject(
					document, inputStream, stream.getFilters(),
					stream.getInt(COSName.WIDTH), stream.getInt(COSName.HEIGHT), stream.getInt(COSName.BITS_PER_COMPONENT),
					PDColorSpace.create(stream.getDictionaryObject(COSName.COLORSPACE))
			);
		} finally {
			if (inputStream != null) {
				inputStream.close();
			}
		}

		BufferedImage image = xObject.getImage();
		PDImageXObject imagex = JPEGFactory.createFromImage(document, image, jpegQuality / 100f);
		for (COSName key : stream.keySet()) {
			if (Arrays.asList(
					COSName.FILTER, COSName.LENGTH, COSName.DECODE_PARMS, COSName.WIDTH, COSName.HEIGHT,
					COSName.BITS_PER_COMPONENT, COSName.COLORSPACE, COSName.DECODE
			).contains(key)) {
				continue;
			}
			imagex.getStream().getCOSObject().setItem(key, stream.getItem(key));
		}
		stream = imagex.getStream().getCOSObject();

		if (currentState instanceof COSObject) {
			COSObject cosObject = (COSObject) currentState;
			cosObject.setObject(stream);
			return cosObject;
		}
		return stream;
	}
}

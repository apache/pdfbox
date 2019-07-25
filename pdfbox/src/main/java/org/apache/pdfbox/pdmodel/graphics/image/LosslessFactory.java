/*
 * Copyright 2014 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pdfbox.pdmodel.graphics.image;

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.filter.Filter;
import org.apache.pdfbox.filter.FilterFactory;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceCMYK;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceColorSpace;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceGray;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.apache.pdfbox.pdmodel.graphics.color.PDICCBased;

/**
 * Factory for creating a PDImageXObject containing a lossless compressed image.
 *
 * @author Tilman Hausherr
 */
public final class LosslessFactory
{
    /** 
     * Internal, only for benchmark purpose
     */
    static boolean usePredictorEncoder = true;

    private LosslessFactory()
    {
    }

    /**
     * Creates a new lossless encoded image XObject from a BufferedImage.
     * <p>
     * <u>New for advanced users from 2.0.12 on:</u><br>
     * If you created your image with a non standard ICC colorspace, it will be
     * preserved. (If you load images in java using ImageIO then no need to read
     * this segment) However a new colorspace will be created for each image. So
     * if you create a PDF with several such images, consider replacing the
     * colorspace with a common object to save space. This is done with
     * {@link PDImageXObject#getColorSpace()} and
     * {@link PDImageXObject#setColorSpace(org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace) PDImageXObject.setColorSpace()}
     *
     * @param document the document where the image will be created
     * @param image the BufferedImage to embed
     * @return a new image XObject
     * @throws IOException if something goes wrong
     */
    public static PDImageXObject createFromImage(PDDocument document, BufferedImage image)
            throws IOException
    {
        if ((image.getType() == BufferedImage.TYPE_BYTE_GRAY && image.getColorModel().getPixelSize() <= 8)
                || (image.getType() == BufferedImage.TYPE_BYTE_BINARY && image.getColorModel().getPixelSize() == 1))
        {
            return createFromGrayImage(image, document);
        }
        else
        {
            // We try to encode the image with predictor
            if (usePredictorEncoder)
            {
                PDImageXObject pdImageXObject = new PredictorEncoder(document, image).encode();
                if (pdImageXObject != null)
                {
                    if (pdImageXObject.getColorSpace() == PDDeviceRGB.INSTANCE &&
                        pdImageXObject.getBitsPerComponent() < 16 &&
                        image.getWidth() * image.getHeight() <= 50 * 50)
                    {
                        // also create classic compressed image, compare sizes
                        PDImageXObject pdImageXObjectClassic = createFromRGBImage(image, document);
                        if (pdImageXObjectClassic.getCOSObject().getLength() < 
                            pdImageXObject.getCOSObject().getLength())
                        {
                            pdImageXObject.getCOSObject().close();
                            return pdImageXObjectClassic;
                        }
                        else
                        {
                            pdImageXObjectClassic.getCOSObject().close();
                        }
                    }
                    return pdImageXObject;
                }
            }

            // Fallback: We export the image as 8-bit sRGB and might loose color information
            return createFromRGBImage(image, document);
        }
    }

    // grayscale images need one color per sample
    private static PDImageXObject createFromGrayImage(BufferedImage image, PDDocument document)
            throws IOException
    {
        int height = image.getHeight();
        int width = image.getWidth();
        int[] rgbLineBuffer = new int[width];
        int bpc = image.getColorModel().getPixelSize();
        ByteArrayOutputStream baos = new ByteArrayOutputStream(((width*bpc/8)+(width*bpc%8 != 0 ? 1:0))*height);
        MemoryCacheImageOutputStream mcios = new MemoryCacheImageOutputStream(baos);
        for (int y = 0; y < height; ++y)
        {
            for (int pixel : image.getRGB(0, y, width, 1, rgbLineBuffer, 0, width))
            {
                mcios.writeBits(pixel & 0xFF, bpc);
            }

            int bitOffset = mcios.getBitOffset();
            if (bitOffset != 0)
            {
                mcios.writeBits(0, 8 - bitOffset);
            }
        }
        mcios.flush();
        mcios.close();
        return prepareImageXObject(document, baos.toByteArray(),
                image.getWidth(), image.getHeight(), bpc, PDDeviceGray.INSTANCE);
    }

    private static PDImageXObject createFromRGBImage(BufferedImage image, PDDocument document) throws IOException
    {
        int height = image.getHeight();
        int width = image.getWidth();
        int[] rgbLineBuffer = new int[width];
        int bpc = 8;
        PDDeviceColorSpace deviceColorSpace = PDDeviceRGB.INSTANCE;
        byte[] imageData = new byte[width * height * 3];
        int byteIdx = 0;
        int alphaByteIdx = 0;
        int alphaBitPos = 7;
        int transparency = image.getTransparency();
        int apbc = transparency == Transparency.BITMASK ? 1 : 8;
        byte[] alphaImageData;
        if (transparency != Transparency.OPAQUE)
        {
            alphaImageData = new byte[((width * apbc / 8) + (width * apbc % 8 != 0 ? 1 : 0)) * height];
        }
        else
        {
            alphaImageData = new byte[0];
        }
        for (int y = 0; y < height; ++y)
        {
            for (int pixel : image.getRGB(0, y, width, 1, rgbLineBuffer, 0, width))
            {
                imageData[byteIdx++] = (byte) ((pixel >> 16) & 0xFF);
                imageData[byteIdx++] = (byte) ((pixel >> 8) & 0xFF);
                imageData[byteIdx++] = (byte) (pixel & 0xFF);
                if (transparency != Transparency.OPAQUE)
                {
                    // we have the alpha right here, so no need to do it separately
                    // as done prior April 2018
                    if (transparency == Transparency.BITMASK)
                    {
                        // write a bit
                        alphaImageData[alphaByteIdx] |= ((pixel >> 24) & 1) << alphaBitPos;
                        if (--alphaBitPos < 0)
                        {
                            alphaBitPos = 7;
                            ++alphaByteIdx;
                        }
                    }
                    else
                    {
                        // write a byte
                        alphaImageData[alphaByteIdx++] = (byte) ((pixel >> 24) & 0xFF);
                    }
                }
            }

            // skip boundary if needed
            if (transparency == Transparency.BITMASK && alphaBitPos != 7)
            {
                alphaBitPos = 7;
                ++alphaByteIdx;
            }
        }
        PDImageXObject pdImage = prepareImageXObject(document, imageData,
                image.getWidth(), image.getHeight(), bpc, deviceColorSpace);      
        if (transparency != Transparency.OPAQUE)
        {
            PDImageXObject pdMask = prepareImageXObject(document, alphaImageData,
                    image.getWidth(), image.getHeight(), apbc, PDDeviceGray.INSTANCE);
            pdImage.getCOSObject().setItem(COSName.SMASK, pdMask);
        }
        return pdImage;
    }

    /**
     * Create a PDImageXObject using the Flate filter.
     * 
     * @param document The document.
     * @param byteArray array with data.
     * @param width the image width
     * @param height the image height
     * @param bitsPerComponent the bits per component
     * @param initColorSpace the color space
     * @return the newly created PDImageXObject with the data compressed.
     * @throws IOException 
     */
    private static PDImageXObject prepareImageXObject(PDDocument document, 
            byte [] byteArray, int width, int height, int bitsPerComponent, 
            PDColorSpace initColorSpace) throws IOException
    {
        //pre-size the output stream to half of the input
        ByteArrayOutputStream baos = new ByteArrayOutputStream(byteArray.length/2);

        Filter filter = FilterFactory.INSTANCE.getFilter(COSName.FLATE_DECODE);
        filter.encode(new ByteArrayInputStream(byteArray), baos, new COSDictionary(), 0);

        ByteArrayInputStream encodedByteStream = new ByteArrayInputStream(baos.toByteArray());
        return new PDImageXObject(document, encodedByteStream, COSName.FLATE_DECODE, 
                width, height, bitsPerComponent, initColorSpace);
    }

    private static class PredictorEncoder
    {
        private final PDDocument document;
        private final BufferedImage image;
        private final int componentsPerPixel;
        private final int transferType;
        private final int bytesPerComponent;
        private final int bytesPerPixel;

        private final int height;
        private final int width;

        private final byte[] dataRawRowNone;
        private final byte[] dataRawRowSub;
        private final byte[] dataRawRowUp;
        private final byte[] dataRawRowAverage;
        private final byte[] dataRawRowPaeth;

        final int imageType;
        final boolean hasAlpha;
        final byte[] alphaImageData;

        final byte[] aValues;
        final byte[] cValues;
        final byte[] bValues;
        final byte[] xValues;
        final byte[] tmpResultValues;

        /**
         * Initialize the encoder and set all final fields
         */
        PredictorEncoder(PDDocument document, BufferedImage image)
        {
            this.document = document;
            this.image = image;

            // The raw count of components per pixel including optional alpha
            this.componentsPerPixel = image.getColorModel().getNumComponents();
            this.transferType = image.getRaster().getTransferType();
            this.bytesPerComponent = (transferType == DataBuffer.TYPE_SHORT
                    || transferType == DataBuffer.TYPE_USHORT) ? 2 : 1;

            // Only the bytes we need in the output (excluding alpha)
            this.bytesPerPixel = image.getColorModel().getNumColorComponents() * bytesPerComponent;

            this.height = image.getHeight();
            this.width = image.getWidth();
            this.imageType = image.getType();
            this.hasAlpha = image.getColorModel().getNumComponents() != image.getColorModel()
                    .getNumColorComponents();
            this.alphaImageData = hasAlpha ? new byte[width * height * bytesPerComponent] : null;

            // The rows have 1-byte encoding marker and width * BYTES_PER_PIXEL pixel-bytes
            int dataRowByteCount = width * bytesPerPixel + 1;
            this.dataRawRowNone = new byte[dataRowByteCount];
            this.dataRawRowSub = new byte[dataRowByteCount];
            this.dataRawRowUp = new byte[dataRowByteCount];
            this.dataRawRowAverage = new byte[dataRowByteCount];
            this.dataRawRowPaeth = new byte[dataRowByteCount];

            // Write the encoding markers
            dataRawRowNone[0] = 0;
            dataRawRowSub[0] = 1;
            dataRawRowUp[0] = 2;
            dataRawRowAverage[0] = 3;
            dataRawRowPaeth[0] = 4;

            // c | b
            // -----
            // a | x
            //
            // x => current pixel
            this.aValues = new byte[bytesPerPixel];
            this.cValues = new byte[bytesPerPixel];
            this.bValues = new byte[bytesPerPixel];
            this.xValues = new byte[bytesPerPixel];
            this.tmpResultValues = new byte[bytesPerPixel];
        }

        /**
         * Tries to compress the image using a predictor.
         *
         * @return the image or null if it is not possible to encoded the image (e.g. not supported
         * raster format etc.)
         */
        PDImageXObject encode() throws IOException
        {
            Raster imageRaster = image.getRaster();
            final int elementsInRowPerPixel;

            // These variables store a row of the image each, the exact type depends
            // on the image encoding. Can be a int[], short[] or byte[]
            Object prevRow;
            Object transferRow;

            switch (imageType)
            {
                case BufferedImage.TYPE_CUSTOM:
                {
                    switch (imageRaster.getTransferType())
                    {
                        case DataBuffer.TYPE_USHORT:
                            elementsInRowPerPixel = componentsPerPixel;
                            prevRow = new short[width * elementsInRowPerPixel];
                            transferRow = new short[width * elementsInRowPerPixel];
                            break;
                        case DataBuffer.TYPE_BYTE:
                            elementsInRowPerPixel = componentsPerPixel;
                            prevRow = new byte[width * elementsInRowPerPixel];
                            transferRow = new byte[width * elementsInRowPerPixel];
                            break;
                        default:
                            return null;
                    }
                    break;
                }

                case BufferedImage.TYPE_3BYTE_BGR:
                case BufferedImage.TYPE_4BYTE_ABGR:
                {
                    elementsInRowPerPixel = componentsPerPixel;
                    prevRow = new byte[width * elementsInRowPerPixel];
                    transferRow = new byte[width * elementsInRowPerPixel];
                    break;
                }

                case BufferedImage.TYPE_INT_BGR:
                case BufferedImage.TYPE_INT_ARGB:
                case BufferedImage.TYPE_INT_RGB:
                {
                    elementsInRowPerPixel = 1;
                    prevRow = new int[width * elementsInRowPerPixel];
                    transferRow = new int[width * elementsInRowPerPixel];
                    break;
                }

                default:
                    // We can not handle this unknown format
                    return null;
            }

            final int elementsInTransferRow = width * elementsInRowPerPixel;

            // pre-size the output stream to half of the maximum size
            ByteArrayOutputStream stream = new ByteArrayOutputStream(
                    height * width * bytesPerPixel / 2);
            Deflater deflater = new Deflater(Filter.getCompressionLevel());
            DeflaterOutputStream zip = new DeflaterOutputStream(stream, deflater);

            int alphaPtr = 0;

            for (int rowNum = 0; rowNum < height; rowNum++)
            {
                imageRaster.getDataElements(0, rowNum, width, 1, transferRow);

                // We start to write at index one, as the predictor marker is in index zero
                int writerPtr = 1;
                Arrays.fill(aValues, (byte) 0);
                Arrays.fill(cValues, (byte) 0);

                final byte[] transferRowByte;
                final byte[] prevRowByte;
                final int[] transferRowInt;
                final int[] prevRowInt;
                final short[] transferRowShort;
                final short[] prevRowShort;

                if (transferRow instanceof byte[])
                {
                    transferRowByte = (byte[]) transferRow;
                    prevRowByte = (byte[]) prevRow;
                    transferRowInt = prevRowInt = null;
                    transferRowShort = prevRowShort = null;
                }
                else if (transferRow instanceof int[])
                {
                    transferRowInt = (int[]) transferRow;
                    prevRowInt = (int[]) prevRow;
                    transferRowShort = prevRowShort = null;
                    transferRowByte = prevRowByte = null;
                }
                else
                {
                    // This must be short[]
                    transferRowShort = (short[]) transferRow;
                    prevRowShort = (short[]) prevRow;
                    transferRowInt = prevRowInt = null;
                    transferRowByte = prevRowByte = null;
                }

                for (int indexInTransferRow = 0; indexInTransferRow < elementsInTransferRow;
                        indexInTransferRow += elementsInRowPerPixel, alphaPtr += bytesPerComponent)
                {
                    // Copy the pixel values into the byte array
                    if (transferRowByte != null)
                    {
                        copyImageBytes(transferRowByte, indexInTransferRow, xValues, alphaImageData,
                                alphaPtr);
                        copyImageBytes(prevRowByte, indexInTransferRow, bValues, null, 0);
                    }
                    else if (transferRowInt != null)
                    {
                        copyIntToBytes(transferRowInt, indexInTransferRow, xValues, alphaImageData,
                                alphaPtr);
                        copyIntToBytes(prevRowInt, indexInTransferRow, bValues, null, 0);
                    }
                    else
                    {
                        // This must be short[]
                        copyShortsToBytes(transferRowShort, indexInTransferRow, xValues, alphaImageData, alphaPtr);
                        copyShortsToBytes(prevRowShort, indexInTransferRow, bValues, null, 0);
                    }

                    // Encode the pixel values in the different encodings
                    int length = xValues.length;
                    for (int bytePtr = 0; bytePtr < length; bytePtr++)
                    {
                        int x = xValues[bytePtr] & 0xFF;
                        int a = aValues[bytePtr] & 0xFF;
                        int b = bValues[bytePtr] & 0xFF;
                        int c = cValues[bytePtr] & 0xFF;
                        dataRawRowNone[writerPtr] = (byte) x;
                        dataRawRowSub[writerPtr] = pngFilterSub(x, a);
                        dataRawRowUp[writerPtr] = pngFilterUp(x, b);
                        dataRawRowAverage[writerPtr] = pngFilterAverage(x, a, b);
                        dataRawRowPaeth[writerPtr] = pngFilterPaeth(x, a, b, c);
                        writerPtr++;
                    }

                    //  We shift the values into the prev / upper left values for the next pixel
                    System.arraycopy(xValues, 0, aValues, 0, bytesPerPixel);
                    System.arraycopy(bValues, 0, cValues, 0, bytesPerPixel);
                }

                byte[] rowToWrite = chooseDataRowToWrite();

                // Write and compress the row as long it is hot (CPU cache wise)
                zip.write(rowToWrite, 0, rowToWrite.length);

                // We swap prev and transfer row, so that we have the prev row for the next row.
                Object temp = prevRow;
                prevRow = transferRow;
                transferRow = temp;
            }
            zip.close();
            deflater.end();

            return preparePredictorPDImage(stream, bytesPerComponent * 8);
        }

        private void copyIntToBytes(int[] transferRow, int indexInTranferRow, byte[] targetValues,
                byte[] alphaImageData, int alphaPtr)
        {
            int val = transferRow[indexInTranferRow];
            byte b0 = (byte) (val & 0xFF);
            byte b1 = (byte) ((val >> 8) & 0xFF);
            byte b2 = (byte) ((val >> 16) & 0xFF);

            switch (imageType)
            {
                case BufferedImage.TYPE_INT_BGR:
                    targetValues[0] = b0;
                    targetValues[1] = b1;
                    targetValues[2] = b2;
                    break;
                case BufferedImage.TYPE_INT_ARGB:
                    targetValues[0] = b2;
                    targetValues[1] = b1;
                    targetValues[2] = b0;
                    if (alphaImageData != null)
                    {
                        byte b3 = (byte) ((val >> 24) & 0xFF);
                        alphaImageData[alphaPtr] = b3;
                    }
                    break;
                case BufferedImage.TYPE_INT_RGB:
                    targetValues[0] = b2;
                    targetValues[1] = b1;
                    targetValues[2] = b0;
                    break;
            }
        }

        private void copyImageBytes(byte[] transferRow, int indexInTranferRow, byte[] targetValues,
                byte[] alphaImageData, int alphaPtr)
        {
            System.arraycopy(transferRow, indexInTranferRow, targetValues, 0, targetValues.length);
            if (alphaImageData != null)
            {
                alphaImageData[alphaPtr] = transferRow[indexInTranferRow + targetValues.length];
            }
        }

        private static void copyShortsToBytes(short[] transferRow, int indexInTranferRow,
                byte[] targetValues, byte[] alphaImageData, int alphaPtr)
        {
            int itr = indexInTranferRow;
            for (int i = 0; i < targetValues.length; i += 2)
            {
                short val = transferRow[itr++];
                targetValues[i] = (byte) ((val >> 8) & 0xFF);
                targetValues[i + 1] = (byte) (val & 0xFF);
            }
            if (alphaImageData != null)
            {
                short alpha = transferRow[itr];
                alphaImageData[alphaPtr] = (byte) ((alpha >> 8) & 0xFF);
                alphaImageData[alphaPtr + 1] = (byte) (alpha & 0xFF);
            }
        }

        private PDImageXObject preparePredictorPDImage(ByteArrayOutputStream stream,
                int bitsPerComponent) throws IOException
        {
            int h = image.getHeight();
            int w = image.getWidth();

            ColorSpace srcCspace = image.getColorModel().getColorSpace();
            int srcCspaceType = srcCspace.getType();
            PDColorSpace pdColorSpace = srcCspaceType == ColorSpace.TYPE_CMYK
                    ? PDDeviceCMYK.INSTANCE
                    : (srcCspaceType == ColorSpace.TYPE_GRAY
                            ? PDDeviceGray.INSTANCE : PDDeviceRGB.INSTANCE);

            // Encode the image profile if the image has one
            if (srcCspace instanceof ICC_ColorSpace)
            {
                ICC_Profile profile = ((ICC_ColorSpace) srcCspace).getProfile();
                // We only encode a color profile if it is not sRGB
                if (profile != ICC_Profile.getInstance(ColorSpace.CS_sRGB))
                {
                    PDICCBased pdProfile = new PDICCBased(document);
                    OutputStream outputStream = pdProfile.getPDStream()
                            .createOutputStream(COSName.FLATE_DECODE);
                    outputStream.write(profile.getData());
                    outputStream.close();
                    pdProfile.getPDStream().getCOSObject().setInt(COSName.N,
                            srcCspace.getNumComponents());
                    pdProfile.getPDStream().getCOSObject().setItem(COSName.ALTERNATE,
                            srcCspaceType == ColorSpace.TYPE_GRAY ? COSName.DEVICEGRAY
                                    : (srcCspaceType == ColorSpace.TYPE_CMYK ? COSName.DEVICECMYK
                                            : COSName.DEVICERGB));
                    pdColorSpace = pdProfile;
                }
            }

            PDImageXObject imageXObject = new PDImageXObject(document,
                    new ByteArrayInputStream(stream.toByteArray()), COSName.FLATE_DECODE, w,
                    h, bitsPerComponent, pdColorSpace);

            COSDictionary decodeParms = new COSDictionary();
            decodeParms.setItem(COSName.BITS_PER_COMPONENT, COSInteger.get(bitsPerComponent));
            decodeParms.setItem(COSName.PREDICTOR, COSInteger.get(15));
            decodeParms.setItem(COSName.COLUMNS, COSInteger.get(w));
            decodeParms.setItem(COSName.COLORS, COSInteger.get(srcCspace.getNumComponents()));
            imageXObject.getCOSObject().setItem(COSName.DECODE_PARMS, decodeParms);

            if (image.getTransparency() != Transparency.OPAQUE)
            {
                PDImageXObject pdMask = prepareImageXObject(document, alphaImageData,
                        image.getWidth(), image.getHeight(), 8 * bytesPerComponent, PDDeviceGray.INSTANCE);
                imageXObject.getCOSObject().setItem(COSName.SMASK, pdMask);
            }
            return imageXObject;
        }

        /**
         * We look which row encoding is the "best" one, ie. has the lowest sum. We don't implement
         * anything fancier to choose the right row encoding. This is just the recommend algorithm
         * in the spec. The get the perfect encoding you would need to do a brute force check how
         * all the different encoded rows compress in the zip stream together. You have would have
         * to check 5*image-height permutations...
         *
         * @return the "best" row encoding of the row encodings
         */
        private byte[] chooseDataRowToWrite()
        {
            byte[] rowToWrite = dataRawRowNone;
            long estCompressSum = estCompressSum(dataRawRowNone);
            long estCompressSumSub = estCompressSum(dataRawRowSub);
            long estCompressSumUp = estCompressSum(dataRawRowUp);
            long estCompressSumAvg = estCompressSum(dataRawRowAverage);
            long estCompressSumPaeth = estCompressSum(dataRawRowPaeth);
            if (estCompressSum > estCompressSumSub)
            {
                rowToWrite = dataRawRowSub;
                estCompressSum = estCompressSumSub;
            }
            if (estCompressSum > estCompressSumUp)
            {
                rowToWrite = dataRawRowUp;
                estCompressSum = estCompressSumUp;
            }
            if (estCompressSum > estCompressSumAvg)
            {
                rowToWrite = dataRawRowAverage;
                estCompressSum = estCompressSumAvg;
            }
            if (estCompressSum > estCompressSumPaeth)
            {
                rowToWrite = dataRawRowPaeth;
            }
            return rowToWrite;
        }

        /*
         * PNG Filters, see https://www.w3.org/TR/PNG-Filters.html
         */
        private static byte pngFilterSub(int x, int a)
        {
            return (byte) ((x & 0xFF) - (a & 0xFF));
        }

        private static byte pngFilterUp(int x, int b)
        {
            // Same as pngFilterSub, just called with the prior row
            return pngFilterSub(x, b);
        }

        private static byte pngFilterAverage(int x, int a, int b)
        {
            return (byte) (x - ((b + a) / 2));
        }

        private static byte pngFilterPaeth(int x, int a, int b, int c)
        {
            int p = a + b - c;
            int pa = Math.abs(p - a);
            int pb = Math.abs(p - b);
            int pc = Math.abs(p - c);
            final int pr;
            if (pa <= pb && pa <= pc)
            {
                pr = a;
            }
            else if (pb <= pc)
            {
                pr = b;
            }
            else
            {
                pr = c;
            }

            int r = x - pr;
            return (byte) (r);
        }

        private static long estCompressSum(byte[] dataRawRowSub)
        {
            long sum = 0;
            for (byte aDataRawRowSub : dataRawRowSub)
            {
                // https://www.w3.org/TR/PNG-Encoders.html#E.Filter-selection
                sum += Math.abs(aDataRawRowSub);
            }
            return sum;
        }
    }
}

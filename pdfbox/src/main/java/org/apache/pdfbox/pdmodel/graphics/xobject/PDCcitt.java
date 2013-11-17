/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pdfbox.pdmodel.graphics.xobject;

import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.io.RandomAccess;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceGray;
import org.apache.pdfbox.pdmodel.graphics.color.PDIndexed;

/**
 * An image class for CCITT Fax.
 * 
 * @author <a href="ben@benlitchfield.com">Ben Litchfield</a>
 * @author paul king
 * 
 */
public class PDCcitt extends PDXObjectImage
{

    private static final List<String> FAX_FILTERS = new ArrayList<String>();

    static
    {
        FAX_FILTERS.add(COSName.CCITTFAX_DECODE.getName());
        FAX_FILTERS.add(COSName.CCITTFAX_DECODE_ABBREVIATION.getName());
    }

    /**
     * Standard constructor.
     * 
     * @param ccitt The PDStream that already contains all ccitt information.
     */
    public PDCcitt(PDStream ccitt)
    {
        super(ccitt, "tiff");

    }

    /**
     * Construct from a tiff file.
     * 
     * @param doc The document to create the image as part of.
     * @param raf The random access TIFF file which contains a suitable CCITT compressed image
     * @throws IOException If there is an error reading the tiff data.
     */

    public PDCcitt(PDDocument doc, RandomAccess raf) throws IOException
    {
        super(new PDStream(doc), "tiff");

        COSDictionary decodeParms = new COSDictionary();

        COSDictionary dic = getCOSStream();

        extractFromTiff(raf, getCOSStream().createFilteredStream(), decodeParms);

        dic.setItem(COSName.FILTER, COSName.CCITTFAX_DECODE);
        dic.setItem(COSName.SUBTYPE, COSName.IMAGE);
        dic.setItem(COSName.TYPE, COSName.XOBJECT);
        dic.setItem(COSName.DECODE_PARMS, decodeParms);

        setBitsPerComponent(1);
        setColorSpace(new PDDeviceGray());
        setWidth(decodeParms.getInt(COSName.COLUMNS));
        setHeight(decodeParms.getInt(COSName.ROWS));

    }

    /**
     * Returns an image of the CCITT Fax, or null if TIFFs are not supported. (Requires additional JAI Image filters )
     * 
     * {@inheritDoc}
     */
    public BufferedImage getRGBImage() throws IOException
    {
        COSStream stream = getCOSStream();
        COSBase decodeP = stream.getDictionaryObject(COSName.DECODE_PARMS);
        COSDictionary decodeParms = null;
        if (decodeP instanceof COSDictionary)
        {
            decodeParms = (COSDictionary) decodeP;
        }
        else if (decodeP instanceof COSArray)
        {
            int index = 0;
            // determine the index for the CCITT-filter
            COSBase filters = stream.getFilters();
            if (filters instanceof COSArray)
            {
                COSArray filterArray = (COSArray) filters;
                while (index < filterArray.size())
                {
                    COSName filtername = (COSName) filterArray.get(index);
                    if (COSName.CCITTFAX_DECODE.equals(filtername))
                    {
                        break;
                    }
                    index++;
                }
            }
            decodeParms = (COSDictionary) ((COSArray) decodeP).getObject(index);
        }
        int cols = decodeParms.getInt(COSName.COLUMNS, 1728);
        int rows = decodeParms.getInt(COSName.ROWS, 0);
        int height = stream.getInt(COSName.HEIGHT, 0);
        if (rows > 0 && height > 0)
        {
            // ensure that rows doesn't contain implausible data, see PDFBOX-771
            rows = Math.min(rows, height);
        }
        else
        {
            // at least one of the values has to have a valid value
            rows = Math.max(rows, height);
        }
        boolean blackIsOne = decodeParms.getBoolean(COSName.BLACK_IS_1, false);
        // maybe a decode array is defined
        COSArray decode = getDecode();
        if (decode != null && decode.getInt(0) == 1)
        {
            // [1.0, 0.0] -> invert the "color" values
            blackIsOne = !blackIsOne;
        }
        byte[] bufferData = null;
        ColorModel colorModel = null;
        PDColorSpace colorspace = getColorSpace();
        // most likely there is no colorspace as a CCITT-filter uses 1-bit values mapped to black/white
        // in some rare cases other colorspaces maybe used such as an indexed colorspace, see PDFBOX-1638
        if (colorspace instanceof PDIndexed)
        {
            PDIndexed csIndexed = (PDIndexed) colorspace;
            COSBase maskArray = getMask();
            if (maskArray != null && maskArray instanceof COSArray)
            {
                colorModel = csIndexed.createColorModel(1, ((COSArray) maskArray).getInt(0));
            }
            else
            {
                colorModel = csIndexed.createColorModel(1);
            }
        }
        else
        {
            byte[] map = new byte[] { (byte) 0x00, (byte) 0xFF };
            colorModel = new IndexColorModel(1, map.length, map, map, map, Transparency.OPAQUE);
        }
        WritableRaster raster = colorModel.createCompatibleWritableRaster(cols, rows);
        DataBufferByte buffer = (DataBufferByte) raster.getDataBuffer();
        bufferData = buffer.getData();
        IOUtils.populateBuffer(stream.getUnfilteredStream(), bufferData);
        BufferedImage image = new BufferedImage(colorModel, raster, false, null);
        if (!blackIsOne)
        {
            // Inverting the bitmap
            // Note the previous approach with starting from an IndexColorModel didn't work
            // reliably. In some cases the image wouldn't be painted for some reason.
            // So a safe but slower approach was taken.
            invertBitmap(bufferData);
        }
        /*
         * If we have an image mask we need to add an alpha channel to the data
         */
        if (hasMask())
        {
            byte[] map = new byte[] { (byte) 0x00, (byte) 0xff };
            IndexColorModel cm = new IndexColorModel(1, map.length, map, map, map, Transparency.OPAQUE);
            raster = cm.createCompatibleWritableRaster(cols, rows);
            bufferData = ((DataBufferByte) raster.getDataBuffer()).getData();

            byte[] array = ((DataBufferByte) image.getData().getDataBuffer()).getData();
            System.arraycopy(array, 0, bufferData, 0,
                    (array.length < bufferData.length ? array.length : bufferData.length));
            BufferedImage indexed = new BufferedImage(cm, raster, false, null);
            image = indexed;
        }
        return applyMasks(image);
    }

    private void invertBitmap(byte[] bufferData)
    {
        for (int i = 0, c = bufferData.length; i < c; i++)
        {
            bufferData[i] = (byte) (~bufferData[i] & 0xFF);
        }
    }

    /**
     * This writes a tiff to out.
     * 
     * {@inheritDoc}
     */
    public void write2OutputStream(OutputStream out) throws IOException
    {
        // We should use another format than TIFF to get rid of the TiffWrapper
        InputStream data = new TiffWrapper(getPDStream().getPartiallyFilteredStream(FAX_FILTERS), getCOSStream());
        IOUtils.copy(data, out);
    }

    /**
     * Extract the ccitt stream from the tiff file.
     * 
     * @param raf - TIFF File
     * @param os - Stream to write raw ccitt data two
     * @param parms - COSDictionary which the encoding parameters are added to
     * @throws IOException If there is an error reading/writing to/from the stream
     */
    private void extractFromTiff(RandomAccess raf, OutputStream os, COSDictionary parms) throws IOException
    {
        try
        {

            // First check the basic tiff header
            raf.seek(0);
            char endianess = (char) raf.read();
            if ((char) raf.read() != endianess)
            {
                throw new IOException("Not a valid tiff file");
            }
            // ensure that endianess is either M or I
            if (endianess != 'M' && endianess != 'I')
            {
                throw new IOException("Not a valid tiff file");
            }
            int magicNumber = readshort(endianess, raf);
            if (magicNumber != 42)
            {
                throw new IOException("Not a valid tiff file");
            }

            // Relocate to the first set of tags
            raf.seek(readlong(endianess, raf));

            int numtags = readshort(endianess, raf);

            // The number 50 is somewhat arbitary, it just stops us load up junk from somewhere and tramping on
            if (numtags > 50)
            {
                throw new IOException("Not a valid tiff file");
            }

            // Loop through the tags, some will convert to items in the parms dictionary
            // Other point us to where to find the data stream
            // The only parm which might change as a result of other options is K, so
            // We'll deal with that as a special;

            int k = -1000; // Default Non CCITT compression
            int dataoffset = 0;
            int datalength = 0;

            for (int i = 0; i < numtags; i++)
            {
                int tag = readshort(endianess, raf);
                int type = readshort(endianess, raf);
                int count = readlong(endianess, raf);
                int val = readlong(endianess, raf); // See note

                // Note, we treated that value as a long. The value always occupies 4 bytes
                // But it might only use the first byte or two. Depending on endianess we might need to correct
                // Note we ignore all other types, they are of little interest for PDFs/CCITT Fax
                if (endianess == 'M')
                {
                    switch (type)
                    {
                    case 1:
                    {
                        val = val >> 24;
                        break; // byte value
                    }
                    case 3:
                    {
                        val = val >> 16;
                        break; // short value
                    }
                    case 4:
                    {
                        break; // long value
                    }
                    default:
                    {
                        // do nothing
                    }
                    }
                }
                switch (tag)
                {
                case 256:
                {
                    parms.setInt(COSName.COLUMNS, val);
                    break;
                }
                case 257:
                {
                    parms.setInt(COSName.ROWS, val);
                    break;
                }
                case 259:
                {
                    if (val == 4)
                    {
                        k = -1;
                    }
                    if (val == 3)
                    {
                        k = 0;
                    }
                    break; // T6/T4 Compression
                }
                case 262:
                {
                    if (val == 1)
                    {
                        parms.setBoolean(COSName.BLACK_IS_1, true);
                    }
                    break;
                }
                case 273:
                {
                    if (count == 1)
                    {
                        dataoffset = val;
                    }
                    break;
                }
                case 279:
                {
                    if (count == 1)
                    {
                        datalength = val;
                    }
                    break;
                }
                case 292:
                {
                    if (val == 1)
                    {
                        k = 50; // T4 2D - arbitary K value
                    }
                    break;
                }
                case 324:
                {
                    if (count == 1)
                    {
                        dataoffset = val;
                    }
                    break;
                }
                case 325:
                {
                    if (count == 1)
                    {
                        datalength = val;
                    }
                    break;
                }
                default:
                {
                    // do nothing
                }
                }
            }

            if (k == -1000)
            {
                throw new IOException("First image in tiff is not CCITT T4 or T6 compressed");
            }
            if (dataoffset == 0)
            {
                throw new IOException("First image in tiff is not a single tile/strip");
            }

            parms.setInt(COSName.K, k);

            raf.seek(dataoffset);

            byte[] buf = new byte[8192];
            int amountRead = -1;
            while ((amountRead = raf.read(buf, 0, Math.min(8192, datalength))) > 0)
            {
                datalength -= amountRead;
                os.write(buf, 0, amountRead);
            }

        }
        finally
        {
            os.close();
        }
    }

    private int readshort(char endianess, RandomAccess raf) throws IOException
    {
        if (endianess == 'I')
        {
            return raf.read() | (raf.read() << 8);
        }
        return (raf.read() << 8) | raf.read();
    }

    private int readlong(char endianess, RandomAccess raf) throws IOException
    {
        if (endianess == 'I')
        {
            return raf.read() | (raf.read() << 8) | (raf.read() << 16) | (raf.read() << 24);
        }
        return (raf.read() << 24) | (raf.read() << 16) | (raf.read() << 8) | raf.read();
    }

    /**
     * Extends InputStream to wrap the data from the CCITT Fax with a suitable TIFF Header. For details see
     * www.tiff.org, which contains useful information including pointers to the TIFF 6.0 Specification
     * 
     */
    private class TiffWrapper extends InputStream
    {

        private int currentOffset; // When reading, where in the tiffheader are we.
        private byte[] tiffheader; // Byte array to store tiff header data
        private InputStream datastream; // Original InputStream

        private TiffWrapper(InputStream rawstream, COSDictionary options)
        {
            buildHeader(options);
            currentOffset = 0;
            datastream = rawstream;
        }

        // Implement basic methods from InputStream
        /**
         * {@inheritDoc}
         */
        public boolean markSupported()
        {
            return false;
        }

        /**
         * {@inheritDoc}
         */
        public void reset() throws IOException
        {
            throw new IOException("reset not supported");
        }

        /**
         * For simple read, take a byte from the tiffheader array or pass through.
         * 
         * {@inheritDoc}
         */
        public int read() throws IOException
        {
            if (currentOffset < tiffheader.length)
            {
                return tiffheader[currentOffset++];
            }
            return datastream.read();
        }

        /**
         * For read methods only return as many bytes as we have left in the header if we've exhausted the header, pass
         * through to the InputStream of the raw CCITT data.
         * 
         * {@inheritDoc}
         */
        public int read(byte[] data) throws IOException
        {
            if (currentOffset < tiffheader.length)
            {
                int length = java.lang.Math.min(tiffheader.length - currentOffset, data.length);
                if (length > 0)
                {
                    System.arraycopy(tiffheader, currentOffset, data, 0, length);
                }
                currentOffset += length;
                return length;
            }
            else
            {
                return datastream.read(data);
            }
        }

        /**
         * For read methods only return as many bytes as we have left in the header if we've exhausted the header, pass
         * through to the InputStream of the raw CCITT data.
         * 
         * {@inheritDoc}
         */
        public int read(byte[] data, int off, int len) throws IOException
        {
            if (currentOffset < tiffheader.length)
            {
                int length = java.lang.Math.min(tiffheader.length - currentOffset, len);
                if (length > 0)
                {
                    System.arraycopy(tiffheader, currentOffset, data, off, length);
                }
                currentOffset += length;
                return length;
            }
            else
            {
                return datastream.read(data, off, len);
            }
        }

        /**
         * When skipping if any header data not yet read, only allow to skip what we've in the buffer Otherwise just
         * pass through.
         * 
         * {@inheritDoc}
         */
        public long skip(long n) throws IOException
        {
            if (currentOffset < tiffheader.length)
            {
                long length = Math.min(tiffheader.length - currentOffset, n);
                currentOffset += length;
                return length;
            }
            else
            {
                return datastream.skip(n);
            }
        }

        // Static data for the beginning of the TIFF header
        private final byte[] basicHeader = { 'I', 'I', 42, 0, 8, 0, 0, 0, // File introducer and pointer to first IFD
                0, 0 }; // Number of tags start with two

        private int additionalOffset; // Offset in header to additional data

        // Builds up the tiffheader based on the options passed through.
        private void buildHeader(COSDictionary options)
        {

            final int numOfTags = 10; // The maximum tags we'll fill
            final int maxAdditionalData = 24; // The maximum amount of additional data
                                              // outside the IFDs. (bytes)

            // The length of the header will be the length of the basic header (10)
            // plus 12 bytes for each IFD, 4 bytes as a pointer to the next IFD (will be 0)
            // plus the length of the additional data

            int ifdSize = 10 + (12 * numOfTags) + 4;
            tiffheader = new byte[ifdSize + maxAdditionalData];
            java.util.Arrays.fill(tiffheader, (byte) 0);
            System.arraycopy(basicHeader, 0, tiffheader, 0, basicHeader.length);

            // Additional data outside the IFD starts after the IFD's and pointer to the next IFD (0)
            additionalOffset = ifdSize;

            // Now work out the variable values from TIFF defaults,
            // PDF Defaults and the Dictionary for this XObject
            short cols = 1728;
            short rows = 0;
            short blackis1 = 0;
            short comptype = 3; // T4 compression
            long t4options = 0; // Will set if 1d or 2d T4

            COSArray decode = getDecode();
            // we have to invert the b/w-values,
            // if the Decode array exists and consists of (1,0)
            if (decode != null && decode.getInt(0) == 1)
            {
                blackis1 = 1;
            }
            COSBase dicOrArrayParms = options.getDictionaryObject(COSName.DECODE_PARMS);
            COSDictionary decodeParms = null;
            if (dicOrArrayParms instanceof COSDictionary)
            {
                decodeParms = (COSDictionary) dicOrArrayParms;
            }
            else
            {
                COSArray parmsArray = (COSArray) dicOrArrayParms;
                if (parmsArray.size() == 1)
                {
                    decodeParms = (COSDictionary) parmsArray.getObject(0);
                }
                else
                {
                    // else find the first dictionary with Row/Column info and use that.
                    for (int i = 0; i < parmsArray.size() && decodeParms == null; i++)
                    {
                        COSDictionary dic = (COSDictionary) parmsArray.getObject(i);
                        if (dic != null
                                && (dic.getDictionaryObject(COSName.COLUMNS) != null || dic
                                        .getDictionaryObject(COSName.ROWS) != null))
                        {
                            decodeParms = dic;
                        }
                    }
                }
            }

            if (decodeParms != null)
            {
                cols = (short) decodeParms.getInt(COSName.COLUMNS, cols);
                rows = (short) decodeParms.getInt(COSName.ROWS, rows);
                if (decodeParms.getBoolean(COSName.BLACK_IS_1, false))
                {
                    blackis1 = 1;
                }
                int k = decodeParms.getInt(COSName.K, 0); // Mandatory parm
                if (k < 0)
                {
                    // T6
                    comptype = 4;
                }
                if (k > 0)
                {
                    // T4 2D
                    comptype = 3;
                    t4options = 1;
                }
                // else k = 0, leave as default T4 1D compression
            }

            // If we couldn't get the number of rows, use the main item from XObject
            if (rows == 0)
            {
                rows = (short) options.getInt(COSName.HEIGHT, rows);
            }

            // Now put the tags into the tiffheader
            // These musn't exceed the maximum set above, and by TIFF spec should be sorted into
            // Numeric sequence.

            addTag(256, cols); // Columns
            addTag(257, rows); // Rows
            addTag(259, comptype); // T6
            addTag(262, blackis1); // Photometric Interpretation
            addTag(273, tiffheader.length); // Offset to start of image data - updated below
            addTag(279, options.getInt(COSName.LENGTH)); // Length of image data
            addTag(282, 300, 1); // X Resolution 300 (default unit Inches) This is arbitary
            addTag(283, 300, 1); // Y Resolution 300 (default unit Inches) This is arbitary
            if (comptype == 3)
            {
                addTag(292, t4options);
            }
            addTag(305, "PDFBOX"); // Software generating image
        }

        /* Tiff types 1 = byte, 2=ascii, 3=short, 4=ulong 5=rational */

        private void addTag(int tag, long value)
        {
            // Adds a tag of type 4 (ulong)
            int count = ++tiffheader[8];
            int offset = (count - 1) * 12 + 10;
            tiffheader[offset] = (byte) (tag & 0xff);
            tiffheader[offset + 1] = (byte) ((tag >> 8) & 0xff);
            tiffheader[offset + 2] = 4; // Type Long
            tiffheader[offset + 4] = 1; // One Value
            tiffheader[offset + 8] = (byte) (value & 0xff);
            tiffheader[offset + 9] = (byte) ((value >> 8) & 0xff);
            tiffheader[offset + 10] = (byte) ((value >> 16) & 0xff);
            tiffheader[offset + 11] = (byte) ((value >> 24) & 0xff);
        }

        private void addTag(int tag, short value)
        {
            // Adds a tag of type 3 (short)
            int count = ++tiffheader[8];
            int offset = (count - 1) * 12 + 10;
            tiffheader[offset] = (byte) (tag & 0xff);
            tiffheader[offset + 1] = (byte) ((tag >> 8) & 0xff);
            tiffheader[offset + 2] = 3; // Type Short
            tiffheader[offset + 4] = 1; // One Value
            tiffheader[offset + 8] = (byte) (value & 0xff);
            tiffheader[offset + 9] = (byte) ((value >> 8) & 0xff);
        }

        private void addTag(int tag, String value)
        {
            // Adds a tag of type 2 (ascii)
            int count = ++tiffheader[8];
            int offset = (count - 1) * 12 + 10;
            tiffheader[offset] = (byte) (tag & 0xff);
            tiffheader[offset + 1] = (byte) ((tag >> 8) & 0xff);
            tiffheader[offset + 2] = 2; // Type Ascii
            int len = value.length() + 1;
            tiffheader[offset + 4] = (byte) (len & 0xff);
            tiffheader[offset + 8] = (byte) (additionalOffset & 0xff);
            tiffheader[offset + 9] = (byte) ((additionalOffset >> 8) & 0xff);
            tiffheader[offset + 10] = (byte) ((additionalOffset >> 16) & 0xff);
            tiffheader[offset + 11] = (byte) ((additionalOffset >> 24) & 0xff);
            try
            {
                System.arraycopy(value.getBytes("US-ASCII"), 0, tiffheader, additionalOffset, value.length());
            }
            catch (UnsupportedEncodingException e)
            {
                throw new RuntimeException("Incompatible VM without US-ASCII encoding", e);
            }
            additionalOffset += len;
        }

        private void addTag(int tag, long numerator, long denominator)
        {
            // Adds a tag of type 5 (rational)
            int count = ++tiffheader[8];
            int offset = (count - 1) * 12 + 10;
            tiffheader[offset] = (byte) (tag & 0xff);
            tiffheader[offset + 1] = (byte) ((tag >> 8) & 0xff);
            tiffheader[offset + 2] = 5; // Type Rational
            tiffheader[offset + 4] = 1; // One Value
            tiffheader[offset + 8] = (byte) (additionalOffset & 0xff);
            tiffheader[offset + 9] = (byte) ((additionalOffset >> 8) & 0xff);
            tiffheader[offset + 10] = (byte) ((additionalOffset >> 16) & 0xff);
            tiffheader[offset + 11] = (byte) ((additionalOffset >> 24) & 0xff);
            tiffheader[additionalOffset++] = (byte) ((numerator) & 0xFF);
            tiffheader[additionalOffset++] = (byte) ((numerator >> 8) & 0xFF);
            tiffheader[additionalOffset++] = (byte) ((numerator >> 16) & 0xFF);
            tiffheader[additionalOffset++] = (byte) ((numerator >> 24) & 0xFF);
            tiffheader[additionalOffset++] = (byte) ((denominator) & 0xFF);
            tiffheader[additionalOffset++] = (byte) ((denominator >> 8) & 0xFF);
            tiffheader[additionalOffset++] = (byte) ((denominator >> 16) & 0xFF);
            tiffheader[additionalOffset++] = (byte) ((denominator >> 24) & 0xFF);
        }
    }
}

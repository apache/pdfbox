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
package org.apache.pdfbox.pdmodel.graphics.image;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Extends InputStream to wrap the data from the CCITT Fax with a suitable TIFF Header.
 * For details see www.tiff.org, which contains useful information including pointers
 * to the TIFF 6.0 Specification.
 *
 * @author Ben Litchfield
 * @author BenKing
 */
// TODO should this extend OutputStream instead?
public final class TIFFInputStream extends InputStream
{
    private static final List<String> FAX_FILTERS = new ArrayList<String>();
    static
    {
        FAX_FILTERS.add(COSName.CCITTFAX_DECODE.getName());
        FAX_FILTERS.add(COSName.CCITTFAX_DECODE_ABBREVIATION.getName());
    }

    private int currentOffset; // When reading, where in the tiffheader are we.
    private byte[] tiffheader; // Byte array to store tiff header data
    private InputStream datastream; // Original InputStream

    /**
     * Writes the TIFF image to an OutputStream.
     * 
     * @param image the image which data should be written
     * @param out the OutputStream to write to
     */
    // TODO this should be refactored
    public static void writeToOutputStream(PDImage image, OutputStream out) throws IOException
    {
        // We should use another format than TIFF to get rid of the TIFFInputStream
        InputStream faxInput = image.getStream().getPartiallyFilteredStream(FAX_FILTERS);
        COSStream cosStream = (COSStream)image.getStream().getCOSObject();

        InputStream data = new TIFFInputStream(faxInput, cosStream);
        IOUtils.copy(data, out);
    }

    private TIFFInputStream(InputStream rawstream, COSDictionary options)
    {
        buildHeader(options);
        currentOffset = 0;
        datastream = rawstream;
    }

    // Implement basic methods from InputStream
    public boolean markSupported()
    {
        return false;
    }

    public void reset() throws IOException
    {
        throw new IOException("reset not supported");
    }

    /**
     * For simple read, take a byte from the tiff header array or pass through.
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
     * For read methods only return as many bytes as we have left in the header if we've
     * exhausted the header, pass through to the InputStream of the raw CCITT data.
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
     * For read methods only return as many bytes as we have left in the header if we've
     * exhausted the header, pass  through to the InputStream of the raw CCITT data.
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
     * When skipping if any header data not yet read, only allow to
     * skip what we've in the buffer Otherwise just pass through.
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
    private final byte[] basicHeader =
            { 'I', 'I', 42, 0, 8, 0, 0, 0, // File introducer and pointer to first IFD
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

        // Additional data outside the IFD starts after the IFD's and pointer to the next IFD(0)
        additionalOffset = ifdSize;

        // Now work out the variable values from TIFF defaults,
        // PDF Defaults and the Dictionary for this XObject
        short cols = 1728;
        short rows = 0;
        short blackis1 = 0;
        short comptype = 3; // T4 compression
        long t4options = 0; // Will set if 1d or 2d T4

        COSArray decode = (COSArray)options.getDictionaryObject(COSName.DECODE);
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
            System.arraycopy(value.getBytes("US-ASCII"), 0, tiffheader,
                    additionalOffset, value.length());
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
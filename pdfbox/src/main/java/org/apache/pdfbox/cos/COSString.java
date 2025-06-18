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
package org.apache.pdfbox.cos;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.util.Hex;

/**
 * A string object, which may be a text string, a PDFDocEncoded string, ASCII string, or byte string.
 *
 * <p>Text strings are used for character strings that contain information intended to be
 * human-readable, such as text annotations, bookmark names, article names, document information,
 * and so forth.
 *
 * <p>PDFDocEncoded strings are used for characters that are represented in a single byte.
 *
 * <p>ASCII strings are used for characters that are represented in a single byte using ASCII
 * encoding.
 *
 * <p>Byte strings are used for binary data represented as a series of bytes, but the encoding is
 * not known. The bytes of the string need not represent characters.
 * 
 * @author Ben Litchfield
 * @author John Hewson
 */
public final class COSString extends COSBase
{
    private static final Log LOG = LogFactory.getLog(COSString.class);

    private byte[] bytes;
    private boolean forceHexForm;

    // legacy behaviour for old PDFParser
    public static final boolean FORCE_PARSING =
            Boolean.getBoolean("org.apache.pdfbox.forceParsing");

    /**
     * Creates a new PDF string from a byte array. This method can be used to read a string from an existing PDF file,
     * or to create a new byte string.
     *
     * @param bytes The raw bytes of the PDF text string or byte string.
     */
    public COSString(byte[] bytes)
    {
        this(bytes, false);
    }

    /**
     * Creates a new PDF string from a byte array. This method can be used to read a string from an existing PDF file,
     * or to create a new byte string.
     *
     * @param bytes The raw bytes of the PDF text string or byte string.
     * @param forceHex forces the hexadecimal presentation of the string if set to true
     * 
     */
    public COSString(byte[] bytes, boolean forceHex)
    {
        forceHexForm = forceHex;
        this.bytes = Arrays.copyOf(bytes, bytes.length);
    }

    /**
     * Creates a new <i>text string</i> from a Java String.
     *
     * @param text The string value of the object.
     */
    public COSString(String text)
    {
        this(text, false);
    }

    /**
     * Creates a new <i>text string</i> from a Java String.
     *
     * @param text The string value of the object.
     * @param forceHex forces the hexadecimal presentation of the string if set to true
     * 
     */
    public COSString(String text, boolean forceHex)
    {
        forceHexForm = forceHex;
        // check whether the string uses only characters available in PDFDocEncoding
        boolean isOnlyPDFDocEncoding = true;
        for (char c : text.toCharArray())
        {
            if (!PDFDocEncoding.containsChar(c))
            {
                isOnlyPDFDocEncoding = false;
                break;
            }
        }

        if (isOnlyPDFDocEncoding)
        {
            // PDFDocEncoded string
            bytes = PDFDocEncoding.getBytes(text);
        }
        else
        {
            // UTF-16BE encoded string with a leading byte order marker
            byte[] data = text.getBytes(StandardCharsets.UTF_16BE);
            bytes = new byte[data.length + 2];
            bytes[0] = (byte) 0xFE;
            bytes[1] = (byte) 0xFF;
            System.arraycopy(data, 0, bytes, 2, data.length);
        }
    }

    /**
     * This will create a COS string from a string of hex characters.
     *
     * @param hex A hex string.
     * @return A cos string with the hex characters converted to their actual bytes.
     * @throws IOException If there is an error with the hex string.
     */
    public static COSString parseHex(String hex) throws IOException
    {
        // skip leading and trailing whitespace
        int end = hex.length();
        while (end > 0 && Character.isWhitespace(hex.charAt(end - 1)))
        {
            end--;
        }
        int start = 0;
        while (start < end && Character.isWhitespace(hex.charAt(start)))
        {
            start++;
        }

        int length = end - start;
        ByteArrayOutputStream bytes = new ByteArrayOutputStream((length + 1) / 2);

        boolean isLengthUneven = length % 2 != 0;
        if (isLengthUneven)
        {
            length--;
        }
        for (int i = 0; i < length; i += 2)
        {
            int value = 16 * Hex.getHexValue(hex.charAt(i)) + Hex.getHexValue(hex.charAt(i + 1));
            if (value >= 0)
            {
                bytes.write(value);
            }
            else if (FORCE_PARSING)
            {
                LOG.warn("Encountered a malformed hex string");
                bytes.write('?'); // todo: what does Acrobat do? Any example PDFs?
            }
            else
            {
                throw new IOException("Invalid hex string: " + hex);
            }
        }
        if (isLengthUneven)
        {
            int value = 16 * Hex.getHexValue(hex.charAt(length));
            if (value >= 0)
            {
                bytes.write(value);
            }
            else if (FORCE_PARSING)
            {
                LOG.warn("Encountered a malformed hex string");
                bytes.write('?'); // todo: what does Acrobat do? Any example PDFs?
            }
            else
            {
                throw new IOException("Invalid hex string: " + hex);
            }
        }
        return new COSString(bytes.toByteArray());
    }

    /**
     * Sets the raw value of this string.
     *
     * @param value The raw bytes of the PDF text string or byte string.
     * 
     * @deprecated to be removed in a future release.
     */
    @Deprecated
    public void setValue(byte[] value)
    {
        bytes = Arrays.copyOf(value, value.length);
    }

    /**
     * Sets whether to force the string is to be written in hex form. This is needed when signing PDF files.
     *
     * @param value True to force hex.
     * 
     * @deprecated to be removed in a future release.
     */
    @Deprecated
    public void setForceHexForm(boolean value)
    {
        this.forceHexForm = value;
    }

    /**
     * Returns true if the string is to be written in hex form.
     * 
     * @return true if the COSString is written in hex form
     */
    public boolean getForceHexForm()
    {
        return forceHexForm;
    }

    /**
     * Returns the content of this string as a PDF <i>text string</i>.
     * 
     * @return the PDF string representation of the COSString
     */
    public String getString()
    {
        // text string - BOM indicates Unicode
        if (bytes.length >= 2)
        {
            if ((bytes[0] & 0xff) == 0xFE && (bytes[1] & 0xff) == 0xFF)
            {
                // UTF-16BE
                return new String(bytes, 2, bytes.length - 2, StandardCharsets.UTF_16BE);
            }
            else if ((bytes[0] & 0xff) == 0xFF && (bytes[1] & 0xff) == 0xFE)
            {
                // UTF-16LE - not in the PDF spec!
                return new String(bytes, 2, bytes.length - 2, StandardCharsets.UTF_16LE);
            }
        }

        // otherwise use PDFDocEncoding
        return PDFDocEncoding.toString(bytes);
    }

    /**
     * Returns the content of this string as a PDF <i>ASCII string</i>.
     * 
     * @return the ASCII string representation of the COSString
     */
    public String getASCII()
    {
        // ASCII string
        return new String(bytes, StandardCharsets.US_ASCII);
    }

    /**
     * Returns the raw bytes of the string using a new byte array. Best used with a PDF <i>byte string</i>.
     * 
     * @return a clone of the underlying byte[] representation of the COSString
     */
    public byte[] getBytes()
    {
        return Arrays.copyOf(bytes, bytes.length);
    }

    /**
     * This will take this string and create a hex representation of the bytes that make the string.
     *
     * @return A hex string representing the bytes in this string.
     */
    public String toHexString()
    {
        return Hex.getString(bytes);
    }

    /**
     * Visitor pattern double dispatch method.
     * 
     * @param visitor The object to notify when visiting this object.
     * @throws IOException If an error occurs while visiting this object.
     */
    @Override
    public void accept(ICOSVisitor visitor) throws IOException
    {
        visitor.visitFromString(this);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof COSString)
        {
            COSString strObj = (COSString) obj;
            return getString().equals(strObj.getString()) &&
                   forceHexForm == strObj.forceHexForm;
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        int result = Arrays.hashCode(bytes);
        return result + (forceHexForm ? 17 : 0);
    }

    @Override
    public String toString()
    {
        return "COSString{" + getString() + "}";
    }
}

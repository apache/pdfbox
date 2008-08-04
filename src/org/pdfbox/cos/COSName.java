/**
 * Copyright (c) 2003-2006, www.pdfbox.org
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of pdfbox; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://www.pdfbox.org
 *
 */
package org.pdfbox.cos;

import java.io.IOException;
import java.io.OutputStream;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.pdfbox.exceptions.COSVisitorException;
import org.pdfbox.persistence.util.COSHEXTable;


/**
 * This class represents a PDF named object.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.42 $
 */
public final class COSName extends COSBase implements Comparable
{
    /**
     * Note: This is synchronized because a HashMap must be synchronized if accessed by 
     * multiple threads.
     */
    private static Map nameMap = Collections.synchronizedMap( new HashMap(8192) );

    
    /**
     * A common COSName value.
     */
    public static final COSName A = new COSName( "A" );
    /**
     * A common COSName value.
     */
    public static final COSName AA = new COSName( "AA" );
    /**
    * A common COSName value.
    */
    public static final COSName ACRO_FORM = new COSName( "AcroForm" );
    /**
    * A common COSName value.
    */
    public static final COSName ANNOTS = new COSName( "Annots" );
    /**
     * A common COSName value.
     */
    public static final COSName ART_BOX = new COSName("ArtBox" );
    /**
    * A common COSName value.
    */
    public static final COSName ASCII85_DECODE = new COSName( "ASCII85Decode" );
    /**
    * A common COSName value.
    */
    public static final COSName ASCII85_DECODE_ABBREVIATION = new COSName( "A85" );
    /**
    * A common COSName value.
    */
    public static final COSName ASCII_HEX_DECODE = new COSName( "ASCIIHexDecode" );
    /**
    * A common COSName value.
    */
    public static final COSName ASCII_HEX_DECODE_ABBREVIATION = new COSName( "AHx" );
    /**
    * A common COSName value.
    */
    public static final COSName AP = new COSName( "AP" );
    /**
     * A common COSName value.
     */
    public static final COSName B = new COSName( "B" );
    /**
    * A common COSName value.
    */
    public static final COSName BASE_ENCODING = new COSName( "BaseEncoding" );
    /**
    * A common COSName value.
    */
    public static final COSName BASE_FONT = new COSName( "BaseFont" );
    /**
    * A common COSName value.
    */
    public static final COSName BBOX = new COSName( "BBox" );
    /**
     * A common COSName value.
     */
    public static final COSName BLEED_BOX = new COSName("BleedBox" );
    /**
    * A common COSName value.
    */
    public static final COSName CATALOG = new COSName( "Catalog" );
    /**
    * A common COSName value.
    */
    public static final COSName CALGRAY = new COSName( "CalGray" );
    /**
    * A common COSName value.
    */
    public static final COSName CALRGB = new COSName( "CalRGB" );
    /**
    * A common COSName value.
    */
    public static final COSName CCITTFAX_DECODE = new COSName( "CCITTFaxDecode" );
    /**
    * A common COSName value.
    */
    public static final COSName CCITTFAX_DECODE_ABBREVIATION = new COSName( "CCF" );
    /**
    * A common COSName value.
    */
    public static final COSName COLORSPACE = new COSName( "ColorSpace" );
    /**
    * A common COSName value.
    */
    public static final COSName CONTENTS = new COSName( "Contents" );
    /**
    * A common COSName value.
    */
    public static final COSName COUNT = new COSName( "Count" );
    /**
     * A common COSName value.
     */
    public static final COSName CROP_BOX = new COSName(  "CropBox" );
    /**
     * A common COSName value.
     */
    public static final COSName DCT_DECODE = new COSName( "DCTDecode" );
    /**
     * A common COSName value.
     */
    public static final COSName DCT_DECODE_ABBREVIATION = new COSName( "DCT" );
    /**
     * A common COSName value.
     */
    public static final COSName DESCENDANT_FONTS = new COSName(  "DescendantFonts" );
    /**
     * A common COSName value.
     */
    public static final COSName DEST = new COSName(  "Dest" );
    /**
    * A common COSName value.
    */
    public static final COSName DEVICECMYK = new COSName( "DeviceCMYK" );
    /**
    * A common COSName value.
    */
    public static final COSName DEVICEGRAY = new COSName( "DeviceGray" );
    /**
    * A common COSName value.
    */
    public static final COSName DEVICEN = new COSName( "DeviceN" );
    /**
    * A common COSName value.
    */
    public static final COSName DEVICERGB = new COSName( "DeviceRGB" );
    /**
     * A common COSName value.
     */
    public static final COSName DIFFERENCES = new COSName( "Differences" );
    /**
    * A common COSName value.
    */
    public static final COSName DV = new COSName( "DV" );
    /**
    * A common COSName value.
    */
    public static final COSName ENCODING = new COSName( "Encoding" );
    /**
     * A common COSName value.
     */
    public static final COSName ENCODING_90MS_RKSJ_H = new COSName( "90ms-RKSJ-H" );
    /**
     * A common COSName value.
     */
    public static final COSName ENCODING_90MS_RKSJ_V = new COSName( "90ms-RKSJ-V" );
    /**
     * A common COSName value.
     */
    public static final COSName ENCODING_ETEN_B5_H = new COSName( "ETen?B5?H" );
    /**
     * A common COSName value.
     */
    public static final COSName ENCODING_ETEN_B5_V = new COSName( "ETen?B5?V" );
    /**
     * A common COSName value.
     */
    public static final COSName FIELDS = new COSName( "Fields" );
    /**
    * A common COSName value.
    */
    public static final COSName FILTER = new COSName( "Filter" );
    /**
    * A common COSName value.
    */
    public static final COSName FIRST_CHAR = new COSName( "FirstChar" );
    /**
    * A common COSName value.
    */
    public static final COSName FLATE_DECODE = new COSName( "FlateDecode" );
    /**
    * A common COSName value.
    */
    public static final COSName FLATE_DECODE_ABBREVIATION = new COSName( "Fl" );
    /**
    * A common COSName value.
    */
    public static final COSName FONT = new COSName( "Font" );
    /**
     * A common COSName value.
     */
    public static final COSName FONT_FILE = new COSName("FontFile");
    /**
     * A common COSName value.
     */
    public static final COSName FONT_FILE2 = new COSName("FontFile2");
    /**
     * A common COSName value.
     */
    public static final COSName FONT_FILE3 = new COSName("FontFile3");
    /**
     * A common COSName value.
     */
    public static final COSName FONT_DESC = new COSName("FontDescriptor");
    /**
     * A common COSName value.
     */
    public static final COSName FONT_MATRIX = new COSName("FontMatrix" );
    /**
    * A common COSName value.
    */
    public static final COSName FORMTYPE = new COSName( "FormType" );
    /**
    * A common COSName value.
    */
    public static final COSName FRM = new COSName( "FRM" );
    /**
     * A common COSName value.
     */
     public static final COSName H = new COSName( "H" );
    /**
    * A common COSName value.
    */
    public static final COSName HEIGHT = new COSName( "Height" );
    /**
    * A common COSName value.
    */
    public static final COSName ICCBASED = new COSName( "ICCBased" );
    /**
    * A common COSName value.
    */
    public static final COSName IDENTITY_H = new COSName( "Identity-H" );
    /**
    * A common COSName value.
    */
    public static final COSName IMAGE = new COSName( "Image" );
    /**
    * A common COSName value.
    */
    public static final COSName INDEXED = new COSName( "Indexed" );
    /**
     * A common COSName value.
     */
    public static final COSName INFO = new COSName( "Info" );
    /**
    * A common COSName value.
    */
    public static final COSName JPX_DECODE = new COSName( "JPXDecode" );
    /**
    * A common COSName value.
    */
    public static final COSName KIDS = new COSName( "Kids" );
    /**
    * A common COSName value.
    */
    public static final COSName LAB = new COSName( "Lab" );
    /**
    * A common COSName value.
    */
    public static final COSName LAST_CHAR = new COSName( "LastChar" );
    /**
    * A common COSName value.
    */
    public static final COSName LENGTH = new COSName( "Length" );
    /**
     * A common COSName value.
     */
    public static final COSName LENGTH1 = new COSName( "Length1" );
    /**
    * A common COSName value.
    */
    public static final COSName LZW_DECODE = new COSName( "LZWDecode" );
    /**
    * A common COSName value.
    */
    public static final COSName LZW_DECODE_ABBREVIATION = new COSName( "LZW" );
    /**
    * A common COSName value.
    */
    public static final COSName MAC_ROMAN_ENCODING = new COSName( "MacRomanEncoding" );
    /**
    * A common COSName value.
    */
    public static final COSName MATRIX = new COSName( "Matrix" );
    /**
     * A common COSName value.
     */
    public static final COSName MEDIA_BOX = new COSName(  "MediaBox" );
    /**
     * A common COSName value.
     */
    public static final COSName METADATA = new COSName(  "Metadata" );
    /**
    * A common COSName value.
    */
    public static final COSName N = new COSName( "N" );
    /**
    * A common COSName value.
    */
    public static final COSName NAME = new COSName( "Name" );
    /**
    * A common COSName value.
    */
    public static final COSName P = new COSName( "P" );
    /**
    * A common COSName value.
    */
    public static final COSName PAGE = new COSName( "Page" );
    /**
    * A common COSName value.
    */
    public static final COSName PAGES = new COSName( "Pages" );
    /**
    * A common COSName value.
    */
    public static final COSName PARENT = new COSName( "Parent" );
    /**
    * A common COSName value.
    */
    public static final COSName PATTERN = new COSName( "Pattern" );
    /**
    * A common COSName value.
    */
    public static final COSName PDF_DOC_ENCODING = new COSName( "PDFDocEncoding" );
    /**
    * A common COSName value.
    */
    public static final COSName PREV = new COSName( "Prev" );
    /**
     * A common COSName value.
     */
     public static final COSName R = new COSName( "R" );
    /**
    * A common COSName value.
    */
    public static final COSName RESOURCES = new COSName( "Resources" );
    /**
    * A common COSName value.
    */
    public static final COSName ROOT = new COSName( "Root" );
    /**
     * A common COSName value.
     */
    public static final COSName ROTATE = new COSName(  "Rotate" );
    /**
    * A common COSName value.
    */
    public static final COSName RUN_LENGTH_DECODE = new COSName( "RunLengthDecode" );
    /**
    * A common COSName value.
    */
    public static final COSName RUN_LENGTH_DECODE_ABBREVIATION = new COSName( "RL" );
    /**
    * A common COSName value.
    */
    public static final COSName SEPARATION = new COSName( "Separation" );
    /**
    * A common COSName value.
    */
    public static final COSName STANDARD_ENCODING = new COSName( "StandardEncoding" );
    /**
    * A common COSName value.
    */
    public static final COSName SUBTYPE = new COSName( "Subtype" );
    /**
     * A common COSName value.
     */
    public static final COSName TRIM_BOX = new COSName("TrimBox" );
    /**
     * A common COSName value.
     */
    public static final COSName TRUE_TYPE = new COSName("TrueType" );
    /**
    * A common COSName value.
    */
    public static final COSName TO_UNICODE = new COSName( "ToUnicode" );
    /**
    * A common COSName value.
    */
    public static final COSName TYPE = new COSName( "Type" );
    /**
     * A common COSName value.
     */
    public static final COSName TYPE0 = new COSName(  "Type0" );
    /**
    * A common COSName value.
    */
    public static final COSName V = new COSName( "V" );
    /**
     * A common COSName value.
     */
     public static final COSName VERSION = new COSName( "Version" );
    /**
    * A common COSName value.
    */
    public static final COSName WIDTHS = new COSName( "Widths" );
    /**
    * A common COSName value.
    */
    public static final COSName WIN_ANSI_ENCODING = new COSName( "WinAnsiEncoding" );
    /**
    * A common COSName value.
    */
    public static final COSName XOBJECT = new COSName( "XObject" );
    
    /**
     * The prefix to a PDF name.
     */
    public static final byte[] NAME_PREFIX = new byte[] { 47  }; // The / character
    /**
     * The escape character for a name.
     */
    public static final byte[] NAME_ESCAPE = new byte[] { 35  };  //The # character

    private String name;
    private int hashCode;

    /**
     * This will get a COSName object with that name.
     *
     * @param aName The name of the object.
     *
     * @return A COSName with the specified name.
     */
    public static final COSName getPDFName( String aName )
    {
        COSName name = null;
        if( aName != null )
        {
            name = (COSName)nameMap.get( aName );
            if( name == null )
            {
                //name is added to map in the constructor
                name = new COSName( aName );
            }
        }
        return name;
    }

    /**
     * Private constructor.  This will limit the number of COSName objects.
     * that are created.
     *
     * @param aName The name of the COSName object.
     */
    private COSName( String aName )
    {
        name = aName;
        nameMap.put( aName, this );
        hashCode = name.hashCode();
    }

    /**
     * This will get the name of this COSName object.
     *
     * @return The name of the object.
     */
    public String getName()
    {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    public String toString()
    {
        return "COSName{" + name + "}";
    }

    /**
     * {@inheritDoc}
     */
    public boolean equals( Object o )
    {
        boolean retval = this == o;
        if( !retval && o instanceof COSName )
        {
            COSName other = (COSName)o;
            retval = name == other.name || name.equals( other.name );
        }
        return retval;
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode()
    {
        return hashCode;
    }
    
    /**
     * {@inheritDoc}
     */
    public int compareTo(Object o)
    {
        COSName other = (COSName)o;
        return this.name.compareTo( other.name );
    }



    /**
     * visitor pattern double dispatch method.
     *
     * @param visitor The object to notify when visiting this object.
     * @return any object, depending on the visitor implementation, or null
     * @throws COSVisitorException If an error occurs while visiting this object.
     */
    public Object accept(ICOSVisitor  visitor) throws COSVisitorException
    {
        return visitor.visitFromName(this);
    }
    
    /**
     * This will output this string as a PDF object.
     *  
     * @param output The stream to write to.
     * @throws IOException If there is an error writing to the stream.
     */
    public void writePDF( OutputStream output ) throws IOException
    {
        output.write(NAME_PREFIX);
        byte[] bytes = getName().getBytes();
        for (int i = 0; i < bytes.length;i++)
        {
            int current = ((bytes[i]+256)%256);

            if(current <= 32 || current >= 127 ||
               current == '(' ||
               current == ')' ||
               current == '[' ||
               current == ']' ||
               current == '/' ||
               current == '%' ||
               current == '<' ||
               current == '>' ||
               current == NAME_ESCAPE[0] )
            {
                output.write(NAME_ESCAPE);
                output.write(COSHEXTable.TABLE[current]);
            }
            else
            {
                output.write(current);
            }
        }
    }
}
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

import java.io.IOException;
import java.io.OutputStream;

import java.util.Collections;
import java.util.HashMap;
import java.util.WeakHashMap;
import java.util.Map;

import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.persistence.util.COSHEXTable;


/**
 * This class represents a PDF named object.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.42 $
 */
public final class COSName extends COSBase implements Comparable<COSName>
{
    /**
     * Note: This is synchronized because a HashMap must be synchronized if accessed by
     * multiple threads.
     */
    private static Map<String, COSName> nameMap =
        Collections.synchronizedMap( new WeakHashMap<String, COSName>(8192) );

    /**
     * All common COSName values are stored in a simple HashMap. They are already defined as
     * static constants and don't need to be synchronized for multithreaded environments.
     */
    private static Map<String, COSName> commonNameMap =
        new HashMap<String, COSName>();

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
     * "ActualText"
     */
    public static final COSName ACTUAL_TEXT = new COSName("ActualText");
    /**
     * "Alt"
     */
    public static final COSName ALT = new COSName("Alt");
    /**
    * A common COSName value.
    */
    public static final COSName ANNOTS = new COSName( "Annots" );
    /**
     * "Artifact"
     */
    public static final COSName ARTIFACT = new COSName("Artifact");
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
     * "Attached"
     */
    public static final COSName ATTACHED = new COSName("Attached");
    /**
    * A common COSName value.
    */
    public static final COSName ASCII_HEX_DECODE = new COSName( "ASCIIHexDecode" );
    /**
    * A common COSName value.
    */
    public static final COSName ASCII_HEX_DECODE_ABBREVIATION = new COSName( "AHx" );

    /** "AP" */
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
     * "C"
     */
    public static final COSName C = new COSName("C");
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
    public static final COSName CHAR_PROCS = new COSName( "CharProcs" );
    /**
    * A common COSName value.
    */
    public static final COSName CHAR_SET = new COSName( "CharSet" );
    /**
    * A common COSName value.
    */
    public static final COSName CID_FONT_TYPE0 = new COSName( "CIDFontType0" );
    /**
    * A common COSName value.
    */
    public static final COSName CID_FONT_TYPE2 = new COSName( "CIDFontType2" );
    /**
    * A common COSName value.
    */
    public static final COSName CIDSYSTEMINFO = new COSName( "CIDSystemInfo" );
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

    /** "D" */
    public static final COSName D = new COSName( "D" );

    /**
     * A common COSName value.
     */
    public static final COSName DCT_DECODE = new COSName( "DCTDecode" );
    /**
     * A common COSName value.
     */
    public static final COSName DCT_DECODE_ABBREVIATION = new COSName( "DCT" );

    /** "DecodeParms" */
    public static final COSName DECODE_PARMS = new COSName( "DecodeParms" );

    /**
     * A common COSName value.
     */
    public static final COSName DESCENDANT_FONTS = new COSName(  "DescendantFonts" );
    /**
     * A common COSName value.
     */
    public static final COSName DEST = new COSName(  "Dest" );

    /** "Dests" */
    public static final COSName DESTS = new COSName( "Dests" );

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

    /** "DL" */
    public static final COSName DL = new COSName( "DL" );

    /** "DP" */
    public static final COSName DP = new COSName( "DP" );

    /**
    * A common COSName value.
    */
    public static final COSName DV = new COSName( "DV" );
    /**
    * A common COSName value.
    */
    public static final COSName DW = new COSName( "DW" );

    /**
     * "E"
     */
    public static final COSName E = new COSName("E");

    /** "EmbeddedFiles" */
    public static final COSName EMBEDDED_FILES = new COSName( "EmbeddedFiles" );

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

    /** "Encrypt" */
    public static final COSName ENCRYPT = new COSName( "Encrypt" );

    /** "ExtGState" */
    public static final COSName EXT_G_STATE = new COSName( "ExtGState" );

    /** "Extends" */
    public static final COSName EXTENDS = new COSName( "Extends" );

    /** "F" */
    public static final COSName F = new COSName( "F" );

    /** "FDecodeParms" */
    public static final COSName F_DECODE_PARMS = new COSName( "FDecodeParms" );

    /** "FFilter" */
    public static final COSName F_FILTER = new COSName( "FFilter" );

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
    public static final COSName FONT_BBOX = new COSName( "FontBBox" );
    /**
     * A common COSName value.
     */
    public static final COSName FONT_FAMILY = new COSName("FontFamily");
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
    public static final COSName FONT_NAME = new COSName("FontName" );
    /**
    * A common COSName value.
    */
    public static final COSName FONT_STRETCH = new COSName("FontStretch" );
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

    /** "ID" */
    public static final COSName ID = new COSName("ID");

    /** "IDTree" */
    public static final COSName ID_TREE = new COSName("IDTree");

    /**
    * A common COSName value.
    */
    public static final COSName IDENTITY_H = new COSName( "Identity-H" );
    /**
    * A common COSName value.
    */
    public static final COSName IMAGE = new COSName( "Image" );

    /** "Index" */
    public static final COSName INDEX = new COSName( "Index" );

    /**
    * A common COSName value.
    */
    public static final COSName INDEXED = new COSName( "Indexed" );
    /**
     * A common COSName value.
     */
    public static final COSName INFO = new COSName( "Info" );

    /** "JavaScript" */
    public static final COSName JAVA_SCRIPT = new COSName( "JavaScript" );

    /**
    * A common COSName value.
    */
    public static final COSName JPX_DECODE = new COSName( "JPXDecode" );

    /** "K" */
    public static final COSName K = new COSName("K");

    /** "Kids" */
    public static final COSName KIDS = new COSName( "Kids" );

    /**
    * A common COSName value.
    */
    public static final COSName LAB = new COSName( "Lab" );

    /** "Lang" */
    public static final COSName LANG = new COSName("Lang");

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

    /** "Limits" */
    public static final COSName LIMITS = new COSName( "Limits" );

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

    /** "MarkInfo" */
    public static final COSName MARK_INFO = new COSName("MarkInfo");

    /**
    * A common COSName value.
    */
    public static final COSName MATRIX = new COSName( "Matrix" );
    /**
     * "MCID"
     */
    public static final COSName MCID = new COSName("MCID");
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
    public static final COSName MM_TYPE1 = new COSName(  "MMType1" );
    /**
    * A common COSName value.
    */
    public static final COSName N = new COSName( "N" );
    /**
    * A common COSName value.
    */
    public static final COSName NAME = new COSName( "Name" );

    /** "Names" */
    public static final COSName NAMES = new COSName( "Names" );

    /** "Numbs" */
    public static final COSName NUMS = new COSName( "Nums" );

    /**
     * "O"
     */
    public static final COSName O = new COSName("O");
    
    /**
     * "Obj"
     */
    public static final COSName OBJ = new COSName("Obj");

    /** "Outlines" */
    public static final COSName OUTLINES = new COSName("Outlines");

    /** "OpenAction" */
    public static final COSName OPEN_ACTION = new COSName("OpenAction");

    /**
     * A common COSName value.
     */
     public static final COSName ORDERING = new COSName( "Ordering" );
     /**
      * A common COSName value.
      */
      public static final COSName P = new COSName( "P" );
    /**
    * A common COSName value.
    */
    public static final COSName PAGE = new COSName( "Page" );

    /** "PageLabels" */
    public static final COSName PAGE_LABELS = new COSName("PageLabels");

    /** "PageLayout" */
    public static final COSName PAGE_LAYOUT = new COSName("PageLayout");

    /** "PageMode" */
    public static final COSName PAGE_MODE = new COSName("PageMode");

    /**
    * A common COSName value.
    */
    public static final COSName PAGES = new COSName( "Pages" );
    /**
    * A common COSName value.
    */
    public static final COSName PARENT = new COSName( "Parent" );
    /**
     * "ParentTreeNextKey"
     */
    public static final COSName PARENT_TREE_NEXT_KEY = new COSName("ParentTreeNextKey");
    /**
    * A common COSName value.
    */
    public static final COSName PATTERN = new COSName( "Pattern" );
    /**
    * A common COSName value.
    */
    public static final COSName PDF_DOC_ENCODING = new COSName( "PDFDocEncoding" );
    /**
     * "Pg"
     */
    public static final COSName PG = new COSName("Pg");
    /**
    * A common COSName value.
    */
    public static final COSName PREV = new COSName( "Prev" );

    /** "ProcSet" */
    public static final COSName PROC_SET = new COSName( "ProcSet" );

    /**
     * A common COSName value.
     */
    public static final COSName R = new COSName( "R" );
     /**
      * A common COSName value.
      */
    public static final COSName REGISTRY = new COSName( "Registry" );
    /**
    * A common COSName value.
    */
    public static final COSName RESOURCES = new COSName( "Resources" );
    /**
     * "RoleMap"
     */
    public static final COSName ROLE_MAP = new COSName("RoleMap");
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
     * "S"
     */
    public static final COSName S = new COSName("S");
    /**
    * A common COSName value.
    */
    public static final COSName SEPARATION = new COSName( "Separation" );

    /** "Shading" */
    public static final COSName SHADING = new COSName( "Shading" );
    
    /** "Size" */
    public static final COSName SIZE = new COSName( "Size" );

    /**
    * A common COSName value.
    */
    public static final COSName STANDARD_ENCODING = new COSName( "StandardEncoding" );

    /** "StructTreeRoot" */
    public static final COSName STRUCT_TREE_ROOT = new COSName("StructTreeRoot");

    /**
    * A common COSName value.
    */
    public static final COSName SUBTYPE = new COSName( "Subtype" );

    /**
     * "T"
     */
    public static final COSName T = new COSName("T");

    /** "Threads" */
    public static final COSName THREADS = new COSName("Threads");

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
    public static final COSName TYPE1 = new COSName(  "Type1" );
    /**
    * A common COSName value.
    */
    public static final COSName TYPE3 = new COSName(  "Type3" );

    /** "URI" */
    public static final COSName URI = new COSName("URI");

    /**
    * A common COSName value.
    */
    public static final COSName V = new COSName( "V" );
    /**
     * A common COSName value.
     */
    public static final COSName VERSION = new COSName( "Version" );

    /** "ViewerPreferences" */
    public static final COSName VIEWER_PREFERENCES = new COSName("ViewerPreferences");

    /**
     * A common COSName value.
     */
    public static final COSName W = new COSName( "W" );
    /**
     * A common COSName value.
     */
    public static final COSName WIDTHS = new COSName( "Widths" );
    /**
    * A common COSName value.
    */
    public static final COSName WIN_ANSI_ENCODING = new COSName( "WinAnsiEncoding" );

    /** "XObject" */
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
            // Is it a common COSName ??
            name = commonNameMap.get( aName );
            if( name == null )
            {
                // It seems to be a document specific COSName
                name = nameMap.get( aName );
                if( name == null )
                {
                    //name is added to the synchronized map in the constructor
                    name = new COSName( aName, false );
                }
            }
        }
        return name;
    }

    /**
     * Private constructor.  This will limit the number of COSName objects.
     * that are created.
     *
     * @param aName The name of the COSName object.
     * @param staticValue Indicates if the COSName object is static so that it can 
     *        be stored in the HashMap without synchronizing.
     */
    private COSName( String aName, boolean staticValue )
    {
        name = aName;
        if ( staticValue )
        {
            commonNameMap.put( aName, this);
        }
        else
        {
            nameMap.put( aName, this );
        }
        hashCode = name.hashCode();
    }

    /**
     * Private constructor.  This will limit the number of COSName objects.
     * that are created.
     *
     * @param aName The name of the COSName object.
     */
    private COSName( String aName )
    {
        this( aName, true );
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
    public int compareTo(COSName other)
    {
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

     /**
      * Not usually needed except if resources need to be reclaimed in a ong
      * running process.
      * Patch provided by flester@GMail.com
      * incorporated 5/23/08, Danielwilson@users.SourceForge.net
      */
     public static synchronized void clearResources()
     {
         // Clear them all
         nameMap.clear();
     }
}

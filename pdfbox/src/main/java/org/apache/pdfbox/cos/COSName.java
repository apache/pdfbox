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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
     * Note: This is a ConcurrentHashMap because a HashMap must be synchronized if accessed by
     * multiple threads.
     */
    private static Map<String, COSName> nameMap = new ConcurrentHashMap<String, COSName>(8192);

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
     * A common COSName value.
     */
    public static final COSName AIS = new COSName( "AIS" );
    /**
     * "Alt"
     */
    public static final COSName ALT = new COSName("Alt");
    /**
     * "Alt"
     */
    public static final COSName ALTERNATE = new COSName("Alternate");
    /**
    * A common COSName value.
    */
    public static final COSName ANNOT = new COSName( "Annot" );
    /**
     * A common COSName value.
     */
    public static final COSName ANNOTS = new COSName( "Annots" );
    /**
     * A common COSName value.
     */
    public static final COSName ANTI_ALIAS = new COSName( "AntiAlias" );
    /**
     * A common COSName value.
     */
    public static final COSName AP_REF = new COSName( "APRef" );
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
    public static final COSName AS = new COSName( "AS" );
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
    public static final COSName ASCENT = new COSName( "Ascent" );
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
    public static final COSName AUTHOR = new COSName( "Author" );

    /**
     * A common COSName value.
     */
    public static final COSName AVG_WIDTH = new COSName( "AvgWidth" );

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

    /** the COSName for "BaseState". */
    public static final COSName BASE_STATE = new COSName( "BaseState" );

    /**
     * A common COSName value.
     */
    public static final COSName BBOX = new COSName( "BBox" );
    /**
     * A common COSName value.
     */
    public static final COSName BLACK_IS_1 = new COSName( "BlackIs1" );
    /**
     * A common COSName value.
     */
    public static final COSName BLACK_POINT = new COSName( "BlackPoint" );

    /**
     * A common COSName value.
     */
    public static final COSName BLEED_BOX = new COSName("BleedBox" );
    /**
     * A common COSName value.
     */
    public static final COSName BITS_PER_COMPONENT = new COSName("BitsPerComponent" );
    /**
     * A common COSName value.
     */
    public static final COSName BITS_PER_SAMPLE = new COSName("BitsPerSample" );
    /**
     * A common COSName value.
     */
    public static final COSName BOUNDS = new COSName("Bounds" );
    /**
     * A common COSName value.
     */
    public static final COSName BPC = new COSName("BPC" );
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
    public static final COSName C0 = new COSName( "C0" );
    /**
     * A common COSName value.
     */
    public static final COSName C1 = new COSName( "C1" );
    /**
     * A common COSName value.
     */
    public static final COSName CA = new COSName( "CA" );
    /**
     * A common COSName value.
     */
    public static final COSName CA_NS = new COSName( "ca" );
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
    public static final COSName CAP_HEIGHT = new COSName( "CapHeight" );
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
    public static final COSName CF = new COSName( "CF" );
    /**
     * A common COSName value.
     */
    public static final COSName CFM = new COSName( "CFM" );
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
    public static final COSName COLORANTS = new COSName( "Colorants" );
    /**
     * A common COSName value.
     */
    public static final COSName COLORS = new COSName( "Colors" );
    /**
     * A common COSName value.
     */
    public static final COSName COLORSPACE = new COSName( "ColorSpace" );
    /**
     * A common COSName value.
     */
    public static final COSName COLUMNS = new COSName( "Columns" );
    /**
     * A common COSName value.
     */
    public static final COSName CONTENTS = new COSName( "Contents" );
     /**
      * A common COSName value.
      */
    public static final COSName COORDS = new COSName( "Coords" );
    /**
    * A common COSName value.
    */
    public static final COSName COUNT = new COSName( "Count" );
    /**
     * A common COSName value.
     */
    public static final COSName CLR_F = new COSName( "ClrF" );
    /**
     * A common COSName value.
     */
    public static final COSName CLR_FF = new COSName( "ClrFf" );
    /**
     * A common COSName value.
     */
    public static final COSName CREATION_DATE = new COSName( "CreationDate" );
    /**
     * A common COSName value.
     */
    public static final COSName CREATOR = new COSName( "Creator" );
    /**
     * A common COSName value.
     */
    public static final COSName CROP_BOX = new COSName(  "CropBox" );
    /**
     * The Crypt filter.
     */
    public static final COSName CRYPT = new COSName( "Crypt" );
    /**
     * A common COSName value.
     */
    public static final COSName CS = new COSName(  "CS" );

    /** "D" */
    public static final COSName D = new COSName( "D" );

    /**
     * A common COSName value.
     */
    public static final COSName DA = new COSName( "DA" );
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
    public static final COSName DECODE = new COSName( "Decode" );
    /** "DecodeParms" */
    public static final COSName DECODE_PARMS = new COSName( "DecodeParms" );

    /**
     * A common COSName value.
     */
    public static final COSName DESCENT = new COSName(  "Descent" );
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

    /**
     * A common COSName value.
     */
    public static final COSName DOMAIN = new COSName( "Domain" );

     /** "DP" */
    public static final COSName DP = new COSName( "DP" );

    /**
     * A common COSName value.
     */
    public static final COSName DR = new COSName( "DR" );
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
    public static final COSName ENCODE = new COSName( "Encode" );
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

    /** "EncryptMetaData" */
    public static final COSName ENCRYPT_META_DATA = new COSName( "EncryptMetadata" );
    
    /** "ExtGState" */
    public static final COSName EXT_G_STATE = new COSName( "ExtGState" );

    /**
     * A common COSName value.
     */
    public static final COSName EXTEND = new COSName( "Extend" );

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
    public static final COSName FF = new COSName( "Ff" );
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
    public static final COSName FL = new COSName( "FL" );
    /**
     * A common COSName value.
     */
    public static final COSName FLAGS = new COSName( "Flags" );
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
    public static final COSName FONT_WEIGHT = new COSName("FontWeight" );
    /**
    * A common COSName value.
    */
    public static final COSName FORM = new COSName( "Form" );
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
    public static final COSName FT = new COSName( "FT" );
    /**
     * A common COSName value.
     */
    public static final COSName FUNCTION = new COSName( "Function" );
    /**
     * A common COSName value.
     */
    public static final COSName FUNCTION_TYPE = new COSName( "FunctionType" );
    /**
     * A common COSName value.
     */
    public static final COSName FUNCTIONS = new COSName( "Functions" );
    /**
     * A common COSName value.
     */
    public static final COSName GAMMA = new COSName( "Gamma" );
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
    public static final COSName IDENTITY = new COSName( "Identity" );
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
    public static final COSName IMAGE_MASK = new COSName( "ImageMask" );

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
    /**
     * A common COSName value.
     */
    public static final COSName ITALIC_ANGLE = new COSName( "ItalicAngle" );

    /** "JavaScript" */
    public static final COSName JAVA_SCRIPT = new COSName( "JavaScript" );

    /**
     * A common COSName value.
     */
    public static final COSName JBIG2_DECODE = new COSName( "JBIG2Decode" );
    /**
    * A common COSName value.
    */
    public static final COSName JPX_DECODE = new COSName( "JPXDecode" );

    /** "K" */
    public static final COSName K = new COSName("K");

    /**
     * A common COSName value.
     */
    public static final COSName KEYWORDS = new COSName( "Keywords" );

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
    public static final COSName LC = new COSName( "LC" );
    /**
     * A common COSName value.
     */
    public static final COSName LEADING = new COSName( "Leading" );
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
    public static final COSName LJ = new COSName( "LJ" );
    /**
    * A common COSName value.
    */
    public static final COSName LW = new COSName( "LW" );
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
    public static final COSName M = new COSName( "M" );
    /**
    * A common COSName value.
    */
    public static final COSName MAC_ROMAN_ENCODING = new COSName( "MacRomanEncoding" );

    /** "MarkInfo" */
    public static final COSName MARK_INFO = new COSName("MarkInfo");

    /**
     * A common COSName value.
     */
    public static final COSName MASK = new COSName( "Mask" );
    /**
     * A common COSName value.
     */
    public static final COSName MATRIX = new COSName( "Matrix" );
    /**
     * A common COSName value.
     */
    public static final COSName MAX_WIDTH = new COSName( "MaxWidth" );
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
    public static final COSName MISSING_WIDTH = new COSName( "MissingWidth" );
    /**
     * A common COSName value.
     */
    public static final COSName ML = new COSName( "ML" );
    /**
    * A common COSName value.
    */
    public static final COSName MM_TYPE1 = new COSName(  "MMType1" );
    /**
     * A common COSName value.
     */
    public static final COSName MOD_DATE = new COSName(  "ModDate" );
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

    /**
     * A common COSName value.
     */
    public static final COSName NEXT = new COSName( "Next" );
    /**
     * A common COSName value.
     */
    public static final COSName NM = new COSName( "NM" );
    /** "Nums" */
    public static final COSName NUMS = new COSName( "Nums" );

    /**
     * "O"
     */
    public static final COSName O = new COSName("O");

    /**
     * "Obj"
     */
    public static final COSName OBJ = new COSName("Obj");

    /**
     * A common COSName value.
     */
    public static final COSName OBJ_STM = new COSName( "ObjStm" );

    /** the COSName for the content group tag. */
    public static final COSName OC = new COSName("OC");
    /** the COSName for an optional content group. */
    public static final COSName OCG = new COSName("OCG");
    /** the COSName for the optional content group list. */
    public static final COSName OCGS = new COSName("OCGs");
    /** the COSName for the optional content properties. */
    public static final COSName OCPROPERTIES = new COSName("OCProperties");

    /** the COSName for the "OFF" value. */
    public static final COSName OFF = new COSName("OFF");
    /** the COSName for the "ON" value. */
    public static final COSName ON = new COSName("ON");

    /**
     * A common COSName value.
     */
    public static final COSName OP = new COSName( "OP" );
    /**
     * A common COSName value.
     */
    public static final COSName OP_NS = new COSName( "op" );
    /**
     * A common COSName value.
     */
    public static final COSName OPM = new COSName( "OPM" );
    /**
     * A common COSName value.
     */
    public static final COSName OPT = new COSName( "Opt" );
    /** "Outlines" */
    public static final COSName OUTLINES = new COSName("Outlines");

    /** "OpenAction" */
    public static final COSName OPEN_ACTION = new COSName("OpenAction");

    /** A common COSName value. */
     public static final COSName ORDER = new COSName( "Order" );

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
    public static final COSName PAINT_TYPE = new COSName( "PaintType" );
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
    public static final COSName PATTERN_TYPE = new COSName( "PatternType" );
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
    public static final COSName PREDICTOR = new COSName( "Predictor" );
    /**
     * A common COSName value.
     */
    public static final COSName PREV = new COSName( "Prev" );

    /** The COSName value for "ProcSet". */
    public static final COSName PROC_SET = new COSName( "ProcSet" );

    /**
     * A common COSName value.
     */
    public static final COSName PRODUCER = new COSName( "Producer" );

    /** The COSName value for "Properties". */
    public static final COSName PROPERTIES = new COSName( "Properties" );

    /**
     * A common COSName value.
     */
    public static final COSName Q = new COSName( "Q" );
    /**
     * A common COSName value.
     */
    public static final COSName R = new COSName( "R" );
    /**
     * A common COSName value.
     */
    public static final COSName RANGE = new COSName( "Range" );
    /**
     * A common COSName value.
     */
    public static final COSName RECIPIENTS = new COSName( "Recipients" );
    /**
     * A common COSName value.
     */
    public static final COSName RECT = new COSName( "Rect" );
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
    public static final COSName RI = new COSName( "RI" );
    /**
     * A common COSName value.
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
    public static final COSName ROWS = new COSName(  "Rows" );
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
    public static final COSName RV = new COSName( "RV" );
    /**
     * "S"
     */
    public static final COSName S = new COSName("S");
    /**
     * A common COSName value.
     */
    public static final COSName SA = new COSName( "SA" );
    /**
     * A common COSName value.
     */
    public static final COSName SE = new COSName( "SE" );
    /**
     * A common COSName value.
     */
    public static final COSName SEPARATION = new COSName( "Separation" );
    /**
     * A common COSName value.
     */
    public static final COSName SET_F = new COSName( "SetF" );
    /**
     * A common COSName value.
     */
    public static final COSName SET_FF = new COSName( "SetFf" );

    /** "Shading" */
    public static final COSName SHADING = new COSName( "Shading" );
    /**
     * A common COSName value.
     */
    public static final COSName SHADING_TYPE = new COSName( "ShadingType" );
    /**
     * A common COSName value.
     */
    public static final COSName SM = new COSName( "SM" );
    /**
     * A common COSName value.
     */
    public static final COSName SMASK = new COSName( "SMask" );
    /** "Size" */
    public static final COSName SIZE = new COSName( "Size" );

    /**
    * A common COSName value.
    */
    public static final COSName STANDARD_ENCODING = new COSName( "StandardEncoding" );
    /**
     * A common COSName value.
     */
    public static final COSName STD_CF = new COSName( "StdCF" );
    /**
     * A common COSName value.
     */
    public static final COSName STEM_H = new COSName( "StemH" );
    /**
     * A common COSName value.
     */
    public static final COSName STEM_V = new COSName( "StemV" );
    /**
     * A common COSName value.
     */
    public static final COSName STM_F = new COSName( "StmF" );
    /**
     * A common COSName value.
     */
    public static final COSName STR_F = new COSName( "StrF" );

    /** "StructTreeRoot" */
    public static final COSName STRUCT_TREE_ROOT = new COSName("StructTreeRoot");

    /**
     * A common COSName value.
     */
    public static final COSName SUB_FILTER = new COSName( "SubFilter" );
    /**
    * A common COSName value.
    */
    public static final COSName SUBJ = new COSName( "Subj" );
    /**
     * A common COSName value.
     */
    public static final COSName SUBJECT = new COSName( "Subject" );
    /**
     * A common COSName value.
     */
    public static final COSName SUPPLEMENT = new COSName( "Supplement" );
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
    public static final COSName TILING_TYPE = new COSName( "TilingType" );
    /**
     * A common COSName value.
     */
    public static final COSName TITLE = new COSName( "Title" );
    /**
     * A common COSName value.
     */
    public static final COSName TK = new COSName( "TK" );
    /**
     * A common COSName value.
     */
    public static final COSName TRAPPED = new COSName("Trapped" );
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
    public static final COSName TU = new COSName("TU" );
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

    /**
     * A common COSName value.
     */
    public static final COSName U = new COSName( "U" );
    /** the COSName for the "Unchanged" value. */
    public static final COSName UNCHANGED = new COSName("Unchanged");
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
    public static final COSName WIDTH = new COSName( "Width" );
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
    public static final COSName WHITE_POINT = new COSName( "WhitePoint" );

    /**
     * A common COSName value.
     */
    public static final COSName XHEIGHT = new COSName( "XHeight" );

    /** "XObject" */
    public static final COSName XOBJECT = new COSName( "XObject" );
    /**
     * A common COSName value.
     */
    public static final COSName XREF = new COSName( "XRef" );
    /**
     * A common COSName value.
     */
    public static final COSName X_STEP = new COSName( "XStep" );
    /**
     * A common COSName value.
     */
    public static final COSName Y_STEP = new COSName( "YStep" );
    /**
     * The prefix to a PDF name.
     */
    public static final byte[] NAME_PREFIX = new byte[] { 47  }; // The / character
    /**
     * The escape character for a name.
     */
    public static final byte[] NAME_ESCAPE = new byte[] { 35  };  //The # character

    /**
     * A common COSName value.
     */
    public static final COSName SUBFILTER = new COSName("SubFilter");
    /**
     * A signature filter value.
     */
    public static final COSName ADOBE_PPKLITE = new COSName("Adobe.PPKLite");
    /**
     * A signature filter value.
     */
    public static final COSName ENTRUST_PPKEF = new COSName("Entrust.PPKEF");
    /**
     * A signature filter value.
     */
    public static final COSName CICI_SIGNIT = new COSName("CICI.SignIt");
    /**
     * A signature filter value.
     */
    public static final COSName VERISIGN_PPKVS = new COSName("VeriSign.PPKVS");
    /**
     * A signature subfilter value.
     */
    public static final COSName ADBE_X509_RSA_SHA1 = new COSName("adbe.x509.rsa_sha1");
    /**
     * A signature subfilter value.
     */
    public static final COSName ADBE_PKCS7_DETACHED = new COSName("adbe.pkcs7.detached");
    /**
     * A signature subfilter value.www
     */
    public static final COSName ADBE_PKCS7_SHA1 = new COSName("adbe.pkcs7.sha1");
    /**
     * A common COSName value.
     */
    public static final COSName LOCATION = new COSName("Location");
    /**
     * A common COSName value.
     */
    public static final COSName REASON = new COSName("Reason");
    /**
     * A common COSName value.
     */
    public static final COSName BYTERANGE = new COSName("ByteRange");
    /**
     * A common COSName value.
     */
    public static final COSName SIG = new COSName("Sig");
    /**
     * A common COSName value.
     */
    public static final COSName SIG_FLAGS = new COSName("SigFlags");

    
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
        byte[] bytes = getName().getBytes("ISO-8859-1");
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

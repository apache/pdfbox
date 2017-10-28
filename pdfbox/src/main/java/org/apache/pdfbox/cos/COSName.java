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

import org.apache.pdfbox.util.Charsets;
import org.apache.pdfbox.util.Hex;

/**
 * A PDF Name object.
 *
 * @author Ben Litchfield
 */
public final class COSName extends COSBase implements Comparable<COSName>
{
    // using ConcurrentHashMap because this can be accessed by multiple threads
    private static Map<String, COSName> nameMap = new ConcurrentHashMap<>(8192);

    // all common COSName values are stored in this HashMap
    // they are already defined as static constants and don't need to be synchronized
    private static Map<String, COSName> commonNameMap = new HashMap<>(768);

    //
    // IMPORTANT: this list is *alphabetized* and does not need any JavaDoc
    //

    // A
    public static final COSName A = new COSName("A");
    public static final COSName AA = new COSName("AA");
    public static final COSName ACRO_FORM = new COSName("AcroForm");
    public static final COSName ACTUAL_TEXT = new COSName("ActualText");
    public static final COSName ADBE_PKCS7_DETACHED = new COSName("adbe.pkcs7.detached");
    public static final COSName ADBE_PKCS7_SHA1 = new COSName("adbe.pkcs7.sha1");
    public static final COSName ADBE_X509_RSA_SHA1 = new COSName("adbe.x509.rsa_sha1");
    public static final COSName ADOBE_PPKLITE = new COSName("Adobe.PPKLite");
    public static final COSName AESV2 = new COSName("AESV2");
    public static final COSName AESV3 = new COSName("AESV3");
    public static final COSName AFTER = new COSName("After");
    public static final COSName AIS = new COSName("AIS");
    public static final COSName ALT = new COSName("Alt");
    public static final COSName ALPHA = new COSName("Alpha");
    public static final COSName ALTERNATE = new COSName("Alternate");
    public static final COSName ANNOT = new COSName("Annot");
    public static final COSName ANNOTS = new COSName("Annots");
    public static final COSName ANTI_ALIAS = new COSName("AntiAlias");
    public static final COSName AP = new COSName("AP");
    public static final COSName AP_REF = new COSName("APRef");
    public static final COSName APP = new COSName("App");
    public static final COSName ART_BOX = new COSName("ArtBox");
    public static final COSName ARTIFACT = new COSName("Artifact");
    public static final COSName AS = new COSName("AS");
    public static final COSName ASCENT = new COSName("Ascent");
    public static final COSName ASCII_HEX_DECODE = new COSName("ASCIIHexDecode");
    public static final COSName ASCII_HEX_DECODE_ABBREVIATION = new COSName("AHx");
    public static final COSName ASCII85_DECODE = new COSName("ASCII85Decode");
    public static final COSName ASCII85_DECODE_ABBREVIATION = new COSName("A85");
    public static final COSName ATTACHED = new COSName("Attached");
    public static final COSName AUTHOR = new COSName("Author");
    public static final COSName AVG_WIDTH = new COSName("AvgWidth");
    // B
    public static final COSName B = new COSName("B");
    public static final COSName BACKGROUND = new COSName("Background");
    public static final COSName BASE_ENCODING = new COSName("BaseEncoding");
    public static final COSName BASE_FONT = new COSName("BaseFont");
    public static final COSName BASE_STATE = new COSName("BaseState");
    public static final COSName BBOX = new COSName("BBox");
    public static final COSName BC = new COSName("BC");
    public static final COSName BE = new COSName("BE");
    public static final COSName BEFORE = new COSName("Before");
    public static final COSName BG = new COSName("BG");
    public static final COSName BITS_PER_COMPONENT = new COSName("BitsPerComponent");
    public static final COSName BITS_PER_COORDINATE = new COSName("BitsPerCoordinate");
    public static final COSName BITS_PER_FLAG = new COSName("BitsPerFlag");
    public static final COSName BITS_PER_SAMPLE = new COSName("BitsPerSample");
    public static final COSName BLACK_IS_1 = new COSName("BlackIs1");
    public static final COSName BLACK_POINT = new COSName("BlackPoint");
    public static final COSName BLEED_BOX = new COSName("BleedBox");
    public static final COSName BM = new COSName("BM");
    public static final COSName BORDER = new COSName("Border");
    public static final COSName BOUNDS = new COSName("Bounds");
    public static final COSName BPC = new COSName("BPC");
    public static final COSName BS = new COSName("BS");
    //** Acro form field type for button fields.
    public static final COSName BTN = new COSName("Btn");
    public static final COSName BYTERANGE = new COSName("ByteRange");
    // C
    public static final COSName C = new COSName("C");
    public static final COSName C0 = new COSName("C0");
    public static final COSName C1 = new COSName("C1");
    public static final COSName CA = new COSName("CA");
    public static final COSName CA_NS = new COSName("ca");
    public static final COSName CALGRAY = new COSName("CalGray");
    public static final COSName CALRGB = new COSName("CalRGB");
    public static final COSName CAP = new COSName("Cap");
    public static final COSName CAP_HEIGHT = new COSName("CapHeight");
    public static final COSName CATALOG = new COSName("Catalog");
    public static final COSName CCITTFAX_DECODE = new COSName("CCITTFaxDecode");
    public static final COSName CCITTFAX_DECODE_ABBREVIATION = new COSName("CCF");
    public static final COSName CENTER_WINDOW = new COSName("CenterWindow");
    public static final COSName CERT = new COSName("Cert");
    public static final COSName CF = new COSName("CF");
    public static final COSName CFM = new COSName("CFM");
    //** Acro form field type for choice fields.
    public static final COSName CH = new COSName("Ch");
    public static final COSName CHAR_PROCS = new COSName("CharProcs");
    public static final COSName CHAR_SET = new COSName("CharSet");
    public static final COSName CICI_SIGNIT = new COSName("CICI.SignIt");
    public static final COSName CID_FONT_TYPE0 = new COSName("CIDFontType0");
    public static final COSName CID_FONT_TYPE2 = new COSName("CIDFontType2");
    public static final COSName CID_TO_GID_MAP = new COSName("CIDToGIDMap");
    public static final COSName CID_SET = new COSName("CIDSet");
    public static final COSName CIDSYSTEMINFO = new COSName("CIDSystemInfo");
    public static final COSName CL = new COSName("CL");
    public static final COSName CLR_F = new COSName("ClrF");
    public static final COSName CLR_FF = new COSName("ClrFf");
    public static final COSName CMAP = new COSName("CMap");
    public static final COSName CMAPNAME = new COSName("CMapName");
    public static final COSName CMYK = new COSName("CMYK");
    public static final COSName CO = new COSName("CO");
    public static final COSName COLOR_BURN = new COSName("ColorBurn");
    public static final COSName COLOR_DODGE = new COSName("ColorDodge");
    public static final COSName COLORANTS = new COSName("Colorants");
    public static final COSName COLORS = new COSName("Colors");
    public static final COSName COLORSPACE = new COSName("ColorSpace");
    public static final COSName COLUMNS = new COSName("Columns");
    public static final COSName COMPATIBLE = new COSName("Compatible");
    public static final COSName COMPONENTS = new COSName("Components");
    public static final COSName CONTACT_INFO = new COSName("ContactInfo");
    public static final COSName CONTENTS = new COSName("Contents");
    public static final COSName COORDS = new COSName("Coords");
    public static final COSName COUNT = new COSName("Count");
    public static final COSName CP = new COSName("CP");
    public static final COSName CREATION_DATE = new COSName("CreationDate");
    public static final COSName CREATOR = new COSName("Creator");
    public static final COSName CROP_BOX = new COSName("CropBox");
    public static final COSName CRYPT = new COSName("Crypt");
    public static final COSName CS = new COSName("CS");
    // D
    public static final COSName D = new COSName("D");
    public static final COSName DA = new COSName("DA");
    public static final COSName DARKEN = new COSName("Darken");
    public static final COSName DATE = new COSName("Date");
    public static final COSName DCT_DECODE = new COSName("DCTDecode");
    public static final COSName DCT_DECODE_ABBREVIATION = new COSName("DCT");
    public static final COSName DECODE = new COSName("Decode");
    public static final COSName DECODE_PARMS = new COSName("DecodeParms");
    public static final COSName DEFAULT = new COSName("default");
    public static final COSName DEFAULT_CMYK = new COSName("DefaultCMYK");
    public static final COSName DEFAULT_GRAY = new COSName("DefaultGray");
    public static final COSName DEFAULT_RGB = new COSName("DefaultRGB");
    public static final COSName DESC = new COSName("Desc");
    public static final COSName DESCENDANT_FONTS = new COSName("DescendantFonts");
    public static final COSName DESCENT = new COSName("Descent");
    public static final COSName DEST = new COSName("Dest");
    public static final COSName DEST_OUTPUT_PROFILE = new COSName("DestOutputProfile");
    public static final COSName DESTS = new COSName("Dests");
    public static final COSName DEVICECMYK = new COSName("DeviceCMYK");
    public static final COSName DEVICEGRAY = new COSName("DeviceGray");
    public static final COSName DEVICEN = new COSName("DeviceN");
    public static final COSName DEVICERGB = new COSName("DeviceRGB");
    public static final COSName DI = new COSName("Di");
    public static final COSName DIFFERENCE = new COSName("Difference");
    public static final COSName DIFFERENCES = new COSName("Differences");
    public static final COSName DIGEST_METHOD = new COSName("DigestMethod");
    public static final COSName DIGEST_RIPEMD160 = new COSName("RIPEMD160");
    public static final COSName DIGEST_SHA1 = new COSName("SHA1");
    public static final COSName DIGEST_SHA256 = new COSName("SHA256");
    public static final COSName DIGEST_SHA384 = new COSName("SHA384");
    public static final COSName DIGEST_SHA512 = new COSName("SHA512");
    public static final COSName DIRECTION = new COSName("Direction");
    public static final COSName DISPLAY_DOC_TITLE = new COSName("DisplayDocTitle");
    public static final COSName DL = new COSName("DL");
    public static final COSName DM = new COSName("Dm");
    public static final COSName DOC = new COSName("Doc");
    public static final COSName DOC_CHECKSUM = new COSName("DocChecksum");
    public static final COSName DOC_TIME_STAMP = new COSName("DocTimeStamp");
    public static final COSName DOCMDP = new COSName("DocMDP");
    public static final COSName DOMAIN = new COSName("Domain");
    public static final COSName DOS = new COSName("DOS");
    public static final COSName DP = new COSName("DP");
    public static final COSName DR = new COSName("DR");
    public static final COSName DS = new COSName("DS");    
    public static final COSName DUPLEX = new COSName("Duplex");
    public static final COSName DUR = new COSName("Dur");
    public static final COSName DV = new COSName("DV");
    public static final COSName DW = new COSName("DW");
    public static final COSName DW2 = new COSName("DW2");
    // E
    public static final COSName E = new COSName("E");
    public static final COSName EARLY_CHANGE = new COSName("EarlyChange");
    public static final COSName EF = new COSName("EF");
    public static final COSName EMBEDDED_FDFS = new COSName("EmbeddedFDFs");
    public static final COSName EMBEDDED_FILES = new COSName("EmbeddedFiles");
    public static final COSName EMPTY = new COSName("");
    public static final COSName ENCODE = new COSName("Encode");
    public static final COSName ENCODED_BYTE_ALIGN = new COSName("EncodedByteAlign");    
    public static final COSName ENCODING = new COSName("Encoding");
    public static final COSName ENCODING_90MS_RKSJ_H = new COSName("90ms-RKSJ-H");
    public static final COSName ENCODING_90MS_RKSJ_V = new COSName("90ms-RKSJ-V");
    public static final COSName ENCODING_ETEN_B5_H = new COSName("ETen-B5-H");
    public static final COSName ENCODING_ETEN_B5_V = new COSName("ETen-B5-V");
    public static final COSName ENCRYPT = new COSName("Encrypt");
    public static final COSName ENCRYPT_META_DATA = new COSName("EncryptMetadata");
    public static final COSName END_OF_LINE = new COSName("EndOfLine");
    public static final COSName ENTRUST_PPKEF = new COSName("Entrust.PPKEF");
    public static final COSName EXCLUSION = new COSName("Exclusion");
    public static final COSName EXT_G_STATE = new COSName("ExtGState");
    public static final COSName EXTEND = new COSName("Extend");
    public static final COSName EXTENDS = new COSName("Extends");
    // F
    public static final COSName F = new COSName("F");
    public static final COSName F_DECODE_PARMS = new COSName("FDecodeParms");
    public static final COSName F_FILTER = new COSName("FFilter");
    public static final COSName FB = new COSName("FB");
    public static final COSName FDF = new COSName("FDF");
    public static final COSName FF = new COSName("Ff");
    public static final COSName FIELDS = new COSName("Fields");
    public static final COSName FILESPEC = new COSName("Filespec");
    public static final COSName FILTER = new COSName("Filter");
    public static final COSName FIRST = new COSName("First");
    public static final COSName FIRST_CHAR = new COSName("FirstChar");
    public static final COSName FIT_WINDOW = new COSName("FitWindow");
    public static final COSName FL = new COSName("FL");
    public static final COSName FLAGS = new COSName("Flags");
    public static final COSName FLATE_DECODE = new COSName("FlateDecode");
    public static final COSName FLATE_DECODE_ABBREVIATION = new COSName("Fl");
    public static final COSName FONT = new COSName("Font");
    public static final COSName FONT_BBOX = new COSName("FontBBox");
    public static final COSName FONT_DESC = new COSName("FontDescriptor");
    public static final COSName FONT_FAMILY = new COSName("FontFamily");
    public static final COSName FONT_FILE = new COSName("FontFile");
    public static final COSName FONT_FILE2 = new COSName("FontFile2");
    public static final COSName FONT_FILE3 = new COSName("FontFile3");
    public static final COSName FONT_MATRIX = new COSName("FontMatrix");
    public static final COSName FONT_NAME = new COSName("FontName");
    public static final COSName FONT_STRETCH = new COSName("FontStretch");
    public static final COSName FONT_WEIGHT = new COSName("FontWeight");
    public static final COSName FORM = new COSName("Form");
    public static final COSName FORMTYPE = new COSName("FormType");
    public static final COSName FRM = new COSName("FRM");
    public static final COSName FT = new COSName("FT");
    public static final COSName FUNCTION = new COSName("Function");
    public static final COSName FUNCTION_TYPE = new COSName("FunctionType");
    public static final COSName FUNCTIONS = new COSName("Functions");
    // G
    public static final COSName G = new COSName("G");
    public static final COSName GAMMA = new COSName("Gamma");
    public static final COSName GROUP = new COSName("Group");
    public static final COSName GTS_PDFA1 = new COSName("GTS_PDFA1");
    // H
    public static final COSName H = new COSName("H");
    public static final COSName HARD_LIGHT = new COSName("HardLight");
    public static final COSName HEIGHT = new COSName("Height");
    public static final COSName HIDE_MENUBAR = new COSName("HideMenubar");
    public static final COSName HIDE_TOOLBAR = new COSName("HideToolbar");
    public static final COSName HIDE_WINDOWUI = new COSName("HideWindowUI");
    // I
    public static final COSName I = new COSName("I");
    public static final COSName IC = new COSName("IC");
    public static final COSName ICCBASED = new COSName("ICCBased");
    public static final COSName ID = new COSName("ID");
    public static final COSName ID_TREE = new COSName("IDTree");
    public static final COSName IDENTITY = new COSName("Identity");
    public static final COSName IDENTITY_H = new COSName("Identity-H");
    public static final COSName IDENTITY_V = new COSName("Identity-V");
    public static final COSName IF = new COSName("IF");
    public static final COSName IM = new COSName("IM");
    public static final COSName IMAGE = new COSName("Image");
    public static final COSName IMAGE_MASK = new COSName("ImageMask");
    public static final COSName INDEX = new COSName("Index");
    public static final COSName INDEXED = new COSName("Indexed");
    public static final COSName INFO = new COSName("Info");
    public static final COSName INKLIST = new COSName("InkList");
    public static final COSName INTERPOLATE = new COSName("Interpolate");
    public static final COSName IT = new COSName("IT");
    public static final COSName ITALIC_ANGLE = new COSName("ItalicAngle");
    public static final COSName ISSUER = new COSName("Issuer");
    // J
    public static final COSName JAVA_SCRIPT = new COSName("JavaScript");
    public static final COSName JBIG2_DECODE = new COSName("JBIG2Decode");
    public static final COSName JBIG2_GLOBALS = new COSName("JBIG2Globals");
    public static final COSName JPX_DECODE = new COSName("JPXDecode");
    public static final COSName JS = new COSName("JS");
    // K
    public static final COSName K = new COSName("K");
    public static final COSName KEYWORDS = new COSName("Keywords");
    public static final COSName KEY_USAGE = new COSName("KeyUsage");
    public static final COSName KIDS = new COSName("Kids");
    // L
    public static final COSName L = new COSName("L");
    public static final COSName LAB = new COSName("Lab");
    public static final COSName LANG = new COSName("Lang");
    public static final COSName LAST = new COSName("Last");
    public static final COSName LAST_CHAR = new COSName("LastChar");
    public static final COSName LAST_MODIFIED = new COSName("LastModified");
    public static final COSName LC = new COSName("LC");
    public static final COSName LE = new COSName("LE");
    public static final COSName LEADING = new COSName("Leading");
    public static final COSName LEGAL_ATTESTATION = new COSName("LegalAttestation");
    public static final COSName LENGTH = new COSName("Length");
    public static final COSName LENGTH1 = new COSName("Length1");
    public static final COSName LENGTH2 = new COSName("Length2");
    public static final COSName LIGHTEN = new COSName("Lighten");
    public static final COSName LIMITS = new COSName("Limits");
    public static final COSName LJ = new COSName("LJ");
    public static final COSName LL = new COSName("LL");
    public static final COSName LLE = new COSName("LLE");
    public static final COSName LLO = new COSName("LLO");
    public static final COSName LOCATION = new COSName("Location");
    public static final COSName LUMINOSITY = new COSName("Luminosity");
    public static final COSName LW = new COSName("LW");
    public static final COSName LZW_DECODE = new COSName("LZWDecode");
    public static final COSName LZW_DECODE_ABBREVIATION = new COSName("LZW");
    // M
    public static final COSName M = new COSName("M");
    public static final COSName MAC = new COSName("Mac");
    public static final COSName MAC_EXPERT_ENCODING = new COSName("MacExpertEncoding");
    public static final COSName MAC_ROMAN_ENCODING = new COSName("MacRomanEncoding");
    public static final COSName MARK_INFO = new COSName("MarkInfo");
    public static final COSName MASK = new COSName("Mask");
    public static final COSName MATRIX = new COSName("Matrix");
    public static final COSName MAX_LEN = new COSName("MaxLen");
    public static final COSName MAX_WIDTH = new COSName("MaxWidth");
    public static final COSName MCID = new COSName("MCID");
    public static final COSName MDP = new COSName("MDP");
    public static final COSName MEDIA_BOX = new COSName("MediaBox");
    public static final COSName METADATA = new COSName("Metadata");
    public static final COSName MISSING_WIDTH = new COSName("MissingWidth");
    public static final COSName MIX = new COSName("Mix");
    public static final COSName MK = new COSName("MK");
    public static final COSName ML = new COSName("ML");
    public static final COSName MM_TYPE1 = new COSName("MMType1");
    public static final COSName MOD_DATE = new COSName("ModDate");
    public static final COSName MULTIPLY = new COSName("Multiply");
    // N
    public static final COSName N = new COSName("N");
    public static final COSName NAME = new COSName("Name");
    public static final COSName NAMES = new COSName("Names");
    public static final COSName NEED_APPEARANCES = new COSName("NeedAppearances");
    public static final COSName NEXT = new COSName("Next");
    public static final COSName NM = new COSName("NM");
    public static final COSName NON_EFONT_NO_WARN = new COSName("NonEFontNoWarn");
    public static final COSName NON_FULL_SCREEN_PAGE_MODE = new COSName("NonFullScreenPageMode");
    public static final COSName NONE = new COSName("None");
    public static final COSName NORMAL = new COSName("Normal");
    public static final COSName NUMS = new COSName("Nums");
    // O
    public static final COSName O = new COSName("O");
    public static final COSName OBJ = new COSName("Obj");
    public static final COSName OBJ_STM = new COSName("ObjStm");
    public static final COSName OC = new COSName("OC");
    public static final COSName OCG = new COSName("OCG");
    public static final COSName OCGS = new COSName("OCGs");
    public static final COSName OCPROPERTIES = new COSName("OCProperties");
    public static final COSName OE = new COSName("OE");
    public static final COSName OID = new COSName("OID");
    
    /**
     * "OFF", to be used for OCGs, not for Acroform
     */
    public static final COSName OFF = new COSName("OFF");
    
    /**
     * "Off", to be used for Acroform, not for OCGs
     */
    public static final COSName Off = new COSName("Off");    
    
    public static final COSName ON = new COSName("ON");
    public static final COSName OP = new COSName("OP");
    public static final COSName OP_NS = new COSName("op");
    public static final COSName OPEN_ACTION = new COSName("OpenAction");
    public static final COSName OPEN_TYPE = new COSName("OpenType");
    public static final COSName OPM = new COSName("OPM");
    public static final COSName OPT = new COSName("Opt");
    public static final COSName ORDER = new COSName("Order");
    public static final COSName ORDERING = new COSName("Ordering");
    public static final COSName OS = new COSName("OS");
    public static final COSName OUTLINES = new COSName("Outlines");
    public static final COSName OUTPUT_CONDITION = new COSName("OutputCondition");
    public static final COSName OUTPUT_CONDITION_IDENTIFIER = new COSName(
            "OutputConditionIdentifier");
    public static final COSName OUTPUT_INTENT = new COSName("OutputIntent");
    public static final COSName OUTPUT_INTENTS = new COSName("OutputIntents");
    public static final COSName OVERLAY = new COSName("Overlay");
    // P
    public static final COSName P = new COSName("P");
    public static final COSName PAGE = new COSName("Page");
    public static final COSName PAGE_LABELS = new COSName("PageLabels");
    public static final COSName PAGE_LAYOUT = new COSName("PageLayout");
    public static final COSName PAGE_MODE = new COSName("PageMode");
    public static final COSName PAGES = new COSName("Pages");
    public static final COSName PAINT_TYPE = new COSName("PaintType");
    public static final COSName PANOSE = new COSName("Panose");    
    public static final COSName PARAMS = new COSName("Params");
    public static final COSName PARENT = new COSName("Parent");
    public static final COSName PARENT_TREE = new COSName("ParentTree");
    public static final COSName PARENT_TREE_NEXT_KEY = new COSName("ParentTreeNextKey");
    public static final COSName PATTERN = new COSName("Pattern");
    public static final COSName PATTERN_TYPE = new COSName("PatternType");
    public static final COSName PDF_DOC_ENCODING = new COSName("PDFDocEncoding");
    public static final COSName PERMS = new COSName("Perms");
    public static final COSName PG = new COSName("Pg");
    public static final COSName PRE_RELEASE = new COSName("PreRelease");
    public static final COSName PREDICTOR = new COSName("Predictor");
    public static final COSName PREV = new COSName("Prev");
    public static final COSName PRINT_AREA = new COSName("PrintArea");
    public static final COSName PRINT_CLIP = new COSName("PrintClip");
    public static final COSName PRINT_SCALING = new COSName("PrintScaling");
    public static final COSName PROC_SET = new COSName("ProcSet");
    public static final COSName PROCESS = new COSName("Process");
    public static final COSName PRODUCER = new COSName("Producer");
    public static final COSName PROP_BUILD = new COSName("Prop_Build");
    public static final COSName PROPERTIES = new COSName("Properties");
    public static final COSName PS = new COSName("PS");
    public static final COSName PUB_SEC = new COSName("PubSec");
    // Q
    public static final COSName Q = new COSName("Q");
    public static final COSName QUADPOINTS = new COSName("QuadPoints");
    // R
    public static final COSName R = new COSName("R");
    public static final COSName RANGE = new COSName("Range");
    public static final COSName RC = new COSName("RC");
    public static final COSName RD = new COSName("RD");
    public static final COSName REASON = new COSName("Reason");
    public static final COSName REASONS = new COSName("Reasons");
    public static final COSName REPEAT = new COSName("Repeat");
    public static final COSName RECIPIENTS = new COSName("Recipients");
    public static final COSName RECT = new COSName("Rect");
    public static final COSName REGISTRY = new COSName("Registry");
    public static final COSName REGISTRY_NAME = new COSName("RegistryName");
    public static final COSName RENAME = new COSName("Rename");
    public static final COSName RESOURCES = new COSName("Resources");
    public static final COSName RGB = new COSName("RGB");
    public static final COSName RI = new COSName("RI");
    public static final COSName ROLE_MAP = new COSName("RoleMap");
    public static final COSName ROOT = new COSName("Root");
    public static final COSName ROTATE = new COSName("Rotate");
    public static final COSName ROWS = new COSName("Rows");
    public static final COSName RUN_LENGTH_DECODE = new COSName("RunLengthDecode");
    public static final COSName RUN_LENGTH_DECODE_ABBREVIATION = new COSName("RL");
    public static final COSName RV = new COSName("RV");
    // S
    public static final COSName S = new COSName("S");
    public static final COSName SA = new COSName("SA");
    public static final COSName SCREEN = new COSName("Screen");
    public static final COSName SE = new COSName("SE");
    public static final COSName SEPARATION = new COSName("Separation");
    public static final COSName SET_F = new COSName("SetF");
    public static final COSName SET_FF = new COSName("SetFf");
    public static final COSName SHADING = new COSName("Shading");
    public static final COSName SHADING_TYPE = new COSName("ShadingType");
    public static final COSName SIG = new COSName("Sig");
    public static final COSName SIG_FLAGS = new COSName("SigFlags");
    public static final COSName SIZE = new COSName("Size");
    public static final COSName SM = new COSName("SM");
    public static final COSName SMASK = new COSName("SMask");
    public static final COSName SOFT_LIGHT = new COSName("SoftLight");
    public static final COSName SOUND = new COSName("Sound");
    public static final COSName SS = new COSName("SS");
    public static final COSName ST = new COSName("St");
    public static final COSName STANDARD_ENCODING = new COSName("StandardEncoding");
    public static final COSName STATE = new COSName("State");
    public static final COSName STATE_MODEL = new COSName("StateModel");
    public static final COSName STATUS = new COSName("Status");
    public static final COSName STD_CF = new COSName("StdCF");
    public static final COSName STEM_H = new COSName("StemH");
    public static final COSName STEM_V = new COSName("StemV");
    public static final COSName STM_F = new COSName("StmF");
    public static final COSName STR_F = new COSName("StrF");
    public static final COSName STRUCT_PARENT = new COSName("StructParent");
    public static final COSName STRUCT_PARENTS = new COSName("StructParents");
    public static final COSName STRUCT_TREE_ROOT = new COSName("StructTreeRoot");
    public static final COSName STYLE = new COSName("Style");
    public static final COSName SUB_FILTER = new COSName("SubFilter");
    public static final COSName SUBJ = new COSName("Subj");
    public static final COSName SUBJECT = new COSName("Subject");
    public static final COSName SUBJECT_DN = new COSName("SubjectDN");
    public static final COSName SUBTYPE = new COSName("Subtype");
    public static final COSName SUPPLEMENT = new COSName("Supplement");
    public static final COSName SV = new COSName("SV");
    public static final COSName SV_CERT = new COSName("SVCert");
    public static final COSName SW = new COSName("SW");
    public static final COSName SY = new COSName("Sy");
    public static final COSName SYNCHRONOUS = new COSName("Synchronous");
    // T
    public static final COSName T = new COSName("T");
    public static final COSName TARGET = new COSName("Target");
    public static final COSName TEMPLATES = new COSName("Templates");
    public static final COSName THREADS = new COSName("Threads");
    public static final COSName THUMB = new COSName("Thumb");
    public static final COSName TI = new COSName("TI");
    public static final COSName TILING_TYPE = new COSName("TilingType");
    public static final COSName TIME_STAMP = new COSName("TimeStamp");
    public static final COSName TITLE = new COSName("Title");
    public static final COSName TK = new COSName("TK");
    public static final COSName TM = new COSName("TM");
    public static final COSName TO_UNICODE = new COSName("ToUnicode");
    public static final COSName TR = new COSName("TR");
    public static final COSName TR2 = new COSName("TR2");
    public static final COSName TRAPPED = new COSName("Trapped");
    public static final COSName TRANS = new COSName("Trans");
    public static final COSName TRANSPARENCY = new COSName("Transparency");
    public static final COSName TREF = new COSName("TRef");
    public static final COSName TRIM_BOX = new COSName("TrimBox");
    public static final COSName TRUE_TYPE = new COSName("TrueType");
    public static final COSName TRUSTED_MODE = new COSName("TrustedMode");
    public static final COSName TU = new COSName("TU");
    /** Acro form field type for text field. */
    public static final COSName TX = new COSName("Tx");
    public static final COSName TYPE = new COSName("Type");
    public static final COSName TYPE0 = new COSName("Type0");
    public static final COSName TYPE1 = new COSName("Type1");
    public static final COSName TYPE3 = new COSName("Type3");
    // U
    public static final COSName U = new COSName("U");
    public static final COSName UE = new COSName("UE");
    public static final COSName UF = new COSName("UF");
    public static final COSName UNCHANGED = new COSName("Unchanged");
    public static final COSName UNIX = new COSName("Unix");
    public static final COSName URI = new COSName("URI");
    public static final COSName URL = new COSName("URL");
    public static final COSName URL_TYPE = new COSName("URLType");
    // V
    public static final COSName V = new COSName("V");
    public static final COSName VERISIGN_PPKVS = new COSName("VeriSign.PPKVS");
    public static final COSName VERSION = new COSName("Version");
    public static final COSName VERTICES = new COSName("Vertices");
    public static final COSName VERTICES_PER_ROW = new COSName("VerticesPerRow");
    public static final COSName VIEW_AREA = new COSName("ViewArea");
    public static final COSName VIEW_CLIP = new COSName("ViewClip");
    public static final COSName VIEWER_PREFERENCES = new COSName("ViewerPreferences");
    public static final COSName VOLUME = new COSName("Volume");
    // W
    public static final COSName W = new COSName("W");
    public static final COSName W2 = new COSName("W2");
    public static final COSName WHITE_POINT = new COSName("WhitePoint");
    public static final COSName WIDGET = new COSName("Widget");
    public static final COSName WIDTH = new COSName("Width");
    public static final COSName WIDTHS = new COSName("Widths");
    public static final COSName WIN_ANSI_ENCODING = new COSName("WinAnsiEncoding");
    // X
    public static final COSName XFA = new COSName("XFA");
    public static final COSName X_STEP = new COSName("XStep");
    public static final COSName XHEIGHT = new COSName("XHeight");
    public static final COSName XOBJECT = new COSName("XObject");
    public static final COSName XREF = new COSName("XRef");
    public static final COSName XREF_STM = new COSName("XRefStm");
    // Y
    public static final COSName Y_STEP = new COSName("YStep");
    public static final COSName YES = new COSName("Yes");

    // fields
    private final String name;
    private final int hashCode;

    /**
     * This will get a COSName object with that name.
     * 
     * @param aName The name of the object.
     * 
     * @return A COSName with the specified name.
     */
    public static COSName getPDFName(String aName)
    {
        COSName name = null;
        if (aName != null)
        {
            // Is it a common COSName ??
            name = commonNameMap.get(aName);
            if (name == null)
            {
                // It seems to be a document specific COSName
                name = nameMap.get(aName);
                if (name == null)
                {
                    // name is added to the synchronized map in the constructor
                    name = new COSName(aName, false);
                }
            }
        }
        return name;
    }

    /**
     * Private constructor. This will limit the number of COSName objects. that are created.
     * 
     * @param aName The name of the COSName object.
     * @param staticValue Indicates if the COSName object is static so that it can be stored in the HashMap without
     * synchronizing.
     */
    private COSName(String aName, boolean staticValue)
    {
        name = aName;
        if (staticValue)
        {
            commonNameMap.put(aName, this);
        }
        else
        {
            nameMap.put(aName, this);
        }
        hashCode = name.hashCode();
    }

    /**
     * Private constructor. This will limit the number of COSName objects. that are created.
     * 
     * @param aName The name of the COSName object.
     */
    private COSName(String aName)
    {
        this(aName, true);
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

    @Override
    public String toString()
    {
        return "COSName{" + name + "}";
    }

    @Override
    public boolean equals(Object object)
    {
        return object instanceof COSName && name.equals(((COSName) object).name);
    }

    @Override
    public int hashCode()
    {
        return hashCode;
    }

    @Override
    public int compareTo(COSName other)
    {
        return name.compareTo(other.name);
    }

    /**
     * Returns true if the name is the empty string.
     * @return true if the name is the empty string.
     */
    public boolean isEmpty()
    {
        return name.isEmpty();
    }

    @Override
    public Object accept(ICOSVisitor visitor) throws IOException
    {
        return visitor.visitFromName(this);
    }

    /**
     * This will output this string as a PDF object.
     * 
     * @param output The stream to write to.
     * @throws IOException If there is an error writing to the stream.
     */
    public void writePDF(OutputStream output) throws IOException
    {
        output.write('/');
        byte[] bytes = getName().getBytes(Charsets.US_ASCII);
        for (byte b : bytes)
        {
            int current = b & 0xFF;

            // be more restrictive than the PDF spec, "Name Objects", see PDFBOX-2073
            if (current >= 'A' && current <= 'Z' ||
                    current >= 'a' && current <= 'z' ||
                    current >= '0' && current <= '9' ||
                    current == '+' ||
                    current == '-' ||
                    current == '_' ||
                    current == '@' ||
                    current == '*' ||
                    current == '$' ||
                    current == ';' ||
                    current == '.')
            {
                output.write(current);
            }
            else
            {
                output.write('#');
                Hex.writeHexByte(b, output);
            }
        }
    }

    /**
     * Not usually needed except if resources need to be reclaimed in a long running process.
     */
    public static synchronized void clearResources()
    {
        // Clear them all
        nameMap.clear();
    }
}

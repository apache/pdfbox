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
import java.lang.ref.Cleaner;
import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.pdfbox.util.Hex;

/**
 * A PDF Name object.
 *
 * @author Ben Litchfield
 */
public final class COSName extends COSBase implements Comparable<COSName>
{
    // using ConcurrentHashMap because this can be accessed by multiple threads
    private static final Map<String, WeakReference<COSName>> NAME_MAP = //
            new ConcurrentHashMap<>(8192);
    private static final Cleaner CLEANER = Cleaner.create();

    //
    // IMPORTANT: this list is *alphabetized* and does not need any JavaDoc
    //

    // A
    public static final COSName A = getPDFName("A");
    public static final COSName AA = getPDFName("AA");
    public static final COSName ABSOLUTE_COLORIMETRIC = getPDFName("AbsoluteColorimetric");
    public static final COSName AC = getPDFName("AC");
    public static final COSName ACRO_FORM = getPDFName("AcroForm");
    public static final COSName ACTUAL_TEXT = getPDFName("ActualText");
    public static final COSName ADBE = getPDFName("ADBE");
    public static final COSName ADBE_PKCS7_DETACHED = getPDFName("adbe.pkcs7.detached");
    public static final COSName ADBE_PKCS7_SHA1 = getPDFName("adbe.pkcs7.sha1");
    public static final COSName ADBE_X509_RSA_SHA1 = getPDFName("adbe.x509.rsa_sha1");
    public static final COSName ADOBE_PPKLITE = getPDFName("Adobe.PPKLite");
    public static final COSName AESV2 = getPDFName("AESV2");
    public static final COSName AESV3 = getPDFName("AESV3");
    public static final COSName AF = getPDFName("AF");
    public static final COSName AF_RELATIONSHIP = COSName.getPDFName("AFRelationship");
    public static final COSName AFTER = getPDFName("After");
    public static final COSName AI_META_DATA = getPDFName("AIMetaData");
    public static final COSName AIS = getPDFName("AIS");
    public static final COSName ALL_OFF = getPDFName("AllOff");
    public static final COSName ALL_ON = getPDFName("AllOn");
    public static final COSName ALT = getPDFName("Alt");
    public static final COSName ALPHA = getPDFName("Alpha");
    public static final COSName ALTERNATE = getPDFName("Alternate");
    public static final COSName ANNOT = getPDFName("Annot");
    public static final COSName ANNOTS = getPDFName("Annots");
    public static final COSName ANTI_ALIAS = getPDFName("AntiAlias");
    public static final COSName ANY_OFF = getPDFName("AnyOff");
    public static final COSName ANY_ON = getPDFName("AnyOn");
    public static final COSName AP = getPDFName("AP");
    public static final COSName AP_REF = getPDFName("APRef");
    public static final COSName APP = getPDFName("App");
    public static final COSName ART_BOX = getPDFName("ArtBox");
    public static final COSName ARTIFACT = getPDFName("Artifact");
    public static final COSName AS = getPDFName("AS");
    public static final COSName ASCENT = getPDFName("Ascent");
    public static final COSName ASCII_HEX_DECODE = getPDFName("ASCIIHexDecode");
    public static final COSName ASCII_HEX_DECODE_ABBREVIATION = getPDFName("AHx");
    public static final COSName ASCII85_DECODE = getPDFName("ASCII85Decode");
    public static final COSName ASCII85_DECODE_ABBREVIATION = getPDFName("A85");
    public static final COSName ATTACHED = getPDFName("Attached");
    public static final COSName AUTHOR = getPDFName("Author");
    public static final COSName AVG_WIDTH = getPDFName("AvgWidth");
    // B
    public static final COSName B = getPDFName("B");
    public static final COSName BACKGROUND = getPDFName("Background");
    public static final COSName BASE_ENCODING = getPDFName("BaseEncoding");
    public static final COSName BASE_FONT = getPDFName("BaseFont");
    public static final COSName BASE_STATE = getPDFName("BaseState");
    public static final COSName BASE_VERSION = getPDFName("BaseVersion");
    public static final COSName BBOX = getPDFName("BBox");
    public static final COSName BC = getPDFName("BC");
    public static final COSName BE = getPDFName("BE");
    public static final COSName BEAD = getPDFName("BEAD");
    public static final COSName BEFORE = getPDFName("Before");
    public static final COSName BG = getPDFName("BG");
    public static final COSName BITS_PER_COMPONENT = getPDFName("BitsPerComponent");
    public static final COSName BITS_PER_COORDINATE = getPDFName("BitsPerCoordinate");
    public static final COSName BITS_PER_FLAG = getPDFName("BitsPerFlag");
    public static final COSName BITS_PER_SAMPLE = getPDFName("BitsPerSample");
    public static final COSName BL = getPDFName("Bl");
    public static final COSName BLACK_IS_1 = getPDFName("BlackIs1");
    public static final COSName BLACK_POINT = getPDFName("BlackPoint");
    public static final COSName BLEED_BOX = getPDFName("BleedBox");
    public static final COSName BM = getPDFName("BM");
    public static final COSName BORDER = getPDFName("Border");
    public static final COSName BOUNDS = getPDFName("Bounds");
    public static final COSName BPC = getPDFName("BPC");
    public static final COSName BS = getPDFName("BS");
    //** Acro form field type for button fields.
    public static final COSName BTN = getPDFName("Btn");
    public static final COSName BYTERANGE = getPDFName("ByteRange");
    // C
    public static final COSName C = getPDFName("C");
    public static final COSName C0 = getPDFName("C0");
    public static final COSName C1 = getPDFName("C1");
    public static final COSName CA = getPDFName("CA");
    public static final COSName CA_NS = getPDFName("ca");
    public static final COSName CALGRAY = getPDFName("CalGray");
    public static final COSName CALRGB = getPDFName("CalRGB");
    public static final COSName CAP = getPDFName("Cap");
    public static final COSName CAP_HEIGHT = getPDFName("CapHeight");
    public static final COSName CATALOG = getPDFName("Catalog");
    public static final COSName CCITTFAX_DECODE = getPDFName("CCITTFaxDecode");
    public static final COSName CCITTFAX_DECODE_ABBREVIATION = getPDFName("CCF");
    public static final COSName CENTER_WINDOW = getPDFName("CenterWindow");
    public static final COSName CERT = getPDFName("Cert");
    public static final COSName CERTS = getPDFName("Certs");
    public static final COSName CF = getPDFName("CF");
    public static final COSName CFM = getPDFName("CFM");
    //** Acro form field type for choice fields.
    public static final COSName CH = getPDFName("Ch");
    public static final COSName CHAR_PROCS = getPDFName("CharProcs");
    public static final COSName CHAR_SET = getPDFName("CharSet");
    public static final COSName CHECK_SUM = getPDFName("CheckSum");
    public static final COSName CI = getPDFName("CI");
    public static final COSName CICI_SIGNIT = getPDFName("CICI.SignIt");
    public static final COSName CID_FONT_TYPE0 = getPDFName("CIDFontType0");
    public static final COSName CID_FONT_TYPE2 = getPDFName("CIDFontType2");
    public static final COSName CID_TO_GID_MAP = getPDFName("CIDToGIDMap");
    public static final COSName CID_SET = getPDFName("CIDSet");
    public static final COSName CIDSYSTEMINFO = getPDFName("CIDSystemInfo");
    public static final COSName CL = getPDFName("CL");
    public static final COSName CLASS_MAP = getPDFName("ClassMap");
    public static final COSName CLR_F = getPDFName("ClrF");
    public static final COSName CLR_FF = getPDFName("ClrFf");
    public static final COSName CMAP = getPDFName("CMap");
    public static final COSName CMAPNAME = getPDFName("CMapName");
    public static final COSName CMYK = getPDFName("CMYK");
    public static final COSName CO = getPDFName("CO");
    public static final COSName COLOR = getPDFName("Color");
    public static final COSName COLLECTION = getPDFName("Collection");
    public static final COSName COLLECTION_ITEM = getPDFName("CollectionItem");
    public static final COSName COLLECTION_FIELD = getPDFName("CollectionField");
    public static final COSName COLLECTION_SCHEMA = getPDFName("CollectionSchema");
    public static final COSName COLLECTION_SORT = getPDFName("CollectionSort");
    public static final COSName COLLECTION_SUBITEM = getPDFName("CollectionSubitem");
    public static final COSName COLOR_BURN = getPDFName("ColorBurn");
    public static final COSName COLOR_DODGE = getPDFName("ColorDodge");
    public static final COSName COLORANTS = getPDFName("Colorants");
    public static final COSName COLORS = getPDFName("Colors");
    public static final COSName COLORSPACE = getPDFName("ColorSpace");
    public static final COSName COLUMNS = getPDFName("Columns");
    public static final COSName COMPATIBLE = getPDFName("Compatible");
    public static final COSName COMPONENTS = getPDFName("Components");
    public static final COSName CONTACT_INFO = getPDFName("ContactInfo");
    public static final COSName CONTENTS = getPDFName("Contents");
    public static final COSName COORDS = getPDFName("Coords");
    public static final COSName COUNT = getPDFName("Count");
    public static final COSName CP = getPDFName("CP");
    public static final COSName CREATION_DATE = getPDFName("CreationDate");
    public static final COSName CREATOR = getPDFName("Creator");
    public static final COSName CRL = getPDFName("CRL");
    public static final COSName CRLS = getPDFName("CRLS");
    public static final COSName CROP_BOX = getPDFName("CropBox");
    public static final COSName CRYPT = getPDFName("Crypt");
    public static final COSName CS = getPDFName("CS");
    public static final COSName CYX = getPDFName("CYX");
    // D
    public static final COSName D = getPDFName("D");
    public static final COSName DA = getPDFName("DA");
    public static final COSName DARKEN = getPDFName("Darken");
    public static final COSName DATE = getPDFName("Date");
    public static final COSName DCT_DECODE = getPDFName("DCTDecode");
    public static final COSName DCT_DECODE_ABBREVIATION = getPDFName("DCT");
    public static final COSName DECODE = getPDFName("Decode");
    public static final COSName DECODE_PARMS = getPDFName("DecodeParms");
    public static final COSName DEFAULT = getPDFName("default");
    public static final COSName DEFAULT_CMYK = getPDFName("DefaultCMYK");
    public static final COSName DEFAULT_CRYPT_FILTER = getPDFName("DefaultCryptFilter");
    public static final COSName DEFAULT_GRAY = getPDFName("DefaultGray");
    public static final COSName DEFAULT_RGB = getPDFName("DefaultRGB");
    public static final COSName DESC = getPDFName("Desc");
    public static final COSName DESCENDANT_FONTS = getPDFName("DescendantFonts");
    public static final COSName DESCENT = getPDFName("Descent");
    public static final COSName DEST = getPDFName("Dest");
    public static final COSName DEST_OUTPUT_PROFILE = getPDFName("DestOutputProfile");
    public static final COSName DESTS = getPDFName("Dests");
    public static final COSName DEVICECMYK = getPDFName("DeviceCMYK");
    public static final COSName DEVICEGRAY = getPDFName("DeviceGray");
    public static final COSName DEVICEN = getPDFName("DeviceN");
    public static final COSName DEVICERGB = getPDFName("DeviceRGB");
    public static final COSName DI = getPDFName("Di");
    public static final COSName DIFFERENCE = getPDFName("Difference");
    public static final COSName DIFFERENCES = getPDFName("Differences");
    public static final COSName DIGEST_METHOD = getPDFName("DigestMethod");
    public static final COSName DIGEST_RIPEMD160 = getPDFName("RIPEMD160");
    public static final COSName DIGEST_SHA1 = getPDFName("SHA1");
    public static final COSName DIGEST_SHA256 = getPDFName("SHA256");
    public static final COSName DIGEST_SHA384 = getPDFName("SHA384");
    public static final COSName DIGEST_SHA512 = getPDFName("SHA512");
    public static final COSName DIRECTION = getPDFName("Direction");
    public static final COSName DISPLAY_DOC_TITLE = getPDFName("DisplayDocTitle");
    public static final COSName DL = getPDFName("DL");
    public static final COSName DM = getPDFName("Dm");
    public static final COSName DOC = getPDFName("Doc");
    public static final COSName DOC_CHECKSUM = getPDFName("DocChecksum");
    public static final COSName DOC_TIME_STAMP = getPDFName("DocTimeStamp");
    public static final COSName DOCMDP = getPDFName("DocMDP");
    public static final COSName DOCUMENT = getPDFName("Document");
    public static final COSName DOMAIN = getPDFName("Domain");
    public static final COSName DOS = getPDFName("DOS");
    public static final COSName DP = getPDFName("DP");
    public static final COSName DR = getPDFName("DR");
    public static final COSName DS = getPDFName("DS");
    public static final COSName DSS = getPDFName("DSS");
    public static final COSName DUPLEX = getPDFName("Duplex");
    public static final COSName DUR = getPDFName("Dur");
    public static final COSName DV = getPDFName("DV");
    public static final COSName DW = getPDFName("DW");
    public static final COSName DW2 = getPDFName("DW2");
    // E
    public static final COSName E = getPDFName("E");
    public static final COSName EARLY_CHANGE = getPDFName("EarlyChange");
    public static final COSName EF = getPDFName("EF");
    public static final COSName EMBEDDED_FDFS = getPDFName("EmbeddedFDFs");
    public static final COSName EMBEDDED_FILE = getPDFName("EmbeddedFile");
    public static final COSName EMBEDDED_FILES = getPDFName("EmbeddedFiles");
    public static final COSName EMPTY = getPDFName("");
    public static final COSName ENCODE = getPDFName("Encode");
    public static final COSName ENCODED_BYTE_ALIGN = getPDFName("EncodedByteAlign");    
    public static final COSName ENCODING = getPDFName("Encoding");
    public static final COSName ENCODING_90MS_RKSJ_H = getPDFName("90ms-RKSJ-H");
    public static final COSName ENCODING_90MS_RKSJ_V = getPDFName("90ms-RKSJ-V");
    public static final COSName ENCODING_ETEN_B5_H = getPDFName("ETen-B5-H");
    public static final COSName ENCODING_ETEN_B5_V = getPDFName("ETen-B5-V");
    public static final COSName ENCRYPT = getPDFName("Encrypt");
    public static final COSName ENCRYPT_META_DATA = getPDFName("EncryptMetadata");
    public static final COSName ENCRYPTED_PAYLOAD = getPDFName("EncryptedPayload");
    public static final COSName END_OF_LINE = getPDFName("EndOfLine");
    public static final COSName ENTRUST_PPKEF = getPDFName("Entrust.PPKEF");
    public static final COSName EXCLUSION = getPDFName("Exclusion");
    public static final COSName EXTENSIONS = getPDFName("Extensions");
    public static final COSName EXTENSION_LEVEL = getPDFName("ExtensionLevel");
    public static final COSName EX_DATA = getPDFName("ExData");
    public static final COSName EXPORT = getPDFName("Export");
    public static final COSName EXPORT_STATE = getPDFName("ExportState");
    public static final COSName EXT_G_STATE = getPDFName("ExtGState");
    public static final COSName EXTEND = getPDFName("Extend");
    public static final COSName EXTENDS = getPDFName("Extends");
    // F
    public static final COSName F = getPDFName("F");
    public static final COSName F_DECODE_PARMS = getPDFName("FDecodeParms");
    public static final COSName F_FILTER = getPDFName("FFilter");
    public static final COSName FB = getPDFName("FB");
    public static final COSName FDF = getPDFName("FDF");
    public static final COSName FF = getPDFName("Ff");
    public static final COSName FIELDS = getPDFName("Fields");
    public static final COSName FILESPEC = getPDFName("Filespec");
    public static final COSName FILTER = getPDFName("Filter");
    public static final COSName FIRST = getPDFName("First");
    public static final COSName FIRST_CHAR = getPDFName("FirstChar");
    public static final COSName FIT_WINDOW = getPDFName("FitWindow");
    public static final COSName FL = getPDFName("FL");
    public static final COSName FLAGS = getPDFName("Flags");
    public static final COSName FLATE_DECODE = getPDFName("FlateDecode");
    public static final COSName FLATE_DECODE_ABBREVIATION = getPDFName("Fl");
    public static final COSName FO = getPDFName("Fo");
    public static final COSName FOLDERS = getPDFName("Folders");
    public static final COSName FONT = getPDFName("Font");
    public static final COSName FONT_BBOX = getPDFName("FontBBox");
    public static final COSName FONT_DESC = getPDFName("FontDescriptor");
    public static final COSName FONT_FAMILY = getPDFName("FontFamily");
    public static final COSName FONT_FILE = getPDFName("FontFile");
    public static final COSName FONT_FILE2 = getPDFName("FontFile2");
    public static final COSName FONT_FILE3 = getPDFName("FontFile3");
    public static final COSName FONT_MATRIX = getPDFName("FontMatrix");
    public static final COSName FONT_NAME = getPDFName("FontName");
    public static final COSName FONT_STRETCH = getPDFName("FontStretch");
    public static final COSName FONT_WEIGHT = getPDFName("FontWeight");
    public static final COSName FORM = getPDFName("Form");
    public static final COSName FORMTYPE = getPDFName("FormType");
    public static final COSName FRM = getPDFName("FRM");
    public static final COSName FS = getPDFName("FS");
    public static final COSName FT = getPDFName("FT");
    public static final COSName FUNCTION = getPDFName("Function");
    public static final COSName FUNCTION_TYPE = getPDFName("FunctionType");
    public static final COSName FUNCTIONS = getPDFName("Functions");
    // G
    public static final COSName G = getPDFName("G");
    public static final COSName GAMMA = getPDFName("Gamma");
    public static final COSName GROUP = getPDFName("Group");
    public static final COSName GTS_PDFA1 = getPDFName("GTS_PDFA1");
    // H
    public static final COSName H = getPDFName("H");
    public static final COSName HARD_LIGHT = getPDFName("HardLight");
    public static final COSName HEIGHT = getPDFName("Height");
    public static final COSName HELV = getPDFName("Helv");
    public static final COSName HIDE_MENUBAR = getPDFName("HideMenubar");
    public static final COSName HIDE_TOOLBAR = getPDFName("HideToolbar");
    public static final COSName HIDE_WINDOWUI = getPDFName("HideWindowUI");
    public static final COSName HUE = getPDFName("Hue");
    // I
    public static final COSName I = getPDFName("I");
    public static final COSName IC = getPDFName("IC");
    public static final COSName ICCBASED = getPDFName("ICCBased");
    public static final COSName ID = getPDFName("ID");
    public static final COSName ID_TREE = getPDFName("IDTree");
    public static final COSName IDENTITY = getPDFName("Identity");
    public static final COSName IDENTITY_H = getPDFName("Identity-H");
    public static final COSName IDENTITY_V = getPDFName("Identity-V");
    public static final COSName IF = getPDFName("IF");
    public static final COSName ILLUSTRATOR = getPDFName("Illustrator");
    public static final COSName IM = getPDFName("IM");
    public static final COSName IMAGE = getPDFName("Image");
    public static final COSName IMAGE_MASK = getPDFName("ImageMask");
    public static final COSName INDEX = getPDFName("Index");
    public static final COSName INDEXED = getPDFName("Indexed");
    public static final COSName INFO = getPDFName("Info");
    public static final COSName INKLIST = getPDFName("InkList");
    public static final COSName INTENT = getPDFName("Intent");
    public static final COSName INTERPOLATE = getPDFName("Interpolate");
    public static final COSName IRT = getPDFName("IRT");
    public static final COSName IT = getPDFName("IT");
    public static final COSName ITALIC_ANGLE = getPDFName("ItalicAngle");
    public static final COSName ISSUER = getPDFName("Issuer");
    public static final COSName IX = getPDFName("IX");

    // J
    public static final COSName JAVA_SCRIPT = getPDFName("JavaScript");
    public static final COSName JBIG2_DECODE = getPDFName("JBIG2Decode");
    public static final COSName JBIG2_GLOBALS = getPDFName("JBIG2Globals");
    public static final COSName JPX_DECODE = getPDFName("JPXDecode");
    public static final COSName JS = getPDFName("JS");
    // K
    public static final COSName K = getPDFName("K");
    public static final COSName KEYWORDS = getPDFName("Keywords");
    public static final COSName KEY_USAGE = getPDFName("KeyUsage");
    public static final COSName KIDS = getPDFName("Kids");
    // L
    public static final COSName L = getPDFName("L");
    public static final COSName LAB = getPDFName("Lab");
    public static final COSName LANG = getPDFName("Lang");
    public static final COSName LAST = getPDFName("Last");
    public static final COSName LAST_CHAR = getPDFName("LastChar");
    public static final COSName LAST_MODIFIED = getPDFName("LastModified");
    public static final COSName LC = getPDFName("LC");
    public static final COSName LE = getPDFName("LE");
    public static final COSName LEADING = getPDFName("Leading");
    public static final COSName LEGAL_ATTESTATION = getPDFName("LegalAttestation");
    public static final COSName LENGTH = getPDFName("Length");
    public static final COSName LENGTH1 = getPDFName("Length1");
    public static final COSName LENGTH2 = getPDFName("Length2");
    public static final COSName LENGTH3 = getPDFName("Length3");
    public static final COSName LIGHTEN = getPDFName("Lighten");
    public static final COSName LIMITS = getPDFName("Limits");
    public static final COSName LINEARIZED = getPDFName("Linearized");
    public static final COSName LJ = getPDFName("LJ");
    public static final COSName LL = getPDFName("LL");
    public static final COSName LLE = getPDFName("LLE");
    public static final COSName LLO = getPDFName("LLO");
    public static final COSName LOCATION = getPDFName("Location");
    public static final COSName LUMINOSITY = getPDFName("Luminosity");
    public static final COSName LW = getPDFName("LW");
    public static final COSName LZW_DECODE = getPDFName("LZWDecode");
    public static final COSName LZW_DECODE_ABBREVIATION = getPDFName("LZW");
    // M
    public static final COSName M = getPDFName("M");
    public static final COSName MAC = getPDFName("Mac");
    public static final COSName MAC_EXPERT_ENCODING = getPDFName("MacExpertEncoding");
    public static final COSName MAC_ROMAN_ENCODING = getPDFName("MacRomanEncoding");
    public static final COSName MARK_INFO = getPDFName("MarkInfo");
    public static final COSName MASK = getPDFName("Mask");
    public static final COSName MATRIX = getPDFName("Matrix");
    public static final COSName MATTE = getPDFName("Matte");
    public static final COSName MAX_LEN = getPDFName("MaxLen");
    public static final COSName MAX_WIDTH = getPDFName("MaxWidth");
    public static final COSName MCID = getPDFName("MCID");
    public static final COSName MDP = getPDFName("MDP");
    public static final COSName MEDIA_BOX = getPDFName("MediaBox");
    public static final COSName MEASURE = getPDFName("Measure");
    public static final COSName METADATA = getPDFName("Metadata");
    public static final COSName MISSING_WIDTH = getPDFName("MissingWidth");
    public static final COSName MIX = getPDFName("Mix");
    public static final COSName MK = getPDFName("MK");
    public static final COSName ML = getPDFName("ML");
    public static final COSName MM_TYPE1 = getPDFName("MMType1");
    public static final COSName MOD_DATE = getPDFName("ModDate");
    public static final COSName MULTIPLY = getPDFName("Multiply");
    // N
    public static final COSName N = getPDFName("N");
    public static final COSName NAME = getPDFName("Name");
    public static final COSName NAMES = getPDFName("Names");
    public static final COSName NAVIGATOR = getPDFName("Navigator");
    public static final COSName NEED_APPEARANCES = getPDFName("NeedAppearances");
    public static final COSName NEW_WINDOW = getPDFName("NewWindow");
    public static final COSName NEXT = getPDFName("Next");
    public static final COSName NM = getPDFName("NM");
    public static final COSName NON_EFONT_NO_WARN = getPDFName("NonEFontNoWarn");
    public static final COSName NON_FULL_SCREEN_PAGE_MODE = getPDFName("NonFullScreenPageMode");
    public static final COSName NONE = getPDFName("None");
    public static final COSName NORMAL = getPDFName("Normal");
    public static final COSName NUMS = getPDFName("Nums");
    // O
    public static final COSName O = getPDFName("O");
    public static final COSName OBJ = getPDFName("Obj");
    public static final COSName OBJR = getPDFName("OBJR");
    public static final COSName OBJ_STM = getPDFName("ObjStm");
    public static final COSName OC = getPDFName("OC");
    public static final COSName OCG = getPDFName("OCG");
    public static final COSName OCGS = getPDFName("OCGs");
    public static final COSName OCMD = getPDFName("OCMD");
    public static final COSName OCPROPERTIES = getPDFName("OCProperties");
    public static final COSName OCSP = getPDFName("OCSP");
    public static final COSName OCSPS = getPDFName("OCSPs");
    public static final COSName OE = getPDFName("OE");
    public static final COSName OID = getPDFName("OID");
    
    /**
     * "OFF", to be used for OCGs, not for Acroform
     */
    public static final COSName OFF = getPDFName("OFF");
    
    /**
     * "Off", to be used for Acroform, not for OCGs
     */
    public static final COSName Off = getPDFName("Off");    
    
    public static final COSName ON = getPDFName("ON");
    public static final COSName OP = getPDFName("OP");
    public static final COSName OP_NS = getPDFName("op");
    public static final COSName OPEN_ACTION = getPDFName("OpenAction");
    public static final COSName OPEN_TYPE = getPDFName("OpenType");
    public static final COSName OPM = getPDFName("OPM");
    public static final COSName OPT = getPDFName("Opt");
    public static final COSName ORDER = getPDFName("Order");
    public static final COSName ORDERING = getPDFName("Ordering");
    public static final COSName OS = getPDFName("OS");
    public static final COSName OUTLINES = getPDFName("Outlines");
    public static final COSName OUTPUT_CONDITION = getPDFName("OutputCondition");
    public static final COSName OUTPUT_CONDITION_IDENTIFIER = getPDFName(
            "OutputConditionIdentifier");
    public static final COSName OUTPUT_INTENT = getPDFName("OutputIntent");
    public static final COSName OUTPUT_INTENTS = getPDFName("OutputIntents");
    public static final COSName OVERLAY = getPDFName("Overlay");
    // P
    public static final COSName P = getPDFName("P");
    public static final COSName PA = getPDFName("PA");
    public static final COSName PAGE = getPDFName("Page");
    public static final COSName PAGE_LABELS = getPDFName("PageLabels");
    public static final COSName PAGE_LAYOUT = getPDFName("PageLayout");
    public static final COSName PAGE_MODE = getPDFName("PageMode");
    public static final COSName PAGES = getPDFName("Pages");
    public static final COSName PAINT_TYPE = getPDFName("PaintType");
    public static final COSName PANOSE = getPDFName("Panose");    
    public static final COSName PARAMS = getPDFName("Params");
    public static final COSName PARENT = getPDFName("Parent");
    public static final COSName PARENT_TREE = getPDFName("ParentTree");
    public static final COSName PARENT_TREE_NEXT_KEY = getPDFName("ParentTreeNextKey");
    public static final COSName PART = getPDFName("Part");
    public static final COSName PATH = getPDFName("Path");
    public static final COSName PATTERN = getPDFName("Pattern");
    public static final COSName PATTERN_TYPE = getPDFName("PatternType");
    public static final COSName PC = getPDFName("PC");
    public static final COSName PDF_DOC_ENCODING = getPDFName("PDFDocEncoding");
    public static final COSName PERMS = getPDFName("Perms");
    public static final COSName PERCEPTUAL = getPDFName("Perceptual");
    public static final COSName PIECE_INFO = getPDFName("PieceInfo");
    public static final COSName PG = getPDFName("Pg");
    public static final COSName PI = getPDFName("PI");
    public static final COSName PO = getPDFName("PO");
    public static final COSName POPUP = getPDFName("Popup");
    public static final COSName PRE_RELEASE = getPDFName("PreRelease");
    public static final COSName PREDICTOR = getPDFName("Predictor");
    public static final COSName PREV = getPDFName("Prev");
    public static final COSName PRINT = getPDFName("Print");
    public static final COSName PRINT_AREA = getPDFName("PrintArea");
    public static final COSName PRINT_CLIP = getPDFName("PrintClip");
    public static final COSName PRINT_SCALING = getPDFName("PrintScaling");
    public static final COSName PRINT_STATE = getPDFName("PrintState");
    public static final COSName PRIVATE = getPDFName("Private");
    public static final COSName PROC_SET = getPDFName("ProcSet");
    public static final COSName PROCESS = getPDFName("Process");
    public static final COSName PRODUCER = getPDFName("Producer");
    public static final COSName PROP_BUILD = getPDFName("Prop_Build");
    public static final COSName PROPERTIES = getPDFName("Properties");
    public static final COSName PS = getPDFName("PS");
    public static final COSName PUB_SEC = getPDFName("PubSec");
    public static final COSName PV = getPDFName("PV");
    // Q
    public static final COSName Q = getPDFName("Q");
    public static final COSName QUADPOINTS = getPDFName("QuadPoints");
    // R
    public static final COSName R = getPDFName("R");
    public static final COSName RANGE = getPDFName("Range");
    public static final COSName RC = getPDFName("RC");
    public static final COSName RD = getPDFName("RD");
    public static final COSName REASON = getPDFName("Reason");
    public static final COSName REASONS = getPDFName("Reasons");
    public static final COSName RECIPIENTS = getPDFName("Recipients");
    public static final COSName RECT = getPDFName("Rect");
    public static final COSName REFERENCE = getPDFName("Reference");
    public static final COSName REGISTRY = getPDFName("Registry");
    public static final COSName REGISTRY_NAME = getPDFName("RegistryName");
    public static final COSName RELATIVE_COLORIMETRIC = getPDFName("RelativeColorimetric");
    public static final COSName RENAME = getPDFName("Rename");
    public static final COSName REPEAT = getPDFName("Repeat");
    public static final COSName RES_FORK = getPDFName("ResFork");
    public static final COSName RESOURCES = getPDFName("Resources");
    public static final COSName RGB = getPDFName("RGB");
    public static final COSName RI = getPDFName("RI");
    public static final COSName ROLE_MAP = getPDFName("RoleMap");
    public static final COSName ROOT = getPDFName("Root");
    public static final COSName ROTATE = getPDFName("Rotate");
    public static final COSName ROWS = getPDFName("Rows");
    public static final COSName RT = getPDFName("RT");
    public static final COSName RUN_LENGTH_DECODE = getPDFName("RunLengthDecode");
    public static final COSName RUN_LENGTH_DECODE_ABBREVIATION = getPDFName("RL");
    public static final COSName RV = getPDFName("RV");
    // S
    public static final COSName S = getPDFName("S");
    public static final COSName SA = getPDFName("SA");
    public static final COSName SATURATION = getPDFName("Saturation");
    public static final COSName SCHEMA = getPDFName("Schema");
    public static final COSName SCREEN = getPDFName("Screen");
    public static final COSName SE = getPDFName("SE");
    public static final COSName SEPARATION = getPDFName("Separation");
    public static final COSName SET_F = getPDFName("SetF");
    public static final COSName SET_FF = getPDFName("SetFf");
    public static final COSName SHADING = getPDFName("Shading");
    public static final COSName SHADING_TYPE = getPDFName("ShadingType");
    public static final COSName SIG = getPDFName("Sig");
    public static final COSName SIG_FLAGS = getPDFName("SigFlags");
    public static final COSName SIG_REF = getPDFName("SigRef");
    public static final COSName SIZE = getPDFName("Size");
    public static final COSName SM = getPDFName("SM");
    public static final COSName SMASK = getPDFName("SMask");
    public static final COSName SMASK_IN_DATA = getPDFName("SMaskInData");    
    public static final COSName SOFT_LIGHT = getPDFName("SoftLight");
    public static final COSName SORT = getPDFName("Sort");
    public static final COSName SOUND = getPDFName("Sound");
    public static final COSName SPLIT = getPDFName("Split");
    public static final COSName SS = getPDFName("SS");
    public static final COSName ST = getPDFName("St");
    public static final COSName STANDARD_ENCODING = getPDFName("StandardEncoding");
    public static final COSName STATE = getPDFName("State");
    public static final COSName STATE_MODEL = getPDFName("StateModel");
    public static final COSName STATUS = getPDFName("Status");
    public static final COSName STD_CF = getPDFName("StdCF");
    public static final COSName STEM_H = getPDFName("StemH");
    public static final COSName STEM_V = getPDFName("StemV");
    public static final COSName STM_F = getPDFName("StmF");
    public static final COSName STR_F = getPDFName("StrF");
    public static final COSName STRUCT_ELEM = getPDFName("StructElem");
    public static final COSName STRUCT_PARENT = getPDFName("StructParent");
    public static final COSName STRUCT_PARENTS = getPDFName("StructParents");
    public static final COSName STRUCT_TREE_ROOT = getPDFName("StructTreeRoot");
    public static final COSName STYLE = getPDFName("Style");
    public static final COSName SUB_FILTER = getPDFName("SubFilter");
    public static final COSName SUBJ = getPDFName("Subj");
    public static final COSName SUBJECT = getPDFName("Subject");
    public static final COSName SUBJECT_DN = getPDFName("SubjectDN");
    public static final COSName SUBTYPE = getPDFName("Subtype");
    public static final COSName SUPPLEMENT = getPDFName("Supplement");
    public static final COSName SV = getPDFName("SV");
    public static final COSName SV_CERT = getPDFName("SVCert");
    public static final COSName SW = getPDFName("SW");
    public static final COSName SY = getPDFName("Sy");
    public static final COSName SYNCHRONOUS = getPDFName("Synchronous");
    // T
    public static final COSName T = getPDFName("T");
    public static final COSName TARGET = getPDFName("Target");
    public static final COSName TEMPLATES = getPDFName("Templates");
    public static final COSName THREAD = getPDFName("Thread");
    public static final COSName THREADS = getPDFName("Threads");
    public static final COSName THREE_DD = getPDFName("3DD");
    public static final COSName THUMB = getPDFName("Thumb");
    public static final COSName TI = getPDFName("TI");
    public static final COSName TILING_TYPE = getPDFName("TilingType");
    public static final COSName TIME_STAMP = getPDFName("TimeStamp");
    public static final COSName TITLE = getPDFName("Title");
    public static final COSName TK = getPDFName("TK");
    public static final COSName TM = getPDFName("TM");
    public static final COSName TO_UNICODE = getPDFName("ToUnicode");
    public static final COSName TR = getPDFName("TR");
    public static final COSName TR2 = getPDFName("TR2");
    public static final COSName TRAPPED = getPDFName("Trapped");
    public static final COSName TRANS = getPDFName("Trans");
    public static final COSName TRANSFORM_METHOD = getPDFName("TransformMethod");
    public static final COSName TRANSFORM_PARAMS = getPDFName("TransformParams");
    public static final COSName TRANSPARENCY = getPDFName("Transparency");
    public static final COSName TREF = getPDFName("TRef");
    public static final COSName TRIM_BOX = getPDFName("TrimBox");
    public static final COSName TRUE_TYPE = getPDFName("TrueType");
    public static final COSName TRUSTED_MODE = getPDFName("TrustedMode");
    public static final COSName TU = getPDFName("TU");
    /** Acro form field type for text field. */
    public static final COSName TX = getPDFName("Tx");
    public static final COSName TYPE = getPDFName("Type");
    public static final COSName TYPE0 = getPDFName("Type0");
    public static final COSName TYPE1 = getPDFName("Type1");
    public static final COSName TYPE3 = getPDFName("Type3");
    // U
    public static final COSName U = getPDFName("U");
    public static final COSName UE = getPDFName("UE");
    public static final COSName UF = getPDFName("UF");
    public static final COSName UNCHANGED = getPDFName("Unchanged");
    public static final COSName UNIX = getPDFName("Unix");
    public static final COSName URI = getPDFName("URI");
    public static final COSName URL = getPDFName("URL");
    public static final COSName URL_TYPE = getPDFName("URLType");
    public static final COSName USAGE = getPDFName("Usage");
    public static final COSName USE_CMAP = getPDFName("UseCMap");
    public static final COSName USER_UNIT = getPDFName("UserUnit");
    // V
    public static final COSName V = getPDFName("V");
    public static final COSName VE = getPDFName("VE");
    public static final COSName VERISIGN_PPKVS = getPDFName("VeriSign.PPKVS");
    public static final COSName VERSION = getPDFName("Version");
    public static final COSName VERTICES = getPDFName("Vertices");
    public static final COSName VERTICES_PER_ROW = getPDFName("VerticesPerRow");
    public static final COSName VIEW = getPDFName("View");
    public static final COSName VIEW_AREA = getPDFName("ViewArea");
    public static final COSName VIEW_CLIP = getPDFName("ViewClip");
    public static final COSName VIEW_STATE = getPDFName("ViewState");
    public static final COSName VIEWER_PREFERENCES = getPDFName("ViewerPreferences");
    public static final COSName VOLUME = getPDFName("Volume");
    public static final COSName VP = getPDFName("VP");
    public static final COSName VRI = getPDFName("VRI");
    // W
    public static final COSName W = getPDFName("W");
    public static final COSName W2 = getPDFName("W2");
    public static final COSName WC = getPDFName("WC");
    public static final COSName WHITE_POINT = getPDFName("WhitePoint");
    public static final COSName WIDGET = getPDFName("Widget");
    public static final COSName WIDTH = getPDFName("Width");
    public static final COSName WIDTHS = getPDFName("Widths");
    public static final COSName WIN = getPDFName("Win");
    public static final COSName WIN_ANSI_ENCODING = getPDFName("WinAnsiEncoding");
    public static final COSName WMODE = getPDFName("WMode");
    public static final COSName WP = getPDFName("WP");
    public static final COSName WS = getPDFName("WS");
    // X
    public static final COSName X = getPDFName("X");
    public static final COSName XFA = getPDFName("XFA");
    public static final COSName X_STEP = getPDFName("XStep");
    public static final COSName XHEIGHT = getPDFName("XHeight");
    public static final COSName XOBJECT = getPDFName("XObject");
    public static final COSName XREF = getPDFName("XRef");
    public static final COSName XREF_STM = getPDFName("XRefStm");
    // Y
    public static final COSName Y = getPDFName("Y");
    public static final COSName Y_STEP = getPDFName("YStep");
    public static final COSName YES = getPDFName("Yes");

    // Z
    public static final COSName ZA_DB = getPDFName("ZaDb");

    // fields
    private final String name;

    /**
     * This will get a COSName object with that name.
     * 
     * @param aName The name of the object.
     * 
     * @return A COSName with the specified name.
     */
    public static COSName getPDFName(String aName)
    {
        WeakReference<COSName> weakRef = NAME_MAP.get(aName);
        COSName name = weakRef != null ? weakRef.get() : null;

        if (name == null)
        {
            // Although we use a ConcurrentHashMap, we cannot use computeIfAbsent() because the returned reference
            // might be stale (even the newly created one).
            // Use double checked locking to make the code thread safe.
            synchronized (NAME_MAP)
            {
                weakRef = NAME_MAP.get(aName);
                name = weakRef != null ? weakRef.get() : null;
                if (name == null)
                {
                    name = new COSName(aName);
                    CLEANER.register(name, () -> NAME_MAP.remove(aName));
                    NAME_MAP.put(aName, new WeakReference<>(name));
                }
            }
        }

        return name;
    }

    /**
     * Private constructor. This will limit the number of COSName objects that are created.
     * 
     * @param aName The name of the COSName object.
     */
    private COSName(String aName)
    {
        this.name = aName;
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
        return name.hashCode();
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
    public void accept(ICOSVisitor visitor) throws IOException
    {
        visitor.visitFromName(this);
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
        byte[] bytes = getName().getBytes(StandardCharsets.UTF_8);
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

}

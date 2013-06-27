/*****************************************************************************
 * 
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * 
 ****************************************************************************/

package org.apache.pdfbox.preflight;

/**
 * This interface provide a set of constants which identify validation error.
 */
public interface PreflightConstants
{

    String FORMAT_PDF_A1B = "PDF/A1-b";
    String FORMAT_PDF_A1A = "PDF/A1-a";

    // -----------------------------------------------------------
    // ---- CONSTANTS
    // -----------------------------------------------------------
    int EDOC_TOKEN_MGR_ERROR = 255;
    String EDOC_TOKEN_MGR_ERROR_TAG = " ERROR_CODE: ";
    int MAX_DICT_ENTRIES = 4095;
    int MAX_ARRAY_ELEMENTS = 8191;
    int MAX_NAME_SIZE = 127;
    int MAX_STRING_LENGTH = 65535;
    int MAX_INDIRECT_OBJ = 8388607;
    int MAX_CID = 65535;
    int MAX_GRAPHIC_STATES = 28;
    int MAX_DEVICE_N_LIMIT = 8;
    float MAX_POSITIVE_FLOAT = 32767f;
    float MAX_NEGATIVE_FLOAT = -32767f;

    String TRAILER_DICTIONARY_KEY_ID = "ID";
    String TRAILER_DICTIONARY_KEY_SIZE = "Size";
    String TRAILER_DICTIONARY_KEY_PREV = "Prev";
    String TRAILER_DICTIONARY_KEY_ROOT = "Root";
    String TRAILER_DICTIONARY_KEY_INFO = "Info";
    String TRAILER_DICTIONARY_KEY_ENCRYPT = "Encrypt";

    String DICTIONARY_KEY_ADDITIONAL_ACTION = "AA";
    String DICTIONARY_KEY_OPEN_ACTION = "OpenAction";

    String DOCUMENT_DICTIONARY_KEY_OUTPUT_INTENTS = "OutputIntents";
    String DOCUMENT_DICTIONARY_KEY_OPTIONAL_CONTENTS = "OCProperties";

    String OUTPUT_INTENT_DICTIONARY_KEY_S = "S";
    String OUTPUT_INTENT_DICTIONARY_KEY_INFO = "Info";
    String OUTPUT_INTENT_DICTIONARY_VALUE_GTS_PDFA1 = "GTS_PDFA1";
    String OUTPUT_INTENT_DICTIONARY_KEY_DEST_OUTPUT_PROFILE = "DestOutputProfile";
    String OUTPUT_INTENT_DICTIONARY_KEY_OUTPUT_CONDITION_IDENTIFIER = "OutputConditionIdentifier";
    String OUTPUT_INTENT_DICTIONARY_VALUE_OUTPUT_CONDITION_IDENTIFIER_CUSTOM = "Custom";

    String TRANPARENCY_DICTIONARY_KEY_EXTGSTATE = "ExtGState";
    String TRANPARENCY_DICTIONARY_KEY_EXTGSTATE_ENTRY_REGEX = "(GS|gs)([0-9])+";

    String TRANSPARENCY_DICTIONARY_KEY_BLEND_MODE = "BM";
    String TRANSPARENCY_DICTIONARY_KEY_UPPER_CA = "CA";
    String TRANSPARENCY_DICTIONARY_KEY_LOWER_CA = "ca";
    String TRANSPARENCY_DICTIONARY_VALUE_SOFT_MASK_NONE = "None";
    String TRANSPARENCY_DICTIONARY_VALUE_BM_NORMAL = "Normal";
    String TRANSPARENCY_DICTIONARY_VALUE_BM_COMPATIBLE = "Compatible";

    String DICTIONARY_KEY_LINEARIZED = "Linearized";
    String DICTIONARY_KEY_LINEARIZED_L = "L";
    String DICTIONARY_KEY_LINEARIZED_H = "H";
    String DICTIONARY_KEY_LINEARIZED_O = "O";
    String DICTIONARY_KEY_LINEARIZED_E = "E";
    String DICTIONARY_KEY_LINEARIZED_N = "N";
    String DICTIONARY_KEY_LINEARIZED_T = "T";

    String DICTIONARY_KEY_XOBJECT = "XObject";
    String DICTIONARY_KEY_PATTERN = "Pattern";

    String DICTIONARY_KEY_PATTERN_TYPE = "PatternType";
    int DICTIONARY_PATTERN_TILING = 1;
    int DICTIONARY_PATTERN_SHADING = 2;

    String PATTERN_KEY_PAINT_TYPE = "PaintType";
    String PATTERN_KEY_TILING_TYPE = "TilingType";
    String PATTERN_KEY_BBOX = "BBox";
    String PATTERN_KEY_XSTEP = "XStep";
    String PATTERN_KEY_YSTEP = "YStep";
    String PATTERN_KEY_SHADING = "Shading";
    String PATTERN_KEY_SHADING_TYPE = "ShadingType";

    String XOBJECT_DICTIONARY_VALUE_SUBTYPE_IMG = "Image";
    String XOBJECT_DICTIONARY_VALUE_SUBTYPE_FORM = "Form";
    String XOBJECT_DICTIONARY_VALUE_SUBTYPE_POSTSCRIPT = "PS";
    String XOBJECT_DICTIONARY_KEY_BBOX = "BBox";
    String XOBJECT_DICTIONARY_KEY_GROUP = "Group";
    String XOBJECT_DICTIONARY_VALUE_S_TRANSPARENCY = "Transparency";
    String PAGE_DICTIONARY_VALUE_THUMB = "Thumb";

    String FONT_DICTIONARY_VALUE_FONT = "Font";
    String FONT_DICTIONARY_VALUE_COMPOSITE = "Type0";
    String FONT_DICTIONARY_VALUE_TRUETYPE = "TrueType";
    String FONT_DICTIONARY_VALUE_TYPE1 = "Type1";
    String FONT_DICTIONARY_VALUE_TYPE1C = "Type1C";
    String FONT_DICTIONARY_VALUE_MMTYPE = "MMType1";
    String FONT_DICTIONARY_VALUE_TYPE3 = "Type3";
    String FONT_DICTIONARY_VALUE_TYPE0 = "CIDFontType0";
    String FONT_DICTIONARY_VALUE_TYPE0C = "CIDFontType0C";
    String FONT_DICTIONARY_VALUE_TYPE2 = "CIDFontType2";
    String FONT_DICTIONARY_VALUE_ENCODING_MAC = "MacRomanEncoding";
    String FONT_DICTIONARY_VALUE_ENCODING_MAC_EXP = "MacExpertEncoding";
    String FONT_DICTIONARY_VALUE_ENCODING_WIN = "WinAnsiEncoding";
    String FONT_DICTIONARY_VALUE_ENCODING_STD = "StandardEncoding";
    String FONT_DICTIONARY_VALUE_ENCODING_PDFDOC = "PDFDocEncoding";

    String FONT_DICTIONARY_VALUE_ENCODING = "Encoding";
    String FONT_DICTIONARY_VALUE_CMAP_IDENTITY_H = "Identity-H";
    String FONT_DICTIONARY_VALUE_CMAP_IDENTITY_V = "Identity-V";
    String FONT_DICTIONARY_VALUE_CMAP_IDENTITY = "Identity";
    String FONT_DICTIONARY_VALUE_TYPE_CMAP = "CMap";

    String FONT_DICTIONARY_KEY_NAME = "Name";
    String FONT_DICTIONARY_KEY_BASEFONT = "BaseFont";
    String FONT_DICTIONARY_KEY_FIRSTCHAR = "FirstChar";
    String FONT_DICTIONARY_KEY_LASTCHAR = "LastChar";
    String FONT_DICTIONARY_KEY_WIDTHS = "Widths";
    String FONT_DICTIONARY_KEY_FONT_DESC = "FontDescriptor";
    String FONT_DICTIONARY_KEY_ENCODING = "Encoding";
    String FONT_DICTIONARY_KEY_TOUNICODE = "ToUnicode";
    String FONT_DICTIONARY_KEY_FONTNAME = "FontName";
    String FONT_DICTIONARY_KEY_FLAGS = "Flags";
    String FONT_DICTIONARY_KEY_ITALICANGLE = "ItalicAngle";
    String FONT_DICTIONARY_KEY_FONTBBOX = "FontBBox";
    String FONT_DICTIONARY_KEY_FONTMATRIX = "FontMatrix";
    String FONT_DICTIONARY_KEY_CHARPROCS = "CharProcs";
    String FONT_DICTIONARY_KEY_ASCENT = "Ascent";
    String FONT_DICTIONARY_KEY_DESCENT = "Descent";
    String FONT_DICTIONARY_KEY_CAPHEIGHT = "CapHeight";
    String FONT_DICTIONARY_KEY_STEMV = "StemV";
    String FONT_DICTIONARY_KEY_LENGTH2 = "Length2";
    String FONT_DICTIONARY_KEY_LENGTH3 = "Length3";
    String FONT_DICTIONARY_KEY_METADATA = "Metadata";
    String FONT_DICTIONARY_KEY_BASEENCODING = "BaseEncoding";
    String FONT_DICTIONARY_KEY_DESCENDANT_FONTS = "DescendantFonts";
    String FONT_DICTIONARY_KEY_CID_GIDMAP = "CIDToGIDMap";
    String FONT_DICTIONARY_KEY_CMAP_NAME = "CMapName";
    String FONT_DICTIONARY_KEY_CMAP_WMODE = "WMode";
    String FONT_DICTIONARY_KEY_CMAP_USECMAP = "UseCMap";
    String FONT_DICTIONARY_KEY_CIDSET = "CIDSet";
    int FONT_DICTIONARY_DEFAULT_CMAP_WMODE = 0;

    String STREAM_DICTIONARY_KEY_DECODEPARAMS = "DecodeParms";

    String STREAM_DICTIONARY_VALUE_FILTER_LZW = "LZWDecode";

    String STREAM_DICTIONARY_VALUE_FILTER_ASCII_HEX = "ASCIIHexDecode";
    String STREAM_DICTIONARY_VALUE_FILTER_ASCII_85 = "ASCII85Decode";
    String STREAM_DICTIONARY_VALUE_FILTER_RUN = "RunLengthDecode";
    String STREAM_DICTIONARY_VALUE_FILTER_CCITTFF = "CCITTFaxDecode";
    String STREAM_DICTIONARY_VALUE_FILTER_JBIG = "JBIG2Decode";
    String STREAM_DICTIONARY_VALUE_FILTER_DCT = "DCTDecode";
    String STREAM_DICTIONARY_VALUE_FILTER_FLATE_DECODE = "FlateDecode";

    String FILE_SPECIFICATION_VALUE_TYPE = "Filespec";
    String FILE_SPECIFICATION_KEY_EMBEDDED_FILE = "EF";

    String INLINE_DICTIONARY_VALUE_FILTER_LZW = "LZW";

    String INLINE_DICTIONARY_VALUE_FILTER_ASCII_HEX = "AHx";
    String INLINE_DICTIONARY_VALUE_FILTER_ASCII_85 = "A85";
    String INLINE_DICTIONARY_VALUE_FILTER_RUN = "RL";
    String INLINE_DICTIONARY_VALUE_FILTER_CCITTFF = "CCF";
    String INLINE_DICTIONARY_VALUE_FILTER_DCT = "DCT";
    String INLINE_DICTIONARY_VALUE_FILTER_FLATE_DECODE = "Fl";

    String ANNOT_DICTIONARY_KEY_QUADPOINTS = "QuadPoints";
    String ANNOT_DICTIONARY_KEY_INKLIST = "InkList";

    String ANNOT_DICTIONARY_VALUE_TYPE = "Annot";
    String ANNOT_DICTIONARY_VALUE_SUBTYPE_TEXT = "Text";
    String ANNOT_DICTIONARY_VALUE_SUBTYPE_LINK = "Link";
    String ANNOT_DICTIONARY_VALUE_SUBTYPE_FREETEXT = "FreeText";
    String ANNOT_DICTIONARY_VALUE_SUBTYPE_LINE = "Line";
    String ANNOT_DICTIONARY_VALUE_SUBTYPE_SQUARE = "Square";
    String ANNOT_DICTIONARY_VALUE_SUBTYPE_CIRCLE = "Circle";
    String ANNOT_DICTIONARY_VALUE_SUBTYPE_HIGHLIGHT = "Highlight";
    String ANNOT_DICTIONARY_VALUE_SUBTYPE_UNDERLINE = "Underline";
    String ANNOT_DICTIONARY_VALUE_SUBTYPE_SQUILGGLY = "Squiggly";
    String ANNOT_DICTIONARY_VALUE_SUBTYPE_STRIKEOUT = "StrikeOut";
    String ANNOT_DICTIONARY_VALUE_SUBTYPE_STAMP = "Stamp";
    String ANNOT_DICTIONARY_VALUE_SUBTYPE_INK = "Ink";
    String ANNOT_DICTIONARY_VALUE_SUBTYPE_POPUP = "Popup";
    String ANNOT_DICTIONARY_VALUE_SUBTYPE_WIDGET = "Widget";
    String ANNOT_DICTIONARY_VALUE_SUBTYPE_PRINTERMARK = "PrinterMark";
    String ANNOT_DICTIONARY_VALUE_SUBTYPE_TRAPNET = "TrapNet";

    String ACTION_DICTIONARY_VALUE_TYPE = "Action";
    String ACTION_DICTIONARY_KEY_NEXT = "Next";
    String ACTION_DICTIONARY_VALUE_ATYPE_GOTO = "GoTo";
    String ACTION_DICTIONARY_VALUE_ATYPE_GOTOR = "GoToR";
    String ACTION_DICTIONARY_VALUE_ATYPE_THREAD = "Thread";
    String ACTION_DICTIONARY_VALUE_ATYPE_URI = "URI";
    String ACTION_DICTIONARY_VALUE_ATYPE_HIDE = "Hide";
    String ACTION_DICTIONARY_VALUE_ATYPE_NAMED = "Named";
    String ACTION_DICTIONARY_VALUE_ATYPE_SUBMIT = "SubmitForm";
    String ACTION_DICTIONARY_VALUE_ATYPE_LAUNCH = "Launch";
    String ACTION_DICTIONARY_VALUE_ATYPE_SOUND = "Sound";
    String ACTION_DICTIONARY_VALUE_ATYPE_MOVIE = "Movie";
    String ACTION_DICTIONARY_VALUE_ATYPE_RESET = "ResetForm";
    String ACTION_DICTIONARY_VALUE_ATYPE_IMPORT = "ImportData";
    String ACTION_DICTIONARY_VALUE_ATYPE_JAVASCRIPT = "JavaScript";
    String ACTION_DICTIONARY_VALUE_ATYPE_SETSTATE = "SetState";
    String ACTION_DICTIONARY_VALUE_ATYPE_NOOP = "NOP";
    String ACTION_DICTIONARY_VALUE_ATYPE_NAMED_NEXT = "NextPage";
    String ACTION_DICTIONARY_VALUE_ATYPE_NAMED_PREV = "PrevPage";
    String ACTION_DICTIONARY_VALUE_ATYPE_NAMED_FIRST = "FirstPage";
    String ACTION_DICTIONARY_VALUE_ATYPE_NAMED_LAST = "LastPage";

    String ACROFORM_DICTIONARY_KEY_NEED_APPEARANCES = "NeedAppearances";

    String RENDERING_INTENT_REL_COLOR = "RelativeColorimetric";
    String RENDERING_INTENT_ABS_COLOR = "AbsoluteColorimetric";
    String RENDERING_INTENT_PERCEPTUAL = "Perceptual";
    String RENDERING_INTENT_SATURATION = "Saturation";

    String ICC_Characterization_Data_Registry_FOGRA43 = "FOGRA43";
    String ICC_Characterization_Data_Registry_CGATS_TR_006 = "CGATS TR 006";
    String ICC_Characterization_Data_Registry_CGATS_TR006 = "CGATS TR006";
    String ICC_Characterization_Data_Registry_FOGRA39 = "FOGRA39";
    String ICC_Characterization_Data_Registry_JC200103 = "JC200103";
    String ICC_Characterization_Data_Registry_FOGRA27 = "FOGRA27";
    String ICC_Characterization_Data_Registry_EUROSB104 = "EUROSB104";
    String ICC_Characterization_Data_Registry_FOGRA45 = "FOGRA45";
    String ICC_Characterization_Data_Registry_FOGRA46 = "FOGRA46";
    String ICC_Characterization_Data_Registry_FOGRA41 = "FOGRA41";
    String ICC_Characterization_Data_Registry_CGATS_TR_001 = "CGATS TR 001";
    String ICC_Characterization_Data_Registry_CGATS_TR001 = "CGATS TR001";
    String ICC_Characterization_Data_Registry_CGATS_TR_003 = "CGATS TR 003";
    String ICC_Characterization_Data_Registry_CGATS_TR003 = "CGATS TR003";
    String ICC_Characterization_Data_Registry_CGATS_TR_005 = "CGATS TR 005";
    String ICC_Characterization_Data_Registry_CGATS_TR005 = "CGATS TR005";
    String ICC_Characterization_Data_Registry_FOGRA28 = "FOGRA28";
    String ICC_Characterization_Data_Registry_JCW2003 = "JCW2003";
    String ICC_Characterization_Data_Registry_EUROSB204 = "EUROSB204";
    String ICC_Characterization_Data_Registry_FOGRA47 = "FOGRA47";
    String ICC_Characterization_Data_Registry_FOGRA44 = "FOGRA44";
    String ICC_Characterization_Data_Registry_FOGRA29 = "FOGRA29";
    String ICC_Characterization_Data_Registry_JC200104 = "JC200104";
    String ICC_Characterization_Data_Registry_FOGRA40 = "FOGRA40";
    String ICC_Characterization_Data_Registry_FOGRA30 = "FOGRA30";
    String ICC_Characterization_Data_Registry_FOGRA42 = "FOGRA42";
    String ICC_Characterization_Data_Registry_IFRA26 = "IFRA26";
    String ICC_Characterization_Data_Registry_JCN2002 = "JCN2002";
    String ICC_Characterization_Data_Registry_CGATS_TR_002 = "CGATS TR 002";
    String ICC_Characterization_Data_Registry_CGATS_TR002 = "CGATS TR002";
    String ICC_Characterization_Data_Registry_FOGRA33 = "FOGRA33";
    String ICC_Characterization_Data_Registry_FOGRA37 = "FOGRA37";
    String ICC_Characterization_Data_Registry_FOGRA31 = "FOGRA31";
    String ICC_Characterization_Data_Registry_FOGRA35 = "FOGRA35";
    String ICC_Characterization_Data_Registry_FOGRA32 = "FOGRA32";
    String ICC_Characterization_Data_Registry_FOGRA34 = "FOGRA34";
    String ICC_Characterization_Data_Registry_FOGRA36 = "FOGRA36";
    String ICC_Characterization_Data_Registry_FOGRA38 = "FOGRA38";
    String ICC_Characterization_Data_Registry_sRGB = "sRGB";
    String ICC_Characterization_Data_Registry_sRGB_IEC = "sRGB IEC61966-2.1";
    String ICC_Characterization_Data_Registry_Adobe = "Adobe RGB (1998)";
    String ICC_Characterization_Data_Registry_bg_sRGB = "bg-sRGB";
    String ICC_Characterization_Data_Registry_sYCC = "sYCC";
    String ICC_Characterization_Data_Registry_scRGB = "scRGB";
    String ICC_Characterization_Data_Registry_scRGB_nl = "scRGB-nl";
    String ICC_Characterization_Data_Registry_scYCC_nl = "scYCC-nl";
    String ICC_Characterization_Data_Registry_ROMM = "ROMM RGB";
    String ICC_Characterization_Data_Registry_RIMM = "RIMM RGB";
    String ICC_Characterization_Data_Registry_ERIMM = "ERIMM RGB";
    String ICC_Characterization_Data_Registry_eciRGB = "eciRGB";
    String ICC_Characterization_Data_Registry_opRGB = "opRGB";
    /**
     * Error code uses by the Valdiator when there are an error without error code.
     */
    String ERROR_UNKOWN_ERROR = "-1";

    // -----------------------------------------------------------
    // ---- FILE STRUCTURE ERRORS 1.x...
    // -----------------------------------------------------------

    /**
     * Error code for syntax error
     */
    String ERROR_SYNTAX_MAIN = "1";
    // error code category which can occur in each pdf part
    String ERROR_SYNTAX_COMMON = "1.0";
    /**
     * Too many entries in a dictionary object
     */
    String ERROR_SYNTAX_TOO_MANY_ENTRIES = "1.0.1";
    /**
     * Too many element in an array object
     */
    String ERROR_SYNTAX_ARRAY_TOO_LONG = "1.0.2";
    /**
     * The name length is too long
     */
    String ERROR_SYNTAX_NAME_TOO_LONG = "1.0.3";
    /**
     * The literal string is too long
     */
    String ERROR_SYNTAX_LITERAL_TOO_LONG = "1.0.4";
    /**
     * The hexa string is too long
     */
    String ERROR_SYNTAX_HEXA_STRING_TOO_LONG = "1.0.5";
    /**
     * The number is out of Range ( ex : greatter than 2^31-1)
     */
    String ERROR_SYNTAX_NUMERIC_RANGE = "1.0.6";
    /**
     * A dictionary key isn't a name
     */
    String ERROR_SYNTAX_DICTIONARY_KEY_INVALID = "1.0.7";
    /**
     * The language declared doesn't match with the RFC1766
     */
    String ERROR_SYNTAX_LANG_NOT_RFC1766 = "1.0.8";
    /**
     * There are too many objects
     */
    String ERROR_SYNTAX_INDIRECT_OBJ_RANGE = "1.0.9";
    /**
     * CID too long
     */
    String ERROR_SYNTAX_CID_RANGE = "1.0.10";
    /**
     * Hexa string shall contain even number of non white space char
     */
    String ERROR_SYNTAX_HEXA_STRING_EVEN_NUMBER = "1.0.11";
    /**
     * Hexa string contain non hexadecimal characters
     */
    String ERROR_SYNTAX_HEXA_STRING_INVALID = "1.0.12";
    /**
     * An object is missing from the document
     */
    String ERROR_SYNTAX_MISSING_OFFSET = "1.0.13";
    /**
     * An object has an invalid offset
     */
    String ERROR_SYNTAX_INVALID_OFFSET = "1.0.14";

    String ERROR_SYNTAX_HEADER = "1.1";

    String ERROR_SYNTAX_HEADER_FIRST_CHAR = "1.1.1";

    String ERROR_SYNTAX_HEADER_FILE_TYPE = "1.1.2";

    /**
     * Common error about body syntax
     */
    String ERROR_SYNTAX_BODY = "1.2";
    /**
     * Error on the object delimiters (obj / endobj)
     */
    String ERROR_SYNTAX_OBJ_DELIMITER = "1.2.1";
    /**
     * Error on the stream delimiters (stream / endstream)
     */
    String ERROR_SYNTAX_STREAM_DELIMITER = "1.2.2";
    /**
     * Required fields are missing from the dictionary
     */
    String ERROR_SYNTAX_DICT_INVALID = "1.2.3";
    /**
     * The length entry is missing from the stream dictionary
     */
    String ERROR_SYNTAX_STREAM_LENGTH_MISSING = "1.2.4";
    /**
     * The length of the stream dictionary and the stream length is inconsistent
     */
    String ERROR_SYNTAX_STREAM_LENGTH_INVALID = "1.2.5";
    /**
     * F or/and FFilter or/and FDecodeParams are present in a stream dictionary
     */
    String ERROR_SYNTAX_STREAM_FX_KEYS = "1.2.6";
    /**
     * The stream uses an invalid filter (The LZW)
     */
    String ERROR_SYNTAX_STREAM_INVALID_FILTER = "1.2.7";
    /**
     * The content stream has some syntax errors
     */
    String ERROR_SYNTAX_CONTENT_STREAM_INVALID = "1.2.8";
    /**
     * EmbeddedFile entry is present in a FileSpecification dictionary
     */
    String ERROR_SYNTAX_EMBEDDED_FILES = "1.2.9";
    /**
     * The content stream uses an unsupported operator
     */
    String ERROR_SYNTAX_CONTENT_STREAM_UNSUPPORTED_OP = "1.2.10";
    /**
     * The content stream contains an invalid argument for the operator
     */
    String ERROR_SYNTAX_CONTENT_STREAM_INVALID_ARGUMENT = "1.2.11";
    /**
     * The stream uses a filter which isn't defined in the PDF Reference document.
     */
    String ERROR_SYNTAX_STREAM_UNDEFINED_FILTER = "1.2.12";
    /**
     * The stream can't be processed
     */
    String ERROR_SYNTAX_STREAM_DAMAGED = "1.2.13";
    /**
     * There are no catalog dictionary in the PDF File
     */
    String ERROR_SYNTAX_NOCATALOG = "1.2.14";
    /**
     * Common error about the cross ref table
     */
    String ERROR_SYNTAX_CROSS_REF = "1.3";
    /**
     * Common error about the trailer
     */
    String ERROR_SYNTAX_TRAILER = "1.4";
    /**
     * ID is missing from the trailer
     */
    String ERROR_SYNTAX_TRAILER_MISSING_ID = "1.4.1";
    /**
     * Encrypt is forbidden
     */
    String ERROR_SYNTAX_TRAILER_ENCRYPT = "1.4.2";
    /**
     * An trailer entry has an invalid type
     */
    String ERROR_SYNTAX_TRAILER_TYPE_INVALID = "1.4.3";
    /**
     * Size is missing from the trailer
     */
    String ERROR_SYNTAX_TRAILER_MISSING_SIZE = "1.4.4";
    /**
     * Root is missing from the trailer
     */
    String ERROR_SYNTAX_TRAILER_MISSING_ROOT = "1.4.5";
    /**
     * ID in 1st trailer and the last is different
     */
    String ERROR_SYNTAX_TRAILER_ID_CONSISTENCY = "1.4.6";
    /**
     * EmbeddedFile entry is present in the Names dictionary
     */
    String ERROR_SYNTAX_TRAILER_CATALOG_EMBEDDEDFILES = "1.4.7";
    /**
     * Optional content is forbidden
     */
    String ERROR_SYNTAX_TRAILER_CATALOG_OCPROPERTIES = "1.4.8";
    /**
     * Errors in the Outlines dictionary
     */
    String ERROR_SYNTAX_TRAILER_OUTLINES_INVALID = "1.4.9";
    /**
     * Last %%EOF sequence is followed by data
     */
    String ERROR_SYNTAX_TRAILER_EOF = "1.4.10";

    // -----------------------------------------------------------
    // ---- GRAPHIC ERRORS 2.x...
    // -----------------------------------------------------------

    /**
     * Main error code for graphical problems
     */
    String ERROR_GRAPHIC_MAIN = "2";
    
    String ERROR_GRAPHIC_INVALID = "2.1";
    /**
     * BBox Entry of a Form XObject is missing or isn't an Array
     */
    String ERROR_GRAPHIC_INVALID_BBOX = "2.1.1";
    /**
     * The OutputIntent dictionary is invalid
     */
    String ERROR_GRAPHIC_OUTPUT_INTENT_INVALID_ENTRY = "2.1.2";
    /**
     * The S entry of the OutputIntent isn't GTS_PDFA1
     */
    String ERROR_GRAPHIC_OUTPUT_INTENT_S_VALUE_INVALID = "2.1.3";
    /**
     * The ICC Profile is invalid
     */
    String ERROR_GRAPHIC_OUTPUT_INTENT_ICC_PROFILE_INVALID = "2.1.4";
    /**
     * There are more than one ICC Profile
     */
    String ERROR_GRAPHIC_OUTPUT_INTENT_ICC_PROFILE_MULTIPLE = "2.1.5";

    /**
     * Profile version is too recent for PDF 1.4 document
     */
    String ERROR_GRAPHIC_OUTPUT_INTENT_ICC_PROFILE_TOO_RECENT = "2.1.6";

    String ERROR_GRAPHIC_MISSING_FIELD = "2.1.7";

    String ERROR_GRAPHIC_TOO_MANY_GRAPHIC_STATES = "2.1.8";

    String ERROR_GRAPHIC_MISSING_OBJECT = "2.1.9";
    
    String ERROR_GRAPHIC_XOBJECT_INVALID_TYPE = "2.1.10";
    
    /**
     * Main error code for graphical transparency problems
     */
    String ERROR_GRAPHIC_TRANSPARENCY = "2.2";
    /**
     * A Group entry with S = Transparency is used or the S = Null
     */
    String ERROR_GRAPHIC_TRANSPARENCY_GROUP = "2.2.1";
    /**
     * A XObject SMask value isn't None
     */
    String ERROR_GRAPHIC_TRANSPARENCY_SMASK = "2.2.2";

    /**
     * A XObject has an unexpected key defined
     */
    String ERROR_GRAPHIC_UNEXPECTED_KEY = "2.3";

    /**
     * A XObject has an unexpected value for a defined key
     */
    String ERROR_GRAPHIC_UNEXPECTED_VALUE_FOR_KEY = "2.3.2";

    /**
     * An invalid color space is used
     */
    String ERROR_GRAPHIC_INVALID_COLOR_SPACE = "2.4";
    /**
     * RGB color space used in the PDF file but the DestOutputProfile isn't RGB
     */
    String ERROR_GRAPHIC_INVALID_COLOR_SPACE_RGB = "2.4.1";
    /**
     * CMYK color space used in the PDF file but the DestOutputProfile isn't CMYK
     */
    String ERROR_GRAPHIC_INVALID_COLOR_SPACE_CMYK = "2.4.2";
    /**
     * color space used in the PDF file but the DestOutputProfile is missing
     */
    String ERROR_GRAPHIC_INVALID_COLOR_SPACE_MISSING = "2.4.3";
    /**
     * Unknown ColorSpace
     */
    String ERROR_GRAPHIC_INVALID_UNKNOWN_COLOR_SPACE = "2.4.4";
    /**
     * The pattern color space can't be used
     */
    String ERROR_GRAPHIC_INVALID_PATTERN_COLOR_SPACE_FORBIDDEN = "2.4.5";
    /**
     * The pattern is invalid due to missing key or invalid value
     */
    String ERROR_GRAPHIC_INVALID_PATTERN_DEFINITION = "2.4.6";
    /**
     * alternate color space used in the PDF file but the DestOutputProfile isn't consistent
     */
    String ERROR_GRAPHIC_INVALID_COLOR_SPACE_ALTERNATE = "2.4.7";
    /**
     * Base ColorSpace in the Indexed color space is invalid
     */
    String ERROR_GRAPHIC_INVALID_COLOR_SPACE_INDEXED = "2.4.8";
    /**
     * ColorSpace is forbidden due to some restriction (ex : Only DeviceXXX are auth in inlined image)
     */
    String ERROR_GRAPHIC_INVALID_COLOR_SPACE_FORBIDDEN = "2.4.9";

    String ERROR_GRAPHIC_INVALID_COLOR_SPACE_TOO_MANY_COMPONENTS_DEVICEN = "2.4.10";
    /**
     * ICC Based color space used in the PDF file is invalid
     */
    String ERROR_GRAPHIC_INVALID_COLOR_SPACE_ICCBASED = "2.4.11";
    /**
     * Validation asked on a missing ColorSpace
     */
    String ERROR_GRAPHIC_MISSING_COLOR_SPACE_ICCBASED = "2.4.12";
    // -----------------------------------------------------------
    // ---- FONT ERRORS 3.x...
    // -----------------------------------------------------------

    /**
     * Main error code for font problems
     */
    String ERROR_FONTS_MAIN = "3";

    String ERROR_FONTS_INVALID_DATA = "3.1";
    /**
     * Some mandatory fields are missing from the FONT Dictionary
     */
    String ERROR_FONTS_DICTIONARY_INVALID = "3.1.1";
    /**
     * Some mandatory fields are missing from the FONT Descriptor Dictionary
     */
    String ERROR_FONTS_DESCRIPTOR_INVALID = "3.1.2";
    /**
     * Error on the "Font File x" in the Font Descriptor (ex : FontFile and FontFile2 are present in the same
     * dictionary)
     */
    String ERROR_FONTS_FONT_FILEX_INVALID = "3.1.3";
    /**
     * Charset declaration is missing in a Type 1 Subset
     */
    String ERROR_FONTS_CHARSET_MISSING_FOR_SUBSET = "3.1.4";
    /**
     * Encoding is inconsistent with the Font (ex : Symbolic TrueType mustn't declare encoding)
     */
    String ERROR_FONTS_ENCODING = "3.1.5";
    /**
     * Width array and Font program Width are inconsistent
     */
    String ERROR_FONTS_METRICS = "3.1.6";
    /**
     * Required entry in a Composite Font dictionary is missing
     */
    String ERROR_FONTS_CIDKEYED_INVALID = "3.1.7";
    /**
     * The CIDSystemInfo dictionary is invalid
     */
    String ERROR_FONTS_CIDKEYED_SYSINFO = "3.1.8";
    /**
     * The CIDToGID is invalid
     */
    String ERROR_FONTS_CIDKEYED_CIDTOGID = "3.1.9";
    /**
     * The CMap of the Composite Font is missing or invalid
     */
    String ERROR_FONTS_CIDKEYED_CMAP_INVALID_OR_MISSING = "3.1.10";
    /**
     * The CIDSet entry i mandatory from a subset of composite font
     */
    String ERROR_FONTS_CIDSET_MISSING_FOR_SUBSET = "3.1.11";
    /**
     * The CMap of the Composite Font is missing or invalid
     */
    String ERROR_FONTS_ENCODING_ERROR = "3.1.12";
    /**
     * Encoding entry can't be read due to IOException
     */
    String ERROR_FONTS_ENCODING_IO = "3.1.13";
    /**
     * The font type is unknown
     */
    String ERROR_FONTS_UNKNOWN_FONT_TYPE = "3.1.14";
    /**
     * The embedded font is damaged
     */
    String ERROR_FONTS_DAMAGED = "3.2";
    /**
     * The embedded Type1 font is damaged
     */
    String ERROR_FONTS_TYPE1_DAMAGED = "3.2.1";
    /**
     * The embedded TrueType font is damaged
     */
    String ERROR_FONTS_TRUETYPE_DAMAGED = "3.2.2";
    /**
     * The embedded composite font is damaged
     */
    String ERROR_FONTS_CID_DAMAGED = "3.2.3";
    /**
     * The embedded type 3 font is damaged
     */
    String ERROR_FONTS_TYPE3_DAMAGED = "3.2.4";
    /**
     * The embedded CID Map is damaged
     */
    String ERROR_FONTS_CID_CMAP_DAMAGED = "3.2.5";

    /**
     * Common error for a Glyph problem
     */
    String ERROR_FONTS_GLYPH = "3.3";
    /**
     * a glyph is missing
     */
    String ERROR_FONTS_GLYPH_MISSING = "3.3.1";
    /**
     * a glyph is missing
     */
    String ERROR_FONTS_UNKNOWN_FONT_REF = "3.3.2";

    // -----------------------------------------------------------
    // ---- TRANSPARENCY ERRORS 4.x...
    // -----------------------------------------------------------
    String ERROR_TRANSPARENCY_MAIN = "4";
    /**
     * Common transparency error
     */
    String ERROR_TRANSPARENCY_EXT_GRAPHICAL_STATE = "4.1";
    /**
     * Soft mask entry is present but is forbidden
     */
    String ERROR_TRANSPARENCY_EXT_GS_SOFT_MASK = "4.1.1";
    /**
     * Ca or/and ca entry are present but the value isn't 1.0
     */
    String ERROR_TRANSPARENCY_EXT_GS_CA = "4.1.2";
    /**
     * BlendMode value isn't valid (only Normal and Compatible are authorized)
     */
    String ERROR_TRANSPARENCY_EXT_GS_BLEND_MODE = "4.1.3";

    // -----------------------------------------------------------
    // ---- ANNOTATION ERRORS 5.x...
    // -----------------------------------------------------------
    String ERROR_ANNOT_MAIN = "5";
    /**
     * Common missing field error in annotation dictionary
     */
    String ERROR_ANNOT_MISSING_FIELDS = "5.1";
    /**
     * The subtype entry is missing from the annotation dictionary
     */
    String ERROR_ANNOT_MISSING_SUBTYPE = "5.1.1";
    /**
     * The AP dictionary of the annotation contains forbidden/invalid entries (only the N entry is authorized)
     */
    String ERROR_ANNOT_MISSING_AP_N_CONTENT = "5.1.2";
    /**
     * An annotation validation is required but there are no element to validate
     */
    String ERROR_ANNOT_MISSING_ANNOTATION_DICTIONARY = "5.1.3";
    /**
     * Common forbidden field error in annotation dictionary
     */
    String ERROR_ANNOT_FORBIDDEN_ELEMENT = "5.2";
    /**
     * This type of annotation is forbidden (ex : Movie)
     */
    String ERROR_ANNOT_FORBIDDEN_SUBTYPE = "5.2.1";
    /**
     * The annotation uses a flag which is forbidden.
     */
    String ERROR_ANNOT_FORBIDDEN_FLAG = "5.2.2";
    /**
     * Annotation uses a Color profile which isn't the same than the profile contained by the OutputIntent
     */
    String ERROR_ANNOT_FORBIDDEN_COLOR = "5.2.3";
    /**
     * Dest entry can't be used if the A element is used too
     */
    String ERROR_ANNOT_FORBIDDEN_DEST = "5.2.4";
    /**
     * The AA field is forbidden for the Widget annotation when the PDF is a PDF/A
     */
    String ERROR_ANNOT_FORBIDDEN_AA = "5.2.5";
    /**
     * The annotation uses a flag which is not recommended but not forbidden by the ISO 19005-1:2005.
     */
    String ERROR_ANNOT_NOT_RECOMMENDED_FLAG = "5.2.6";
    /**
     * The AA field is forbidden for the Catalog when the PDF is a PDF/A
     */
    String ERROR_ANNOT_CATALOG_FORBIDDEN_AA = "5.2.7";
    /**
     * Common Invalid field error in annotation dictionary
     */
    String ERROR_ANNOT_INVALID_ELEMENT = "5.3";
    /**
     * The AP dictionary of the annotation contains forbidden/invalid entries (only the N entry is authorized)
     */
    String ERROR_ANNOT_INVALID_AP_CONTENT = "5.3.1";
    /**
     * Ca or/and ca entry are present but the value isn't 1.0
     */
    String ERROR_ANNOT_INVALID_CA = "5.3.2";
    /**
     * Dest entry of an annotation can't be checked due to an IO Exception
     */
    String ERROR_ANNOT_INVALID_DEST = "5.3.3";

    // -----------------------------------------------------------
    // ---- ACTION ERRORS 6.x...
    // -----------------------------------------------------------
    String ERROR_ACTION_MAIN = "6";

    /**
     * Common invalid action error
     */
    String ERROR_ACTION_INVALID_ACTIONS = "6.1";
    /**
     * A mandatory entry in the action dictionary is missing
     */
    String ERROR_ACTION_MISING_KEY = "6.1.1";
    /**
     * Some elements of the annotation dictionary have an invalid type (ex : array instead of Dictionary)
     */
    String ERROR_ACTION_INVALID_TYPE = "6.1.3";
    /**
     * The H entry of a Hide action is set to true (so some annotation can be hide)
     */
    String ERROR_ACTION_HIDE_H_INVALID = "6.1.4";
    /**
     * An action validation is required but there are no element to validate
     */
    String ERROR_ACTION_MISSING_ACTION_DICTIONARY = "6.1.5";
    /**
     * Common forbidden action error
     */
    String ERROR_ACTION_FORBIDDEN_ACTIONS = "6.2";
    /**
     * Named action other than predefined not allowed
     */
    String ERROR_ACTION_FORBIDDEN_ACTIONS_NAMED = "6.2.1";
    /**
     * Additional action entry is forbidden
     */
    String ERROR_ACTION_FORBIDDEN_ADDITIONAL_ACTION = "6.2.2";
    /**
     * Additional action entry is forbidden in a form field object
     */
    String ERROR_ACTION_FORBIDDEN_ADDITIONAL_ACTIONS_FIELD = "6.2.3";
    /**
     * A widget annotation linked with a form field shall not have any action
     */
    String ERROR_ACTION_FORBIDDEN_WIDGET_ACTION_FIELD = "6.2.4";
    /**
     * An explicitly forbidden action is used in the PDF file.
     */
    String ERROR_ACTION_FORBIDDEN_ACTIONS_EXPLICITLY_FORBIDDEN = "6.2.5";
    /**
     * Actions is rejected if it isn't defined in the PDF Reference Third Edition This is to avoid not consistent file
     * due to new features of the PDF format.
     */
    String ERROR_ACTION_FORBIDDEN_ACTIONS_UNDEF = "6.2.6";
    // -----------------------------------------------------------
    // ---- METADATA ERRORS 7.x...
    // -----------------------------------------------------------
    /**
     * Main metadata error code
     */
    String ERROR_METADATA_MAIN = "7";

    /**
     * Invalid metadata found
     */
    String ERROR_METADATA_FORMAT = "7.1";

    /**
     * Unknown metadata
     */
    String ERROR_METADATA_PROPERTY_UNKNOWN = "7.1.1";

    /**
     * Invalid xmp metadata format
     */
    String ERROR_METADATA_PROPERTY_FORMAT = "7.1.2";
    /**
     * Unexpected type of a Metadata entry
     */
    String ERROR_METADATA_FORMAT_UNKOWN = "7.1.3";
    /**
     * Invalid metadata, unable to process the font due to IOException
     */
    String ERROR_METADATA_FORMAT_STREAM = "7.1.4";
    /**
     * Invalid metadata, unable to process the font due to Invalid XPacket exception
     */
    String ERROR_METADATA_FORMAT_XPACKET = "7.1.5";
    /**
     * Metadata mismatch between PDF Dictionnary and xmp
     */
    String ERROR_METADATA_MISMATCH = "7.2";

    /**
     * Invalid information in xpacket processing instruction
     */
    String ERROR_METADATA_XPACKET_DEPRECATED = "7.0.0";

    /**
     * Description schema required not embedded
     */
    String ERROR_METADATA_ABSENT_DESCRIPTION_SCHEMA = "7.3";

    /**
     * A required namespace URI missing
     */
    String ERROR_METADATA_NS_URI_MISSING = "7.4";

    /**
     * A namespace URI has an unexpected value
     */
    String ERROR_METADATA_WRONG_NS_URI = "7.4.1";

    /**
     * A namespace prefix has an unexpected value
     */
    String ERROR_METADATA_WRONG_NS_PREFIX = "7.4.2";

    /**
     * Required property is missing
     */
    String ERROR_METADATA_PROPERTY_MISSING = "7.5";

    /**
     * A valueType is used but is not declared
     */
    String ERROR_METADATA_UNKNOWN_VALUETYPE = "7.6";

    /**
     * PDF/A Identification Schema not found
     */
    String ERROR_METADATA_PDFA_ID_MISSING = "7.11";

    /**
     * PDF/A Identification Conformance Invalid
     */
    String ERROR_METADATA_INVALID_PDFA_CONFORMANCE = "7.11.1";

    /**
     * PDF/A Identification Version Identifier Invalid (pdfaid:part)
     */
    String ERROR_METADATA_INVALID_PDFA_VERSION_ID = "7.11.2";

    /**
     * rdf:about is missing
     */
    String ERROR_METADATA_RDF_ABOUT_ATTRIBUTE_MISSING = "7.0";

    /**
     * One of rdf:about attribute embedded in RDF:rdf have a different value than the others
     */
    String ERROR_METADATA_RDF_ABOUT_ATTRIBUTE_INEQUAL_VALUE = "7.0.1";

    /**
     * a category has an invalid value in one property description (must be internal or external)
     */
    String ERROR_METADATA_CATEGORY_PROPERTY_INVALID = "7.5.1";

    /**
     * the infor dictionary is corrupt or value can't be read
     */
    String ERROR_METADATA_DICT_INFO_CORRUPT = "7.12";
    /**
     * Error about PDF processing : that is not necessary a specific PDF/A validation error
     * but a PDF specification requirement that isn't respected.
     */
    String ERROR_PDF_PROCESSING = "8";
    /**
     * A Mandatory element is missing
     */
    String ERROR_PDF_PROCESSING_MISSING = "8.1";
}

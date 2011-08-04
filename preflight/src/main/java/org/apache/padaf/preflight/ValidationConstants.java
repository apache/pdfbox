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

package org.apache.padaf.preflight;

/**
 * This interface provide a set of constants which identify validation error.
 */
public interface ValidationConstants {

  // -----------------------------------------------------------
  // ---- CONSTANTS
  // -----------------------------------------------------------
  static int EDOC_TOKEN_MGR_ERROR = 255;
  static String EDOC_TOKEN_MGR_ERROR_TAG = " ERROR_CODE: ";
  static int MAX_DICT_ENTRIES = 4095;
  static int MAX_ARRAY_ELEMENTS = 8191;
  static int MAX_NAME_SIZE = 127;
  static int MAX_STRING_LENGTH = 65535;
  static int MAX_INDIRECT_OBJ = 8388607;
  static int MAX_CID = 65535;
  static int MAX_GRAPHIC_STATES = 28;
  static int MAX_DEVICE_N_LIMIT = 8;
  static float MAX_POSITIVE_FLOAT = 32767f;
  static float MAX_NEGATIVE_FLOAT = -32767f;

  static String TRAILER_DICTIONARY_KEY_ID = "ID";
  static String TRAILER_DICTIONARY_KEY_SIZE = "Size";
  static String TRAILER_DICTIONARY_KEY_PREV = "Prev";
  static String TRAILER_DICTIONARY_KEY_ROOT = "Root";
  static String TRAILER_DICTIONARY_KEY_INFO = "Info";
  static String TRAILER_DICTIONARY_KEY_ENCRYPT = "Encrypt";

  static String DICTIONARY_KEY_ACTION = "A";
  static String DICTIONARY_KEY_DESTINATION = "Dest";
  static String DICTIONARY_KEY_ADDITIONAL_ACTION = "AA";
  static String DICTIONARY_KEY_OPEN_ACTION = "OpenAction";

  static String DOCUMENT_DICTIONARY_KEY_OUTPUT_INTENTS = "OutputIntents";
  static String DOCUMENT_DICTIONARY_KEY_OPTIONAL_CONTENTS = "OCProperties";

  static String OUTPUT_INTENT_DICTIONARY_KEY_S = "S";
  static String OUTPUT_INTENT_DICTIONARY_KEY_INFO = "Info";
  static String OUTPUT_INTENT_DICTIONARY_VALUE_GTS_PDFA1 = "GTS_PDFA1";
  static String OUTPUT_INTENT_DICTIONARY_KEY_DEST_OUTPUT_PROFILE = "DestOutputProfile";
  static String OUTPUT_INTENT_DICTIONARY_KEY_OUTPUT_CONDITION_IDENTIFIER = "OutputConditionIdentifier";
  static String OUTPUT_INTENT_DICTIONARY_VALUE_OUTPUT_CONDITION_IDENTIFIER_CUSTOM = "Custom";

  static String TRANPARENCY_DICTIONARY_KEY_EXTGSTATE = "ExtGState";
  static String TRANPARENCY_DICTIONARY_KEY_EXTGSTATE_ENTRY_REGEX = "(GS|gs)([0-9])+";

  static String TRANSPARENCY_DICTIONARY_KEY_BLEND_MODE = "BM";
  static String TRANSPARENCY_DICTIONARY_KEY_UPPER_CA = "CA";
  static String TRANSPARENCY_DICTIONARY_KEY_LOWER_CA = "ca";
  static String TRANSPARENCY_DICTIONARY_KEY_SOFT_MASK = "SMask";
  static String TRANSPARENCY_DICTIONARY_VALUE_SOFT_MASK_NONE = "None";
  static String TRANSPARENCY_DICTIONARY_VALUE_BM_NORMAL = "Normal";
  static String TRANSPARENCY_DICTIONARY_VALUE_BM_COMPATIBLE = "Compatible";

  static String DICTIONARY_KEY_LINEARIZED = "Linearized";
  static String DICTIONARY_KEY_LINEARIZED_L = "L";
  static String DICTIONARY_KEY_LINEARIZED_H = "H";
  static String DICTIONARY_KEY_LINEARIZED_O = "O";
  static String DICTIONARY_KEY_LINEARIZED_E = "E";
  static String DICTIONARY_KEY_LINEARIZED_N = "N";
  static String DICTIONARY_KEY_LINEARIZED_T = "T";
  static String DICTIONARY_KEY_TYPE = "Type";
  static String DICTIONARY_KEY_SUBTYPE = "Subtype";

  static String DICTIONARY_KEY_XOBJECT = "XObject";
  static String DICTIONARY_KEY_PATTERN = "Pattern";
  static String DICTIONARY_KEY_FONT = "Font";

  static String DICTIONARY_KEY_PATTERN_TYPE = "PatternType";
  static int DICTIONARY_PATTERN_TILING = 1;
  static int DICTIONARY_PATTERN_SHADING = 2;

  static String PATTERN_KEY_PAINT_TYPE = "PaintType";
  static String PATTERN_KEY_TILING_TYPE = "TilingType";
  static String PATTERN_KEY_BBOX = "BBox";
  static String PATTERN_KEY_XSTEP = "XStep";
  static String PATTERN_KEY_YSTEP = "YStep";
  static String PATTERN_KEY_SHADING = "Shading";
  static String PATTERN_KEY_SHADING_TYPE = "ShadingType";

  static String XOBJECT_DICTIONARY_VALUE_SUBTYPE_IMG = "Image";
  static String XOBJECT_DICTIONARY_VALUE_SUBTYPE_FORM = "Form";
  static String XOBJECT_DICTIONARY_KEY_COLOR_SPACE = "ColorSpace";
  static final String XOBJECT_DICTIONARY_VALUE_SUBTYPE_POSTSCRIPT = "PS";
  static String XOBJECT_DICTIONARY_KEY_BBOX = "BBox";
  static String XOBJECT_DICTIONARY_KEY_GROUP = "Group";
  static String XOBJECT_DICTIONARY_KEY_GROUP_S = "S";
  static String XOBJECT_DICTIONARY_VALUE_S_TRANSPARENCY = "Transparency";

  static String FONT_DICTIONARY_VALUE_FONT = "Font";
  static String FONT_DICTIONARY_VALUE_COMPOSITE = "Type0";
  static String FONT_DICTIONARY_VALUE_TRUETYPE = "TrueType";
  static String FONT_DICTIONARY_VALUE_TYPE1 = "Type1";
  static String FONT_DICTIONARY_VALUE_TYPE1C = "Type1C";
  static String FONT_DICTIONARY_VALUE_MMTYPE = "MMType1";
  static String FONT_DICTIONARY_VALUE_TYPE3 = "Type3";
  static String FONT_DICTIONARY_VALUE_TYPE0 = "CIDFontType0";
  static String FONT_DICTIONARY_VALUE_TYPE0C = "CIDFontType0C";
  static String FONT_DICTIONARY_VALUE_TYPE2 = "CIDFontType2";
  static String FONT_DICTIONARY_VALUE_ENCODING_MAC = "MacRomanEncoding";
  static String FONT_DICTIONARY_VALUE_ENCODING_MAC_EXP = "MacExpertEncoding";
  static String FONT_DICTIONARY_VALUE_ENCODING_WIN = "WinAnsiEncoding";
  static String FONT_DICTIONARY_VALUE_ENCODING_STD = "StandardEncoding";
  static String FONT_DICTIONARY_VALUE_ENCODING_PDFDOC = "PDFDocEncoding";

  static String FONT_DICTIONARY_VALUE_ENCODING = "Encoding";
  static String FONT_DICTIONARY_VALUE_CMAP_IDENTITY_H = "Identity-H";
  static String FONT_DICTIONARY_VALUE_CMAP_IDENTITY_V = "Identity-V";
  static String FONT_DICTIONARY_VALUE_CMAP_IDENTITY = "Identity";
  static String FONT_DICTIONARY_VALUE_TYPE_CMAP = "CMap";

  static String FONT_DICTIONARY_KEY_NAME = "Name";
  static String FONT_DICTIONARY_KEY_BASEFONT = "BaseFont";
  static String FONT_DICTIONARY_KEY_FIRSTCHAR = "FirstChar";
  static String FONT_DICTIONARY_KEY_LASTCHAR = "LastChar";
  static String FONT_DICTIONARY_KEY_WIDTHS = "Widths";
  static String FONT_DICTIONARY_KEY_FONT_DESC = "FontDescriptor";
  static String FONT_DICTIONARY_KEY_ENCODING = "Encoding";
  static String FONT_DICTIONARY_KEY_TOUNICODE = "ToUnicode";
  static String FONT_DICTIONARY_KEY_FONTNAME = "FontName";
  static String FONT_DICTIONARY_KEY_FLAGS = "Flags";
  static String FONT_DICTIONARY_KEY_ITALICANGLE = "ItalicAngle";
  static String FONT_DICTIONARY_KEY_FONTBBOX = "FontBBox";
  static String FONT_DICTIONARY_KEY_FONTMATRIX = "FontMatrix";
  static String FONT_DICTIONARY_KEY_CHARPROCS = "CharProcs";
  static String DICTIONARY_KEY_RESOURCES = "Resources";
  static String FONT_DICTIONARY_KEY_ASCENT = "Ascent";
  static String FONT_DICTIONARY_KEY_DESCENT = "Descent";
  static String FONT_DICTIONARY_KEY_CAPHEIGHT = "CapHeight";
  static String FONT_DICTIONARY_KEY_STEMV = "StemV";
  static String FONT_DICTIONARY_KEY_LENGTH1 = "Length1";
  static String FONT_DICTIONARY_KEY_LENGTH2 = "Length2";
  static String FONT_DICTIONARY_KEY_LENGTH3 = "Length3";
  static String FONT_DICTIONARY_KEY_METADATA = "Metadata";
  static String FONT_DICTIONARY_KEY_BASEENCODING = "BaseEncoding";
  static String FONT_DICTIONARY_KEY_DIFFERENCES = "Differences";
  static String FONT_DICTIONARY_KEY_DESCENDANT_FONTS = "DescendantFonts";
  static String FONT_DICTIONARY_KEY_CID_SYSINFO = "CIDSystemInfo";
  static String FONT_DICTIONARY_KEY_CID_GIDMAP = "CIDToGIDMap";
  static String FONT_DICTIONARY_KEY_SYSINFO_REGISTRY = "Registry";
  static String FONT_DICTIONARY_KEY_SYSINFO_ORDERING = "Ordering";
  static String FONT_DICTIONARY_KEY_SYSINFO_SUPPLEMENT = "Supplement";
  static String FONT_DICTIONARY_KEY_CMAP_NAME = "CMapName";
  static String FONT_DICTIONARY_KEY_CMAP_WMODE = "WMode";
  static String FONT_DICTIONARY_KEY_CMAP_USECMAP = "UseCMap";
  static String FONT_DICTIONARY_KEY_CIDSET = "CIDSet";

  static String STREAM_DICTIONARY_KEY_LENGHT = "Length";
  static String STREAM_DICTIONARY_KEY_FILTER = "Filter";
  static String STREAM_DICTIONARY_KEY_DECODEPARAMS = "DecodeParms";
  static String STREAM_DICTIONARY_KEY_F = "F";
  static String STREAM_DICTIONARY_KEY_FFILTER = "FFilter";
  static String STREAM_DICTIONARY_KEY_FDECODEPARAMS = "FDecodeParms";
  static String STREAM_DICTIONARY_KEY_COLOR_SPACE = "CS";

  static String STREAM_DICTIONARY_VALUE_FILTER_LZW = "LZWDecode";

  static String STREAM_DICTIONARY_VALUE_FILTER_ASCII_HEX = "ASCIIHexDecode";
  static String STREAM_DICTIONARY_VALUE_FILTER_ASCII_85 = "ASCII85Decode";
  static String STREAM_DICTIONARY_VALUE_FILTER_RUN = "RunLengthDecode";
  static String STREAM_DICTIONARY_VALUE_FILTER_CCITTFF = "CCITTFaxDecode";
  static String STREAM_DICTIONARY_VALUE_FILTER_JBIG = "JBIG2Decode";
  static String STREAM_DICTIONARY_VALUE_FILTER_DCT = "DCTDecode";
  static String STREAM_DICTIONARY_VALUE_FILTER_FLATE_DECODE = "FlateDecode";

  static String FILE_SPECIFICATION_VALUE_TYPE = "Filespec";
  static String FILE_SPECIFICATION_KEY_EMBEDDED_FILE = "EF";

  static String INLINE_DICTIONARY_VALUE_FILTER_LZW = "LZW";

  static String INLINE_DICTIONARY_VALUE_FILTER_ASCII_HEX = "AHx";
  static String INLINE_DICTIONARY_VALUE_FILTER_ASCII_85 = "A85";
  static String INLINE_DICTIONARY_VALUE_FILTER_RUN = "RL";
  static String INLINE_DICTIONARY_VALUE_FILTER_CCITTFF = "CCF";
  static String INLINE_DICTIONARY_VALUE_FILTER_DCT = "DCT";
  static String INLINE_DICTIONARY_VALUE_FILTER_FLATE_DECODE = "Fl";
  
  static String ANNOT_DICTIONARY_KEY_CONTENTS = "Contents";
  static String ANNOT_DICTIONARY_KEY_RECT = "Rect";
  static String ANNOT_DICTIONARY_KEY_CA = "CA";
  static String ANNOT_DICTIONARY_KEY_DA = "DA";
  static String ANNOT_DICTIONARY_KEY_QUADPOINTS = "QuadPoints";
  static String ANNOT_DICTIONARY_KEY_L = "L";
  static String ANNOT_DICTIONARY_KEY_F = "F";
  static String ANNOT_DICTIONARY_KEY_C = "C";
  static String ANNOT_DICTIONARY_KEY_N = "N";
  static String ANNOT_DICTIONARY_KEY_D = "D";
  static String ANNOT_DICTIONARY_KEY_R = "R";
  static String ANNOT_DICTIONARY_KEY_INKLIST = "InkList";

  static String ANNOT_DICTIONARY_VALUE_TYPE = "Annot";
  static String ANNOT_DICTIONARY_VALUE_SUBTYPE_TEXT = "Text";
  static String ANNOT_DICTIONARY_VALUE_SUBTYPE_LINK = "Link";
  static String ANNOT_DICTIONARY_VALUE_SUBTYPE_FREETEXT = "FreeText";
  static String ANNOT_DICTIONARY_VALUE_SUBTYPE_LINE = "Line";
  static String ANNOT_DICTIONARY_VALUE_SUBTYPE_SQUARE = "Square";
  static String ANNOT_DICTIONARY_VALUE_SUBTYPE_CIRCLE = "Circle";
  static String ANNOT_DICTIONARY_VALUE_SUBTYPE_HIGHLIGHT = "Highlight";
  static String ANNOT_DICTIONARY_VALUE_SUBTYPE_UNDERLINE = "Underline";
  static String ANNOT_DICTIONARY_VALUE_SUBTYPE_SQUILGGLY = "Squiggly";
  static String ANNOT_DICTIONARY_VALUE_SUBTYPE_STRIKEOUT = "StrikeOut";
  static String ANNOT_DICTIONARY_VALUE_SUBTYPE_STAMP = "Stamp";
  static String ANNOT_DICTIONARY_VALUE_SUBTYPE_INK = "Ink";
  static String ANNOT_DICTIONARY_VALUE_SUBTYPE_POPUP = "Popup";
  static String ANNOT_DICTIONARY_VALUE_SUBTYPE_WIDGET = "Widget";
  static String ANNOT_DICTIONARY_VALUE_SUBTYPE_PRINTERMARK = "PrinterMark";
  static String ANNOT_DICTIONARY_VALUE_SUBTYPE_TRAPNET = "TrapNet";

  static String ACTION_DICTIONARY_VALUE_TYPE = "Action";
  static String ACTION_DICTIONARY_KEY_S = "S";
  static String ACTION_DICTIONARY_KEY_N = "N";
  static String ACTION_DICTIONARY_KEY_T = "T";
  static String ACTION_DICTIONARY_KEY_H = "H";
  static String ACTION_DICTIONARY_KEY_D = "D";
  static String ACTION_DICTIONARY_KEY_F = "F";
  static String ACTION_DICTIONARY_KEY_URI = "URI";
  static String ACTION_DICTIONARY_KEY_NEXT = "Next";
  static String ACTION_DICTIONARY_VALUE_ATYPE_GOTO = "GoTo";
  static String ACTION_DICTIONARY_VALUE_ATYPE_GOTOR = "GoToR";
  static String ACTION_DICTIONARY_VALUE_ATYPE_THREAD = "Thread";
  static String ACTION_DICTIONARY_VALUE_ATYPE_URI = "URI";
  static String ACTION_DICTIONARY_VALUE_ATYPE_HIDE = "Hide";
  static String ACTION_DICTIONARY_VALUE_ATYPE_NAMED = "Named";
  static String ACTION_DICTIONARY_VALUE_ATYPE_SUBMIT = "SubmitForm";
  static String ACTION_DICTIONARY_VALUE_ATYPE_LAUNCH = "Launch";
  static String ACTION_DICTIONARY_VALUE_ATYPE_SOUND = "Sound";
  static String ACTION_DICTIONARY_VALUE_ATYPE_MOVIE = "Movie";
  static String ACTION_DICTIONARY_VALUE_ATYPE_RESET = "ResetForm";
  static String ACTION_DICTIONARY_VALUE_ATYPE_IMPORT = "ImportData";
  static String ACTION_DICTIONARY_VALUE_ATYPE_JAVASCRIPT = "JavaScript";
  static String ACTION_DICTIONARY_VALUE_ATYPE_SETSTATE = "SetState";
  static String ACTION_DICTIONARY_VALUE_ATYPE_NOOP = "NOP";
  static String ACTION_DICTIONARY_VALUE_ATYPE_NAMED_NEXT = "NextPage";
  static String ACTION_DICTIONARY_VALUE_ATYPE_NAMED_PREV = "PrevPage";
  static String ACTION_DICTIONARY_VALUE_ATYPE_NAMED_FIRST = "FirstPage";
  static String ACTION_DICTIONARY_VALUE_ATYPE_NAMED_LAST = "LastPage";

  static String ACROFORM_DICTIONARY_KEY_NEED_APPEARANCES = "NeedAppearances";

  static final String RENDERING_INTENT_REL_COLOR = "RelativeColorimetric";
  static final String RENDERING_INTENT_ABS_COLOR = "AbsoluteColorimetric";
  static final String RENDERING_INTENT_PERCEPTUAL = "Perceptual";
  static final String RENDERING_INTENT_SATURATION = "Saturation";

  static final String ICC_Characterization_Data_Registry_FOGRA43 = "FOGRA43";
  static final String ICC_Characterization_Data_Registry_CGATS_TR_006 = "CGATS TR 006";
  static final String ICC_Characterization_Data_Registry_FOGRA39 = "FOGRA39";
  static final String ICC_Characterization_Data_Registry_JC200103 = "JC200103";
  static final String ICC_Characterization_Data_Registry_FOGRA27 = "FOGRA27";
  static final String ICC_Characterization_Data_Registry_EUROSB104 = "EUROSB104";
  static final String ICC_Characterization_Data_Registry_FOGRA45 = "FOGRA45";
  static final String ICC_Characterization_Data_Registry_FOGRA46 = "FOGRA46";
  static final String ICC_Characterization_Data_Registry_FOGRA41 = "FOGRA41";
  static final String ICC_Characterization_Data_Registry_CGATS_TR_001 = "CGATS TR 001";
  static final String ICC_Characterization_Data_Registry_CGATS_TR_003 = "CGATS TR 003";
  static final String ICC_Characterization_Data_Registry_CGATS_TR_005 = "CGATS TR 005";
  static final String ICC_Characterization_Data_Registry_FOGRA28 = "FOGRA28";
  static final String ICC_Characterization_Data_Registry_JCW2003 = "JCW2003";
  static final String ICC_Characterization_Data_Registry_EUROSB204 = "EUROSB204";
  static final String ICC_Characterization_Data_Registry_FOGRA47 = "FOGRA47";
  static final String ICC_Characterization_Data_Registry_FOGRA44 = "FOGRA44";
  static final String ICC_Characterization_Data_Registry_FOGRA29 = "FOGRA29";
  static final String ICC_Characterization_Data_Registry_JC200104 = "JC200104";
  static final String ICC_Characterization_Data_Registry_FOGRA40 = "FOGRA40";
  static final String ICC_Characterization_Data_Registry_FOGRA30 = "FOGRA30";
  static final String ICC_Characterization_Data_Registry_FOGRA42 = "FOGRA42";
  static final String ICC_Characterization_Data_Registry_IFRA26 = "IFRA26";
  static final String ICC_Characterization_Data_Registry_JCN2002 = "JCN2002";
  static final String ICC_Characterization_Data_Registry_CGATS_TR_002 = "CGATS TR 002";
  static final String ICC_Characterization_Data_Registry_FOGRA33 = "FOGRA33";
  static final String ICC_Characterization_Data_Registry_FOGRA37 = "FOGRA37";
  static final String ICC_Characterization_Data_Registry_FOGRA31 = "FOGRA31";
  static final String ICC_Characterization_Data_Registry_FOGRA35 = "FOGRA35";
  static final String ICC_Characterization_Data_Registry_FOGRA32 = "FOGRA32";
  static final String ICC_Characterization_Data_Registry_FOGRA34 = "FOGRA34";
  static final String ICC_Characterization_Data_Registry_FOGRA36 = "FOGRA36";
  static final String ICC_Characterization_Data_Registry_FOGRA38 = "FOGRA38";
  static final String ICC_Characterization_Data_Registry_sRGB = "sRGB";
  static final String ICC_Characterization_Data_Registry_sRGB_IEC = "sRGB IEC61966-2.1";
  static final String ICC_Characterization_Data_Registry_Adobe = "Adobe RGB (1998)";
  static final String ICC_Characterization_Data_Registry_bg_sRGB = "bg-sRGB";
  static final String ICC_Characterization_Data_Registry_sYCC = "sYCC";
  static final String ICC_Characterization_Data_Registry_scRGB = "scRGB";
  static final String ICC_Characterization_Data_Registry_scRGB_nl = "scRGB-nl";
  static final String ICC_Characterization_Data_Registry_scYCC_nl = "scYCC-nl";
  static final String ICC_Characterization_Data_Registry_ROMM = "ROMM RGB";
  static final String ICC_Characterization_Data_Registry_RIMM = "RIMM RGB";
  static final String ICC_Characterization_Data_Registry_ERIMM = "ERIMM RGB";
  static final String ICC_Characterization_Data_Registry_eciRGB = "eciRGB";
  static final String ICC_Characterization_Data_Registry_opRGB = "opRGB";
  /**
   * Error code uses by the Valdiator when there are an error without error
   * code.
   */
  static final String ERROR_UNKOWN_ERROR = "-1";

  // -----------------------------------------------------------
  // ---- FILE STRUCTURE ERRORS 1.x...
  // -----------------------------------------------------------

  /**
   * Error code for syntax error
   */
  static final String ERROR_SYNTAX_MAIN = "1";
  // error code category which can occur in each pdf part
  static final String ERROR_SYNTAX_COMMON = "1.0";
  /**
   * Too many entries in a dictionary object
   */
  static final String ERROR_SYNTAX_TOO_MANY_ENTRIES = "1.0.1";
  /**
   * Too many element in an array object
   */
  static final String ERROR_SYNTAX_ARRAY_TOO_LONG = "1.0.2";
  /**
   * The name length is too long
   */
  static final String ERROR_SYNTAX_NAME_TOO_LONG = "1.0.3";
  /**
   * The literal string is too long
   */
  static final String ERROR_SYNTAX_LITERAL_TOO_LONG = "1.0.4";
  /**
   * The hexa string is too long
   */
  static final String ERROR_SYNTAX_HEXA_STRING_TOO_LONG = "1.0.5";
  /**
   * The number is out of Range ( ex : greatter than 2^31-1)
   */
  static final String ERROR_SYNTAX_NUMERIC_RANGE = "1.0.6";
  /**
   * A dictionary key isn't a name
   */
  static final String ERROR_SYNTAX_DICTIONARY_KEY_INVALID = "1.0.7";
  /**
   * The language declared doesn't match with the RFC1766
   */
  static final String ERROR_SYNTAX_LANG_NOT_RFC1766 = "1.0.8";
  /**
   * There are too many objects
   */
  static final String ERROR_SYNTAX_INDIRECT_OBJ_RANGE = "1.0.9";
  /**
   * CID too long
   */
  static final String ERROR_SYNTAX_CID_RANGE = "1.0.10";
  
  static final String ERROR_SYNTAX_HEADER = "1.1";
  static final String ERROR_SYNTAX_HEADER_FIRST_CHAR = "1.1.1";
  static final String ERROR_SYNTAX_HEADER_FILE_TYPE = "1.1.2";

  /**
   * Common error about body syntax
   */
  static final String ERROR_SYNTAX_BODY = "1.2";
  /**
   * Error on the object delimiters (obj / endobj)
   */
  static final String ERROR_SYNTAX_OBJ_DELIMITER = "1.2.1";
  /**
   * Error on the stream delimiters (stream / endstream)
   */
  static final String ERROR_SYNTAX_STREAM_DELIMITER = "1.2.2";
  /**
   * Required fields are missing from the dictionary
   */
  static final String ERROR_SYNTAX_DICT_INVALID = "1.2.3";
  /**
   * The length entry is missing from the stream dictionary
   */
  static final String ERROR_SYNTAX_STREAM_LENGTH_MISSING = "1.2.4";
  /**
   * The length of the stream dictionary and the stream length is inconsistent
   */
  static final String ERROR_SYNTAX_STREAM_LENGTH_INVALID = "1.2.5";
  /**
   * F or/and FFilter or/and FDecodeParams are present in a stream dictionary
   */
  static final String ERROR_SYNTAX_STREAM_FX_KEYS = "1.2.6";
  /**
   * The stream uses an invalid filter (The LZW)
   */
  static final String ERROR_SYNTAX_STREAM_INVALID_FILTER = "1.2.7";
  /**
   * The stream uses a filter which isn't defined in the PDF Reference document. 
   */
  static final String ERROR_SYNTAX_STREAM_UNDEFINED_FILTER = "1.2.12";
  /**
   * The content stream has some syntax errors
   */
  static final String ERROR_SYNTAX_CONTENT_STREAM_INVALID = "1.2.8";
  /**
   * EmbeddedFile entry is present in a FileSpecification dictionary
   */
  static final String ERROR_SYNTAX_EMBEDDED_FILES = "1.2.9";
  /**
   * The content stream uses an unsupported operator
   */
  static final String ERROR_SYNTAX_CONTENT_STREAM_UNSUPPORTED_OP = "1.2.10";
  /**
   * The content stream contains an invalid argument for the operator
   */
  static final String ERROR_SYNTAX_CONTENT_STREAM_INVALID_ARGUMENT = "1.2.11";
  /**
   * Common error about the cross ref table
   */
  static final String ERROR_SYNTAX_CROSS_REF = "1.3";
  /**
   * Common error about the trailer
   */
  static final String ERROR_SYNTAX_TRAILER = "1.4";
  /**
   * ID is missing from the trailer
   */
  static final String ERROR_SYNTAX_TRAILER_MISSING_ID = "1.4.1";
  /**
   * Encrypt is forbidden
   */
  static final String ERROR_SYNTAX_TRAILER_ENCRYPT = "1.4.2";
  /**
   * An trailer entry has an invalid type
   */
  static final String ERROR_SYNTAX_TRAILER_TYPE_INVALID = "1.4.3";
  /**
   * Size is missing from the trailer
   */
  static final String ERROR_SYNTAX_TRAILER_MISSING_SIZE = "1.4.4";
  /**
   * Root is missing from the trailer
   */
  static final String ERROR_SYNTAX_TRAILER_MISSING_ROOT = "1.4.5";
  /**
   * ID in 1st trailer and the last is different
   */
  static final String ERROR_SYNTAX_TRAILER_ID_CONSISTENCY = "1.4.6";
  /**
   * EmbeddedFile entry is present in the Names dictionary
   */
  static final String ERROR_SYNTAX_TRAILER_CATALOG_EMBEDDEDFILES = "1.4.7";
  /**
   * Optional content is forbidden
   */
  static final String ERROR_SYNTAX_TRAILER_CATALOG_OCPROPERTIES = "1.4.8";
  /**
   * Errors in the Outlines dictionary
   */
  static final String ERROR_SYNTAX_TRAILER_OUTLINES_INVALID = "1.4.9";

  // -----------------------------------------------------------
  // ---- GRAPHIC ERRORS 2.x...
  // -----------------------------------------------------------

  /**
   * Main error code for graphical problems
   */
  static final String ERROR_GRAPHIC_MAIN = "2";
  static final String ERROR_GRAPHIC_INVALID = "2.1";
  /**
   * BBox Entry of a Form XObject is missing or isn't an Array
   */
  static final String ERROR_GRAPHIC_INVALID_BBOX = "2.1.1";
  /**
   * The OutputIntent dictionary is invalid
   */
  static final String ERROR_GRAPHIC_OUTPUT_INTENT_INVALID_ENTRY = "2.1.2";
  /**
   * The S entry of the OutputIntent isn't GTS_PDFA1
   */
  static final String ERROR_GRAPHIC_OUTPUT_INTENT_S_VALUE_INVALID = "2.1.3";
  /**
   * The ICC Profile is invalid
   */
  static final String ERROR_GRAPHIC_OUTPUT_INTENT_ICC_PROFILE_INVALID = "2.1.4";
  /**
   * There are more than one ICC Profile
   */
  static final String ERROR_GRAPHIC_OUTPUT_INTENT_ICC_PROFILE_MULTIPLE = "2.1.5";

  /**
   * Profile version is too recent for PDF 1.4 document
   */
  static final String ERROR_GRAPHIC_OUTPUT_INTENT_ICC_PROFILE_TOO_RECENT = "2.1.6";

  static final String ERROR_GRAPHIC_MISSING_FIELD = "2.1.7";

  static final String ERROR_GRAPHIC_TOO_MANY_GRAPHIC_STATES = "2.1.8";
  
  /**
   * Main error code for graphical transparency problems
   */
  static final String ERROR_GRAPHIC_TRANSPARENCY = "2.2";
  /**
   * A Group entry with S = Transparency is used or the S = Null
   */
  static final String ERROR_GRAPHIC_TRANSPARENCY_GROUP = "2.2.1";
  /**
   * A XObject SMask value isn't None
   */
  static final String ERROR_GRAPHIC_TRANSPARENCY_SMASK = "2.2.2";

  /**
   * A XObject has an unexpected key defined
   */
  static final String ERROR_GRAPHIC_UNEXPECTED_KEY = "2.3";

  /**
   * A XObject has an unexpected value for a defined key
   */
  static final String ERROR_GRAPHIC_UNEXPECTED_VALUE_FOR_KEY = "2.3.2";

  /**
   * An invalid color space is used
   */
  static final String ERROR_GRAPHIC_INVALID_COLOR_SPACE = "2.4";
  /**
   * RGB color space used in the PDF file but the DestOutputProfile isn't RGB
   */
  static final String ERROR_GRAPHIC_INVALID_COLOR_SPACE_RGB = "2.4.1";
  /**
   * CMYK color space used in the PDF file but the DestOutputProfile isn't CMYK
   */
  static final String ERROR_GRAPHIC_INVALID_COLOR_SPACE_CMYK = "2.4.2";
  /**
   * color space used in the PDF file but the DestOutputProfile is missing
   */
  static final String ERROR_GRAPHIC_INVALID_COLOR_SPACE_MISSING = "2.4.3";
  /**
   * Unknown ColorSpace
   */
  static final String ERROR_GRAPHIC_INVALID_UNKNOWN_COLOR_SPACE = "2.4.4";
  /**
   * The pattern color space can't be used
   */
  static final String ERROR_GRAPHIC_INVALID_PATTERN_COLOR_SPACE_FORBIDDEN = "2.4.5";
  /**
   * The pattern is invalid due to missing key or invalid value
   */
  static final String ERROR_GRAPHIC_INVALID_PATTERN_DEFINITION = "2.4.6";
  /**
   * alternate color space used in the PDF file but the DestOutputProfile isn't
   * consistent
   */
  static final String ERROR_GRAPHIC_INVALID_COLOR_SPACE_ALTERNATE = "2.4.7";
  /**
   * Base ColorSpace in the Indexed color space is invalid
   */
  static final String ERROR_GRAPHIC_INVALID_COLOR_SPACE_INDEXED = "2.4.8";
  /**
   * ColorSpace is forbidden due to some restriction (ex : Only DeviceXXX are
   * auth in inlined image)
   */
  static final String ERROR_GRAPHIC_INVALID_COLOR_SPACE_FORBIDDEN = "2.4.9";
  
  static final String ERROR_GRAPHIC_INVALID_COLOR_SPACE_TOO_MANY_COMPONENTS_DEVICEN = "2.4.10";

  // -----------------------------------------------------------
  // ---- FONT ERRORS 3.x...
  // -----------------------------------------------------------

  /**
   * Main error code for font problems
   */
  static final String ERROR_FONTS_MAIN = "3";

  static final String ERROR_FONTS_INVALID_DATA = "3.1";
  /**
   * Some mandatory fields are missing from the FONT Dictionary
   */
  static final String ERROR_FONTS_DICTIONARY_INVALID = "3.1.1";
  /**
   * Some mandatory fields are missing from the FONT Descriptor Dictionary
   */
  static final String ERROR_FONTS_DESCRIPTOR_INVALID = "3.1.2";
  /**
   * Error on the "Font File x" in the Font Descriptor (ex : FontFile and
   * FontFile2 are present in the same dictionary)
   */
  static final String ERROR_FONTS_FONT_FILEX_INVALID = "3.1.3";
  /**
   * Charset declaration is missing in a Type 1 Subset
   */
  static final String ERROR_FONTS_CHARSET_MISSING_FOR_SUBSET = "3.1.4";
  /**
   * Encoding is inconsistent with the Font (ex : Symbolic TrueType mustn't
   * declare encoding)
   */
  static final String ERROR_FONTS_ENCODING = "3.1.5";
  /**
   * Width array and Font program Width are inconsistent
   */
  static final String ERROR_FONTS_METRICS = "3.1.6";
  /**
   * Required entry in a Composite Font dictionary is missing
   */
  static final String ERROR_FONTS_CIDKEYED_INVALID = "3.1.7";
  /**
   * The CIDSystemInfo dictionary is invalid
   */
  static final String ERROR_FONTS_CIDKEYED_SYSINFO = "3.1.8";
  /**
   * The CIDToGID is invalid
   */
  static final String ERROR_FONTS_CIDKEYED_CIDTOGID = "3.1.9";
  /**
   * The CMap of the Composite Font is missing or invalid
   */
  static final String ERROR_FONTS_CIDKEYED_CMAP_INVALID_OR_MISSING = "3.1.10";
  /**
   * The CIDSet entry i mandatory from a subset of composite font
   */
  static final String ERROR_FONTS_CIDSET_MISSING_FOR_SUBSET = "3.1.11";
  /**
   * The CMap of the Composite Font is missing or invalid
   */
  static final String ERROR_FONTS_ENCODING_ERROR = "3.1.12";
  /**
   * Encoding entry can't be read due to IOException
   */
  static final String ERROR_FONTS_ENCODING_IO = "3.1.13";
  /**
   * The embedded font is damaged
   */
  static final String ERROR_FONTS_DAMAGED = "3.2";
  /**
   * The embedded Type1 font is damaged
   */
  static final String ERROR_FONTS_TYPE1_DAMAGED = "3.2.1";
  /**
   * The embedded TrueType font is damaged
   */
  static final String ERROR_FONTS_TRUETYPE_DAMAGED = "3.2.2";
  /**
   * The embedded composite font is damaged
   */
  static final String ERROR_FONTS_CID_DAMAGED = "3.2.3";
  /**
   * The embedded type 3 font is damaged
   */
  static final String ERROR_FONTS_TYPE3_DAMAGED = "3.2.4";
  /**
   * The embedded CID Map is damaged
   */
  static final String ERROR_FONTS_CID_CMAP_DAMAGED = "3.2.5";

  /**
   * Common error for a Glyph problem
   */
  static final String ERROR_FONTS_GLYPH = "3.3";
  /**
   * a glyph is missing
   */
  static final String ERROR_FONTS_GLYPH_MISSING = "3.3.1";
  /**
   * a glyph is missing
   */
  static final String ERROR_FONTS_UNKNOWN_FONT_REF = "3.3.2";

  // -----------------------------------------------------------
  // ---- TRANSPARENCY ERRORS 4.x...
  // -----------------------------------------------------------
  static final String ERROR_TRANSPARENCY_MAIN = "4";
  /**
   * Common transparency error
   */
  static final String ERROR_TRANSPARENCY_EXT_GRAPHICAL_STATE = "4.1";
  /**
   * Soft mask entry is present but is forbidden
   */
  static final String ERROR_TRANSPARENCY_EXT_GS_SOFT_MASK = "4.1.1";
  /**
   * Ca or/and ca entry are present but the value isn't 1.0
   */
  static final String ERROR_TRANSPARENCY_EXT_GS_CA = "4.1.2";
  /**
   * BlendMode value isn't valid (only Normal and Compatible are authorized)
   */
  static final String ERROR_TRANSPARENCY_EXT_GS_BLEND_MODE = "4.1.3";

  // -----------------------------------------------------------
  // ---- ANNOTATION ERRORS 5.x...
  // -----------------------------------------------------------
  static final String ERROR_ANNOT_MAIN = "5";
  /**
   * Common missing field error in annotation dictionary
   */
  static final String ERROR_ANNOT_MISSING_FIELDS = "5.1";
  /**
   * The subtype entry is missing from the annotation dictionary
   */
  static final String ERROR_ANNOT_MISSING_SUBTYPE = "5.1.1";
  /**
   * The AP dictionary of the annotation contains forbidden/invalid entries
   * (only the N entry is authorized)
   */
  static final String ERROR_ANNOT_MISSING_AP_N_CONTENT = "5.1.2";

  /**
   * Common forbidden field error in annotation dictionary
   */
  static final String ERROR_ANNOT_FORBIDDEN_ELEMENT = "5.2";
  /**
   * This type of annotation is forbidden (ex : Movie)
   */
  static final String ERROR_ANNOT_FORBIDDEN_SUBTYPE = "5.2.1";
  /**
   * The annotation uses a flag which is forbidden.
   */
  static final String ERROR_ANNOT_FORBIDDEN_FLAG = "5.2.2";
  /**
   * Annotation uses a Color profile which isn't the same than the profile
   * contained by the OutputIntent
   */
  static final String ERROR_ANNOT_FORBIDDEN_COLOR = "5.2.3";
  /**
   * Dest entry can't be used if the A element is used too
   */
  static final String ERROR_ANNOT_FORBIDDEN_DEST = "5.2.4";
  /**
   * The AA field is forbidden for the Widget annotation when the PDF is a PDF/A
   */
  static final String ERROR_ANNOT_FORBIDDEN_AA = "5.2.5";
  /**
   * The annotation uses a flag which is not recommended but not forbidden by
   * the ISO 19005-1:2005.
   */
  static final String ERROR_ANNOT_NOT_RECOMMENDED_FLAG = "5.2.6";

  /**
   * Common Invalid field error in annotation dictionary
   */
  static final String ERROR_ANNOT_INVALID_ELEMENT = "5.3";
  /**
   * The AP dictionary of the annotation contains forbidden/invalid entries
   * (only the N entry is authorized)
   */
  static final String ERROR_ANNOT_INVALID_AP_CONTENT = "5.3.1";
  /**
   * Ca or/and ca entry are present but the value isn't 1.0
   */
  static final String ERROR_ANNOT_INVALID_CA = "5.3.2";
  /**
   * Dest entry of an annotation can't be checked due to an IO Exception
   */
  static final String ERROR_ANNOT_INVALID_DEST = "5.3.3";

  // -----------------------------------------------------------
  // ---- ACTION ERRORS 6.x...
  // -----------------------------------------------------------
  static final String ERROR_ACTION_MAIN = "6";

  /**
   * Common invalid action error
   */
  static final String ERROR_ACTION_INVALID_ACTIONS = "6.1";
  /**
   * A mandatory entry in the action dictionary is missing
   */
  static final String ERROR_ACTION_MISING_KEY = "6.1.1";
  /**
   * Some elements of the annotation dictionary have an invalid type (ex : array
   * instead of Dictionary)
   */
  static final String ERROR_ACTION_INVALID_TYPE = "6.1.3";
  /**
   * The H entry of a Hide action is set to true (so some annotation can be
   * hide)
   */
  static final String ERROR_ACTION_HIDE_H_INVALID = "6.1.4";
  /**
   * Common forbidden action error
   */
  static final String ERROR_ACTION_FORBIDDEN_ACTIONS = "6.2";
  /**
   * Named action other than predefined not allowed
   */
  static final String ERROR_ACTION_FORBIDDEN_ACTIONS_NAMED = "6.2.1";
  /**
   * Additional action entry is forbidden
   */
  static final String ERROR_ACTION_FORBIDDEN_ADDITIONAL_ACTION = "6.2.2";
  /**
   * Additional action entry is forbidden in a form field object
   */
  static final String ERROR_ACTION_FORBIDDEN_ADDITIONAL_ACTIONS_FIELD = "6.2.3";
  /**
   * A widget annotation linked with a form field shall not have any action
   */
  static final String ERROR_ACTION_FORBIDDEN_WIDGET_ACTION_FIELD = "6.2.4";
  /**
   * An explicitly forbidden action is used in the PDF file.
   */
  static final String ERROR_ACTION_FORBIDDEN_ACTIONS_EXPLICITLY_FORBIDDEN = "6.2.5";
  /**
   * Actions is rejected if it isn't defined in the PDF Reference Third Edition
   * This is to avoid not consistent file due to new features of the PDF format.
   */
  static final String ERROR_ACTION_FORBIDDEN_ACTIONS_UNDEF = "6.2.6";
  // -----------------------------------------------------------
  // ---- METADATA ERRORS 7.x...
  // -----------------------------------------------------------
  /**
   * Main metadata error code
   */
  static final String ERROR_METADATA_MAIN = "7";

  /**
   * Invalid metadata found
   */
  static final String ERROR_METADATA_FORMAT = "7.1";

  /**
   * Unknown metadata
   */
  static final String ERROR_METADATA_PROPERTY_UNKNOWN = "7.1.1";

  /**
   * Invalid xmp metadata format
   */
  static final String ERROR_METADATA_PROPERTY_FORMAT = "7.1.2";

  /**
   * Metadata mismatch between PDF Dictionnary and xmp
   */
  static final String ERROR_METADATA_MISMATCH = "7.2";

  /**
   * Invalid information in xpacket processing instruction
   */
  static final String ERROR_METADATA_XPACKET_DEPRECATED = "7.0.0";

  /**
   * Description schema required not embedded
   */
  static final String ERROR_METADATA_ABSENT_DESCRIPTION_SCHEMA = "7.3";

  /**
   * A required namespace URI missing
   */
  static final String ERROR_METADATA_NS_URI_MISSING = "7.4";

  /**
   * A namespace URI has an unexpected value
   */
  static final String ERROR_METADATA_WRONG_NS_URI = "7.4.1";

  /**
   * A namespace prefix has an unexpected value
   */
  static final String ERROR_METADATA_WRONG_NS_PREFIX = "7.4.2";

  /**
   * Required property is missing
   */
  static final String ERROR_METADATA_PROPERTY_MISSING = "7.5";

  /**
   * A valueType is used but is not declared
   */
  static final String ERROR_METADATA_UNKNOWN_VALUETYPE = "7.6";

  /**
   * PDF/A Identification Schema not found
   */
  static final String ERROR_METADATA_PDFA_ID_MISSING = "7.11";

  /**
   * PDF/A Identification Conformance Invalid
   */
  static final String ERROR_METADATA_INVALID_PDFA_CONFORMANCE = "7.11.1";

  /**
   * PDF/A Identification Version Identifier Invalid (pdfaid:part)
   */
  static final String ERROR_METADATA_INVALID_PDFA_VERSION_ID = "7.11.2";

  /**
   * rdf:about is missing
  */
  static final String ERROR_METADATA_RDF_ABOUT_ATTRIBUTE_MISSING="7.0";
	
	
  /**
  * One of rdf:about attribute embedded in RDF:rdf have a different value than the others
  */
  static final String ERROR_METADATA_RDF_ABOUT_ATTRIBUTE_INEQUAL_VALUE="7.0.1";

  /**
  * a category has an invalid value in one property description (must be internal or external)
  */
  static final String ERROR_METADATA_CATEGORY_PROPERTY_INVALID="7.5.1";

}

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
public class PreflightConstants
{

    private PreflightConstants()
    {
    }

    public static final String FORMAT_PDF_A1B = "PDF/A1-b";
    public static final String FORMAT_PDF_A1A = "PDF/A1-a";

    // -----------------------------------------------------------
    // ---- CONSTANTS
    // -----------------------------------------------------------
    public static final int EDOC_TOKEN_MGR_ERROR = 255;
    public static final String EDOC_TOKEN_MGR_ERROR_TAG = " ERROR_CODE: ";
    public static final int MAX_DICT_ENTRIES = 4095;
    public static final int MAX_ARRAY_ELEMENTS = 8191;
    public static final int MAX_NAME_SIZE = 127;
    public static final int MAX_STRING_LENGTH = 65535;
    public static final int MAX_INDIRECT_OBJ = 8388607;
    public static final int MAX_CID = 65535;
    public static final int MAX_GRAPHIC_STATES = 28;
    public static final int MAX_DEVICE_N_LIMIT = 8;
    public static final float MAX_POSITIVE_FLOAT = 32767f;
    public static final float MAX_NEGATIVE_FLOAT = -32767f;

    public static final String FONT_DICTIONARY_VALUE_COMPOSITE = "Type0";
    public static final String FONT_DICTIONARY_VALUE_TRUETYPE = "TrueType";
    public static final String FONT_DICTIONARY_VALUE_TYPE1 = "Type1";
    public static final String FONT_DICTIONARY_VALUE_TYPE1C = "Type1C";
    public static final String FONT_DICTIONARY_VALUE_MMTYPE = "MMType1";
    public static final String FONT_DICTIONARY_VALUE_TYPE3 = "Type3";
    public static final String FONT_DICTIONARY_VALUE_TYPE0 = "CIDFontType0";
    public static final String FONT_DICTIONARY_VALUE_TYPE0C = "CIDFontType0C";
    public static final String FONT_DICTIONARY_VALUE_TYPE2 = "CIDFontType2";

    public static final String FONT_DICTIONARY_VALUE_CMAP_IDENTITY_H = "Identity-H";
    public static final String FONT_DICTIONARY_VALUE_CMAP_IDENTITY_V = "Identity-V";

    public static final int FONT_DICTIONARY_DEFAULT_CMAP_WMODE = 0;

    public static final String STREAM_DICTIONARY_VALUE_FILTER_LZW = "LZWDecode";
    public static final String STREAM_DICTIONARY_VALUE_FILTER_ASCII_HEX = "ASCIIHexDecode";
    public static final String STREAM_DICTIONARY_VALUE_FILTER_ASCII_85 = "ASCII85Decode";
    public static final String STREAM_DICTIONARY_VALUE_FILTER_RUN = "RunLengthDecode";
    public static final String STREAM_DICTIONARY_VALUE_FILTER_CCITTFF = "CCITTFaxDecode";
    public static final String STREAM_DICTIONARY_VALUE_FILTER_JBIG = "JBIG2Decode";
    public static final String STREAM_DICTIONARY_VALUE_FILTER_DCT = "DCTDecode";
    public static final String STREAM_DICTIONARY_VALUE_FILTER_FLATE_DECODE = "FlateDecode";

    public static final String INLINE_DICTIONARY_VALUE_FILTER_LZW = "LZW";

    public static final String INLINE_DICTIONARY_VALUE_FILTER_ASCII_HEX = "AHx";
    public static final String INLINE_DICTIONARY_VALUE_FILTER_ASCII_85 = "A85";
    public static final String INLINE_DICTIONARY_VALUE_FILTER_RUN = "RL";
    public static final String INLINE_DICTIONARY_VALUE_FILTER_CCITTFF = "CCF";
    public static final String INLINE_DICTIONARY_VALUE_FILTER_DCT = "DCT";
    public static final String INLINE_DICTIONARY_VALUE_FILTER_FLATE_DECODE = "Fl";

    public static final String ANNOT_DICTIONARY_VALUE_SUBTYPE_PRINTERMARK = "PrinterMark";
    public static final String ANNOT_DICTIONARY_VALUE_SUBTYPE_TRAPNET = "TrapNet";

    public static final String ACTION_DICTIONARY_VALUE_ATYPE_NOOP = "NOP";
    public static final String ACTION_DICTIONARY_VALUE_ATYPE_SETSTATE = "SetState";

    public static final String ACTION_DICTIONARY_VALUE_ATYPE_NAMED_NEXT = "NextPage";
    public static final String ACTION_DICTIONARY_VALUE_ATYPE_NAMED_PREV = "PrevPage";
    public static final String ACTION_DICTIONARY_VALUE_ATYPE_NAMED_FIRST = "FirstPage";
    public static final String ACTION_DICTIONARY_VALUE_ATYPE_NAMED_LAST = "LastPage";

    
    /**
     * Error code uses by the Validator when there is an error without error code.
     */
    public static final String ERROR_UNKNOWN_ERROR = "-1";

    // -----------------------------------------------------------
    // ---- FILE STRUCTURE ERRORS 1.x...
    // -----------------------------------------------------------

    /**
     * Error code for syntax error
     */
    public static final String ERROR_SYNTAX_MAIN = "1";
    // error code category which can occur in each pdf part
    public static final String ERROR_SYNTAX_COMMON = "1.0";
    /**
     * Too many entries in a dictionary object
     */
    public static final String ERROR_SYNTAX_TOO_MANY_ENTRIES = "1.0.1";
    /**
     * Too many element in an array object
     */
    public static final String ERROR_SYNTAX_ARRAY_TOO_LONG = "1.0.2";
    /**
     * The name length is too long
     */
    public static final String ERROR_SYNTAX_NAME_TOO_LONG = "1.0.3";
    /**
     * The literal string is too long
     */
    public static final String ERROR_SYNTAX_LITERAL_TOO_LONG = "1.0.4";
    /**
     * The hexa string is too long
     */
    public static final String ERROR_SYNTAX_HEXA_STRING_TOO_LONG = "1.0.5";
    /**
     * The number is out of Range ( ex : greater than 2^31-1)
     */
    public static final String ERROR_SYNTAX_NUMERIC_RANGE = "1.0.6";
    /**
     * A dictionary key isn't a name
     */
    public static final String ERROR_SYNTAX_DICTIONARY_KEY_INVALID = "1.0.7";
    /**
     * The language declared doesn't match with the RFC1766
     */
    public static final String ERROR_SYNTAX_LANG_NOT_RFC1766 = "1.0.8";
    /**
     * There are too many objects
     */
    public static final String ERROR_SYNTAX_INDIRECT_OBJ_RANGE = "1.0.9";
    /**
     * CID too long
     */
    public static final String ERROR_SYNTAX_CID_RANGE = "1.0.10";
    /**
     * Hexa string shall contain even number of non white space char
     */
    public static final String ERROR_SYNTAX_HEXA_STRING_EVEN_NUMBER = "1.0.11";
    /**
     * Hexa string contain non hexadecimal characters
     */
    public static final String ERROR_SYNTAX_HEXA_STRING_INVALID = "1.0.12";
    /**
     * An object is missing from the document
     */
    public static final String ERROR_SYNTAX_MISSING_OFFSET = "1.0.13";
    /**
     * An object has an invalid offset
     */
    public static final String ERROR_SYNTAX_INVALID_OFFSET = "1.0.14";

    public static final String ERROR_SYNTAX_HEADER = "1.1";

    public static final String ERROR_SYNTAX_HEADER_FIRST_CHAR = "1.1.1";

    public static final String ERROR_SYNTAX_HEADER_FILE_TYPE = "1.1.2";

    /**
     * Common error about body syntax
     */
    public static final String ERROR_SYNTAX_BODY = "1.2";
    /**
     * Error on the object delimiters (obj / endobj)
     */
    public static final String ERROR_SYNTAX_OBJ_DELIMITER = "1.2.1";
    /**
     * Error on the stream delimiters (stream / endstream)
     */
    public static final String ERROR_SYNTAX_STREAM_DELIMITER = "1.2.2";
    /**
     * Required fields are missing from the dictionary
     */
    public static final String ERROR_SYNTAX_DICT_INVALID = "1.2.3";
    /**
     * The length entry is missing from the stream dictionary
     */
    public static final String ERROR_SYNTAX_STREAM_LENGTH_MISSING = "1.2.4";
    /**
     * The length of the stream dictionary and the stream length is inconsistent
     */
    public static final String ERROR_SYNTAX_STREAM_LENGTH_INVALID = "1.2.5";
    /**
     * F or/and FFilter or/and FDecodeParms are present in a stream dictionary
     */
    public static final String ERROR_SYNTAX_STREAM_FX_KEYS = "1.2.6";
    /**
     * The stream uses an invalid filter (The LZW)
     */
    public static final String ERROR_SYNTAX_STREAM_INVALID_FILTER = "1.2.7";
    /**
     * The content stream has some syntax errors
     */
    public static final String ERROR_SYNTAX_CONTENT_STREAM_INVALID = "1.2.8";
    /**
     * EmbeddedFile entry is present in a FileSpecification dictionary
     */
    public static final String ERROR_SYNTAX_EMBEDDED_FILES = "1.2.9";
    /**
     * The content stream uses an unsupported operator
     */
    public static final String ERROR_SYNTAX_CONTENT_STREAM_UNSUPPORTED_OP = "1.2.10";
    /**
     * The content stream contains an invalid argument for the operator
     */
    public static final String ERROR_SYNTAX_CONTENT_STREAM_INVALID_ARGUMENT = "1.2.11";
    /**
     * The stream uses a filter which isn't defined in the PDF Reference document.
     */
    public static final String ERROR_SYNTAX_STREAM_UNDEFINED_FILTER = "1.2.12";
    /**
     * The stream can't be processed
     */
    public static final String ERROR_SYNTAX_STREAM_DAMAGED = "1.2.13";
    /**
     * There are no catalog dictionary in the PDF File
     */
    public static final String ERROR_SYNTAX_NOCATALOG = "1.2.14";
    /**
     * Common error about the cross ref table
     */
    public static final String ERROR_SYNTAX_CROSS_REF = "1.3";
    /**
     * Common error about the trailer
     */
    public static final String ERROR_SYNTAX_TRAILER = "1.4";
    /**
     * ID is missing from the trailer
     */
    public static final String ERROR_SYNTAX_TRAILER_MISSING_ID = "1.4.1";
    /**
     * Encrypt is forbidden
     */
    public static final String ERROR_SYNTAX_TRAILER_ENCRYPT = "1.4.2";
    /**
     * An trailer entry has an invalid type
     */
    public static final String ERROR_SYNTAX_TRAILER_TYPE_INVALID = "1.4.3";
    /**
     * Size is missing from the trailer
     */
    public static final String ERROR_SYNTAX_TRAILER_MISSING_SIZE = "1.4.4";
    /**
     * Root is missing from the trailer
     */
    public static final String ERROR_SYNTAX_TRAILER_MISSING_ROOT = "1.4.5";
    /**
     * ID in 1st trailer and the last is different
     */
    public static final String ERROR_SYNTAX_TRAILER_ID_CONSISTENCY = "1.4.6";
    /**
     * EmbeddedFile entry is present in the Names dictionary
     */
    public static final String ERROR_SYNTAX_TRAILER_CATALOG_EMBEDDEDFILES = "1.4.7";
    /**
     * Optional content is forbidden
     */
    public static final String ERROR_SYNTAX_TRAILER_CATALOG_OCPROPERTIES = "1.4.8";
    /**
     * Errors in the Outlines dictionary
     */
    public static final String ERROR_SYNTAX_TRAILER_OUTLINES_INVALID = "1.4.9";
    /**
     * Last %%EOF sequence is followed by data
     */
    public static final String ERROR_SYNTAX_TRAILER_EOF = "1.4.10";

    // -----------------------------------------------------------
    // ---- GRAPHIC ERRORS 2.x...
    // -----------------------------------------------------------

    /**
     * Main error code for graphical problems
     */
    public static final String ERROR_GRAPHIC_MAIN = "2";
    
    public static final String ERROR_GRAPHIC_INVALID = "2.1";
    /**
     * BBox Entry of a Form XObject is missing or isn't an Array
     */
    public static final String ERROR_GRAPHIC_INVALID_BBOX = "2.1.1";
    /**
     * The OutputIntent dictionary is invalid
     */
    public static final String ERROR_GRAPHIC_OUTPUT_INTENT_INVALID_ENTRY = "2.1.2";
    /**
     * The S entry of the OutputIntent isn't GTS_PDFA1
     */
    public static final String ERROR_GRAPHIC_OUTPUT_INTENT_S_VALUE_INVALID = "2.1.3";
    /**
     * The ICC Profile is invalid
     */
    public static final String ERROR_GRAPHIC_OUTPUT_INTENT_ICC_PROFILE_INVALID = "2.1.4";
    /**
     * There are more than one ICC Profile
     */
    public static final String ERROR_GRAPHIC_OUTPUT_INTENT_ICC_PROFILE_MULTIPLE = "2.1.5";

    /**
     * Profile version is too recent for PDF 1.4 document
     */
    public static final String ERROR_GRAPHIC_OUTPUT_INTENT_ICC_PROFILE_TOO_RECENT = "2.1.6";

    public static final String ERROR_GRAPHIC_MISSING_FIELD = "2.1.7";

    public static final String ERROR_GRAPHIC_TOO_MANY_GRAPHIC_STATES = "2.1.8";

    public static final String ERROR_GRAPHIC_MISSING_OBJECT = "2.1.9";
    
    public static final String ERROR_GRAPHIC_XOBJECT_INVALID_TYPE = "2.1.10";
    
    /**
     * Main error code for graphical transparency problems
     */
    public static final String ERROR_GRAPHIC_TRANSPARENCY = "2.2";
    /**
     * A Group entry with S = Transparency is used or the S = Null
     */
    public static final String ERROR_GRAPHIC_TRANSPARENCY_GROUP = "2.2.1";
    /**
     * A XObject SMask value isn't None
     */
    public static final String ERROR_GRAPHIC_TRANSPARENCY_SMASK = "2.2.2";

    /**
     * A XObject has an unexpected key defined
     */
    public static final String ERROR_GRAPHIC_UNEXPECTED_KEY = "2.3";

    /**
     * A XObject has an unexpected value for a defined key
     */
    public static final String ERROR_GRAPHIC_UNEXPECTED_VALUE_FOR_KEY = "2.3.2";

    /**
     * An invalid color space is used
     */
    public static final String ERROR_GRAPHIC_INVALID_COLOR_SPACE = "2.4";
    /**
     * RGB color space used in the PDF file but the DestOutputProfile isn't RGB
     */
    public static final String ERROR_GRAPHIC_INVALID_COLOR_SPACE_RGB = "2.4.1";
    /**
     * CMYK color space used in the PDF file but the DestOutputProfile isn't CMYK
     */
    public static final String ERROR_GRAPHIC_INVALID_COLOR_SPACE_CMYK = "2.4.2";
    /**
     * color space used in the PDF file but the DestOutputProfile is missing
     */
    public static final String ERROR_GRAPHIC_INVALID_COLOR_SPACE_MISSING = "2.4.3";
    /**
     * Unknown ColorSpace
     */
    public static final String ERROR_GRAPHIC_INVALID_UNKNOWN_COLOR_SPACE = "2.4.4";
    /**
     * The pattern color space can't be used
     */
    public static final String ERROR_GRAPHIC_INVALID_PATTERN_COLOR_SPACE_FORBIDDEN = "2.4.5";
    /**
     * The pattern is invalid due to missing key or invalid value
     */
    public static final String ERROR_GRAPHIC_INVALID_PATTERN_DEFINITION = "2.4.6";
    /**
     * alternate color space used in the PDF file but the DestOutputProfile isn't consistent
     */
    public static final String ERROR_GRAPHIC_INVALID_COLOR_SPACE_ALTERNATE = "2.4.7";
    /**
     * Base ColorSpace in the Indexed color space is invalid
     */
    public static final String ERROR_GRAPHIC_INVALID_COLOR_SPACE_INDEXED = "2.4.8";
    /**
     * ColorSpace is forbidden due to some restriction (ex : Only DeviceXXX are auth in inlined image)
     */
    public static final String ERROR_GRAPHIC_INVALID_COLOR_SPACE_FORBIDDEN = "2.4.9";

    public static final String ERROR_GRAPHIC_INVALID_COLOR_SPACE_TOO_MANY_COMPONENTS_DEVICEN = "2.4.10";
    /**
     * ICC Based color space used in the PDF file is invalid
     */
    public static final String ERROR_GRAPHIC_INVALID_COLOR_SPACE_ICCBASED = "2.4.11";
    /**
     * Validation asked on a missing ColorSpace
     */
    public static final String ERROR_GRAPHIC_MISSING_COLOR_SPACE_ICCBASED = "2.4.12";
    // -----------------------------------------------------------
    // ---- FONT ERRORS 3.x...
    // -----------------------------------------------------------

    /**
     * Main error code for font problems
     */
    public static final String ERROR_FONTS_MAIN = "3";

    public static final String ERROR_FONTS_INVALID_DATA = "3.1";
    /**
     * Some mandatory fields are missing from the FONT Dictionary
     */
    public static final String ERROR_FONTS_DICTIONARY_INVALID = "3.1.1";
    /**
     * Some mandatory fields are missing from the FONT Descriptor Dictionary
     */
    public static final String ERROR_FONTS_DESCRIPTOR_INVALID = "3.1.2";
    /**
     * Error on the "Font File x" in the Font Descriptor (ex : FontFile and FontFile2 are present in the same
     * dictionary)
     */
    public static final String ERROR_FONTS_FONT_FILEX_INVALID = "3.1.3";
    /**
     * Charset declaration is missing in a Type 1 Subset
     */
    public static final String ERROR_FONTS_CHARSET_MISSING_FOR_SUBSET = "3.1.4";
    /**
     * Encoding is inconsistent with the Font (ex : Symbolic TrueType mustn't declare encoding)
     */
    public static final String ERROR_FONTS_ENCODING = "3.1.5";
    /**
     * Width array and Font program Width are inconsistent
     */
    public static final String ERROR_FONTS_METRICS = "3.1.6";
    /**
     * Required entry in a Composite Font dictionary is missing
     */
    public static final String ERROR_FONTS_CIDKEYED_INVALID = "3.1.7";
    /**
     * The CIDSystemInfo dictionary is invalid
     */
    public static final String ERROR_FONTS_CIDKEYED_SYSINFO = "3.1.8";
    /**
     * The CIDToGID is invalid
     */
    public static final String ERROR_FONTS_CIDKEYED_CIDTOGID = "3.1.9";
    /**
     * The CMap of the Composite Font is missing or invalid
     */
    public static final String ERROR_FONTS_CIDKEYED_CMAP_INVALID_OR_MISSING = "3.1.10";
    /**
     * The CIDSet entry i mandatory from a subset of composite font
     */
    public static final String ERROR_FONTS_CIDSET_MISSING_FOR_SUBSET = "3.1.11";
    /**
     * The CMap of the Composite Font is missing or invalid
     */
    public static final String ERROR_FONTS_ENCODING_ERROR = "3.1.12";
    /**
     * Encoding entry can't be read due to IOException
     */
    public static final String ERROR_FONTS_ENCODING_IO = "3.1.13";
    /**
     * The font type is unknown
     */
    public static final String ERROR_FONTS_UNKNOWN_FONT_TYPE = "3.1.14";
    /**
     * The embedded font is damaged
     */
    public static final String ERROR_FONTS_DAMAGED = "3.2";
    /**
     * The embedded Type1 font is damaged
     */
    public static final String ERROR_FONTS_TYPE1_DAMAGED = "3.2.1";
    /**
     * The embedded TrueType font is damaged
     */
    public static final String ERROR_FONTS_TRUETYPE_DAMAGED = "3.2.2";
    /**
     * The embedded composite font is damaged
     */
    public static final String ERROR_FONTS_CID_DAMAGED = "3.2.3";
    /**
     * The embedded type 3 font is damaged
     */
    public static final String ERROR_FONTS_TYPE3_DAMAGED = "3.2.4";
    /**
     * The embedded CID Map is damaged
     */
    public static final String ERROR_FONTS_CID_CMAP_DAMAGED = "3.2.5";

    /**
     * Common error for a Glyph problem
     */
    public static final String ERROR_FONTS_GLYPH = "3.3";
    /**
     * a glyph is missing
     */
    public static final String ERROR_FONTS_GLYPH_MISSING = "3.3.1";
    /**
     * a glyph is missing
     */
    public static final String ERROR_FONTS_UNKNOWN_FONT_REF = "3.3.2";

    // -----------------------------------------------------------
    // ---- TRANSPARENCY ERRORS 4.x...
    // -----------------------------------------------------------
    public static final String ERROR_TRANSPARENCY_MAIN = "4";
    /**
     * Common transparency error
     */
    public static final String ERROR_TRANSPARENCY_EXT_GRAPHICAL_STATE = "4.1";
    /**
     * Soft mask entry is present but is forbidden
     */
    public static final String ERROR_TRANSPARENCY_EXT_GS_SOFT_MASK = "4.1.1";
    /**
     * Ca or/and ca entry are present but the value isn't 1.0
     */
    public static final String ERROR_TRANSPARENCY_EXT_GS_CA = "4.1.2";
    /**
     * BlendMode value isn't valid (only Normal and Compatible are authorized)
     */
    public static final String ERROR_TRANSPARENCY_EXT_GS_BLEND_MODE = "4.1.3";

    // -----------------------------------------------------------
    // ---- ANNOTATION ERRORS 5.x...
    // -----------------------------------------------------------
    public static final String ERROR_ANNOT_MAIN = "5";
    /**
     * Common missing field error in annotation dictionary
     */
    public static final String ERROR_ANNOT_MISSING_FIELDS = "5.1";
    /**
     * The subtype entry is missing from the annotation dictionary
     */
    public static final String ERROR_ANNOT_MISSING_SUBTYPE = "5.1.1";
    /**
     * The AP dictionary of the annotation contains forbidden/invalid entries (only the N entry is authorized)
     */
    public static final String ERROR_ANNOT_MISSING_AP_N_CONTENT = "5.1.2";
    /**
     * An annotation validation is required but there are no element to validate
     */
    public static final String ERROR_ANNOT_MISSING_ANNOTATION_DICTIONARY = "5.1.3";
    /**
     * Common forbidden field error in annotation dictionary
     */
    public static final String ERROR_ANNOT_FORBIDDEN_ELEMENT = "5.2";
    /**
     * This type of annotation is forbidden (ex : Movie)
     */
    public static final String ERROR_ANNOT_FORBIDDEN_SUBTYPE = "5.2.1";
    /**
     * The annotation uses a flag which is forbidden.
     */
    public static final String ERROR_ANNOT_FORBIDDEN_FLAG = "5.2.2";
    /**
     * Annotation uses a Color profile which isn't the same than the profile contained by the OutputIntent
     */
    public static final String ERROR_ANNOT_FORBIDDEN_COLOR = "5.2.3";
    /**
     * Dest entry can't be used if the A element is used too
     */
    public static final String ERROR_ANNOT_FORBIDDEN_DEST = "5.2.4";
    /**
     * The AA field is forbidden for the Widget annotation when the PDF is a PDF/A
     */
    public static final String ERROR_ANNOT_FORBIDDEN_AA = "5.2.5";
    /**
     * The annotation uses a flag which is not recommended but not forbidden by the ISO 19005-1:2005.
     */
    public static final String ERROR_ANNOT_NOT_RECOMMENDED_FLAG = "5.2.6";
    /**
     * The AA field is forbidden for the Catalog when the PDF is a PDF/A
     */
    public static final String ERROR_ANNOT_CATALOG_FORBIDDEN_AA = "5.2.7";
    /**
     * Common Invalid field error in annotation dictionary
     */
    public static final String ERROR_ANNOT_INVALID_ELEMENT = "5.3";
    /**
     * The AP dictionary of the annotation contains forbidden/invalid entries (only the N entry is authorized)
     */
    public static final String ERROR_ANNOT_INVALID_AP_CONTENT = "5.3.1";
    /**
     * Ca or/and ca entry are present but the value isn't 1.0
     */
    public static final String ERROR_ANNOT_INVALID_CA = "5.3.2";
    /**
     * Dest entry of an annotation can't be checked due to an IO Exception
     */
    public static final String ERROR_ANNOT_INVALID_DEST = "5.3.3";

    // -----------------------------------------------------------
    // ---- ACTION ERRORS 6.x...
    // -----------------------------------------------------------
    public static final String ERROR_ACTION_MAIN = "6";

    /**
     * Common invalid action error
     */
    public static final String ERROR_ACTION_INVALID_ACTIONS = "6.1";
    /**
     * A mandatory entry in the action dictionary is missing
     */
    public static final String ERROR_ACTION_MISING_KEY = "6.1.1";
    /**
     * Some elements of the annotation dictionary have an invalid type (ex : array instead of Dictionary)
     */
    public static final String ERROR_ACTION_INVALID_TYPE = "6.1.3";
    /**
     * The H entry of a Hide action is set to true (so some annotation can be hide)
     */
    public static final String ERROR_ACTION_HIDE_H_INVALID = "6.1.4";
    /**
     * An action validation is required but there are no element to validate
     */
    public static final String ERROR_ACTION_MISSING_ACTION_DICTIONARY = "6.1.5";
    /**
     * Common forbidden action error
     */
    public static final String ERROR_ACTION_FORBIDDEN_ACTIONS = "6.2";
    /**
     * Named action other than predefined not allowed
     */
    public static final String ERROR_ACTION_FORBIDDEN_ACTIONS_NAMED = "6.2.1";
    /**
     * Additional action entry is forbidden
     */
    public static final String ERROR_ACTION_FORBIDDEN_ADDITIONAL_ACTION = "6.2.2";
    /**
     * Additional action entry is forbidden in a form field object
     */
    public static final String ERROR_ACTION_FORBIDDEN_ADDITIONAL_ACTIONS_FIELD = "6.2.3";
    /**
     * A widget annotation linked with a form field shall not have any action
     */
    public static final String ERROR_ACTION_FORBIDDEN_WIDGET_ACTION_FIELD = "6.2.4";
    /**
     * An explicitly forbidden action is used in the PDF file.
     */
    public static final String ERROR_ACTION_FORBIDDEN_ACTIONS_EXPLICITLY_FORBIDDEN = "6.2.5";
    /**
     * Actions is rejected if it isn't defined in the PDF Reference Third Edition This is to avoid not consistent file
     * due to new features of the PDF format.
     */
    public static final String ERROR_ACTION_FORBIDDEN_ACTIONS_UNDEF = "6.2.6";
    // -----------------------------------------------------------
    // ---- METADATA ERRORS 7.x...
    // -----------------------------------------------------------
    /**
     * Main metadata error code
     */
    public static final String ERROR_METADATA_MAIN = "7";

    /**
     * Invalid metadata found
     */
    public static final String ERROR_METADATA_FORMAT = "7.1";

    /**
     * Unknown metadata
     */
    public static final String ERROR_METADATA_PROPERTY_UNKNOWN = "7.1.1";

    /**
     * Invalid xmp metadata format
     */
    public static final String ERROR_METADATA_PROPERTY_FORMAT = "7.1.2";
    /**
     * Unexpected type of a Metadata entry
     */
    public static final String ERROR_METADATA_FORMAT_UNKNOWN = "7.1.3";
    /**
     * Invalid metadata, unable to process the font due to IOException
     */
    public static final String ERROR_METADATA_FORMAT_STREAM = "7.1.4";
    /**
     * Invalid metadata, unable to process the font due to Invalid XPacket exception
     */
    public static final String ERROR_METADATA_FORMAT_XPACKET = "7.1.5";
    /**
     * Metadata mismatch between PDF Dictionary and xmp
     */
    public static final String ERROR_METADATA_MISMATCH = "7.2";

    /**
     * Invalid information in xpacket processing instruction
     */
    public static final String ERROR_METADATA_XPACKET_DEPRECATED = "7.0.0";

    /**
     * Description schema required not embedded
     */
    public static final String ERROR_METADATA_ABSENT_DESCRIPTION_SCHEMA = "7.3";

    /**
     * A required namespace URI missing
     */
    public static final String ERROR_METADATA_NS_URI_MISSING = "7.4";

    /**
     * A namespace URI has an unexpected value
     */
    public static final String ERROR_METADATA_WRONG_NS_URI = "7.4.1";

    /**
     * A namespace prefix has an unexpected value
     */
    public static final String ERROR_METADATA_WRONG_NS_PREFIX = "7.4.2";

    /**
     * Required property is missing
     */
    public static final String ERROR_METADATA_PROPERTY_MISSING = "7.5";

    /**
     * A valueType is used but is not declared
     */
    public static final String ERROR_METADATA_UNKNOWN_VALUETYPE = "7.6";

    /**
     * PDF/A Identification Schema not found
     */
    public static final String ERROR_METADATA_PDFA_ID_MISSING = "7.11";

    /**
     * PDF/A Identification Conformance Invalid
     */
    public static final String ERROR_METADATA_INVALID_PDFA_CONFORMANCE = "7.11.1";

    /**
     * PDF/A Identification Version Identifier Invalid (pdfaid:part)
     */
    public static final String ERROR_METADATA_INVALID_PDFA_VERSION_ID = "7.11.2";

    /**
     * rdf:about is missing
     */
    public static final String ERROR_METADATA_RDF_ABOUT_ATTRIBUTE_MISSING = "7.0";

    /**
     * One of rdf:about attribute embedded in RDF:rdf have a different value than the others
     */
    public static final String ERROR_METADATA_RDF_ABOUT_ATTRIBUTE_INEQUAL_VALUE = "7.0.1";

    /**
     * a category has an invalid value in one property description (must be internal or external)
     */
    public static final String ERROR_METADATA_CATEGORY_PROPERTY_INVALID = "7.5.1";

    /**
     * the info dictionary is corrupt or value can't be read
     */
    public static final String ERROR_METADATA_DICT_INFO_CORRUPT = "7.12";
    /**
     * Error about PDF processing : that is not necessary a specific PDF/A validation error
     * but a PDF specification requirement that isn't respected.
     */
    public static final String ERROR_PDF_PROCESSING = "8";
    /**
     * A Mandatory element is missing
     */
    public static final String ERROR_PDF_PROCESSING_MISSING = "8.1";
}

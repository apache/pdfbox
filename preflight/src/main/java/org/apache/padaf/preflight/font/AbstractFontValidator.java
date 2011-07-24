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

package org.apache.padaf.preflight.font;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


import org.apache.commons.io.IOUtils;
import org.apache.padaf.preflight.DocumentHandler;
import org.apache.padaf.preflight.ValidationConstants;
import org.apache.padaf.preflight.ValidationException;
import org.apache.padaf.preflight.ValidationResult.ValidationError;
import org.apache.padaf.preflight.font.AbstractFontContainer.State;
import org.apache.padaf.xmpbox.XMPMetadata;
import org.apache.padaf.xmpbox.parser.XMPDocumentBuilder;
import org.apache.padaf.xmpbox.parser.XmpExpectedRdfAboutAttribute;
import org.apache.padaf.xmpbox.parser.XmpParsingException;
import org.apache.padaf.xmpbox.parser.XmpSchemaException;
import org.apache.padaf.xmpbox.parser.XmpUnknownValueTypeException;
import org.apache.padaf.xmpbox.parser.XmpXpacketEndException;
import org.apache.padaf.xmpbox.type.BadFieldValueException;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDFontDescriptor;
import org.apache.pdfbox.pdmodel.font.PDFontFactory;

public abstract class AbstractFontValidator implements FontValidator,ValidationConstants {
  /**
   * DocumentHandler which contains all useful objects to validate a PDF/A ex :
   * parser JavaCC
   */
  protected DocumentHandler handler = null;
  /**
   * The COSObject which is the starting point of the Font description in the
   * PDF/A file. This object should be an insteance of COSDictionary
   */
  protected COSObject cObj = null;
  /**
   * The cObj casted as COSDictionary
   */
  protected COSDictionary fDictionary = null;
  /**
   * The PdfBox font dictionary wrapper.
   */
  protected PDFont pFont = null;

  /**
   * The Font Container contains the Font Validation state ( valid or not,
   * why...) This Font Container is tested when the font is used as resource.
   * According to the state of this font, the PDF File will be PDF/A conforming
   * file or not. (Ex : if the FontContainer flags this font as not embedded,
   * the PDF is a PDF/A only if the font is used in a Rendering Mode 3.)
   */
  protected AbstractFontContainer fontContainer = null;

  /**
   * Abstract Constructor
   * @param handler the handled document
   * @param cObj The cos object representing the font
   * @throws ValidationException when object creation fails
   */
  public AbstractFontValidator(DocumentHandler handler, COSObject cObj)
  throws ValidationException {
    try {
      this.handler = handler;
      this.cObj = cObj;
      this.fDictionary = (COSDictionary) cObj.getObject();
      this.pFont = PDFontFactory.createFont(fDictionary);

      this.fontContainer = instanciateContainer(this.pFont);
      this.handler.addFont(this.pFont.getCOSObject(), this.fontContainer);
    } catch (IOException e) {
      throw new ValidationException(
          "Unable to instantiate a FontValidator object : " + e.getMessage());
    }
  }

  protected AbstractFontContainer instanciateContainer (PDFont fd) {
    String subtype = fd.getSubType();
    if (FONT_DICTIONARY_VALUE_TRUETYPE.equals(subtype)) {
      return new TrueTypeFontContainer(fd);
    } else if (FONT_DICTIONARY_VALUE_MMTYPE.equals(subtype)) {
      return new Type1FontContainer(fd);
    } else if (FONT_DICTIONARY_VALUE_TYPE1.equals(subtype)) {
      return new Type1FontContainer(fd);
    } else if (FONT_DICTIONARY_VALUE_TYPE3.equals(subtype)) {
      return new Type3FontContainer(fd);
    } else if (FONT_DICTIONARY_VALUE_COMPOSITE.equals(subtype)) {
      return new CompositeFontContainer(fd);
    } else {
      return new UndefFontContainer(fd);
    }
  }

  private static final String subSetPattern = "^[A-Z]{6}\\+.*";

  public static boolean isSubSet(String fontName) {
    return fontName.matches(subSetPattern);
  }

  public static String getSubSetPatternDelimiter() {
    return "+";
  }

  /*
   * (non-Javadoc)
   * 
   * @see net.awl.edoc.pdfa.validation.font.FontValidator#getState()
   */
  public State getState() {
    return this.fontContainer.isValid();
  }

  /*
   * (non-Javadoc)
   * 
   * @see net.awl.edoc.pdfa.validation.font.FontValidator#getValdiationErrors()
   */
  public List<ValidationError> getValdiationErrors() {
    return this.fontContainer.getErrors();
  }

  /**
   * Type0, Type1 and TrueType FontValidatir call this method to check the
   * FontFile meta data.
   * 
   * @param fontDesc
   *          The FontDescriptor which contains the FontFile stream
   * @param fontFile
   *          The font file stream to check
   * @return true if the meta data is valid, false otherwise
   * @throws ValidationException when checking fails
   */
  protected boolean checkFontFileMetaData(PDFontDescriptor fontDesc,
      PDStream fontFile) throws ValidationException {
    PDMetadata metadata = fontFile.getMetadata();
    if (metadata != null) {
      // --- Filters are forbidden in a XMP stream
      if (metadata.getFilters() != null && !metadata.getFilters().isEmpty()) {
        fontContainer.addError(new ValidationError(
            ValidationConstants.ERROR_SYNTAX_STREAM_INVALID_FILTER,
        "Filter specified in font file metadata dictionnary"));
        return false;
      }

      // --- extract the meta data content
      byte[] mdAsBytes = null;
      try {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        InputStream metaDataContent = metadata.createInputStream();
        IOUtils.copyLarge(metaDataContent, bos);
        IOUtils.closeQuietly(metaDataContent);
        IOUtils.closeQuietly(bos);
        mdAsBytes = bos.toByteArray();
      } catch (IOException e) {
        throw new ValidationException("Unable to read font metadata due to : "
            + e.getMessage(), e);
      }

      try {

        XMPDocumentBuilder xmpBuilder = new XMPDocumentBuilder();
        XMPMetadata xmpMeta = xmpBuilder.parse(mdAsBytes);

        FontMetaDataValidation fontMDval = new FontMetaDataValidation();
        List<ValidationError> ve = new ArrayList<ValidationError>();
        boolean isVal = fontMDval.analyseFontName(xmpMeta, fontDesc, ve);
        isVal = isVal && fontMDval.analyseFontName(xmpMeta, fontDesc, ve);
        for (ValidationError validationError : ve) {
          fontContainer.addError(validationError);
        }
        return isVal;

      } catch (XmpUnknownValueTypeException e) {
        fontContainer.addError(new ValidationError(
            ValidationConstants.ERROR_METADATA_UNKNOWN_VALUETYPE, e
            .getMessage()));
        return false;
      } catch (XmpParsingException e) {
        fontContainer.addError(new ValidationError(
            ValidationConstants.ERROR_METADATA_FORMAT, e.getMessage()));
        return false;
      } catch (XmpSchemaException e) {
        fontContainer.addError(new ValidationError(
            ValidationConstants.ERROR_METADATA_FORMAT, e.getMessage()));
        return false;
      } catch (XmpExpectedRdfAboutAttribute e) {
        fontContainer.addError(new ValidationError(ValidationConstants.ERROR_METADATA_RDF_ABOUT_ATTRIBUTE_MISSING,e.getMessage()));
        return false;
      } catch (BadFieldValueException e) {
        fontContainer.addError(new ValidationError(ValidationConstants.ERROR_METADATA_CATEGORY_PROPERTY_INVALID,e.getMessage()));
        return false;
      } catch (XmpXpacketEndException e) {
        throw new ValidationException("Unable to parse font metadata due to : "
            + e.getMessage(), e);
      }
    }

    // --- No MetaData, valid
    return true;
  }
}

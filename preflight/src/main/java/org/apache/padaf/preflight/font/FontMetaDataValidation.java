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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


import org.apache.padaf.preflight.ValidationConstants;
import org.apache.padaf.preflight.ValidationException;
import org.apache.padaf.preflight.ValidationResult.ValidationError;
import org.apache.padaf.xmpbox.XMPMetadata;
import org.apache.padaf.xmpbox.schema.DublinCoreSchema;
import org.apache.padaf.xmpbox.schema.XMPRightsManagementSchema;
import org.apache.padaf.xmpbox.type.AbstractField;
import org.apache.padaf.xmpbox.type.BooleanType;
import org.apache.padaf.xmpbox.type.ComplexProperty;
import org.apache.padaf.xmpbox.type.TextType;
import org.apache.pdfbox.pdmodel.font.PDFontDescriptor;

/**
 * Class used to validate the MetaData entry of the Font File Stream dictionary.
 */
public class FontMetaDataValidation {

  public List<ValidationError> validatePDFAIdentifer(XMPMetadata metadata,
      PDFontDescriptor fontDesc) throws ValidationException {
    List<ValidationError> ve = new ArrayList<ValidationError>();

    analyseFontName(metadata, fontDesc, ve);
    analyseRights(metadata, fontDesc, ve);

    return ve;
  }

  /**
   * Value of the dc:title must be the same as the FontName in the font
   * descriptor.
   * 
   * @param metadata
   *          XMPMetaData of the Font File Stream
   * @param fontDesc
   *          The FontDescriptor dictionary
   * @param ve
   *          the list of validation error to update if the validation fails
   * @throws ValidationException
   */
  public boolean analyseFontName(XMPMetadata metadata,
      PDFontDescriptor fontDesc, List<ValidationError> ve)
      throws ValidationException {
    String fontName = fontDesc.getFontName();
    String noSubSetName = fontName;
    if (AbstractFontValidator.isSubSet(fontName)) {
      noSubSetName = fontName.split(AbstractFontValidator
          .getSubSetPatternDelimiter())[1];
    }

    DublinCoreSchema dc = metadata.getDublinCoreSchema();
    if (dc.getTitle() != null) {
      String defaultTitle = dc.getTitleValue("x-default");
      if (defaultTitle != null) {

        if (!defaultTitle.equals(fontName)
            && (noSubSetName != null && !defaultTitle.equals(noSubSetName))) {
          StringBuilder sb = new StringBuilder(80);
          sb
              .append("FontName")
              .append(
                  " present in the FontDescriptor dictionary doesn't match with XMP information dc:title of the Font File Stream.");
          ve.add(new ValidationError(
              ValidationConstants.ERROR_METADATA_MISMATCH, sb.toString()));
          return false;
        }

        // --- default value is the right one
        return true;
      } else {
        Iterator<AbstractField> it = dc.getTitle().getContainer()
            .getAllProperties().iterator();
        boolean empty = true;
        while (it.hasNext()) {
          empty = false;
          AbstractField tmp = it.next();
          if (tmp != null && tmp instanceof TextType) {
            if (((TextType) tmp).getStringValue().equals(fontName)
                || (noSubSetName != null && ((TextType) tmp).getStringValue()
                    .equals(noSubSetName))) {
              // value found, return
              return true;
            }
          }
        }

        // title doesn't match, it is an error.
        StringBuilder sb = new StringBuilder(80);
        sb.append("FontName");
        if (empty) {
          sb
              .append(" present in the FontDescriptor dictionary can't be found in XMP information the Font File Stream.");
          ve.add(new ValidationError(
              ValidationConstants.ERROR_METADATA_PROPERTY_MISSING, sb
                  .toString()));
        } else {
          sb
              .append(" present in the FontDescriptor dictionary doesn't match with XMP information dc:title of the Font File Stream.");
          ve.add(new ValidationError(
              ValidationConstants.ERROR_METADATA_MISMATCH, sb.toString()));
        }
        return false;
      }
    }

    // ---- dc:title is required
    ve.add(new ValidationError(
        ValidationConstants.ERROR_METADATA_PROPERTY_MISSING,
        "dc:title is missing from the FontFile MetaData"));
    return false;
  }

  /**
   * If XMP MetaData are present, they must have followings information :
   * <UL>
   * <li>dc:rights
   * <li>Marked (with the value true)
   * <li>Owner
   * <li>UsageTerms
   * </UL>
   * 
   * @param metadata
   *          XMPMetaData of the Font File Stream
   * @param fontDesc
   *          The FontDescriptor dictionary
   * @param ve
   *          the list of validation error to update if the validation fails
   * @throws ValidationException
   */
  public boolean analyseRights(XMPMetadata metadata, PDFontDescriptor fontDesc,
      List<ValidationError> ve) throws ValidationException {
    DublinCoreSchema dc = metadata.getDublinCoreSchema();
    ComplexProperty copyrights = dc.getRights();
    if (copyrights == null || copyrights.getContainer() == null
        || copyrights.getContainer().getAllProperties().isEmpty()) {
      ve
          .add(new ValidationError(
              ValidationConstants.ERROR_METADATA_PROPERTY_MISSING,
              "CopyRights is missing from the XMP information (dc:rights) of the Font File Stream."));
      return false;
    }

    XMPRightsManagementSchema rights = metadata.getXMPRightsManagementSchema();
    BooleanType marked = rights.getMarked();
    if (marked == null) {
      ve
          .add(new ValidationError(
              ValidationConstants.ERROR_METADATA_PROPERTY_MISSING,
              "the XMP information (xmpRights:Marked) missing for the Font File Stream."));
      return false;
    } else if (!marked.getValue()) {
      ve
          .add(new ValidationError(
              ValidationConstants.ERROR_METADATA_PROPERTY_MISSING,
              "the XMP information (xmpRights:Marked) is invalid for the Font File Stream."));
      return false;
    }

    ComplexProperty usage = rights.getUsageTerms();
    if (usage == null || usage.getContainer() == null
        || usage.getContainer().getAllProperties().isEmpty()) {
      ve
          .add(new ValidationError(
              ValidationConstants.ERROR_METADATA_PROPERTY_MISSING,
              "Usage Terms are missing from the XMP information (xmpRights:UsageTerms) of the Font File Stream."));
      return false;
    }

    List<String> owners = rights.getOwnerValue();
    if (owners == null || owners.isEmpty()) {
      ve.add(new ValidationError(
              ValidationConstants.ERROR_METADATA_PROPERTY_MISSING,
              "Owner missing from the XMP information (xmpRights:Owner) of the Font File Stream."));
      return false;
    } else {
    	boolean allEmpty = true;
    	for (String owner : owners) {
			if (!"".equals(owner)) {
				allEmpty = false;
			}
		}
    	if (allEmpty) {
    		ve.add(new ValidationError(
    	              ValidationConstants.ERROR_METADATA_PROPERTY_MISSING,
    	              "Owner missing from the XMP information (xmpRights:Owner) of the Font File Stream."));
    	      return false;	
    	}
    }

    return true;
  }
}

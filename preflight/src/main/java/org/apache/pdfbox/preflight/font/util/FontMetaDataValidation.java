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

package org.apache.pdfbox.preflight.font.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.pdfbox.pdmodel.font.PDFontDescriptor;
import org.apache.pdfbox.preflight.PreflightConstants;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.preflight.exception.ValidationException;
import org.apache.pdfbox.preflight.font.descriptor.FontDescriptorHelper;
import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.schema.DublinCoreSchema;
import org.apache.xmpbox.schema.XMPRightsManagementSchema;
import org.apache.xmpbox.type.AbstractField;
import org.apache.xmpbox.type.ArrayProperty;
import org.apache.xmpbox.type.BadFieldValueException;
import org.apache.xmpbox.type.BooleanType;
import org.apache.xmpbox.type.TextType;

/**
 * Class used to validate the MetaData entry of the Font File Stream dictionary.
 */
public class FontMetaDataValidation
{

    public List<ValidationError> validatePDFAIdentifer(final XMPMetadata metadata, final PDFontDescriptor fontDesc)
            throws ValidationException
    {
        final List<ValidationError> ve = new ArrayList<>();

        analyseFontName(metadata, fontDesc, ve);
        analyseRights(metadata, fontDesc, ve);

        return ve;
    }

    /**
     * Value of the dc:title must be the same as the FontName in the font descriptor.
     * 
     * @param metadata
     *            XMPMetaData of the Font File Stream
     * @param fontDesc
     *            The FontDescriptor dictionary
     * @param ve
     *            the list of validation error to update if the validation fails
     */
    public boolean analyseFontName(final XMPMetadata metadata, final PDFontDescriptor fontDesc, final List<ValidationError> ve)
    {
        final String fontName = fontDesc.getFontName();
        String noSubSetName = fontName;
        if (FontDescriptorHelper.isSubSet(fontName))
        {
            noSubSetName = fontName.split("\\+")[1];
        }

        final DublinCoreSchema dc = metadata.getDublinCoreSchema();
        if (dc != null && dc.getTitleProperty() != null)
        {
            final String defaultTitle;
            try
            {
                defaultTitle = dc.getTitle("x-default");
            }
            catch (BadFieldValueException badFieldValueException)
            {
                final StringBuilder sb = new StringBuilder(80);
                sb.append("Title property of XMP information is not a multi-lingual property");
                ve.add(new ValidationError(PreflightConstants.ERROR_METADATA_PROPERTY_FORMAT, sb.toString()));
                return false;
            }
            if (defaultTitle != null)
            {
                if (!defaultTitle.equals(fontName) && (noSubSetName != null && !defaultTitle.equals(noSubSetName)))
                {
                    final StringBuilder sb = new StringBuilder(80);
                    sb.append("FontName")
                            .append(" present in the FontDescriptor dictionary doesn't match with XMP information dc:title of the Font File Stream.");
                    ve.add(new ValidationError(PreflightConstants.ERROR_METADATA_MISMATCH, sb.toString()));
                    return false;
                }

                // --- default value is the right one
                return true;
            }
            else
            {
                final Iterator<AbstractField> it = dc.getTitleProperty().getContainer().getAllProperties().iterator();
                boolean empty = true;
                while (it.hasNext())
                {
                    empty = false;
                    final AbstractField tmp = it.next();
                    if (tmp instanceof TextType)
                    {
                        final String val = ((TextType) tmp).getStringValue();
                        if (val.equals(fontName) || val.equals(noSubSetName))
                        {
                            // value found, return
                            return true;
                        }
                    }
                }

                // title doesn't match, it is an error.
                final StringBuilder sb = new StringBuilder(80);
                sb.append("FontName");
                if (empty)
                {
                    sb.append(" present in the FontDescriptor dictionary can't be found in XMP information the Font File Stream.");
                    ve.add(new ValidationError(PreflightConstants.ERROR_METADATA_PROPERTY_MISSING, sb.toString()));
                }
                else
                {
                    sb.append(" present in the FontDescriptor dictionary doesn't match with XMP information dc:title of the Font File Stream.");
                    ve.add(new ValidationError(PreflightConstants.ERROR_METADATA_MISMATCH, sb.toString()));
                }
                return false;
            }
        }
        return true;
    }

    /**
     * If XMP MetaData is present, they must have the following information :
     * <UL>
     * <li>dc:rights
     * <li>Marked (with the value true)
     * <li>Owner
     * <li>UsageTerms
     * </UL>
     * 
     * @param metadata
     *            XMPMetaData of the Font File Stream
     * @param fontDesc
     *            The FontDescriptor dictionary
     * @param ve
     *            the list of validation error to update if the validation fails
     * @return true if the analysis found no problems, false if it did.
     */
    public boolean analyseRights(final XMPMetadata metadata, final PDFontDescriptor fontDesc, final List<ValidationError> ve)
    {

        final DublinCoreSchema dc = metadata.getDublinCoreSchema();
        if (dc != null)
        {
            final ArrayProperty copyrights = dc.getRightsProperty();
            if (copyrights == null || copyrights.getContainer() == null
                    || copyrights.getContainer().getAllProperties().isEmpty())
            {
                ve.add(new ValidationError(PreflightConstants.ERROR_METADATA_PROPERTY_MISSING,
                        "CopyRights is missing from the XMP information (dc:rights) of the Font File Stream."));
                return false;
            }
        }

        final XMPRightsManagementSchema rights = metadata.getXMPRightsManagementSchema();
        if (rights != null)
        {
            final BooleanType marked = rights.getMarkedProperty();
            if (marked != null && !marked.getValue())
            {
                ve.add(new ValidationError(PreflightConstants.ERROR_METADATA_PROPERTY_MISSING,
                        "the XMP information (xmpRights:Marked) is invalid for the Font File Stream."));
                return false;
            }

            /*
             * rights.getUsageTerms() & rights.getOwnerValue() should be present but it is only a recommendation : may
             * be it should be useful to append a Warning if these entries are missing.
             */
        }
        return true;
    }
}

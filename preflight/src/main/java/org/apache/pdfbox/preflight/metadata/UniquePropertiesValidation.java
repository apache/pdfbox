/** ***************************************************************************
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
 *************************************************************************** */
package org.apache.pdfbox.preflight.metadata;

import java.util.ArrayList;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.preflight.PreflightConstants;
import org.apache.pdfbox.preflight.ValidationResult;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.preflight.exception.ValidationException;
import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.schema.AdobePDFSchema;
import org.apache.xmpbox.schema.DublinCoreSchema;
import org.apache.xmpbox.schema.XMPBasicSchema;
import org.apache.xmpbox.schema.XMPSchema;
import org.apache.xmpbox.type.AbstractField;

/**
 * Class which checks that certain metadata properties are unique, see PDFBOX-4860.
 *
 * @author Tilman Hausherr
 *
 */
public class UniquePropertiesValidation
{

    /**
     * Checks that certain metadata properties are unique.
     *
     * @param document the PDF Document
     * @param metadata the XMP MetaData
     * @return List of validation errors
     * @throws ValidationException
     */
    public List<ValidationResult.ValidationError> validatePropertiesUniqueness(PDDocument document, XMPMetadata metadata)
            throws ValidationException
    {
        List<ValidationResult.ValidationError> ve = new ArrayList<ValidationResult.ValidationError>();

        if (document == null)
        {
            throw new ValidationException("Document provided is null");
        }
        analyzePropertyUniqueness(metadata.getDublinCoreSchema(), DublinCoreSchema.CREATOR, ve);
        analyzePropertyUniqueness(metadata.getDublinCoreSchema(), DublinCoreSchema.TITLE, ve);
        analyzePropertyUniqueness(metadata.getDublinCoreSchema(), DublinCoreSchema.DESCRIPTION, ve);

        analyzePropertyUniqueness(metadata.getAdobePDFSchema(), AdobePDFSchema.PRODUCER, ve);
        analyzePropertyUniqueness(metadata.getAdobePDFSchema(), AdobePDFSchema.KEYWORDS, ve);

        analyzePropertyUniqueness(metadata.getXMPBasicSchema(), XMPBasicSchema.CREATORTOOL, ve);
        analyzePropertyUniqueness(metadata.getXMPBasicSchema(), XMPBasicSchema.CREATEDATE, ve);
        analyzePropertyUniqueness(metadata.getXMPBasicSchema(), XMPBasicSchema.MODIFYDATE, ve);

        // should any other properties be checked for uniqueness? Let us know.

        return ve;
    }

    private static void analyzePropertyUniqueness(XMPSchema schema, String propertyName,
            List<ValidationResult.ValidationError> ve)
    {
        if (schema == null)
        {
            return;
        }
        int count = 0;
        for (AbstractField field : schema.getAllProperties())
        {
            if (propertyName.equals(field.getPropertyName()))
            {
                ++count;
            }
        }
        if (count > 1)
        {
            ve.add(new ValidationError(PreflightConstants.ERROR_METADATA_PROPERTY_FORMAT,
                    "property '" + schema.getPrefix() + ":" + propertyName +
                    "' occurs multiple times"));
        }
    }
}

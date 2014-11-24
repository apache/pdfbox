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

package org.apache.pdfbox.preflight.metadata;

import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_METADATA_INVALID_PDFA_CONFORMANCE;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_METADATA_INVALID_PDFA_VERSION_ID;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_METADATA_PDFA_ID_MISSING;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_METADATA_WRONG_NS_PREFIX;

import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.preflight.exception.ValidationException;
import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.schema.PDFAIdentificationSchema;
import org.apache.xmpbox.schema.XMPBasicSchema;
import org.apache.xmpbox.type.StructuredType;

/**
 * Class which check if PDF/A Identification Schema contains good information
 * 
 * @author Germain Costenobel
 * 
 */
public class PDFAIdentificationValidation
{

    /**
     * Check if PDFAIdentification is valid
     * 
     * @param metadata the XMP MetaData.
     * @return the list of validation errors.
     * @throws ValidationException
     */
    public List<ValidationError> validatePDFAIdentifer(XMPMetadata metadata) throws ValidationException
    {
        List<ValidationError> ve = new ArrayList<ValidationError>();
        PDFAIdentificationSchema id = metadata.getPDFIdentificationSchema();
        if (id == null)
        {
            ve.add(new ValidationError(ERROR_METADATA_PDFA_ID_MISSING,
                    "PDF/A identification schema "
                    + PDFAIdentificationSchema.class.getAnnotation(StructuredType.class).namespace()
                    + " is missing"));
            return ve;
        }

        // According to the PDF/A specification, the prefix must be pdfaid for this schema.
        StructuredType stBasic = XMPBasicSchema.class.getAnnotation(StructuredType.class);
        StructuredType stPdfaIdent = PDFAIdentificationSchema.class.getAnnotation(StructuredType.class);
        if (!id.getPrefix().equals(stPdfaIdent.preferedPrefix()))
        {
            if (metadata.getSchema(stPdfaIdent.preferedPrefix(), stBasic.namespace()) == null)
            {
                ve.add(unexpectedPrefixFoundError(id.getPrefix(), stPdfaIdent.preferedPrefix(),
                        PDFAIdentificationSchema.class.getName()));
            }
            else
            {
                id = (PDFAIdentificationSchema) metadata.getSchema(stPdfaIdent.preferedPrefix(),
                        stPdfaIdent.namespace());
            }
        }
        checkConformanceLevel(ve, id.getConformance());
        checkPartNumber(ve, id.getPart());
        return ve;
    }

    /**
     * Return a validationError formatted when a schema has not the expected prefix
     * 
     * @param prefFound
     * @param prefExpected
     * @param schema
     * @return the validation error.
     */
    protected ValidationError unexpectedPrefixFoundError(String prefFound, String prefExpected, String schema)
    {
        StringBuilder sb = new StringBuilder(80);
        sb.append(schema).append(" found but prefix used is '").append(prefFound).append("', prefix '")
                .append(prefExpected).append("' is expected.");

        return new ValidationError(ERROR_METADATA_WRONG_NS_PREFIX, sb.toString());
    }

    protected void checkConformanceLevel(List<ValidationError> ve, String value)
    {
        if (value == null || !(value.equals("A") || value.equals("B")))
        {
            ve.add(new ValidationError(ERROR_METADATA_INVALID_PDFA_CONFORMANCE));
        }
    }

    protected void checkPartNumber(List<ValidationError> ve, int value)
    {
        if (value != 1)
        {
            ve.add(new ValidationError(ERROR_METADATA_INVALID_PDFA_VERSION_ID));
        }
    }
}

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

package org.apache.pdfbox.preflight.process;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.preflight.PreflightConstants;
import org.apache.pdfbox.preflight.PreflightContext;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.preflight.exception.ValidationException;
import org.apache.pdfbox.preflight.metadata.PDFAIdentificationValidation;
import org.apache.pdfbox.preflight.metadata.RDFAboutAttributeConcordanceValidation;
import org.apache.pdfbox.preflight.metadata.RDFAboutAttributeConcordanceValidation.DifferentRDFAboutException;
import org.apache.pdfbox.preflight.metadata.SynchronizedMetaDataValidation;
import org.apache.pdfbox.preflight.metadata.XpacketParsingException;
import org.apache.pdfbox.preflight.utils.COSUtils;
import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.xml.DomXmpParser;
import org.apache.xmpbox.xml.XmpParsingException;
import org.apache.xmpbox.xml.XmpParsingException.ErrorType;

public class MetadataValidationProcess extends AbstractProcess
{

    public void validate(PreflightContext ctx) throws ValidationException
    {
        try
        {
            PDDocument document = ctx.getDocument();

            byte[] tmp = getXpacket(document.getDocument());
            DomXmpParser builder;
            builder = new DomXmpParser();
            XMPMetadata metadata;
            metadata = builder.parse(tmp);
            ctx.setMetadata(metadata);

            // 6.7.5 no deprecated attribute in xpacket processing instruction
            if (metadata.getXpacketBytes() != null)
            {
                addValidationError(ctx, new ValidationError(PreflightConstants.ERROR_METADATA_XPACKET_DEPRECATED,
                        "bytes attribute is forbidden"));
            }
            if (metadata.getXpacketEncoding() != null)
            {
                addValidationError(ctx, new ValidationError(PreflightConstants.ERROR_METADATA_XPACKET_DEPRECATED,
                        "encoding attribute is forbidden"));
            }

            // Call metadata synchronization checking
            addValidationErrors(ctx,
                    new SynchronizedMetaDataValidation().validateMetadataSynchronization(document, metadata));

            // Call PDF/A Identifier checking
            addValidationErrors(ctx, new PDFAIdentificationValidation().validatePDFAIdentifer(metadata));

            // Call rdf:about checking
            try
            {
                new RDFAboutAttributeConcordanceValidation().validateRDFAboutAttributes(metadata);
            }
            catch (DifferentRDFAboutException e)
            {
                addValidationError(ctx, new ValidationError(
                        PreflightConstants.ERROR_METADATA_RDF_ABOUT_ATTRIBUTE_INEQUAL_VALUE, e.getMessage()));
            }

        }
        catch (XpacketParsingException e)
        {
            if (e.getError() != null)
            {
                addValidationError(ctx, e.getError());
            }
            else
            {
                addValidationError(ctx, new ValidationError(PreflightConstants.ERROR_METADATA_MAIN, "Unexpected error"));
            }
        }
        catch (XmpParsingException e)
        {
            if (e.getErrorType() == ErrorType.NoValueType)
            {
                addValidationError(ctx,
                        new ValidationError(PreflightConstants.ERROR_METADATA_UNKNOWN_VALUETYPE, e.getMessage()));
            }
            else if (e.getErrorType() == ErrorType.RequiredProperty)
            {
                addValidationError(ctx,
                        new ValidationError(PreflightConstants.ERROR_METADATA_PROPERTY_MISSING, e.getMessage()));
            }
            else if (e.getErrorType() == ErrorType.InvalidPrefix)
            {
                addValidationError(ctx, new ValidationError(
                        PreflightConstants.ERROR_METADATA_ABSENT_DESCRIPTION_SCHEMA, e.getMessage()));
            }
            else if (e.getErrorType() == ErrorType.InvalidType)
            {
                addValidationError(ctx,
                        new ValidationError(PreflightConstants.ERROR_METADATA_PROPERTY_UNKNOWN, e.getMessage()));
            }
            else if (e.getErrorType() == ErrorType.XpacketBadEnd)
            {
                throw new ValidationException("Unable to parse font metadata due to : " + e.getMessage(), e);
            }
            else if (e.getErrorType() == ErrorType.NoSchema)
            {
                addValidationError(ctx, new ValidationError(
                        PreflightConstants.ERROR_METADATA_ABSENT_DESCRIPTION_SCHEMA, e.getMessage()));
            }
            else if (e.getErrorType() == ErrorType.InvalidPdfaSchema)
            {
                addValidationError(ctx,
                        new ValidationError(PreflightConstants.ERROR_METADATA_WRONG_NS_URI, e.getMessage()));
            }
            else
            {
                addValidationError(ctx, new ValidationError(PreflightConstants.ERROR_METADATA_FORMAT, e.getMessage()));
            }
        }
        catch (IOException e)
        {
            throw new ValidationException("Failed while validating", e);
        }
    }

    /**
     * Return the xpacket from the dictionary's stream
     */
    public static byte[] getXpacket(COSDocument cdocument) throws IOException, XpacketParsingException
    {
        COSObject catalog = cdocument.getCatalog();
        COSBase cb = catalog.getDictionaryObject(COSName.METADATA);
        if (cb == null)
        {
            // missing Metadata Key in catalog
            ValidationError error = new ValidationError(PreflightConstants.ERROR_METADATA_FORMAT,
                    "Missing Metadata Key in catalog");
            throw new XpacketParsingException("Failed while retrieving xpacket", error);
        }
        // no filter key
        COSDictionary metadataDictionnary = COSUtils.getAsDictionary(cb, cdocument);
        if (metadataDictionnary.getItem(COSName.FILTER) != null)
        {
            // should not be defined
            ValidationError error = new ValidationError(PreflightConstants.ERROR_SYNTAX_STREAM_INVALID_FILTER,
                    "Filter specified in metadata dictionnary");
            throw new XpacketParsingException("Failed while retrieving xpacket", error);
        }

        PDStream stream = PDStream.createFromCOS(metadataDictionnary);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        InputStream is = stream.createInputStream();
        IOUtils.copy(is, bos);
        is.close();
        bos.close();
        return bos.toByteArray();
    }

    /**
     * Check if metadata dictionary has no stream filter
     * 
     * @param doc
     * @return
     */
    protected List<ValidationError> checkStreamFilterUsage(PDDocument doc)
    {
        List<ValidationError> ve = new ArrayList<ValidationError>();
        List<?> filters = doc.getDocumentCatalog().getMetadata().getFilters();
        if (filters != null && !filters.isEmpty())
        {
            ve.add(new ValidationError(PreflightConstants.ERROR_METADATA_MAIN,
                    "Using stream filter on metadata dictionary is forbidden"));
        }
        return ve;
    }
}

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

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
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
import org.apache.pdfbox.util.Hex;
import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.schema.XMPBasicSchema;
import org.apache.xmpbox.type.BadFieldValueException;
import org.apache.xmpbox.type.ThumbnailType;
import org.apache.xmpbox.xml.DomXmpParser;
import org.apache.xmpbox.xml.XmpParsingException;
import org.apache.xmpbox.xml.XmpParsingException.ErrorType;

public class MetadataValidationProcess extends AbstractProcess
{

    @Override
    public void validate(PreflightContext ctx) throws ValidationException
    {
        try
        {
            PDDocument document = ctx.getDocument();

            InputStream is = getXpacket(document);
            DomXmpParser builder = new DomXmpParser();
            XMPMetadata metadata = builder.parse(is);
            is.close();
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
            
            checkThumbnails(ctx, metadata);

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
                        PreflightConstants.ERROR_METADATA_RDF_ABOUT_ATTRIBUTE_INEQUAL_VALUE, e.getMessage(), e));
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
                addValidationError(ctx, new ValidationError(PreflightConstants.ERROR_METADATA_MAIN, "Unexpected error", e));
            }
        }
        catch (XmpParsingException e)
        {
            if (e.getErrorType() == ErrorType.NoValueType)
            {
                addValidationError(ctx,
                        new ValidationError(PreflightConstants.ERROR_METADATA_UNKNOWN_VALUETYPE, e.getMessage(), e));
            }
            else if (e.getErrorType() == ErrorType.RequiredProperty)
            {
                addValidationError(ctx,
                        new ValidationError(PreflightConstants.ERROR_METADATA_PROPERTY_MISSING, e.getMessage(), e));
            }
            else if (e.getErrorType() == ErrorType.InvalidPrefix)
            {
                addValidationError(ctx, new ValidationError(
                        PreflightConstants.ERROR_METADATA_ABSENT_DESCRIPTION_SCHEMA, e.getMessage(), e));
            }
            else if (e.getErrorType() == ErrorType.InvalidType)
            {
                addValidationError(ctx,
                        new ValidationError(PreflightConstants.ERROR_METADATA_PROPERTY_UNKNOWN, e.getMessage(), e));
            }
            else if (e.getErrorType() == ErrorType.XpacketBadEnd)
            {
                throw new ValidationException("Unable to parse font metadata due to : " + e.getMessage(), e);
            }
            else if (e.getErrorType() == ErrorType.NoSchema)
            {
                addValidationError(ctx, new ValidationError(
                        PreflightConstants.ERROR_METADATA_ABSENT_DESCRIPTION_SCHEMA, e.getMessage(), e));
            }
            else if (e.getErrorType() == ErrorType.InvalidPdfaSchema)
            {
                addValidationError(ctx,
                        new ValidationError(PreflightConstants.ERROR_METADATA_WRONG_NS_URI, e.getMessage(), e));
            }
            else
            {
                addValidationError(ctx, new ValidationError(PreflightConstants.ERROR_METADATA_FORMAT, e.getMessage(), e));
            }
        }
        catch (IOException e)
        {
            throw new ValidationException("Failed while validating", e);
        }
    }

    // check thumbnails. See Bavaria Testsuite file PDFA_Conference_2009_nc.pdf for an example.
    private void checkThumbnails(PreflightContext ctx, XMPMetadata metadata)
    {
        XMPBasicSchema xmp = metadata.getXMPBasicSchema();
        if (xmp == null)
        {
            return;
        }
        List<ThumbnailType> tbProp;
        try
        {
            tbProp = xmp.getThumbnailsProperty();
        }
        catch (BadFieldValueException e)
        {
            // should not happen here because it would have happened in XmpParser already
            addValidationError(ctx, new ValidationError(PreflightConstants.ERROR_METADATA_FORMAT, e.getMessage(), e));
            return;
        }
        if (tbProp == null)
        {
            return;
        }
        for (ThumbnailType tb : tbProp)
        {
            checkThumbnail(tb, ctx);
        }
    }

    private void checkThumbnail(ThumbnailType tb, PreflightContext ctx)
    {
        byte[] binImage;
        try
        {
            binImage = Hex.decodeBase64(tb.getImage());
        }
        catch (IllegalArgumentException e)
        {
            addValidationError(ctx, new ValidationError(PreflightConstants.ERROR_METADATA_FORMAT,
                    "xapGImg:image is not correct base64 encoding"));
            return;
        }
        if (!hasJpegMagicNumber(binImage))
        {
            addValidationError(ctx, new ValidationError(PreflightConstants.ERROR_METADATA_FORMAT,
                    "xapGImg:image decoded base64 content is not in JPEG format"));
            return;
        }
        BufferedImage bim;
        try
        {
            bim = ImageIO.read(new ByteArrayInputStream(binImage));
        }
        catch (IOException e)
        {
            addValidationError(ctx, new ValidationError(PreflightConstants.ERROR_METADATA_FORMAT, e.getMessage(), e));
            return;
        }
        if (!"JPEG".equals(tb.getFormat()))
        {
            addValidationError(ctx, new ValidationError(PreflightConstants.ERROR_METADATA_FORMAT,
                    "xapGImg:format must be 'JPEG'"));
        }
        if (bim.getHeight() != tb.getHeight())
        {
            addValidationError(ctx, new ValidationError(PreflightConstants.ERROR_METADATA_FORMAT,
                    "xapGImg:height does not match the actual base64-encoded thumbnail image data"));
        }
        if (bim.getWidth() != tb.getWidth())
        {
            addValidationError(ctx, new ValidationError(PreflightConstants.ERROR_METADATA_FORMAT,
                    "xapGImg:witdh does not match the actual base64-encoded thumbnail image data"));
        }
    }
    
    private boolean hasJpegMagicNumber(byte[] binImage)
    {
        if (binImage.length < 4)
        {
            return false;
        }
        return (binImage[0] == (byte) 0xFF
                && binImage[1] == (byte) 0xD8
                && binImage[binImage.length - 2] == (byte) 0xFF
                && binImage[binImage.length - 1] == (byte) 0xD9);
    }

    /**
     * Return the xpacket from the dictionary's stream
     */
    private static InputStream getXpacket(PDDocument document)
            throws IOException, XpacketParsingException
    {
        PDDocumentCatalog catalog = document.getDocumentCatalog();
        PDMetadata metadata = catalog.getMetadata();
        if (metadata == null)
        {
            COSBase metaObject = catalog.getCOSObject().getDictionaryObject(COSName.METADATA);
            if (!(metaObject instanceof COSStream))
            {
                // the Metadata object isn't a stream
                ValidationError error = new ValidationError(
                        PreflightConstants.ERROR_METADATA_FORMAT, "Metadata is not a stream");
                throw new XpacketParsingException("Failed while retrieving xpacket", error);
            }
            // missing Metadata Key in catalog
            ValidationError error = new ValidationError(PreflightConstants.ERROR_METADATA_FORMAT,
                    "Missing Metadata Key in catalog");
            throw new XpacketParsingException("Failed while retrieving xpacket", error);
        }

        // no filter key
        if (metadata.getFilters() != null)
        {
            // should not be defined
            ValidationError error = new ValidationError(
                    PreflightConstants.ERROR_SYNTAX_STREAM_INVALID_FILTER,
                    "Filter specified in metadata dictionnary");
            throw new XpacketParsingException("Failed while retrieving xpacket", error);
        }

        return metadata.exportXMPMetadata();
    }

    /**
     * Check if metadata dictionary has no stream filter
     * 
     * @param doc the document to check.
     * @return the list of validation errors.
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

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.preflight.PreflightConstants;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.preflight.exception.ValidationException;
import org.apache.xmpbox.DateConverter;
import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.schema.AdobePDFSchema;
import org.apache.xmpbox.schema.DublinCoreSchema;
import org.apache.xmpbox.schema.XMPBasicSchema;
import org.apache.xmpbox.type.AbstractField;
import org.apache.xmpbox.type.TextType;

/**
 * Class which check if document information available in a document are synchronized with XMP
 * 
 * @author Germain Costenobel
 * 
 */
public class SynchronizedMetaDataValidation
{

    /**
     * Analyze if Title embedded in Document Information dictionary and in XMP properties are synchronized
     * 
     * @param dico
     *            Document Information Dictionary
     * @param dc
     *            Dublin Core Schema
     * @param ve
     *            The list of validation errors
     */
    protected void analyzeTitleProperty(PDDocumentInformation dico, DublinCoreSchema dc, List<ValidationError> ve)
    {
        String title = dico.getTitle();
        if (title != null)
        {
            if (dc != null)
            {
                // Check the x-default value, if not found, check with the first value
                // found
                if (dc.getTitle() != null)
                {
                    if (dc.getTitle("x-default") != null)
                    {
                        if (!dc.getTitle("x-default").equals(title))
                        {
                            ve.add(unsynchronizedMetaDataError("Title"));
                        }
                    }
                    else
                    {
                        // This search of first value is made just to keep compatibility
                        // with lot of PDF documents
                        // which use title without lang definition
                        // REM : MAY we have to delete this option in the future
                        Iterator<AbstractField> it = dc.getTitleProperty().getContainer().getAllProperties().iterator();
                        if (it.hasNext())
                        {
                            AbstractField tmp = it.next();
                            if (tmp instanceof TextType)
                            {
                                if (!((TextType) tmp).getStringValue().equals(title))
                                {
                                    ve.add(unsynchronizedMetaDataError("Title"));
                                }
                            }
                            else
                            {
                                ve.add(AbsentXMPPropertyError("Title", "Property is badly defined"));
                            }
                        }
                        else
                        {
                            ve.add(AbsentXMPPropertyError("Title", "Property is not defined"));
                        }
                    }

                }
                else
                {
                    ve.add(AbsentXMPPropertyError("Title", "Property is not defined"));
                }
            }
            else
            {
                ve.add(AbsentSchemaMetaDataError("Title", "Dublin Core"));
            }
        }
    }

    /**
     * Analyze if Author(s) embedded in Document Information dictionary and in XMP properties are synchronized
     * 
     * @param dico
     *            Document Information Dictionary
     * @param dc
     *            Dublin Core Schema
     * @param ve
     *            The list of validation errors
     */
    protected void analyzeAuthorProperty(PDDocumentInformation dico, DublinCoreSchema dc, List<ValidationError> ve)
    {
        String author = dico.getAuthor();
        if (author != null)
        {
            if (dc != null)
            {
                if (dc.getCreatorsProperty() != null)
                {
                    if (dc.getCreators().size() != 1)
                    {
                        ve.add(AbsentXMPPropertyError("Author",
                                "In XMP metadata, Author(s) must be represented by a single entry in a text array (dc:creator) "));
                    }
                    else
                    {
                        if (dc.getCreators().get(0) == null)
                        {
                            ve.add(AbsentXMPPropertyError("Author", "Property is defined as null"));
                        }
                        else
                        {
                            if (!dc.getCreators().get(0).equals(author))
                            {
                                ve.add(unsynchronizedMetaDataError("Author"));
                            }
                        }
                    }
                }
                else
                {
                    ve.add(AbsentXMPPropertyError("Author", "Property is not defined in XMP Metadata"));
                }
            }
            else
            {
                ve.add(AbsentSchemaMetaDataError("Author", "Dublin Core"));
            }
        }
    }

    /**
     * Analyze if Subject(s) embedded in Document Information dictionary and in XMP properties are synchronized
     * 
     * @param dico
     *            Document Information Dictionary
     * @param dc
     *            Dublin Core Schema
     * @param ve
     *            The list of validation errors
     */
    protected void analyzeSubjectProperty(PDDocumentInformation dico, DublinCoreSchema dc, List<ValidationError> ve)
    {
        String subject = dico.getSubject();
        if (subject != null)
        {
            if (dc != null)
            {
                // PDF/A Conformance Erratum (2007) specifies XMP Subject
                // as a Text type embedded in the dc:description["x-default"].
                if (dc.getDescriptionProperty() != null)
                {
                    if (dc.getDescription("x-default") == null)
                    {
                        ve.add(AbsentXMPPropertyError("Subject",
                                "Subject not found in XMP (dc:description[\"x-default\"] not found)"));
                    }
                    else
                    {
                        if (!dc.getDescription("x-default").equals(subject))
                        {
                            ve.add(unsynchronizedMetaDataError("Subject"));

                        }
                    }
                }
                else
                {
                    ve.add(AbsentXMPPropertyError("Subject", "Property is defined as null"));
                }
            }
            else
            {
                ve.add(AbsentSchemaMetaDataError("Subject", "Dublin Core"));
            }
        }
    }

    /**
     * Analyze if Keyword(s) embedded in Document Information dictionary and in XMP properties are synchronized
     * 
     * @param dico
     *            Document Information Dictionary
     * @param pdf
     *            PDF Schema
     * @param ve
     *            The list of validation errors
     */
    protected void analyzeKeywordsProperty(PDDocumentInformation dico, AdobePDFSchema pdf, List<ValidationError> ve)
    {
        String keyword = dico.getKeywords();
        if (keyword != null)
        {
            if (pdf != null)
            {
                if (pdf.getKeywordsProperty() == null)
                {
                    ve.add(AbsentXMPPropertyError("Keywords", "Property is not defined"));
                }
                else
                {
                    if (!pdf.getKeywords().equals(keyword))
                    {
                        ve.add(unsynchronizedMetaDataError("Keywords"));
                    }
                }
            }
            else
            {
                ve.add(AbsentSchemaMetaDataError("Keywords", "PDF"));
            }
        }
    }

    /**
     * Analyze if Producer embedded in Document Information dictionary and in XMP properties are synchronized
     * 
     * @param dico
     *            Document Information Dictionary
     * @param pdf
     *            PDF Schema
     * @param ve
     *            The list of validation errors
     */
    protected void analyzeProducerProperty(PDDocumentInformation dico, AdobePDFSchema pdf, List<ValidationError> ve)
    {
        String producer = dico.getProducer();
        if (producer != null)
        {
            if (pdf != null)
            {
                if (pdf.getProducerProperty() == null)
                {
                    ve.add(AbsentXMPPropertyError("Producer", "Property is not defined"));
                }
                else
                {
                    if (!pdf.getProducer().equals(producer))
                    {
                        ve.add(unsynchronizedMetaDataError("Producer"));
                    }
                }
            }
            else
            {
                ve.add(AbsentSchemaMetaDataError("Producer", "PDF"));
            }
        }

    }

    /**
     * Analyze if the creator tool embedded in Document Information dictionary and in XMP properties are synchronized
     * 
     * @param dico
     *            Document Information Dictionary
     * @param xmp
     *            XMP Basic Schema
     * @param ve
     *            The list of validation errors
     * 
     */
    protected void analyzeCreatorToolProperty(PDDocumentInformation dico, XMPBasicSchema xmp, List<ValidationError> ve)
    {
        String creatorTool = dico.getCreator();
        if (creatorTool != null)
        {
            if (xmp != null)
            {
                if (xmp.getCreatorToolProperty() == null)
                {
                    ve.add(AbsentXMPPropertyError("CreatorTool", "Property is not defined"));
                }
                else
                {
                    if (!xmp.getCreatorTool().equals(creatorTool))
                    {
                        ve.add(unsynchronizedMetaDataError("CreatorTool"));
                    }
                }
            }
            else
            {
                ve.add(AbsentSchemaMetaDataError("CreatorTool", "PDF"));
            }
        }

    }

    /**
     * Analyze if the CreationDate embedded in Document Information dictionary and in XMP properties are synchronized
     * 
     * @param dico
     *            Document Information Dictionary
     * @param xmp
     *            XMP Basic Schema
     * @param ve
     *            The list of validation errors
     * @throws ValidationException
     */
    protected void analyzeCreationDateProperty(PDDocumentInformation dico, XMPBasicSchema xmp, List<ValidationError> ve)
            throws ValidationException
    {
        Calendar creationDate = null;
        try
        {
            creationDate = dico.getCreationDate();
        }
        catch (IOException e)
        {
            // If there is an error while converting this property to a date
            ve.add(new ValidationError(PreflightConstants.ERROR_METADATA_DICT_INFO_CORRUPT, "Document Information 'CreationDate' can't be read : " + e.getMessage()));
        }
        if (creationDate != null)
        {
            if (xmp != null)
            {
                Calendar xmpCreationDate = xmp.getCreateDate();

                if (xmpCreationDate == null)
                {
                    ve.add(AbsentXMPPropertyError("CreationDate", "Property is not defined"));
                }
                else
                {
                    if (!DateConverter.toISO8601(xmpCreationDate).equals(DateConverter.toISO8601(creationDate)))
                    {
                        ve.add(unsynchronizedMetaDataError("CreationDate"));
                    }
                }

            }
            else
            {
                ve.add(AbsentSchemaMetaDataError("CreationDate", "Basic XMP"));
            }
        }
    }

    /**
     * Analyze if the ModifyDate embedded in Document Information dictionary and in XMP properties are synchronized
     * 
     * @param dico
     *            Document Information Dictionary
     * @param xmp
     *            XMP Basic Schema
     * @param ve
     *            The list of validation errors
     * @throws ValidationException
     */
    protected void analyzeModifyDateProperty(PDDocumentInformation dico, XMPBasicSchema xmp, List<ValidationError> ve)
            throws ValidationException
    {
        Calendar modifyDate;
        try
        {
            modifyDate = dico.getModificationDate();
            if (modifyDate != null)
            {
                if (xmp != null)
                {

                    Calendar xmpModifyDate = xmp.getModifyDate();
                    if (xmpModifyDate == null)
                    {
                        ve.add(AbsentXMPPropertyError("ModifyDate", "Property is not defined"));
                    }
                    else
                    {
                        if (!DateConverter.toISO8601(xmpModifyDate).equals(DateConverter.toISO8601(modifyDate)))
                        {

                            ve.add(unsynchronizedMetaDataError("ModificationDate"));
                        }
                    }

                }
                else
                {
                    ve.add(AbsentSchemaMetaDataError("ModifyDate", "Basic XMP"));
                }
            }
        }
        catch (IOException e)
        {
            // If there is an error while converting this property to a date
            ve.add(new ValidationError(PreflightConstants.ERROR_METADATA_DICT_INFO_CORRUPT, "Document Information 'ModifyDate' can't be read : " + e.getMessage()));
        }

    }

    /**
     * Check if document information entries and XMP information are synchronized
     * 
     * @param document
     *            the PDF Document
     * @param metadata
     *            the XMP MetaData
     * @return List of validation errors
     * @throws ValidationException
     */
    public List<ValidationError> validateMetadataSynchronization(PDDocument document, XMPMetadata metadata)
            throws ValidationException
    {
        List<ValidationError> ve = new ArrayList<ValidationError>();

        if (document == null)
        {
            throw new ValidationException("Document provided is null");
        }
        else
        {
            PDDocumentInformation dico = document.getDocumentInformation();
            if (metadata == null)
            {
                throw new ValidationException("Metadata provided are null");
            }
            else
            {
                DublinCoreSchema dc = metadata.getDublinCoreSchema();

                // TITLE
                analyzeTitleProperty(dico, dc, ve);
                // AUTHOR
                analyzeAuthorProperty(dico, dc, ve);
                // SUBJECT
                analyzeSubjectProperty(dico, dc, ve);

                AdobePDFSchema pdf = metadata.getAdobePDFSchema();

                // KEYWORDS
                analyzeKeywordsProperty(dico, pdf, ve);
                // PRODUCER
                analyzeProducerProperty(dico, pdf, ve);

                XMPBasicSchema xmp = metadata.getXMPBasicSchema();

                // CREATOR TOOL
                analyzeCreatorToolProperty(dico, xmp, ve);

                // CREATION DATE
                analyzeCreationDateProperty(dico, xmp, ve);

                // MODIFY DATE
                analyzeModifyDateProperty(dico, xmp, ve);

            }

        }
        return ve;
    }

    /**
     * Return a validationError formatted when a schema has not the expected prefix
     * 
     * @param prefFound
     * @param prefExpected
     * @param schema
     * @return
     */
    protected ValidationError unexpectedPrefixFoundError(String prefFound, String prefExpected, String schema)
    {
        StringBuilder sb = new StringBuilder(80);
        sb.append(schema).append(" found but prefix used is '").append(prefFound).append("', prefix '")
                .append(prefExpected).append("' is expected.");

        return new ValidationError(PreflightConstants.ERROR_METADATA_WRONG_NS_PREFIX, sb.toString());
    }

    /**
     * Return an exception formatted on IOException when accessing on metadata schema
     * 
     * @param target
     *            the name of the schema
     * @param cause
     *            the raised IOException
     * @return the generated exception
     */
    protected ValidationException SchemaAccessException(String target, Throwable cause)
    {
        StringBuilder sb = new StringBuilder(80);
        sb.append("Cannot access to the ").append(target).append(" schema");
        return new ValidationException(sb.toString(), cause);
    }

    /**
     * Return a formatted validation error when metadata are not synchronized
     * 
     * @param target
     *            the concerned property
     * @return the generated validation error
     */
    protected ValidationError unsynchronizedMetaDataError(String target)
    {
        StringBuilder sb = new StringBuilder(80);
        sb.append(target).append(" present in the document catalog dictionary doesn't match with XMP information");
        return new ValidationError(PreflightConstants.ERROR_METADATA_MISMATCH, sb.toString());
    }

    /**
     * Return a formatted validation error when a specific metadata schema can't be found
     * 
     * @param target
     *            the concerned property
     * @param schema
     *            the XMP schema which can't be found
     * @return the generated validation error
     */
    protected ValidationError AbsentSchemaMetaDataError(String target, String schema)
    {
        StringBuilder sb = new StringBuilder(80);
        sb.append(target).append(" present in the document catalog dictionary can't be found in XMP information (")
                .append(schema).append(" schema not declared)");
        return new ValidationError(PreflightConstants.ERROR_METADATA_MISMATCH, sb.toString());
    }

    /**
     * Return a formatted validation error when a specific XMP property can't be found
     * 
     * @param target
     *            the concerned property
     * @param details
     *            comments about the XMP property
     * @return the generated validation error
     */
    protected ValidationError AbsentXMPPropertyError(String target, String details)
    {
        StringBuilder sb = new StringBuilder(80);
        sb.append(target).append(" present in the document catalog dictionary can't be found in XMP information (")
                .append(details).append(")");
        return new ValidationError(PreflightConstants.ERROR_METADATA_MISMATCH, sb.toString());
    }
}

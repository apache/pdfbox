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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSString;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.preflight.PreflightConstants;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.preflight.exception.ValidationException;
import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.schema.AdobePDFSchema;
import org.apache.xmpbox.schema.DublinCoreSchema;
import org.apache.xmpbox.schema.XMPBasicSchema;
import org.apache.xmpbox.type.AbstractField;
import org.apache.xmpbox.type.BadFieldValueException;
import org.apache.xmpbox.type.TextType;

/**
 * Class which checks if document information available in a document is synchronized with XMP
 * 
 * @author Germain Costenobel
 * 
 */
public class SynchronizedMetaDataValidation
{

    /**
     * Analyze if Title embedded in Document Information dictionary and in XMP properties are synchronized
     * 
     * @param dico the Document Information Dictionary.
     * @param dc the Dublin Core Schema.
     * @param ve the list of validation errors.
     */
    protected void analyzeTitleProperty(PDDocumentInformation dico, DublinCoreSchema dc, List<ValidationError> ve)
    {
        String title = dico.getTitle();
        if (title != null)
        {
            // automatically strip trailing Nul values
            title = removeTrailingNul(title);
            if (dc != null)
            {
                try
                {
                    // Check the x-default value, if not found, check with the first value found
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
                                    ve.add(absentXMPPropertyError("Title", "Property is badly defined"));
                                }
                            }
                            else
                            {
                                ve.add(absentXMPPropertyError("Title", "Property is not defined"));
                            }
                        }
                    }
                    else
                    {
                        ve.add(absentXMPPropertyError("Title", "Property is not defined"));
                    }
                }
                catch (BadFieldValueException ex)
                {
                    ve.add(badFieldXMPPropertyError("Title", ex.getMessage()));
                }
            }
            else
            {
                ve.add(absentSchemaMetaDataError("Title", "Dublin Core"));
            }
        }
    }

    /**
     * Analyze if Author(s) embedded in Document Information dictionary and in XMP properties are
     * synchronized
     *
     * @param dico Document Information Dictionary
     * @param dc Dublin Core Schema
     * @param ve The list of validation errors
     */
    protected void analyzeAuthorProperty(PDDocumentInformation dico, DublinCoreSchema dc, List<ValidationError> ve)
    {
        String author = dico.getAuthor();
        if (author != null)
        {
            // automatically strip trailing Nul values
            author = removeTrailingNul(author);
            if (dc != null)
            {
                if (dc.getCreatorsProperty() != null)
                {
                    if (dc.getCreators().size() != 1)
                    {
                        ve.add(absentXMPPropertyError("Author",
                                "In XMP metadata, Author(s) must be represented by a single entry in a text array (dc:creator) "));
                    }
                    else
                    {
                        if (dc.getCreators().get(0) == null)
                        {
                            ve.add(absentXMPPropertyError("Author", "Property is defined as null"));
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
                    ve.add(absentXMPPropertyError("Author", "Property is not defined in XMP Metadata"));
                }
            }
            else
            {
                ve.add(absentSchemaMetaDataError("Author", "Dublin Core"));
            }
        }
    }

    /**
     * Analyze if Subject(s) embedded in Document Information dictionary and in XMP properties are
     * synchronized
     *
     * @param dico Document Information Dictionary
     * @param dc Dublin Core Schema
     * @param ve The list of validation errors
     */
    protected void analyzeSubjectProperty(PDDocumentInformation dico, DublinCoreSchema dc, List<ValidationError> ve)
    {
        String subject = dico.getSubject();
        if (subject != null)
        {
            // automatically strip trailing Nul values
            subject = removeTrailingNul(subject);
            if (dc != null)
            {
                // PDF/A Conformance Erratum (2007) specifies XMP Subject
                // as a Text type embedded in the dc:description["x-default"].
                if (dc.getDescriptionProperty() != null)
                {
                    try
                    {
                        if (dc.getDescription("x-default") == null)
                        {
                            ve.add(absentXMPPropertyError("Subject",
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
                    catch (BadFieldValueException ex)
                    {
                        ve.add(badFieldXMPPropertyError("Subject", ex.getMessage()));
                    }
                }
                else
                {
                    ve.add(absentXMPPropertyError("Subject", "Property is defined as null"));
                }
            }
            else
            {
                ve.add(absentSchemaMetaDataError("Subject", "Dublin Core"));
            }
        }
    }

    /**
     * Analyze if Keyword(s) embedded in Document Information dictionary and in XMP properties are
     * synchronized
     *
     * @param dico Document Information Dictionary
     * @param pdf PDF Schema
     * @param ve The list of validation errors
     */
    protected void analyzeKeywordsProperty(PDDocumentInformation dico, AdobePDFSchema pdf, List<ValidationError> ve)
    {
        String keyword = dico.getKeywords();
        if (keyword != null)
        {
            // automatically strip trailing Nul values
            keyword = removeTrailingNul(keyword);
            if (pdf != null)
            {
                if (pdf.getKeywordsProperty() == null)
                {
                    ve.add(absentXMPPropertyError("Keywords", "Property is not defined"));
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
                ve.add(absentSchemaMetaDataError("Keywords", "PDF"));
            }
        }
    }

    /**
     * Analyze if Producer embedded in Document Information dictionary and in XMP properties are
     * synchronized
     *
     * @param dico Document Information Dictionary
     * @param pdf PDF Schema
     * @param ve The list of validation errors
     */
    protected void analyzeProducerProperty(PDDocumentInformation dico, AdobePDFSchema pdf, List<ValidationError> ve)
    {
        String producer = dico.getProducer();
        if (producer != null)
        {
            // automatically strip trailing Nul values
            producer = removeTrailingNul(producer);
            if (pdf != null)
            {
                if (pdf.getProducerProperty() == null)
                {
                    ve.add(absentXMPPropertyError("Producer", "Property is not defined"));
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
                ve.add(absentSchemaMetaDataError("Producer", "PDF"));
            }
        }

    }

    /**
     * Analyze if the creator tool embedded in Document Information dictionary and in XMP properties
     * are synchronized
     *
     * @param dico Document Information Dictionary
     * @param xmp XMP Basic Schema
     * @param ve The list of validation errors
     *
     */
    protected void analyzeCreatorToolProperty(PDDocumentInformation dico, XMPBasicSchema xmp, List<ValidationError> ve)
    {
        String creatorTool = dico.getCreator();
        if (creatorTool != null)
        {
            // automatically strip trailing Nul values
            creatorTool = removeTrailingNul(creatorTool);
            if (xmp != null)
            {
                if (xmp.getCreatorToolProperty() == null)
                {
                    ve.add(absentXMPPropertyError("CreatorTool", "Property is not defined"));
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
                ve.add(absentSchemaMetaDataError("CreatorTool", "PDF"));
            }
        }
    }

    /**
     * Analyze if the CreationDate embedded in Document Information dictionary and in XMP properties
     * are synchronized
     *
     * @param dico Document Information Dictionary
     * @param xmp XMP Basic Schema
     * @param ve The list of validation errors
     * @throws ValidationException
     */
    protected void analyzeCreationDateProperty(PDDocumentInformation dico, XMPBasicSchema xmp, List<ValidationError> ve)
            throws ValidationException
    {
        Calendar creationDate = dico.getCreationDate();
        COSBase item = dico.getCOSObject().getItem(COSName.CREATION_DATE);
        if (creationDate != null && isValidPDFDateFormat(item))
        {
            if (xmp != null)
            {
                Calendar xmpCreationDate = xmp.getCreateDate();

                if (xmpCreationDate == null)
                {
                    ve.add(absentXMPPropertyError("CreationDate", "Property is not defined"));
                }
                else
                {
                    if (xmpCreationDate.compareTo(creationDate) != 0)
                    {
                        ve.add(unsynchronizedMetaDataError("CreationDate"));
                    }
                    else if (hasTimeZone(xmp.getCreateDateProperty().getRawValue()) != 
                            hasTimeZone(dico.getPropertyStringValue("CreationDate")))
                    {
                        ve.add(unsynchronizedMetaDataError("CreationDate"));
                    }
                }
            }
            else
            {
                ve.add(absentSchemaMetaDataError("CreationDate", "Basic XMP"));
            }
        }
    }

    /**
     * Analyze if the ModifyDate embedded in Document Information dictionary and in XMP properties
     * are synchronized
     *
     * @param dico Document Information Dictionary
     * @param xmp XMP Basic Schema
     * @param ve The list of validation errors
     * @throws ValidationException
     */
    protected void analyzeModifyDateProperty(PDDocumentInformation dico, XMPBasicSchema xmp, List<ValidationError> ve)
            throws ValidationException
    {
        Calendar modifyDate = dico.getModificationDate();
        COSBase item = dico.getCOSObject().getItem(COSName.MOD_DATE);        
        if (modifyDate != null && isValidPDFDateFormat(item))
        {
            if (xmp != null)
            {
                Calendar xmpModifyDate = xmp.getModifyDate();
                if (xmpModifyDate == null)
                {
                    ve.add(absentXMPPropertyError("ModifyDate", "Property is not defined"));
                }
                else
                {
                    if (xmpModifyDate.compareTo(modifyDate) != 0)
                    {
                        ve.add(unsynchronizedMetaDataError("ModificationDate"));
                    }
                    else if (hasTimeZone(xmp.getModifyDateProperty().getRawValue())
                            != hasTimeZone(dico.getPropertyStringValue("ModDate")))
                    {
                        ve.add(unsynchronizedMetaDataError("ModificationDate"));
                    }
                }
            }
            else
            {
                ve.add(absentSchemaMetaDataError("ModifyDate", "Basic XMP"));
            }
        }
    }

    /**
     * Check if document information entries and XMP information are synchronized
     *
     * @param document the PDF Document
     * @param metadata the XMP MetaData
     * @return List of validation errors
     * @throws ValidationException
     */
    public List<ValidationError> validateMetadataSynchronization(PDDocument document, XMPMetadata metadata)
            throws ValidationException
    {
        List<ValidationError> ve = new ArrayList<>();

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
     * @return the generated validation error.
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
     * @param target the name of the schema
     * @param cause the raised IOException
     * @return the generated exception
     */
    protected ValidationException schemaAccessException(String target, Throwable cause)
    {
        StringBuilder sb = new StringBuilder(80);
        sb.append("Cannot access to the ").append(target).append(" schema");
        return new ValidationException(sb.toString(), cause);
    }

    /**
     * Return a formatted validation error when metadata are not synchronized
     *
     * @param target the concerned property
     * @return the generated validation error
     */
    protected ValidationError unsynchronizedMetaDataError(String target)
    {
        return new ValidationError(PreflightConstants.ERROR_METADATA_MISMATCH, target +
                " present in the document catalog dictionary doesn't match with XMP information");
    }

    /**
     * Return a formatted validation error when a specific metadata schema can't be found
     *
     * @param target the concerned property
     * @param schema the XMP schema which can't be found
     * @return the generated validation error
     */
    protected ValidationError absentSchemaMetaDataError(String target, String schema)
    {
        StringBuilder sb = new StringBuilder(80);
        sb.append(target).append(" present in the document catalog dictionary can't be found in XMP information (")
                .append(schema).append(" schema not declared)");
        return new ValidationError(PreflightConstants.ERROR_METADATA_MISMATCH, sb.toString());
    }

    /**
     * Return a formatted validation error when a specific XMP property can't be found
     *
     * @param target the concerned property
     * @param details comments about the XMP property
     * @return the generated validation error
     */
    protected ValidationError absentXMPPropertyError(String target, String details)
    {
        StringBuilder sb = new StringBuilder(80);
        sb.append(target).append(" present in the document catalog dictionary can't be found in XMP information (")
                .append(details).append(")");
        return new ValidationError(PreflightConstants.ERROR_METADATA_MISMATCH, sb.toString());
    }
    
    /**
     * Return a formatted validation error when a specific XMP property has the wrong type.
     *
     * @param target the concerned property
     * @param details comments about the XMP property
     * @return the generated validation error
     */
    protected ValidationError badFieldXMPPropertyError(String target, String details)
    {
        StringBuilder sb = new StringBuilder(80);
        sb.append(target).append(" property is not a multi-lingual property in XMP information(")
                .append(details).append(")");
        return new ValidationError(PreflightConstants.ERROR_METADATA_MISMATCH, sb.toString());
    }
    
    /**
     * A given string from the DocumentInformation dictionary may have some trailing Nul values
     * which have to be stripped.
     *
     * @param string to be stripped
     * @return the stripped string
     */
    private String removeTrailingNul(String string)
    {
        // remove trailing NUL values
        int length = string.length();
        while (length > 0 && string.charAt(length - 1) == 0)
        {
            length--;
        }
        return string.substring(0, length);
    }
    
    /**
     * Verify if the date string has time zone information.
     * <p>
     * <strong>This method doesn't do a complete parsing as this is a helper AFTER a date has proven
     * to be valid
     * </strong>
     * </p>
     *
     * @param date
     * @return the validation result
     */
    private boolean hasTimeZone(Object date)
    {
        final String datePattern = "^D:.*[Z]$|^D:.*[+-].*|^\\d{4}.*T.*Z(\\d{2}:\\d{2}){0,1}$|^\\d{4}.*T.*[+-]\\d{2}.*$";
        if (date instanceof Calendar)
        {
            // A Java Calendar object always has a time zone information
            return true;
        }
        else if (date instanceof String)
        {
            return Pattern.matches(datePattern, (String) date);
        }
        return false;
    }

    /**
     * Verifies that a date item is a COSString and has the format "D:YYYYMMDDHHmmSSOHH'mm'", where
     * D:YYYY is mandatory and the next fields optional, but only if all of their preceding fields
     * are also present. This needs to be done because the other date utilities are too lenient.
     *
     * @param item the date item that is to be checked.
     * @return true if the date format is assumed to be valid, false if not.
     */
    private boolean isValidPDFDateFormat(COSBase item)
    {
        if (item instanceof COSString)
        {
            String date = ((COSString) item).getString();
            if (date.matches("D:\\d{4}(\\d{2}(\\d{2}(\\d{2}(\\d{2}(\\d{2}([\\+\\-Z](\\d{2}'\\d{2}')?)?)?)?)?)?)?"))
            {
                return true;
            }
        }
        return false;
    }

}

/*

 Licensed to the Apache Software Foundation (ASF) under one
 or more contributor license agreements.  See the NOTICE file
 distributed with this work for additional information
 regarding copyright ownership.  The ASF licenses this file
 to you under the Apache License, Version 2.0 (the
 "License"); you may not use this file except in compliance
 with the License.  You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.

 */

package org.apache.pdfbox.preflight;

import java.io.Closeable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdfparser.XrefTrailerResolver;
import org.apache.pdfbox.pdmodel.common.COSObjectable;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.preflight.font.container.FontContainer;
import org.apache.pdfbox.preflight.graphic.ICCProfileWrapper;
import org.apache.xmpbox.XMPMetadata;

public class PreflightContext implements Closeable
{
    /**
     * Contains the list of font name embedded in the PDF document.
     */
    private final Map<COSBase, FontContainer<?>> fontContainers = new HashMap<>();

    /**
     * The PDFbox object representation of the PDF source.
     */
    private PreflightDocument document = null;

    /**
     * Contains all Xref/trailer objects and resolves them into single object using startxref reference.
     */
    private XrefTrailerResolver xrefTrailerResolver;

    /**
     * This wrapper contains the ICCProfile used by the PDF file.
     */
    private ICCProfileWrapper iccProfileWrapper = null;

    /**
     * 
     */
    private boolean iccProfileAlreadySearched = false;

    /**
     * MetaData of the current pdf file.
     */
    private XMPMetadata metadata = null;

    private PreflightConfiguration config;

    private PreflightPath validationPath = new PreflightPath();

    private final Set<COSObjectable> processedSet = new HashSet<>();

    private Integer currentPageNumber = null;
    
    private long fileLen;

    /**
     * Create the DocumentHandler using the DataSource which represent the PDF file to check.
     */
    public PreflightContext()
    {
        this.config = null;
    }

    public PreflightContext(final PreflightConfiguration configuration)
    {
        this.config = configuration;
    }

    /**
     * @return the metadata
     */
    public XMPMetadata getMetadata()
    {
        return metadata;
    }

    /**
     * @param metadata
     *            the metadata to set
     */
    public void setMetadata(final XMPMetadata metadata)
    {
        this.metadata = metadata;
    }

    /**
     * @return the PDFBox object representation of the document
     */
    public PreflightDocument getDocument()
    {
        return document;
    }

    public XrefTrailerResolver getXrefTrailerResolver()
    {
        return xrefTrailerResolver;
    }

    public void setXrefTrailerResolver(final XrefTrailerResolver xrefTrailerResolver)
    {
        this.xrefTrailerResolver = xrefTrailerResolver;
    }

    /**
     * Initialize the PDFBox object which present the PDF File.
     * 
     * @param document
     */
    public void setDocument(final PreflightDocument document)
    {
        this.document = document;
    }

    /**
     * Add a FontContainer to allow TextObject validation.
     * 
     * @param cBase the COSBase for the font container.
     * @param fc the font container.
     */
    public void addFontContainer(final COSBase cBase, final FontContainer<?> fc)
    {
        this.fontContainers.put(cBase, fc);
    }

    /**
     * Return the FontContainer identified by the COSBase. If the given object is missing from the
     * {@link #fontContainers} map, the null value is returned.
     * 
     * @param cBase the COSBase for the font container
     * @return the font container.
     */
    public FontContainer<?> getFontContainer(final COSBase cBase)
    {
        return this.fontContainers.get(cBase);
    }

    /**
     * @return the iccProfileWrapper
     */
    public ICCProfileWrapper getIccProfileWrapper()
    {
        return iccProfileWrapper;
    }

    /**
     * @param iccProfileWrapper
     *            the iccProfileWrapper to set
     */
    public void setIccProfileWrapper(final ICCProfileWrapper iccProfileWrapper)
    {
        this.iccProfileWrapper = iccProfileWrapper;
    }

    public PreflightConfiguration getConfig()
    {
        return config;
    }

    public void setConfig(final PreflightConfiguration config)
    {
        this.config = config;
    }

    /**
     * Close all opened resources
     */
    @Override
    public void close()
    {
        IOUtils.closeQuietly(document);
    }

    /**
     * Add the given error the PreflightDocument
     * 
     * @param error
     */
    public void addValidationError(final ValidationError error)
    {
        final PreflightDocument pfDoc = this.document;
        error.setPageNumber(currentPageNumber);
        pfDoc.addValidationError(error);
    }

    /**
     * Add the given errors the PreflightDocument
     * 
     * @param errors the list of validation errors.
     */
    public void addValidationErrors(final List<ValidationError> errors)
    {
        errors.forEach(this.document::addValidationError);
    }

    public PreflightPath getValidationPath()
    {
        return validationPath;
    }

    public void setValidationPath(final PreflightPath validationPath)
    {
        this.validationPath = validationPath;
    }

    public boolean isIccProfileAlreadySearched()
    {
        return iccProfileAlreadySearched;
    }

    public void setIccProfileAlreadySearched(final boolean iccProfileAlreadySearched)
    {
        this.iccProfileAlreadySearched = iccProfileAlreadySearched;
    }

    /**
     * Sets or resets the current page number.
     *
     * @param currentPageNumber zero based page number or null if none is known.
     */
    public void setCurrentPageNumber(final Integer currentPageNumber)
    {
        this.currentPageNumber = currentPageNumber;
    }

    /**
     * Returns the current page number or null if none is known.
     */
    public Integer getCurrentPageNumber()
    {
        return currentPageNumber;
    }

    public void setFileLen(final long fileLen)
    {
        this.fileLen = fileLen;
    }

    public long getFileLen()
    {
        return fileLen;
    }

    /**
     * Add the argument to the set of processed elements,
     *
     * @param cos
     */
    public void addToProcessedSet(final COSObjectable cos)
    {
        processedSet.add(cos);
    }

    /**
     * Tell if the argument is in the set of processed elements.
     *
     * @param cos
     * @return true if in the set, false if not.
     */
    public boolean isInProcessedSet(final COSObjectable cos)
    {
        return processedSet.contains(cos);
    }
}

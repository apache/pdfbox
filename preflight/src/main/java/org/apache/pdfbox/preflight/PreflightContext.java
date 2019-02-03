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

package org.apache.pdfbox.preflight;

import java.io.Closeable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.activation.DataSource;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.pdfparser.XrefTrailerResolver;
import org.apache.pdfbox.pdmodel.common.COSObjectable;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.preflight.font.container.FontContainer;
import org.apache.pdfbox.preflight.graphic.ICCProfileWrapper;
import org.apache.pdfbox.preflight.utils.COSUtils;
import org.apache.xmpbox.XMPMetadata;

public class PreflightContext implements Closeable
{
    /**
     * Contains the list of font name embedded in the PDF document.
     */
    private final Map<COSBase, FontContainer<?>> fontContainers = new HashMap<COSBase, FontContainer<?>>();

    /**
     * The PDFbox object representation of the PDF source.
     */
    private PreflightDocument document = null;

    /**
     * The datasource to load the document from. Needed by StreamValidationProcess.
     */
    private DataSource dataSource = null;

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

    private PreflightConfiguration config = null;

    private PreflightPath validationPath = new PreflightPath();

    private final Set<COSObjectable> processedSet = new HashSet<COSObjectable>();

    private Integer currentPageNumber = null;
    
    private long fileLen;

    /**
     * Create the DocumentHandler using the DataSource which represent the PDF file to check.
     * 
     * @param dataSource
     */
    public PreflightContext(DataSource dataSource)
    {
        this.dataSource = dataSource;
    }

    public PreflightContext(DataSource dataSource, PreflightConfiguration configuration)
    {
        this.dataSource = dataSource;
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
    public void setMetadata(XMPMetadata metadata)
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

    public void setXrefTrailerResolver(XrefTrailerResolver xrefTrailerResolver)
    {
        this.xrefTrailerResolver = xrefTrailerResolver;
    }

    /**
     * Initialize the PDFBox object which present the PDF File.
     * 
     * @param document
     */
    public void setDocument(PreflightDocument document)
    {
        this.document = document;
    }

    /**
     * 
     * @return The datasource of the pdf document
     */
    public DataSource getSource()
    {
        return dataSource;
    }

    public boolean isComplete()
    {
        return (document != null) && (dataSource != null);
    }

    /**
     * Add a FontContainer to allow TextObject validation.
     * 
     * @param cBase the COSBase for the font container.
     * @param fc the font container.
     */
    public void addFontContainer(COSBase cBase, FontContainer<?> fc)
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
    public FontContainer<?> getFontContainer(COSBase cBase)
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
    public void setIccProfileWrapper(ICCProfileWrapper iccProfileWrapper)
    {
        this.iccProfileWrapper = iccProfileWrapper;
    }

    public PreflightConfiguration getConfig()
    {
        return config;
    }

    public void setConfig(PreflightConfiguration config)
    {
        this.config = config;
    }

    /**
     * Close all opened resources
     */
    @Override
    public void close()
    {
        COSUtils.closeDocumentQuietly(document);
    }

    /**
     * Add the given error the PreflightDocument
     * 
     * @param error
     */
    public void addValidationError(ValidationError error)
    {
        PreflightDocument pfDoc = this.document;
        error.setPageNumber(currentPageNumber);
        pfDoc.addValidationError(error);
    }

    /**
     * Add the given errors the PreflightDocument
     * 
     * @param errors the list of validation errors.
     */
    public void addValidationErrors(List<ValidationError> errors)
    {
        PreflightDocument pfDoc = this.document;
        for (ValidationError error : errors)
        {
            pfDoc.addValidationError(error);
        }
    }

    public PreflightPath getValidationPath()
    {
        return validationPath;
    }

    public void setValidationPath(PreflightPath validationPath)
    {
        this.validationPath = validationPath;
    }

    public boolean isIccProfileAlreadySearched()
    {
        return iccProfileAlreadySearched;
    }

    public void setIccProfileAlreadySearched(boolean iccProfileAlreadySearched)
    {
        this.iccProfileAlreadySearched = iccProfileAlreadySearched;
    }

    /**
     * Sets or resets the current page number.
     *
     * @param currentPageNumber zero based page number or null if none is known.
     */
    public void setCurrentPageNumber(Integer currentPageNumber)
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

    public void setFileLen(long fileLen)
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
    public void addToProcessedSet(COSObjectable cos)
    {
        processedSet.add(cos);
    }

    /**
     * Tell if the argument is in the set of processed elements.
     *
     * @param cos
     * @return true if in the set, false if not.
     */
    public boolean isInProcessedSet(COSObjectable cos)
    {
        return processedSet.contains(cos);
    }
}

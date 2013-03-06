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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import javax.activation.DataSource;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.pdfparser.XrefTrailerResolver;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.preflight.font.container.FontContainer;
import org.apache.pdfbox.preflight.graphic.ICCProfileWrapper;
import org.apache.pdfbox.preflight.utils.COSUtils;
import org.apache.xmpbox.XMPMetadata;

public class PreflightContext
{
    /**
     * Contains the list of font name embedded in the PDF document.
     */
    protected Map<COSBase, FontContainer> fontContainers = new HashMap<COSBase, FontContainer>();

    /**
     * The PDFbox object representation of the PDF source.
     */
    protected PreflightDocument document = null;

    /**
     * The datasource to load the document from
     */
    protected DataSource source = null;
    //
    // /**
    // * JavaCC Token Manager used to get some content of the PDF file as string (ex
    // * : Trailers)
    // */
    // protected ExtractorTokenManager pdfExtractor = null;

    /**
     * Contains all Xref/trailer objects and resolves them into single object using startxref reference.
     */
    private XrefTrailerResolver xrefTableResolver;

    /**
     * This wrapper contains the ICCProfile used by the PDF file.
     */
    protected ICCProfileWrapper iccProfileWrapper = null;

    /**
     * 
     */
    protected boolean iccProfileAlreadySearched = false;

    /**
     * MetaData of the current pdf file.
     */
    protected XMPMetadata metadata = null;

    protected PreflightConfiguration config = null;

    protected PreflightPath validationPath = new PreflightPath();

    /**
     * Create the DocumentHandler using the DataSource which represent the PDF file to check.
     * 
     * @param source
     */
    public PreflightContext(DataSource source)
    {
        this.source = source;
    }

    public PreflightContext(DataSource source, PreflightConfiguration configuration)
    {
        this.source = source;
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

    // /**
    // * @return the value of the pdfExtractor attribute.
    // */
    // public ExtractorTokenManager getPdfExtractor() {
    // return pdfExtractor;
    // }
    //
    // /**
    // * Initialize the pdfExtractor attribute.
    // *
    // * @param pdfExtractor
    // */
    // public void setPdfExtractor(ExtractorTokenManager pdfExtractor) {
    // this.pdfExtractor = pdfExtractor;
    // }

    /**
     * @return the PDFBox object representation of the document
     */
    public PreflightDocument getDocument()
    {
        return document;
    }

    public XrefTrailerResolver getXrefTableResolver()
    {
        return xrefTableResolver;
    }

    public void setXrefTableResolver(XrefTrailerResolver xrefTableResolver)
    {
        this.xrefTableResolver = xrefTableResolver;
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
        return source;
    }

    public boolean isComplete()
    {
        return (document != null) && (source != null);
    }

    /**
     * Add a FontContainer to allow TextObject validation.
     * 
     * @param fKey
     * @param fc
     */
    public void addFontContainer(COSBase cBase, FontContainer fc)
    {
        this.fontContainers.put(cBase, fc);
    }

    /**
     * Return the FontContainer identified by the COSBase. If the given object is missing from the
     * {@link #fontContainers} map, the null value is returned.
     * 
     * @param fKey
     * @return
     */
    public FontContainer getFontContainer(COSBase cBase)
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
        PreflightDocument document = (PreflightDocument) this.document;
        document.addValidationError(error);
    }

    /**
     * Add the given errors the PreflightDocument
     * 
     * @param error
     */
    public void addValidationErrors(List<ValidationError> errors)
    {
        PreflightDocument document = (PreflightDocument) this.document;
        for (ValidationError error : errors)
        {
            document.addValidationError(error);
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

}

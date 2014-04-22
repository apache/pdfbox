/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pdfbox.pdmodel.interactive.digitalsignature.visible;

import java.io.IOException;
import java.io.InputStream;

/**
 * This builder class is in order to create visible signature properties.
 * 
 * @author <a href="mailto:vakhtang.koroghlishvili@gmail.com"> vakhtang koroghlishvili (gogebashvili) </a>
 * 
 */
public class PDVisibleSigProperties
{
    private String signerName;
    private String signerLocation;
    private String signatureReason;
    private boolean visualSignEnabled;
    private int page;
    private int preferredSize;

    private InputStream visibleSignature;
    private PDVisibleSignDesigner pdVisibleSignature;

    /**
     * start building of visible signature
     * 
     * @throws IOException
     */
    public void buildSignature() throws IOException
    {
        PDFTemplateBuilder builder = new PDVisibleSigBuilder();
        PDFTemplateCreator creator = new PDFTemplateCreator(builder);
        setVisibleSignature(creator.buildPDF(getPdVisibleSignature()));
    }

    /**
     * 
     * @return - signer name
     */
    public String getSignerName()
    {
        return signerName;
    }

    /**
     * Sets signer name
     * @param signerName
     * @return
     */
    public PDVisibleSigProperties signerName(String signerName)
    {
        this.signerName = signerName;
        return this;
    }

    /**
     * Gets signer locations
     * @return - location
     */
    public String getSignerLocation()
    {
        return signerLocation;
    }

    /**
     * Sets location
     * @param signerLocation
     * @return
     */
    public PDVisibleSigProperties signerLocation(String signerLocation)
    {
        this.signerLocation = signerLocation;
        return this;
    }

    /**
     * gets reason of signing
     * @return 
     */
    public String getSignatureReason()
    {
        return signatureReason;
    }

    /**
     * sets reason of signing
     * @param signatureReason
     * @return
     */
    public PDVisibleSigProperties signatureReason(String signatureReason)
    {
        this.signatureReason = signatureReason;
        return this;
    }

    /**
     * returns your page
     * @return 
     */
    public int getPage()
    {
        return page;
    }

    /**
     * sets page number
     * @param page
     * @return
     */
    public PDVisibleSigProperties page(int page)
    {
        this.page = page;
        return this;
    }

    /**
     * gets our preferred size
     * @return
     */
    public int getPreferredSize()
    {
        return preferredSize;
    }

    /**
     * sets our preferred size
     * @param preferredSize
     * @return
     */
    public PDVisibleSigProperties preferredSize(int preferredSize)
    {
        this.preferredSize = preferredSize;
        return this;
    }

    /**
     * checks if we need to add visible signature
     * @return
     */
    public boolean isVisualSignEnabled()
    {
        return visualSignEnabled;
    }

    /**
     * sets visible signature to be added or not
     * @param visualSignEnabled
     * @return
     */
    public PDVisibleSigProperties visualSignEnabled(boolean visualSignEnabled)
    {
        this.visualSignEnabled = visualSignEnabled;
        return this;
    }

    /**
     * this method gets visible signature configuration object
     * @return
     */
    public PDVisibleSignDesigner getPdVisibleSignature()
    {
        return pdVisibleSignature;
    }

    /**
     * Sets visible signature configuration Object
     * @param pdVisibleSignature
     * @return
     */
    public PDVisibleSigProperties setPdVisibleSignature(PDVisibleSignDesigner pdVisibleSignature)
    {
        this.pdVisibleSignature = pdVisibleSignature;
        return this;
    }

    /**
     * returns visible signature configuration object
     * @return
     */
    public InputStream getVisibleSignature()
    {
        return visibleSignature;
    }

    /**
     * sets configuration object of visible signature
     * @param visibleSignature
     */
    public void setVisibleSignature(InputStream visibleSignature)
    {
        this.visibleSignature = visibleSignature;
    }

}

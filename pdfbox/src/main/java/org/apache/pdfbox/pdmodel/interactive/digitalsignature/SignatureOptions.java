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
package org.apache.pdfbox.pdmodel.interactive.digitalsignature;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.io.RandomAccessBuffer;
import org.apache.pdfbox.io.RandomAccessBufferedFileInputStream;
import org.apache.pdfbox.io.RandomAccessRead;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.visible.PDVisibleSigProperties;

/**
 * This contains the visual signature as a COSDocument, its preferred size and the page.
 */
public class SignatureOptions implements Closeable
{
    private COSDocument visualSignature;
    private int preferredSignatureSize;
    private int pageNo;

    // the pdf to be read
    // this is done analog to PDDocument
    private RandomAccessRead pdfSource = null;

    public static final int DEFAULT_SIGNATURE_SIZE = 0x2500;

    /**
     * Creates the default signature options.
     */
    public SignatureOptions()
    {
        pageNo = 0;
    }

    /**
     * Set the 0-based page number.
     *
     * @param pageNo the page number
     */
    public void setPage(int pageNo)
    {
        this.pageNo = pageNo;
    }

    /**
     * Get the 0-based page number.
     *
     * @return the page number
     */
    public int getPage()
    {
        return pageNo;
    }

    /**
     * Reads the visual signature from the given file.
     *
     * @param file the file containing the visual signature
     * @throws IOException when something went wrong during parsing
     */
    public void setVisualSignature(File file) throws IOException
    {
        initFromRandomAccessRead(new RandomAccessBufferedFileInputStream(file));
    }

    /**
     * Reads the visual signature from the given input stream.
     *
     * @param is the input stream containing the visual signature
     * @throws IOException when something went wrong during parsing
     */
    public void setVisualSignature(InputStream is) throws IOException
    {
        initFromRandomAccessRead(new RandomAccessBuffer(is));
    }
    
    private void initFromRandomAccessRead(RandomAccessRead rar) throws IOException
    {
        pdfSource = rar;
        PDFParser parser = new PDFParser(pdfSource);
        parser.parse();
        visualSignature = parser.getDocument();
    }

    /**
     * Reads the visual signature from the given visual signature properties
     *
     * @param visSignatureProperties the <code>PDVisibleSigProperties</code> object containing the
     * visual signature
     *
     * @throws IOException when something went wrong during parsing
     */
    public void setVisualSignature(PDVisibleSigProperties visSignatureProperties) throws IOException
    {
        setVisualSignature(visSignatureProperties.getVisibleSignature());
    }

    /**
     * Get the visual signature.
     *
     * @return the visual signature
     */
    public COSDocument getVisualSignature()
    {
        return visualSignature;
    }

    /**
     * Get the preferred size of the signature.
     *
     * @return the preferred size of the signature in bytes.
     */
    public int getPreferredSignatureSize()
    {
        return preferredSignatureSize;
    }

    /**
     * Set the preferred size of the signature.
     *
     * @param size the size of the signature in bytes. Only values above 0 will be considered.
     */
    public void setPreferredSignatureSize(int size)
    {
        if (size > 0)
        {
            preferredSignatureSize = size;
        }
    }

    /**
     * Closes the visual signature COSDocument, if any.
     *
     * @throws IOException if the document could not be closed
     */
    @Override
    public void close() throws IOException
    {
        if (visualSignature != null)
        {
            visualSignature.close();
        }
        if (pdfSource != null)
        {
            pdfSource.close();
        }
    }
}

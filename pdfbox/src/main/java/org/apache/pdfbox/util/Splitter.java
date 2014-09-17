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
package org.apache.pdfbox.util;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

/**
 * Split a document into several other documents.
 *
 * @author Mario Ivankovits
 * @author Ben Litchfield
 */
public class Splitter
{
    private PDDocument sourceDocument;
    private PDDocument currentDestinationDocument;

    private int splitLength = 1;
    private int startPage = Integer.MIN_VALUE;
    private int endPage = Integer.MAX_VALUE;
    private List<PDDocument> destinationDocuments;

    private int pageNumber = 0;

    /**
     * This will take a document and split into several other documents.
     *
     * @param document The document to split.
     *
     * @return A list of all the split documents.
     *
     * @throws IOException If there is an IOError
     */
    public List<PDDocument> split(PDDocument document) throws IOException
    {
        destinationDocuments = new ArrayList<PDDocument>();
        sourceDocument = document;
        processPages();
        return destinationDocuments;
    }

    /**
     * This will tell the splitting algorithm where to split the pages.  The default
     * is 1, so every page will become a new document.  If it was to then each document would
     * contain 2 pages.  So it the source document had 5 pages it would split into
     * 3 new documents, 2 documents containing 2 pages and 1 document containing one
     * page.
     *
     * @param split The number of pages each split document should contain.
     */
    public void setSplitAtPage(int split)
    {
        if(split <= 0)
        {
            throw new RuntimeException("Error split must be at least one page.");
        }
        splitLength = split;
    }

    /**
     * This will set the start page.
     *
     * @param start the start page
     */
    public void setStartPage(int start)
    {
        if(start <= 0)
        {
            throw new RuntimeException("Error split must be at least one page.");
        }
        startPage = start;
    }

    /**
     * This will set the end page.
     *
     * @param end the end page
     */
    public void setEndPage(int end)
    {
        if(end <= 0)
        {
            throw new RuntimeException("Error split must be at least one page.");
        }
        endPage = end;
    }

    /**
     * Interface method to handle the start of the page processing.
     *
     * @throws IOException If an IO error occurs.
     */
    private void processPages() throws IOException
    {
        for (int i = 0; i < sourceDocument.getNumberOfPages(); i++)
        {
            PDPage page = sourceDocument.getPage(i);
            if (pageNumber + 1 >= startPage && pageNumber + 1 <= endPage)
            {
                processPage(page);
                pageNumber++;
            }
            else
            {
                if (pageNumber > endPage)
                {
                    break;
                }
                else
                {
                    pageNumber++;
                }
            }
        }
    }

    /**
     * Interface method, you can control where a document gets split by implementing
     * this method.  By default a split occurs at every page.  If you wanted to split
     * based on some complex logic then you could override this method.  For example.
     * <code>
     * protected void createNewDocumentIfNecessary()
     * {
     *     if(isPrime(pageNumber))
     *     {
     *         super.createNewDocumentIfNecessary();
     *     }
     * }
     * </code>
     *
     * @throws IOException If there is an error creating the new document.
     */
    private void createNewDocumentIfNecessary() throws IOException
    {
        if (splitAtPage(pageNumber) || currentDestinationDocument == null)
        {
            currentDestinationDocument = createNewDocument();
            destinationDocuments.add(currentDestinationDocument);
        }
    }

    /**
     * Check if it is necessary to create a new document.
     *
     * @return true If a new document should be created.
     */
    protected boolean splitAtPage(int pageNumber)
    {
        return pageNumber % splitLength == 0;
    }

    /**
     * Create a new document to write the split contents to.
     *
     * @throws IOException If there is an problem creating the new document.
     */
    protected PDDocument createNewDocument() throws IOException
    {
        PDDocument document = new PDDocument();
        document.setDocumentInformation(getSourceDocument().getDocumentInformation());
        document.getDocumentCatalog().setViewerPreferences(
                getSourceDocument().getDocumentCatalog().getViewerPreferences());
        return document;
    }

    /**
     * Interface to start processing a new page.
     *
     * @param page The page that is about to get processed.
     *
     * @throws IOException If there is an error creating the new document.
     */
    protected void processPage(PDPage page) throws IOException
    {
        createNewDocumentIfNecessary();
        PDPage imported = getDestinationDocument().importPage(page);
        imported.setCropBox(page.findCropBox());
        imported.setMediaBox(page.findMediaBox());
        // only the resources of the page will be copied
        imported.setResources(page.getResources());
        imported.setRotation(page.findRotation());
    }

    /**
     * The source PDF document.
     */
    protected final PDDocument getSourceDocument()
    {
        return sourceDocument;
    }

    /**
     * The source PDF document.
     */
    protected final PDDocument getDestinationDocument()
    {
        return currentDestinationDocument;
    }
}

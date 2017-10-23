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
package org.apache.pdfbox.multipdf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.action.PDAction;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionGoTo;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageDestination;

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

    private int currentPageNumber = 0;

    private MemoryUsageSetting memoryUsageSetting = null;

    /**
     * @return the current memory setting.
     */
    public MemoryUsageSetting getMemoryUsageSetting()
    {
        return memoryUsageSetting;
    }

    /**
     * Set the memory setting.
     * 
     * @param memoryUsageSetting 
     */
    public void setMemoryUsageSetting(MemoryUsageSetting memoryUsageSetting)
    {
        this.memoryUsageSetting = memoryUsageSetting;
    }

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
        destinationDocuments = new ArrayList<>();
        sourceDocument = document;
        processPages();
        return destinationDocuments;
    }

    /**
     * This will tell the splitting algorithm where to split the pages.  The default
     * is 1, so every page will become a new document.  If it was two then each document would
     * contain 2 pages.  If the source document had 5 pages it would split into
     * 3 new documents, 2 documents containing 2 pages and 1 document containing one
     * page.
     *
     * @param split The number of pages each split document should contain.
     * @throws IllegalArgumentException if the page is smaller than one.
     */
    public void setSplitAtPage(int split)
    {
        if(split <= 0)
        {
            throw new IllegalArgumentException("Number of pages is smaller than one");
        }
        splitLength = split;
    }

    /**
     * This will set the start page.
     *
     * @param start the 1-based start page
     * @throws IllegalArgumentException if the start page is smaller than one.
     */
    public void setStartPage(int start)
    {
        if(start <= 0)
        {
            throw new IllegalArgumentException("Start page is smaller than one");
        }
        startPage = start;
    }

    /**
     * This will set the end page.
     *
     * @param end the 1-based end page
     * @throws IllegalArgumentException if the end page is smaller than one.
     */
    public void setEndPage(int end)
    {
        if(end <= 0)
        {
            throw new IllegalArgumentException("End page is smaller than one");
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
            if (currentPageNumber + 1 >= startPage && currentPageNumber + 1 <= endPage)
            {
                processPage(page);
                currentPageNumber++;
            }
            else
            {
                if (currentPageNumber > endPage)
                {
                    break;
                }
                else
                {
                    currentPageNumber++;
                }
            }
        }
    }

    /**
     * Helper method for creating new documents at the appropriate pages.
     *
     * @throws IOException If there is an error creating the new document.
     */
    private void createNewDocumentIfNecessary() throws IOException
    {
        if (splitAtPage(currentPageNumber) || currentDestinationDocument == null)
        {
            currentDestinationDocument = createNewDocument();
            destinationDocuments.add(currentDestinationDocument);
        }
    }

    /**
     * Check if it is necessary to create a new document.
     * By default a split occurs at every page.  If you wanted to split
     * based on some complex logic then you could override this method.  For example.
     * <code>
     * protected void splitAtPage()
     * {
     *     // will split at pages with prime numbers only
     *     return isPrime(pageNumber);
     * }
     * </code>
     * @param pageNumber the 0-based page number to be checked as splitting page
     * 
     * @return true If a new document should be created.
     */
    protected boolean splitAtPage(int pageNumber)
    {
        return (pageNumber + 1 - Math.max(1, startPage)) % splitLength == 0;
    }

    /**
     * Create a new document to write the split contents to.
     *
     * @return the newly created PDDocument. 
     * @throws IOException If there is an problem creating the new document.
     */
    protected PDDocument createNewDocument() throws IOException
    {
        PDDocument document = memoryUsageSetting == null ?
                                new PDDocument() : new PDDocument(memoryUsageSetting);
        document.getDocument().setVersion(getSourceDocument().getVersion());
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
        imported.setResources(page.getResources());
        // remove page links to avoid copying not needed resources 
        processAnnotations(imported);
    }

    private void processAnnotations(PDPage imported) throws IOException
    {
        List<PDAnnotation> annotations = imported.getAnnotations();
        for (PDAnnotation annotation : annotations)
        {
            if (annotation instanceof PDAnnotationLink)
            {
                PDAnnotationLink link = (PDAnnotationLink)annotation;   
                PDDestination destination = link.getDestination();
                if (destination == null && link.getAction() != null)
                {
                    PDAction action = link.getAction();
                    if (action instanceof PDActionGoTo)
                    {
                        destination = ((PDActionGoTo)action).getDestination();
                    }
                }
                if (destination instanceof PDPageDestination)
                {
                    // TODO preserve links to pages within the splitted result  
                    ((PDPageDestination) destination).setPage(null);
                }
            }
            // TODO preserve links to pages within the splitted result  
            annotation.setPage(null);
        }
    }
    /**
     * The source PDF document.
     * 
     * @return the pdf to be splitted
     */
    protected final PDDocument getSourceDocument()
    {
        return sourceDocument;
    }

    /**
     * The source PDF document.
     * 
     * @return current destination pdf
     */
    protected final PDDocument getDestinationDocument()
    {
        return currentDestinationDocument;
    }
}

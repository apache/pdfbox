/**
 * Copyright (c) 2004-2005, www.pdfbox.org
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of pdfbox; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://www.pdfbox.org
 *
 */
package org.pdfbox.util;

import org.pdfbox.pdmodel.PDDocument;
import org.pdfbox.pdmodel.PDPage;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Split a document into several other documents.
 *
 * @author Mario Ivankovits (mario@ops.co.at)
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.7 $
 */
public class Splitter
{

    /**
     * The source PDF document.
     */
    protected PDDocument pdfDocument;
    
    /**
     * The current PDF document that contains the splitted page.
     */
    protected PDDocument currentDocument = null;
    
    private int splitAtPage=1;
    private List newDocuments = null;

    /**
     * The current page number that we are processing, zero based.
     */
    protected int pageNumber = 0;

    /**
     * This will take a document and split into several other documents.
     *
     * @param document The document to split.
     *
     * @return A list of all the split documents.
     *
     * @throws IOException If there is an IOError
     */
    public List split( PDDocument document ) throws IOException
    {
        newDocuments = new ArrayList();
        pdfDocument = document;

        List pages = pdfDocument.getDocumentCatalog().getAllPages();
        processPages(pages);
        return newDocuments;
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
    public void setSplitAtPage( int split )
    {
        if( split <= 0 )
        {
            throw new RuntimeException( "Error split must be at least one page." );
        }
        splitAtPage = split;
    }

    /**
     * This will return how many pages each split document will contain.
     *
     * @return The split parameter.
     */
    public int getSplitAtPage()
    {
        return splitAtPage;
    }

    /**
     * Interface method to handle the start of the page processing.
     *
     * @param pages The list of pages from the source document.
     *
     * @throws IOException If an IO error occurs.
     */
    protected void processPages(List pages) throws IOException
    {
        Iterator iter = pages.iterator();
        while( iter.hasNext() )
        {
            PDPage page = (PDPage)iter.next();
            processNextPage( page );
        }
    }
    
    /**
     * Interface method, you can control where a document gets split by implementing
     * this method.  By default a split occurs at every page.  If you wanted to split
     * based on some complex logic then you could override this method.  For example.
     * <code>
     * protected void createNewDocumentIfNecessary()
     * {
     *     if( isPrime( pageNumber ) )
     *     {
     *         super.createNewDocumentIfNecessary();
     *     }
     * }
     * </code>
     *
     * @throws IOException If there is an error creating the new document.
     */
    protected void createNewDocumentIfNecessary() throws IOException 
    {
        if (isNewDocNecessary()) 
        {
            createNewDocument();
        }
    }
    
    /**
     * Check if it is necessary to create a new document.
     * 
     * @return true If a new document should be created.
     */
    protected boolean isNewDocNecessary() 
    {
        return pageNumber % splitAtPage == 0 || currentDocument == null;
    }
    
    /**
     * Create a new document to write the splitted contents to.
     * 
     * @throws IOException If there is an problem creating the new document.
     */
    protected void createNewDocument() throws IOException 
    {
        currentDocument = new PDDocument();
        currentDocument.setDocumentInformation(pdfDocument.getDocumentInformation());
        currentDocument.getDocumentCatalog().setViewerPreferences(
        pdfDocument.getDocumentCatalog().getViewerPreferences());
        newDocuments.add(currentDocument);
    }



    /**
     * Interface to start processing a new page.
     *
     * @param page The page that is about to get processed.
     *
     * @throws IOException If there is an error creating the new document.
     */
    protected void processNextPage( PDPage page ) throws IOException
    {
        createNewDocumentIfNecessary();
        PDPage imported = currentDocument.importPage( page );
        imported.setCropBox( page.findCropBox() );
        imported.setMediaBox( page.findMediaBox() );
        imported.setResources( page.findResources() );
        imported.setRotation( page.findRotation() );
        pageNumber++;
    }
}
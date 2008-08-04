/**
 * Copyright (c) 2006, www.pdfbox.org
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
package org.pdfbox;

import org.pdfbox.util.PDFMergerUtility;

/**
 * This is the main program that will take a list of pdf documents and merge them, 
 * saving the result in a new document.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.2 $
 */
public class PDFMerger
{
    /**
     * Infamous main method.
     *
     * @param args Command line arguments, should be at least 3.
     *
     * @throws Exception If there is an error parsing the document.
     */
    public static void main( String[] args ) throws Exception
    {
        PDFMerger merge = new PDFMerger();
        merge.merge( args );
    }

    private void merge( String[] args ) throws Exception
    {
        String destinationFileName = "";
        String sourceFileName = null;
        
        if ( args.length < 3 )
        {
            usage();
        }

        PDFMergerUtility merger = new PDFMergerUtility();
        for( int i=0; i<args.length-1; i++ )
        {
            sourceFileName = args[i];
            merger.addSource(sourceFileName);
        }
        
        destinationFileName = args[args.length-1];
        
        merger.setDestinationFileName(destinationFileName);
        
        merger.mergeDocuments();
    }

    /**
     * This will print the usage requirements and exit.
     */
    private static void usage()
    {
        System.err.println( "Usage: java org.pdfbox.PDFMerger <Source PDF File 2..n> <Destination PDF File>\n" +
            "  <Source PDF File 2..n>       2 or more source PDF documents to merge\n" +
            "  <Destination PDF File>       The PDF document to save the merged documents to\n"
            );
        System.exit( 1 );
    }
}
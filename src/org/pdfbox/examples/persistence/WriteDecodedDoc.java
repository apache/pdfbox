/**
 * Copyright (c) 2003-2004, www.pdfbox.org
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
package org.pdfbox.examples.persistence;

import java.io.IOException;

import java.util.Iterator;

import org.pdfbox.cos.COSBase;
import org.pdfbox.cos.COSObject;
import org.pdfbox.cos.COSStream;

import org.pdfbox.pdmodel.PDDocument;

import org.pdfbox.exceptions.COSVisitorException;

import org.pdfbox.exceptions.InvalidPasswordException;

/**
 * load document and write with all streams decoded.
 *
 * @author Michael Traut
 * @version $Revision: 1.8 $
 */
public class WriteDecodedDoc
{

    /**
     * Constructor.
     */
    public WriteDecodedDoc()
    {
        super();
    }

    /**
     * This will perform the document reading, decoding and writing.
     *
     * @param in The filename used for input.
     * @param out The filename used for output.
     *
     * @throws IOException If there is an error parsing the document.
     * @throws COSVisitorException If there is an error while copying the document.
     */
    public void doIt(String in, String out) throws IOException, COSVisitorException
    {
        PDDocument doc = null;
        try
        {
            doc = PDDocument.load( in );
            if( doc.isEncrypted() )
            {
                try
                {
                    doc.decrypt( "" );
                }
                catch( InvalidPasswordException e )
                {
                    System.err.println( "Error: The document is encrypted." );
                }
                catch( org.pdfbox.exceptions.CryptographyException e )
                {
                    e.printStackTrace();
                }
            }

            for (Iterator i = doc.getDocument().getObjects().iterator(); i.hasNext();)
            {
                COSBase base = ((COSObject) i.next()).getObject();
                if (base instanceof COSStream)
                {
                    // just kill the filters
                    COSStream cosStream = (COSStream)base;
                    cosStream.getUnfilteredStream();
                    cosStream.setFilters(null);
                }
            }
            doc.save( out );
        }
        finally
        {
            if( doc != null )
            {
                doc.close();
            }
        }
    }

    /**
     * This will write a PDF document with completely decoded streams.
     * <br />
     * see usage() for commandline
     *
     * @param args command line arguments
     */
    public static void main(String[] args)
    {
        WriteDecodedDoc app = new WriteDecodedDoc();
        try
        {
            if( args.length != 2 )
            {
                app.usage();
            }
            else
            {
                app.doIt( args[0], args[1]);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * This will print out a message telling how to use this example.
     */
    private void usage()
    {
        System.err.println( "usage: " + this.getClass().getName() + " <input-file> <output-file>" );
    }
}
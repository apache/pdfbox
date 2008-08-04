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
package org.pdfbox;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.pdfbox.pdmodel.PDDocument;
import org.pdfbox.pdmodel.PDPage;
import org.pdfbox.pdmodel.PDResources;
import org.pdfbox.pdmodel.encryption.AccessPermission;
import org.pdfbox.pdmodel.encryption.StandardDecryptionMaterial;
import org.pdfbox.pdmodel.graphics.xobject.PDXObjectImage;

/**
 * This will read a read pdf and extract images. <br/><br/>
 *
 * usage: java org.pdfbox.ExtractImages &lt;pdffile&gt; &lt;password&gt; [imageprefix]
 *
 * @author  <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.7 $
 */
public class ExtractImages
{
    private int imageCounter = 1;
    
    private static final String PASSWORD = "-password";
    private static final String PREFIX = "-prefix";
    
    /**
     * This is the entry point for the application.
     *
     * @param args The command-line arguments.
     *
     * @throws Exception If there is an error decrypting the document.
     */
    public static void main( String[] args ) throws Exception
    {
        ExtractImages extractor = new ExtractImages();
        extractor.extractImages( args );
    }

    private void extractImages( String[] args ) throws Exception
    {
        if( args.length < 1 || args.length > 3 )
        {
            usage();
        }
        else
        {
            String pdfFile = null;
            String password = "";
            String prefix = null;
            for( int i=0; i<args.length; i++ )
            {
                if( args[i].equals( PASSWORD ) )
                {
                    i++;
                    if( i >= args.length )
                    {
                        usage();
                    }
                    password = args[i];
                }
                else if( args[i].equals( PREFIX ) )
                {
                    i++;
                    if( i >= args.length )
                    {
                        usage();
                    }
                    prefix = args[i];
                }
                else
                {
                    if( pdfFile == null )
                    {
                        pdfFile = args[i];
                    }
                }
            }
            if(pdfFile == null)
            {
                usage();
            }
            else
            {
                if( prefix == null && pdfFile.length() >4 )
                {
                    prefix = pdfFile.substring( 0, pdfFile.length() -4 );
                }
    
                PDDocument document = null;
    
                try
                {
                    document = PDDocument.load( pdfFile );
    
                    if( document.isEncrypted() )
                    {
                    
                        StandardDecryptionMaterial spm = new StandardDecryptionMaterial(password);
                        document.openProtection(spm);
                        AccessPermission ap = document.getCurrentAccessPermission();
                            
                        
                        if( ! ap.canExtractContent() )
                        {
                            throw new IOException(
                                "Error: You do not have permission to extract images." );
                        }
                    }
                    
                    List pages = document.getDocumentCatalog().getAllPages();
                    Iterator iter = pages.iterator();
                    while( iter.hasNext() )
                    {
                        PDPage page = (PDPage)iter.next();
                        PDResources resources = page.getResources();
                        Map images = resources.getImages();
                        if( images != null )
                        {
                            Iterator imageIter = images.keySet().iterator();
                            while( imageIter.hasNext() )
                            {
                                String key = (String)imageIter.next();
                                PDXObjectImage image = (PDXObjectImage)images.get( key );
                                String name = getUniqueFileName( key, image.getSuffix() );
                                System.out.println( "Writing image:" + name );
                                image.write2file( name );
                            }
                        }
                    }
                }
                finally
                {
                    if( document != null )
                    {
                        document.close();
                    }
                }
            }
        }
    }
    
    private String getUniqueFileName( String prefix, String suffix )
    {
        String uniqueName = null;
        File f = null;
        while( f == null || f.exists() )
        {
            uniqueName = prefix + "-" + imageCounter;
            f = new File( uniqueName + "." + suffix );
            imageCounter++;
        }
        return uniqueName;
    }

    /**
     * This will print the usage requirements and exit.
     */
    private static void usage()
    {
        System.err.println( "Usage: java org.pdfbox.ExtractImages [OPTIONS] <PDF file>\n" +
            "  -password  <password>        Password to decrypt document\n" +
            "  -prefix  <image-prefix>      Image prefix(default to pdf name)\n" +
            "  <PDF file>                   The PDF document to use\n"
            );
        System.exit( 1 );
    }

}
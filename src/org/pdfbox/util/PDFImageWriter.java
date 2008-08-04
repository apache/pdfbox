/**
 * Copyright (c) 2003-2007, www.pdfbox.org
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

import java.io.IOException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.imageio.IIOException;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import org.pdfbox.exceptions.InvalidPasswordException;

import org.pdfbox.pdmodel.PDDocument;
import org.pdfbox.pdmodel.PDPage;

/**
 * This class will take a pdf document and strip out all of the text and ignore the
 * formatting and such.  Please note; it is up to clients of this class to verify that
 * a specific user has the correct permissions to extract text from the
 * PDF document.
 *
 *Patterned after PDFTextStripper
 *
 * @author <a href="mailto:DanielWilson@Users.SourceForge.net">Daniel Wilson</a>
 * @version $Revision: 1.1 $
 */
public class PDFImageWriter extends PDFStreamEngine
{   

    /**
     * Instantiate a new PDFImageWriter object.  
     * @throws IOException If there is an error loading the properties.
     */
    public PDFImageWriter() throws IOException
    {
        super();// ResourceLoader.loadProperties( "Resources/PDFImageWriter.properties", true ) );
    }
    
    /**
     * Instantiate a new PDFImageWriter object.  Loading all of the operator mappings
     * from the properties object that is passed in.
     * 
     * @param props The properties containing the mapping of operators to PDFOperator 
     * classes.
     * 
     * @throws IOException If there is an error reading the properties.
     */
    public PDFImageWriter( Properties props ) throws IOException
    {
        super( props );
    }

    
    public boolean WriteImage(PDDocument document, String imageType, String password, int startPage, int endPage, String outputPrefix) throws IOException {
	    boolean bSuccess = true;
	     List pages = document.getDocumentCatalog().getAllPages();
                for( int i=startPage-1; i<endPage && i<pages.size(); i++ )
                {
                    ImageOutputStream output = null; 
                    ImageWriter imageWriter = null;
                    try
                    {
                        PDPage page = (PDPage)pages.get( i );
                        BufferedImage image = page.convertToImage();
                        String fileName = outputPrefix + (i+1) + "." + imageType;
                        System.out.println( "Writing:" + fileName );
                        output = ImageIO.createImageOutputStream( new File( fileName ) );
                        
                        boolean foundWriter = false;
                        Iterator writerIter = ImageIO.getImageWritersByFormatName( imageType );
                        while( writerIter.hasNext() && !foundWriter )
                        {
                            try
                            {
                                imageWriter = (ImageWriter)writerIter.next();
                                ImageWriteParam writerParams = imageWriter.getDefaultWriteParam();
                                if(writerParams.canWriteCompressed() )
                                {
                                    writerParams.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                                    writerParams.setCompressionQuality(1.0f);
                                }
                                
                                
                                imageWriter.setOutput( output );
                                imageWriter.write( null, new IIOImage( image, null, null), writerParams );
                                foundWriter = true;
                            }
                            catch( IIOException io )
                            {
                                //ignore exception
                            }
                            finally
                            {
                                if( imageWriter != null )
                                {
                                    imageWriter.dispose();
                                }
                            }
                        }
                        if( !foundWriter )
                        {
				bSuccess=false;
                            throw new RuntimeException( "Error: no writer found for image type '" + imageType + "'" );
				
                        }
                }
		finally
	       {
		   if( output != null )
		   {
		       output.flush();
		       output.close();
		   }
	       }
            }
        //}
	return bSuccess;
}
    
}
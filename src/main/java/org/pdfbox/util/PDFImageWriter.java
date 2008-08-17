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

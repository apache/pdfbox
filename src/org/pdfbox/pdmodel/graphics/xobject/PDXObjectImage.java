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
package org.pdfbox.pdmodel.graphics.xobject;

import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.pdfbox.cos.COSBase;
import org.pdfbox.cos.COSName;
import org.pdfbox.pdmodel.PDDocument;
import org.pdfbox.pdmodel.common.PDStream;
import org.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.pdfbox.pdmodel.graphics.color.PDColorSpaceFactory;
import org.pdfbox.pdmodel.graphics.color.PDDeviceGray;

/**
 * The prototype for all PDImages. 
 * 
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @author mathiak
 * @version $Revision: 1.9 $
 */
public abstract class PDXObjectImage extends PDXObject
{
    /**
     * The XObject subtype.
     */
    public static final String SUB_TYPE = "Image";
    
    /**
     * This contains the suffix used when writing to file.
     */
    private String suffix;
    
    /**
     * Standard constuctor. 
     * 
     * @param imageStream The XObject is passed as a COSStream.
     * @param fileSuffix The file suffix, jpg/png.
     */
    public PDXObjectImage(PDStream imageStream, String fileSuffix) 
    {
        super( imageStream );
        suffix = fileSuffix;
    }
    
    /**
     * Standard constuctor. 
     * 
     * @param doc The document to store the stream in.
     * @param fileSuffix The file suffix, jpg/png.
     */
    public PDXObjectImage(PDDocument doc, String fileSuffix) 
    {
        super( doc );
        getCOSStream().setName( COSName.SUBTYPE, SUB_TYPE );
        suffix = fileSuffix;
    }
    
    /**
     * Returns an java.awt.Image, that can be used for display etc.  
     * 
     * @return This PDF object as an AWT image.
     * 
     * @throws IOException If there is an error creating the image.
     */
    public abstract BufferedImage getRGBImage() throws IOException;
    
    /**
     * Writes the Image to out. 
     * @param out the OutputStream that the Image is written to. 
     * @throws IOException when somethings wrong with out
     */
    public abstract void write2OutputStream(OutputStream out) throws IOException;
    
    /**
     * Writes the image to a file with the filename + an appropriate suffix, like "Image.jpg". 
     * The suffix is automatically set by the   
     * @param filename the filename
     * @throws IOException When somethings wrong with the corresponding file. 
     */
    public void write2file(String filename) throws IOException 
    {
        FileOutputStream out = null;
        try
        {
            out = new FileOutputStream(filename + "." + suffix);
            write2OutputStream(out);
            out.flush();
        }
        finally
        {
            if( out != null )
            {
                out.close();
            }
        }
    }
    
    /**
     * Get the height of the image.
     * 
     * @return The height of the image.
     */
    public int getHeight()
    {
        return getCOSStream().getInt( "Height", -1 );
    }
    
    /**
     * Set the height of the image.
     * 
     * @param height The height of the image.
     */
    public void setHeight( int height )
    {
        getCOSStream().setInt( "Height", height );
    }
    
    /**
     * Get the width of the image.
     * 
     * @return The width of the image.
     */
    public int getWidth()
    {
        return getCOSStream().getInt( "Width", -1 );
    }
    
    /**
     * Set the width of the image.
     * 
     * @param width The width of the image.
     */
    public void setWidth( int width )
    {
        getCOSStream().setInt( "Width", width );
    }
    
    /**
     * The bits per component of this image.  This will return -1 if one has not
     * been set.
     *
     * @return The number of bits per component.
     */
    public int getBitsPerComponent()
    {
        return getCOSStream().getInt( new String[] { "BPC", "BitsPerComponent"}, -1 );
    }

    /**
     * Set the number of bits per component.
     *
     * @param bpc The number of bits per component.
     */
    public void setBitsPerComponent( int bpc )
    {
        getCOSStream().setInt( "BitsPerComponent", bpc );
    }
    
    /**
     * This will get the color space or null if none exists.
     *
     * @return The color space for this image.
     *
     * @throws IOException If there is an error getting the colorspace.
     */
    public PDColorSpace getColorSpace() throws IOException
    {
        COSBase cs = getCOSStream().getDictionaryObject( new String[]{ "CS", "ColorSpace" } );
        PDColorSpace retval = null;
        if( cs != null )
        {
            retval = PDColorSpaceFactory.createColorSpace( cs );
        }
        else
        {
            //there are some cases where the 'required' CS value is not present
            //but we know that it will be grayscale for a CCITT filter.
            COSBase filter = getCOSStream().getDictionaryObject( "Filter" );
            if( COSName.CCITTFAX_DECODE.equals( filter ) ||
                COSName.CCITTFAX_DECODE_ABBREVIATION.equals( filter ) )
            {
                retval = new PDDeviceGray();
            }
        }
        return retval;
    }

    /**
     * This will set the color space for this image.
     *
     * @param cs The color space for this image.
     */
    public void setColorSpace( PDColorSpace cs )
    {
        COSBase base = null;
        if( cs != null )
        {
            base = cs.getCOSObject();
        }
        getCOSStream().setItem( COSName.getPDFName( "ColorSpace" ), base );
    }
    
    /**
     * This will get the suffix for this image type, jpg/png.
     * 
     * @return The image suffix.
     */
    public String getSuffix()
    {
        return suffix;
    }
}

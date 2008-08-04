/**
 * Copyright (c) 2003-2006, www.pdfbox.org
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
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.pdfbox.filter.Filter;
import org.pdfbox.filter.FilterManager;
import org.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.pdfbox.util.ImageParameters;

/**
 * This class represents an inlined image.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.6 $
 */
public class PDInlinedImage
{
    private ImageParameters params;
    private byte[] imageData;

    /**
     * This will get the image parameters.
     *
     * @return The image parameters.
     */
    public ImageParameters getImageParameters()
    {
        return params;
    }

    /**
     * This will set the image parameters for this image.
     *
     * @param imageParams The imageParams.
     */
    public void setImageParameters( ImageParameters imageParams )
    {
        params = imageParams;
    }

    /**
     * Get the bytes for the image.
     *
     * @return The image data.
     */
    public byte[] getImageData()
    {
        return imageData;
    }

    /**
     * Set the bytes that make up the image.
     *
     * @param value The image data.
     */
    public void setImageData(byte[] value)
    {
        imageData = value;
    }

    /**
     * This will take the inlined image information and create a java.awt.Image from
     * it.
     *
     * @return The image that this object represents.
     * 
     * @throws IOException If there is an error creating the image.
     */
    public BufferedImage createImage() throws IOException
    {
        /*
         * This was the previous implementation, not sure which is better right now.
         *         byte[] transparentColors = new byte[]{(byte)0xFF,(byte)0xFF};
        byte[] colors=new byte[]{0, (byte)0xFF};
        IndexColorModel colorModel = new IndexColorModel( 1, 2, colors, colors, colors, transparentColors );
        BufferedImage image = new BufferedImage(
            params.getWidth(),
            params.getHeight(),
            BufferedImage.TYPE_BYTE_BINARY,
            colorModel );
        DataBufferByte buffer = new DataBufferByte( getImageData(), 1 );
        WritableRaster raster =
            Raster.createPackedRaster(
                buffer,
                params.getWidth(),
                params.getHeight(),
                params.getBitsPerComponent(),
                new Point(0,0) );
        image.setData( raster );
        return image;
         */
        
        
        //verify again pci32.pdf before changing below
        PDColorSpace pcs = params.getColorSpace();
        ColorModel colorModel = null;
        if(pcs != null)
        {
            colorModel =
                params.getColorSpace().createColorModel(
                        params.getBitsPerComponent() );
        }
        else 
        {
            byte[] transparentColors = new
            byte[]{(byte)0xFF,(byte)0xFF};
            byte[] colors=new byte[]{0, (byte)0xFF};
            colorModel = new IndexColorModel( 1, 2,
                    colors, colors, colors, transparentColors );
        } 
        List filters = params.getFilters();
        byte[] finalData = null;
        if( filters == null )
        {
            finalData = getImageData();
        }
        else
        {
            ByteArrayInputStream in = new ByteArrayInputStream( getImageData() );
            ByteArrayOutputStream out = new ByteArrayOutputStream(getImageData().length);
            FilterManager filterManager = new FilterManager();
            for( int i=0; i<filters.size(); i++ )
            {
                out.reset();
                Filter filter = filterManager.getFilter( (String)filters.get( i ) );
                filter.decode( in, out, params.getDictionary(), i );
                in = new ByteArrayInputStream( out.toByteArray() ); 
            }
            finalData = out.toByteArray();
        }

        WritableRaster raster = colorModel.createCompatibleWritableRaster( params.getWidth(), params.getHeight() );
        /*    Raster.createPackedRaster(
                buffer,
                params.getWidth(),
                params.getHeight(),
                params.getBitsPerComponent(),
                new Point(0,0) );
                */
        DataBuffer rasterBuffer = raster.getDataBuffer(); 
        if( rasterBuffer instanceof DataBufferByte )
        {
            DataBufferByte byteBuffer = (DataBufferByte)rasterBuffer;
            byte[] data = byteBuffer.getData();
            System.arraycopy( finalData, 0, data, 0, data.length );
        }
        else if( rasterBuffer instanceof DataBufferInt )
        {
            DataBufferInt byteBuffer = (DataBufferInt)rasterBuffer;
            int[] data = byteBuffer.getData();
            for( int i=0; i<finalData.length; i++ )
            {
                data[i] = (finalData[i]+256)%256;
            }
        }
        BufferedImage image = new BufferedImage(
                colorModel, raster, false, null );
        image.setData( raster );
        return image;
    }
}
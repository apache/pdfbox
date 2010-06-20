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
package org.apache.pdfbox.pdmodel.graphics.color;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.cos.COSString;

import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;

import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * This class represents an Indexed color space.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.4 $
 */
public class PDIndexed extends PDColorSpace
{

    /**
     * The name of this color space.
     */
    public static final String NAME = "Indexed";

    /**
     * The abbreviated name of this color space.
     */
    public static final String ABBREVIATED_NAME = "I";

    private COSArray array;

    /**
     * The lookup data as byte array.
     */
    private byte[] lookupData;
    
    /**
     * Constructor, default DeviceRGB, hival 255.
     */
    public PDIndexed()
    {
        array = new COSArray();
        array.add( COSName.INDEXED );
        array.add( COSName.DEVICERGB );
        array.add( COSInteger.get( 255 ) );
        array.add( org.apache.pdfbox.cos.COSNull.NULL );
    }

    /**
     * Constructor.
     *
     * @param indexedArray The array containing the indexed parameters
     */
    public PDIndexed( COSArray indexedArray )
    {
        array = indexedArray;
    }

    /**
     * This will return the number of color components.  This will return the
     * number of color components in the base color.
     *
     * @return The number of components in this color space.
     *
     * @throws IOException If there is an error getting the number of color components.
     */
    public int getNumberOfComponents() throws IOException
    {
        return getBaseColorSpace().getNumberOfComponents();
    }

    /**
     * This will return the name of the color space.
     *
     * @return The name of the color space.
     */
    public String getName()
    {
        return NAME;
    }

    /**
     * Create a Java colorspace for this colorspace.
     *
     * @return A color space that can be used for Java AWT operations.
     *
     * @throws IOException If there is an error creating the color space.
     */
    protected ColorSpace createColorSpace() throws IOException
    {
        throw new IOException( "Not implemented" );
    }

    /**
     * Create a Java color model for this colorspace.
     *
     * @param bpc The number of bits per component.
     *
     * @return A color model that can be used for Java AWT operations.
     *
     * @throws IOException If there is an error creating the color model.
     */
    public ColorModel createColorModel( int bpc ) throws IOException
    {
        int size = getHighValue();
        byte[] index = getLookupData();
        PDColorSpace baseColorSpace = getBaseColorSpace();
        ColorModel cm = null;
        if( baseColorSpace instanceof PDDeviceRGB )
        {
            cm = new IndexColorModel(bpc, size+1, index,0,false);
        }
        else
        {
            ColorModel baseColorModel = baseColorSpace.createColorModel(bpc);
            if( baseColorModel.getTransferType() != DataBuffer.TYPE_BYTE )
            {
                throw new IOException( "Not implemented" );
            }
            byte[] r = new byte[size+1];
            byte[] g = new byte[size+1];
            byte[] b = new byte[size+1];
            byte[] a = baseColorModel.hasAlpha() ? new byte[size+1] : null;
            byte[] inData = new byte[baseColorModel.getNumComponents()];
            for( int i = 0; i <= size; i++ )
            {
                System.arraycopy(index, i * inData.length, inData, 0, inData.length);
                r[i] = (byte)baseColorModel.getRed(inData);
                g[i] = (byte)baseColorModel.getGreen(inData);
                b[i] = (byte)baseColorModel.getBlue(inData);
                if(a != null)
                {
                    a[i] = (byte)baseColorModel.getAlpha(inData);
                }
            }
            cm = a == null ? new IndexColorModel(bpc, size+1, r, g, b) : new IndexColorModel(bpc, size+1, r, g, b, a);
        }
        return cm;
    }

    /**
     * This will get the color space that acts as the index for this color space.
     *
     * @return The base color space.
     *
     * @throws IOException If there is error creating the base color space.
     */
    public PDColorSpace getBaseColorSpace() throws IOException
    {
        COSBase base = array.getObject( 1 );
        return PDColorSpaceFactory.createColorSpace( base );
    }

    /**
     * This will set the base color space.
     *
     * @param base The base color space to use as the index.
     */
    public void setBaseColorSpace( PDColorSpace base )
    {
        array.set( 1, base.getCOSObject() );
    }

    /**
     * Get the highest value for the lookup.
     *
     * @return The hival entry.
     */
    public int getHighValue()
    {
        return ((COSNumber)array.getObject( 2 )).intValue();
    }

    /**
     * This will set the highest value that is allowed.  This cannot be higher
     * than 255.
     *
     * @param high The highest value for the lookup table.
     */
    public void setHighValue( int high )
    {
        array.set( 2, high );
    }

    /**
     * This will perform a lookup into the color lookup table.
     *
     * @param lookupIndex The zero-based index into the table, should not exceed the high value.
     * @param componentNumber The component number, probably 1,2,3,3.
     *
     * @return The value that was from the lookup table.
     *
     * @throws IOException If there is an error looking up the color.
     */
    public int lookupColor( int lookupIndex, int componentNumber ) throws IOException
    {
        PDColorSpace baseColor = getBaseColorSpace();
        byte[] data = getLookupData();
        int numberOfComponents = baseColor.getNumberOfComponents();
        return (data[lookupIndex*numberOfComponents + componentNumber]+256)%256;
    }

    /**
     * Get the lookup data table.
     * 
     * @return a byte array containing the the lookup data.
     * @throws IOException if an error occurs.
     */
    public byte[] getLookupData() throws IOException
    {
        if ( lookupData == null)
        {
            COSBase lookupTable = array.getObject( 3 );
            if( lookupTable instanceof COSString )
            {
                lookupData = ((COSString)lookupTable).getBytes();
            }
            else if( lookupTable instanceof COSStream )
            {
                //Data will be small so just load the whole thing into memory for
                //easier processing
                COSStream lookupStream = (COSStream)lookupTable;
                InputStream input = lookupStream.getUnfilteredStream();
                ByteArrayOutputStream output = new ByteArrayOutputStream(1024);
                byte[] buffer = new byte[ 1024 ];
                int amountRead;
                while( (amountRead = input.read(buffer, 0, buffer.length)) != -1 )
                {
                    output.write( buffer, 0, amountRead );
                }
                lookupData = output.toByteArray();
            }
            else if( lookupTable == null )
            {
                lookupData = new byte[0];
            }
            else
            {
                throw new IOException( "Error: Unknown type for lookup table " + lookupTable );
            }
        }
        return lookupData;
    }

    /**
     * This will set a color in the color lookup table.
     *
     * @param lookupIndex The zero-based index into the table, should not exceed the high value.
     * @param componentNumber The component number, probably 1,2,3,3.
     * @param color The color that will go into the table.
     *
     * @throws IOException If there is an error looking up the color.
     */
    public void setLookupColor( int lookupIndex, int componentNumber, int color ) throws IOException
    {
        PDColorSpace baseColor = getBaseColorSpace();
        int numberOfComponents = baseColor.getNumberOfComponents();
        byte[] data = getLookupData();
        data[lookupIndex*numberOfComponents + componentNumber] = (byte)color;
        COSString string = new COSString( data );
        array.set( 3, string );
    }
}

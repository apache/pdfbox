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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.COSArrayList;
import org.apache.pdfbox.pdmodel.common.PDRange;
import org.apache.pdfbox.pdmodel.common.PDStream;

import java.awt.Color;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;

import java.io.InputStream;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a ICC profile color space.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.6 $
 */
public class PDICCBased extends PDColorSpace
{
   
    
    /**
     * Log instance.
     */
    private static final Log LOG = LogFactory.getLog(PDICCBased.class);

    /**
     * The name of this color space.
     */
    public static final String NAME = "ICCBased";

    //private COSArray array;
    private PDStream stream;

    /**
     *  Number of color components.
     */
    private int numberOfComponents = -1;
    
    /**
     * Default constructor, creates empty stream.
     *
     * @param doc The document to store the icc data.
     */
    public PDICCBased( PDDocument doc )
    {
        array = new COSArray();
        array.add( COSName.ICCBASED );
        array.add( new PDStream( doc ) );
    }

    /**
     * Constructor.
     *
     * @param iccArray The ICC stream object.
     */
    public PDICCBased( COSArray iccArray )
    {
        array = iccArray;
        stream = new PDStream( (COSStream)iccArray.getObject( 1 ) );
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
     * Convert this standard java object to a COS object.
     *
     * @return The cos object that matches this Java object.
     */
    public COSBase getCOSObject()
    {
        return array;
    }

    /**
     * Get the pd stream for this icc color space.
     *
     * @return Get the stream for this icc based color space.
     */
    public PDStream getPDStream()
    {
        return stream;
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
        InputStream profile = null;
        ColorSpace cSpace = null;
        try
        {
            profile = stream.createInputStream();
            ICC_Profile iccProfile = ICC_Profile.getInstance( profile );
            cSpace = new ICC_ColorSpace( iccProfile );
            float[] components = new float[numberOfComponents];
            // there maybe a ProfileDataException or a CMMException as there
            // are some issues when loading ICC_Profiles, see PDFBOX-1295
            // Try to create a color as test ...
            new Color(cSpace,components,1f);
        }
        catch (RuntimeException e)
        {
            // we are using an alternate colorspace as fallback
            LOG.debug("Can't read ICC-profile, using alternate colorspace instead");
            List alternateCSList = getAlternateColorSpaces();
            PDColorSpace alternate = (PDColorSpace)alternateCSList.get(0);
            cSpace = alternate.getJavaColorSpace();
        }
        finally
        {
            if( profile != null )
            {
                profile.close();
            }
        }
        return cSpace;
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

            int[] nbBits;
            int numOfComponents = getNumberOfComponents();
            switch (numOfComponents) 
            {
                case 1:
                    // DeviceGray
                    nbBits = new int[]{ bpc };
                    break;
                case 3:
                    // DeviceRGB
                    nbBits = new int[]{ bpc, bpc, bpc };
                    break;
                case 4:
                    // DeviceCMYK
                    nbBits = new int[]{ bpc, bpc, bpc, bpc };
                    break;
                default:
                    throw new IOException( "Unknown colorspace number of components:" + numOfComponents );
            }
            ComponentColorModel componentColorModel =
                    new ComponentColorModel( getJavaColorSpace(),
                                             nbBits,
                                             false,
                                             false,
                                             Transparency.OPAQUE,
                                             DataBuffer.TYPE_BYTE );
            return componentColorModel;
        
    }

    /**
     * This will return the number of color components.  As of PDF 1.4 this will
     * be 1,3,4.
     *
     * @return The number of components in this color space.
     *
     * @throws IOException If there is an error getting the number of color components.
     */
    public int getNumberOfComponents() throws IOException
    {
        if (numberOfComponents < 0)
        {
            numberOfComponents = stream.getStream().getInt(COSName.N);
        }
        return numberOfComponents;
    }

    /**
     * This will set the number of color components.
     *
     * @param n The number of color components.
     */
    public void setNumberOfComponents( int n )
    {
        numberOfComponents = n;
        stream.getStream().setInt( COSName.N, n );
    }

    /**
     * This will return a list of alternate color spaces(PDColorSpace) if the display application
     * does not support this icc stream.
     *
     * @return A list of alternate color spaces.
     *
     * @throws IOException If there is an error getting the alternate color spaces.
     */
    public List getAlternateColorSpaces() throws IOException
    {
        COSBase alternate = stream.getStream().getDictionaryObject( COSName.ALTERNATE );
        COSArray alternateArray = null;
        if( alternate == null )
        {
            alternateArray = new COSArray();
            int numComponents = getNumberOfComponents();
            COSName csName = null;
            if( numComponents == 1 )
            {
                csName = COSName.DEVICEGRAY;
            }
            else if( numComponents == 3 )
            {
                csName = COSName.DEVICERGB;
            }
            else if( numComponents == 4 )
            {
                csName = COSName.DEVICECMYK;
            }
            else
            {
                throw new IOException( "Unknown colorspace number of components:" + numComponents );
            }
            alternateArray.add( csName );
        }
        else
        {
            if( alternate instanceof COSArray )
            {
                alternateArray = (COSArray)alternate;
            }
            else if( alternate instanceof COSName )
            {
                alternateArray = new COSArray();
                alternateArray.add( alternate );
            }
            else
            {
                throw new IOException( "Error: expected COSArray or COSName and not " +
                    alternate.getClass().getName() );
            }
        }
        List retval = new ArrayList();
        for( int i=0; i<alternateArray.size(); i++ )
        {
            retval.add( PDColorSpaceFactory.createColorSpace( alternateArray.get( i ) ) );
        }
        return new COSArrayList( retval, alternateArray );
    }

    /**
     * This will set the list of alternate color spaces.  This should be a list
     * of PDColorSpace objects.
     *
     * @param list The list of colorspace objects.
     */
    public void setAlternateColorSpaces( List list )
    {
        COSArray altArray = null;
        if( list != null )
        {
            altArray = COSArrayList.converterToCOSArray( list );
        }
        stream.getStream().setItem( COSName.ALTERNATE, altArray );
    }

    private COSArray getRangeArray( int n )
    {
        COSArray rangeArray = (COSArray)stream.getStream().getDictionaryObject( COSName.RANGE);
        if( rangeArray == null )
        {
            rangeArray = new COSArray();
            stream.getStream().setItem( COSName.RANGE, rangeArray );
            while( rangeArray.size() < n*2 )
            {
                rangeArray.add( new COSFloat( -100 ) );
                rangeArray.add( new COSFloat( 100 ) );
            }
        }
        return rangeArray;
    }

    /**
     * This will get the range for a certain component number.  This is will never
     * return null.  If it is not present then the range -100 to 100 will
     * be returned.
     *
     * @param n The component number to get the range for.
     *
     * @return The range for this component.
     */
    public PDRange getRangeForComponent( int n )
    {
        COSArray rangeArray = getRangeArray( n );
        return new PDRange( rangeArray, n );
    }

    /**
     * This will set the a range for this color space.
     *
     * @param range The new range for the a component.
     * @param n The component to set the range for.
     */
    public void setRangeForComponent( PDRange range, int n )
    {
        COSArray rangeArray = getRangeArray( n );
        rangeArray.set( n*2, new COSFloat( range.getMin() ) );
        rangeArray.set( n*2+1, new COSFloat( range.getMax() ) );
    }

    /**
     * This will get the metadata stream for this object.  Null if there is no
     * metadata stream.
     *
     * @return The metadata stream, if it exists.
     */
    public COSStream getMetadata()
    {
        return (COSStream)stream.getStream().getDictionaryObject( COSName.METADATA );
    }

    /**
     * This will set the metadata stream that is associated with this color space.
     *
     * @param metadata The new metadata stream.
     */
    public void setMetadata( COSStream metadata )
    {
        stream.getStream().setItem( COSName.METADATA, metadata );
    }
    
    // Need more info on the ICCBased ones ... Array contains very little.
    /**
     * {@inheritDoc}
     */
    public String toString()
    {
        String retVal = super.toString() + "\n\t Number of Components: ";
        try
        {
            retVal = retVal + getNumberOfComponents();
        }
        catch (IOException exception)
        {
            retVal = retVal + exception.toString();
        }
        return retVal;
    }
}

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

import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.COSArrayList;
import org.apache.pdfbox.pdmodel.common.PDRange;
import org.apache.pdfbox.pdmodel.common.PDStream;

import java.awt.Color;
import java.awt.color.CMMException;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.color.ProfileDataException;
import java.awt.image.BufferedImage;

import java.awt.image.WritableRaster;
import java.io.InputStream;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

/**
 * ICCBased colour spaces are based on a cross-platform colour profile as defined by the
 * International Color Consortium (ICC).
 *
 * @author Ben Litchfield
 * @author John Hewson
 */
public final class PDICCBased extends PDCIEBasedColorSpace
{
    private static final Log LOG = LogFactory.getLog(PDICCBased.class);

    private PDStream stream;
    private int numberOfComponents = -1;
    private ICC_Profile iccProfile;
    private PDColorSpace alternateColorSpace;
    private ICC_ColorSpace awtColorSpace;
    private PDColor initialColor;

    /**
     * Creates a new ICC color space with an empty stream.
     * @param doc the document to store the ICC data
     */
    public PDICCBased(PDDocument doc)
    {
        array = new COSArray();
        array.add(COSName.ICCBASED);
        array.add(new PDStream(doc));
    }

    /**
     * Creates a new ICC color space using the PDF array.
     * @param iccArray the ICC stream object
     */
    public PDICCBased(COSArray iccArray) throws IOException
    {
        array = iccArray;
        stream = new PDStream((COSStream)iccArray.getObject(1));
        loadICCProfile();
    }

    @Override
    public String getName()
    {
        return COSName.ICCBASED.getName();
    }

    @Override
    public COSBase getCOSObject()
    {
        return array;
    }

    /**
     * Get the underlying ICC profile stream.
     * @return the underlying ICC profile stream
     */
    public PDStream getPDStream()
    {
        return stream;
    }

    // load the ICC profile, or init alternateColorSpace color space
    private void loadICCProfile() throws IOException
    {
        InputStream profile = null;
        try
        {
            profile = stream.createInputStream();
            iccProfile = ICC_Profile.getInstance(profile);
            awtColorSpace = new ICC_ColorSpace(iccProfile);

            // set initial colour
            float[] initial = new float[getColorSpaceType()];
            for (int c = 0; c < getNumberOfComponents(); c++)
            {
                initial[c] = Math.max(0, getRangeForComponent(c).getMin());
            }
            initialColor = new PDColor(initial);

            // create a color in order to trigger a ProfileDataException
            // or CMMException due to invalid profiles, see PDFBOX-1295 and PDFBOX-1740
            new Color(awtColorSpace, new float[getNumberOfComponents()], 1f);
        }
        catch (RuntimeException e)
        {
            if (e instanceof ProfileDataException || e instanceof CMMException)
            {
                // fall back to alternateColorSpace color space
                LOG.error("Can't read embedded ICC profile, using alternate color space");
                awtColorSpace = null;
                alternateColorSpace = getAlternateColorSpaces().get(0);
                initialColor = alternateColorSpace.getInitialColor();
            }
            else
            {
                throw e;
            }
        }
        finally
        {
            IOUtils.closeQuietly(profile);
        }
    }

    @Override
    public float[] toRGB(float[] value) throws IOException
    {
        if (awtColorSpace != null)
        {
            // WARNING: toRGB is very slow when used with LUT-based ICC profiles
            return awtColorSpace.toRGB(value);
        }
        else
        {
            return alternateColorSpace.toRGB(value);
        }
    }

    @Override
    public BufferedImage toRGBImage(WritableRaster raster) throws IOException
    {
        if (awtColorSpace != null)
        {
            return toRGBImageAWT(raster, awtColorSpace);
        }
        else
        {
            return alternateColorSpace.toRGBImage(raster);
        }
    }

    @Override
    public int getNumberOfComponents()
    {
        if (numberOfComponents < 0)
        {
            numberOfComponents = stream.getStream().getInt(COSName.N);
        }
        return numberOfComponents;
    }

    @Override
    public float[] getDefaultDecode()
    {
        if (awtColorSpace != null)
        {
            int n = getNumberOfComponents();
            float[] decode = new float[n * 2];
            for (int i = 0; i < n; i++)
            {
                decode[i * 2] = awtColorSpace.getMinValue(i);
                decode[i * 2 + 1] = awtColorSpace.getMaxValue(i);
            }
            return decode;
        }
        else
        {
            return alternateColorSpace.getDefaultDecode();
        }
    }

    @Override
    public PDColor getInitialColor()
    {
        return initialColor;
    }

    /**
     * Returns a list of alternate color spaces for non-conforming readers.
     * WARNING: Do not use the information in a conforming reader.
     * @return A list of alternateColorSpace color spaces.
     * @throws IOException If there is an error getting the alternateColorSpace color spaces.
     */
    public List<PDColorSpace> getAlternateColorSpaces() throws IOException
    {
        COSBase alternate = stream.getStream().getDictionaryObject(COSName.ALTERNATE);
        COSArray alternateArray;
        if(alternate == null)
        {
            alternateArray = new COSArray();
            int numComponents = getNumberOfComponents();
            COSName csName;
            if(numComponents == 1)
            {
                csName = COSName.DEVICEGRAY;
            }
            else if(numComponents == 3)
            {
                csName = COSName.DEVICERGB;
            }
            else if(numComponents == 4)
            {
                csName = COSName.DEVICECMYK;
            }
            else
            {
                throw new IOException("Unknown color space number of components:" + numComponents);
            }
            alternateArray.add(csName);
        }
        else
        {
            if(alternate instanceof COSArray)
            {
                alternateArray = (COSArray)alternate;
            }
            else if(alternate instanceof COSName)
            {
                alternateArray = new COSArray();
                alternateArray.add(alternate);
            }
            else
            {
                throw new IOException("Error: expected COSArray or COSName and not " +
                    alternate.getClass().getName());
            }
        }
        List<PDColorSpace> list = new ArrayList<PDColorSpace>();
        for(int i=0; i<alternateArray.size(); i++)
        {
            list.add(PDColorSpace.create(alternateArray.get(i)));
        }
        return list;
    }

    private COSArray getRangeArray(int n)
    {
        COSArray rangeArray = (COSArray)stream.getStream().getDictionaryObject(COSName.RANGE);
        if(rangeArray == null)
        {
            rangeArray = new COSArray();
            stream.getStream().setItem(COSName.RANGE, rangeArray);
            while(rangeArray.size() < n*2)
            {
                rangeArray.add(new COSFloat(-100));
                rangeArray.add(new COSFloat(100));
            }
        }
        return rangeArray;
    }

    /**
     * Returns the range for a certain component number.
     * This is will never return null.
     * If it is not present then the range 0..1 will be returned.
     * @param n the component number to get the range for
     * @return the range for this component
     */
    public PDRange getRangeForComponent(int n)
    {
        COSArray rangeArray = getRangeArray(n);

        if (rangeArray.size() == 0)
        {
            return new PDRange(); // 0..1
        }
        return new PDRange(rangeArray, n);
    }

    /**
     * Returns the metadata stream for this object, or null if there is no metadata stream.
     * @return the metadata stream, or null if there is none
     */
    public COSStream getMetadata()
    {
        return (COSStream)stream.getStream().getDictionaryObject(COSName.METADATA);
    }

    /**
     * Returns the type of the color space in the ICC profile.
     * Will be one of {@code TYPE_GRAY}, {@code TYPE_RGB}, or {@code TYPE_CMYK}.
     * @return an ICC color space type
     */
    public int getColorSpaceType()
    {
        return iccProfile.getColorSpaceType();
    }

    /**
     * Sets the number of color components.
     * @param n the number of color components
     */
    // TODO it's probably not safe to use this
    @Deprecated
    public void setNumberOfComponents(int n)
    {
        numberOfComponents = n;
        stream.getStream().setInt(COSName.N, n);
    }

    /**
     * Sets the list of alternateColorSpace color spaces.
     * This should be a list of PDColorSpace objects.
     * @param list the list of color space objects
     */
    public void setAlternateColorSpaces(List list)
    {
        COSArray altArray = null;
        if(list != null)
        {
            altArray = COSArrayList.converterToCOSArray(list);
        }
        stream.getStream().setItem(COSName.ALTERNATE, altArray);
    }

    /**
     * Sets the range for this color space.
     * @param range the new range for the a component
     * @param n the component to set the range for
     */
    public void setRangeForComponent(PDRange range, int n)
    {
        COSArray rangeArray = getRangeArray(n);
        rangeArray.set(n*2, new COSFloat(range.getMin()));
        rangeArray.set(n*2+1, new COSFloat(range.getMax()));
    }
    
    /**
     * Sets the metadata stream that is associated with this color space.
     * @param metadata the new metadata stream
     */
    public void setMetadata(COSStream metadata)
    {
        stream.getStream().setItem(COSName.METADATA, metadata);
    }

    @Override
    public String toString()
    {
        return getName() + "{numberOfComponents: " + getNumberOfComponents() + "}";
    }
}

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

import java.awt.Color;
import java.awt.color.ColorSpace;
import java.util.Arrays;
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

import java.awt.color.CMMException;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.color.ProfileDataException;
import java.awt.image.BufferedImage;

import java.awt.image.WritableRaster;
import java.io.InputStream;
import java.io.IOException;

import java.util.List;
import org.apache.pdfbox.util.Charsets;

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

    private final PDStream stream;
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
        stream = new PDStream(doc);
        array.add(stream);
    }

    /**
     * Creates a new ICC color space using the PDF array.
     *
     * @param iccArray the ICC stream object
     * @throws java.io.IOException if there is an error reading the ICC profile.
     */
    public PDICCBased(COSArray iccArray) throws IOException
    {
        array = iccArray;
        stream = new PDStream((COSStream) iccArray.getObject(1));
        loadICCProfile();
    }

    @Override
    public String getName()
    {
        return COSName.ICCBASED.getName();
    }

    /**
     * Get the underlying ICC profile stream.
     * @return the underlying ICC profile stream
     */
    public PDStream getPDStream()
    {
        return stream;
    }

    /**
     * Load the ICC profile, or init alternateColorSpace color space.
     */
    private void loadICCProfile() throws IOException
    {
        InputStream input = null;
        try
        {
            input = this.stream.createInputStream();

            // if the embedded profile is sRGB then we can use Java's built-in profile, which
            // results in a large performance gain as it's our native color space, see PDFBOX-2587
            ICC_Profile profile;
            synchronized (LOG)
            {
                profile = ICC_Profile.getInstance(input);
                if (is_sRGB(profile))
                {
                    awtColorSpace = (ICC_ColorSpace) ColorSpace.getInstance(ColorSpace.CS_sRGB);
                    iccProfile = awtColorSpace.getProfile();
                }
                else
                {
                    awtColorSpace = new ICC_ColorSpace(profile);
                    iccProfile = profile;
                }

                // set initial colour
                float[] initial = new float[getNumberOfComponents()];
                for (int c = 0; c < getNumberOfComponents(); c++)
                {
                    initial[c] = Math.max(0, getRangeForComponent(c).getMin());
                }
                initialColor = new PDColor(initial, this);

                // do things that trigger a ProfileDataException
                // or CMMException due to invalid profiles, see PDFBOX-1295 and PDFBOX-1740
                // or ArrayIndexOutOfBoundsException, see PDFBOX-3610
                awtColorSpace.fromRGB(new float[3]);
                awtColorSpace.toRGB(new float[awtColorSpace.getNumComponents()]);
                awtColorSpace.fromCIEXYZ(new float[3]);
                awtColorSpace.toCIEXYZ(new float[awtColorSpace.getNumComponents()]);
                // this one triggers an exception for PDFBOX-3549 with KCMS
                new Color(awtColorSpace, new float[getNumberOfComponents()], 1f);
            }
        }
        catch (RuntimeException e)
        {
            if (e instanceof ProfileDataException ||
                e instanceof CMMException ||
                e instanceof IllegalArgumentException ||
                e instanceof ArrayIndexOutOfBoundsException)
            {
                // fall back to alternateColorSpace color space
                awtColorSpace = null;
                alternateColorSpace = getAlternateColorSpace();
                LOG.error("Can't read embedded ICC profile (" + e.getLocalizedMessage() + "), using alternate color space: " + alternateColorSpace.getName());
                initialColor = alternateColorSpace.getInitialColor();
            }
            else
            {
                throw e;
            }
        }
        finally
        {
            IOUtils.closeQuietly(input);
        }
    }

    /**
     * Returns true if the given profile is represents sRGB.
     */
    private boolean is_sRGB(ICC_Profile profile)
    {
        byte[] bytes = Arrays.copyOfRange(profile.getData(ICC_Profile.icSigHead),
                ICC_Profile.icHdrModel, ICC_Profile.icHdrModel + 7);
        String deviceModel = new String(bytes, Charsets.US_ASCII).trim();
        return deviceModel.equals("sRGB");
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
            numberOfComponents = stream.getCOSObject().getInt(COSName.N);
        }
        return numberOfComponents;
    }

    @Override
    public float[] getDefaultDecode(int bitsPerComponent)
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
            return alternateColorSpace.getDefaultDecode(bitsPerComponent);
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
    public PDColorSpace getAlternateColorSpace() throws IOException
    {
        COSBase alternate = stream.getCOSObject().getDictionaryObject(COSName.ALTERNATE);
        COSArray alternateArray;
        if(alternate == null)
        {
            alternateArray = new COSArray();
            int numComponents = getNumberOfComponents();
            COSName csName;
            switch (numComponents)
            {
                case 1:
                    csName = COSName.DEVICEGRAY;
                    break;
                case 3:
                    csName = COSName.DEVICERGB;
                    break;
                case 4:
                    csName = COSName.DEVICECMYK;
                    break;
                default:
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
        return PDColorSpace.create(alternateArray);
    }

    /**
     * Returns the range for a certain component number.
     * This will never return null.
     * If it is not present then the range 0..1 will be returned.
     * @param n the component number to get the range for
     * @return the range for this component
     */
    public PDRange getRangeForComponent(int n)
    {
        COSArray rangeArray = (COSArray) stream.getCOSObject().getDictionaryObject(COSName.RANGE);
        if (rangeArray == null || rangeArray.size() < getNumberOfComponents() * 2)
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
        return (COSStream)stream.getCOSObject().getDictionaryObject(COSName.METADATA);
    }

    /**
     * Returns the type of the color space in the ICC profile.
     * Will be one of {@code TYPE_GRAY}, {@code TYPE_RGB}, or {@code TYPE_CMYK}.
     * @return an ICC color space type
     */
    public int getColorSpaceType()
    {
        if (iccProfile != null)
        {
            return iccProfile.getColorSpaceType();
        }
        else
        {
            // if the ICC Profile could not be read
            if (alternateColorSpace.getNumberOfComponents() == 1)
            {
                return ICC_ColorSpace.TYPE_GRAY;
            }
            else if (alternateColorSpace.getNumberOfComponents() == 3)
            {
                return ICC_ColorSpace.TYPE_RGB;
            }
            else if (alternateColorSpace.getNumberOfComponents() == 4)
            {
                return ICC_ColorSpace.TYPE_CMYK;
            }
            else
            {
                // should not happen as all ICC color spaces in PDF must have 1,3, or 4 components
                return -1;
            }
        }
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
        stream.getCOSObject().setInt(COSName.N, n);
    }

    /**
     * Sets the list of alternateColorSpace color spaces.
     *
     * @param list the list of color space objects
     */
    public void setAlternateColorSpaces(List<PDColorSpace> list)
    {
        COSArray altArray = null;
        if(list != null)
        {
            altArray = COSArrayList.converterToCOSArray(list);
        }
        stream.getCOSObject().setItem(COSName.ALTERNATE, altArray);
    }

    /**
     * Sets the range for this color space.
     * @param range the new range for the a component
     * @param n the component to set the range for
     */
    public void setRangeForComponent(PDRange range, int n)
    {
        COSArray rangeArray = (COSArray) stream.getCOSObject().getDictionaryObject(COSName.RANGE);
        if (rangeArray == null)
        {
            rangeArray = new COSArray();
            stream.getCOSObject().setItem(COSName.RANGE, rangeArray);
        }
        // extend range array with default values if needed
        while (rangeArray.size() < (n + 1) * 2)
        {
            rangeArray.add(new COSFloat(0));
            rangeArray.add(new COSFloat(1));
        }
        rangeArray.set(n*2, new COSFloat(range.getMin()));
        rangeArray.set(n*2+1, new COSFloat(range.getMax()));
    }
    
    /**
     * Sets the metadata stream that is associated with this color space.
     * @param metadata the new metadata stream
     */
    public void setMetadata(COSStream metadata)
    {
        stream.getCOSObject().setItem(COSName.METADATA, metadata);
    }

    @Override
    public String toString()
    {
        return getName() + "{numberOfComponents: " + getNumberOfComponents() + "}";
    }
}

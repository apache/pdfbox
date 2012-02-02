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
package org.apache.pdfbox.pdmodel.graphics.shading;



import java.io.IOException;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.common.COSObjectable;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpaceFactory;

/**
 * This represents resources for a shading.
 *
 * @version $Revision: 1.0 $
 */
public abstract class PDShadingResources implements COSObjectable
{
    private COSDictionary dictionary;
    private COSArray background = null;
    private PDRectangle bBox = null;
    private PDColorSpace colorspace = null;
    
    /**
     * shading type 1 = function based shading.
     */
    public static final int SHADING_TYPE1 = 1;
    /**
     * shading type 2 = axial shading.
     */
    public static final int SHADING_TYPE2 = 2;
    /**
     * shading type 3 = radial shading.
     */
    public static final int SHADING_TYPE3 = 3;
    /**
     * shading type 4 = Free-Form Gouraud-Shaded Triangle Meshes.
     */
    public static final int SHADING_TYPE4 = 4;
    /**
     * shading type 5 = Lattice-Form Gouraud-Shaded Triangle Meshes.
     */
    public static final int SHADING_TYPE5 = 5;
    /**
     * shading type 6 = Coons Patch Meshes.
     */
    public static final int SHADING_TYPE6 = 6;
    /**
     * shading type 7 = Tensor-Product Patch Meshes.
     */
    public static final int SHADING_TYPE7 = 7;
    
    /**
     * Default constructor.
     */
    public PDShadingResources()
    {
        dictionary = new COSDictionary();
    }

    /**
     * Constructor using the given shading dictionary.
     *
     * @param shadingDictionary The dictionary for this shading.
     */
    public PDShadingResources( COSDictionary shadingDictionary )
    {
        dictionary = shadingDictionary;
    }

    /**
     * This will get the underlying dictionary.
     *
     * @return The dictionary for this shading.
     */
    public COSDictionary getCOSDictionary()
    {
        return dictionary;
    }

    /**
     * Convert this standard java object to a COS object.
     *
     * @return The cos object that matches this Java object.
     */
    public COSBase getCOSObject()
    {
        return dictionary;
    }

    /**
     * This will return the type.
     *
     * @return The type of object that this is.
     */
    public String getType()
    {
        return COSName.SHADING.getName();
    }

    /**
     * This will set the shading type.
     *
     * @param shadingType The new shading type.
     */
    public void setShadingType(int shadingType)
    {
        dictionary.setInt(COSName.SHADING_TYPE, shadingType);
    }

    /**
     * This will return the shading type.
     *
     * @return The shading type
     */
    public abstract int getShadingType();

    /**
     * This will set the background.
     *
     * @param newBackground The new background.
     */
    public void setBackground(COSArray newBackground)
    {
        background = newBackground;
        dictionary.setItem(COSName.BACKGROUND, newBackground);
    }

    /**
     * This will return the background.
     *
     * @return The background
     */
    public COSArray getBackground()
    {
        if (background == null) 
        {
            background = (COSArray)dictionary.getDictionaryObject( COSName.BACKGROUND );
        }
        return background;
    }

    /**
     * An array of four numbers in the form coordinate system (see
     * below), giving the coordinates of the left, bottom, right, and top edges,
     * respectively, of the shadings's bounding box.
     *
     * @return The BBox of the form.
     */
    public PDRectangle getBBox()
    {
        if (bBox == null) 
        {
            COSArray array = (COSArray)dictionary.getDictionaryObject( COSName.BBOX );
            if( array != null )
            {
                bBox = new PDRectangle( array );
            }
        }
        return bBox;
    }

    /**
     * This will set the BBox (bounding box) for this Shading.
     *
     * @param newBBox The new BBox.
     */
    public void setBBox(PDRectangle newBBox)
    {
        bBox = newBBox;
        if( bBox == null )
        {
            dictionary.removeItem( COSName.BBOX );
        }
        else
        {
            dictionary.setItem( COSName.BBOX, bBox.getCOSArray() );
        }
    }

    /**
     * This will set the AntiAlias value.
     *
     * @param antiAlias The new AntiAlias value.
     */
    public void setAntiAlias(boolean antiAlias)
    {
        dictionary.setBoolean(COSName.ANTI_ALIAS, antiAlias);
    }

    /**
     * This will return the AntiAlias value.
     *
     * @return The AntiAlias value
     */
    public boolean getAntiAlias()
    {
        return dictionary.getBoolean( COSName.ANTI_ALIAS, false );
    }

    /**
     * This will get the color space or null if none exists.
     *
     * @return The color space for the shading.
     *
     * @throws IOException If there is an error getting the colorspace.
     */
    public PDColorSpace getColorSpace() throws IOException
    {
        if( colorspace == null )
        {
            COSBase colorSpaceDictionary = dictionary.getDictionaryObject( COSName.CS, COSName.COLORSPACE );
            colorspace = PDColorSpaceFactory.createColorSpace( colorSpaceDictionary );
        }
        return colorspace;
    }

    /**
     * This will set the color space for the shading.
     *
     * @param newColorspace The color space
     */
    public void setColorSpace( PDColorSpace newColorspace )
    {
        colorspace = newColorspace;
        if( newColorspace != null )
        {
            dictionary.setItem( COSName.COLORSPACE, newColorspace.getCOSObject() );
        }
        else
        {
            dictionary.removeItem( COSName.COLORSPACE );
        }
    }

    /**
     * Create the correct PD Model shading based on the COS base shading.
     * 
     * @param resourceDictionary the COS shading dictionary
     * 
     * @return the newly created shading resources object
     * 
     * @throws IOException If we are unable to create the PDShading object.
     */
    public static PDShadingResources create(COSDictionary resourceDictionary) throws IOException
    {
        PDShadingResources shading = null;
        int shadingType = resourceDictionary.getInt( COSName.SHADING_TYPE, 0 );
        switch (shadingType) 
        {
            case SHADING_TYPE1: 
                shading = new PDShadingType1(resourceDictionary);
                break;
            case SHADING_TYPE2:
                shading = new PDShadingType2(resourceDictionary);
                break;
            case SHADING_TYPE3:
                shading = new PDShadingType3(resourceDictionary);
                break;
            case SHADING_TYPE4:
                shading = new PDShadingType4(resourceDictionary);
                break;
            case SHADING_TYPE5:
                shading = new PDShadingType5(resourceDictionary);
                break;
            case SHADING_TYPE6:
                shading = new PDShadingType6(resourceDictionary);
                break;
            case SHADING_TYPE7:
                shading = new PDShadingType7(resourceDictionary);
                break;
            default:
                throw new IOException( "Error: Unknown shading type " + shadingType );
        }
        return shading;
    }

}

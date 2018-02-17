/*
 * Copyright 2018 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.pdfbox.pdmodel.interactive.annotation;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.interactive.annotation.handlers.PDAppearanceHandler;
import org.apache.pdfbox.pdmodel.interactive.annotation.handlers.PDInkAppearanceHandler;

/**
 *
 * @author Paul King
 */
public class PDAnnotationInk extends PDAnnotationMarkup
{
    /**
     * The type of annotation.
     */
    public static final String SUB_TYPE = "Ink";

    private PDAppearanceHandler customAppearanceHandler;

    /**
     * Constructor.
     */
    public PDAnnotationInk()
    {
        getCOSObject().setName(COSName.SUBTYPE, SUB_TYPE);
    }

    /**
     * Constructor.
     *
     * @param dict The annotations dictionary.
     */
    public PDAnnotationInk(COSDictionary dict)
    {
        super(dict);
    }

    /**
     * Sets the paths that make this annotation.
     *
     * @param inkList An array of arrays, each representing a stroked path. Each array shall be a
     * series of alternating horizontal and vertical coordinates. If the parameter is null the entry
     * will be removed.
     */
    public void setInkList(float[][] inkList)
    {
        if (inkList == null)
        {
            getCOSObject().removeItem(COSName.INKLIST);
            return;
        }
        COSArray array = new COSArray();
        for (float[] path : inkList)
        {
            COSArray innerArray = new COSArray();
            innerArray.setFloatArray(path);
            array.add(innerArray);
        }
        getCOSObject().setItem(COSName.INKLIST, array);
    }

    /**
     * Get one or more disjoint paths that make this annotation.
     *
     * @return An array of arrays, each representing a stroked path. Each array shall be a series of
     * alternating horizontal and vertical coordinates.
     */
    public float[][] getInkList()
    {
        COSBase base = getCOSObject().getDictionaryObject(COSName.INKLIST);
        if (base instanceof COSArray)
        {
            COSArray array = (COSArray) base;
            float[][] inkList = new float[array.size()][];
            for (int i = 0; i < array.size(); ++i)
            {
                COSBase base2 = array.getObject(i);
                if (base2 instanceof COSArray)
                {
                    inkList[i] = ((COSArray) array.getObject(i)).toFloatArray();
                }
                else
                {
                    inkList[i] = new float[0];
                }
            }
            return inkList;
        }
        return new float[0][0];
    }

    /**
     * Set a custom appearance handler for generating the annotations appearance streams.
     * 
     * @param appearanceHandler
     */
    public void setCustomAppearanceHandler(PDAppearanceHandler appearanceHandler)
    {
        customAppearanceHandler = appearanceHandler;
    }

    @Override
    public void constructAppearances()
    {
        if (customAppearanceHandler == null)
        {
            PDInkAppearanceHandler appearanceHandler = new PDInkAppearanceHandler(this);
            appearanceHandler.generateAppearanceStreams();
        }
        else
        {
            customAppearanceHandler.generateAppearanceStreams();
        }
    }
}

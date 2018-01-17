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

    private PDAppearanceHandler inkAppearanceHandler;

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


    //TODO setInkList, javadoc
 
    public float[][] getInkList()
    {
        COSBase base = getCOSObject().getDictionaryObject(COSName.INKLIST);
        if (base instanceof COSArray)
        {
            COSArray array = (COSArray) base;
            float[][] inkList = new float[array.size()][];
            for (int i = 0; i < array.size(); ++i)
            {
                //TODO check for class
                COSArray innerArray = (COSArray) array.getObject(i);
                inkList[i] = innerArray.toFloatArray();
            }
            return inkList;
        }
        // Should never happen as this is a required item
        return null; 
    }

    /**
     * Set a custom appearance handler for generating the annotations appearance streams.
     * 
     * @param inkAppearanceHandler
     */
    public void setCustomInkAppearanceHandler(PDAppearanceHandler inkAppearanceHandler)
    {
        this.inkAppearanceHandler = inkAppearanceHandler;
    }

    @Override
    public void constructAppearances()
    {
        if (inkAppearanceHandler == null)
        {
            PDInkAppearanceHandler appearanceHandler = new PDInkAppearanceHandler(this);
            appearanceHandler.generateAppearanceStreams();
        }
        else
        {
            inkAppearanceHandler.generateAppearanceStreams();
        }
    }
}

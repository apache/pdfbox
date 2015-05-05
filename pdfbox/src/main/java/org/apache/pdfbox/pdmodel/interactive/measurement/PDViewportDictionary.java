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
package org.apache.pdfbox.pdmodel.interactive.measurement;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.common.COSObjectable;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

/**
 * This class represents a viewport dictionary.
 * 
 */
public class PDViewportDictionary implements COSObjectable
{

    /**
     * The type of this annotation.
     */
    public static final String TYPE = "Viewport";
    
    private COSDictionary viewportDictionary;

    /**
     * Constructor.
     */
    public PDViewportDictionary()
    {
        this.viewportDictionary = new COSDictionary();
    }

    /**
     * Constructor.
     * 
     * @param dictionary the dictionary
     */
    public PDViewportDictionary(COSDictionary dictionary)
    {
        this.viewportDictionary = dictionary;
    }

    /**
     * This will return the corresponding dictionary.
     * 
     * @return the viewport dictionary
     */
    @Override
    public COSDictionary getCOSObject()
    {
        return this.viewportDictionary;
    }

    /**
     * Returns the type of the viewport dictionary.
     * It must be "Viewport"
     * @return the type of the external data dictionary
     */

    public String getType()
    {
        return TYPE;
    }

    /**
     * This will retrieve the rectangle specifying the location of the viewport.
     * 
     * @return the location
     */
    public PDRectangle getBBox()
    {
        COSArray bbox = (COSArray)this.getCOSObject().getDictionaryObject("BBox");
        if (bbox != null)
        {
            return new PDRectangle(bbox);
        }
        return null;
    }

    /**
     * This will set the rectangle specifying the location of the viewport.
     * 
     * @param rectangle the rectangle specifying the location.
     */
    public void setBBox(PDRectangle rectangle)
    {
        this.getCOSObject().setItem("BBox", rectangle);
    }

    /**
     * This will retrieve the name of the viewport.
     * 
     * @return the name of the viewport
     */
    public String getName()
    {
        return this.getCOSObject().getNameAsString(COSName.NAME);
    }

   /**
    * This will set the name of the viewport.
    *  
    * @param name the name of the viewport
    */
    public void setName(String name)
    {
        this.getCOSObject().setName(COSName.NAME, name);
    }

    /**
     * This will retrieve the measure dictionary.
     * 
     * @return the measure dictionary
     */
    public PDMeasureDictionary getMeasure()
    {
        COSDictionary measure = (COSDictionary)this.getCOSObject().getDictionaryObject("Measure");
        if (measure != null)
        {
            return new PDMeasureDictionary(measure);
        }
        return null;
    }

    /**
     * This will set the measure dictionary.
     * 
     * @param measure the measure dictionary
     */
    public void setMeasure(PDMeasureDictionary measure)
    {
        this.getCOSObject().setItem("Measure", measure);
    }

}

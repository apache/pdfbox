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
package org.apache.pdfbox.pdmodel.interactive.annotation;

import org.apache.pdfbox.cos.COSDictionary;

import org.apache.pdfbox.pdmodel.common.COSObjectable;

/**
 * This class represents a PDF /BE entry the border effect dictionary.
 *
 * @author Paul King
 */
public class PDBorderEffectDictionary implements COSObjectable
{

    /*
     * The various values of the effect applied to the border as defined in the PDF 1.6 reference Table 8.14
     */

    /**
     * Constant for the name for no effect.
     */
    public static final String STYLE_SOLID = "S";

    /**
     * Constant for the name of a cloudy effect.
     */
    public static final String STYLE_CLOUDY = "C";

    private final COSDictionary dictionary;

    /**
     * Constructor.
     */
    public PDBorderEffectDictionary()
    {
        dictionary = new COSDictionary();
    }

    /**
     * Constructor.
     *
     * @param dict a border style dictionary.
     */
    public PDBorderEffectDictionary(COSDictionary dict)
    {
        dictionary = dict;
    }

    /**
     * returns the dictionary.
     *
     * @return the dictionary
     */
    @Override
    public COSDictionary getCOSObject()
    {
        return dictionary;
    }

    /**
     * This will set the intensity of the applied effect.
     *
     * @param i the intensity of the effect values 0 to 2
     */
    public void setIntensity(float i)
    {
        getCOSObject().setFloat("I", i);
    }

    /**
     * This will retrieve the intensity of the applied effect.
     *
     * @return the intensity value 0 to 2
     */
    public float getIntensity()
    {
        return getCOSObject().getFloat("I", 0);
    }

    /**
     * This will set the border effect, see the STYLE_* constants for valid values.
     *
     * @param s the border effect to use
     */
    public void setStyle(String s)
    {
        getCOSObject().setName("S", s);
    }

    /**
     * This will retrieve the border effect, see the STYLE_* constants for valid values.
     *
     * @return the effect of the border
     */
    public String getStyle()
    {
        return getCOSObject().getNameAsString("S", STYLE_SOLID);
    }

}

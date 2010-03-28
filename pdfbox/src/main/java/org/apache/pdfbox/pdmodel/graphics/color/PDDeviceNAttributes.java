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

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;

import org.apache.pdfbox.pdmodel.common.COSDictionaryMap;

import java.io.IOException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This class represents attributes for a DeviceN color space.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.2 $
 */
public class PDDeviceNAttributes
{
    private COSDictionary dictionary;

    /**
     * Constructor.
     */
    public PDDeviceNAttributes()
    {
        dictionary = new COSDictionary();
    }

    /**
     * Constructor.
     *
     * @param attributes A dictionary that has all of the attributes.
     */
    public PDDeviceNAttributes( COSDictionary attributes )
    {
        dictionary = attributes;
    }

    /**
     * This will get the underlying cos dictionary.
     *
     * @return The dictionary that this object wraps.
     */
    public COSDictionary getCOSDictionary()
    {
        return dictionary;
    }

    /**
     * This will get a map of colorants.  See the PDF Reference for more details about
     * this attribute.  The map will contain a java.lang.String as the key, a colorant name,
     * and a PDColorSpace as the value.
     *
     * @return The colorant map.
     *
     * @throws IOException If there is an error getting the colorspaces.
     */
    public Map getColorants() throws IOException
    {
        Map actuals = new HashMap();
        COSDictionary colorants = (COSDictionary)dictionary.getDictionaryObject( COSName.COLORANTS );
        if( colorants == null )
        {
            colorants = new COSDictionary();
            dictionary.setItem( COSName.COLORANTS, colorants );
        }
        for( COSName name : colorants.keySet() )
        {
            COSBase value = colorants.getDictionaryObject( name );
            actuals.put( name.getName(), PDColorSpaceFactory.createColorSpace( value ) );
        }
        return new COSDictionaryMap( actuals, colorants );
    }

    /**
     * This will replace the existing colorant attribute.  The key should be strings
     * and the values should be PDColorSpaces.
     *
     * @param colorants The map of colorants.
     */
    public void setColorants( Map colorants )
    {
        COSDictionary colorantDict = null;
        if( colorants != null )
        {
            colorantDict = COSDictionaryMap.convert( colorants );
        }
        dictionary.setItem( COSName.COLORANTS, colorantDict );
    }
}

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

package org.apache.pdfbox.pdmodel;

import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.pdmodel.documentinterchange.markedcontent.PDPropertyList;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.pdmodel.graphics.pattern.PDAbstractPattern;
import org.apache.pdfbox.pdmodel.graphics.shading.PDShading;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;

/**
 * A document-wide cache for page resources.
 *
 * @author John Hewson
 */
public interface ResourceCache
{
    /**
     * Returns the font resource for the given indirect object, if it is in the cache.
     * 
     * @param indirect the indirect reference of the font to be returned
     * @return the cached instance of the referenced font, if available
     */
    PDFont getFont(COSObject indirect);

    /**
     * Returns the color space resource for the given indirect object, if it is in the cache.
     * 
     * @param indirect the indirect reference of the colorspace to be returned
     * @return the cached instance of the referenced colorspace, if available
     */
    PDColorSpace getColorSpace(COSObject indirect);

    /**
     * Returns the extended graphics state resource for the given indirect object, if it is in the cache.
     * 
     * @param indirect the indirect reference of the extended graphics state to be returned
     * @return the cached instance of the referenced extended graphics state, if available
     */
    PDExtendedGraphicsState getExtGState(COSObject indirect);
        
    /**
     * Returns the shading resource for the given indirect object, if it is in the cache.
     * 
     * @param indirect the indirect reference of the shading to be returned
     * @return the cached instance of the referenced shading, if available
     */
    PDShading getShading(COSObject indirect);
    
    /**
     * Returns the pattern resource for the given indirect object, if it is in the cache.
     *
     * @param indirect the indirect reference of the pattern to be returned
     * @return the cached instance of the referenced pattern, if available
     */
    PDAbstractPattern getPattern(COSObject indirect);
        
    /**
     * Returns the property list resource for the given indirect object, if it is in the cache.
     * 
     * @param indirect the indirect reference of the property list to be returned
     * @return the cached instance of the referenced property list, if available
     */
    PDPropertyList getProperties(COSObject indirect);
        
    /**
     * Returns the XObject resource for the given indirect object, if it is in the cache.
     * 
     * @param indirect the indirect reference of the XObject to be returned
     * @return the cached instance of the referenced XObject, if available
     */
    PDXObject getXObject(COSObject indirect);

    /**
     * Puts the given indirect font resource in the cache.
     * 
     * @param indirect the indirect reference of the font to be cached
     * @param font the font to be cached
     */
    void put(COSObject indirect, PDFont font);

    /**
     * Puts the given indirect color space resource in the cache.
     * 
     * @param indirect the indirect reference of the colorspace to be cached
     * @param colorSpace the colorspace to be cached
     */
    void put(COSObject indirect, PDColorSpace colorSpace);

    /**
     * Puts the given indirect extended graphics state resource in the cache.
     * 
     * @param indirect the indirect reference of the extended graphics state to be cached
     * @param extGState the extended graphics state to be cached
     */
    void put(COSObject indirect, PDExtendedGraphicsState extGState);

    /**
     * Puts the given indirect shading resource in the cache.
     * 
     * @param indirect the indirect reference of the shading to be cached
     * @param shading the shading to be cached
     */
    void put(COSObject indirect, PDShading shading);

    /**
     * Puts the given indirect pattern resource in the cache.
     * 
     * @param indirect the indirect reference of the pattern to be cached
     * @param pattern the pattern to be cached
     */
    void put(COSObject indirect, PDAbstractPattern pattern);

    /**
     * Puts the given indirect property list resource in the cache.
     * 
     * @param indirect the indirect reference of the property list to be cached
     * @param propertyList the property list to be cached
     */
    void put(COSObject indirect, PDPropertyList propertyList);
    
    /**
     * Puts the given indirect XObject resource in the cache.
     * 
     * @param indirect the indirect reference of the XObject to be cached
     * @param xobject the XObject to be cached
     */
    void put(COSObject indirect, PDXObject xobject);

    /**
     * Removes the given indirect color space resource from the cache.
     * 
     * @param indirect the indirect reference of the color space to be removed
     * 
     * @return the removed resource if present
     */
    default PDColorSpace removeColorSpace(COSObject indirect)
    {
        return null;
    }

    /**
     * Removes the given indirect extended graphics state resource from the cache.
     * 
     * @param indirect the indirect reference of the extended graphics state to be removed
     * 
     * @return the removed resource if present
     */
    default PDExtendedGraphicsState removeExtState(COSObject indirect)
    {
        return null;
    }

    /**
     * Removes the given indirect font resource from the cache.
     * 
     * @param indirect the indirect reference of the font to be removed
     * 
     * @return the removed resource if present
     */
    default PDFont removeFont(COSObject indirect)
    {
        return null;
    }

    /**
     * Removes the given indirect shading resource from the cache.
     * 
     * @param indirect the indirect reference of the shading to be removed
     * 
     * @return the removed resource if present
     */
    default PDShading removeShading(COSObject indirect)
    {
        return null;
    }

    /**
     * Removes the given indirect pattern resource from the cache.
     * 
     * @param indirect the indirect reference of the pattern to be removed
     * 
     * @return the removed resource if present
     */
    default PDAbstractPattern removePattern(COSObject indirect)
    {
        return null;
    }

    /**
     * Removes the given indirect property list resource from the cache.
     * 
     * @param indirect the indirect reference of the property list to be removed
     * 
     * @return the removed resource if present
     */
    default PDPropertyList removeProperties(COSObject indirect)
    {
        return null;
    }

    /**
     * Removes the given indirect XObject resource from the cache.
     * 
     * @param indirect the indirect reference of the XObject to be removed
     * 
     * @return the removed resource if present
     */
    default PDXObject removeXObject(COSObject indirect)
    {
        return null;
    }

}

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

import java.io.IOException;
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
     * 
     * @throws IOException if the given indirect object could not be dereferenced
     */
    PDFont getFont(COSObject indirect) throws IOException;

    /**
     * Returns the color space resource for the given indirect object, if it is in the cache.
     * 
     * @param indirect the indirect reference of the colorspace to be returned
     * @return the cached instance of the referenced colorspace, if available
     * 
     * @throws IOException if the given indirect object could not be dereferenced
     */
    PDColorSpace getColorSpace(COSObject indirect) throws IOException;

    /**
     * Returns the extended graphics state resource for the given indirect object, if it is in the cache.
     * 
     * @param indirect the indirect reference of the extended graphics state to be returned
     * @return the cached instance of the referenced extended graphics state, if available
     * 
     * @throws IOException if the given indirect object could not be dereferenced
     */
    PDExtendedGraphicsState getExtGState(COSObject indirect);
        
    /**
     * Returns the shading resource for the given indirect object, if it is in the cache.
     * 
     * @param indirect the indirect reference of the shading to be returned
     * @return the cached instance of the referenced shading, if available
     * 
     * @throws IOException if the given indirect object could not be dereferenced
     */
    PDShading getShading(COSObject indirect) throws IOException;
    
    /**
     * Returns the pattern resource for the given indirect object, if it is in the cache.
     *
     * @param indirect the indirect reference of the pattern to be returned
     * @return the cached instance of the referenced pattern, if available
     * 
     * @throws IOException if the given indirect object could not be dereferenced
     */
    PDAbstractPattern getPattern(COSObject indirect) throws IOException;
        
    /**
     * Returns the property list resource for the given indirect object, if it is in the cache.
     * 
     * @param indirect the indirect reference of the property list to be returned
     * @return the cached instance of the referenced property list, if available
     * 
     * @throws IOException if the given indirect object could not be dereferenced
     */
    PDPropertyList getProperties(COSObject indirect);
        
    /**
     * Returns the XObject resource for the given indirect object, if it is in the cache.
     * 
     * @param indirect the indirect reference of the XObject to be returned
     * @return the cached instance of the referenced XObject, if available
     * 
     * @throws IOException if the given indirect object could not be dereferenced
     */
    PDXObject getXObject(COSObject indirect) throws IOException;

    /**
     * Puts the given indirect font resource in the cache.
     * 
     * @param indirect the indirect reference of the font to be cached
     * @param font the font to be cached
     * 
     * @throws IOException if the given indirect object could not be dereferenced
     */
    void put(COSObject indirect, PDFont font) throws IOException;

    /**
     * Puts the given indirect color space resource in the cache.
     * 
     * @param indirect the indirect reference of the colorspace to be cached
     * @param font the colorspace to be cached
     * 
     * @throws IOException if the given indirect object could not be dereferenced
     */
    void put(COSObject indirect, PDColorSpace colorSpace) throws IOException;

    /**
     * Puts the given indirect extended graphics state resource in the cache.
     * 
     * @param indirect the indirect reference of the extended graphics state to be cached
     * @param font the extended graphics state to be cached
     * 
     * @throws IOException if the given indirect object could not be dereferenced
     */
    void put(COSObject indirect, PDExtendedGraphicsState extGState);

    /**
     * Puts the given indirect shading resource in the cache.
     * 
     * @param indirect the indirect reference of the shading to be cached
     * @param font the shading to be cached
     * 
     * @throws IOException if the given indirect object could not be dereferenced
     */
    void put(COSObject indirect, PDShading shading) throws IOException;

    /**
     * Puts the given indirect pattern resource in the cache.
     * 
     * @param indirect the indirect reference of the pattern to be cached
     * @param font the pattern to be cached
     * 
     * @throws IOException if the given indirect object could not be dereferenced
     */
    void put(COSObject indirect, PDAbstractPattern pattern) throws IOException;

    /**
     * Puts the given indirect property list resource in the cache.
     * 
     * @param indirect the indirect reference of the property list to be cached
     * @param font the property list to be cached
     * 
     * @throws IOException if the given indirect object could not be dereferenced
     */
    void put(COSObject indirect, PDPropertyList propertyList);
    
    /**
     * Puts the given indirect XObject resource in the cache.
     * 
     * @param indirect the indirect reference of the XObject to be cached
     * @param font the XObject to be cached
     * 
     * @throws IOException if the given indirect object could not be dereferenced
     */
    void put(COSObject indirect, PDXObject xobject) throws IOException;
}

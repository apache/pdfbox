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

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.pdmodel.documentinterchange.markedcontent.PDPropertyList;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.pdmodel.graphics.pattern.PDAbstractPattern;
import org.apache.pdfbox.pdmodel.graphics.shading.PDShading;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;

/**
 * A resource cached based on SoftReference, retains resources until memory pressure causes them to be garbage
 * collected.
 *
 * Cached resources may be removed pro actively.
 * 
 * Resources which are removed and added several times, can't be removed after hitting the threshold of
 * {@value #maxRemovals}. This may happen if resources are shared among several pages. This kind of stable caching is
 * enabled by default and can be deactivated.
 * 
 * @author John Hewson
 */
public class DefaultResourceCache implements ResourceCache
{
    private static final int maxRemovals = 3;

    private final boolean stableCacheEnabled;

    private final Map<COSObject, SoftReference<PDFont>> fonts =
            new HashMap<>();
    private final Map<Long, Integer> removedFonts = new HashMap<>();
    private final Set<Long> stableFonts = new HashSet<>();
    
    private final Map<COSObject, SoftReference<PDColorSpace>> colorSpaces =
            new HashMap<>();
    private final Map<Long, Integer> removedColorSpaces = new HashMap<>();
    private final Set<Long> stableColorSpaces = new HashSet<>();

    private final Map<COSObject, SoftReference<PDXObject>> xobjects =
            new HashMap<>();
    private final Map<Long, Integer> removedXObjects = new HashMap<>();
    private final Set<Long> stableXObject = new HashSet<>();

    private final Map<COSObject, SoftReference<PDExtendedGraphicsState>> extGStates =
            new HashMap<>();
    private final Map<Long, Integer> removedExtGStates = new HashMap<>();
    private final Set<Long> stableExtGStates = new HashSet<>();

    private final Map<COSObject, SoftReference<PDShading>> shadings =
            new HashMap<>();
    private final Map<Long, Integer> removedShadings = new HashMap<>();
    private final Set<Long> stableShadings = new HashSet<>();

    private final Map<COSObject, SoftReference<PDAbstractPattern>> patterns =
            new HashMap<>();
    private final Map<Long, Integer> removedPatterns = new HashMap<>();
    private final Set<Long> stablePatterns = new HashSet<>();

    private final Map<COSObject, SoftReference<PDPropertyList>> properties =
            new HashMap<>();
    private final Map<Long, Integer> removedProperties = new HashMap<>();
    private final Set<Long> stableProperties = new HashSet<>();

    /**
     * Default constructor.
     */
    public DefaultResourceCache()
    {
        this(true);
    }

    /**
     * Constructor providing a parameter to enable/disable the stable object cache.
     * 
     * @param enableStableCache enables/disables the stable object cache
     * 
     */
    public DefaultResourceCache(boolean enableStableCache)
    {
        stableCacheEnabled = enableStableCache;
    }

    @Override
    public PDFont getFont(COSObject indirect)
    {
        SoftReference<PDFont> font = fonts.get(indirect);
        if (font != null)
        {
            return font.get();
        }
        return null;
    }

    @Override
    public void put(COSObject indirect, PDFont font)
    {
        fonts.put(indirect, new SoftReference<>(font));
    }

    @Override
    public PDFont removeFont(COSObject indirect)
    {
        Long objectKey = stableCacheEnabled && indirect.getKey() != null
                ? indirect.getKey().getInternalHash() : null;
        if (objectKey != null)
        {
            if (stableFonts.contains(objectKey))
            {
                return null;
            }
            Integer counter = removedFonts.computeIfAbsent(objectKey, v -> 1);
            if (counter < maxRemovals)
            {
                removedFonts.put(objectKey, ++counter);
            }
            else
            {
                stableFonts.add(objectKey);
                removedFonts.remove(objectKey);
                return null;
            }
        }
        SoftReference<PDFont> font = fonts.remove(indirect);
        return font != null ? font.get() : null;
    }

    @Override
    public PDColorSpace getColorSpace(COSObject indirect)
    {
        SoftReference<PDColorSpace> colorSpace = colorSpaces.get(indirect);
        if (colorSpace != null)
        {
            return colorSpace.get();
        }
        return null;
    }

    @Override
    public void put(COSObject indirect, PDColorSpace colorSpace)
    {
        colorSpaces.put(indirect, new SoftReference<>(colorSpace));
    }

    @Override
    public PDColorSpace removeColorSpace(COSObject indirect)
    {
        Long objectKey = stableCacheEnabled && indirect.getKey() != null
                ? indirect.getKey().getInternalHash() : null;
        if (objectKey != null)
        {
            if (stableColorSpaces.contains(objectKey))
            {
                return null;
            }
            Integer counter = removedColorSpaces.computeIfAbsent(objectKey, v -> 1);
            if (counter < maxRemovals)
            {
                removedColorSpaces.put(objectKey, ++counter);
            }
            else
            {
                stableColorSpaces.add(objectKey);
                removedColorSpaces.remove(objectKey);
                return null;
            }
        }
        SoftReference<PDColorSpace> colorSpace = colorSpaces.remove(indirect);
        return colorSpace != null ? colorSpace.get() : null;
    }

    @Override
    public PDExtendedGraphicsState getExtGState(COSObject indirect)
    {
        SoftReference<PDExtendedGraphicsState> extGState = extGStates.get(indirect);
        if (extGState != null)
        {
            return extGState.get();
        }
        return null;
    }

    @Override
    public void put(COSObject indirect, PDExtendedGraphicsState extGState)
    {
        extGStates.put(indirect, new SoftReference<>(extGState));
    }

    @Override
    public PDExtendedGraphicsState removeExtState(COSObject indirect)
    {
        Long objectKey = stableCacheEnabled && indirect.getKey() != null
                ? indirect.getKey().getInternalHash() : null;
        if (objectKey != null)
        {
            if (stableExtGStates.contains(objectKey))
            {
                return null;
            }
            Integer counter = removedExtGStates.computeIfAbsent(objectKey, v -> 1);
            if (counter < maxRemovals)
            {
                removedExtGStates.put(objectKey, ++counter);
            }
            else
            {
                stableExtGStates.add(objectKey);
                removedExtGStates.remove(objectKey);
                return null;
            }
        }
        SoftReference<PDExtendedGraphicsState> extGState = extGStates.remove(indirect);
        return extGState != null ? extGState.get() : null;
    }

    @Override
    public PDShading getShading(COSObject indirect)
    {
        SoftReference<PDShading> shading = shadings.get(indirect);
        if (shading != null)
        {
            return shading.get();
        }
        return null;
    }

    @Override
    public void put(COSObject indirect, PDShading shading)
    {
        shadings.put(indirect, new SoftReference<>(shading));
    }

    @Override
    public PDShading removeShading(COSObject indirect)
    {
        Long objectKey = stableCacheEnabled && indirect.getKey() != null
                ? indirect.getKey().getInternalHash() : null;
        if (objectKey != null)
        {
            if (stableShadings.contains(objectKey))
            {
                return null;
            }
            Integer counter = removedShadings.computeIfAbsent(objectKey, v -> 1);
            if (counter < maxRemovals)
            {
                removedShadings.put(objectKey, ++counter);
            }
            else
            {
                stableShadings.add(objectKey);
                removedShadings.remove(objectKey);
                return null;
            }
        }
        SoftReference<PDShading> shading = shadings.remove(indirect);
        return shading != null ? shading.get() : null;
    }

    @Override
    public PDAbstractPattern getPattern(COSObject indirect)
    {
        SoftReference<PDAbstractPattern> pattern = patterns.get(indirect);
        if (pattern != null)
        {
            return pattern.get();
        }
        return null;
    }

    @Override
    public void put(COSObject indirect, PDAbstractPattern pattern)
    {
        patterns.put(indirect, new SoftReference<>(pattern));
    }
    
    @Override
    public PDAbstractPattern removePattern(COSObject indirect)
    {
        Long objectKey = stableCacheEnabled && indirect.getKey() != null
                ? indirect.getKey().getInternalHash() : null;
        if (objectKey != null)
        {
            if (stablePatterns.contains(objectKey))
            {
                return null;
            }
            Integer counter = removedPatterns.computeIfAbsent(objectKey, v -> 1);
            if (counter < maxRemovals)
            {
                removedPatterns.put(objectKey, ++counter);
            }
            else
            {
                stablePatterns.add(objectKey);
                removedPatterns.remove(objectKey);
                return null;
            }
        }
        SoftReference<PDAbstractPattern> pattern = patterns.remove(indirect);
        return pattern != null ? pattern.get() : null;
    }

    @Override
    public PDPropertyList getProperties(COSObject indirect)
    {
        SoftReference<PDPropertyList> propertyList = properties.get(indirect);
        if (propertyList != null)
        {
            return propertyList.get();
        }
        return null;
    }

    @Override
    public void put(COSObject indirect, PDPropertyList propertyList)
    {
        properties.put(indirect, new SoftReference<>(propertyList));
    }

    @Override
    public PDPropertyList removeProperties(COSObject indirect)
    {
        Long objectKey = stableCacheEnabled && indirect.getKey() != null
                ? indirect.getKey().getInternalHash() : null;
        if (objectKey != null)
        {
            if (stableProperties.contains(objectKey))
            {
                return null;
            }
            Integer counter = removedProperties.computeIfAbsent(objectKey, v -> 1);
            if (counter < maxRemovals)
            {
                removedProperties.put(objectKey, ++counter);
            }
            else
            {
                stableProperties.add(objectKey);
                removedProperties.remove(objectKey);
                return null;
            }
        }
        SoftReference<PDPropertyList> propertyList = properties.remove(indirect);
        return propertyList != null ? propertyList.get() : null;
    }

    @Override
    public PDXObject getXObject(COSObject indirect)
    {
        SoftReference<PDXObject> xobject = xobjects.get(indirect);
        if (xobject != null)
        {
            return xobject.get();
        }
        return null;
    }

    @Override
    public void put(COSObject indirect, PDXObject xobject)
    {
        xobjects.put(indirect, new SoftReference<>(xobject));
    }

    @Override
    public PDXObject removeXObject(COSObject indirect)
    {
        Long objectKey = stableCacheEnabled && indirect.getKey() != null
                ? indirect.getKey().getInternalHash() : null;
        if (objectKey != null)
        {

            if (stableXObject.contains(objectKey))
            {
                return null;
            }
            Integer counter = removedXObjects.computeIfAbsent(objectKey, v -> 1);
            if (counter < maxRemovals)
            {
                removedXObjects.put(objectKey, ++counter);
            }
            else
            {
                stableXObject.add(objectKey);
                removedXObjects.remove(objectKey);
                return null;
            }
        }
        SoftReference<PDXObject> xobject = xobjects.remove(indirect);
        return xobject != null ? xobject.get() : null;
    }

}

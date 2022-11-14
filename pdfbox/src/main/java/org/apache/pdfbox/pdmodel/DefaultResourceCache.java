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
import java.util.Map;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.pdmodel.documentinterchange.markedcontent.PDPropertyList;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.pdmodel.graphics.pattern.PDAbstractPattern;
import org.apache.pdfbox.pdmodel.graphics.shading.PDShading;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;

/**
 * A resource cached based on SoftReference, retains resources until memory pressure causes them
 * to be garbage collected.
 *
 * @author John Hewson
 */
public class DefaultResourceCache implements ResourceCache
{
    private final Map<COSObject, SoftReference<PDFont>> fonts =
            new HashMap<>();
    
    private final Map<COSObject, SoftReference<PDColorSpace>> colorSpaces =
            new HashMap<>();

    private final Map<COSObject, SoftReference<PDXObject>> xobjects =
            new HashMap<>();

    private final Map<COSObject, SoftReference<PDExtendedGraphicsState>> extGStates =
            new HashMap<>();

    private final Map<COSObject, SoftReference<PDShading>> shadings =
            new HashMap<>();

    private final Map<COSObject, SoftReference<PDAbstractPattern>> patterns =
            new HashMap<>();

    private final Map<COSObject, SoftReference<PDPropertyList>> properties =
            new HashMap<>();

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
}

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
    public PDFont getFont(final COSObject indirect) throws IOException
    {
        final SoftReference<PDFont> font = fonts.get(indirect);
        if (font != null)
        {
            return font.get();
        }
        return null;
    }

    @Override
    public void put(final COSObject indirect, final PDFont font) throws IOException
    {
        fonts.put(indirect, new SoftReference<>(font));
    }

    @Override
    public PDColorSpace getColorSpace(final COSObject indirect) throws IOException
    {
        final SoftReference<PDColorSpace> colorSpace = colorSpaces.get(indirect);
        if (colorSpace != null)
        {
            return colorSpace.get();
        }
        return null;
    }

    @Override
    public void put(final COSObject indirect, final PDColorSpace colorSpace) throws IOException
    {
        colorSpaces.put(indirect, new SoftReference<>(colorSpace));
    }

    @Override
    public PDExtendedGraphicsState getExtGState(final COSObject indirect)
    {
        final SoftReference<PDExtendedGraphicsState> extGState = extGStates.get(indirect);
        if (extGState != null)
        {
            return extGState.get();
        }
        return null;
    }

    @Override
    public void put(final COSObject indirect, final PDExtendedGraphicsState extGState)
    {
        extGStates.put(indirect, new SoftReference<>(extGState));
    }

    @Override
    public PDShading getShading(final COSObject indirect) throws IOException
    {
        final SoftReference<PDShading> shading = shadings.get(indirect);
        if (shading != null)
        {
            return shading.get();
        }
        return null;
    }

    @Override
    public void put(final COSObject indirect, final PDShading shading) throws IOException
    {
        shadings.put(indirect, new SoftReference<>(shading));
    }

    @Override
    public PDAbstractPattern getPattern(final COSObject indirect) throws IOException
    {
        final SoftReference<PDAbstractPattern> pattern = patterns.get(indirect);
        if (pattern != null)
        {
            return pattern.get();
        }
        return null;
    }

    @Override
    public void put(final COSObject indirect, final PDAbstractPattern pattern) throws IOException
    {
        patterns.put(indirect, new SoftReference<>(pattern));
    }
    
    @Override
    public PDPropertyList getProperties(final COSObject indirect)
    {
        final SoftReference<PDPropertyList> propertyList = properties.get(indirect);
        if (propertyList != null)
        {
            return propertyList.get();
        }
        return null;
    }

    @Override
    public void put(final COSObject indirect, final PDPropertyList propertyList)
    {
        properties.put(indirect, new SoftReference<>(propertyList));
    }

    @Override
    public PDXObject getXObject(final COSObject indirect) throws IOException
    {
        final SoftReference<PDXObject> xobject = xobjects.get(indirect);
        if (xobject != null)
        {
            return xobject.get();
        }
        return null;
    }

    @Override
    public void put(final COSObject indirect, final PDXObject xobject) throws IOException
    {
        xobjects.put(indirect, new SoftReference<>(xobject));
    }
}

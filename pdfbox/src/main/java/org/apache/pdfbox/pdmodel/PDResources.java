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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.common.COSDictionaryMap;
import org.apache.pdfbox.pdmodel.common.COSObjectable;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDFontFactory;
import org.apache.pdfbox.pdmodel.graphics.PDExtendedGraphicsState;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpaceFactory;
import org.apache.pdfbox.pdmodel.graphics.pattern.PDPatternResources;
import org.apache.pdfbox.pdmodel.graphics.shading.PDShadingResources;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectImage;
import org.apache.pdfbox.pdmodel.markedcontent.PDPropertyList;
import org.apache.pdfbox.util.MapUtil;

/**
 * This represents a set of resources available at the page/pages/stream level.
 * 
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * 
 */
public class PDResources implements COSObjectable
{
    private COSDictionary resources;
    private Map<String, PDFont> fonts = null;
    private Map<PDFont, String> fontMappings = new HashMap<PDFont, String>();
    private Map<String, PDColorSpace> colorspaces = null;
    private Map<String, PDXObject> xobjects = null;
    private Map<PDXObject, String> xobjectMappings = null;
    private HashMap<String, PDXObjectImage> images = null;
    private Map<String, PDExtendedGraphicsState> graphicsStates = null;
    private Map<String, PDPatternResources> patterns = null;
    private Map<String, PDShadingResources> shadings = null;

    /**
     * Log instance.
     */
    private static final Log LOG = LogFactory.getLog(PDResources.class);

    /**
     * Default constructor.
     */
    public PDResources()
    {
        resources = new COSDictionary();
    }

    /**
     * Prepopulated resources.
     * 
     * @param resourceDictionary The cos dictionary for this resource.
     */
    public PDResources(COSDictionary resourceDictionary)
    {
        resources = resourceDictionary;
    }

    /**
     * This will get the underlying dictionary.
     * 
     * @return The dictionary for these resources.
     */
    public COSDictionary getCOSDictionary()
    {
        return resources;
    }

    /**
     * Convert this standard java object to a COS object.
     * 
     * @return The cos object that matches this Java object.
     */
    public COSBase getCOSObject()
    {
        return resources;
    }

    /**
     * Calling this will release all cached information.
     * 
     */
    public void clear()
    {
        if (fonts != null)
        {
            fonts.clear();
            fonts = null;
        }
        if (fontMappings != null)
        {
            fontMappings.clear();
            fontMappings = null;
        }
        if (colorspaces != null)
        {
            colorspaces.clear();
            colorspaces = null;
        }
        if (xobjects != null)
        {
            xobjects.clear();
            xobjects = null;
        }
        if (xobjectMappings != null)
        {
            xobjectMappings.clear();
            xobjectMappings = null;
        }
        if (images != null)
        {
            images.clear();
            images = null;
        }
        if (graphicsStates != null)
        {
            graphicsStates.clear();
            graphicsStates = null;
        }
        if (patterns != null)
        {
            patterns.clear();
            patterns = null;
        }
        if (shadings != null)
        {
            shadings.clear();
            shadings = null;
        }
    }

    /**
     * This will get the map of fonts. This will never return null. The keys are string and the values are PDFont
     * objects.
     * 
     * @param fontCache A map of existing PDFont objects to reuse.
     * @return The map of fonts.
     * 
     * @throws IOException If there is an error getting the fonts.
     * 
     * @deprecated due to some side effects font caching is no longer supported, use {@link #getFonts()} instead
     */
    public Map<String, PDFont> getFonts(Map<String, PDFont> fontCache) throws IOException
    {
        return getFonts();
    }

    /**
     * This will get the map of fonts. This will never return null.
     * 
     * @return The map of fonts.
     */
    public Map<String, PDFont> getFonts()
    {
        if (fonts == null)
        {
            // at least an empty map will be returned
            // TODO we should return null instead of an empty map
            fonts = new HashMap<String, PDFont>();
            COSDictionary fontsDictionary = (COSDictionary) resources.getDictionaryObject(COSName.FONT);
            if (fontsDictionary == null)
            {
                fontsDictionary = new COSDictionary();
                resources.setItem(COSName.FONT, fontsDictionary);
            }
            else
            {
                for (COSName fontName : fontsDictionary.keySet())
                {
                    COSBase font = fontsDictionary.getDictionaryObject(fontName);
                    // data-000174.pdf contains a font that is a COSArray, looks to be an error in the
                    // PDF, we will just ignore entries that are not dictionaries.
                    if (font instanceof COSDictionary)
                    {
                        PDFont newFont = null;
                        try
                        {
                            newFont = PDFontFactory.createFont((COSDictionary) font);
                        }
                        catch (IOException exception)
                        {
                            LOG.error("error while creating a font", exception);
                        }
                        if (newFont != null)
                        {
                            fonts.put(fontName.getName(), newFont);
                        }
                    }
                }
            }
            setFonts(fonts);
        }
        return fonts;
    }

    /**
     * This will get the map of PDXObjects that are in the resource dictionary. This will never return null.
     * 
     * @return The map of xobjects.
     */
    public Map<String, PDXObject> getXObjects()
    {
        if (xobjects == null)
        {
            // at least an empty map will be returned
            // TODO we should return null instead of an empty map
            xobjects = new HashMap<String, PDXObject>();
            COSDictionary xobjectsDictionary = (COSDictionary) resources.getDictionaryObject(COSName.XOBJECT);
            if (xobjectsDictionary == null)
            {
                xobjectsDictionary = new COSDictionary();
                resources.setItem(COSName.XOBJECT, xobjectsDictionary);
            }
            else
            {
                xobjects = new HashMap<String, PDXObject>();
                for (COSName objName : xobjectsDictionary.keySet())
                {
                    PDXObject xobject = null;
                    try
                    {
                        xobject = PDXObject.createXObject(xobjectsDictionary.getDictionaryObject(objName));
                    }
                    catch (IOException exception)
                    {
                        LOG.error("error while creating a xobject", exception);
                    }
                    if (xobject != null)
                    {
                        xobjects.put(objName.getName(), xobject);
                    }
                }
            }
            setXObjects(xobjects);
        }
        return xobjects;
    }

    /**
     * This will get the map of images. An empty map will be returned if there are no underlying images. So far the keys
     * are COSName of the image and the value is the corresponding PDXObjectImage.
     * 
     * @author By BM
     * @return The map of images.
     * @throws IOException If there is an error writing the picture.
     * 
     * @deprecated use {@link #getXObjects()} instead, as the images map isn't synchronized with the XObjects map.
     */
    public Map<String, PDXObjectImage> getImages() throws IOException
    {
        if (images == null)
        {
            Map<String, PDXObject> allXObjects = getXObjects();
            images = new HashMap<String, PDXObjectImage>();
            for (Map.Entry<String, PDXObject> entry : allXObjects.entrySet())
            {
                PDXObject xobject = entry.getValue();
                if (xobject instanceof PDXObjectImage)
                {
                    images.put(entry.getKey(), (PDXObjectImage) xobject);
                }
            }
        }
        return images;
    }

    /**
     * This will set the map of fonts.
     * 
     * @param fontsValue The new map of fonts.
     */
    public void setFonts(Map<String, PDFont> fontsValue)
    {
        fonts = fontsValue;
        if (fontsValue != null)
        {
            resources.setItem(COSName.FONT, COSDictionaryMap.convert(fontsValue));
            fontMappings = reverseMap(fontsValue, PDFont.class);
        }
        else
        {
            resources.removeItem(COSName.FONT);
            fontMappings = null;
        }
    }

    /**
     * This will set the map of xobjects.
     * 
     * @param xobjectsValue The new map of xobjects.
     */
    public void setXObjects(Map<String, PDXObject> xobjectsValue)
    {
        xobjects = xobjectsValue;
        if (xobjectsValue != null)
        {
            resources.setItem(COSName.XOBJECT, COSDictionaryMap.convert(xobjectsValue));
            xobjectMappings = reverseMap(xobjects, PDXObject.class);
        }
        else
        {
            resources.removeItem(COSName.XOBJECT);
            xobjectMappings = null;
        }
    }

    /**
     * This will get the map of colorspaces. This will return null if the underlying resources dictionary does not have
     * a colorspace dictionary. The keys are string and the values are PDColorSpace objects.
     * 
     * @return The map of colorspaces.
     */
    public Map<String, PDColorSpace> getColorSpaces()
    {
        if (colorspaces == null)
        {
            COSDictionary csDictionary = (COSDictionary) resources.getDictionaryObject(COSName.COLORSPACE);
            if (csDictionary != null)
            {
                colorspaces = new HashMap<String, PDColorSpace>();
                for (COSName csName : csDictionary.keySet())
                {
                    COSBase cs = csDictionary.getDictionaryObject(csName);
                    PDColorSpace colorspace = null;
                    try
                    {
                        colorspace = PDColorSpaceFactory.createColorSpace(cs);
                    }
                    catch (IOException exception)
                    {
                        LOG.error("error while creating a colorspace", exception);
                    }
                    if (colorspace != null)
                    {
                        colorspaces.put(csName.getName(), colorspace);
                    }
                }
            }
        }
        return colorspaces;
    }

    /**
     * This will set the map of colorspaces.
     * 
     * @param csValue The new map of colorspaces.
     */
    public void setColorSpaces(Map<String, PDColorSpace> csValue)
    {
        colorspaces = csValue;
        if (csValue != null)
        {
            resources.setItem(COSName.COLORSPACE, COSDictionaryMap.convert(csValue));
        }
        else
        {
            resources.removeItem(COSName.COLORSPACE);
        }
    }

    /**
     * This will get the map of graphic states. This will return null if the underlying resources dictionary does not
     * have a graphics dictionary. The keys are the graphic state name as a String and the values are
     * PDExtendedGraphicsState objects.
     * 
     * @return The map of extended graphic state objects.
     */
    public Map<String, PDExtendedGraphicsState> getGraphicsStates()
    {
        if (graphicsStates == null)
        {
            COSDictionary states = (COSDictionary) resources.getDictionaryObject(COSName.EXT_G_STATE);
            if (states != null)
            {
                graphicsStates = new HashMap<String, PDExtendedGraphicsState>();
                for (COSName name : states.keySet())
                {
                    COSDictionary dictionary = (COSDictionary) states.getDictionaryObject(name);
                    graphicsStates.put(name.getName(), new PDExtendedGraphicsState(dictionary));
                }
            }
        }
        return graphicsStates;
    }

    /**
     * This will set the map of graphics states.
     * 
     * @param states The new map of states.
     */
    public void setGraphicsStates(Map<String, PDExtendedGraphicsState> states)
    {
        graphicsStates = states;
        if (states != null)
        {
            Iterator<String> iter = states.keySet().iterator();
            COSDictionary dic = new COSDictionary();
            while (iter.hasNext())
            {
                String name = (String) iter.next();
                PDExtendedGraphicsState state = states.get(name);
                dic.setItem(COSName.getPDFName(name), state.getCOSObject());
            }
            resources.setItem(COSName.EXT_G_STATE, dic);
        }
        else
        {
            resources.removeItem(COSName.EXT_G_STATE);
        }
    }

    /**
     * Returns the dictionary mapping resource names to property list dictionaries for marked content.
     * 
     * @return the property list
     */
    public PDPropertyList getProperties()
    {
        PDPropertyList retval = null;
        COSDictionary props = (COSDictionary) resources.getDictionaryObject(COSName.PROPERTIES);

        if (props != null)
        {
            retval = new PDPropertyList(props);
        }
        return retval;
    }

    /**
     * Sets the dictionary mapping resource names to property list dictionaries for marked content.
     * 
     * @param props the property list
     */
    public void setProperties(PDPropertyList props)
    {
        resources.setItem(COSName.PROPERTIES, props.getCOSObject());
    }

    /**
     * This will get the map of patterns. This will return null if the underlying resources dictionary does not have a
     * patterns dictionary. The keys are the pattern name as a String and the values are PDPatternResources objects.
     * 
     * @return The map of pattern resources objects.
     * 
     * @throws IOException If there is an error getting the pattern resources.
     */
    public Map<String, PDPatternResources> getPatterns() throws IOException
    {
        if (patterns == null)
        {
            COSDictionary patternsDictionary = (COSDictionary) resources.getDictionaryObject(COSName.PATTERN);
            if (patternsDictionary != null)
            {
                patterns = new HashMap<String, PDPatternResources>();
                for (COSName name : patternsDictionary.keySet())
                {
                    COSDictionary dictionary = (COSDictionary) patternsDictionary.getDictionaryObject(name);
                    patterns.put(name.getName(), PDPatternResources.create(dictionary));
                }
            }
        }
        return patterns;
    }

    /**
     * This will set the map of patterns.
     * 
     * @param patternsValue The new map of patterns.
     */
    public void setPatterns(Map<String, PDPatternResources> patternsValue)
    {
        patterns = patternsValue;
        if (patternsValue != null)
        {
            Iterator<String> iter = patternsValue.keySet().iterator();
            COSDictionary dic = new COSDictionary();
            while (iter.hasNext())
            {
                String name = iter.next();
                PDPatternResources pattern = patternsValue.get(name);
                dic.setItem(COSName.getPDFName(name), pattern.getCOSObject());
            }
            resources.setItem(COSName.PATTERN, dic);
        }
        else
        {
            resources.removeItem(COSName.PATTERN);
        }
    }

    /**
     * This will get the map of shadings. This will return null if the underlying resources dictionary does not have a
     * shading dictionary. The keys are the shading name as a String and the values are PDShadingResources objects.
     * 
     * @return The map of shading resources objects.
     * 
     * @throws IOException If there is an error getting the shading resources.
     */
    public Map<String, PDShadingResources> getShadings() throws IOException
    {
        if (shadings == null)
        {
            COSDictionary shadingsDictionary = (COSDictionary) resources.getDictionaryObject(COSName.SHADING);
            if (shadingsDictionary != null)
            {
                shadings = new HashMap<String, PDShadingResources>();
                for (COSName name : shadingsDictionary.keySet())
                {
                    COSDictionary dictionary = (COSDictionary) shadingsDictionary.getDictionaryObject(name);
                    shadings.put(name.getName(), PDShadingResources.create(dictionary));
                }
            }
        }
        return shadings;
    }

    /**
     * This will set the map of shadings.
     * 
     * @param shadingsValue The new map of shadings.
     */
    public void setShadings(Map<String, PDShadingResources> shadingsValue)
    {
        shadings = shadingsValue;
        if (shadingsValue != null)
        {
            Iterator<String> iter = shadingsValue.keySet().iterator();
            COSDictionary dic = new COSDictionary();
            while (iter.hasNext())
            {
                String name = iter.next();
                PDShadingResources shading = shadingsValue.get(name);
                dic.setItem(COSName.getPDFName(name), shading.getCOSObject());
            }
            resources.setItem(COSName.SHADING, dic);
        }
        else
        {
            resources.removeItem(COSName.SHADING);
        }
    }

    /**
     * Adds the given font to the resources of the current page.
     * 
     * @param font the font to be added
     * @return the font name to be used within the content stream.
     */
    public String addFont(PDFont font)
    {
        // use the getter to initialize a possible empty fonts map
        return addFont(font, MapUtil.getNextUniqueKey(getFonts(), "F"));
    }

    /**
     * Adds the given font to the resources of the current page using the given font key.
     * 
     * @param font the font to be added
     * @param fontKey key to used to map to the given font
     * @return the font name to be used within the content stream.
     */
    public String addFont(PDFont font, String fontKey)
    {
        if (fonts == null)
        {
            // initialize fonts map
            getFonts();
        }

        String fontMapping = fontMappings.get(font);
        if (fontMapping == null)
        {
            fontMapping = fontKey;
            fontMappings.put(font, fontMapping);
            fonts.put(fontMapping, font);
            addFontToDictionary(font, fontMapping);
        }
        return fontMapping;
    }

    private void addFontToDictionary(PDFont font, String fontName)
    {
        COSDictionary fontsDictionary = (COSDictionary) resources.getDictionaryObject(COSName.FONT);
        fontsDictionary.setItem(fontName, font);
    }

    /**
     * Adds the given XObject to the resources of the current the page.
     * 
     * @param xobject the XObject to be added
     * @param prefix the prefix to be used for the name
     * 
     * @return the XObject name to be used within the content stream.
     */
    public String addXObject(PDXObject xobject, String prefix)
    {
        if (xobjects == null)
        {
            // initialize XObject map
            getXObjects();
        }
        String objMapping = xobjectMappings.get(xobject);
        if (objMapping == null)
        {
            objMapping = MapUtil.getNextUniqueKey(xobjects, prefix);
            xobjectMappings.put(xobject, objMapping);
            xobjects.put(objMapping, xobject);
            addXObjectToDictionary(xobject, objMapping);
        }
        return objMapping;
    }

    private void addXObjectToDictionary(PDXObject xobject, String xobjectName)
    {
        COSDictionary xobjectsDictionary = (COSDictionary) resources.getDictionaryObject(COSName.XOBJECT);
        xobjectsDictionary.setItem(xobjectName, xobject);
    }

    private <T> Map<T, String> reverseMap(Map<String, T> map, Class<T> keyClass)
    {
        Map<T, String> reversed = new java.util.HashMap<T, String>();
        for (Map.Entry<String, T> entry : map.entrySet())
        {
            reversed.put(keyClass.cast(entry.getValue()), (String) entry.getKey());
        }
        return reversed;
    }

}

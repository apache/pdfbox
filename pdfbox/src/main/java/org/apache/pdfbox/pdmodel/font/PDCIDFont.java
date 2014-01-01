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
package org.apache.pdfbox.pdmodel.font;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.util.ResourceLoader;

/**
 * This is implementation for the CIDFontType0/CIDFontType2 Fonts.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * 
 */
public abstract class PDCIDFont extends PDSimpleFont
{
    /**
     * Log instance.
     */
    private static final Log LOG = LogFactory.getLog(PDCIDFont.class);

    private Map<Integer, Float> widthCache = null;
    private long defaultWidth = 0;

    /**
     * Constructor.
     */
    public PDCIDFont()
    {
        super();
    }

    /**
     * Constructor.
     *
     * @param fontDictionary The font dictionary according to the PDF specification.
     */
    public PDCIDFont(COSDictionary fontDictionary)
    {
        super(fontDictionary);
        extractWidths();
    }

    /**
     * This will get the fonts bounding box.
     *
     * @return The fonts bounding box.
     *
     * @throws IOException If there is an error getting the font bounding box.
     */
    @Override
    public PDRectangle getFontBoundingBox() throws IOException
    {
        throw new RuntimeException("getFontBoundingBox(): Not yet implemented");
    }

    /**
     * This will get the default width.  The default value for the default width is 1000.
     *
     * @return The default width for the glyphs in this font.
     */
    public long getDefaultWidth()
    {
        if (defaultWidth == 0)
        {
            COSNumber number = (COSNumber) font.getDictionaryObject(COSName.DW);
            if (number != null)
            {
                defaultWidth = number.intValue();
            }
            else
            {
                defaultWidth = 1000;
            }
        }
        return defaultWidth;
    }

    /**
     * This will set the default width for the glyphs of this font.
     *
     * @param dw The default width.
     */
    public void setDefaultWidth(long dw)
    {
        defaultWidth = dw;
        font.setLong(COSName.DW, dw);
    }

    /**
     * This will get the font width for a character.
     *
     * @param c The character code to get the width for.
     * @param offset The offset into the array.
     * @param length The length of the data.
     *
     * @return The width is in 1000 unit of text space, ie 333 or 777
     *
     * @throws IOException If an error occurs while parsing.
     */
    @Override
    public float getFontWidth(byte[] c, int offset, int length) throws IOException
    {

        float retval = getDefaultWidth();
        int code = getCodeFromArray(c, offset, length);

        Float widthFloat = widthCache.get(code);
        if (widthFloat != null)
        {
            retval = widthFloat.floatValue();
        }
        return retval;
    }

    private void extractWidths()
    {
        if (widthCache == null)
        {
            widthCache = new HashMap<Integer, Float>();
            COSArray widths = (COSArray) font.getDictionaryObject(COSName.W);
            if (widths != null)
            {
                int size = widths.size();
                int counter = 0;
                while (counter < size)
                {
                    COSNumber firstCode = (COSNumber) widths.getObject(counter++);
                    COSBase next = widths.getObject(counter++);
                    if (next instanceof COSArray)
                    {
                        COSArray array = (COSArray) next;
                        int startRange = firstCode.intValue();
                        int arraySize = array.size();
                        for (int i = 0; i < arraySize; i++)
                        {
                            COSNumber width = (COSNumber) array.get(i);
                            widthCache.put(startRange + i, width.floatValue());
                        }
                    }
                    else
                    {
                        COSNumber secondCode = (COSNumber) next;
                        COSNumber rangeWidth = (COSNumber) widths.getObject(counter++);
                        int startRange = firstCode.intValue();
                        int endRange = secondCode.intValue();
                        float width = rangeWidth.floatValue();
                        for (int i = startRange; i <= endRange; i++)
                        {
                            widthCache.put(i, width);
                        }
                    }
                }
            }
        }
    }

    /**
     * This will get the font height for a character.
     *
     * @param c The character code to get the height for.
     * @param offset The offset into the array.
     * @param length The length of the data.
     *
     * @return The width is in 1000 unit of text space, ie 333 or 777
     *
     * @throws IOException If an error occurs while parsing.
     */
    @Override
    public float getFontHeight(byte[] c, int offset, int length) throws IOException
    {
        float retval = 0;
        PDFontDescriptor desc = getFontDescriptor();
        float xHeight = desc.getXHeight();
        float capHeight = desc.getCapHeight();
        if (xHeight != 0f && capHeight != 0)
        {
            // do an average of these two. Can we do better???
            retval = (xHeight + capHeight) / 2f;
        }
        else if (xHeight != 0)
        {
            retval = xHeight;
        }
        else if (capHeight != 0)
        {
            retval = capHeight;
        }
        else
        {
            retval = 0;
        }
        if (retval == 0)
        {
            retval = desc.getAscent();
        }
        return retval;
    }

    /**
     * This will get the average font width for all characters.
     *
     * @return The width is in 1000 unit of text space, ie 333 or 777
     *
     * @throws IOException If an error occurs while parsing.
     */
    @Override
    public float getAverageFontWidth() throws IOException
    {
        float totalWidths = 0.0f;
        float characterCount = 0.0f;
        COSArray widths = (COSArray) font.getDictionaryObject(COSName.W);

        if (widths != null)
        {
            for (int i = 0; i < widths.size(); i++)
            {
                COSNumber firstCode = (COSNumber) widths.getObject(i++);
                COSBase next = widths.getObject(i);
                if (next instanceof COSArray)
                {
                    COSArray array = (COSArray) next;
                    for (int j = 0; j < array.size(); j++)
                    {
                        COSNumber width = (COSNumber) array.get(j);
                        totalWidths += width.floatValue();
                        characterCount += 1;
                    }
                }
                else
                {
                    i++;
                    COSNumber rangeWidth = (COSNumber) widths.getObject(i);
                    if (rangeWidth.floatValue() > 0)
                    {
                        totalWidths += rangeWidth.floatValue();
                        characterCount += 1;
                    }
                }
            }
        }
        float average = totalWidths / characterCount;
        if (average <= 0)
        {
            average = getDefaultWidth();
        }
        return average;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getFontWidth(int charCode)
    {
        float width = getDefaultWidth();
        if (widthCache.containsKey(charCode))
        {
            width = widthCache.get(charCode);
        }
        return width;
    }

    /**
     * Extract the CIDSystemInfo.
     * @return the CIDSystemInfo as String
     */
    private String getCIDSystemInfo()
    {
        String cidSystemInfo = null;
        COSDictionary cidsysteminfo = (COSDictionary) font.getDictionaryObject(COSName.CIDSYSTEMINFO);
        if (cidsysteminfo != null)
        {
            String ordering = cidsysteminfo.getString(COSName.ORDERING);
            String registry = cidsysteminfo.getString(COSName.REGISTRY);
            int supplement = cidsysteminfo.getInt(COSName.SUPPLEMENT);
            cidSystemInfo = registry + "-" + ordering + "-" + supplement;
        }
        return cidSystemInfo;
    }

    @Override
    protected void determineEncoding()
    {
        String cidSystemInfo = getCIDSystemInfo();
        if (cidSystemInfo != null)
        {
            if (cidSystemInfo.contains("Identity"))
            {
                cidSystemInfo = "Identity-H";
            }
            else if (cidSystemInfo.startsWith("Adobe-UCS-"))
            {
                cidSystemInfo = "Adobe-Identity-UCS";
            }
            else
            {
                cidSystemInfo = cidSystemInfo.substring(0, cidSystemInfo.lastIndexOf("-")) + "-UCS2";
            }
            cmap = cmapObjects.get(cidSystemInfo);
            if (cmap == null)
            {
            	InputStream cmapStream = null;
                try
                {
                    // look for a predefined CMap with the given name
                    cmapStream = ResourceLoader.loadResource(resourceRootCMAP + cidSystemInfo);
                    if (cmapStream != null)
                    {
                    	cmap = parseCmap(resourceRootCMAP, cmapStream);
                    	if (cmap == null)
                    	{
                    		LOG.error("Error: Could not parse predefined CMAP file for '" + cidSystemInfo + "'");
                    	}
                    }
                    else
                    {
                		LOG.debug("Debug: '" + cidSystemInfo + "' isn't a predefined CMap, most likely it's embedded in the pdf itself.");
                    }
                }
                catch (IOException exception)
                {
                    LOG.error("Error: Could not find predefined CMAP file for '" + cidSystemInfo + "'");
                }
                finally
                {
                	IOUtils.closeQuietly(cmapStream);
                }
            }
        }
        else
        {
            super.determineEncoding();
        }
    }

    @Override
    public String encode(byte[] c, int offset, int length) throws IOException
    {
        String result = null;
        if (cmap != null)
        {
            result = cmapEncoding(getCodeFromArray(c, offset, length), length, true, cmap);
        }
        else
        {
            result = super.encode(c, offset, length);
        }
        return result;
    }
}

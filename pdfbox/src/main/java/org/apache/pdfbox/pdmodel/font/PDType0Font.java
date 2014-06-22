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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.encoding.DictionaryEncoding;
import org.apache.pdfbox.encoding.Encoding;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.util.ResourceLoader;

/**
 * Type 0 (composite) Font.
 * 
 * @author Ben Litchfield
 */
public class PDType0Font extends PDFont
{
    private static final Log LOG = LogFactory.getLog(PDType0Font.class);

    private PDCIDFont descendantFont;
    private COSDictionary descendantFontDictionary;

    /**
     * Constructor.
     * 
     * @param fontDictionary The font dictionary according to the PDF specification.
     */
    public PDType0Font(COSDictionary fontDictionary)
    {
        super(fontDictionary);
        COSArray descendantFonts = (COSArray)dict.getDictionaryObject(COSName.DESCENDANT_FONTS);
        descendantFontDictionary = (COSDictionary)descendantFonts.getObject(0);
        if (descendantFontDictionary != null)
        {
            try
            {
                descendantFont = PDFontFactory.createDescendantFont(descendantFontDictionary, this);
            }
            catch (IOException exception)
            {
                LOG.error("Error while creating the descendant font!");
            }
        }
    }

    /**
     * Returns the descendant font.
     *
     * @return the descendant font.
     */
    public PDCIDFont getDescendantFont()
    {
        return descendantFont;
    }

    @Override
    public PDFontDescriptor getFontDescriptor()
    {
        return descendantFont.getFontDescriptor();
    }

    @Override
    public PDRectangle getFontBoundingBox() throws IOException
    {
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public float getFontWidth(byte[] c, int offset, int length) throws IOException
    {
        return descendantFont.getFontWidth(c, offset, length);
    }

    @Override
    public float getFontHeight(byte[] c, int offset, int length) throws IOException
    {
        return descendantFont.getFontHeight(c, offset, length);
    }

    @Override
    public float getAverageFontWidth() throws IOException
    {
        return descendantFont.getAverageFontWidth();
    }

    @Override
    public float getFontWidth(int charCode)
    {
        return descendantFont.getFontWidth(charCode);
    }

    // todo: copied from PDSimpleFont and modified
    // todo: for a Type 0 font this can only be "The name of a predefined CMap, or a stream containing a
    // CMap that maps character codes to font numbers and CIDs", so I should adjust this accordingly
    @Override
    protected void determineEncoding()
    {
        String cmapName = null;
        COSName encodingName = null;
        COSBase encoding = dict.getDictionaryObject(COSName.ENCODING);
        Encoding fontEncoding = null;
        if (encoding != null)
        {
            if (encoding instanceof COSName)
            {
                if (cmap == null)
                {
                    encodingName = (COSName) encoding;
                    cmap = cmapObjects.get(encodingName.getName());
                    if (cmap == null)
                    {
                        cmapName = encodingName.getName();
                    }
                }
                if (cmap == null && cmapName != null)
                {
                    try
                    {
                        fontEncoding = Encoding.getInstance(encodingName);
                    }
                    catch (IOException exception)
                    {
                        LOG.debug("Debug: Could not find encoding for " + encodingName);
                    }
                }
            }
            else if (encoding instanceof COSStream)
            {
                if (cmap == null)
                {
                    COSStream encodingStream = (COSStream) encoding;
                    try
                    {
                        InputStream is = encodingStream.getUnfilteredStream();
                        cmap = parseCmap(null, is);
                        IOUtils.closeQuietly(is);
                    }
                    catch (IOException exception)
                    {
                        LOG.error("Error: Could not parse the embedded CMAP");
                    }
                }
            }
            else if (encoding instanceof COSDictionary)
            {
                try
                {
                    fontEncoding = new DictionaryEncoding((COSDictionary) encoding);
                }
                catch (IOException exception)
                {
                    LOG.error("Error: Could not create the DictionaryEncoding");
                }
            }
        }
        this.fontEncoding = fontEncoding;
        extractToUnicodeEncoding(); // todo: IMPORTANT!

        if (cmap == null && cmapName != null)
        {
            InputStream cmapStream = null;
            try
            {
                // look for a predefined CMap with the given name
                cmapStream = ResourceLoader.loadResource(resourceRootCMAP + cmapName);
                if (cmapStream != null)
                {
                    cmap = parseCmap(resourceRootCMAP, cmapStream);
                    if (cmap == null && encodingName == null)
                    {
                        LOG.error("Error: Could not parse predefined CMAP file for '" +
                                cmapName + "'");
                    }
                }
                else
                {
                    LOG.debug("Debug: '" + cmapName + "' isn't a predefined map, most likely it's" +
                            "embedded in the pdf itself.");
                }
            }
            catch (IOException exception)
            {
                LOG.error("Error: Could not find predefined CMAP file for '" + cmapName + "'");
            }
            finally
            {
                IOUtils.closeQuietly(cmapStream);
            }
        }
    }

    @Override
    public String encode(byte[] c, int offset, int length) throws IOException
    {
        String retval = null;
        if (hasToUnicode())
        {
            retval = super.encode(c, offset, length);
        }

        if (retval == null)
        {
            int result = cmap.lookupCID(c, offset, length);
            if (result != -1)
            {
                retval = descendantFont.cmapEncoding(result, 2, true, null);
            }
        }
        return retval;
    }

    @Override
    public void clear()
    {
        super.clear();
        if (descendantFont != null)
        {
            descendantFont.clear();
            descendantFont = null;
        }
        descendantFontDictionary = null;
    }
}

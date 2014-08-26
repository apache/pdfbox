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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.encoding.DictionaryEncoding;
import org.apache.pdfbox.encoding.Encoding;
import org.apache.pdfbox.encoding.GlyphList;
import org.apache.pdfbox.encoding.MacRomanEncoding;
import org.apache.pdfbox.encoding.StandardEncoding;
import org.apache.pdfbox.encoding.WinAnsiEncoding;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * A simple font. Simple fonts use a PostScript encoding vector.
 *
 * @author John Hewson
 */
public abstract class PDSimpleFont extends PDFont
{
    private static final Log LOG = LogFactory.getLog(PDSimpleFont.class);

    protected Encoding encoding;
    private final Set<Integer> noUnicode = new HashSet<Integer>();

    /**
     * Constructor
     */
    protected PDSimpleFont()
    {
        super();
    }

    /**
     * Constructor.
     *
     * @param fontDictionary Font dictionary.
     */
    protected PDSimpleFont(COSDictionary fontDictionary) throws IOException
    {
        super(fontDictionary);
    }

    /**
     * Reads the Encoding from the Font dictionary or the embedded or substituted font file.
     * Must be called at the end of any subclass constructors.
     *
     * @throws IOException if the font file could not be read
     */
    protected final void readEncoding() throws IOException
    {
        COSBase encoding = dict.getDictionaryObject(COSName.ENCODING);
        if (encoding != null)
        {
            if (encoding instanceof COSName)
            {
                COSName encodingName = (COSName)encoding;
                this.encoding = Encoding.getInstance(encodingName);
                if (this.encoding == null)
                {
                    LOG.warn("Unknown encoding: " + encodingName);
                    this.encoding = readEncodingFromFont(); // fallback
                }
            }
            else if (encoding instanceof COSDictionary)
            {
                COSDictionary encodingDict = (COSDictionary)encoding;
                Encoding builtIn = null;
                if (!encodingDict.containsKey(COSName.BASE_ENCODING) && isSymbolic())
                {
                    builtIn = readEncodingFromFont();
                }
                this.encoding = new DictionaryEncoding(encodingDict, isSymbolic(), builtIn);
            }
        }
        else
        {
            this.encoding = readEncodingFromFont();
        }
    }

    /**
     * Called by readEncoding() if the encoding needs to be extracted from the font file.
     *
     * @throws IOException if the font file could not be read
     */
    protected abstract Encoding readEncodingFromFont() throws IOException;

    /**
     * Returns the Encoding vector.
     */
    public Encoding getEncoding()
    {
        return encoding;
    }

    @Override
    protected Boolean isFontSymbolic()
    {
        Boolean result = super.isFontSymbolic();
        if (result != null)
        {
            return result;
        }
        else
        {
            if (encoding instanceof WinAnsiEncoding ||encoding instanceof MacRomanEncoding ||
                encoding instanceof StandardEncoding)
            {
                return false;
            }
            else if (encoding instanceof DictionaryEncoding)
            {
                // each name in Differences array must also be in the latin character se
                DictionaryEncoding enc = (DictionaryEncoding)encoding;
                for (String name : enc.getDifferences().values())
                {
                    if (!(WinAnsiEncoding.INSTANCE.contains(name) &&
                          MacRomanEncoding.INSTANCE.contains(name) &&
                          StandardEncoding.INSTANCE.contains(name)))
                    {
                        return true;
                    }

                }
                return false;
            }
            else
            {
                // we don't know
                return null;
            }
        }
    }

    @Override
    public String toUnicode(int code)
    {
        // first try to use a ToUnicode CMap
        String unicode = super.toUnicode(code);
        if (unicode != null)
        {
            return unicode;
        }

        // if the font is a "simple font" and uses MacRoman/MacExpert/WinAnsi[Encoding]
        // or has Differences with names from only Adobe Standard and/or Symbol, then:
        //
        //    a) Map the character codes to names
        //    b) Look up the name in the Adobe Glyph List to obtain the Unicode value

        String name = null;
        if (getEncoding() != null)
        {
            name = encoding.getName(code);
            unicode = GlyphList.toUnicode(name);
            if (unicode != null)
            {
                return unicode;
            }
        }

        // if no value has been produced, there is no way to obtain Unicode for the character.
        if (LOG.isWarnEnabled() && !noUnicode.contains(code))
        {
            // we keep track of which warnings have been issued, so we don't log multiple times
            noUnicode.add(code);
            if (name != null)
            {
                LOG.warn("No Unicode mapping for " + name + " (" + code + ") in font " +
                        getBaseFont());
            }
            else
            {
                LOG.warn("No Unicode mapping for character code " + code + " in font " +
                        getBaseFont());
            }
        }

        return null;
    }
}

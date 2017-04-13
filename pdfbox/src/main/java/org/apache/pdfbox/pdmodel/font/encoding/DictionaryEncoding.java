/*
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
package org.apache.pdfbox.pdmodel.font.encoding;

import java.util.HashMap;
import java.util.Map;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNumber;

/**
 * This will perform the encoding from a dictionary.
 *
 * @author Ben Litchfield
 */
public class DictionaryEncoding extends Encoding
{
    private final COSDictionary encoding;
    private final Encoding baseEncoding;
    private final Map<Integer, String> differences = new HashMap<>();

    /**
     * Creates a new DictionaryEncoding for embedding.
     *
     * @param baseEncoding
     * @param differences
     */
    public DictionaryEncoding(COSName baseEncoding, COSArray differences)
    {
        encoding = new COSDictionary();
        encoding.setItem(COSName.NAME, COSName.ENCODING);
        encoding.setItem(COSName.DIFFERENCES, differences);
        if (baseEncoding != COSName.STANDARD_ENCODING)
        {
            encoding.setItem(COSName.BASE_ENCODING, baseEncoding);
            this.baseEncoding = Encoding.getInstance(baseEncoding);
        }
        else
        {
            this.baseEncoding = Encoding.getInstance(baseEncoding);
        }

        if (this.baseEncoding == null)
        {
            throw new IllegalArgumentException("Invalid encoding: " + baseEncoding);
        }
        
        codeToName.putAll(this.baseEncoding.codeToName);
        inverted.putAll(this.baseEncoding.inverted);
        applyDifferences();
    }

    /**
     * Creates a new DictionaryEncoding for a Type 3 font from a PDF.
     *
     * @param fontEncoding The Type 3 encoding dictionary.
     */
    public DictionaryEncoding(COSDictionary fontEncoding)
    {
        encoding = fontEncoding;
        baseEncoding = null;
        applyDifferences();
    }
    
    /**
     * Creates a new DictionaryEncoding from a PDF.
     *
     * @param fontEncoding The encoding dictionary.
     * @param isNonSymbolic True if the font is non-symbolic. False for Type 3 fonts.
     * @param builtIn The font's built-in encoding. Null for Type 3 fonts.
     */
    public DictionaryEncoding(COSDictionary fontEncoding, boolean isNonSymbolic, Encoding builtIn)
    {
        encoding = fontEncoding;

        Encoding base = null;
        boolean hasBaseEncoding = encoding.containsKey(COSName.BASE_ENCODING);
        if (hasBaseEncoding)
        {
            COSName name = encoding.getCOSName(COSName.BASE_ENCODING);
            base = Encoding.getInstance(name); // null when the name is invalid
        }

        if (base == null)
        {
            if (isNonSymbolic)
            {
                // Otherwise, for a nonsymbolic font, it is StandardEncoding
                base = StandardEncoding.INSTANCE;
            }
            else
            {
                // and for a symbolic font, it is the font's built-in encoding.
                if (builtIn != null)
                {
                    base = builtIn;
                }
                else
                {
                    // triggering this error indicates a bug in PDFBox. Every font should always have
                    // a built-in encoding, if not, we parsed it incorrectly.
                    throw new IllegalArgumentException("Symbolic fonts must have a built-in " + 
                                                       "encoding");
                }
            }
        }
        baseEncoding = base;

        codeToName.putAll(baseEncoding.codeToName);
        inverted.putAll(baseEncoding.inverted);
        applyDifferences();
    }

    private void applyDifferences()
    {
        // now replace with the differences
        COSBase base = encoding.getDictionaryObject(COSName.DIFFERENCES);
        if (!(base instanceof COSArray))
        {
            return;
        }
        COSArray diffArray = (COSArray) base;
        int currentIndex = -1;
        for (int i = 0; i < diffArray.size(); i++)
        {
            COSBase next = diffArray.getObject(i);
            if( next instanceof COSNumber)
            {
                currentIndex = ((COSNumber)next).intValue();
            }
            else if( next instanceof COSName )
            {
                COSName name = (COSName)next;
                overwrite(currentIndex, name.getName());
                this.differences.put(currentIndex, name.getName());
                currentIndex++;
            }
        }
    }

    /**
     * Returns the base encoding. Will be null for Type 3 fonts.
     */
    public Encoding getBaseEncoding()
    {
        return baseEncoding;
    }

    /**
     * Returns the Differences array.
     */
    public Map<Integer, String> getDifferences()
    {
        return differences;
    }

    @Override
    public COSBase getCOSObject()
    {
        return encoding;
    }

    @Override
    public String getEncodingName()
    {
        return baseEncoding.getEncodingName() + " with differences";
    }
}

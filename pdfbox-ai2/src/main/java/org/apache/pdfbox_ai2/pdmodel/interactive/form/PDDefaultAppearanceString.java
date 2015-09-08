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
package org.apache.pdfbox_ai2.pdmodel.interactive.form;

import java.io.IOException;
import java.util.List;
import org.apache.pdfbox_ai2.contentstream.operator.Operator;
import org.apache.pdfbox_ai2.cos.COSName;
import org.apache.pdfbox_ai2.cos.COSNumber;
import org.apache.pdfbox_ai2.cos.COSString;
import org.apache.pdfbox_ai2.pdfparser.PDFStreamParser;
import org.apache.pdfbox_ai2.pdmodel.PDPageContentStream;
import org.apache.pdfbox_ai2.pdmodel.PDResources;
import org.apache.pdfbox_ai2.pdmodel.font.PDFont;
import org.apache.pdfbox_ai2.pdmodel.interactive.annotation.PDAppearanceStream;

/**
 * Represents a default appearance string, as found in the /DA entry of free text annotations.
 * 
 * <p>The default appearance string (DA) contains any graphics state or text state operators needed
 * to establish the graphics state parameters, such as text size and colour, for displaying the
 * field’s variable text. Only operators that are allowed within text objects shall occur in this
 * string.
 * 
 * Note: This class is not yet public, as its API is still unstable.
 */
class PDDefaultAppearanceString
{
    /**
     * The default font size used by Acrobat.
     */
    private static final float DEFAULT_FONT_SIZE = 12;
    
    private final List<Object> tokens;
    private final PDResources defaultResources;
    
    /**
     * Constructor for reading an existing DA string.
     * 
     * @param defaultResources DR entry
     * @param defaultAppearance DA entry
     * @throws IOException If the DA could not be parsed
     */
    PDDefaultAppearanceString(COSString defaultAppearance, PDResources defaultResources) throws IOException
    {
        if (defaultAppearance == null)
        {
            throw new IllegalArgumentException("/DA is a required entry");
        }
        
        if (defaultResources == null)
        {
            throw new IllegalArgumentException("/DR is a required entry");
        }
        
        PDFStreamParser parser = new PDFStreamParser(defaultAppearance.getBytes());
        parser.parse();
        tokens = parser.getTokens();
        
        this.defaultResources = defaultResources;
    }
    
    /**
     * Returns the font size.
     */
    public float getFontSize()
    {
        if (!tokens.isEmpty())
        {
            // daString looks like "BMC /Helv 3.4 Tf EMC"
            // use the fontsize of the default existing apperance stream
            int fontIndex = tokens.indexOf(Operator.getOperator("Tf"));
            if (fontIndex != -1)
            {
                return ((COSNumber) tokens.get(fontIndex - 1)).floatValue();
            }
        }
        
        return DEFAULT_FONT_SIZE;
    }
    
    /**
     * w in an appearance stream represents the lineWidth.
     *
     * @return the linewidth
     */
    public float getLineWidth()
    {
        float retval = 0f;
        if (tokens != null)
        {
            int btIndex = tokens.indexOf(Operator.getOperator("BT"));
            int wIndex = tokens.indexOf(Operator.getOperator("w"));
            // the w should only be used if it is before the first BT.
            if (wIndex > 0 && (wIndex < btIndex || btIndex == -1))
            {
                retval = ((COSNumber) tokens.get(wIndex - 1)).floatValue();
            }
        }
        return retval;
    }

    /**
     * Returns the font.
     * 
     * @throws IOException If the font could not be found.
     */
    public PDFont getFont() throws IOException
    {
        COSName name = getFontResourceName();
        PDFont font = defaultResources.getFont(name);
        
        // todo: handle cases where font == null with special mapping logic (see PDFBOX-2661)
        if (font == null)
        {
            throw new IOException("Could not find font: /" + name.getName());
        }
        
        return font;
    }

    /**
     * Returns the name of the font in the Resources.
     */
    private COSName getFontResourceName()
    {
        int setFontOperatorIndex = tokens.indexOf(Operator.getOperator("Tf"));
        return (COSName) tokens.get(setFontOperatorIndex - 2);
    }

    /**
     * Writes the DA string to the given content stream.
     */
    void writeTo(PDPageContentStream contents, float zeroFontSize) throws IOException
    {
        float fontSize = getFontSize();
        if (fontSize == 0)
        {
            fontSize = zeroFontSize;
        }
        contents.setFont(getFont(), fontSize);
        // todo: set more state...
    }

    /**
     * Copies any needed resources from the document’s DR dictionary into the stream’s Resources
     * dictionary. Resources with the same name shall be left intact.
     */
    void copyNeededResourcesTo(PDAppearanceStream appearanceStream) throws IOException
    {
        // make sure we have resources
        PDResources streamResources = appearanceStream.getResources();
        if (streamResources == null)
        {
            streamResources = new PDResources();
            appearanceStream.setResources(streamResources);
        }
        
        // fonts
        COSName fontName = getFontResourceName();
        if (streamResources.getFont(fontName) == null)
        {
            streamResources.put(fontName, getFont());
        }

        // todo: other kinds of resource...
    }
}

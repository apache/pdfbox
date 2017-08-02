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
package org.apache.pdfbox.pdmodel.interactive.form;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.pdfparser.PDFStreamParser;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceCMYK;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceGray;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceContentStream;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceStream;

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
    
    private final PDResources defaultResources;
    
    private COSName fontName;
    private PDFont font;
    private float fontSize = DEFAULT_FONT_SIZE;
    private PDColor fontColor;
    
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
        
        this.defaultResources = defaultResources;
        processAppearanceStringOperators(defaultAppearance.getBytes());
    }
    
    /**
     * Processes the operators of the given content stream.
     *
     * @param content the content to parse.
     * @throws IOException if there is an error reading or parsing the content stream.
     */
    private void processAppearanceStringOperators(byte[] content) throws IOException
    {
        List<COSBase> arguments = new ArrayList<>();
        PDFStreamParser parser = new PDFStreamParser(content);
        Object token = parser.parseNextToken();
        while (token != null)
        {
            if (token instanceof COSObject)
            {
                arguments.add(((COSObject) token).getObject());
            }
            else if (token instanceof Operator)
            {
                processOperator((Operator) token, arguments);
                arguments = new ArrayList<>();
            }
            else
            {
                arguments.add((COSBase) token);
            }
            token = parser.parseNextToken();
        }
    }
    
    /**
     * This is used to handle an operation.
     * 
     * @param operator The operation to perform.
     * @param operands The list of arguments.
     * @throws IOException If there is an error processing the operation.
     */
    private void processOperator(Operator operator, List<COSBase> operands) throws IOException
    {
        switch (operator.getName())
        {
            case "Tf":
                processSetFont(operands);
                break;
            case "g":
            case "rg":
            case "k":
                processSetFontColor(operands);
                break;
            default:
                break;
        }
    }
    
    /**
     * Process the set font and font size operator.
     * 
     * @param operands the font name and size
     * @throws IOException in case there are missing operators or the font is not within the resources
     */
    private void processSetFont(List<COSBase> operands) throws IOException
    {
        if (operands.size() < 2)
        {
            throw new IOException("Missing operands for set font operator " + Arrays.toString(operands.toArray()));
        }

        COSBase base0 = operands.get(0);
        COSBase base1 = operands.get(1);
        if (!(base0 instanceof COSName))
        {
            return;
        }
        if (!(base1 instanceof COSNumber))
        {
            return;
        }
        COSName fontName = (COSName) base0;
        
        PDFont font = defaultResources.getFont(fontName);
        float fontSize = ((COSNumber) base1).floatValue();
        
        // todo: handle cases where font == null with special mapping logic (see PDFBOX-2661)
        if (font == null)
        {
            throw new IOException("Could not find font: /" + fontName.getName());
        }
        setFontName(fontName);
        setFont(font);
        setFontSize(fontSize);
    }
    
    /**
     * Process the font color operator.
     * 
     * This is assumed to be an RGB color.
     * 
     * @param operands the color components
     * @throws IOException in case of the color components not matching
     */
    private void processSetFontColor(List<COSBase> operands) throws IOException
    {
        PDColorSpace colorSpace;

        switch (operands.size())
        {
            case 1:
                colorSpace = PDDeviceGray.INSTANCE;
                break;
            case 3:
                colorSpace = PDDeviceRGB.INSTANCE;
                break;
            case 4:
                colorSpace = PDDeviceCMYK.INSTANCE;
                break;
            default:
                throw new IOException("Missing operands for set non stroking color operator " + Arrays.toString(operands.toArray()));
        }
        COSArray array = new COSArray();
        array.addAll(operands);
        setFontColor(new PDColor(array, colorSpace));
    }

    /**
     * Get the font name
     * 
     * @return the font name to use for resource lookup
     */
    COSName getFontName()
    {
        return fontName;
    }

    /**
     * Set the font name.
     * 
     * @param fontName the font name to use for resource lookup
     */
    void setFontName(COSName fontName)
    {
        this.fontName = fontName;
    }
    
    /**
     * Returns the font.
     */
    PDFont getFont() throws IOException
    {
        return font;
    }
    
    /**
     * Set the font.
     * 
     * @param font the font to use.
     */
    void setFont(PDFont font)
    {
        this.font = font;
    }

    /**
     * Returns the font size.
     */
    public float getFontSize()
    {
        return fontSize;
    }
    
    /**
     * Set the font size.
     * 
     * @param fontSize the font size.
     */
    void setFontSize(float fontSize)
    {
        this.fontSize = fontSize;
    }
    
    /**
     * Returns the font color
     */
    PDColor getFontColor()
    {
        return fontColor;
    }

    /**
     * Set the font color.
     * 
     * @param fontColor the fontColor to use.
     */
    void setFontColor(PDColor fontColor)
    {
        this.fontColor = fontColor;
    }

    /**
     * Writes the DA string to the given content stream.
     */
    void writeTo(PDAppearanceContentStream contents, float zeroFontSize) throws IOException
    {
        float fontSize = getFontSize();
        if (fontSize == 0)
        {
            fontSize = zeroFontSize;
        }
        contents.setFont(getFont(), fontSize);
        
        if (getFontColor() != null)
        {
            contents.setNonStrokingColor(getFontColor());
        }
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
        
        if (streamResources.getFont(getFontName()) == null)
        {
            streamResources.put(fontName, getFont());
        }

        // todo: other kinds of resource...
    }
}

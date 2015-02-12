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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.pdfparser.PDFStreamParser;

/**
 * The default appearance, an inheritable attribute contained in the dictionaries
 * /DA entry, contains any graphics state or text state operators needed
 * to establish the graphics state parameters, such as text size
 * and color, for displaying the field’s variable text.
 * <p>
 * Allowed operators are all which are permitted in text objects.
 * The Tf operator is required specifying the font and the font size
 * </p>
 * 
 * <p>
 * <strong>Currently only the Tf operator is abstracted through this class!</strong>
 * </p>
 */
class DefaultAppearanceHandler
{
    /**
     * The tokens making up the content of the default appearance string.
     */
    private List<Object> appearanceTokens;
    
    DefaultAppearanceHandler(String defaultApperanceString) throws IOException
    {
        appearanceTokens = getStreamTokens(defaultApperanceString);
    }
    
    /**
     * Get the font size.
     * @return resolved font size.
     */
    float getFontSize()
    {
        if (!appearanceTokens.isEmpty())
        {
            // daString looks like "BMC /Helv 3.4 Tf EMC"
            // use the fontsize of the default existing apperance stream
            int fontIndex = appearanceTokens.indexOf(Operator.getOperator("Tf"));
            if (fontIndex != -1)
            {
                return ((COSNumber) appearanceTokens.get(fontIndex - 1)).floatValue();
            }
        }
        return 0f;
    }
    
    /**
     * Set the font size.
     * @param fontSize the font size for the Tf operator
     */
    void setFontSize(float fontSize)
    {
        int fontIndex = appearanceTokens.indexOf(Operator.getOperator("Tf"));
        if (fontIndex != -1)
        {
            appearanceTokens.set(fontIndex - 1, new COSFloat(fontSize));
        }
    }
    /**
     * Get the font name.
     * @return the resolved font name.
     */
    COSName getFontName()
    {
        int setFontOperatorIndex = appearanceTokens.indexOf(Operator.getOperator("Tf"));
        return (COSName) appearanceTokens.get(setFontOperatorIndex - 2);
    }
    
    /**
     * Get the tokens resolved from the default appearance string.
     * @return the resolved tokens
     */
    List<Object> getTokens()
    {
        return appearanceTokens;
    }
    
    private List<Object> getStreamTokens(String defaultAppearanceString) throws IOException
    {
        List<Object> tokens = new ArrayList<Object>();
        if (defaultAppearanceString != null && !defaultAppearanceString.isEmpty())
        {
            PDFStreamParser parser = null;
            ByteArrayInputStream stream = new ByteArrayInputStream(defaultAppearanceString.getBytes());
            parser = new PDFStreamParser(stream);
            parser.parse();
            tokens = parser.getTokens();
            parser.close();
        }
        return tokens;
    }
}

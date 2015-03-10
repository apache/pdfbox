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

import org.apache.pdfbox.pdmodel.font.PDFont;

/**
 * Define styling attributes to be used for text formatting.
 * 
 */
class AppearanceStyle
{
    private PDFont font;
    /**
     * The font size to be used for text formatting.
     *
     * Defaulting to 12 to match Acrobats default.
     */
    private float fontSize = 12.0f;
    
    /**
     * The leading (distance between lines) to be used for text formatting.
     *
     * Defaulting to 1.2*fontSize to match Acrobats default.
     */
    private float leading = 14.4f;
    
    /**
     * Get the font used for text formatting.
     * 
     * @return the font used for text formatting.
     */
    PDFont getFont()
    {
        return font;
    }
    
    /**
     * Set the font to be used for text formatting.
     * 
     * @param font the font to be used.
     */
    void setFont(PDFont font)
    {
        this.font = font;
    }
    
    /**
     * Get the fontSize used for text formatting.
     * 
     * @return the fontSize used for text formatting.
     */
    float getFontSize()
    {
        return fontSize;
    }
    
    /**
     * Set the font size to be used for formatting.
     * 
     * @param fontSize the font size.
     */
    void setFontSize(float fontSize)
    {
        this.fontSize = fontSize;
        leading = fontSize * 1.2f;
    }

    /**
     * Get the leading used for text formatting.
     * 
     * @return the leading used for text formatting.
     */
    float getLeading()
    {
        return leading;
    }
    
    /**
     * Set the leading used for text formatting.
     * 
     * @param leading the leading to be used.
     */
    void setLeading(float leading)
    {
        this.leading = leading;
    }
}
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

import org.apache.pdfbox.pdmodel.font.PDFont;

class AppearanceStyle
{
    private static final float FONTSCALE = 1000f;

    private PDFont font;
    private float fontSize;
    private float leading;
    
    PDFont getFont()
    {
        return font;
    }
    
    void setFont(PDFont font)
    {
        this.font = font;
    }
    
    float getFontSize()
    {
        return fontSize;
    }
    
    void setFontSize(float fontSize)
    {
        final float scale = fontSize/FONTSCALE;
        this.fontSize = fontSize;
        if (leading == 0)
        {
            try
            {
                leading = font.getBoundingBox().getHeight() * scale;
            }
            catch (IOException e)
            {
                leading = fontSize * 1.2f;
            }
        }
    }
    
    float getLeading()
    {
        return leading;
    }
    
    void setLeading(float leading)
    {
        this.leading = leading;
    }
}
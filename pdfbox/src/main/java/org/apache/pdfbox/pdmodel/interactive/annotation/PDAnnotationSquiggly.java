/*
 * Copyright 2018 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.pdfbox.pdmodel.interactive.annotation;

import org.apache.pdfbox.pdmodel.interactive.annotation.handlers.PDSquigglyAppearanceHandler;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.pdmodel.interactive.annotation.handlers.PDAppearanceHandler;

/**
 *
 * @author Paul King
 */
public class PDAnnotationSquiggly extends PDAnnotationTextMarkup
{
    /**
     * The type of annotation.
     */
    public static final String SUB_TYPE = "Squiggly";

    private PDAppearanceHandler customAppearanceHandler;

     /**
     * Constructor.
     */
    public PDAnnotationSquiggly()
    {
        super(SUB_TYPE);
    }

    /**
     * Constructor.
     *
     * @param dict The annotations dictionary.
     */
    public PDAnnotationSquiggly(COSDictionary dict)
    {
        super(dict);
    }

    /**
     * Set a custom appearance handler for generating the annotations appearance streams.
     * 
     * @param appearanceHandler
     */
    public void setCustomAppearanceHandler(PDAppearanceHandler appearanceHandler)
    {
        customAppearanceHandler = appearanceHandler;
    }

    @Override
    public void constructAppearances()
    {
        if (customAppearanceHandler == null)
        {
            PDSquigglyAppearanceHandler appearanceHandler = new PDSquigglyAppearanceHandler(this);
            appearanceHandler.generateAppearanceStreams();
        }
        else
        {
            customAppearanceHandler.generateAppearanceStreams();
        }
    }
}

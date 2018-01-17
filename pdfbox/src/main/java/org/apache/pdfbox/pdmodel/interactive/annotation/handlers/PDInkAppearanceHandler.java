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

package org.apache.pdfbox.pdmodel.interactive.annotation.handlers;

import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;

/**
 * Handler to generate the ink annotations appearance.
 *
 */
public class PDInkAppearanceHandler extends PDAbstractAppearanceHandler
{
    public PDInkAppearanceHandler(PDAnnotation annotation)
    {
        super(annotation);
    }

    @Override
    public void generateAppearanceStreams()
    {
        generateNormalAppearance();
        generateRolloverAppearance();
        generateDownAppearance();
    }

    @Override
    public void generateNormalAppearance()
    {
        //TODO
    }

    @Override
    public void generateRolloverAppearance()
    {
        // No rollover appearance generated for a polygon annotation
    }

    @Override
    public void generateDownAppearance()
    {
        // No down appearance generated for a polygon annotation
    }
}

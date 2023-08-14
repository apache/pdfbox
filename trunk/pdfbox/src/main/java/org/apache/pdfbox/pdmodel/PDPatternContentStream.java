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
package org.apache.pdfbox.pdmodel;

import java.io.IOException;

import org.apache.pdfbox.pdmodel.graphics.pattern.PDTilingPattern;

/**
 *
 * @author Tilman Hausherr
 */
public final class PDPatternContentStream extends PDAbstractContentStream
{
    /**
     * Create a new tiling pattern content stream.
     *
     * @param pattern The tiling pattern stream to write to.
     * 
     * @throws IOException If there is an error writing to the form contents.
     */
    public PDPatternContentStream(PDTilingPattern pattern) throws IOException
    {
        super(null, pattern.getContentStream().createOutputStream(), pattern.getResources());
    }
}

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
package org.apache.pdfbox.debugger.pagepane;

import java.io.IOException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.rendering.PageDrawer;
import org.apache.pdfbox.rendering.PageDrawerParameters;

/**
 * A custom PDFRenderer which creates instances of DebugPageDrawer.
 *
 * @author John Hewson
 */
final class DebugPDFRenderer extends PDFRenderer
{
    private final boolean showGlyphBounds;
    
    DebugPDFRenderer(PDDocument document, boolean showGlyphBounds)
    {
        super(document);
        this.showGlyphBounds = showGlyphBounds;
    }

    @Override
    protected PageDrawer createPageDrawer(PageDrawerParameters parameters) throws IOException
    {
        return new DebugPageDrawer(parameters, this.showGlyphBounds);
    }
}

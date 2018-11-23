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

package org.apache.pdfbox.rendering;

import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;

/**
 * Parameters for a PageDrawer. This class ensures allows PDFRenderer and PageDrawer to share
 * private implementation data in a future-proof manner, while still allowing end-users to create
 * their own subclasses of PageDrawer.
 * 
 * @author John Hewson
 */
public final class PageDrawerParameters
{
    private final PDFRenderer renderer;
    private final PDPage page;
    private final boolean subsamplingAllowed;
    private final PDColorSpace colorSpace;
    private final int component;

    /**
     * Package-private constructor.
     */
    PageDrawerParameters(PDFRenderer renderer, PDPage page, boolean subsamplingAllowed, PDColorSpace colorSpace, int component)
    {
        this.renderer = renderer;
        this.page = page;
        this.subsamplingAllowed = subsamplingAllowed;
        this.colorSpace = colorSpace;
        this.component = component;
    }

    /**
     * Returns the page.
     */
    public PDPage getPage()
    {
        return page;
    }
    
    /**
     * Returns the renderer.
     */
    PDFRenderer getRenderer()
    {
        return renderer;
    }

    /**
     * Returns whether to allow subsampling of images.
     */
    public boolean isSubsamplingAllowed()
    {
        return subsamplingAllowed;
    }

    /**
     * Returns the separation color space or null when rendering composite
     */
    public PDColorSpace getColorSpace() {
        return colorSpace;
    }

    /**
     * Returns the component of the separation color space or -1 when rendering all components
     */
    public int getComponent() {
        return component;
    }
}

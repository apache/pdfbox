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

import java.awt.RenderingHints;

import org.apache.pdfbox.pdmodel.PDPage;

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
    private final RenderDestination destination; 
    private final RenderingHints renderingHints;
    private final float imageDownscalingOptimizationThreshold;

    /**
     * Package-private constructor.
     */
    PageDrawerParameters(PDFRenderer renderer, PDPage page, boolean subsamplingAllowed,
                         RenderDestination destination, RenderingHints renderingHints,
                         float imageDownscalingOptimizationThreshold)
    {
        this.renderer = renderer;
        this.page = page;
        this.subsamplingAllowed = subsamplingAllowed;
        this.destination = destination;
        this.renderingHints = renderingHints;
        this.imageDownscalingOptimizationThreshold = imageDownscalingOptimizationThreshold;
    }

    /**
     * Returns the page.
     * 
     * @return the page
     */
    public PDPage getPage()
    {
        return page;
    }
    
    /**
     * Returns the renderer.
     * 
     * @return the renderer
     */
    PDFRenderer getRenderer()
    {
        return renderer;
    }

    /**
     * Returns whether to allow subsampling of images.
     * 
     * @return true if subsampling of images os allowed
     */
    public boolean isSubsamplingAllowed()
    {
        return subsamplingAllowed;
    }

    /**
     * @return the destination
     */
    public RenderDestination getDestination()
    {
        return this.destination;
    }

    /**
     * @return the rendering hints.
     */
    public RenderingHints getRenderingHints()
    {
        return renderingHints;
    }

    /**
     * 
     * @return the imageDownscalingOptimizationThreshold
     */

    public float getImageDownscalingOptimizationThreshold()
    {
        return imageDownscalingOptimizationThreshold;
    }
}

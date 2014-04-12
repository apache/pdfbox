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
package org.apache.pdfbox.tools.gui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import java.io.IOException;

import javax.swing.JPanel;

import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.pdmodel.PDPage;

import org.apache.pdfbox.pdmodel.common.PDRectangle;

/**
 * This is a simple JPanel that can be used to display a PDF page.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.4 $
 */
public class PDFPagePanel extends JPanel
{
    private static final long serialVersionUID = -4629033339560890669L;

    private PDFRenderer renderer;
    private PDPage page;
    private int pageNum;
    private Dimension pageDimension = null;
    private Dimension drawDimension = null;

    /**
     * This will set the page that should be displayed in this panel.
     *
     * @param renderer The renderer to render the page.
     * @param page The PDF page to display.
     * @param pageNum The number of the page.
     * @throws IOException if something goes wrong.
     */
    public void setPage(PDFRenderer renderer, PDPage page, int pageNum) throws IOException
    {
        this.renderer = renderer;
        this.page = page;
        this.pageNum = pageNum;

        PDRectangle cropBox = page.findCropBox();
        drawDimension = cropBox.createDimension();
        int rotation = page.findRotation();
        if (rotation == 90 || rotation == 270)
        {
            pageDimension = new Dimension(drawDimension.height, drawDimension.width);
        }
        else
        {
            pageDimension = drawDimension;
        }
        setSize(pageDimension);
        setBackground(java.awt.Color.white);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void paint(Graphics g)
    {
        try
        {
            g.setColor(getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());
            renderer.renderPageToGraphics(pageNum, (Graphics2D) g);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}

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

package org.apache.pdfbox.examples.rendering;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.contentstream.PDFGraphicsStreamEngine;
import org.apache.pdfbox.contentstream.PDFStreamEngine;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.image.PDImage;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.util.Matrix;
import org.apache.pdfbox.util.Vector;

/**
 * Example of a custom PDFGraphicsStreamEngine subclass. Allows text and graphics to be processed
 * in a custom manner. This example simply prints the operations to stdout.
 *
 * <p>See {@link PDFStreamEngine} for further methods which may be overridden.
 * 
 * @author John Hewson
 */
public class CustomGraphicsStreamEngine extends PDFGraphicsStreamEngine
{
    /**
     * Constructor.
     *
     * @param page PDF Page
     */
    protected CustomGraphicsStreamEngine(final PDPage page)
    {
        super(page);
    }

    public static void main(final String[] args) throws IOException
    {
        final File file = new File("src/main/resources/org/apache/pdfbox/examples/rendering/",
                             "custom-render-demo.pdf");

        try (PDDocument doc = Loader.loadPDF(file))
        {
            final PDPage page = doc.getPage(0);
            final CustomGraphicsStreamEngine engine = new CustomGraphicsStreamEngine(page);
            engine.run();
        }
    }
    
    /**
     * Runs the engine on the current page.
     *
     * @throws IOException If there is an IO error while drawing the page.
     */
    public void run() throws IOException
    {
        processPage(getPage());

        for (final PDAnnotation annotation : getPage().getAnnotations())
        {
            showAnnotation(annotation);
        }
    }
    
    @Override
    public void appendRectangle(final Point2D p0, final Point2D p1, final Point2D p2, final Point2D p3) throws IOException
    {
        System.out.printf("appendRectangle %.2f %.2f, %.2f %.2f, %.2f %.2f, %.2f %.2f%n",
                p0.getX(), p0.getY(), p1.getX(), p1.getY(),
                p2.getX(), p2.getY(), p3.getX(), p3.getY());
    }

    @Override
    public void drawImage(final PDImage pdImage) throws IOException
    {
        System.out.println("drawImage");
    }

    @Override
    public void clip(final int windingRule) throws IOException
    {
        System.out.println("clip");
    }

    @Override
    public void moveTo(final float x, final float y) throws IOException
    {
        System.out.printf("moveTo %.2f %.2f%n", x, y);
    }

    @Override
    public void lineTo(final float x, final float y) throws IOException
    {
        System.out.printf("lineTo %.2f %.2f%n", x, y);
    }

    @Override
    public void curveTo(final float x1, final float y1, final float x2, final float y2, final float x3, final float y3) throws IOException
    {
        System.out.printf("curveTo %.2f %.2f, %.2f %.2f, %.2f %.2f%n", x1, y1, x2, y2, x3, y3);
    }

    @Override
    public Point2D getCurrentPoint() throws IOException
    {
        // if you want to build paths, you'll need to keep track of this like PageDrawer does
        return new Point2D.Float(0, 0);
    }

    @Override
    public void closePath() throws IOException
    {
        System.out.println("closePath");
    }

    @Override
    public void endPath() throws IOException
    {
        System.out.println("endPath");
    }

    @Override
    public void strokePath() throws IOException
    {
        System.out.println("strokePath");
    }

    @Override
    public void fillPath(final int windingRule) throws IOException
    {
        System.out.println("fillPath");
    }

    @Override
    public void fillAndStrokePath(final int windingRule) throws IOException
    {
        System.out.println("fillAndStrokePath");
    }

    @Override
    public void shadingFill(final COSName shadingName) throws IOException
    {
        System.out.println("shadingFill " + shadingName.toString());
    }

    /**
     * Overridden from PDFStreamEngine.
     */
    @Override
    public void showTextString(final byte[] string) throws IOException
    {
        System.out.print("showTextString \"");
        super.showTextString(string);
        System.out.println("\"");
    }

    /**
     * Overridden from PDFStreamEngine.
     */
    @Override
    public void showTextStrings(final COSArray array) throws IOException
    {
        System.out.print("showTextStrings \"");
        super.showTextStrings(array);
        System.out.println("\"");
    }

    /**
     * Overridden from PDFStreamEngine.
     */
    @Override
    protected void showGlyph(final Matrix textRenderingMatrix, final PDFont font, final int code, final Vector displacement)
            throws IOException
    {
        System.out.print("showGlyph " + code);
        super.showGlyph(textRenderingMatrix, font, code, displacement);
    }
    
    // NOTE: there are may more methods in PDFStreamEngine which can be overridden here too.
}

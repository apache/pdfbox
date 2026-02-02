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
package org.apache.pdfbox.contentstream;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.graphics.image.PDImage;

import java.awt.geom.Point2D;
import java.io.IOException;
import org.apache.pdfbox.contentstream.operator.color.SetNonStrokingColor;
import org.apache.pdfbox.contentstream.operator.color.SetNonStrokingColorN;
import org.apache.pdfbox.contentstream.operator.color.SetNonStrokingColorSpace;
import org.apache.pdfbox.contentstream.operator.color.SetNonStrokingDeviceCMYKColor;
import org.apache.pdfbox.contentstream.operator.color.SetNonStrokingDeviceGrayColor;
import org.apache.pdfbox.contentstream.operator.color.SetNonStrokingDeviceRGBColor;
import org.apache.pdfbox.contentstream.operator.color.SetStrokingColor;
import org.apache.pdfbox.contentstream.operator.color.SetStrokingColorN;
import org.apache.pdfbox.contentstream.operator.color.SetStrokingColorSpace;
import org.apache.pdfbox.contentstream.operator.color.SetStrokingDeviceCMYKColor;
import org.apache.pdfbox.contentstream.operator.color.SetStrokingDeviceGrayColor;
import org.apache.pdfbox.contentstream.operator.color.SetStrokingDeviceRGBColor;
import org.apache.pdfbox.contentstream.operator.graphics.AppendRectangleToPath;
import org.apache.pdfbox.contentstream.operator.graphics.BeginInlineImage;
import org.apache.pdfbox.contentstream.operator.graphics.ClipEvenOddRule;
import org.apache.pdfbox.contentstream.operator.graphics.ClipNonZeroRule;
import org.apache.pdfbox.contentstream.operator.graphics.CloseAndStrokePath;
import org.apache.pdfbox.contentstream.operator.graphics.CloseFillEvenOddAndStrokePath;
import org.apache.pdfbox.contentstream.operator.graphics.CloseFillNonZeroAndStrokePath;
import org.apache.pdfbox.contentstream.operator.graphics.ClosePath;
import org.apache.pdfbox.contentstream.operator.graphics.CurveTo;
import org.apache.pdfbox.contentstream.operator.graphics.CurveToReplicateFinalPoint;
import org.apache.pdfbox.contentstream.operator.graphics.CurveToReplicateInitialPoint;
import org.apache.pdfbox.contentstream.operator.graphics.DrawObject;
import org.apache.pdfbox.contentstream.operator.graphics.EndPath;
import org.apache.pdfbox.contentstream.operator.graphics.FillEvenOddAndStrokePath;
import org.apache.pdfbox.contentstream.operator.graphics.FillEvenOddRule;
import org.apache.pdfbox.contentstream.operator.graphics.FillNonZeroAndStrokePath;
import org.apache.pdfbox.contentstream.operator.graphics.FillNonZeroRule;
import org.apache.pdfbox.contentstream.operator.graphics.LegacyFillNonZeroRule;
import org.apache.pdfbox.contentstream.operator.graphics.LineTo;
import org.apache.pdfbox.contentstream.operator.graphics.MoveTo;
import org.apache.pdfbox.contentstream.operator.graphics.ShadingFill;
import org.apache.pdfbox.contentstream.operator.graphics.StrokePath;
import org.apache.pdfbox.contentstream.operator.markedcontent.BeginMarkedContentSequence;
import org.apache.pdfbox.contentstream.operator.markedcontent.BeginMarkedContentSequenceWithProperties;
import org.apache.pdfbox.contentstream.operator.markedcontent.EndMarkedContentSequence;
import org.apache.pdfbox.contentstream.operator.state.Concatenate;
import org.apache.pdfbox.contentstream.operator.state.Restore;
import org.apache.pdfbox.contentstream.operator.state.Save;
import org.apache.pdfbox.contentstream.operator.state.SetFlatness;
import org.apache.pdfbox.contentstream.operator.state.SetGraphicsStateParameters;
import org.apache.pdfbox.contentstream.operator.state.SetLineCapStyle;
import org.apache.pdfbox.contentstream.operator.state.SetLineDashPattern;
import org.apache.pdfbox.contentstream.operator.state.SetLineJoinStyle;
import org.apache.pdfbox.contentstream.operator.state.SetLineMiterLimit;
import org.apache.pdfbox.contentstream.operator.state.SetLineWidth;
import org.apache.pdfbox.contentstream.operator.state.SetMatrix;
import org.apache.pdfbox.contentstream.operator.state.SetRenderingIntent;
import org.apache.pdfbox.contentstream.operator.text.BeginText;
import org.apache.pdfbox.contentstream.operator.text.EndText;
import org.apache.pdfbox.contentstream.operator.text.SetFontAndSize;
import org.apache.pdfbox.contentstream.operator.text.SetTextHorizontalScaling;
import org.apache.pdfbox.contentstream.operator.text.ShowTextAdjusted;
import org.apache.pdfbox.contentstream.operator.text.ShowTextLine;
import org.apache.pdfbox.contentstream.operator.text.ShowTextLineAndSpace;
import org.apache.pdfbox.contentstream.operator.text.MoveText;
import org.apache.pdfbox.contentstream.operator.text.MoveTextSetLeading;
import org.apache.pdfbox.contentstream.operator.text.NextLine;
import org.apache.pdfbox.contentstream.operator.text.SetCharSpacing;
import org.apache.pdfbox.contentstream.operator.text.SetTextLeading;
import org.apache.pdfbox.contentstream.operator.text.SetTextRenderingMode;
import org.apache.pdfbox.contentstream.operator.text.SetTextRise;
import org.apache.pdfbox.contentstream.operator.text.SetWordSpacing;
import org.apache.pdfbox.contentstream.operator.text.ShowText;

/**
 * PDFStreamEngine subclass for advanced processing of graphics.
 * This class should be subclassed by end users looking to hook into graphics operations.
 *
 * @author John Hewson
 */
public abstract class PDFGraphicsStreamEngine extends PDFStreamEngine
{
    // may be null, for example if the stream is a tiling pattern
    private final PDPage page;

    /**
     * Constructor.
     * 
     * @param page the page the content stream belongs to
     */
    protected PDFGraphicsStreamEngine(PDPage page)
    {
        this.page = page;

        addOperator(new CloseFillNonZeroAndStrokePath(this));
        addOperator(new FillNonZeroAndStrokePath(this));
        addOperator(new CloseFillEvenOddAndStrokePath(this));
        addOperator(new FillEvenOddAndStrokePath(this));
        addOperator(new BeginInlineImage(this));
        addOperator(new BeginText(this));
        addOperator(new CurveTo(this));
        addOperator(new Concatenate(this));
        addOperator(new SetStrokingColorSpace(this));
        addOperator(new SetNonStrokingColorSpace(this));
        addOperator(new SetLineDashPattern(this));
        addOperator(new DrawObject(this)); // special graphics version
        addOperator(new EndText(this));
        addOperator(new FillNonZeroRule(this));
        addOperator(new LegacyFillNonZeroRule(this));
        addOperator(new FillEvenOddRule(this));
        addOperator(new SetStrokingDeviceGrayColor(this));
        addOperator(new SetNonStrokingDeviceGrayColor(this));
        addOperator(new SetGraphicsStateParameters(this));
        addOperator(new ClosePath(this));
        addOperator(new SetFlatness(this));
        addOperator(new SetLineJoinStyle(this));
        addOperator(new SetLineCapStyle(this));
        addOperator(new SetStrokingDeviceCMYKColor(this));
        addOperator(new SetNonStrokingDeviceCMYKColor(this));
        addOperator(new LineTo(this));
        addOperator(new MoveTo(this));
        addOperator(new SetLineMiterLimit(this));
        addOperator(new EndPath(this));
        addOperator(new Save(this));
        addOperator(new Restore(this));
        addOperator(new AppendRectangleToPath(this));
        addOperator(new SetStrokingDeviceRGBColor(this));
        addOperator(new SetNonStrokingDeviceRGBColor(this));
        addOperator(new SetRenderingIntent(this));
        addOperator(new CloseAndStrokePath(this));
        addOperator(new StrokePath(this));
        addOperator(new SetStrokingColor(this));
        addOperator(new SetNonStrokingColor(this));
        addOperator(new SetStrokingColorN(this));
        addOperator(new SetNonStrokingColorN(this));
        addOperator(new ShadingFill(this));
        addOperator(new NextLine(this));
        addOperator(new SetCharSpacing(this));
        addOperator(new MoveText(this));
        addOperator(new MoveTextSetLeading(this));
        addOperator(new SetFontAndSize(this));
        addOperator(new ShowText(this));
        addOperator(new ShowTextAdjusted(this));
        addOperator(new SetTextLeading(this));
        addOperator(new SetMatrix(this));
        addOperator(new SetTextRenderingMode(this));
        addOperator(new SetTextRise(this));
        addOperator(new SetWordSpacing(this));
        addOperator(new SetTextHorizontalScaling(this));
        addOperator(new CurveToReplicateInitialPoint(this));
        addOperator(new SetLineWidth(this));
        addOperator(new ClipNonZeroRule(this));
        addOperator(new ClipEvenOddRule(this));
        addOperator(new CurveToReplicateFinalPoint(this));
        addOperator(new ShowTextLine(this));
        addOperator(new ShowTextLineAndSpace(this));
        addOperator(new BeginMarkedContentSequence(this));
        addOperator(new BeginMarkedContentSequenceWithProperties(this));
        addOperator(new EndMarkedContentSequence(this));
    }

    /**
     * Returns the page.
     * 
     * @return the current page
     */
    protected final PDPage getPage()
    {
        return page;
    }

    /**
     * Append a rectangle to the current path.
     * 
     * @param p0 starting coordinate of the rectangle
     * @param p1 second coordinate of the rectangle
     * @param p2 third coordinate of the rectangle
     * @param p3 last coordinate of the rectangle
     * 
     * @throws IOException if the rectangle could not be appended
     */
    public abstract void appendRectangle(Point2D p0, Point2D p1,
                                         Point2D p2, Point2D p3) throws IOException;

    /**
     * Draw the image.
     *
     * @param pdImage The image to draw.
     * 
     * @throws IOException if the image could not be drawn
     */
    public abstract void drawImage(PDImage pdImage) throws IOException;

    /**
     * Modify the current clipping path by intersecting it with the current path. The clipping path will not be updated
     * until the succeeding painting operator is called.
     *
     * @param windingRule The winding rule which will be used for clipping.
     * 
     * @throws IOException if the clipping path could not be modified
     */
    public abstract void clip(int windingRule) throws IOException;

    /**
     * Starts a new path at (x,y).
     * 
     * @param x the x-coordinate to move to
     * @param y the y-coordinate to move to
     * 
     * @throws IOException if the something went wrong when moving to the given coordinate
     */
    public abstract void moveTo(float x, float y) throws IOException;

    /**
     * Draws a line from the current point to (x,y).
     * 
     * @param x the X-coordinate of the ending-point of the line to be drawn
     * @param y the Y-coordinate of the ending-point of the line to be drawn
     * 
     * @throws IOException if the line could not be drawn
     */
    public abstract void lineTo(float x, float y) throws IOException;

    /**
     * Draws a curve from the current point to (x3,y3) using (x1,y1) and (x2,y2) as control points.
     * 
     * @param x1 the X coordinate of the first B&eacute;zier control point
     * @param y1 the Y coordinate of the first B&eacute;zier control point
     * @param x2 the X coordinate of the second B&eacute;zier control point
     * @param y2 the Y coordinate of the second B&eacute;zier control point
     * @param x3 the X coordinate of the final end point
     * @param y3 the Y coordinate of the final end point
     * 
     * @throws IOException if the curve could not be drawn
     */
    public abstract void curveTo(float x1, float y1,
                                 float x2, float y2,
                                 float x3, float y3) throws IOException;

    /**
     * Returns the current point of the current path.
     * 
     * @return the current point or null
     * 
     * @throws IOException if the something went wrong when providing the current point
     */
    public abstract Point2D getCurrentPoint() throws IOException;

    /**
     * Closes the current path.
     * 
     * @throws IOException if the current path could not be closed
     */
    public abstract void closePath() throws IOException;

    /**
     * Ends the current path without filling or stroking it. The clipping path is updated here.
     * 
     * @throws IOException if the current path could not be ended
     */
    public abstract void endPath() throws IOException;

    /**
     * Stroke the path.
     *
     * @throws IOException If there is an IO error while stroking the path.
     */
    public abstract void strokePath() throws IOException;

    /**
     * Fill the path.
     *
     * @param windingRule The winding rule this path will use.
     * 
     * @throws IOException if the path could not be filled
     */
    public abstract void fillPath(int windingRule) throws IOException;

    /**
     * Fills and then strokes the path.
     *
     * @param windingRule The winding rule this path will use.
     * 
     * @throws IOException if the path could not be filled and stroke
     */
    public abstract void fillAndStrokePath(int windingRule) throws IOException;

    /**
     * Fill with Shading.
     *
     * @param shadingName The name of the Shading Dictionary to use for this fill instruction.
     * 
     * @throws IOException if the path could not be filled using the given shading
     */
    public abstract void shadingFill(COSName shadingName) throws IOException;
}

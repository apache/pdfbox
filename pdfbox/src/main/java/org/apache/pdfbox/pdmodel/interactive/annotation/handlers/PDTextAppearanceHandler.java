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

import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDAppearanceContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.blend.BlendMode;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationText;
import org.apache.pdfbox.util.Matrix;

/**
 *
 * @author Tilman Hausherr
 */
public class PDTextAppearanceHandler extends PDAbstractAppearanceHandler
{
    private static final Log LOG = LogFactory.getLog(PDTextAppearanceHandler.class);

    private static final Set<String> SUPPORTED_NAMES = new HashSet<>();

    static
    {
        SUPPORTED_NAMES.add(PDAnnotationText.NAME_NOTE);
        SUPPORTED_NAMES.add(PDAnnotationText.NAME_INSERT);
        SUPPORTED_NAMES.add(PDAnnotationText.NAME_CROSS);
        SUPPORTED_NAMES.add(PDAnnotationText.NAME_HELP);
        SUPPORTED_NAMES.add(PDAnnotationText.NAME_CIRCLE);
        SUPPORTED_NAMES.add(PDAnnotationText.NAME_PARAGRAPH);
        SUPPORTED_NAMES.add(PDAnnotationText.NAME_NEW_PARAGRAPH);
    }

    public PDTextAppearanceHandler(PDAnnotation annotation)
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
        PDAnnotationText annotation = (PDAnnotationText) getAnnotation();
        if (!SUPPORTED_NAMES.contains(annotation.getName()))
        {
            //TODO Comment, Key
            // BBox values:
            // key 18 18
            // Comment 18 18
            return;
        }

        try (PDAppearanceContentStream contentStream = getNormalAppearanceAsContentStream())
        {
            PDColor bgColor = getColor();
            if (bgColor == null)
            {
                // White is used by Adobe when /C entry is missing
                contentStream.setNonStrokingColor(1f);
            }
            else
            {
                contentStream.setNonStrokingColor(bgColor);
            }
            // stroking color is always black which is the PDF default

            setOpacity(contentStream, annotation.getConstantOpacity());

            switch (annotation.getName())
            {
                case PDAnnotationText.NAME_NOTE:
                    drawNote(annotation, contentStream);
                    break;
                case PDAnnotationText.NAME_CROSS:
                    drawCross(annotation, contentStream);
                    break;
                case PDAnnotationText.NAME_CIRCLE:
                    drawCircles(annotation, contentStream);
                    break;
                case PDAnnotationText.NAME_INSERT:
                    drawInsert(annotation, contentStream);
                    break;
                case PDAnnotationText.NAME_HELP:
                    drawHelp(annotation, contentStream);
                    break;
                case PDAnnotationText.NAME_PARAGRAPH:
                    drawParagraph(annotation, contentStream);
                    break;
                case PDAnnotationText.NAME_NEW_PARAGRAPH:
                    drawNewParagraph(annotation, contentStream);
                    break;
                default:
                    break;
            }
        }
        catch (IOException e)
        {
            LOG.error(e);
        }
    }

    private PDRectangle adjustRectAndBBox(PDAnnotationText annotation, float width, float height)
    {
        // For /Note (other types have different values):
        // Adobe takes the left upper bound as anchor, and adjusts the rectangle to 18 x 20.
        // Observed with files 007071.pdf, 038785.pdf, 038787.pdf,
        // but not with 047745.pdf p133 and 084374.pdf p48, both have the NoZoom flag.
        // there the BBox is also set to fixed values, but the rectangle is left untouched.
        // When no flags are there, Adobe sets /F 24 = NoZoom NoRotate.
            
        PDRectangle rect = getRectangle();
        PDRectangle bbox;
        if (!annotation.isNoZoom())
        {
            rect.setUpperRightX(rect.getLowerLeftX() + width);
            rect.setLowerLeftY(rect.getUpperRightY() - height);
            annotation.setRectangle(rect);
        }
        if (!annotation.getCOSObject().containsKey(COSName.F))
        {
            // We set these flags because Adobe does so, but PDFBox doesn't support them when rendering.
            annotation.setNoRotate(true);
            annotation.setNoZoom(true);
        }
        bbox = new PDRectangle(width, height);
        annotation.getNormalAppearanceStream().setBBox(bbox);
        return bbox;
    }

    private void drawNote(PDAnnotationText annotation, final PDAppearanceContentStream contentStream)
            throws IOException
    {
        PDRectangle bbox = adjustRectAndBBox(annotation, 18, 20);
        contentStream.setMiterLimit(4);

        // get round edge the easy way. Adobe uses 4 lines with 4 arcs of radius 0.785 which is bigger.
        contentStream.setLineJoinStyle(1);

        contentStream.setLineCapStyle(0);
        contentStream.setLineWidth(0.61f); // value from Adobe
        contentStream.addRect(1, 1, bbox.getWidth() - 2, bbox.getHeight() - 2);
        contentStream.moveTo(bbox.getWidth() / 4,         bbox.getHeight() / 7 * 2);
        contentStream.lineTo(bbox.getWidth() * 3 / 4 - 1, bbox.getHeight() / 7 * 2);
        contentStream.moveTo(bbox.getWidth() / 4,         bbox.getHeight() / 7 * 3);
        contentStream.lineTo(bbox.getWidth() * 3 / 4 - 1, bbox.getHeight() / 7 * 3);
        contentStream.moveTo(bbox.getWidth() / 4,         bbox.getHeight() / 7 * 4);
        contentStream.lineTo(bbox.getWidth() * 3 / 4 - 1, bbox.getHeight() / 7 * 4);
        contentStream.moveTo(bbox.getWidth() / 4,         bbox.getHeight() / 7 * 5);
        contentStream.lineTo(bbox.getWidth() * 3 / 4 - 1, bbox.getHeight() / 7 * 5);
        contentStream.fillAndStroke();
    }

    private void drawCircles(PDAnnotationText annotation, final PDAppearanceContentStream contentStream)
            throws IOException
    {
        PDRectangle bbox = adjustRectAndBBox(annotation, 20, 20);

        // strategy used by Adobe:
        // 1) add small circle in white using /ca /CA 0.6 and width 1
        // 2) fill
        // 3) add small circle in one direction
        // 4) add large circle in other direction
        // 5) stroke + fill
        // with square width 20 small r = 6.36, large r = 9.756

        float smallR = 6.36f;
        float largeR = 9.756f;

        contentStream.setMiterLimit(4);
        contentStream.setLineJoinStyle(1);
        contentStream.setLineCapStyle(0);
        contentStream.saveGraphicsState();
        contentStream.setLineWidth(1);
        PDExtendedGraphicsState gs = new PDExtendedGraphicsState();
        gs.setAlphaSourceFlag(false);
        gs.setStrokingAlphaConstant(0.6f);
        gs.setNonStrokingAlphaConstant(0.6f);
        gs.setBlendMode(BlendMode.NORMAL);
        contentStream.setGraphicsStateParameters(gs);
        contentStream.setNonStrokingColor(1f);
        drawCircle(contentStream, bbox.getWidth() / 2, bbox.getHeight() / 2, smallR);
        contentStream.fill();
        contentStream.restoreGraphicsState();

        contentStream.setLineWidth(0.59f); // value from Adobe
        drawCircle(contentStream, bbox.getWidth() / 2, bbox.getHeight() / 2, smallR);
        drawCircle2(contentStream, bbox.getWidth() / 2, bbox.getHeight() / 2, largeR);
        contentStream.fillAndStroke();
    }

    private void drawInsert(PDAnnotationText annotation, final PDAppearanceContentStream contentStream)
            throws IOException
    {
        PDRectangle bbox = adjustRectAndBBox(annotation, 17, 20);

        contentStream.setMiterLimit(4);
        contentStream.setLineJoinStyle(0);
        contentStream.setLineCapStyle(0);
        contentStream.setLineWidth(0.59f); // value from Adobe
        contentStream.moveTo(bbox.getWidth() / 2 - 1, bbox.getHeight() - 2);
        contentStream.lineTo(1, 1);
        contentStream.lineTo(bbox.getWidth() - 2, 1);
        contentStream.closeAndFillAndStroke();
    }

    private void drawCross(PDAnnotationText annotation, final PDAppearanceContentStream contentStream)
            throws IOException
    {
        PDRectangle bbox = adjustRectAndBBox(annotation, 19, 19);

        // should be a square, but who knows...
        float min = Math.min(bbox.getWidth(), bbox.getHeight());

        // small = offset nearest bbox edge
        // large = offset second nearest bbox edge
        float small = min / 10;
        float large = min / 5;

        contentStream.setMiterLimit(4);
        contentStream.setLineJoinStyle(1);
        contentStream.setLineCapStyle(0);
        contentStream.setLineWidth(0.59f); // value from Adobe

        contentStream.moveTo(small, large);
        contentStream.lineTo(large, small);
        contentStream.lineTo(min / 2, min / 2 - small);
        contentStream.lineTo(min - large, small);
        contentStream.lineTo(min - small, large);
        contentStream.lineTo(min / 2 + small, min / 2);
        contentStream.lineTo(min - small, min - large);
        contentStream.lineTo(min - large, min - small);
        contentStream.lineTo(min / 2, min / 2 + small);
        contentStream.lineTo(large, min - small);
        contentStream.lineTo(small, min - large);
        contentStream.lineTo(min / 2 - small, min / 2);
        contentStream.closeAndFillAndStroke();
    }

    private void drawHelp(PDAnnotationText annotation, final PDAppearanceContentStream contentStream)
            throws IOException
    {
        PDRectangle bbox = adjustRectAndBBox(annotation, 20, 20);

        float min = Math.min(bbox.getWidth(), bbox.getHeight());

        contentStream.setMiterLimit(4);
        contentStream.setLineJoinStyle(1);
        contentStream.setLineCapStyle(0);
        contentStream.setLineWidth(0.59f); // value from Adobe

        // Adobe first fills a white circle with CA ca 0.6, so do we
        contentStream.saveGraphicsState();
        contentStream.setLineWidth(1);
        PDExtendedGraphicsState gs = new PDExtendedGraphicsState();
        gs.setAlphaSourceFlag(false);
        gs.setStrokingAlphaConstant(0.6f);
        gs.setNonStrokingAlphaConstant(0.6f);
        gs.setBlendMode(BlendMode.NORMAL);
        contentStream.setGraphicsStateParameters(gs);
        contentStream.setNonStrokingColor(1f);
        drawCircle2(contentStream, min / 2, min / 2, min / 2 - 1);
        contentStream.fill();
        contentStream.restoreGraphicsState();

        contentStream.saveGraphicsState();
        // rescale so that "?" fits into circle and move "?" to circle center
        // values gathered by trial and error
        contentStream.transform(Matrix.getScaleInstance(0.001f * min / 2.25f, 0.001f * min / 2.25f));
        contentStream.transform(Matrix.getTranslateInstance(555, 375));

        // we get the shape of an Helvetica "?" and use that one.
        // Adobe uses a different font (which one?), or created the shape from scratch.
        GeneralPath path = PDType1Font.HELVETICA.getPath("question");
        addPath(contentStream, path);
        contentStream.restoreGraphicsState();
        // draw the outer circle counterclockwise to fill area between circle and "?"
        drawCircle2(contentStream, min / 2, min / 2, min / 2 - 1);
        contentStream.fillAndStroke();
    }

    //TODO this is mostly identical to drawHelp, except for scale, translation and symbol
     private void drawParagraph(PDAnnotationText annotation, final PDAppearanceContentStream contentStream)
            throws IOException
    {
        PDRectangle bbox = adjustRectAndBBox(annotation, 20, 20);

        float min = Math.min(bbox.getWidth(), bbox.getHeight());

        contentStream.setMiterLimit(4);
        contentStream.setLineJoinStyle(1);
        contentStream.setLineCapStyle(0);
        contentStream.setLineWidth(0.59f); // value from Adobe

        // Adobe first fills a white circle with CA ca 0.6, so do we
        contentStream.saveGraphicsState();
        contentStream.setLineWidth(1);
        PDExtendedGraphicsState gs = new PDExtendedGraphicsState();
        gs.setAlphaSourceFlag(false);
        gs.setStrokingAlphaConstant(0.6f);
        gs.setNonStrokingAlphaConstant(0.6f);
        gs.setBlendMode(BlendMode.NORMAL);
        contentStream.setGraphicsStateParameters(gs);
        contentStream.setNonStrokingColor(1f);
        drawCircle2(contentStream, min / 2, min / 2, min / 2 - 1);
        contentStream.fill();
        contentStream.restoreGraphicsState();

        contentStream.saveGraphicsState();
        // rescale so that "?" fits into circle and move "?" to circle center
        // values gathered by trial and error
        contentStream.transform(Matrix.getScaleInstance(0.001f * min / 3, 0.001f * min / 3));
        contentStream.transform(Matrix.getTranslateInstance(850, 900));

        // we get the shape of an Helvetica "?" and use that one.
        // Adobe uses a different font (which one?), or created the shape from scratch.
        GeneralPath path = PDType1Font.HELVETICA.getPath("paragraph");
        addPath(contentStream, path);
        contentStream.restoreGraphicsState();
        // draw the outer circle counterclockwise to fill area between circle and "?"
        drawCircle2(contentStream, min / 2, min / 2, min / 2 - 1);
        contentStream.fillAndStroke();
    }

    private void drawNewParagraph(PDAnnotationText annotation, final PDAppearanceContentStream contentStream)
            throws IOException
    {
        adjustRectAndBBox(annotation, 13, 20);

        contentStream.setMiterLimit(4);
        contentStream.setLineJoinStyle(0);
        contentStream.setLineCapStyle(0);
        contentStream.setLineWidth(0.59f); // value from Adobe

        // small triangle (values from Adobe)
        contentStream.moveTo(6.4995f, 20);
        contentStream.lineTo(0.295f, 7.287f);
        contentStream.lineTo(12.705f, 7.287f);
        contentStream.closeAndFillAndStroke();

        // rescale and translate so that "NP" fits below the triangle
        // values gathered by trial and error
        contentStream.transform(Matrix.getScaleInstance(0.001f * 4, 0.001f * 4));
        contentStream.transform(Matrix.getTranslateInstance(200, 0));
        addPath(contentStream, PDType1Font.HELVETICA_BOLD.getPath("N"));
        contentStream.transform(Matrix.getTranslateInstance(1300, 0));
        addPath(contentStream, PDType1Font.HELVETICA_BOLD.getPath("P"));
        contentStream.fill();
    }

    private void addPath(final PDAppearanceContentStream contentStream, GeneralPath path) throws IOException
    {
        PathIterator it = path.getPathIterator(new AffineTransform());
        double[] coords = new double[6];
        while (!it.isDone())
        {
            int type = it.currentSegment(coords);
            switch (type)
            {
                case PathIterator.SEG_CLOSE:
                    contentStream.closePath();
                    break;
                case PathIterator.SEG_CUBICTO:
                    contentStream.curveTo((float) coords[0], (float) coords[1], (float) coords[2],
                                          (float) coords[3], (float) coords[4], (float) coords[5]);
                    break;
                case PathIterator.SEG_QUADTO:
                    contentStream.curveTo1((float) coords[0], (float) coords[1], (float) coords[2], (float) coords[3]);
                    // not sure whether curveTo1 or curveTo2 is to be used here
                    break;
                case PathIterator.SEG_LINETO:
                    contentStream.lineTo((float) coords[0], (float) coords[1]);
                    break;
                case PathIterator.SEG_MOVETO:
                    contentStream.moveTo((float) coords[0], (float) coords[1]);
                    break;
                default:
                    break;
            }
            it.next();
        }
    }

    @Override
    public void generateRolloverAppearance()
    {
        // No rollover appearance generated
    }

    @Override
    public void generateDownAppearance()
    {
        // No down appearance generated
    }
}

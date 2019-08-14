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
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDAppearanceContentStream;
import org.apache.pdfbox.pdmodel.PDDocument;
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

    private static final Set<String> SUPPORTED_NAMES = new HashSet<String>();

    static
    {
        SUPPORTED_NAMES.add(PDAnnotationText.NAME_NOTE);
        SUPPORTED_NAMES.add(PDAnnotationText.NAME_INSERT);
        SUPPORTED_NAMES.add(PDAnnotationText.NAME_CROSS);
        SUPPORTED_NAMES.add(PDAnnotationText.NAME_HELP);
        SUPPORTED_NAMES.add(PDAnnotationText.NAME_CIRCLE);
        SUPPORTED_NAMES.add(PDAnnotationText.NAME_PARAGRAPH);
        SUPPORTED_NAMES.add(PDAnnotationText.NAME_NEW_PARAGRAPH);
        SUPPORTED_NAMES.add(PDAnnotationText.NAME_CHECK);
        SUPPORTED_NAMES.add(PDAnnotationText.NAME_STAR);
        SUPPORTED_NAMES.add(PDAnnotationText.NAME_RIGHT_ARROW);
        SUPPORTED_NAMES.add(PDAnnotationText.NAME_RIGHT_POINTER);
        SUPPORTED_NAMES.add(PDAnnotationText.NAME_CROSS_HAIRS);
        SUPPORTED_NAMES.add(PDAnnotationText.NAME_UP_ARROW);
        SUPPORTED_NAMES.add(PDAnnotationText.NAME_UP_LEFT_ARROW);
        SUPPORTED_NAMES.add(PDAnnotationText.NAME_COMMENT);
        SUPPORTED_NAMES.add(PDAnnotationText.NAME_KEY);
    }

    public PDTextAppearanceHandler(PDAnnotation annotation)
    {
        super(annotation);
    }

    public PDTextAppearanceHandler(PDAnnotation annotation, PDDocument document)
    {
        super(annotation, document);
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
            return;
        }

        PDAppearanceContentStream contentStream = null; 

        try
        {
            contentStream = getNormalAppearanceAsContentStream();

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

            String annotationTypeName =  annotation.getName();

            if (PDAnnotationText.NAME_NOTE.equals(annotationTypeName))
            {
                drawNote(annotation, contentStream);
            }
            else if (PDAnnotationText.NAME_CROSS.equals(annotationTypeName))
            {
                drawCross(annotation, contentStream);
            }
            else if (PDAnnotationText.NAME_CIRCLE.equals(annotationTypeName))
            {
                drawCircles(annotation, contentStream);
            }
            else if (PDAnnotationText.NAME_INSERT.equals(annotationTypeName))
            {
                drawInsert(annotation, contentStream);
            }
            else if (PDAnnotationText.NAME_HELP.equals(annotationTypeName))
            {
                drawHelp(annotation, contentStream);
            }
            else if (PDAnnotationText.NAME_PARAGRAPH.equals(annotationTypeName))
            {
                drawParagraph(annotation, contentStream);
            }
            else if (PDAnnotationText.NAME_NEW_PARAGRAPH.equals(annotationTypeName))
            {
                drawNewParagraph(annotation, contentStream);
            }
            else if (PDAnnotationText.NAME_STAR.equals(annotationTypeName))
            {
                drawStar(annotation, contentStream);
            }
            else if (PDAnnotationText.NAME_CHECK.equals(annotationTypeName))
            {
                drawCheck(annotation, contentStream);
            }
            else if (PDAnnotationText.NAME_RIGHT_ARROW.equals(annotationTypeName))
            {
                drawRightArrow(annotation, contentStream);
            }
            else if (PDAnnotationText.NAME_RIGHT_POINTER.equals(annotationTypeName))
            {
                drawRightPointer(annotation, contentStream);
            }
            else if (PDAnnotationText.NAME_CROSS_HAIRS.equals(annotationTypeName))
            {
                drawCrossHairs(annotation, contentStream);
            }
            else if (PDAnnotationText.NAME_UP_ARROW.equals(annotationTypeName))
            {
                drawUpArrow(annotation, contentStream);
            }
            else if (PDAnnotationText.NAME_UP_LEFT_ARROW.equals(annotationTypeName))
            {
                drawUpLeftArrow(annotation, contentStream);
            }
            else if (PDAnnotationText.NAME_COMMENT.equals(annotationTypeName))
            {
                drawComment(annotation, contentStream);
            }
            else if (PDAnnotationText.NAME_KEY.equals(annotationTypeName))
            {
                drawKey(annotation, contentStream);
            }
        }
        catch (IOException e)
        {
            LOG.error(e);
        }
        finally
        {
            IOUtils.closeQuietly(contentStream);
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
        
        // alternatively, this could also be drawn with Zapf Dingbats "a21"
        // see DrawStar()
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
        contentStream.transform(Matrix.getTranslateInstance(500, 375));

        // we get the shape of an Helvetica bold "?" and use that one.
        // Adobe uses a different font (which one?), or created the shape from scratch.
        GeneralPath path = PDType1Font.HELVETICA_BOLD.getPath("question");
        addPath(contentStream, path);
        contentStream.restoreGraphicsState();
        // draw the outer circle counterclockwise to fill area between circle and "?"
        drawCircle2(contentStream, min / 2, min / 2, min / 2 - 1);
        contentStream.fillAndStroke();
    }

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
        contentStream.fillAndStroke();
        drawCircle(contentStream, min / 2, min / 2, min / 2 - 1);
        contentStream.stroke();
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

    private void drawStar(PDAnnotationText annotation, final PDAppearanceContentStream contentStream)
            throws IOException
    {
        PDRectangle bbox = adjustRectAndBBox(annotation, 20, 19);

        float min = Math.min(bbox.getWidth(), bbox.getHeight());

        contentStream.setMiterLimit(4);
        contentStream.setLineJoinStyle(1);
        contentStream.setLineCapStyle(0);
        contentStream.setLineWidth(0.59f); // value from Adobe

        contentStream.transform(Matrix.getScaleInstance(0.001f * min / 0.8f, 0.001f * min / 0.8f));

        // we get the shape of a Zapf Dingbats star (0x2605) and use that one.
        // Adobe uses a different font (which one?), or created the shape from scratch.
        GeneralPath path = PDType1Font.ZAPF_DINGBATS.getPath("a35");
        addPath(contentStream, path);
        contentStream.fillAndStroke();
    }

    //TODO this is mostly identical to drawStar, except for scale, translation and symbol
    // maybe use a table with all values and draw from there
    // this could also optionally use outer circle
    private void drawCheck(PDAnnotationText annotation, final PDAppearanceContentStream contentStream)
            throws IOException
    {
        PDRectangle bbox = adjustRectAndBBox(annotation, 20, 19);

        float min = Math.min(bbox.getWidth(), bbox.getHeight());

        contentStream.setMiterLimit(4);
        contentStream.setLineJoinStyle(1);
        contentStream.setLineCapStyle(0);
        contentStream.setLineWidth(0.59f); // value from Adobe

        contentStream.transform(Matrix.getScaleInstance(0.001f * min / 0.8f, 0.001f * min / 0.8f));
        contentStream.transform(Matrix.getTranslateInstance(0, 50));

        // we get the shape of a Zapf Dingbats check (0x2714) and use that one.
        // Adobe uses a different font (which one?), or created the shape from scratch.
        GeneralPath path = PDType1Font.ZAPF_DINGBATS.getPath("a20");
        addPath(contentStream, path);
        contentStream.fillAndStroke();
    }

    //TODO this is mostly identical to drawStar, except for scale, translation and symbol
    private void drawRightPointer(PDAnnotationText annotation, final PDAppearanceContentStream contentStream)
            throws IOException
    {
        PDRectangle bbox = adjustRectAndBBox(annotation, 20, 17);

        float min = Math.min(bbox.getWidth(), bbox.getHeight());

        contentStream.setMiterLimit(4);
        contentStream.setLineJoinStyle(1);
        contentStream.setLineCapStyle(0);
        contentStream.setLineWidth(0.59f); // value from Adobe

        contentStream.transform(Matrix.getScaleInstance(0.001f * min / 0.8f, 0.001f * min / 0.8f));
        contentStream.transform(Matrix.getTranslateInstance(0, 50));

        // we get the shape of a Zapf Dingbats right pointer (0x27A4) and use that one.
        // Adobe uses a different font (which one?), or created the shape from scratch.
        GeneralPath path = PDType1Font.ZAPF_DINGBATS.getPath("a174");
        addPath(contentStream, path);
        contentStream.fillAndStroke();
    }

    private void drawCrossHairs(PDAnnotationText annotation, final PDAppearanceContentStream contentStream)
            throws IOException
    {
        PDRectangle bbox = adjustRectAndBBox(annotation, 20, 20);

        float min = Math.min(bbox.getWidth(), bbox.getHeight());

        contentStream.setMiterLimit(4);
        contentStream.setLineJoinStyle(0);
        contentStream.setLineCapStyle(0);
        contentStream.setLineWidth(0.61f); // value from Adobe

        contentStream.transform(Matrix.getScaleInstance(0.001f * min / 1.5f, 0.001f * min / 1.5f));
        contentStream.transform(Matrix.getTranslateInstance(0, 50));

        // we get the shape of a Symbol crosshair (0x2295) and use that one.
        // Adobe uses a different font (which one?), or created the shape from scratch.
        GeneralPath path = PDType1Font.SYMBOL.getPath("circleplus");
        addPath(contentStream, path);
        contentStream.fillAndStroke();
    }

    private void drawUpArrow(PDAnnotationText annotation, final PDAppearanceContentStream contentStream)
                 throws IOException
    {
        adjustRectAndBBox(annotation, 17, 20);

        contentStream.setMiterLimit(4);
        contentStream.setLineJoinStyle(1);
        contentStream.setLineCapStyle(0);
        contentStream.setLineWidth(0.59f); // value from Adobe

        contentStream.moveTo(1, 7);
        contentStream.lineTo(5, 7);
        contentStream.lineTo(5, 1);
        contentStream.lineTo(12, 1);
        contentStream.lineTo(12, 7);
        contentStream.lineTo(16, 7);
        contentStream.lineTo(8.5f, 19);
        contentStream.closeAndFillAndStroke();
    }

    private void drawUpLeftArrow(PDAnnotationText annotation, final PDAppearanceContentStream contentStream)
                 throws IOException
    {
        adjustRectAndBBox(annotation, 17, 17);

        contentStream.setMiterLimit(4);
        contentStream.setLineJoinStyle(1);
        contentStream.setLineCapStyle(0);
        contentStream.setLineWidth(0.59f); // value from Adobe
        
        contentStream.transform(Matrix.getRotateInstance(Math.toRadians(45), 8, -4));

        contentStream.moveTo(1, 7);
        contentStream.lineTo(5, 7);
        contentStream.lineTo(5, 1);
        contentStream.lineTo(12, 1);
        contentStream.lineTo(12, 7);
        contentStream.lineTo(16, 7);
        contentStream.lineTo(8.5f, 19);
        contentStream.closeAndFillAndStroke();
    }
    
    private void drawRightArrow(PDAnnotationText annotation, final PDAppearanceContentStream contentStream)
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
        // rescale so that the glyph fits into circle and move it to circle center
        // values gathered by trial and error
        contentStream.transform(Matrix.getScaleInstance(0.001f * min / 1.3f, 0.001f * min / 1.3f));
        contentStream.transform(Matrix.getTranslateInstance(200, 300));

        // we get the shape of a Zapf Dingbats right arrow (0x2794) and use that one.
        // Adobe uses a different font (which one?), or created the shape from scratch.
        GeneralPath path = PDType1Font.ZAPF_DINGBATS.getPath("a160");
        addPath(contentStream, path);
        contentStream.restoreGraphicsState();
        // surprisingly, this one not counterclockwise.
        drawCircle(contentStream, min / 2, min / 2, min / 2 - 1);
        contentStream.fillAndStroke();
    }

    private void drawComment(PDAnnotationText annotation, final PDAppearanceContentStream contentStream)
                 throws IOException
    {
        adjustRectAndBBox(annotation, 18, 18);

        contentStream.setMiterLimit(4);
        contentStream.setLineJoinStyle(1);
        contentStream.setLineCapStyle(0);
        contentStream.setLineWidth(200);

        // Adobe first fills a white rectangle with CA ca 0.6, so do we
        contentStream.saveGraphicsState();
        contentStream.setLineWidth(1);
        PDExtendedGraphicsState gs = new PDExtendedGraphicsState();
        gs.setAlphaSourceFlag(false);
        gs.setStrokingAlphaConstant(0.6f);
        gs.setNonStrokingAlphaConstant(0.6f);
        gs.setBlendMode(BlendMode.NORMAL);
        contentStream.setGraphicsStateParameters(gs);
        contentStream.setNonStrokingColor(1f);
        contentStream.addRect(0.3f, 0.3f, 18-0.6f, 18-0.6f);
        contentStream.fill();
        contentStream.restoreGraphicsState();

        contentStream.transform(Matrix.getScaleInstance(0.003f, 0.003f));
        contentStream.transform(Matrix.getTranslateInstance(500, -300));

        // outer shape was gathered from Font Awesome by "printing" comment.svg
        // into a PDF and looking at the content stream
        contentStream.moveTo(2549, 5269);
        contentStream.curveTo(1307, 5269, 300, 4451, 300, 3441);
        contentStream.curveTo(300, 3023, 474, 2640, 764, 2331);
        contentStream.curveTo(633, 1985, 361, 1691, 357, 1688);
        contentStream.curveTo(299, 1626, 283, 1537, 316, 1459);
        contentStream.curveTo(350, 1382, 426, 1332, 510, 1332);
        contentStream.curveTo(1051, 1332, 1477, 1558, 1733, 1739);
        contentStream.curveTo(1987, 1659, 2261, 1613, 2549, 1613);
        contentStream.curveTo(3792, 1613, 4799, 2431, 4799, 3441);
        contentStream.curveTo(4799, 4451, 3792, 5269, 2549, 5269);
        contentStream.closePath();

        // can't use addRect: if we did that, we wouldn't get the donut effect
        contentStream.moveTo(0.3f / 0.003f - 500, 0.3f / 0.003f + 300);
        contentStream.lineTo(0.3f / 0.003f - 500, 0.3f / 0.003f + 300 + 17.4f / 0.003f);
        contentStream.lineTo(0.3f / 0.003f - 500 + 17.4f / 0.003f, 0.3f / 0.003f + 300 + 17.4f / 0.003f);
        contentStream.lineTo(0.3f / 0.003f - 500 + 17.4f / 0.003f, 0.3f / 0.003f + 300);

        contentStream.closeAndFillAndStroke();
    }
    
    private void drawKey(PDAnnotationText annotation, final PDAppearanceContentStream contentStream)
                 throws IOException
    {
        adjustRectAndBBox(annotation, 13, 18);

        contentStream.setMiterLimit(4);
        contentStream.setLineJoinStyle(1);
        contentStream.setLineCapStyle(0);
        contentStream.setLineWidth(200);

        contentStream.transform(Matrix.getScaleInstance(0.003f, 0.003f));
        contentStream.transform(Matrix.getRotateInstance(Math.toRadians(45), 2500, -800));

        // shape was gathered from Font Awesome by "printing" key.svg into a PDF
        // and looking at the content stream
        contentStream.moveTo(4799, 4004);
        contentStream.curveTo(4799, 3149, 4107, 2457, 3253, 2457);
        contentStream.curveTo(3154, 2457, 3058, 2466, 2964, 2484);
        contentStream.lineTo(2753, 2246);
        contentStream.curveTo(2713, 2201, 2656, 2175, 2595, 2175);
        contentStream.lineTo(2268, 2175);
        contentStream.lineTo(2268, 1824);
        contentStream.curveTo(2268, 1707, 2174, 1613, 2057, 1613);
        contentStream.lineTo(1706, 1613);
        contentStream.lineTo(1706, 1261);
        contentStream.curveTo(1706, 1145, 1611, 1050, 1495, 1050);
        contentStream.lineTo(510, 1050);
        contentStream.curveTo(394, 1050, 300, 1145, 300, 1261);
        contentStream.lineTo(300, 1947);
        contentStream.curveTo(300, 2003, 322, 2057, 361, 2097);
        contentStream.lineTo(1783, 3519);
        contentStream.curveTo(1733, 3671, 1706, 3834, 1706, 4004);
        contentStream.curveTo(1706, 4858, 2398, 5550, 3253, 5550);
        contentStream.curveTo(4109, 5550, 4799, 4860, 4799, 4004);
        contentStream.closePath();
        contentStream.moveTo(3253, 4425);
        contentStream.curveTo(3253, 4192, 3441, 4004, 3674, 4004);
        contentStream.curveTo(3907, 4004, 4096, 4192, 4096, 4425);
        contentStream.curveTo(4096, 4658, 3907, 4847, 3674, 4847);
        contentStream.curveTo(3441, 4847, 3253, 4658, 3253, 4425);
        contentStream.fillAndStroke();
    }
    
    private void addPath(final PDAppearanceContentStream contentStream, GeneralPath path) throws IOException
    {
        double curX = 0;
        double curY = 0;
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
                    curX = coords[4];
                    curY = coords[5];
                    break;
                case PathIterator.SEG_QUADTO:
                    // Convert quadratic Bézier curve to cubic
                    // https://fontforge.github.io/bezier.html
                    // CP1 = QP0 + 2/3 *(QP1-QP0)
                    // CP2 = QP2 + 2/3 *(QP1-QP2)
                    double cp1x = curX + 2d / 3d * (coords[0] - curX);
                    double cp1y = curY + 2d / 3d * (coords[1] - curY);
                    double cp2x = coords[2] + 2d / 3d * (coords[0] - coords[2]);
                    double cp2y = coords[3] + 2d / 3d * (coords[1] - coords[3]);
                    contentStream.curveTo((float) cp1x, (float) cp1y,
                                          (float) cp2x, (float) cp2y,
                                          (float) coords[2], (float) coords[3]);
                    curX = coords[2];
                    curY = coords[3];
                    break;
                case PathIterator.SEG_LINETO:
                    contentStream.lineTo((float) coords[0], (float) coords[1]);
                    curX = coords[0];
                    curY = coords[1];
                    break;
                case PathIterator.SEG_MOVETO:
                    contentStream.moveTo((float) coords[0], (float) coords[1]);
                    curX = coords[0];
                    curY = coords[1];
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

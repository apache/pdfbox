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

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.contentstream.PDAbstractContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLine;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceContentStream;
import org.apache.pdfbox.util.Matrix;

/**
 *
 */
public class PDLineAppearanceHandler extends PDAbstractAppearanceHandler
{
    private static final Log LOG = LogFactory.getLog(PDLineAppearanceHandler.class);

    static final double ARROW_ANGLE = Math.toRadians(30);
    static final int FONT_SIZE = 9;

    /**
     * styles where the line has to be drawn shorter (minus line width).
     */
    private static final Set<String> SHORT_STYLES = new HashSet<>();

    /**
     * styles where there is an interior color.
     */
    private static final Set<String> INTERIOR_COLOR_STYLES = new HashSet<>();

    static
    {
        SHORT_STYLES.add(PDAnnotationLine.LE_OPEN_ARROW);
        SHORT_STYLES.add(PDAnnotationLine.LE_CLOSED_ARROW);
        SHORT_STYLES.add(PDAnnotationLine.LE_SQUARE);
        SHORT_STYLES.add(PDAnnotationLine.LE_CIRCLE);
        SHORT_STYLES.add(PDAnnotationLine.LE_DIAMOND);

        INTERIOR_COLOR_STYLES.add(PDAnnotationLine.LE_CLOSED_ARROW);
        INTERIOR_COLOR_STYLES.add(PDAnnotationLine.LE_CIRCLE);
        INTERIOR_COLOR_STYLES.add(PDAnnotationLine.LE_DIAMOND);
        INTERIOR_COLOR_STYLES.add(PDAnnotationLine.LE_R_CLOSED_ARROW);
        INTERIOR_COLOR_STYLES.add(PDAnnotationLine.LE_SQUARE);
    }

    public PDLineAppearanceHandler(PDAnnotation annotation)
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
        PDAnnotationLine annotation = (PDAnnotationLine) getAnnotation();
        PDRectangle rect = annotation.getRectangle();
        float[] pathsArray = annotation.getLine();
        if (pathsArray == null)
        {
            return;
        }
        AnnotationBorder ab = AnnotationBorder.getAnnotationBorder(annotation, annotation.getBorderStyle());
        if (ab.color.getComponents().length == 0)
        {
            return;
        }
        float ll = annotation.getLeaderLineLength();
        float lle = annotation.getLeaderLineExtensionLength();
        float llo = annotation.getLeaderLineOffsetLength();

        // Adjust rectangle even if not empty, see PLPDF.com-MarkupAnnotations.pdf
        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float maxX = Float.MIN_VALUE;
        float maxY = Float.MIN_VALUE;
        for (int i = 0; i < pathsArray.length / 2; ++i)
        {
            float x = pathsArray[i * 2];
            float y = pathsArray[i * 2 + 1];
            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
            maxX = Math.max(maxX, x);
            maxY = Math.max(maxY, y);
        }

        // Leader lines
        if (ll < 0)
        {
            // /LLO and /LLE go in the same direction as /LL
            llo = -llo;
            lle = -lle;
        }

        // add/substract with, font height, and arrows
        // arrow length is 9 * width at about 30° => 10 * width seems to be enough
        // but need to consider /LL, /LLE and /LLO too
        //TODO find better way to calculate padding
        rect.setLowerLeftX(Math.min(minX - Math.max(ab.width * 10, Math.abs(llo+ll+lle)), rect.getLowerLeftX()));
        rect.setLowerLeftY(Math.min(minY - Math.max(ab.width * 10, Math.abs(llo+ll+lle)), rect.getLowerLeftY()));
        rect.setUpperRightX(Math.max(maxX + Math.max(ab.width * 10, Math.abs(llo+ll+lle)), rect.getUpperRightX()));
        rect.setUpperRightY(Math.max(maxY + Math.max(ab.width * 10, Math.abs(llo+ll+lle)), rect.getUpperRightY()));

        annotation.setRectangle(rect);

        try
        {
            try (PDAppearanceContentStream cs = getNormalAppearanceAsContentStream())
            {
                handleOpacity(annotation.getConstantOpacity());

                // Tested with Adobe Reader:
                // text is written first (TODO)
                // width 0 is used by Adobe as such (but results in a visible line in rendering)
                // empty color array results in an invisible line ("n" operator) but the rest is visible
                // empty content is like no caption

                boolean hasStroke = cs.setStrokingColorOnDemand(getColor());

                if (ab.dashArray != null)
                {
                    cs.setLineDashPattern(ab.dashArray, 0);
                }
                cs.setLineWidth(ab.width);

                float x1 = pathsArray[0];
                float y1 = pathsArray[1];
                float x2 = pathsArray[2];
                float y2 = pathsArray[3];

                // if there are leader lines, then the /L coordinates represent
                // the endpoints of the leader lines rather than the endpoints of the line itself.
                // so for us, llo + ll is the vertical offset for the line.
                float y = llo + ll;

                String contents = annotation.getContents();
                if (contents == null)
                {
                    contents = "";
                }

                double angle = Math.atan2(y2 - y1, x2 - x1);
                cs.transform(Matrix.getRotateInstance(angle, x1, y1));
                float lineLength = (float) Math.sqrt(((x2 - x1) * (x2 - x1)) + ((y2 - y1) * (y2 - y1)));
                if (annotation.hasCaption() && !contents.isEmpty())
                {
                    PDType1Font font = PDType1Font.HELVETICA;
                    // TODO: support newlines!!!!!
                    // see https://www.pdfill.com/example/pdf_commenting_new.pdf
                    float contentLength = 0;
                    try
                    {
                        contentLength = font.getStringWidth(annotation.getContents()) / 1000 * FONT_SIZE;

                        //TODO How to decide the size of the font?
                        // 9 seems to be standard, but if the text doesn't fit, a scaling is done
                        // see AnnotationSample.Standard.pdf, diagonal line
                    }
                    catch (IllegalArgumentException ex)
                    {
                        // Adobe Reader displays placeholders instead
                        LOG.error("line text '" + annotation.getContents() + "' can't be shown", ex);
                    }
                    float xOffset = (lineLength - contentLength) / 2;
                    float yOffset;
                    
                    // Leader lines
                    cs.moveTo(0, llo);
                    cs.lineTo(0, llo + ll + lle);
                    cs.moveTo(lineLength, llo);
                    cs.lineTo(lineLength, llo + ll + lle);

                    String captionPositioning = annotation.getCaptionPositioning();

                    // draw the line horizontally, using the rotation CTM to get to correct final position
                    // that's the easiest way to calculate the positions for the line before and after inline caption
                    if (SHORT_STYLES.contains(annotation.getStartPointEndingStyle()))
                    {
                        cs.moveTo(ab.width, y);
                    }
                    else
                    {
                        cs.moveTo(0, y);
                    }
                    if ("Top".equals(captionPositioning))
                    {
                        // this arbitrary number is from Adobe
                        yOffset = 1.908f;
                    }
                    else
                    {
                        // Inline
                        // this arbitrary number is from Adobe
                        yOffset = -2.6f;

                        cs.lineTo(xOffset - ab.width, y);
                        cs.moveTo(lineLength - xOffset + ab.width, y);
                    }
                    if (SHORT_STYLES.contains(annotation.getEndPointEndingStyle()))
                    {
                        cs.lineTo(lineLength - ab.width, y);
                    }
                    else
                    {
                        cs.lineTo(lineLength, y);
                    }
                    cs.drawShape(ab.width, hasStroke, false);

                    // /CO entry (caption offset)
                    float captionHorizontalOffset = annotation.getCaptionHorizontalOffset();
                    float captionVerticalOffset = annotation.getCaptionVerticalOffset();

                    // check contentLength so we don't show if there was trouble before
                    if (contentLength > 0)
                    {
                        prepareResources();

                        cs.beginText();
                        cs.setFont(font, FONT_SIZE);
                        cs.newLineAtOffset(xOffset + captionHorizontalOffset, 
                                           y + yOffset + captionVerticalOffset);
                        cs.showText(annotation.getContents());
                        cs.endText();
                    }

                    if (captionVerticalOffset != 0)
                    {
                        // Adobe paints vertical bar to the caption
                        cs.moveTo(0 + lineLength / 2, y);
                        cs.lineTo(0 + lineLength / 2, y + captionVerticalOffset);
                        cs.drawShape(ab.width, hasStroke, false);
                    }
                }
                else
                {
                    if (SHORT_STYLES.contains(annotation.getStartPointEndingStyle()))
                    {
                        cs.moveTo(ab.width, y);
                    }
                    else
                    {
                        cs.moveTo(0, y);
                    }
                    if (SHORT_STYLES.contains(annotation.getEndPointEndingStyle()))
                    {
                        cs.lineTo(lineLength - ab.width, y);
                    }
                    else
                    {
                        cs.lineTo(lineLength, y);
                    }
                    cs.drawShape(ab.width, hasStroke, false);
                }

                // do this here and not before showing the text, or the text would appear in the
                // interior color
                boolean hasBackground = cs.setNonStrokingColorOnDemand(annotation.getInteriorColor());
                switch (annotation.getStartPointEndingStyle())
                {
                    case PDAnnotationLine.LE_OPEN_ARROW:
                    case PDAnnotationLine.LE_CLOSED_ARROW:
                        drawArrow(cs, ab.width, y, ab.width * 9);
                        if (PDAnnotationLine.LE_CLOSED_ARROW.equals(annotation.getStartPointEndingStyle()))
                        {
                            cs.closePath();
                        }
                        break;
                    case PDAnnotationLine.LE_BUTT:
                        cs.moveTo(0, y - ab.width * 3);
                        cs.lineTo(0, y + ab.width * 3);
                        break;
                    case PDAnnotationLine.LE_DIAMOND:
                        drawDiamond(cs, 0, y, ab.width * 3);
                        break;
                    case PDAnnotationLine.LE_SQUARE:
                        cs.addRect(0 - ab.width * 3, y - ab.width * 3, ab.width * 6, ab.width * 6);
                        break;
                    case PDAnnotationLine.LE_CIRCLE:
                        addCircle(cs, 0, y, ab.width * 3);
                        break;
                    case PDAnnotationLine.LE_R_OPEN_ARROW:
                    case PDAnnotationLine.LE_R_CLOSED_ARROW:
                        drawArrow(cs, -ab.width, y, -ab.width * 9);
                        if (PDAnnotationLine.LE_R_CLOSED_ARROW.equals(annotation.getStartPointEndingStyle()))
                        {
                            cs.closePath();
                        }
                        break;
                    case PDAnnotationLine.LE_SLASH:
                        // the line is 18 x linewidth at an angle of 60°
                        cs.moveTo((float) (Math.cos(Math.toRadians(60)) * ab.width * 9),
                              y + (float) (Math.sin(Math.toRadians(60)) * ab.width * 9));
                        cs.lineTo((float) (Math.cos(Math.toRadians(240)) * ab.width * 9),
                              y + (float) (Math.sin(Math.toRadians(240)) * ab.width * 9));
                        break;
                    default:
                        break;
                }
                if (INTERIOR_COLOR_STYLES.contains(annotation.getStartPointEndingStyle()))
                {
                    cs.drawShape(ab.width, hasStroke, hasBackground);
                }
                else if (!PDAnnotationLine.LE_NONE.equals(annotation.getStartPointEndingStyle()))
                {
                    // need to do this separately, because sometimes /IC is set anyway
                    cs.drawShape(ab.width, hasStroke, false);
                }

                switch (annotation.getEndPointEndingStyle())
                {
                    case PDAnnotationLine.LE_OPEN_ARROW:
                    case PDAnnotationLine.LE_CLOSED_ARROW:
                        drawArrow(cs, lineLength - ab.width, y, -ab.width * 9);
                        if (PDAnnotationLine.LE_CLOSED_ARROW.equals(annotation.getEndPointEndingStyle()))
                        {
                            cs.closePath();
                        }
                        break;
                    case PDAnnotationLine.LE_BUTT:
                        cs.moveTo(lineLength, y - ab.width * 3);
                        cs.lineTo(lineLength, y + ab.width * 3);
                        break;
                    case PDAnnotationLine.LE_DIAMOND:
                        drawDiamond(cs, lineLength, y, ab.width * 3);
                        break;
                    case PDAnnotationLine.LE_SQUARE:
                        cs.addRect(lineLength - ab.width * 3, y - ab.width * 3, ab.width * 6, ab.width * 6);
                        break;
                    case PDAnnotationLine.LE_CIRCLE:
                        addCircle(cs, lineLength, y, ab.width * 3);
                        break;
                    case PDAnnotationLine.LE_R_OPEN_ARROW:
                    case PDAnnotationLine.LE_R_CLOSED_ARROW:
                        drawArrow(cs, lineLength + ab.width, y, ab.width * 9);
                        if (PDAnnotationLine.LE_R_CLOSED_ARROW.equals(annotation.getEndPointEndingStyle()))
                        {
                            cs.closePath();
                        }
                        break;
                    case PDAnnotationLine.LE_SLASH:
                        // the line is 18 x linewidth at an angle of 60Â°
                        cs.moveTo(lineLength + (float) (Math.cos(Math.toRadians(60)) * ab.width * 9),
                                           y + (float) (Math.sin(Math.toRadians(60)) * ab.width * 9));
                        cs.lineTo(lineLength + (float) (Math.cos(Math.toRadians(240)) * ab.width * 9),
                                           y + (float) (Math.sin(Math.toRadians(240)) * ab.width * 9));
                        break;
                    default:
                        break;
                }
                if (INTERIOR_COLOR_STYLES.contains(annotation.getEndPointEndingStyle()))
                {
                    cs.drawShape(ab.width, hasStroke, hasBackground);
                }
                else if (!PDAnnotationLine.LE_NONE.equals(annotation.getEndPointEndingStyle()))
                {
                    // need to do this separately, because sometimes /IC is set anyway
                    cs.drawShape(ab.width, hasStroke, false);
                }
            }
        }
        catch (IOException ex)
        {
            LOG.error(ex);
        }
    }

    /**
     * Add the two arms of a horizontal arrow.
     * 
     * @param cs Content stream
     * @param x
     * @param y
     * @param len The arm length. Positive goes to the right, negative goes to the left.
     * 
     * @throws IOException If the content stream could not be written
     */
    private void drawArrow(PDAbstractContentStream cs, float x, float y, float len) throws IOException
    {
        // strategy for arrows: angle 30°, arrow arm length = 9 * line width
        // cos(angle) = x position
        // sin(angle) = y position
        // this comes very close to what Adobe is doing
        cs.moveTo(x + (float) (Math.cos(ARROW_ANGLE) * len), y + (float) (Math.sin(ARROW_ANGLE) * len));
        cs.lineTo(x, y);
        cs.lineTo(x + (float) (Math.cos(ARROW_ANGLE) * len), y - (float) (Math.sin(ARROW_ANGLE) * len));
    }

    /**
     * Add a square diamond shape (corner on top) to the path.
     *
     * @param cs Content stream
     * @param x
     * @param y
     * @param r Radius (to a corner)
     * 
     * @throws IOException If the content stream could not be written
     */
    private void drawDiamond(PDAbstractContentStream cs, float x, float y, float r) throws IOException
    {
        cs.moveTo(x - r, y);
        cs.lineTo(x, y + r);
        cs.lineTo(x + r, y);
        cs.lineTo(x, y - r);
        cs.closePath();
    }

    /**
     * Add a circle shape to the path.
     *
     * @param cs Content stream
     * @param x
     * @param y
     * @param r Radius
     * 
     * @throws IOException If the content stream could not be written
     */
    private void addCircle(PDAbstractContentStream cs, float x, float y, float r) throws IOException
    {
        // http://stackoverflow.com/a/2007782/535646
        float magic = r * 0.551784f;
        cs.moveTo(x, y + r);
        cs.curveTo(x + magic, y + r, x + r, y + magic, x + r, y);
        cs.curveTo(x + r, y - magic, x + magic, y - r, x, y - r);
        cs.curveTo(x - magic, y - r, x - r, y - magic, x - r, y);
        cs.curveTo(x - r, y + magic, x - magic, y + r, x, y + r);
        cs.closePath();
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

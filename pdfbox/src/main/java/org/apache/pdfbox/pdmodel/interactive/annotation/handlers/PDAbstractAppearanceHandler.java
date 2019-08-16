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

package org.apache.pdfbox.pdmodel.interactive.annotation.handlers;

import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.PDAppearanceContentStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLine;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationSquareCircle;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceDictionary;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceEntry;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceStream;

/**
 * Generic handler to generate the fields appearance.
 * 
 * Individual handler will provide specific implementations for different field
 * types.
 * 
 */
public abstract class PDAbstractAppearanceHandler implements PDAppearanceHandler
{
    private final PDAnnotation annotation;
    protected PDDocument document;

    /**
     * Line ending styles where the line has to be drawn shorter (minus line width).
     */
    protected static final Set<String> SHORT_STYLES = createShortStyles();

    static final double ARROW_ANGLE = Math.toRadians(30);

    /**
     * Line ending styles where there is an interior color.
     */
    protected static final Set<String> INTERIOR_COLOR_STYLES = createInteriorColorStyles();
    
    /**
     * Line ending styles where the shape changes its angle, e.g. arrows.
     */
    protected static final Set<String> ANGLED_STYLES = createAngledStyles();

    public PDAbstractAppearanceHandler(PDAnnotation annotation)
    {
        this(annotation, null);
    }

    public PDAbstractAppearanceHandler(PDAnnotation annotation, PDDocument document)
    {
        this.annotation = annotation;
        this.document = document;
    }

    @Override
    public abstract void generateNormalAppearance();

    @Override
    public abstract void generateRolloverAppearance();

    @Override
    public abstract void generateDownAppearance();

    PDAnnotation getAnnotation()
    {
        return annotation;
    }

    PDColor getColor()
    {
        return annotation.getColor();
    }

    PDRectangle getRectangle()
    {
        return annotation.getRectangle();
    }
    
    protected COSStream createCOSStream()
    {
        return document == null ? new COSStream() : document.getDocument().createCOSStream();
    }

    /**
     * Get the annotations appearance dictionary.
     * 
     * <p>
     * This will get the annotations appearance dictionary. If this is not
     * existent an empty appearance dictionary will be created.
     * 
     * @return the annotations appearance dictionary
     */
    PDAppearanceDictionary getAppearance()
    {
        PDAppearanceDictionary appearanceDictionary = annotation.getAppearance();
        if (appearanceDictionary == null)
        {
            appearanceDictionary = new PDAppearanceDictionary();
            annotation.setAppearance(appearanceDictionary);
        }
        return appearanceDictionary;
    }

    /**
     * Get the annotations normal appearance content stream.
     * 
     * <p>
     * This will get the annotations normal appearance content stream, to 'draw' to. It will be
     * uncompressed.
     *
     * @return the appearance entry representing the normal appearance.
     * @throws IOException 
     */
    PDAppearanceContentStream getNormalAppearanceAsContentStream() throws IOException
    {
        return getNormalAppearanceAsContentStream(false);
    }
    
    /**
     * Get the annotations normal appearance content stream.
     * 
     * <p>
     * This will get the annotations normal appearance content stream, to 'draw' to.
     * 
     * @param compress whether the content stream is to be compressed. Set this to true when
     * creating long content streams.
     * @return the appearance entry representing the normal appearance.
     * @throws IOException
     */
    PDAppearanceContentStream getNormalAppearanceAsContentStream(boolean compress) throws IOException
    {
        PDAppearanceEntry appearanceEntry = getNormalAppearance();
        return getAppearanceEntryAsContentStream(appearanceEntry, compress);
    }
    
    /**
     * Get the annotations down appearance.
     * 
     * <p>
     * This will get the annotations down appearance. If this is not existent an
     * empty appearance entry will be created.
     * 
     * @return the appearance entry representing the down appearance.
     */
    PDAppearanceEntry getDownAppearance()
    {
        PDAppearanceDictionary appearanceDictionary = getAppearance();
        PDAppearanceEntry downAppearanceEntry = appearanceDictionary.getDownAppearance();

        if (downAppearanceEntry.isSubDictionary())
        {
            downAppearanceEntry = new PDAppearanceEntry(createCOSStream());
            appearanceDictionary.setDownAppearance(downAppearanceEntry);
        }

        return downAppearanceEntry;
    }

    /**
     * Get the annotations rollover appearance.
     * 
     * <p>
     * This will get the annotations rollover appearance. If this is not
     * existent an empty appearance entry will be created.
     * 
     * @return the appearance entry representing the rollover appearance.
     */
    PDAppearanceEntry getRolloverAppearance()
    {
        PDAppearanceDictionary appearanceDictionary = getAppearance();
        PDAppearanceEntry rolloverAppearanceEntry = appearanceDictionary.getRolloverAppearance();

        if (rolloverAppearanceEntry.isSubDictionary())
        {
            rolloverAppearanceEntry = new PDAppearanceEntry(createCOSStream());
            appearanceDictionary.setRolloverAppearance(rolloverAppearanceEntry);
        }

        return rolloverAppearanceEntry;
    }

    /**
     * Get a padded rectangle.
     * 
     * <p>Creates a new rectangle with padding applied to each side.
     * .
     * @param rectangle the rectangle.
     * @param padding the padding to apply.
     * @return the padded rectangle.
     */
    PDRectangle getPaddedRectangle(PDRectangle rectangle, float padding)
    {
        return new PDRectangle(rectangle.getLowerLeftX() + padding, rectangle.getLowerLeftY() + padding,
                rectangle.getWidth() - 2 * padding, rectangle.getHeight() - 2 * padding);
    }
    
    /**
     * Get a rectangle enlarged by the differences.
     *
     * <p>
     * Creates a new rectangle with differences added to each side. If there are no valid
     * differences, then the original rectangle is returned.
     *
     * @param rectangle the rectangle.
     * @param differences the differences to apply.
     * @return the padded rectangle.
     */
    PDRectangle addRectDifferences(PDRectangle rectangle, float[] differences)
    {
        if (differences == null || differences.length != 4)
        {
            return rectangle;
        }
        
        return new PDRectangle(rectangle.getLowerLeftX() - differences[0],
                rectangle.getLowerLeftY() - differences[1],
                rectangle.getWidth() + differences[0] + differences[2],
                rectangle.getHeight() + differences[1] + differences[3]);
    }
    
    /**
     * Get a rectangle with the differences applied to each side.
     *
     * <p>
     * Creates a new rectangle with differences added to each side. If there are no valid
     * differences, then the original rectangle is returned.
     *
     * @param rectangle the rectangle.
     * @param differences the differences to apply.
     * @return the padded rectangle.
     */
    PDRectangle applyRectDifferences(PDRectangle rectangle, float[] differences)
    {
        if (differences == null || differences.length != 4)
        {
            return rectangle;
        }
        return new PDRectangle(rectangle.getLowerLeftX() + differences[0],
                rectangle.getLowerLeftY() + differences[1],
                rectangle.getWidth() - differences[0] - differences[2],
                rectangle.getHeight() - differences[1] - differences[3]);
    }

    void setOpacity(PDAppearanceContentStream contentStream, float opacity) throws IOException
    {
        if (opacity < 1)
        {
            PDExtendedGraphicsState gs = new PDExtendedGraphicsState();
            gs.setStrokingAlphaConstant(opacity);
            gs.setNonStrokingAlphaConstant(opacity);

            contentStream.setGraphicsStateParameters(gs);
        }
    }

    /**
     * Draw a line ending style.
     * 
     * @param style
     * @param cs
     * @param x
     * @param y
     * @param width
     * @param hasStroke
     * @param hasBackground
     * @param ending false if left, true if right of an imagined horizontal line (important for
     * arrows).
     *
     * @throws IOException
     */
    void drawStyle(String style, final PDAppearanceContentStream cs, float x, float y,
                   float width, boolean hasStroke, boolean hasBackground, boolean ending) throws IOException
    {
        int sign = ending ? -1 : 1;

        if (PDAnnotationLine.LE_OPEN_ARROW.equals(style) || PDAnnotationLine.LE_CLOSED_ARROW.equals(style))
        {
            drawArrow(cs, x + sign * width, y, sign * width * 9);
        }
        else if (PDAnnotationLine.LE_BUTT.equals(style))
        {
            cs.moveTo(x, y - width * 3);
            cs.lineTo(x, y + width * 3);
        }
        else if (PDAnnotationLine.LE_DIAMOND.equals(style))
        {
            drawDiamond(cs, x, y, width * 3);
        }
        else if (PDAnnotationLine.LE_SQUARE.equals(style))
        {
            cs.addRect(x - width * 3, y - width * 3, width * 6, width * 6);
        }
        else if (PDAnnotationLine.LE_CIRCLE.equals(style))
        {
            drawCircle(cs, x, y, width * 3);
        }
        else if (PDAnnotationLine.LE_R_OPEN_ARROW.equals(style) || PDAnnotationLine.LE_R_CLOSED_ARROW.equals(style))
        {
            drawArrow(cs, x + (0 - sign) * width, y, (0 - sign) * width * 9);
        }
        else if (PDAnnotationLine.LE_SLASH.equals(style))
        {
            // the line is 18 x linewidth at an angle of 60°
            cs.moveTo(x + (float) (Math.cos(Math.toRadians(60)) * width * 9),
            y + (float) (Math.sin(Math.toRadians(60)) * width * 9));
            cs.lineTo(x + (float) (Math.cos(Math.toRadians(240)) * width * 9),
            y + (float) (Math.sin(Math.toRadians(240)) * width * 9));
        }



        if (PDAnnotationLine.LE_R_CLOSED_ARROW.equals(style) || 
            PDAnnotationLine.LE_CLOSED_ARROW.equals(style))
        {
            cs.closePath();
        }
        cs.drawShape(width, hasStroke, 
                     // make sure to only paint a background color (/IC value) 
                     // for interior color styles, even if an /IC value is set.
                     INTERIOR_COLOR_STYLES.contains(style) ? hasBackground : false);
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
    void drawArrow(PDAppearanceContentStream cs, float x, float y, float len) throws IOException
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
    void drawDiamond(PDAppearanceContentStream cs, float x, float y, float r) throws IOException
    {
        cs.moveTo(x - r, y);
        cs.lineTo(x, y + r);
        cs.lineTo(x + r, y);
        cs.lineTo(x, y - r);
        cs.closePath();
    }

    /**
     * Add a circle shape to the path in clockwise direction.
     *
     * @param cs Content stream
     * @param x
     * @param y
     * @param r Radius
     * 
     * @throws IOException If the content stream could not be written.
     */
    void drawCircle(PDAppearanceContentStream cs, float x, float y, float r) throws IOException
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

    /**
     * Add a circle shape to the path in counterclockwise direction. You'll need this e.g. when
     * drawing a doughnut shape. See "Nonzero Winding Number Rule" for more information.
     *
     * @param cs Content stream
     * @param x
     * @param y
     * @param r Radius
     *
     * @throws IOException If the content stream could not be written.
     */
    void drawCircle2(PDAppearanceContentStream cs, float x, float y, float r) throws IOException
    {
        // http://stackoverflow.com/a/2007782/535646
        float magic = r * 0.551784f;
        cs.moveTo(x, y + r);
        cs.curveTo(x - magic, y + r, x - r, y + magic, x - r, y);
        cs.curveTo(x - r, y - magic, x - magic, y - r, x, y - r);
        cs.curveTo(x + magic, y - r, x + r, y - magic, x + r, y);
        cs.curveTo(x + r, y + magic, x + magic, y + r, x, y + r);
        cs.closePath();
    }

    private static Set<String> createShortStyles()
    {
        Set<String> shortStyles = new HashSet<String>();
        shortStyles.add(PDAnnotationLine.LE_OPEN_ARROW);
        shortStyles.add(PDAnnotationLine.LE_CLOSED_ARROW);
        shortStyles.add(PDAnnotationLine.LE_SQUARE);
        shortStyles.add(PDAnnotationLine.LE_CIRCLE);
        shortStyles.add(PDAnnotationLine.LE_DIAMOND);
        return Collections.unmodifiableSet(shortStyles);
    }

    private static Set<String> createInteriorColorStyles()
    {
        Set<String> interiorColorStyles = new HashSet<String>();
        interiorColorStyles.add(PDAnnotationLine.LE_CLOSED_ARROW);
        interiorColorStyles.add(PDAnnotationLine.LE_CIRCLE);
        interiorColorStyles.add(PDAnnotationLine.LE_DIAMOND);
        interiorColorStyles.add(PDAnnotationLine.LE_R_CLOSED_ARROW);
        interiorColorStyles.add(PDAnnotationLine.LE_SQUARE);
        return Collections.unmodifiableSet(interiorColorStyles);
    }

    private static Set<String> createAngledStyles()
    {
        Set<String> angledStyles = new HashSet<String>();
        angledStyles.add(PDAnnotationLine.LE_CLOSED_ARROW);
        angledStyles.add(PDAnnotationLine.LE_OPEN_ARROW);
        angledStyles.add(PDAnnotationLine.LE_R_CLOSED_ARROW);
        angledStyles.add(PDAnnotationLine.LE_R_OPEN_ARROW);
        angledStyles.add(PDAnnotationLine.LE_BUTT);
        angledStyles.add(PDAnnotationLine.LE_SLASH);
        return Collections.unmodifiableSet(angledStyles);
    }

    /**
     * Get the annotations normal appearance.
     * 
     * <p>
     * This will get the annotations normal appearance. If this is not existent
     * an empty appearance entry will be created.
     * 
     * @return the appearance entry representing the normal appearance.
     */
    private PDAppearanceEntry getNormalAppearance()
    {
        PDAppearanceDictionary appearanceDictionary = getAppearance();
        PDAppearanceEntry normalAppearanceEntry = appearanceDictionary.getNormalAppearance();

        if (normalAppearanceEntry == null || normalAppearanceEntry.isSubDictionary())
        {
            normalAppearanceEntry = new PDAppearanceEntry(createCOSStream());
            appearanceDictionary.setNormalAppearance(normalAppearanceEntry);
        }

        return normalAppearanceEntry;
    }
    
    
    private PDAppearanceContentStream getAppearanceEntryAsContentStream(
              PDAppearanceEntry appearanceEntry, boolean compress) throws IOException
    {
        PDAppearanceStream appearanceStream = appearanceEntry.getAppearanceStream();
        setTransformationMatrix(appearanceStream);

        // ensure there are resources
        PDResources resources = appearanceStream.getResources();
        if (resources == null)
        {
            resources = new PDResources();
            appearanceStream.setResources(resources);
        }

        return new PDAppearanceContentStream(appearanceStream, compress);
    }
    
    private void setTransformationMatrix(PDAppearanceStream appearanceStream)
    {
        PDRectangle bbox = getRectangle();
        appearanceStream.setBBox(bbox);
        AffineTransform transform = AffineTransform.getTranslateInstance(-bbox.getLowerLeftX(),
                -bbox.getLowerLeftY());
        appearanceStream.setMatrix(transform);
    }

    PDRectangle handleBorderBox(PDAnnotationSquareCircle annotation, float lineWidth)
    {
        // There are two options. The handling is not part of the PDF specification but
        // implementation specific to Adobe Reader
        // - if /RD is set the border box is the /Rect entry inset by the respective
        //   border difference.
        // - if /RD is not set the border box is defined by the /Rect entry. The /RD entry will
        //   be set to be the line width and the /Rect is enlarged by the /RD amount
        PDRectangle borderBox;
        float[] rectDifferences = annotation.getRectDifferences();
        if (rectDifferences.length == 0)
        {
            borderBox = getPaddedRectangle(getRectangle(), lineWidth / 2);
            // the differences rectangle
            annotation.setRectDifferences(lineWidth / 2);
            annotation.setRectangle(addRectDifferences(getRectangle(), annotation.getRectDifferences()));
            // when the normal appearance stream was generated BBox and Matrix have been set to the
            // values of the original /Rect. As the /Rect was changed that needs to be adjusted too.
            annotation.getNormalAppearanceStream().setBBox(getRectangle());
            AffineTransform transform = AffineTransform.getTranslateInstance(-getRectangle().getLowerLeftX(), -getRectangle().getLowerLeftY());
            annotation.getNormalAppearanceStream().setMatrix(transform);
        }
        else
        {
            borderBox = applyRectDifferences(getRectangle(), rectDifferences);
            borderBox = getPaddedRectangle(borderBox, lineWidth / 2);
        }
        return borderBox;
    }
}

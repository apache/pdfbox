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

import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationSquareCircle;
import org.apache.pdfbox.pdmodel.PDAppearanceContentStream;
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
    
    public PDAbstractAppearanceHandler(PDAnnotation annotation)
    {
        this.annotation = annotation;
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
     * This will get the annotations normal appearance content stream,
     * to 'draw' to.
     * 
     * @return the appearance entry representing the normal appearance.
     * @throws IOException 
     */
    PDAppearanceContentStream getNormalAppearanceAsContentStream() throws IOException
    {
        PDAppearanceEntry appearanceEntry = getNormalAppearance();
        return getAppearanceEntryAsContentStream(appearanceEntry);
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
            //TODO replace with "document.getDocument().createCOSStream()" 
            downAppearanceEntry = new PDAppearanceEntry(new COSStream());
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
            //TODO replace with "document.getDocument().createCOSStream()" 
            rolloverAppearanceEntry = new PDAppearanceEntry(new COSStream());
            appearanceDictionary.setRolloverAppearance(rolloverAppearanceEntry);
        }

        return rolloverAppearanceEntry;
    }
    
    /**
     * Set the differences rectangle.
     */
    void setRectDifference(float lineWidth)
    {
        if (annotation instanceof PDAnnotationSquareCircle && lineWidth > 0)
        {
            PDRectangle differences = new PDRectangle(lineWidth/2, lineWidth/2,0,0);
            ((PDAnnotationSquareCircle) annotation).setRectDifference(differences);
        }
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
     * <p>Creates a new rectangle with differences added to each side.
     * .
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
     * <p>Creates a new rectangle with differences added to each side.
     * .
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

        if (normalAppearanceEntry.isSubDictionary())
        {
            //TODO replace with "document.getDocument().createCOSStream()" 
            normalAppearanceEntry = new PDAppearanceEntry(new COSStream());
            appearanceDictionary.setNormalAppearance(normalAppearanceEntry);
        }

        return normalAppearanceEntry;
    }
    
    
    private PDAppearanceContentStream getAppearanceEntryAsContentStream(PDAppearanceEntry appearanceEntry) throws IOException
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

        return new PDAppearanceContentStream(appearanceStream);
    }
    
    private void setTransformationMatrix(PDAppearanceStream appearanceStream)
    {
        PDRectangle bbox = getRectangle();
        appearanceStream.setBBox(bbox);
        AffineTransform transform = AffineTransform.getTranslateInstance(-bbox.getLowerLeftX(),
                -bbox.getLowerLeftY());
        appearanceStream.setMatrix(transform);
    }
}

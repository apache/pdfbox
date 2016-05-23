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

import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationSquareCircle;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceDictionary;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceEntry;

/**
 * Generic handler to generate the fields appearance.
 * 
 * Individual handler will provide specific implementations for different field
 * types.
 * 
 */
public abstract class PDAppearanceHandler
{

    private PDAnnotation annotation;

    public PDAppearanceHandler(PDAnnotation annotation)
    {
        this.annotation = annotation;

    }

    public void generateAppearanceStreams()
    {
        if (annotation.getRectangle() != null)
        {
            generateNormalAppearance();
            generateRolloverAppearance();
            generateDownAppearance();
        }
    }

    public abstract void generateNormalAppearance();

    public abstract void generateRolloverAppearance();

    public abstract void generateDownAppearance();

    PDAnnotation getAnnotation()
    {
        return annotation;
    }

    /**
     * Get the line with of the border.
     * 
     * Get the width of the line used to draw a border around the annotation.
     * This may either be specified by the annotation dictionaries Border
     * setting or by the W entry in the BS border style dictionary. If both are
     * missing the default width is 1.
     * 
     * @return the line width
     */
    // TODO: according to the PDF spec the use of the BS entry is annotation
    // specific
    // so we will leave that to be implemented by individual handlers.
    // If at the end all annotations support the BS entry this can be handled
    // here and removed from the individual handlers.
    abstract float getLineWidth();

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
     * Get the annotations normal appearance.
     * 
     * <p>
     * This will get the annotations normal appearance. If this is not existent
     * an empty appearance entry will be created.
     * 
     * @return the appearance entry representing the normal appearance.
     */
    PDAppearanceEntry getNormalAppearance()
    {
        PDAppearanceDictionary appearanceDictionary = getAppearance();
        PDAppearanceEntry appearanceEntry = appearanceDictionary.getNormalAppearance();

        if (appearanceEntry.isSubDictionary())
        {
            appearanceEntry = new PDAppearanceEntry(new COSStream());
            appearanceDictionary.setNormalAppearance(appearanceEntry);
        }

        return appearanceEntry;
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
        PDAppearanceEntry appearanceEntry = appearanceDictionary.getDownAppearance();

        if (appearanceEntry.isSubDictionary())
        {
            appearanceEntry = new PDAppearanceEntry(new COSStream());
            appearanceDictionary.setDownAppearance(appearanceEntry);
        }

        return appearanceEntry;
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
        PDAppearanceEntry appearanceEntry = appearanceDictionary.getRolloverAppearance();

        if (appearanceEntry.isSubDictionary())
        {
            appearanceEntry = new PDAppearanceEntry(new COSStream());
            appearanceDictionary.setRolloverAppearance(appearanceEntry);
        }

        return appearanceEntry;
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
}

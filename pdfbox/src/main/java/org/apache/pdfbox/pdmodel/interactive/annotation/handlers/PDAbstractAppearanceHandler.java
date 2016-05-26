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
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceContentStream;
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
    private PDAnnotation annotation;
    private PDAppearanceEntry appearanceEntry;
    private PDAppearanceContentStream contentStream;
    
    public PDAbstractAppearanceHandler(PDAnnotation annotation)
    {
        this.annotation = annotation;
    }

    public abstract void generateNormalAppearance();

    public abstract void generateRolloverAppearance();

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
        appearanceEntry = getNormalAppearance();
        contentStream = getAppearanceEntryAsContentStream(appearanceEntry);
        return contentStream;
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

    void handleOpacity(float opacity) throws IOException
    {
        if (opacity < 1)
        {
            PDExtendedGraphicsState gs = new PDExtendedGraphicsState();
            gs.setStrokingAlphaConstant(opacity);
            gs.setNonStrokingAlphaConstant(opacity);
            
            PDAppearanceStream appearanceStream = appearanceEntry.getAppearanceStream();
            
            PDResources resources = appearanceStream.getResources();
            if (resources == null)
            {
                resources = new PDResources();
                appearanceStream.setResources(resources);
                contentStream.setResources(resources);
            }
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
        PDAppearanceEntry appearanceEntry = appearanceDictionary.getNormalAppearance();

        if (appearanceEntry.isSubDictionary())
        {
            appearanceEntry = new PDAppearanceEntry(new COSStream());
            appearanceDictionary.setNormalAppearance(appearanceEntry);
        }

        return appearanceEntry;
    }
    
    
    private PDAppearanceContentStream getAppearanceEntryAsContentStream(PDAppearanceEntry appearanceEntry) throws IOException
    {
        PDAppearanceStream appearanceStream = appearanceEntry.getAppearanceStream();
        setTransformationMatrix(appearanceStream);
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

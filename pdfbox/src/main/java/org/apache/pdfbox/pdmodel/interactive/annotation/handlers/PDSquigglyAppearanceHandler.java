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
import java.io.IOException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDAppearanceContentStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDFormContentStream;
import org.apache.pdfbox.pdmodel.PDPatternContentStream;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.apache.pdfbox.pdmodel.graphics.color.PDPattern;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.pattern.PDTilingPattern;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationTextMarkup;
import org.apache.pdfbox.util.Matrix;

/**
 *
 */
public class PDSquigglyAppearanceHandler extends PDAbstractAppearanceHandler
{
    private static final Log LOG = LogFactory.getLog(PDSquigglyAppearanceHandler.class);

    public PDSquigglyAppearanceHandler(PDAnnotation annotation)
    {
        super(annotation);
    }

    public PDSquigglyAppearanceHandler(PDAnnotation annotation, PDDocument document)
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
        PDAnnotationTextMarkup annotation = (PDAnnotationTextMarkup) getAnnotation();
        PDRectangle rect = annotation.getRectangle();
        float[] pathsArray = annotation.getQuadPoints();
        if (pathsArray == null)
        {
            return;
        }
        AnnotationBorder ab = AnnotationBorder.getAnnotationBorder(annotation, annotation.getBorderStyle());
        PDColor color = annotation.getColor();
        if (color == null || color.getComponents().length == 0)
        {
            return;
        }
        if (Float.compare(ab.width, 0) == 0)
        {
            // value found in adobe reader
            ab.width = 1.5f;
        }

        // Adjust rectangle even if not empty, see PLPDF.com-MarkupAnnotations.pdf
        //TODO in a class structure this should be overridable
        // this is similar to polyline but different data type
        // all coordinates (unlike painting) are used because I'm lazy
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
        rect.setLowerLeftX(Math.min(minX - ab.width / 2, rect.getLowerLeftX()));
        rect.setLowerLeftY(Math.min(minY - ab.width / 2, rect.getLowerLeftY()));
        rect.setUpperRightX(Math.max(maxX + ab.width / 2, rect.getUpperRightX()));
        rect.setUpperRightY(Math.max(maxY + ab.width / 2, rect.getUpperRightY()));
        annotation.setRectangle(rect);

        PDAppearanceContentStream cs = null;

        try
        {
            cs = getNormalAppearanceAsContentStream();

            setOpacity(cs, annotation.getConstantOpacity());

            cs.setStrokingColor(color);

            //TODO we ignore dash pattern and line width for now. Do they have any effect?


            // quadpoints spec is incorrect
            // https://stackoverflow.com/questions/9855814/pdf-spec-vs-acrobat-creation-quadpoints
            for (int i = 0; i < pathsArray.length / 8; ++i)
            {
                // Adobe uses a fixed pattern that assumes a height of 40, and it transforms to that height
                // horizontally and the same / 1.8 vertically.
                // translation apparently based on bottom left, but slightly different in Adobe
                //TODO what if the annotation is not horizontal?
                float height = pathsArray[i * 8 + 1] - pathsArray[i * 8 + 5];
                cs.transform(new Matrix(height / 40f, 0, 0, height / 40f / 1.8f, pathsArray[i * 8 + 4], pathsArray[i * 8 + 5]));

                // Create form, BBox is mostly fixed, except for the horizontal size which is
                // horizontal size divided by the horizontal transform factor from above
                // (almost)
                PDFormXObject form = new PDFormXObject(createCOSStream());
                form.setBBox(new PDRectangle(-0.5f, -0.5f, (pathsArray[i * 8 + 2] - pathsArray[i * 8]) / height * 40f + 0.5f, 13));
                form.setResources(new PDResources());
                form.setMatrix(AffineTransform.getTranslateInstance(0.5f, 0.5f));
                cs.drawForm(form);

                PDFormContentStream formCS = null;

                try
                {
                    formCS = new PDFormContentStream(form);
                    PDTilingPattern pattern = new PDTilingPattern();
                    pattern.setBBox(new PDRectangle(0, 0, 10, 12));
                    pattern.setXStep(10);
                    pattern.setYStep(13);
                    pattern.setTilingType(PDTilingPattern.TILING_CONSTANT_SPACING_FASTER_TILING);
                    pattern.setPaintType(PDTilingPattern.PAINT_UNCOLORED);

                    PDPatternContentStream patternCS = null;

                    try
                    {
                        patternCS = new PDPatternContentStream(pattern);
                        // from Adobe
                        patternCS.setLineCapStyle(1);
                        patternCS.setLineJoinStyle(1);
                        patternCS.setLineWidth(1);
                        patternCS.setMiterLimit(10);
                        patternCS.moveTo(0, 1);
                        patternCS.lineTo(5, 11);
                        patternCS.lineTo(10, 1);
                        patternCS.stroke();
                    }
                    finally
                    {
                        IOUtils.closeQuietly(patternCS);
                    }

                    COSName patternName = form.getResources().add(pattern);
                    PDColorSpace patternColorSpace = new PDPattern(null, PDDeviceRGB.INSTANCE);
                    PDColor patternColor = new PDColor(color.getComponents(), patternName, patternColorSpace);
                    formCS.setNonStrokingColor(patternColor);

                    // With Adobe, the horizontal size is slightly different, don't know why
                    formCS.addRect(0, 0, (pathsArray[i * 8 + 2] - pathsArray[i * 8]) / height * 40f, 12);
                    formCS.fill();
                }
                finally
                {
                    IOUtils.closeQuietly(formCS);
                }
            }
        }
        catch (IOException ex)
        {
            LOG.error(ex);
        }
        finally
        {
            IOUtils.closeQuietly(cs);
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

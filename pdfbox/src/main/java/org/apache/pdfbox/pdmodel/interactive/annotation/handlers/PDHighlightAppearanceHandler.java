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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDFormContentStream;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.blend.BlendMode;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationTextMarkup;
import org.apache.pdfbox.pdmodel.PDAppearanceContentStream;
import org.apache.pdfbox.pdmodel.PDDocument;

/**
 * 
 * @author Tilman Hausherr
 */
public class PDHighlightAppearanceHandler extends PDAbstractAppearanceHandler
{
    private static final Log LOG = LogFactory.getLog(PDHighlightAppearanceHandler.class);

    public PDHighlightAppearanceHandler(PDAnnotation annotation)
    {
        super(annotation);
    }

    public PDHighlightAppearanceHandler(PDAnnotation annotation, PDDocument document)
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

        // Adjust rectangle even if not empty, see PLPDF.com-MarkupAnnotations.pdf
        //TODO in a class structure this should be overridable
        // this is similar to polyline but different data type
        //TODO padding should consider the curves too; needs to know in advance where the curve is
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

        // get the delta used for curves and use it for padding
        float maxDelta = 0;
        for (int i = 0; i < pathsArray.length / 8; ++i)
        {
            // one of the two is 0, depending whether the rectangle is 
            // horizontal or vertical
            // if it is diagonal then... uh...
            float delta = Math.max((pathsArray[i + 0] - pathsArray[i + 4]) / 4, 
                                   (pathsArray[i + 1] - pathsArray[i + 5]) / 4);
            maxDelta = Math.max(delta, maxDelta);
        }

        rect.setLowerLeftX(Math.min(minX - ab.width / 2 - maxDelta, rect.getLowerLeftX()));
        rect.setLowerLeftY(Math.min(minY - ab.width / 2 - maxDelta, rect.getLowerLeftY()));
        rect.setUpperRightX(Math.max(maxX + ab.width + maxDelta, rect.getUpperRightX()));
        rect.setUpperRightY(Math.max(maxY + ab.width + maxDelta, rect.getUpperRightY()));
        annotation.setRectangle(rect);

        PDAppearanceContentStream cs = null;

        try
        {

            cs = getNormalAppearanceAsContentStream();

            PDExtendedGraphicsState r0 = new PDExtendedGraphicsState();
            PDExtendedGraphicsState r1 = new PDExtendedGraphicsState();
            r0.setAlphaSourceFlag(false);
            r0.setStrokingAlphaConstant(annotation.getConstantOpacity());
            r0.setNonStrokingAlphaConstant(annotation.getConstantOpacity());
            r1.setAlphaSourceFlag(false);
            r1.setBlendMode(BlendMode.MULTIPLY);
            cs.setGraphicsStateParameters(r0);
            cs.setGraphicsStateParameters(r1);
            //TODO replace with document.getDocument().createCOSStream()
            //     or call new PDFormXObject(document)
            PDFormXObject frm1 = new PDFormXObject(createCOSStream());
            PDFormXObject frm2 = new PDFormXObject(createCOSStream());
            frm1.setResources(new PDResources());

            PDFormContentStream mwfofrmCS =null;
            try
            {
                mwfofrmCS = new PDFormContentStream(frm1);
                mwfofrmCS.drawForm(frm2);
            }
            finally
            {
                IOUtils.closeQuietly(mwfofrmCS);
            }
            frm1.setBBox(annotation.getRectangle());
            COSDictionary groupDict = new COSDictionary();
            groupDict.setItem(COSName.S, COSName.TRANSPARENCY);
            //TODO PDFormXObject.setGroup() is missing
            frm1.getCOSObject().setItem(COSName.GROUP, groupDict);
            cs.drawForm(frm1);
            frm2.setBBox(annotation.getRectangle());

            PDFormContentStream frm2CS = null;

            try
            {
                frm2CS = new PDFormContentStream(frm2);
                frm2CS.setNonStrokingColor(color);
                int of = 0;
                while (of + 7 < pathsArray.length)
                {
                    // quadpoints spec sequence is incorrect, correct one is (4,5 0,1 2,3 6,7)
                    // https://stackoverflow.com/questions/9855814/pdf-spec-vs-acrobat-creation-quadpoints

                    // for "curvy" highlighting, two Bézier control points are used that seem to have a
                    // distance of about 1/4 of the height.
                    // note that curves won't appear if outside of the rectangle
                    float delta = 0;
                    if (Float.compare(pathsArray[of + 0], pathsArray[of + 4]) == 0 &&
                        Float.compare(pathsArray[of + 1], pathsArray[of + 3]) == 0 &&
                        Float.compare(pathsArray[of + 2], pathsArray[of + 6]) == 0 &&
                        Float.compare(pathsArray[of + 5], pathsArray[of + 7]) == 0)
                    {
                        // horizontal highlight
                        delta = (pathsArray[of + 1] - pathsArray[of + 5]) / 4;
                    }
                    else if (Float.compare(pathsArray[of + 1], pathsArray[of + 5]) == 0 &&
                             Float.compare(pathsArray[of + 0], pathsArray[of + 2]) == 0 &&
                             Float.compare(pathsArray[of + 3], pathsArray[of + 7]) == 0 &&
                             Float.compare(pathsArray[of + 4], pathsArray[of + 6]) == 0)
                    {
                        // vertical highlight
                        delta = (pathsArray[of + 0] - pathsArray[of + 4]) / 4;
                    }

                    frm2CS.moveTo(pathsArray[of + 4], pathsArray[of + 5]);

                    if (Float.compare(pathsArray[of + 0], pathsArray[of + 4]) == 0)
                    {
                        // horizontal highlight
                        frm2CS.curveTo(pathsArray[of + 4] - delta, pathsArray[of + 5] + delta,
                                       pathsArray[of + 0] - delta, pathsArray[of + 1] - delta,
                                       pathsArray[of + 0], pathsArray[of + 1]);
                    }
                    else if (Float.compare(pathsArray[of + 5], pathsArray[of + 1]) == 0)
                    {
                        // vertical highlight
                        frm2CS.curveTo(pathsArray[of + 4] + delta, pathsArray[of + 5] + delta,
                                       pathsArray[of + 0] - delta, pathsArray[of + 1] + delta,
                                       pathsArray[of + 0], pathsArray[of + 1]);
                    }
                    else
                    {
                        frm2CS.lineTo(pathsArray[of + 0], pathsArray[of + 1]);
                    }
                    frm2CS.lineTo(pathsArray[of + 2], pathsArray[of + 3]);
                    if (Float.compare(pathsArray[of + 2], pathsArray[of + 6]) == 0)
                    {
                        // horizontal highlight
                        frm2CS.curveTo(pathsArray[of + 2] + delta, pathsArray[of + 3] - delta,
                                       pathsArray[of + 6] + delta, pathsArray[of + 7] + delta,
                                       pathsArray[of + 6], pathsArray[of + 7]);
                    }
                    else if (Float.compare(pathsArray[of + 3], pathsArray[of + 7]) == 0)
                    {
                        // vertical highlight
                        frm2CS.curveTo(pathsArray[of + 2] - delta, pathsArray[of + 3] - delta,
                                       pathsArray[of + 6] + delta, pathsArray[of + 7] - delta,
                                       pathsArray[of + 6], pathsArray[of + 7]);
                    }
                    else
                    {
                        frm2CS.lineTo(pathsArray[of + 6], pathsArray[of + 7]);
                    }

                    frm2CS.fill();
                    of += 8;
                }
            }
            finally
            {
                IOUtils.closeQuietly(frm2CS);
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

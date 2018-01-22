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
import java.io.OutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationHighlight;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceContentStream;
import org.apache.pdfbox.util.Charsets;

/**
 *
 */
public class PDHighlightAppearanceHandler extends PDAbstractAppearanceHandler
{
    private static final Log LOG = LogFactory.getLog(PDHighlightAppearanceHandler.class);

    public PDHighlightAppearanceHandler(PDAnnotation annotation)
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
        PDAnnotationHighlight annotation = (PDAnnotationHighlight) getAnnotation();
        PDRectangle rect = annotation.getRectangle();
        float[] pathsArray = annotation.getQuadPoints();
        if (pathsArray == null)
        {
            return;
        }
        AnnotationBorder ab = AnnotationBorder.getAnnotationBorder(annotation, annotation.getBorderStyle());
        if (ab.color.getComponents().length == 0)
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
        rect.setLowerLeftX(Math.min(minX - ab.width / 2, rect.getLowerLeftX()));
        rect.setLowerLeftY(Math.min(minY - ab.width / 2, rect.getLowerLeftY()));
        rect.setUpperRightX(Math.max(maxX + ab.width, rect.getUpperRightX()));
        rect.setUpperRightY(Math.max(maxY + ab.width, rect.getUpperRightY()));
        annotation.setRectangle(rect);

        try
        {
            try (PDAppearanceContentStream cs = getNormalAppearanceAsContentStream())
            {
                prepareResources();
                PDExtendedGraphicsState r0 = new PDExtendedGraphicsState();
                PDExtendedGraphicsState r1 = new PDExtendedGraphicsState();
                r0.setAlphaSourceFlag(false);
                r0.setStrokingAlphaConstant(annotation.getConstantOpacity());
                r0.setNonStrokingAlphaConstant(annotation.getConstantOpacity());
                r1.setAlphaSourceFlag(false);
                //TODO PDExtendedGraphicsState.setBlendMode() is missing
                r1.getCOSObject().setItem(COSName.BM, COSName.MULTIPLY);
                cs.setGraphicsStateParameters(r0);
                cs.setGraphicsStateParameters(r1);
                //TODO replace with document.getDocument().createCOSStream()
                COSStream mwfoformStrm = new COSStream();
                OutputStream os = mwfoformStrm.createOutputStream();
                os.write("/Form Do".getBytes(Charsets.ISO_8859_1));
                os.close();
                PDFormXObject mwfofrm = new PDFormXObject(mwfoformStrm);
                cs.drawForm(mwfofrm);
                COSStream frmStrm2 = new COSStream();
                PDFormXObject frm2 = new PDFormXObject(frmStrm2);
                PDResources res = new PDResources();
                mwfofrm.setBBox(annotation.getRectangle());
                mwfofrm.setResources(res);
                COSDictionary groupDict = new COSDictionary();
                groupDict.setItem(COSName.S, COSName.TRANSPARENCY);
                //TODO PDFormXObject.setGroup() is missing
                mwfofrm.getCOSObject().setItem(COSName.GROUP, groupDict);
                res.put(COSName.getPDFName("Form"), frm2);
                frm2.setBBox(annotation.getRectangle());
                os = frm2.getCOSObject().createOutputStream();
                //TODO why can't we get a "classic" content stream?
                PDColor color = annotation.getColor();
                switch (color.getComponents().length)
                {
                    case 1:
                        os.write(String.format(java.util.Locale.US, "%.6f g%n", 
                                color.getComponents()[0]).getBytes(Charsets.ISO_8859_1));
                        break;
                    case 3:
                        os.write(String.format(java.util.Locale.US, "%.6f %.6f %.6f rg%n", 
                                color.getComponents()[0], 
                                color.getComponents()[1], 
                                color.getComponents()[2]).getBytes(Charsets.ISO_8859_1));
                        break;
                    case 4:
                        os.write(String.format(java.util.Locale.US, "%.6f %.6f %.6f %.6f k%n", 
                                color.getComponents()[0], 
                                color.getComponents()[1], 
                                color.getComponents()[2], 
                                color.getComponents()[3]).getBytes(Charsets.ISO_8859_1));
                        break;
                    default:
                        break;
                }
                int of = 0;
                while (of + 7 < pathsArray.length)
                {
                    // quadpoints spec sequence is incorrect, correct one is (4,5 0,1 2,3 6,7)
                    // https://stackoverflow.com/questions/9855814/pdf-spec-vs-acrobat-creation-quadpoints

                    // for "curvy" highlighting, two Bézier control points are used that seem to have a 
                    // distance of about 1/4 of the height.
                    // note that curves won't appear if outside of the rectangle
                    float delta = 0;
                    if (pathsArray[of + 0] == pathsArray[of + 4] && 
                        pathsArray[of + 1] == pathsArray[of + 3] && 
                        pathsArray[of + 2] == pathsArray[of + 6] && 
                        pathsArray[of + 5] == pathsArray[of + 7])
                    {
                        // horizontal highlight
                        delta = (pathsArray[of + 1] - pathsArray[of + 5]) / 4;
                    }
                    else if (pathsArray[of + 1] == pathsArray[of + 5] && 
                             pathsArray[of + 0] == pathsArray[of + 2] && 
                             pathsArray[of + 3] == pathsArray[of + 7] && 
                             pathsArray[of + 4] == pathsArray[of + 6])
                    {
                        // vertical highlight
                        delta = (pathsArray[of + 0] - pathsArray[of + 4]) / 4;
                    }

                    os.write(String.format(java.util.Locale.US, "%.4f %.4f m%n",
                            pathsArray[of + 4], pathsArray[of + 5]).getBytes(Charsets.ISO_8859_1));
                    if (pathsArray[of + 0] == pathsArray[of + 4])
                    {
                        // horizontal highlight
                        os.write(String.format(java.util.Locale.US, "%.4f %.4f %.4f %.4f %.4f %.4f c%n",
                                pathsArray[of + 4] - delta, pathsArray[of + 5] + delta,
                                pathsArray[of + 0] - delta, pathsArray[of + 1] - delta,
                                pathsArray[of + 0], pathsArray[of + 1]).getBytes(Charsets.ISO_8859_1));
                    }
                    else if (pathsArray[of + 5] == pathsArray[of + 1])
                    {
                        // vertical highlight
                        os.write(String.format(java.util.Locale.US, "%.4f %.4f %.4f %.4f %.4f %.4f c%n",
                                pathsArray[of + 4] + delta, pathsArray[of + 5] + delta,
                                pathsArray[of + 0] - delta, pathsArray[of + 1] + delta,
                                pathsArray[of + 0], pathsArray[of + 1]).getBytes(Charsets.ISO_8859_1));
                    }
                    else
                    {
                        os.write(String.format(java.util.Locale.US, "%.4f %.4f l%n",
                                pathsArray[of + 0], pathsArray[of + 1]).getBytes(Charsets.ISO_8859_1));
                    }
                    os.write(String.format(java.util.Locale.US, "%.4f %.4f l%n",
                            pathsArray[of + 2], pathsArray[of + 3]).getBytes(Charsets.ISO_8859_1));
                    if (pathsArray[of + 2] == pathsArray[of + 6])
                    {
                        // horizontal highlight
                        os.write(String.format(java.util.Locale.US, "%.4f %.4f %.4f %.4f %.4f %.4f c%n",
                                pathsArray[of + 2] + delta, pathsArray[of + 3] - delta,
                                pathsArray[of + 6] + delta, pathsArray[of + 7] + delta,
                                pathsArray[of + 6], pathsArray[of + 7]).getBytes(Charsets.ISO_8859_1));
                    }
                    else if (pathsArray[of + 3] == pathsArray[of + 7])
                    {
                        // vertical highlight
                        os.write(String.format(java.util.Locale.US, "%.4f %.4f %.4f %.4f %.4f %.4f c%n",
                                pathsArray[of + 2] - delta, pathsArray[of + 3] - delta,
                                pathsArray[of + 6] + delta, pathsArray[of + 7] - delta,
                                pathsArray[of + 6], pathsArray[of + 7]).getBytes(Charsets.ISO_8859_1));
                    }
                    else
                    {
                        os.write(String.format(java.util.Locale.US, "%.4f %.4f l%n",
                                pathsArray[of + 6], pathsArray[of + 7]).getBytes(Charsets.ISO_8859_1));
                    }

                    os.write("f\n".getBytes(Charsets.ISO_8859_1));
                    of += 8;

                    //TODO Adobe puts a "w" (line width). Why?
                    //TODO If quadpoints is not present or the conforming reader does not recognize it, 
                    //     the region specified by the Rect entry should be used.
                    //     QuadPoints shall be ignored if any coordinate in the array lies
                    //     outside the region specified by Rect
                }
                os.close();
            }
        }
        catch (IOException ex)
        {
            LOG.error(ex);
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

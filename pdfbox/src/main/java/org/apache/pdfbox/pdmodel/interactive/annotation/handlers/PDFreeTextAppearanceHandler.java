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
import org.apache.fontbox.util.Charsets;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.pdfparser.PDFStreamParser;
import org.apache.pdfbox.pdmodel.PDAppearanceContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceCMYK;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceGray;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationFreeText;
import static org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLine.LE_NONE;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceStream;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDBorderEffectDictionary;
import static org.apache.pdfbox.pdmodel.interactive.annotation.handlers.PDAbstractAppearanceHandler.SHORT_STYLES;
import org.apache.pdfbox.pdmodel.interactive.annotation.layout.AppearanceStyle;
import org.apache.pdfbox.pdmodel.interactive.annotation.layout.PlainText;
import org.apache.pdfbox.pdmodel.interactive.annotation.layout.PlainTextFormatter;
import org.apache.pdfbox.util.Matrix;

public class PDFreeTextAppearanceHandler extends PDAbstractAppearanceHandler
{
    private static final Log LOG = LogFactory.getLog(PDFreeTextAppearanceHandler.class);

    public PDFreeTextAppearanceHandler(PDAnnotation annotation)
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
        PDAnnotationFreeText annotation = (PDAnnotationFreeText) getAnnotation();
        float[] pathsArray = new float[0];
        if ("FreeTextCallout".equals(annotation.getIntent()))
        {
            pathsArray = annotation.getCallout();
            if (pathsArray == null || pathsArray.length != 4 && pathsArray.length != 6)
            {
                pathsArray = new float[0];
            }
        }
        AnnotationBorder ab = AnnotationBorder.getAnnotationBorder(annotation, annotation.getBorderStyle());

        try (PDAppearanceContentStream cs = getNormalAppearanceAsContentStream())
        {
            // The fill color is the /C entry, there is no /IC entry defined
            boolean hasBackground = cs.setNonStrokingColorOnDemand(annotation.getColor());
            setOpacity(cs, annotation.getConstantOpacity());

            // Adobe uses the last non stroking color from /DA as stroking color!
            PDColor strokingColor = extractNonStrokingColor(annotation);
            boolean hasStroke = cs.setStrokingColorOnDemand(strokingColor);

            if (ab.dashArray != null)
            {
                cs.setLineDashPattern(ab.dashArray, 0);
            }
            cs.setLineWidth(ab.width);

            // draw callout line(s)
            // must be done before retangle paint to avoid a line cutting through cloud
            // see CTAN-example-Annotations.pdf
            for (int i = 0; i < pathsArray.length / 2; ++i)
            {
                float x = pathsArray[i * 2];
                float y = pathsArray[i * 2 + 1];
                if (i == 0)
                {
                    if (SHORT_STYLES.contains(annotation.getLineEndingStyle()))
                    {
                        // modify coordinate to shorten the segment
                        // https://stackoverflow.com/questions/7740507/extend-a-line-segment-a-specific-distance
                        float x1 = pathsArray[2];
                        float y1 = pathsArray[3];
                        float len = (float) (Math.sqrt(Math.pow(x - x1, 2) + Math.pow(y - y1, 2)));
                        if (Float.compare(len, 0) != 0)
                        {
                            x += (x1 - x) / len * ab.width;
                            y += (y1 - y) / len * ab.width;
                        }
                    }
                    cs.moveTo(x, y);
                }
                else
                {
                    cs.lineTo(x, y);
                }
            }
            cs.stroke();

            // paint the styles here and after line(s) draw, to avoid line crossing a filled shape       
            if ("FreeTextCallout".equals(annotation.getIntent())
                    // check only needed to avoid q cm Q if LE_NONE
                    && !LE_NONE.equals(annotation.getLineEndingStyle())
                    && pathsArray.length >= 4)
            {
                float x2 = pathsArray[2];
                float y2 = pathsArray[3];
                float x1 = pathsArray[0];
                float y1 = pathsArray[1];
                cs.saveGraphicsState();
                if (ANGLED_STYLES.contains(annotation.getLineEndingStyle()))
                {
                    // do a transform so that first "arm" is imagined flat,
                    // like in line handler.
                    // The alternative would be to apply the transform to the 
                    // LE shape coordinates directly, which would be more work 
                    // and produce code difficult to understand
                    double angle = Math.atan2(y2 - y1, x2 - x1);
                    cs.transform(Matrix.getRotateInstance(angle, x1, y1));
                }
                else
                {
                    cs.transform(Matrix.getTranslateInstance(x1, y1));
                }
                drawStyle(annotation.getLineEndingStyle(), cs, 0, 0, ab.width, hasStroke, hasBackground);
                cs.restoreGraphicsState();
            }



            //TODO this segment was copied from square handler. Refactor?
            PDBorderEffectDictionary borderEffect = annotation.getBorderEffect();
            if (borderEffect != null && borderEffect.getStyle().equals(PDBorderEffectDictionary.STYLE_CLOUDY))
            {
                CloudyBorder cloudyBorder = new CloudyBorder(cs,
                    borderEffect.getIntensity(), ab.width, getRectangle());
                cloudyBorder.createCloudyRectangle(annotation.getRectDifference());
                annotation.setRectangle(cloudyBorder.getRectangle());
                annotation.setRectDifference(cloudyBorder.getRectDifference());
                PDAppearanceStream appearanceStream = annotation.getNormalAppearanceStream();
                appearanceStream.setBBox(cloudyBorder.getBBox());
                appearanceStream.setMatrix(cloudyBorder.getMatrix());
            }
            else
            {
                // handle the border box
                //
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
                    borderBox = getPaddedRectangle(getRectangle(), ab.width/2);
                    // the differences rectangle
                    annotation.setRectDifferences(ab.width/2);
                    annotation.setRectangle(addRectDifferences(getRectangle(), annotation.getRectDifferences()));

                    // when the normal appearance stream was generated BBox and Matrix have been set to the
                    // values of the original /Rect. As the /Rect was changed that needs to be adjusted too.
                    annotation.getNormalAppearanceStream().setBBox(getRectangle());
                    AffineTransform transform = AffineTransform.getTranslateInstance(-getRectangle().getLowerLeftX(),
                            -getRectangle().getLowerLeftY());
                    annotation.getNormalAppearanceStream().setMatrix(transform);
                }
                else
                {
                    borderBox = applyRectDifferences(getRectangle(), rectDifferences);
                    borderBox = getPaddedRectangle(borderBox, ab.width/2);
                }
                cs.addRect(borderBox.getLowerLeftX(), borderBox.getLowerLeftY(),
                        borderBox.getWidth(), borderBox.getHeight());
            }
            cs.drawShape(ab.width, hasStroke, hasBackground);

            // rotation is an undocumented feature, but Adobe uses it. Examples can be found
            // in pdf_commenting_new.pdf file.
            int rotation = annotation.getCOSObject().getInt(COSName.ROTATE, 0);
            cs.transform(Matrix.getRotateInstance(Math.toRadians(rotation), 0, 0));
            float xOffset;
            float yOffset;
            float width = rotation == 90 || rotation == 270 ? getRectangle().getHeight(): getRectangle().getWidth();
            // somewhat inspired by AppearanceGeneratorHelper.insertGeneratedAppearance()
            cs.beginText();
            PDFont font = PDType1Font.HELVETICA;
            int factor = 1;
            if (borderEffect != null && borderEffect.getStyle().equals(PDBorderEffectDictionary.STYLE_CLOUDY))
            {
                factor = 2;
            }
            float fontSize = extractFontSize(annotation);
            switch (rotation)
            {
                case 180:
                    xOffset = - getRectangle().getUpperRightX() + fontSize / 2 * factor; 
                    yOffset = - getRectangle().getLowerLeftY() - font.getBoundingBox().getHeight() * fontSize / 1000 * factor;
                    break;
                case 90:
                    xOffset = getRectangle().getLowerLeftY() + fontSize / 2 * factor;
                    yOffset = - getRectangle().getLowerLeftX() - font.getBoundingBox().getHeight() * fontSize / 1000 * factor;
                    break;
                case 270:
                    xOffset = - getRectangle().getUpperRightY() + fontSize / 2 * factor;
                    yOffset = getRectangle().getUpperRightX() - font.getBoundingBox().getHeight() * fontSize / 1000 * factor;
                    break;
                case 0:
                default:
                    xOffset = getRectangle().getLowerLeftX() + fontSize / 2 * factor;
                    yOffset = getRectangle().getUpperRightY() - font.getBoundingBox().getHeight() * fontSize / 1000 * factor;
                    break;
            }
            cs.setFont(font, fontSize);
            cs.setNonStrokingColor(strokingColor);
            AppearanceStyle appearanceStyle = new AppearanceStyle();
            appearanceStyle.setFont(font);
            appearanceStyle.setFontSize(fontSize);
            PlainTextFormatter formatter = new PlainTextFormatter.Builder(cs)
                    .style(appearanceStyle)
                    .text(new PlainText(annotation.getContents()))
                    .width(width - fontSize / factor)
                    .wrapLines(true)
                    //TODO some reverse engineering needed to find out padding
                    .initialOffset(xOffset, yOffset)
                    // Adobe ignores the /Q
                    //.textAlign(annotation.getQ())
                    .build();
            formatter.format();
            cs.endText();


            if (pathsArray.length > 0)
            {
                PDRectangle rect = getRectangle();

                // Adjust rectangle
                // important to do this after the rectangle has been painted, because the
                // final rectangle will be bigger due to callout
                // CTAN-example-Annotations.pdf p1
                //TODO in a class structure this should be overridable
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
                // arrow length is 9 * width at about 30Â° => 10 * width seems to be enough
                rect.setLowerLeftX(Math.min(minX - ab.width * 10, rect.getLowerLeftX()));
                rect.setLowerLeftY(Math.min(minY - ab.width * 10, rect.getLowerLeftY()));
                rect.setUpperRightX(Math.max(maxX + ab.width * 10, rect.getUpperRightX()));
                rect.setUpperRightY(Math.max(maxY + ab.width * 10, rect.getUpperRightY()));
                annotation.setRectangle(rect);
                
                // need to set the BBox too, because rectangle modification came later
                annotation.getNormalAppearanceStream().setBBox(getRectangle());
            }
        }
        catch (IOException ex)
        {
            LOG.error(ex);
        }
    }

    // get the last non stroking color from the /DA entry
    private PDColor extractNonStrokingColor(PDAnnotationFreeText annotation)
    {
        // It could also work with a regular expression, but that should be written so that
        // "/LucidaConsole 13.94766 Tf .392 .585 .93 rg" does not produce "2 .585 .93 rg" as result
        // Another alternative might be to create a PDDocument and a PDPage with /DA content as /Content,
        // process the whole thing and then get the non stroking color.

        PDColor strokingColor = new PDColor(new float[]{0}, PDDeviceGray.INSTANCE);
        String defaultAppearance = annotation.getDefaultAppearance();
        if (defaultAppearance == null)
        {
            return strokingColor;
        }

        try
        {
            // not sure if charset is correct, but we only need numbers and simple characters
            PDFStreamParser parser = new PDFStreamParser(defaultAppearance.getBytes(Charsets.US_ASCII));
            COSArray arguments = new COSArray();
            COSArray colors = null;
            Operator graphicOp = null;
            for (Object token = parser.parseNextToken(); token != null; token = parser.parseNextToken())
            {
                if (token instanceof COSObject)
                {
                    arguments.add(((COSObject) token).getObject());
                }
                else if (token instanceof Operator)
                {
                    Operator op = (Operator) token;
                    String name = op.getName();
                    if ("g".equals(name) || "rg".equals(name) || "k".equals(name))
                    {
                        graphicOp = op;
                        colors = arguments;
                    }
                    arguments = new COSArray();
                }
                else
                {
                    arguments.add((COSBase) token);
                }
            }
            if (graphicOp != null)
            {
                switch (graphicOp.getName())
                {
                    case "g":
                        strokingColor = new PDColor(colors, PDDeviceGray.INSTANCE);
                        break;
                    case "rg":
                        strokingColor = new PDColor(colors, PDDeviceRGB.INSTANCE);
                        break;
                    case "k":
                        strokingColor = new PDColor(colors, PDDeviceCMYK.INSTANCE);
                        break;
                    default:
                        break;
                }
            }
        }
        catch (IOException ex)
        {
            LOG.warn("Problem parsing /DA, will use default black", ex);
        }
        return strokingColor;
    }

    //TODO extractNonStrokingColor and extractFontSize
    // might somehow be replaced with PDDefaultAppearanceString,
    // which is quite similar.
    private float extractFontSize(PDAnnotationFreeText annotation)
    {
        String defaultAppearance = annotation.getDefaultAppearance();
        if (defaultAppearance == null)
        {
            return 10;
        }

        try
        {
            // not sure if charset is correct, but we only need numbers and simple characters
            PDFStreamParser parser = new PDFStreamParser(defaultAppearance.getBytes(Charsets.US_ASCII));
            COSArray arguments = new COSArray();
            COSArray fontArguments = new COSArray();
            for (Object token = parser.parseNextToken(); token != null; token = parser.parseNextToken())
            {
                if (token instanceof COSObject)
                {
                    arguments.add(((COSObject) token).getObject());
                }
                else if (token instanceof Operator)
                {
                    Operator op = (Operator) token;
                    String name = op.getName();
                    if ("Tf".equals(name))
                    {
                        fontArguments = arguments;
                    }
                    arguments = new COSArray();
                }
                else
                {
                    arguments.add((COSBase) token);
                }
            }
            if (fontArguments.size() >= 2)
            {
                COSBase base = fontArguments.get(1);
                if (base instanceof COSNumber)
                {
                    return ((COSNumber) base).floatValue();
                }
            }
        }
        catch (IOException ex)
        {
            LOG.warn("Problem parsing /DA, will use default 10", ex);
        }
        return 10;
    }

    @Override
    public void generateRolloverAppearance()
    {
        // TODO to be implemented
    }

    @Override
    public void generateDownAppearance()
    {
        // TODO to be implemented
    }
}

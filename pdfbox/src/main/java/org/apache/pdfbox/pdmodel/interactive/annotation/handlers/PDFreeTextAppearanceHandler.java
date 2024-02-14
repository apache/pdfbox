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
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.contentstream.operator.OperatorName;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.pdfparser.PDFStreamParser;
import org.apache.pdfbox.pdmodel.PDAppearanceContentStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceCMYK;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceGray;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationFreeText;
import static org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLine.LE_NONE;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceStream;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDBorderEffectDictionary;
import org.apache.pdfbox.pdmodel.interactive.annotation.layout.AppearanceStyle;
import org.apache.pdfbox.pdmodel.interactive.annotation.layout.PlainText;
import org.apache.pdfbox.pdmodel.interactive.annotation.layout.PlainTextFormatter;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.util.Matrix;

public class PDFreeTextAppearanceHandler extends PDAbstractAppearanceHandler
{
    private static final Log LOG = LogFactory.getLog(PDFreeTextAppearanceHandler.class);

    private static final Pattern COLOR_PATTERN =
            Pattern.compile(".*color\\:\\s*\\#([0-9a-fA-F]{6}).*");

    private float fontSize = 10;
    private COSName fontName = COSName.HELV;

    public PDFreeTextAppearanceHandler(PDAnnotation annotation)
    {
        super(annotation);
    }

    public PDFreeTextAppearanceHandler(PDAnnotation annotation, PDDocument document)
    {
        super(annotation, document);
    }

    @Override
    public void generateNormalAppearance()
    {
        PDAnnotationFreeText annotation = (PDAnnotationFreeText) getAnnotation();
        float[] pathsArray;
        if (PDAnnotationFreeText.IT_FREE_TEXT_CALLOUT.equals(annotation.getIntent()))
        {
            pathsArray = annotation.getCallout();
            if (pathsArray == null || pathsArray.length != 4 && pathsArray.length != 6)
            {
                pathsArray = new float[0];
            }
        }
        else
        {
            pathsArray = new float[0];
        }
        AnnotationBorder ab = AnnotationBorder.getAnnotationBorder(annotation, annotation.getBorderStyle());

        try (PDAppearanceContentStream cs = getNormalAppearanceAsContentStream(true))
        {
            // The fill color is the /C entry, there is no /IC entry defined
            boolean hasBackground = cs.setNonStrokingColorOnDemand(annotation.getColor());
            setOpacity(cs, annotation.getConstantOpacity());

            // Adobe uses the last non stroking color from /DA as stroking color!
            // But if there is a color in /DS, then that one is used for text.
            PDColor strokingColor = extractNonStrokingColor(annotation);
            boolean hasStroke = cs.setStrokingColorOnDemand(strokingColor);
            PDColor textColor = strokingColor;
            String defaultStyleString = annotation.getDefaultStyleString();
            if (defaultStyleString != null)
            {
                Matcher m = COLOR_PATTERN.matcher(defaultStyleString);
                if (m.find())
                {
                    int color = Integer.parseInt(m.group(1), 16);
                    float r = ((color >> 16) & 0xFF) / 255f;
                    float g = ((color >> 8) & 0xFF) / 255f;
                    float b = (color & 0xFF) / 255f;
                    textColor = new PDColor( new float[] { r, g, b }, PDDeviceRGB.INSTANCE);
                }
            }

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
            if (pathsArray.length > 0)
            {
                cs.stroke();
            }

            // paint the styles here and after line(s) draw, to avoid line crossing a filled shape       
            if (PDAnnotationFreeText.IT_FREE_TEXT_CALLOUT.equals(annotation.getIntent())
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
                drawStyle(annotation.getLineEndingStyle(), cs, 0, 0, ab.width, hasStroke, hasBackground, false);
                cs.restoreGraphicsState();
            }

            PDRectangle borderBox;
            PDBorderEffectDictionary borderEffect = annotation.getBorderEffect();
            if (borderEffect != null && borderEffect.getStyle().equals(PDBorderEffectDictionary.STYLE_CLOUDY))
            {
                // Adobe draws the text with the original rectangle in mind.
                // but if there is an /RD, then writing area get smaller.
                // do this here because /RD is overwritten in a few lines
                borderBox = applyRectDifferences(getRectangle(), annotation.getRectDifferences());

                //TODO this segment was copied from square handler. Refactor?
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
                // - if /RD is not set then we don't touch /RD etc because Adobe doesn't either.
                borderBox = applyRectDifferences(getRectangle(), annotation.getRectDifferences());
                annotation.getNormalAppearanceStream().setBBox(borderBox);

                // note that borderBox is not modified
                PDRectangle paddedRectangle = getPaddedRectangle(borderBox, ab.width / 2);
                cs.addRect(paddedRectangle.getLowerLeftX(), paddedRectangle.getLowerLeftY(),
                           paddedRectangle.getWidth(), paddedRectangle.getHeight());
            }
            cs.drawShape(ab.width, hasStroke, hasBackground);

            // rotation is an undocumented feature, but Adobe uses it. Examples can be found
            // in pdf_commenting_new.pdf file, page 3.
            int rotation = annotation.getCOSObject().getInt(COSName.ROTATE, 0);
            cs.transform(Matrix.getRotateInstance(Math.toRadians(rotation), 0, 0));
            float xOffset;
            float yOffset;
            float width = rotation == 90 || rotation == 270 ? borderBox.getHeight() : borderBox.getWidth();
            // strategy to write formatted text is somewhat inspired by 
            // AppearanceGeneratorHelper.insertGeneratedAppearance()
            PDFont font = null;
            float clipY;
            float clipWidth = width - ab.width * 4;
            float clipHeight = rotation == 90 || rotation == 270 ? 
                                borderBox.getWidth() - ab.width * 4 : borderBox.getHeight() - ab.width * 4;
            extractFontDetails(annotation);
            if (document != null)
            {
                PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();
                if (acroForm != null)
                {
                    // Try to get font from AcroForm default resources
                    // Sample file: https://gitlab.freedesktop.org/poppler/poppler/issues/6
                    PDResources defaultResources = acroForm.getDefaultResources();
                    if (defaultResources != null)
                    {
                        PDFont defaultResourcesFont = defaultResources.getFont(fontName);
                        if (defaultResourcesFont != null)
                        {
                            font = defaultResourcesFont;
                        }
                    }
                }
            }
            if (font == null)
            {
                font = getDefaultFont();
            }
            // value used by Adobe, no idea where it comes from, actual font bbox max y is 0.931
            // gathered by creating an annotation with width 0.
            float yDelta = 0.7896f;
            switch (rotation)
            {
                case 180:
                    xOffset = - borderBox.getUpperRightX() + ab.width * 2;
                    yOffset = - borderBox.getLowerLeftY() - ab.width * 2 - yDelta * fontSize;
                    clipY = - borderBox.getUpperRightY() + ab.width * 2;
                    break;
                case 90:
                    xOffset = borderBox.getLowerLeftY() + ab.width * 2;
                    yOffset = - borderBox.getLowerLeftX() - ab.width * 2 - yDelta * fontSize;
                    clipY = - borderBox.getUpperRightX() + ab.width * 2;
                    break;
                case 270:
                    xOffset = - borderBox.getUpperRightY() + ab.width * 2;
                    yOffset = borderBox.getUpperRightX() - ab.width * 2 - yDelta * fontSize;
                    clipY = borderBox.getLowerLeftX() + ab.width * 2;
                    break;
                case 0:
                default:
                    xOffset = borderBox.getLowerLeftX() + ab.width * 2;
                    yOffset = borderBox.getUpperRightY() - ab.width * 2 - yDelta * fontSize;
                    clipY = borderBox.getLowerLeftY() + ab.width * 2;
                    break;
            }

            // clip writing area
            cs.addRect(xOffset, clipY, clipWidth, clipHeight);
            cs.clip();

            if (annotation.getContents() != null)
            {
                cs.beginText();
                cs.setFont(font, fontSize);
                cs.setNonStrokingColor(textColor.getComponents());
                AppearanceStyle appearanceStyle = new AppearanceStyle();
                appearanceStyle.setFont(font);
                appearanceStyle.setFontSize(fontSize);
                PlainTextFormatter formatter = new PlainTextFormatter.Builder(cs)
                        .style(appearanceStyle)
                        .text(new PlainText(annotation.getContents()))
                        .width(width - ab.width * 4)
                        .wrapLines(true)
                        .initialOffset(xOffset, yOffset)
                        // Adobe ignores the /Q
                        //.textAlign(annotation.getQ())
                        .build();
                try
                {
                    formatter.format();
                }
                catch (IllegalArgumentException ex)
                {
                    throw new IOException(ex);
                }
                finally
                {
                    cs.endText();
                }
            }

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
                // arrow length is 9 * width at about 30° => 10 * width seems to be enough
                rect.setLowerLeftX(Math.min(minX - ab.width * 10, rect.getLowerLeftX()));
                rect.setLowerLeftY(Math.min(minY - ab.width * 10, rect.getLowerLeftY()));
                rect.setUpperRightX(Math.max(maxX + ab.width * 10, rect.getUpperRightX()));
                rect.setUpperRightY(Math.max(maxY + ab.width * 10, rect.getUpperRightY()));
                annotation.setRectangle(rect);
                
                // need to set the BBox too, because rectangle modification came later
                annotation.getNormalAppearanceStream().setBBox(getRectangle());
                
                //TODO when callout is used, /RD should be so that the result is the writable part
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
            PDFStreamParser parser = new PDFStreamParser(defaultAppearance.getBytes(StandardCharsets.US_ASCII));
            COSArray arguments = new COSArray();
            COSArray colors = null;
            Operator graphicOp = null;
            for (Object token = parser.parseNextToken(); token != null; token = parser.parseNextToken())
            {
                if (token instanceof Operator)
                {
                    Operator op = (Operator) token;
                    String name = op.getName();
                    if (OperatorName.NON_STROKING_GRAY.equals(name) ||
                        OperatorName.NON_STROKING_RGB.equals(name) ||
                        OperatorName.NON_STROKING_CMYK.equals(name))
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
                    case OperatorName.NON_STROKING_GRAY:
                        strokingColor = new PDColor(colors, PDDeviceGray.INSTANCE);
                        break;
                    case OperatorName.NON_STROKING_RGB:
                        strokingColor = new PDColor(colors, PDDeviceRGB.INSTANCE);
                        break;
                    case OperatorName.NON_STROKING_CMYK:
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

    //TODO extractNonStrokingColor and extractFontDetails
    // might somehow be replaced with PDDefaultAppearanceString, which is quite similar.
    private void extractFontDetails(PDAnnotationFreeText annotation)
    {
        String defaultAppearance = annotation.getDefaultAppearance();
        if (defaultAppearance == null && document != null)
        {
            PDAcroForm pdAcroForm = document.getDocumentCatalog().getAcroForm();
            if (pdAcroForm != null)
            {
                defaultAppearance = pdAcroForm.getDefaultAppearance();
            }
        }
        if (defaultAppearance == null)
        {
            return;
        }

        try
        {
            // not sure if charset is correct, but we only need numbers and simple characters
            PDFStreamParser parser = new PDFStreamParser(defaultAppearance.getBytes(StandardCharsets.US_ASCII));
            COSArray arguments = new COSArray();
            COSArray fontArguments = new COSArray();
            for (Object token = parser.parseNextToken(); token != null; token = parser.parseNextToken())
            {
                if (token instanceof Operator)
                {
                    Operator op = (Operator) token;
                    String name = op.getName();
                    if (OperatorName.SET_FONT_AND_SIZE.equals(name))
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
                COSBase base = fontArguments.get(0);
                if (base instanceof COSName)
                {
                    fontName = (COSName) base;
                }
                base = fontArguments.get(1);
                if (base instanceof COSNumber)
                {
                    fontSize = ((COSNumber) base).floatValue();
                }
            }
        }
        catch (IOException ex)
        {
            LOG.warn("Problem parsing /DA, will use default 'Helv 10'", ex);
        }
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

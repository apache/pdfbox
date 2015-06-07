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
package org.apache.pdfbox.pdmodel.interactive.form;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdfparser.PDFStreamParser;
import org.apache.pdfbox.pdfwriter.ContentStreamWriter;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.interactive.action.PDFormFieldAdditionalActions;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceDictionary;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceEntry;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceStream;

/**
 * Create the AcroForms field appearance helper.
 * 
 * @author Stephan Gerhard
 * @author Ben Litchfield
 */
class AppearanceGeneratorHelper
{
    private static final Operator BMC = Operator.getOperator("BMC");
    private static final Operator EMC = Operator.getOperator("EMC");
    
    private final PDVariableText field;
    private final PDAppearanceString defaultAppearance;
    private String value;
    
    /**
     * Constructs a COSAppearance from the given field.
     *
     * @param field the field which you wish to control the appearance of
     * @throws IOException 
     */
    AppearanceGeneratorHelper(PDVariableText field) throws IOException
    {
        this.field = field;
        this.defaultAppearance = field.getDefaultAppearanceString();
    }
    
    /**
     * This is the public method for setting the appearance stream.
     *
     * @param apValue the String value which the appearance should represent
     * @throws IOException If there is an error creating the stream.
     */
    public void setAppearanceValue(String apValue) throws IOException
    {
        value = apValue;

        for (PDAnnotationWidget widget : field.getWidgets())
        {
            PDFormFieldAdditionalActions actions = field.getActions();

            // in case all tests fail the field will be formatted by acrobat
            // when it is opened. See FreedomExpressions.pdf for an example of this.  
            if (actions == null || actions.getF() == null ||
                widget.getCOSObject().getDictionaryObject(COSName.AP) != null)
            {
                PDAppearanceDictionary appearanceDict = widget.getAppearance();
                if (appearanceDict == null)
                {
                    appearanceDict = new PDAppearanceDictionary();
                    widget.setAppearance(appearanceDict);
                }

                PDAppearanceEntry appearance = appearanceDict.getNormalAppearance();
                // TODO support appearances other than "normal"
                
                PDAppearanceStream appearanceStream;
                if (appearance.isStream())
                {
                    appearanceStream = appearance.getAppearanceStream();
                }
                else
                {
                    appearanceStream = new PDAppearanceStream(field.getAcroForm().getDocument());
                    appearanceStream.setBBox(widget.getRectangle().createRetranslatedRectangle());
                    appearanceDict.setNormalAppearance(appearanceStream);
                    // TODO support appearances other than "normal"
                }
                
                setAppearanceContent(widget, appearanceStream);
            }
        }
    }
    
    /**
     * Parses an appearance stream into tokens.
     */
    private List<Object> tokenize(PDAppearanceStream appearanceStream) throws IOException
    {
        COSStream stream = appearanceStream.getCOSStream();
        PDFStreamParser parser = new PDFStreamParser(stream);
        parser.parse();
        return parser.getTokens();
    }

    /**
     * Constructs and sets new contents for given appearance stream.
     */
    private void setAppearanceContent(PDAnnotationWidget widget,
                                      PDAppearanceStream appearanceStream) throws IOException
    {
        // first copy any needed resources from the document’s DR dictionary into
        // the stream’s Resources dictionary
        defaultAppearance.copyNeededResourcesTo(appearanceStream);
        
        // then replace the existing contents of the appearance stream from /Tx BMC
        // to the matching EMC
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ContentStreamWriter writer = new ContentStreamWriter(output);

        List<Object> tokens = tokenize(appearanceStream);
        int bmcIndex = tokens.indexOf(Operator.getOperator("BMC"));
        if (bmcIndex == -1)
        {
            // append to existing stream
            writer.writeTokens(tokens);
            writer.writeTokens(COSName.TX, BMC);
        }
        else
        {
            // prepend content before BMC
            writer.writeTokens(tokens.subList(0, bmcIndex + 1));
        }
        
        // insert field contents
        insertGeneratedAppearance(widget, appearanceStream, output);
        
        int emcIndex = tokens.indexOf(Operator.getOperator("EMC"));
        if (emcIndex == -1)
        {
            // append EMC
            writer.writeTokens(EMC);
        }
        else
        {
            // append contents after EMC
            writer.writeTokens(tokens.subList(emcIndex, tokens.size()));
        }

        output.close();
        writeToStream(output.toByteArray(), appearanceStream);
    }
    
    /**
     * Generate and insert text content and clipping around it.   
     */
    private void insertGeneratedAppearance(PDAnnotationWidget widget,
                                           PDAppearanceStream appearanceStream,
                                           OutputStream output) throws IOException
    {
        PDPageContentStream contents = new PDPageContentStream(field.getAcroForm().getDocument(),
                                                               appearanceStream, output);
        
        // Acrobat calculates the left and right padding dependent on the offset of the border edge
        // This calculation works for forms having been generated by Acrobat.
        // The minimum distance is always 1f even if there is no rectangle being drawn around.
        float borderWidth = 0;
        if (widget.getBorderStyle() != null)
        {
            borderWidth = widget.getBorderStyle().getWidth();
        }
        PDRectangle bbox = resolveBoundingBox(widget, appearanceStream);
        PDRectangle clipRect = applyPadding(bbox, Math.max(1f, borderWidth));
        PDRectangle contentRect = applyPadding(clipRect, Math.max(1f, borderWidth));
        
        contents.saveGraphicsState();
        
        // add a clipping path to avoid overlapping with the border
        if (borderWidth > 0)
        {
            contents.addRect(clipRect.getLowerLeftX(), clipRect.getLowerLeftY(),
                             clipRect.getWidth(), clipRect.getHeight());
            contents.clip();
        }
        
        // start the text output
        contents.beginText();

        // get the font
        PDFont font = field.getDefaultAppearanceString().getFont();
        
        // calculate the fontSize (because 0 = autosize)
        float fontSize = calculateFontSize(font, contentRect);
        
        // write the /DA string
        field.getDefaultAppearanceString().writeTo(contents, fontSize);
       
        // calculate the y-position of the baseline
        float y;
        if (field instanceof PDTextField && ((PDTextField) field).isMultiline())
        {
            float height = font.getBoundingBox().getHeight() / 1000 * fontSize;
            y = contentRect.getUpperRightY() - height;
        }
        else
        {
            float minY = font.getBoundingBox().getLowerLeftY() / 1000 * fontSize;
            y = Math.max(bbox.getHeight() / 2f + minY, 0);
        }

        // show the text
        float x = contentRect.getLowerLeftX();
        PlainText textContent = new PlainText(value);
        AppearanceStyle appearanceStyle = new AppearanceStyle();
        appearanceStyle.setFont(font);
        appearanceStyle.setFontSize(fontSize);
        
        // Adobe Acrobat uses the font's bounding box for the leading between the lines
        appearanceStyle.setLeading(font.getBoundingBox().getHeight() / 1000  * fontSize);
        
        PlainTextFormatter formatter = new PlainTextFormatter
                                            .Builder(contents)
                                                .style(appearanceStyle)
                                                .text(textContent)
                                                .width(contentRect.getWidth())
                                                .wrapLines(true)
                                                .initialOffset(x, y)
                                                .textAlign(field.getQ())
                                                .build();
        formatter.format();
    
        contents.endText();
        contents.restoreGraphicsState();
        contents.close();
    }
    
    private boolean isMultiLine()
    {
        return field instanceof PDTextField && ((PDTextField) field).isMultiline();
    }

    /**
     * Writes the stream to the actual stream in the COSStream.
     *
     * @throws IOException If there is an error writing to the stream
     */
    private void writeToStream(byte[] data, PDAppearanceStream appearanceStream) throws IOException
    {
        OutputStream out = appearanceStream.getCOSStream().createUnfilteredStream();
        out.write(data);
        out.flush();
    }

    /**
     * My "not so great" method for calculating the fontsize. It does not work superb, but it
     * handles ok.
     * 
     * @return the calculated font-size
     * @throws IOException If there is an error getting the font information.
     */
    private float calculateFontSize(PDFont font, PDRectangle contentRect) throws IOException
    {
        float fontSize = defaultAppearance.getFontSize();
        
        // zero is special, it means the text is auto-sized
        if (fontSize == 0)
        {
            if (isMultiLine())
            {
                // Acrobat defaults to 12 for multiline text with size 0
                return 12f;
            }
            else
            {
                // fit width
                float width = font.getStringWidth(value) / 1000;
                float widthBasedFontSize = contentRect.getWidth() / width;

                // fit height
                float height = (font.getFontDescriptor().getAscent() +
                               -font.getFontDescriptor().getDescent()) / 1000;
                if (height <= 0)
                {
                    height = font.getBoundingBox().getHeight() / 1000;
                }
                float heightBasedFontSize = contentRect.getHeight() / height;

                return Math.min(heightBasedFontSize, widthBasedFontSize);
            }
        }
        return fontSize;
    }
    
    /**
     * Resolve the bounding box.
     * 
     * @param fieldWidget the annotation widget.
     * @param appearanceStream the annotations appearance stream.
     * @return the resolved boundingBox.
     */
    private PDRectangle resolveBoundingBox(PDAnnotationWidget fieldWidget,
                                           PDAppearanceStream appearanceStream)
    {
        PDRectangle boundingBox = appearanceStream.getBBox();
        if (boundingBox == null)
        {
            boundingBox = fieldWidget.getRectangle().createRetranslatedRectangle();
        }
        return boundingBox;
    }
    
    /**
     * Apply padding to a box.
     * 
     * @param box box
     * @return the padded box.
     */
    private PDRectangle applyPadding(PDRectangle box, float padding)
    {
        return new PDRectangle(box.getLowerLeftX() + padding, 
                               box.getLowerLeftY() + padding, 
                               box.getWidth() - 2 * padding,
                               box.getHeight() - 2 * padding);
    }
}

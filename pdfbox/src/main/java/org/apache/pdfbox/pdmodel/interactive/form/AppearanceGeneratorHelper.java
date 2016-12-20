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

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdfparser.PDFStreamParser;
import org.apache.pdfbox.pdfwriter.ContentStreamWriter;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.interactive.action.PDFormFieldAdditionalActions;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceCharacteristicsDictionary;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceContentStream;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceDictionary;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceEntry;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceStream;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDBorderStyleDictionary;
import org.apache.pdfbox.util.Matrix;

/**
 * Create the AcroForms field appearance helper.
 * 
 * @author Stephan Gerhard
 * @author Ben Litchfield
 */
class AppearanceGeneratorHelper
{
    private static final Log LOG = LogFactory.getLog(AppearanceGeneratorHelper.class);

    private static final Operator BMC = Operator.getOperator("BMC");
    private static final Operator EMC = Operator.getOperator("EMC");
 
    private final PDVariableText field;
    private final PDDefaultAppearanceString defaultAppearance;
    private String value;
    
    /**
     * The highlight color
     *
     * The color setting is used by Adobe to display the highlight box for selected entries in a list box.
     *
     * Regardless of other settings in an existing appearance stream Adobe will always use this value.
     */
    private static final int[] HIGHLIGHT_COLOR = {153,193,215};
 
    /**
     * The scaling factor for font units to PDF units
     */
    private static final int FONTSCALE = 1000;
    
    /**
     * The default font size used for multiline text
     */
    private static final float DEFAULT_FONT_SIZE = 12;    
    
    /**
     * The default padding applied by Acrobat to the fields bbox.
     */
    private static final float DEFAULT_PADDING = 0.5f;
    
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
        	
        	PDRectangle rect = widget.getRectangle();
            if (rect == null)
            {
            	widget.getCOSObject().removeItem(COSName.AP);
                LOG.warn("widget of field " + field.getFullyQualifiedName() + " has no rectangle, no appearance stream created");
                continue;
            }
        	
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
                    
                    // Calculate the entries for the bounding box and the transformation matrix
                    // settings for the appearance stream
                    int rotation = resolveRotation(widget);
                    Matrix matrix = Matrix.getRotateInstance(Math.toRadians(rotation), 0, 0);
                    Point2D.Float point2D = matrix.transformPoint(rect.getWidth(), rect.getHeight());
                    
                    PDRectangle bbox = new PDRectangle(Math.abs((float) point2D.getX()), Math.abs((float) point2D.getY()));
                    appearanceStream.setBBox(bbox);
                    
                    appearanceStream.setMatrix(calculateMatrix(bbox, rotation));
                    appearanceStream.setFormType(1);

                    appearanceStream.setResources(new PDResources());

                    appearanceDict.setNormalAppearance(appearanceStream);
                    // TODO support appearances other than "normal"
                }
                
                /*
                 * Adobe Acrobat always recreates the complete appearance stream if there is an appearance characteristics
                 * entry (the widget dictionaries MK entry). In addition if there is no content yet also create the appearance
                 * stream from the entries.
                 * 
                 */
                if (widget.getAppearanceCharacteristics() != null || appearanceStream.getContentStream().getLength() == 0)
                {
                    initializeAppearanceContent(widget, appearanceStream);
                }
                
                setAppearanceContent(widget, appearanceStream);
            }
        }
    }
    
    private int resolveRotation(PDAnnotationWidget widget)
    {
        PDAppearanceCharacteristicsDictionary  characteristicsDictionary = widget.getAppearanceCharacteristics();
        if (characteristicsDictionary != null)
        {
            // 0 is the default value if the R key doesn't exist
            return characteristicsDictionary.getRotation();
        }
        return 0;
    }

    /**
     * Initialize the content of the appearance stream.
     * 
     * Get settings like border style, border width and colors to be used to draw a rectangle and background color 
     * around the widget
     * 
     * @param widget the field widget
     * @param appearanceStream the appearance stream to be used
     * @throws IOException in case we can't write to the appearance stream
     */
    private void initializeAppearanceContent(PDAnnotationWidget widget, PDAppearanceStream appearanceStream) throws IOException
    {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PDAppearanceContentStream contents = new PDAppearanceContentStream(appearanceStream, output);
        PDAppearanceCharacteristicsDictionary appearanceCharacteristics = widget.getAppearanceCharacteristics();
        
        // TODO: support more entries like patterns, etc.
        if (appearanceCharacteristics != null)
        {
            PDColor backgroundColour = appearanceCharacteristics.getBackground();
            if (backgroundColour != null)
            {
                contents.setNonStrokingColor(backgroundColour);
                PDRectangle bbox = resolveBoundingBox(widget, appearanceStream);
                contents.addRect(bbox.getLowerLeftX(),bbox.getLowerLeftY(),bbox.getWidth(), bbox.getHeight());
                contents.fill();
            }

            float lineWidth = 0f;
            PDColor borderColour = appearanceCharacteristics.getBorderColour();
            if (borderColour != null)
            {
                contents.setStrokingColor(borderColour);
                lineWidth = 1f;
            }
            PDBorderStyleDictionary borderStyle = widget.getBorderStyle();
            if (borderStyle != null && borderStyle.getWidth() > 0)
            {
                lineWidth = borderStyle.getWidth();
            }

            if (lineWidth > 0 && borderColour != null)
            {
                if (lineWidth != 1)
                {
                    contents.setLineWidth(lineWidth);
                }
                PDRectangle bbox = resolveBoundingBox(widget, appearanceStream);
                PDRectangle clipRect = applyPadding(bbox, Math.max(DEFAULT_PADDING, lineWidth/2)); 
                contents.addRect(clipRect.getLowerLeftX(),clipRect.getLowerLeftY(),clipRect.getWidth(), clipRect.getHeight());
                contents.closeAndStroke();
            }
        }
        
        contents.close();
        output.close();
        writeToStream(output.toByteArray(), appearanceStream);
    }
    
    /**
     * Parses an appearance stream into tokens.
     */
    private List<Object> tokenize(PDAppearanceStream appearanceStream) throws IOException
    {
        PDFStreamParser parser = new PDFStreamParser(appearanceStream);
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
        int bmcIndex = tokens.indexOf(BMC);
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
        
        int emcIndex = tokens.indexOf(EMC);
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
        PDAppearanceContentStream contents = new PDAppearanceContentStream(appearanceStream, output);
        
        PDRectangle bbox = resolveBoundingBox(widget, appearanceStream);
        
        // Acrobat calculates the left and right padding dependent on the offset of the border edge
        // This calculation works for forms having been generated by Acrobat.
        // The minimum distance is always 1f even if there is no rectangle being drawn around.
        float borderWidth = 0;
        if (widget.getBorderStyle() != null)
        {
            borderWidth = widget.getBorderStyle().getWidth();
        }
        PDRectangle clipRect = applyPadding(bbox, Math.max(1f, borderWidth));
        PDRectangle contentRect = applyPadding(clipRect, Math.max(1f, borderWidth));
        
        contents.saveGraphicsState();
        
        // Acrobat always adds a clipping path
        contents.addRect(clipRect.getLowerLeftX(), clipRect.getLowerLeftY(),
                clipRect.getWidth(), clipRect.getHeight());
        contents.clip();
        
        // get the font
        PDFont font = field.getDefaultAppearanceString().getFont();
        
        // calculate the fontSize (because 0 = autosize)
        float fontSize = calculateFontSize(font, contentRect);
        
        // for a listbox generate the highlight rectangle for the selected
        // options
        if (field instanceof PDListBox)
        {
            insertGeneratedSelectionHighlight(contents, appearanceStream, font, fontSize);
        }
        
        // start the text output
        contents.beginText();

        // write the /DA string
        field.getDefaultAppearanceString().writeTo(contents, fontSize);
       
        // calculate the y-position of the baseline
        float y;
        
        // calculate font metrics at font size
        float fontScaleY = fontSize / FONTSCALE;
        float fontBoundingBoxAtSize = font.getBoundingBox().getHeight() * fontScaleY;
        float fontCapAtSize = font.getFontDescriptor().getCapHeight() * fontScaleY;
        float fontDescentAtSize = font.getFontDescriptor().getDescent() * fontScaleY;
        
        if (field instanceof PDTextField && ((PDTextField) field).isMultiline())
        {
            y = contentRect.getUpperRightY() - fontBoundingBoxAtSize;
        }
        else
        {
            // Adobe shows the text 'shiftet up' in case the caps don't fit into the clipping area
            if (fontCapAtSize > clipRect.getHeight())
            {
                y = clipRect.getLowerLeftY() + -fontDescentAtSize;
            }
            else
            {
                // calculate the position based on the content rectangle
                y = clipRect.getLowerLeftY() + (clipRect.getHeight() - fontCapAtSize) / 2;
    
                // check to ensure that ascents and descents fit
                if (y - clipRect.getLowerLeftY() < -fontDescentAtSize) {
    
                    float fontDescentBased = -fontDescentAtSize + contentRect.getLowerLeftY();
                    float fontCapBased = contentRect.getHeight() - contentRect.getLowerLeftY() - fontCapAtSize;
    
                    y = Math.min(fontDescentBased, Math.max(y, fontCapBased));
                }
            }
        }
        
        // show the text
        float x = contentRect.getLowerLeftX();
        
        // special handling for comb boxes as these are like table cells with individual
        // chars
        if (shallComb()) {
            insertGeneratedCombAppearance(contents, appearanceStream, font, fontSize);
        }
        else if (field instanceof PDListBox)
        {
            insertGeneratedListboxAppearance(contents, appearanceStream, contentRect, font, fontSize);
        }
        else
        {         
            PlainText textContent = new PlainText(value);
            AppearanceStyle appearanceStyle = new AppearanceStyle();
            appearanceStyle.setFont(font);
            appearanceStyle.setFontSize(fontSize);
            
            // Adobe Acrobat uses the font's bounding box for the leading between the lines
            appearanceStyle.setLeading(font.getBoundingBox().getHeight() * fontScaleY);
            
            PlainTextFormatter formatter = new PlainTextFormatter
                                                .Builder(contents)
                                                    .style(appearanceStyle)
                                                    .text(textContent)
                                                    .width(contentRect.getWidth())
                                                    .wrapLines(isMultiLine())
                                                    .initialOffset(x, y)
                                                    .textAlign(field.getQ())
                                                    .build();
            formatter.format();
        }
    
        contents.endText();
        contents.restoreGraphicsState();
        contents.close();
    }
    
    private AffineTransform calculateMatrix(PDRectangle bbox, int rotation)
    {
        if (rotation == 0)
        {
            return new AffineTransform();
        }
        else
        {
            float tx=0, ty=0;

            if (rotation == 90)
            {
                tx = bbox.getUpperRightY();
            }
            else if (rotation == 180)
            {
                tx = bbox.getUpperRightY();
                ty = bbox.getUpperRightX();
            }
            else if (rotation == 270)
            {
                ty = bbox.getUpperRightX();
            }

            Matrix matrix = Matrix.getRotateInstance(Math.toRadians(rotation), tx, ty);
            return matrix.createAffineTransform();
        }
    }

    private boolean isMultiLine()
    {
        return field instanceof PDTextField && ((PDTextField) field).isMultiline();
    }
    
    /**
     * Determine if the appearance shall provide a comb output.
     * 
     * <p>
     * May be set only if the MaxLen entry is present in the text field dictionary
     * and if the Multiline, Password, and FileSelect flags are clear.
     * If set, the field shall be automatically divided into as many equally spaced positions,
     * or combs, as the value of MaxLen, and the text is laid out into those combs.
     * </p>
     * 
     * @return the comb state
     */
    private boolean shallComb()
    {
        return field instanceof PDTextField &&
                ((PDTextField) field).isComb() &&
                !((PDTextField) field).isMultiline() &&
                !((PDTextField) field).isPassword() &&
                !((PDTextField) field).isFileSelect();           
    }
    
    /**
     * Generate the appearance for comb fields.
     * 
     * @param contents the content stream to write to
     * @param appearanceStream the appearance stream used
     * @param font the font to be used
     * @param fontSize the font size to be used
     * @throws IOException
     */
    private void insertGeneratedCombAppearance(PDAppearanceContentStream contents, PDAppearanceStream appearanceStream,
            PDFont font, float fontSize) throws IOException
    {
        
        // TODO:    Currently the quadding is not taken into account
        //          so the comb is always filled from left to right.
        
        int maxLen = ((PDTextField) field).getMaxLen();
        int numChars = Math.min(value.length(), maxLen);
        
        PDRectangle paddingEdge = applyPadding(appearanceStream.getBBox(), 1);
        
        float combWidth = appearanceStream.getBBox().getWidth() / maxLen;
        float ascentAtFontSize = font.getFontDescriptor().getAscent() / FONTSCALE * fontSize;
        float baselineOffset = paddingEdge.getLowerLeftY() +  
                (appearanceStream.getBBox().getHeight() - ascentAtFontSize)/2;
        
        float prevCharWidth = 0f;
        float currCharWidth = 0f;
        
        float xOffset =  combWidth/2;

        String combString = "";
        
        for (int i = 0; i < numChars; i++) 
        {
            combString = value.substring(i, i+1);
            currCharWidth = font.getStringWidth(combString) / FONTSCALE * fontSize/2;
            
            xOffset = xOffset + prevCharWidth/2 - currCharWidth/2;
            
            contents.newLineAtOffset(xOffset, baselineOffset);
            contents.showText(combString);
            
            baselineOffset = 0;
            prevCharWidth = currCharWidth;
            xOffset = combWidth;
        }
    }
    
    private void insertGeneratedSelectionHighlight(PDAppearanceContentStream contents, PDAppearanceStream appearanceStream,
            PDFont font, float fontSize) throws IOException
    {
        List<Integer> indexEntries = ((PDListBox) field).getSelectedOptionsIndex();
        List<String> values = ((PDListBox) field).getValue();
        List<String> options = ((PDListBox) field).getOptionsExportValues();
        
        // TODO: support highlighting multiple items if multiselect is set
        
        int selectedIndex = 0;
        
        if (!values.isEmpty() && !options.isEmpty())
        {
            if (!indexEntries.isEmpty())
            {
                selectedIndex = indexEntries.get(0);
            }
            else
            {
                selectedIndex = options.indexOf(values.get(0));
            }
        }
        
        // The first entry which shall be presented might be adjusted by the optional TI key
        // If this entry is present the first entry to be displayed is the keys value otherwise
        // display starts with the first entry in Opt.
        int topIndex = ((PDListBox) field).getTopIndex();
        
        float highlightBoxHeight = font.getBoundingBox().getHeight() * fontSize / FONTSCALE - 2f;
        
        // the padding area 
        PDRectangle paddingEdge = applyPadding(appearanceStream.getBBox(), 1);
        
        contents.setNonStrokingColor(HIGHLIGHT_COLOR[0],HIGHLIGHT_COLOR[1],HIGHLIGHT_COLOR[2]);
        
        contents.addRect(paddingEdge.getLowerLeftX(), 
                paddingEdge.getUpperRightY() - highlightBoxHeight * (selectedIndex - topIndex + 1),
                paddingEdge.getWidth(),
                highlightBoxHeight);
        contents.fill();
        contents.setNonStrokingColor(0);
    }
    
    
    private void insertGeneratedListboxAppearance(PDAppearanceContentStream contents, PDAppearanceStream appearanceStream,
            PDRectangle contentRect, PDFont font, float fontSize) throws IOException
    {
        contents.setNonStrokingColor(0);
        
        int q = field.getQ();

        if (q == PDVariableText.QUADDING_CENTERED || q == PDVariableText.QUADDING_RIGHT)
        {
            float fieldWidth = appearanceStream.getBBox().getWidth();
            float stringWidth = (font.getStringWidth(value) / FONTSCALE) * fontSize;
            float adjustAmount = fieldWidth - stringWidth - 4;

            if (q == PDVariableText.QUADDING_CENTERED)
            {
                adjustAmount = adjustAmount / 2.0f;
            }

            contents.newLineAtOffset(adjustAmount, 0);
        }
        else if (q != PDVariableText.QUADDING_LEFT)
        {
            throw new IOException("Error: Unknown justification value:" + q);
        }

        List<String> options = ((PDListBox) field).getOptionsDisplayValues();
        int numOptions = options.size();

        float yTextPos = contentRect.getUpperRightY();

        int topIndex = ((PDListBox) field).getTopIndex();
        
        for (int i = topIndex; i < numOptions; i++)
        {
           
            if (i == topIndex)
            {
                yTextPos = yTextPos - font.getFontDescriptor().getAscent() / FONTSCALE * fontSize;
            }
            else
            {
                yTextPos = yTextPos - font.getBoundingBox().getHeight() / FONTSCALE * fontSize;
                contents.beginText();
            }

            contents.newLineAtOffset(contentRect.getLowerLeftX(), yTextPos);
            contents.showText(options.get(i));

            if (i - topIndex != (numOptions - 1))
            {
                contents.endText();
            }
        }
    }
    
    /**
     * Writes the stream to the actual stream in the COSStream.
     *
     * @throws IOException If there is an error writing to the stream
     */
    private void writeToStream(byte[] data, PDAppearanceStream appearanceStream) throws IOException
    {
        OutputStream out = appearanceStream.getCOSObject().createOutputStream();
        out.write(data);
        out.close();
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
                return DEFAULT_FONT_SIZE;
            }
            else
            {
                float yScalingFactor = FONTSCALE * font.getFontMatrix().getScaleY();
                float xScalingFactor = FONTSCALE * font.getFontMatrix().getScaleX();
                
                // fit width
                float width = font.getStringWidth(value) * font.getFontMatrix().getScaleX();
                float widthBasedFontSize = contentRect.getWidth() / width * xScalingFactor;

                // fit height
                float height = (font.getFontDescriptor().getCapHeight() +
                               -font.getFontDescriptor().getDescent()) * font.getFontMatrix().getScaleY();
                if (height <= 0)
                {
                    height = font.getBoundingBox().getHeight() * font.getFontMatrix().getScaleY();
                }

                float heightBasedFontSize = contentRect.getHeight() / height * yScalingFactor;
                
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
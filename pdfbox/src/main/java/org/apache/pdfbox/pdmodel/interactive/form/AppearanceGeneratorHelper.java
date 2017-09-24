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
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSString;
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
    
    private PDDefaultAppearanceString defaultAppearance;
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
        validateAndEnsureAcroFormResources();
        
        this.defaultAppearance = field.getDefaultAppearanceString();
    }
    
    /*
     * Adobe Reader/Acrobat are adding resources which are at the field/widget level
     * to the AcroForm level. 
     */
    private void validateAndEnsureAcroFormResources() {
        // add font resources which might be available at the field 
        // level but are not at the AcroForm level to the AcroForm
        // to match Adobe Reader/Acrobat behavior        
        if (field.getAcroForm().getDefaultResources() == null)
        {
            return;
        }
        
        PDResources acroFormResources = field.getAcroForm().getDefaultResources();
        
        for (PDAnnotationWidget widget : field.getWidgets())
        {
            if (widget.getNormalAppearanceStream() != null && widget.getNormalAppearanceStream().getResources() != null)
            {
                PDResources widgetResources = widget.getNormalAppearanceStream().getResources();
                for (COSName fontResourceName : widgetResources.getFontNames())
                {
                    try
                    {
                        if (acroFormResources.getFont(fontResourceName) == null)
                        {
                            LOG.debug("Adding font resource " + fontResourceName + " from widget to AcroForm");
                            acroFormResources.put(fontResourceName, widgetResources.getFont(fontResourceName));
                        }
                    }
                    catch (IOException e)
                    {
                        LOG.warn("Unable to match field level font with AcroForm font");
                    }
                }
            }
        }
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

        // Treat multiline field values in single lines as single lime values.
        // This is in line with how Adobe Reader behaves when enetring text
        // interactively but NOT how it behaves when the field value has been
        // set programmatically and Reader is forced to generate the appearance
        // using PDAcroForm.setNeedAppearances
        // see PDFBOX-3911
        if (field instanceof PDTextField && !((PDTextField) field).isMultiline())
        {
            value = apValue.replaceAll("\\u000D\\u000A|[\\u000A\\u000B\\u000C\\u000D\\u0085\\u2028\\u2029]", " ");
        }

        for (PDAnnotationWidget widget : field.getWidgets())
        {
            // some fields have the /Da at the widget level if the 
            // widgets differ in layout.
            PDDefaultAppearanceString acroFormAppearance = defaultAppearance;
            
            if (widget.getCOSObject().getDictionaryObject(COSName.DA) != null)
            {
                defaultAppearance = getWidgetDefaultAppearanceString(widget);
            }

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
                if (appearance != null && appearance.isStream())
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
            
            // restore the field level appearance
            defaultAppearance =  acroFormAppearance;
        }
    }
    
    private PDDefaultAppearanceString getWidgetDefaultAppearanceString(PDAnnotationWidget widget) throws IOException
    {
        COSString da = (COSString) widget.getCOSObject().getDictionaryObject(COSName.DA);
        PDResources dr = field.getAcroForm().getDefaultResources();
        return new PDDefaultAppearanceString(da, dr);
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
        try (PDAppearanceContentStream contents = new PDAppearanceContentStream(appearanceStream, output))
        {
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
            PDFont font = defaultAppearance.getFont();
            
            // calculate the fontSize (because 0 = autosize)
            float fontSize = defaultAppearance.getFontSize();
            
            if (fontSize == 0)
            {
                fontSize = calculateFontSize(font, contentRect);
            }
            
            // for a listbox generate the highlight rectangle for the selected
            // options
            if (field instanceof PDListBox)
            {
                insertGeneratedSelectionHighlight(contents, appearanceStream, font, fontSize);
            }
            
            // start the text output
            contents.beginText();
            
            // write the /DA string
            defaultAppearance.writeTo(contents, fontSize);
            
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
            if (shallComb())
            {
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
        }
    }
    
    private AffineTransform calculateMatrix(PDRectangle bbox, int rotation)
    {
        if (rotation == 0)
        {
            return new AffineTransform();
        }
        float tx = 0, ty = 0;
        switch (rotation)
        {
            case 90:
                tx = bbox.getUpperRightY();
                break;
            case 180:
                tx = bbox.getUpperRightY();
                ty = bbox.getUpperRightX();
                break;
            case 270:
                ty = bbox.getUpperRightX();
                break;
            default:
                break;
        }
        Matrix matrix = Matrix.getRotateInstance(Math.toRadians(rotation), tx, ty);
        return matrix.createAffineTransform();
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
        
        float xOffset = combWidth / 2;

        for (int i = 0; i < numChars; i++) 
        {
            String combString = value.substring(i, i+1);
            float currCharWidth = font.getStringWidth(combString) / FONTSCALE * fontSize/2;
            
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
        
        if (!values.isEmpty() && !options.isEmpty() && indexEntries.isEmpty())
        {
            // create indexEntries from options
            indexEntries = new ArrayList<>();
            for (String v : values)
            {
                indexEntries.add(options.indexOf(v));
            }
        }

        // The first entry which shall be presented might be adjusted by the optional TI key
        // If this entry is present the first entry to be displayed is the keys value otherwise
        // display starts with the first entry in Opt.
        int topIndex = ((PDListBox) field).getTopIndex();
        
        float highlightBoxHeight = font.getBoundingBox().getHeight() * fontSize / FONTSCALE;       

        // the padding area 
        PDRectangle paddingEdge = applyPadding(appearanceStream.getBBox(), 1);

        for (int selectedIndex : indexEntries)
        {
            contents.setNonStrokingColor(HIGHLIGHT_COLOR[0], HIGHLIGHT_COLOR[1], HIGHLIGHT_COLOR[2]);

            contents.addRect(paddingEdge.getLowerLeftX(),
                    paddingEdge.getUpperRightY() - highlightBoxHeight * (selectedIndex - topIndex + 1) + 2,
                    paddingEdge.getWidth(),
                    highlightBoxHeight);
            contents.fill();
        }
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
        try (OutputStream out = appearanceStream.getCOSObject().createOutputStream())
        {
            out.write(data);
        }
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

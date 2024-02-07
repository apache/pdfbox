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
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fontbox.util.BoundingBox;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.pdfparser.PDFStreamParser;
import org.apache.pdfbox.pdfwriter.ContentStreamWriter;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDSimpleFont;
import org.apache.pdfbox.pdmodel.font.PDType3CharProc;
import org.apache.pdfbox.pdmodel.font.PDType3Font;
import org.apache.pdfbox.pdmodel.font.PDVectorFont;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.interactive.action.PDAction;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionJavaScript;
import org.apache.pdfbox.pdmodel.interactive.action.PDFormFieldAdditionalActions;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceCharacteristicsDictionary;
import org.apache.pdfbox.pdmodel.PDAppearanceContentStream;
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
    private static final float[] HIGHLIGHT_COLOR = {153/255f, 193/255f, 215/255f};
 
    /**
     * The scaling factor for font units to PDF units
     */
    private static final int FONTSCALE = 1000;
    
    /**
     * The default font size used for multiline text
     */
    private static final float DEFAULT_FONT_SIZE = 12;

    /**
     * The minimum/maximum font sizes used for multiline text auto sizing
     */
    private static final float MINIMUM_FONT_SIZE = 4;
    private static final float MAXIMUM_FONT_SIZE = 300;
    
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
        
        try
        {
            this.defaultAppearance = field.getDefaultAppearanceString();
        }
        catch (IOException ex)
        {
            throw new IOException("Could not process default appearance string '" +
                                   field.getDefaultAppearance() + "' for field '" +
                                   field.getFullyQualifiedName() + "': " + ex.getMessage(), ex);
        }
    }
    
    /*
     * Adobe Reader/Acrobat are adding resources which are at the field/widget level
     * to the AcroForm level. 
     */
    private void validateAndEnsureAcroFormResources()
    {
        // add font resources which might be available at the field 
        // level but are not at the AcroForm level to the AcroForm
        // to match Adobe Reader/Acrobat behavior        
        PDResources acroFormResources = field.getAcroForm().getDefaultResources();
        if (acroFormResources == null)
        {
            return;
        }
        
        for (PDAnnotationWidget widget : field.getWidgets())
        {
            PDAppearanceStream stream = widget.getNormalAppearanceStream();
            if (stream == null)
            {
                continue;
            }
            PDResources widgetResources = stream.getResources();
            if (widgetResources == null)
            {
                continue;
            }
            COSDictionary widgetFontDict = widgetResources.getCOSObject()
                    .getCOSDictionary(COSName.FONT);
            COSDictionary acroFormFontDict = acroFormResources.getCOSObject()
                    .getCOSDictionary(COSName.FONT);
            for (COSName fontResourceName : widgetResources.getFontNames())
            {
                try
                {
                    if (acroFormResources.getFont(fontResourceName) == null)
                    {
                        LOG.debug("Adding font resource " + fontResourceName + " from widget to AcroForm");
                        // use the COS-object to preserve a possible indirect object reference
                        acroFormFontDict.setItem(fontResourceName,
                                widgetFontDict.getItem(fontResourceName));
                    }
                }
                catch (IOException e)
                {
                    LOG.warn("Unable to match field level font with AcroForm font", e);
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
        value = getFormattedValue(apValue);

        // Treat multiline field values in single lines as single lime values.
        // This is in line with how Adobe Reader behaves when entering text
        // interactively but NOT how it behaves when the field value has been
        // set programmatically and Reader is forced to generate the appearance
        // using PDAcroForm.setNeedAppearances
        // see PDFBOX-3911
        if (field instanceof PDTextField && !((PDTextField) field).isMultiline())
        {
            value = value.replaceAll("\\u000D\\u000A|[\\u000A\\u000B\\u000C\\u000D\\u0085\\u2028\\u2029]", " ");
        }

        for (PDAnnotationWidget widget : field.getWidgets())
        {
            if (widget.getCOSObject().containsKey("PMD"))
            {
                LOG.warn("widget of field " + field.getFullyQualifiedName() + " is a PaperMetaData widget, no appearance stream created");
                continue;
            }

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

            PDAppearanceDictionary appearanceDict = widget.getAppearance();
            if (appearanceDict == null)
            {
                appearanceDict = new PDAppearanceDictionary();
                widget.setAppearance(appearanceDict);
            }

            PDAppearanceEntry appearance = appearanceDict.getNormalAppearance();
            // TODO support appearances other than "normal"
                
            PDAppearanceStream appearanceStream;
            if (isValidAppearanceStream(appearance))
            {
                appearanceStream = appearance.getAppearanceStream();
            }
            else
            {
                appearanceStream = prepareNormalAppearanceStream(widget);
                appearanceDict.setNormalAppearance(appearanceStream);
                // TODO support appearances other than "normal"
            }
            PDAppearanceCharacteristicsDictionary appearanceCharacteristics =
                    widget.getAppearanceCharacteristics();
                
            /*
             * Adobe Acrobat always recreates the complete appearance stream if there is an appearance characteristics
             * entry (the widget dictionaries MK entry). In addition if there is no content yet also create the appearance
             * stream from the entries.
             * 
             */
            if (appearanceCharacteristics != null || appearanceStream.getContentStream().getLength() == 0)
            {
                initializeAppearanceContent(widget, appearanceCharacteristics, appearanceStream);
            }
                
            setAppearanceContent(widget, appearanceStream);
            
            
            // restore the field level appearance
            defaultAppearance =  acroFormAppearance;
        }
    }

    private String getFormattedValue(String apValue)
    {
        // format the field value for the appearance if there is scripting support and the field
        // has a format event
        PDFormFieldAdditionalActions actions = field.getActions();
        if (actions == null)
        {
            return apValue;
        }
        PDAction actionF = actions.getF();
        if (actionF != null)
        {
            if (field.getAcroForm().getScriptingHandler() != null)
            {
                ScriptingHandler scriptingHandler = field.getAcroForm().getScriptingHandler();
                return scriptingHandler.format((PDActionJavaScript) actionF, apValue);
            }
            LOG.info("Field contains a formatting action but no ScriptingHandler " +
                     "has been supplied - formatted value might be incorrect");
        }
        return apValue;
    }

    private static boolean isValidAppearanceStream(PDAppearanceEntry appearance)
    {
        if (appearance == null)
        {
            return false;
        }
        if (!appearance.isStream())
        {
            return false;
        }
        PDRectangle bbox = appearance.getAppearanceStream().getBBox();
        if (bbox == null)
        {
            return false;
        }
        return Math.abs(bbox.getWidth()) > 0 && Math.abs(bbox.getHeight()) > 0;
    }

    private PDAppearanceStream prepareNormalAppearanceStream(PDAnnotationWidget widget)
    {
        PDAppearanceStream appearanceStream = new PDAppearanceStream(field.getAcroForm().getDocument());

        // Calculate the entries for the bounding box and the transformation matrix
        // settings for the appearance stream
        int rotation = resolveRotation(widget);
        PDRectangle rect = widget.getRectangle();
        Matrix matrix = Matrix.getRotateInstance(Math.toRadians(rotation), 0, 0);
        Point2D.Float point2D = matrix.transformPoint(rect.getWidth(), rect.getHeight());

        PDRectangle bbox = new PDRectangle(Math.abs((float) point2D.getX()), Math.abs((float) point2D.getY()));
        appearanceStream.setBBox(bbox);

        AffineTransform at = calculateMatrix(bbox, rotation);
        if (!at.isIdentity())
        {
            appearanceStream.setMatrix(at);
        }
        appearanceStream.setFormType(1);
        appearanceStream.setResources(new PDResources());
        return appearanceStream;
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
     * @param appearanceCharacteristics the appearance characteristics dictionary from the widget or
     * null
     * @param appearanceStream the appearance stream to be used
     * @throws IOException in case we can't write to the appearance stream
     */
    private void initializeAppearanceContent(PDAnnotationWidget widget,
            PDAppearanceCharacteristicsDictionary appearanceCharacteristics,
            PDAppearanceStream appearanceStream) throws IOException
    {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream();
             PDAppearanceContentStream contents = new PDAppearanceContentStream(appearanceStream, output))
        {
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
                    if (Float.compare(lineWidth, 1) != 0)
                    {
                        contents.setLineWidth(lineWidth);
                    }
                    PDRectangle bbox = resolveBoundingBox(widget, appearanceStream);
                    PDRectangle clipRect = applyPadding(bbox, Math.max(DEFAULT_PADDING, lineWidth/2)); 
                    contents.addRect(clipRect.getLowerLeftX(),clipRect.getLowerLeftY(),clipRect.getWidth(), clipRect.getHeight());
                    contents.closeAndStroke();
                }
            }
            
            writeToStream(output.toByteArray(), appearanceStream);

        }
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
        try (ByteArrayOutputStream output = new ByteArrayOutputStream())
        {
            ContentStreamWriter writer = new ContentStreamWriter(output);
            
            List<Object> tokens = new PDFStreamParser(appearanceStream).parse();
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
            writeToStream(output.toByteArray(), appearanceStream);
        }
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
            float padding = Math.max(1f, borderWidth);
            PDRectangle clipRect = applyPadding(bbox, padding);
            PDRectangle contentRect = applyPadding(clipRect, padding);
            
            contents.saveGraphicsState();
            
            // Acrobat always adds a clipping path
            contents.addRect(clipRect.getLowerLeftX(), clipRect.getLowerLeftY(),
                    clipRect.getWidth(), clipRect.getHeight());
            contents.clip();
            
            // get the font
            PDFont font = defaultAppearance.getFont();
            if (font == null)
            {
                throw new IllegalArgumentException("font is null, check whether /DA entry is incomplete or incorrect");
            }
            if (font.getName().contains("+"))
            {
                LOG.warn("Font '" + defaultAppearance.getFontName().getName() +
                         "' of field '" + field.getFullyQualifiedName() + 
                         "' contains subsetted font '" + font.getName() + "'");
                LOG.warn("This may bring trouble with PDField.setValue(), PDAcroForm.flatten() or " +
                         "PDAcroForm.refreshAppearances()");
                LOG.warn("You should replace this font with a non-subsetted font:");
                LOG.warn("PDFont font = PDType0Font.load(doc, new FileInputStream(fontfile), false);");
                LOG.warn("acroForm.getDefaultResources().put(COSName.getPDFName(\"" +
                         defaultAppearance.getFontName().getName() + "\", font);");
            }
            // calculate the fontSize (because 0 = autosize)
            float fontSize = defaultAppearance.getFontSize();
            
            if (Float.compare(fontSize, 0) == 0)
            {
                fontSize = calculateFontSize(font, contentRect);
            }
            
            // for a listbox generate the highlight rectangle for the selected
            // options
            if (field instanceof PDListBox)
            {
                insertGeneratedListboxSelectionHighlight(contents, appearanceStream, font, fontSize);
            }
            
            // start the text output
            contents.beginText();

            // write font and color from the /DA string, with the calculated font size
            defaultAppearance.writeTo(contents, fontSize);

            // calculate the y-position of the baseline
            float y;
            
            // calculate font metrics at font size
            float fontScaleY = fontSize / FONTSCALE;
            float fontBoundingBoxAtSize = font.getBoundingBox().getHeight() * fontScaleY;

            float fontCapAtSize;
            float fontDescentAtSize;
    
            if (font.getFontDescriptor() != null) {
                fontCapAtSize = font.getFontDescriptor().getCapHeight() * fontScaleY;
                fontDescentAtSize = font.getFontDescriptor().getDescent() * fontScaleY;
            } else {
                float fontCapHeight = resolveCapHeight(font);
                float fontDescent = resolveDescent(font);
                LOG.debug("missing font descriptor - resolved Cap/Descent to " + fontCapHeight + "/" + fontDescent);
                fontCapAtSize = fontCapHeight * fontScaleY;
                fontDescentAtSize = fontDescent * fontScaleY;
            }
            
            if (field instanceof PDTextField && ((PDTextField) field).isMultiline())
            {
                y = contentRect.getUpperRightY() - fontBoundingBoxAtSize;
            }
            else
            {
                // Adobe shows the text 'shifted up' in case the caps don't fit into the clipping area
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
                        .textAlign(getTextAlign(widget))
                        .build();
                formatter.format();
            }
            
            contents.endText();
            contents.restoreGraphicsState();
        }
    }
    
    /*
     * PDFBox handles a widget with a joined in field dictionary and without
     * an individual name as a widget only. As a result - as a widget can't have a
     * quadding /Q entry we need to do a low level access to the dictionary and
     * otherwise get the quadding from the field.
     */
    private int getTextAlign(PDAnnotationWidget widget)
    {
        // Use quadding value from joined field/widget if set, else use from field.
        return widget.getCOSObject().getInt(COSName.Q, field.getQ());
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
                ((PDTextField) field).getMaxLen() != -1 &&
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
        int maxLen = ((PDTextField) field).getMaxLen();
        int quadding = field.getQ();
        int numChars = Math.min(value.length(), maxLen);
        
        PDRectangle paddingEdge = applyPadding(appearanceStream.getBBox(), 1);
        
        float combWidth = appearanceStream.getBBox().getWidth() / maxLen;
        float ascentAtFontSize = font.getFontDescriptor().getAscent() / FONTSCALE * fontSize;
        float baselineOffset = paddingEdge.getLowerLeftY() +  
                (appearanceStream.getBBox().getHeight() - ascentAtFontSize)/2;
        
        float prevCharWidth = 0f;
        
        float xOffset = combWidth / 2;

        // add to initial offset if right aligned or centered
        if (quadding == 2)
        {
            xOffset = xOffset + (maxLen - numChars) * combWidth;
        }
        else if (quadding == 1)
        {
            xOffset = xOffset + Math.floorDiv(maxLen - numChars, 2) * combWidth;
        }

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
    
    private void insertGeneratedListboxSelectionHighlight(PDAppearanceContentStream contents, PDAppearanceStream appearanceStream,
            PDFont font, float fontSize) throws IOException
    {
        PDListBox listBox = (PDListBox) field;
        List<Integer> indexEntries = listBox.getSelectedOptionsIndex();
        List<String> values = listBox.getValue();
        List<String> options = listBox.getOptionsExportValues();

        if (!values.isEmpty() && !options.isEmpty() && indexEntries.isEmpty())
        {
            // create indexEntries from options
            indexEntries = new ArrayList<>(values.size());
            for (String v : values)
            {
                indexEntries.add(options.indexOf(v));
            }
        }

        // The first entry which shall be presented might be adjusted by the optional TI key
        // If this entry is present, the first entry to be displayed is the keys value,
        // otherwise display starts with the first entry in Opt.
        int topIndex = listBox.getTopIndex();
        
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
        contents.setNonStrokingColor(0f);
    }
    
    
    private void insertGeneratedListboxAppearance(PDAppearanceContentStream contents, PDAppearanceStream appearanceStream,
            PDRectangle contentRect, PDFont font, float fontSize) throws IOException
    {
        contents.setNonStrokingColor(0f);
        
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
        float ascent = font.getFontDescriptor().getAscent();
        float height = font.getBoundingBox().getHeight();
        
        for (int i = topIndex; i < numOptions; i++)
        {
            if (i == topIndex)
            {
                yTextPos = yTextPos - ascent / FONTSCALE * fontSize;
            }
            else
            {
                yTextPos = yTextPos - height / FONTSCALE * fontSize;
                contents.beginText();
            }

            contents.newLineAtOffset(contentRect.getLowerLeftX(), yTextPos);
            contents.showText(options.get(i));

            if (i != (numOptions - 1))
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
        if (Float.compare(fontSize, 0) == 0)
        {
            if (isMultiLine())
            {
                PlainText textContent = new PlainText(value);
                if (textContent.getParagraphs() != null)
                {
                    float width = contentRect.getWidth() - contentRect.getLowerLeftX();
                    float fs = MINIMUM_FONT_SIZE;
                    while (fs <= DEFAULT_FONT_SIZE)
                    {
                        // determine the number of lines needed for this font and contentRect
                        int numLines = 0;
                        for (PlainText.Paragraph paragraph : textContent.getParagraphs())
                        {
                            numLines += paragraph.getLines(font, fs, width).size();
                        }
                        // calculate the height required for this font size
                        float fontScaleY = fs / FONTSCALE;
                        float leading = font.getBoundingBox().getHeight() * fontScaleY;
                        float height = leading * numLines;

                        // if this font size didn't fit, use the prior size that did fit
                        if (height > contentRect.getHeight())
                        {
                            return Math.max(fs - 1, MINIMUM_FONT_SIZE);
                        }
                        fs++;
                    }
                    return Math.min(fs, DEFAULT_FONT_SIZE);
                }
                
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
                if (Float.isInfinite(widthBasedFontSize))
                {
                    // PDFBOX-5763: avoids -Infinity if empty value and tiny rectangle
                    return heightBasedFontSize;
                }
                
                return Math.min(heightBasedFontSize, widthBasedFontSize);
            }
        }
        return fontSize;
    }

    /*
     * Resolve the cap height.
     * 
     * This is a very basic implementation using the height of "H" as reference.
     */
    private float resolveCapHeight(PDFont font) throws IOException {
        return resolveGlyphHeight(font, "H".codePointAt(0));
    }

    /*
     * Resolve the descent.
     * 
     * This is a very basic implementation using the height of "y" - "a" as reference.
     */
    private float resolveDescent(PDFont font) throws IOException {
        return resolveGlyphHeight(font, "y".codePointAt(0)) - resolveGlyphHeight(font, "a".codePointAt(0));
    }

    // this calculates the real (except for type 3 fonts) individual glyph bounds
    private float resolveGlyphHeight(PDFont font, int code) throws IOException {
        GeneralPath path = null;
        if (font instanceof PDType3Font) {
            // It is difficult to calculate the real individual glyph bounds for type 3
            // fonts
            // because these are not vector fonts, the content stream could contain almost
            // anything
            // that is found in page content streams.
            PDType3Font t3Font = (PDType3Font) font;
            PDType3CharProc charProc = t3Font.getCharProc(code);
            if (charProc != null) {
                BoundingBox fontBBox = t3Font.getBoundingBox();
                PDRectangle glyphBBox = charProc.getGlyphBBox();
                if (glyphBBox != null) {
                    // PDFBOX-3850: glyph bbox could be larger than the font bbox
                    glyphBBox.setLowerLeftX(Math.max(fontBBox.getLowerLeftX(), glyphBBox.getLowerLeftX()));
                    glyphBBox.setLowerLeftY(Math.max(fontBBox.getLowerLeftY(), glyphBBox.getLowerLeftY()));
                    glyphBBox.setUpperRightX(Math.min(fontBBox.getUpperRightX(), glyphBBox.getUpperRightX()));
                    glyphBBox.setUpperRightY(Math.min(fontBBox.getUpperRightY(), glyphBBox.getUpperRightY()));
                    path = glyphBBox.toGeneralPath();
                }
            }
        } else if (font instanceof PDVectorFont) {
            PDVectorFont vectorFont = (PDVectorFont) font;
            path = vectorFont.getPath(code);
        } else if (font instanceof PDSimpleFont) {
            PDSimpleFont simpleFont = (PDSimpleFont) font;

            // these two lines do not always work, e.g. for the TT fonts in file 032431.pdf
            // which is why PDVectorFont is tried first.
            String name = simpleFont.getEncoding().getName(code);
            path = simpleFont.getPath(name);
        } else {
            // shouldn't happen, please open issue in JIRA
            LOG.warn("Unknown font class: " + font.getClass());
        }
        if (path == null) {
            return -1;
        }
        return (float) path.getBounds2D().getHeight();
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

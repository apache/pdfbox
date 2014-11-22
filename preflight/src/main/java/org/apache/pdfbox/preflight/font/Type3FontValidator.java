/*****************************************************************************
 * 
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * 
 ****************************************************************************/

package org.apache.pdfbox.preflight.font;

import static org.apache.pdfbox.preflight.PreflightConfiguration.RESOURCES_PROCESS;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_FONTS_DICTIONARY_INVALID;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_FONTS_METRICS;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_FONTS_TYPE3_DAMAGED;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.font.PDType3CharProc;
import org.apache.pdfbox.pdmodel.font.encoding.DictionaryEncoding;
import org.apache.pdfbox.pdmodel.font.encoding.Encoding;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.COSArrayList;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDFontFactory;
import org.apache.pdfbox.pdmodel.font.PDType3Font;
import org.apache.pdfbox.preflight.PreflightConstants;
import org.apache.pdfbox.preflight.PreflightContext;
import org.apache.pdfbox.preflight.PreflightPath;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.preflight.content.ContentStreamException;
import org.apache.pdfbox.preflight.exception.ValidationException;
import org.apache.pdfbox.preflight.font.container.FontContainer;
import org.apache.pdfbox.preflight.font.container.Type3Container;
import org.apache.pdfbox.preflight.font.util.GlyphException;
import org.apache.pdfbox.preflight.font.util.PreflightType3Stream;
import org.apache.pdfbox.preflight.utils.COSUtils;
import org.apache.pdfbox.preflight.utils.ContextHelper;

public class Type3FontValidator extends FontValidator<Type3Container>
{
    protected PDType3Font font;
    protected COSDictionary fontDictionary;
    protected COSDocument cosDocument;
    protected Encoding encoding;

    public Type3FontValidator(PreflightContext context, PDType3Font font)
    {
        super(context, font.getCOSObject(), new Type3Container(font));
        this.cosDocument = context.getDocument().getDocument();
        this.fontDictionary = font.getCOSObject();
        this.font = font;
    }

    @Override
    public void validate() throws ValidationException
    {
        checkMandatoryField();
        checkFontBBox();
        checkFontMatrix();
        checkEncoding();
        checkResources();
        checkCharProcsAndMetrics();
        checkToUnicode();
    }

    protected void checkMandatoryField()
    {
        boolean areFieldsPResent = fontDictionary.containsKey(COSName.FONT_BBOX);
        areFieldsPResent &= fontDictionary.containsKey(COSName.FONT_MATRIX);
        areFieldsPResent &= fontDictionary.containsKey(COSName.CHAR_PROCS);
        areFieldsPResent &= fontDictionary.containsKey(COSName.ENCODING);
        areFieldsPResent &= fontDictionary.containsKey(COSName.FIRST_CHAR);
        areFieldsPResent &= fontDictionary.containsKey(COSName.LAST_CHAR);
        areFieldsPResent &= fontDictionary.containsKey(COSName.WIDTHS);

        if (!areFieldsPResent)
        {
            this.fontContainer.push(new ValidationError(ERROR_FONTS_DICTIONARY_INVALID,
                    "Some required fields are missing from the Font dictionary."));
        }
    }

    /**
     * Check that the FontBBox element has the right format as declared in the
     * PDF reference document.
     */
    private void checkFontBBox()
    {
        COSBase fontBBox = fontDictionary.getItem(COSName.FONT_BBOX);

        if (!COSUtils.isArray(fontBBox, cosDocument))
        {
            this.fontContainer.push(new ValidationError(ERROR_FONTS_DICTIONARY_INVALID,
                    "The FontBBox element isn't an array"));
            return;
        }

        /*
         * check the content of the FontBBox. Should be an array with 4 numbers
         */
        COSArray bbox = COSUtils.getAsArray(fontBBox, cosDocument);
        if (bbox.size() != 4)
        {
            this.fontContainer.push(new ValidationError(ERROR_FONTS_DICTIONARY_INVALID,
                    "The FontBBox element is invalid"));
            return;
        }
        
        for (int i = 0; i < 4; i++)
        {
            COSBase elt = bbox.get(i);
            if (!(COSUtils.isFloat(elt, cosDocument) || COSUtils.isInteger(elt, cosDocument)))
            {
                this.fontContainer.push(new ValidationError(ERROR_FONTS_DICTIONARY_INVALID,
                        "An element of FontBBox isn't a number"));
                return;
            }
        }
    }

    /**
     * Check that the FontMatrix element has the right format as declared in the PDF reference document.
     */
    private void checkFontMatrix()
    {
        COSBase fontMatrix = fontDictionary.getItem(COSName.FONT_MATRIX);

        if (!COSUtils.isArray(fontMatrix, cosDocument))
        {
            this.fontContainer.push(new ValidationError(ERROR_FONTS_DICTIONARY_INVALID,
                    "The FontMatrix element isn't an array"));
            return;
        }

        /*
         * Check the content of the FontMatrix. Should be an array with 6 numbers
         */
        COSArray matrix = COSUtils.getAsArray(fontMatrix, cosDocument);
        if (matrix.size() != 6)
        {
            this.fontContainer.push(new ValidationError(ERROR_FONTS_DICTIONARY_INVALID,
                    "The FontMatrix element is invalid"));
            return;
        }

        for (int i = 0; i < 6; i++)
        {
            COSBase elt = matrix.get(i);
            if (!(COSUtils.isFloat(elt, cosDocument) || COSUtils.isInteger(elt, cosDocument)))
            {
                this.fontContainer.push(new ValidationError(ERROR_FONTS_DICTIONARY_INVALID,
                        "An element of FontMatrix isn't a number"));
                return;
            }
        }
    }

    /**
     * For a Type3 font, the mapping between the Character Code and the
     * Character name is entirely defined in the Encoding Entry. The Encoding
     * Entry can be a Name (For the 5 predefined Encoding) or a Dictionary. If
     * it is a dictionary, the "Differences" array contains the correspondence
     * between a character code and a set of character name which are different
     * from the encoding entry of the dictionary.
     *
     * This method checks that the encoding is :
     * <UL>
     * <li>An existing encoding name.
     * <li>A dictionary with an existing encoding name (the name is optional)
     * and a well formed "Differences" array (the array is optional)
     * </UL>
     *
     * At the end of this method, if the validation succeed the Font encoding is
     * kept in the {@link #encoding} attribute
     */
    @Override
    protected void checkEncoding()
    {
        COSBase fontEncoding = fontDictionary.getItem(COSName.ENCODING);
        if (COSUtils.isString(fontEncoding, cosDocument))
        {
            checkEncodingAsString(fontEncoding);
        }
        else if (COSUtils.isDictionary(fontEncoding, cosDocument))
        {
            checkEncodingAsDictionary(fontEncoding);
        }
        else
        {
            // the encoding entry is invalid
            this.fontContainer.push(new ValidationError(ERROR_FONTS_TYPE3_DAMAGED,
                    "The Encoding entry doesn't have the right type"));
        }
    }

    /**
     * This method is called by the CheckEncoding method if the Encoding entry is a String. In this case, the String
     * must be an existing encoding name. (WinAnsi, MacRoman...)
     * 
     * @param fontEncoding
     */
    private void checkEncodingAsString(COSBase fontEncoding)
    {
        // Encoding is a Name, check if it is an Existing Encoding
        String enc = COSUtils.getAsString(fontEncoding, cosDocument);
        this.encoding = Encoding.getInstance(COSName.getPDFName(enc));
    }

    /**
     * This method is called by the CheckEncoding method if the Encoding entry is an instance of COSDictionary. In this
     * case, a new instance of {@link DictionaryEncoding} is created. If an IOException is thrown by the
     * DictionaryEncoding constructor the ERROR_FONTS_ENCODING is pushed in the
     * FontContainer.
     * 
     * Differences entry validation is implicitly done by the DictionaryEncoding constructor.
     * 
     * @param fontEncoding
     */
    private void checkEncodingAsDictionary(COSBase fontEncoding)
    {
        COSDictionary encodingDictionary = COSUtils.getAsDictionary(fontEncoding, cosDocument);
        this.encoding = new DictionaryEncoding(encodingDictionary, false, null);
    }

    /**
     * CharProcs is a dictionary where the key is a character name and the value is a Stream which contains the glyph
     * representation of the key.
     * 
     * This method checks that all characters codes defined in the Widths Array exist in the CharProcs dictionary. If
     * the CharProcs doesn't know the Character, it is mapped with the .notdef one.
     * 
     * For each character, the Glyph width must be the same as the Width value declared in the Widths array.
     */
    private void checkCharProcsAndMetrics() throws ValidationException
    {
        List<Integer> widths = getWidths(font);
        if (widths == null || widths.isEmpty())
        {
            this.fontContainer.push(new ValidationError(ERROR_FONTS_DICTIONARY_INVALID,
                    "The Witdhs array is unreachable"));
            return;
        }

        COSDictionary charProcs = COSUtils.getAsDictionary(fontDictionary.getItem(COSName.CHAR_PROCS), cosDocument);
        if (charProcs == null)
        {
            this.fontContainer.push(new ValidationError(ERROR_FONTS_DICTIONARY_INVALID,
                    "The CharProcs element isn't a dictionary"));
            return;
        }

        int fc = font.getCOSObject().getInt(COSName.FIRST_CHAR, -1);
        int lc = font.getCOSObject().getInt(COSName.LAST_CHAR, -1);

        /*
         * wArr length = (lc - fc) + 1 and it is an array of int. 
         * If FirstChar is greater than LastChar, the validation
         * will fail because of the array will have an expected size &lt;= 0.
         */
        int expectedLength = (lc - fc) + 1;
        if (widths.size() != expectedLength)
        {
            this.fontContainer.push(new ValidationError(ERROR_FONTS_DICTIONARY_INVALID,
                    "The length of Witdhs array is invalid. Expected : \"" + expectedLength + "\" Current : \""
                            + widths.size() + "\""));
            return;
        }

        // Check width consistency
        for (int i = 0; i < expectedLength; i++)
        {
            int code = fc + i;
            float width = widths.get(i);

            PDType3CharProc charProc = getCharProc(code);
            if (charProc != null)
            {
                try
                {
                    float fontProgramWidth = getWidthFromCharProc(charProc);
                    if (width == fontProgramWidth)
                    {
                        // Glyph is OK, we keep the CID.
                        this.fontContainer.markAsValid(code);
                    }
                    else
                    {
                        GlyphException glyphEx = new GlyphException(ERROR_FONTS_METRICS, code,
                                "The character with CID\"" + code + "\" should have a width equals to " + width);
                        this.fontContainer.markAsInvalid(code, glyphEx);
                    }
                }
                catch (ContentStreamException e)
                {
                    // TODO spaces/isartor-6-2-3-3-t02-fail-h.pdf --> si ajout de l'erreur dans le container le test
                    // echoue... pourquoi si la font est utilisée ca devrait planter???
                    this.context.addValidationError(new ValidationError(((ContentStreamException) e).getErrorCode(), e
                            .getMessage()));
                    return;
                }
                catch (IOException e)
                {
                    this.fontContainer.push(new ValidationError(ERROR_FONTS_TYPE3_DAMAGED,
                            "The CharProcs references an element which can't be read"));
                    return;
                }
            }
        }
    }

    public List<Integer> getWidths(PDFont font)
    {
        List<Integer> widths;
        COSArray array = (COSArray) font.getCOSObject().getDictionaryObject(COSName.WIDTHS);
        if (array != null)
        {
            widths = COSArrayList.convertIntegerCOSArrayToList(array);
        }
        else
        {
            widths = Collections.emptyList();
        }
        return widths;
    }

    private PDType3CharProc getCharProc(int code) throws ValidationException
    {
        PDType3CharProc charProc = font.getCharProc(code);
        if (charProc == null)
        {
            // There are no character description, we declare the Glyph as Invalid. If the character
            // is used in a Stream, the GlyphDetail will throw an exception.
            GlyphException glyphEx = new GlyphException(ERROR_FONTS_METRICS, code,
                    "The CharProcs \"" + font.getEncoding().getName(code)  + "\" doesn't exist");
            this.fontContainer.markAsInvalid(code, glyphEx);
        }
        return charProc;
    }

    /**
     * Parse the Glyph description to obtain the Width
     * 
     * @return the width of the character
     */
    private float getWidthFromCharProc(PDType3CharProc charProc) throws IOException
    {
        PreflightPath vPath = context.getValidationPath();
        PreflightType3Stream parser = new PreflightType3Stream(context, vPath.getClosestPathElement(PDPage.class), charProc);
        parser.showType3Character(charProc);
        return parser.getWidth();
    }

    /**
     * If the Resources entry is present, this method checks its content. Only fonts and Images are checked because this
     * resource describes glyphs.
     */
    private void checkResources() throws ValidationException
    {
        COSBase resources = this.fontDictionary.getItem(COSName.RESOURCES);
        if (resources != null)
        {

            COSDictionary dictionary = COSUtils.getAsDictionary(resources, cosDocument);
            if (dictionary == null)
            {
                this.fontContainer.push(new ValidationError(ERROR_FONTS_DICTIONARY_INVALID,
                        "The Resources element isn't a dictionary"));
                return;
            }

            // process Fonts and XObjects
            ContextHelper.validateElement(context, new PDResources(dictionary), RESOURCES_PROCESS);
            COSBase cbFont = dictionary.getItem(COSName.FONT);

            if (cbFont != null)
            {
                /*
                 * Check that all referenced object are present in the PDF file
                 */
                COSDictionary dicFonts = COSUtils.getAsDictionary(cbFont, cosDocument);
                Set<COSName> keyList = dicFonts.keySet();
                for (Object key : keyList)
                {

                    COSBase item = dicFonts.getItem((COSName) key);
                    COSDictionary xObjFont = COSUtils.getAsDictionary(item, cosDocument);

                    try
                    {
                        PDFont aFont = PDFontFactory.createFont(xObjFont);
                        FontContainer aContainer = this.context.getFontContainer(aFont.getCOSObject());
                        // another font is used in the Type3, check if the font is valid.
                        if (!aContainer.isValid())
                        {
                            this.fontContainer.push(new ValidationError(ERROR_FONTS_TYPE3_DAMAGED,
                                    "The Resources dictionary of type 3 font contains invalid font"));
                        }
                    }
                    catch (IOException e)
                    {
                        context.addValidationError(new ValidationError(PreflightConstants.ERROR_FONTS_DAMAGED,
                                "Unable to valid the Type3 : " + e.getMessage()));
                    }
                }
            }
        }
    }
}

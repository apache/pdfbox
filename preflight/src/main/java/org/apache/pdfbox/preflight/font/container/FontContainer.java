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

package org.apache.pdfbox.preflight.font.container;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.pdmodel.font.PDFontLike;
import org.apache.pdfbox.preflight.PreflightConstants;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.preflight.font.util.GlyphDetail;
import org.apache.pdfbox.preflight.font.util.GlyphException;

public abstract class FontContainer<T extends PDFontLike>
{
    /**
     * List of validation errors that occur during the font validation. If the font is used by an object in the PDF, all
     * these errors will be appended to the Error list of the PreflightContext.
     */
    protected List<ValidationError> errorBuffer = new ArrayList<ValidationError>();

    /**
     * Boolean used to known if the font is embedded.
     */
    protected boolean embeddedFont = true;

    private final Map<Integer, GlyphDetail> codeToDetail = new HashMap<Integer, GlyphDetail>();
    private boolean errorsAlreadyMerged = false;

    /**
     * Font-like object.
     */
    protected final T font;

    public FontContainer(T font)
    {
        this.font = font;
    }

    public void push(ValidationError error)
    {
        this.errorBuffer.add(error);
    }

    public void push(List<ValidationError> errors)
    {
        this.errorBuffer.addAll(errors);
    }

    public List<ValidationError> getAllErrors()
    {
        return this.errorBuffer;
    }

    public boolean isValid()
    {
        return this.errorBuffer.isEmpty() && isEmbeddedFont();
    }

    public boolean errorsAleadyMerged()
    {
        return errorsAlreadyMerged;
    }

    public void setErrorsAlreadyMerged(boolean errorsAlreadyMerged)
    {
        this.errorsAlreadyMerged = errorsAlreadyMerged;
    }

    public boolean isEmbeddedFont()
    {
        return embeddedFont;
    }

    public void notEmbedded()
    {
        this.embeddedFont = false;
    }

    /**
     * Check that the Width or W entry in the PDF matches the widths in the embedded font program.
     *
     * @param code character code
     * @throws GlyphException
     */
    public void checkGlyphWidth(int code) throws GlyphException
    {
        if (isAlreadyProcessed(code))
        {
            return;
        }

        try
        {
            // check for missing glyphs
            if (!hasGlyph(code))
            {
                GlyphException e = new GlyphException(PreflightConstants.ERROR_FONTS_GLYPH_MISSING, code, "The character code "
                        + code + " in the font program \"" + font.getName()
                        + "\" is missing from the Character Encoding");
                markAsInvalid(code, e);
                throw e;
            }

            // check widths
            float expectedWidth = font.getWidth(code);
            float foundWidth = font.getWidthFromFont(code);
            checkWidthsConsistency(code, expectedWidth, foundWidth);
        }
        catch (IOException e)
        {
            throw new GlyphException(PreflightConstants.ERROR_FONTS_GLYPH, code,
                    "Unexpected error during the width validation for the character code " + code +
                    " : " + e.getMessage(), e);
        }
    }

    /**
     * Checks if the embedded font contains a glyph for the given character code.
     *
     * @param code character code
     */
    protected abstract boolean hasGlyph(int code) throws IOException;

    /**
     * Check if the given character code has already been processed.
     * 
     * @param code character code
     * @return true if the CID has previously been marked as valid, false otherwise
     * @throws GlyphException if the code has previously been marked as invalid // TODO useful ??
     */
    private boolean isAlreadyProcessed(int code) throws GlyphException
    {
        boolean already = false;
        GlyphDetail detail = codeToDetail.get(code);
        if (detail != null)
        {
            detail.throwExceptionIfNotValid();
            already = true;
        }
        return already;
    }

    /**
     * Test if both widths are consistent. At the end of this method, the CID is
     * marked as valid or invalid.
     *
     * @param code character code
     * @param expectedWidth expected with given in the PDF file
     * @param foundWidth the glyph width found in the font program, a negative
     * value if the CID is missing from the font.
     * @throws GlyphException the appropriate exception if the CID is invalid.
     */
    private void checkWidthsConsistency(int code, float expectedWidth, float foundWidth) throws GlyphException
    {
        // consistent is defined to be a difference of no more than 1/1000 unit.
        if (Math.abs(foundWidth - expectedWidth) > 1)
        {
            GlyphException e = new GlyphException(PreflightConstants.ERROR_FONTS_METRICS, code,
                    "Width (" + foundWidth + ") of the character \"" + code + "\" in the font program \"" + this.font.getName()
                            + "\" is inconsistent with the width (" + expectedWidth + ") in the PDF dictionary.");
            markAsInvalid(code, e);
            throw e;
        }
        markAsValid(code);
    }

    public final void markAsValid(int code)
    {
        this.codeToDetail.put(code, new GlyphDetail(code));
    }

    public final void markAsInvalid(int code, GlyphException e)
    {
        this.codeToDetail.put(code, new GlyphDetail(code, e));
    }
}

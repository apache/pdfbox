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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.preflight.PreflightConstants;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.preflight.font.util.GlyphDetail;
import org.apache.pdfbox.preflight.font.util.GlyphException;

public abstract class FontContainer
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

    /**
     * Link CID to an Object that contain information about the Glyph state (Valid or no)
     */
    protected Map<Integer, GlyphDetail> computedCid = new HashMap<Integer, GlyphDetail>();

    protected boolean errorsAleadyMerged = false;

    protected PDFont font;

    public FontContainer(PDFont font)
    {
        super();
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
        return (this.errorBuffer.isEmpty() && isEmbeddedFont());
    }

    public boolean errorsAleadyMerged()
    {
        return errorsAleadyMerged;
    }

    public void setErrorsAleadyMerged(boolean errorsAleadyMerged)
    {
        this.errorsAleadyMerged = errorsAleadyMerged;
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
     * 
     * @param cid
     * @throws GlyphException
     */
    public void checkGlyphWith(int cid) throws GlyphException
    {
        if (isAlreadyComputedCid(cid))
        {
            return;
        }

        final float expectedWidth = this.font.getFontWidth(cid);
        final float foundWidth = getFontProgramWidth(cid);
        checkWidthsConsistency(cid, expectedWidth, foundWidth);
    }

    /**
     * Check if the given CID is already computed
     * 
     * @param cid
     *            the CID to check
     * @return true if the CID has previously been marked as valid, false otherwise
     * @throws GlyphException
     *             if the CID has previously been marked as invalid // TODO useful ??
     */
    protected boolean isAlreadyComputedCid(int cid) throws GlyphException
    {
        boolean already = false;
        GlyphDetail gdetail = this.computedCid.get(cid);
        if (gdetail != null)
        {
            gdetail.throwExceptionIfNotValid();
            already = true;
        }
        return already;
    }

    /**
     * Extract the Glyph width for the given CID.
     * 
     * @param cid
     * @return The Glyph width in 'em' unit.
     * @throws GlyphException 
     */
    protected abstract float getFontProgramWidth(int cid) throws GlyphException;

    /**
     * Test if both width are consistent. At the end of this method, the CID is marked as valid or invalid.
     * 
     * @param cid
     * @param expectedWidth
     * @param foundWidth
     *            the glyph width found in the font program, a negative value if the CID is missing from the font.
     * @throws GlyphException
     */
    protected void checkWidthsConsistency(int cid, float expectedWidth, float foundWidth) throws GlyphException
    {
        if (foundWidth < 0)
        {
            GlyphException e = new GlyphException(PreflightConstants.ERROR_FONTS_GLYPH_MISSING, cid, "The character \""
                    + cid + "\" in the font program \"" + this.font.getBaseFont()
                    + "\"is missing from the Charater Encoding.");
            markCIDAsInvalid(cid, e);
            throw e;
        }

        // consistent is defined to be a difference of no more than 1/1000 unit.
        if (Math.abs(foundWidth - expectedWidth) > 1)
        {
            GlyphException e = new GlyphException(PreflightConstants.ERROR_FONTS_METRICS, cid,
                    "Width of the character \"" + cid + "\" in the font program \"" + this.font.getBaseFont()
                            + "\"is inconsistent with the width in the PDF dictionary.");
            markCIDAsInvalid(cid, e);
            throw e;
        }
        markCIDAsValid(cid);
    }

    public final void markCIDAsValid(int cid)
    {
        this.computedCid.put(cid, new GlyphDetail(cid));
    }

    public final void markCIDAsInvalid(int cid, GlyphException gex)
    {
        this.computedCid.put(cid, new GlyphDetail(cid, gex));
    }
}

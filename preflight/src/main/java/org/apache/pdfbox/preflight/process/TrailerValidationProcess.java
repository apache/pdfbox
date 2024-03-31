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

package org.apache.pdfbox.preflight.process;

import java.util.List;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.cos.COSObjectKey;
import org.apache.pdfbox.preflight.PreflightConstants;
import org.apache.pdfbox.preflight.PreflightContext;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.preflight.exception.ValidationException;

public class TrailerValidationProcess extends AbstractProcess
{

    @Override
    public void validate(PreflightContext ctx) throws ValidationException
    {
        PDDocument pdfDoc = ctx.getDocument();

        COSDictionary linearizedDict = pdfDoc.getDocument().getLinearizedDictionary();
        // linearized files have two trailers, everything else is not a linearized file
        // so don't make the checks for updated linearized files
        if (linearizedDict != null && ctx.getXrefTrailerResolver().getTrailerCount() == 2 &&
                ctx.getFileLen() == linearizedDict.getLong(COSName.L))
        {
            // it is a linearized PDF, check the linearized dictionary
            checkLinearizedDictionary(ctx, linearizedDict);

            // if the pdf is a linearized pdf. the first trailer must be checked
            // and it must have the same ID than the last trailer.
            // According to the PDF version, trailers are available by the trailer key word (pdf <= 1.4)
            // or in the dictionary of the XRef stream ( PDF >= 1.5)
            float pdfVersion = pdfDoc.getVersion();
            if (pdfVersion <= 1.4f)
            {
                checkTrailersForLinearizedPDF14(ctx);
            }
            else
            {
                checkTrailersForLinearizedPDF15(ctx);
            }
        }
        else
        {
            // If the PDF isn't a linearized one, only the last trailer must be checked
            checkMainTrailer(ctx, pdfDoc.getDocument().getTrailer());
        }
    }

    /**
     * Extracts and compares first and last trailers for PDF version between 1.1 and 1.4.
     * 
     * @param ctx the preflight context.
     */
    protected void checkTrailersForLinearizedPDF14(PreflightContext ctx)
    {
        COSDictionary first = ctx.getXrefTrailerResolver().getFirstTrailer();
        if (first == null)
        {
            addValidationError(ctx, new ValidationError(PreflightConstants.ERROR_SYNTAX_TRAILER,
                    "There are no trailer in the PDF file"));
        }
        else
        {
            COSDictionary last = ctx.getXrefTrailerResolver().getLastTrailer();
            checkMainTrailer(ctx, first);
            if (!compareIds(first, last))
            {
                addValidationError(ctx, new ValidationError(PreflightConstants.ERROR_SYNTAX_TRAILER_ID_CONSISTENCY,
                        "ID is different in the first and the last trailer"));
            }
        }
    }

    /**
     * Accesses and compares First and Last trailers for a PDF version higher than 1.4.
     * 
     * @param ctx the preflight context.
     */
    protected void checkTrailersForLinearizedPDF15(PreflightContext ctx)
    {
        COSDocument cosDocument = ctx.getDocument().getDocument();
        List<COSObject> xrefs = cosDocument.getObjectsByType(COSName.XREF);

        if (xrefs.isEmpty())
        {
            // no XRef CosObject, maybe this pdf file used the PDF 1.4 syntax
            checkTrailersForLinearizedPDF14(ctx);
        }
        else
        {
            long min = Long.MAX_VALUE;
            long max = Long.MIN_VALUE;
            COSDictionary firstTrailer = null;
            COSDictionary lastTrailer = null;

            // Search First and Last trailers according to offset position.
            for (COSObject co : xrefs)
            {
                long offset = cosDocument.getXrefTable().get(co.getKey());
                if (offset < min)
                {
                    min = offset;
                    firstTrailer = (COSDictionary) co.getObject();
                }

                if (offset > max)
                {
                    max = offset;
                    lastTrailer = (COSDictionary) co.getObject();
                }

            }

            checkMainTrailer(ctx, firstTrailer);
            if (!compareIds(firstTrailer, lastTrailer))
            {
                addValidationError(ctx, new ValidationError(PreflightConstants.ERROR_SYNTAX_TRAILER_ID_CONSISTENCY,
                        "ID is different in the first and the last trailer"));
            }
        }
    }

    /**
     * Return true if the ID of the first dictionary is the same as the id of the last dictionary Return false
     * otherwise.
     * 
     * @param first the first dictionary for comparison.
     * @param last the last dictionary for comparison.
     * @return true if the IDs of the first and last dictionary are the same.
     */
    protected boolean compareIds(COSDictionary first, COSDictionary last)
    {
        COSBase idFirst = first.getDictionaryObject(COSName.ID);
        COSBase idLast = last.getDictionaryObject(COSName.ID);
        // According to the revised PDF/A specification the IDs have to be identical
        // if both are present, otherwise everything is fine
        if (idFirst != null && idLast != null)
        {
            // ---- if one COSArray is null, the PDF/A isn't valid
            if (!(idFirst instanceof COSArray) || !(idLast instanceof COSArray))
            {
                return false;
            }
            // ---- compare both arrays
            boolean isEqual = true;
            for (COSBase of : (COSArray) idFirst)
            {
                boolean oneIsEquals = false;
                String ofString = ((COSString) of).getString();
                for (COSBase ol : (COSArray) idLast)
                {
                    // ---- according to PDF Reference 1-4, ID is an array containing two strings
                    if (oneIsEquals)
                    {
                        break;
                    }
                    oneIsEquals = ((COSString) ol).getString().equals(ofString);
                }
                isEqual &= oneIsEquals;
                if (!isEqual)
                {
                    break;
                }
            }
            return isEqual;
        }
        else
        {
            return true;
        }
    }

    /**
     * check if all keys are authorized in a trailer dictionary and if the type is valid.
     * 
     * @param ctx the preflight context.
     * @param trailer the trailer dictionary.
     */
    protected void checkMainTrailer(PreflightContext ctx, COSDictionary trailer)
    {
        // PDF/A Trailer dictionary must contain the ID key
        if (!trailer.containsKey(COSName.ID))
        {
            addValidationError(ctx,
                    new ValidationError(PreflightConstants.ERROR_SYNTAX_TRAILER_MISSING_ID,
                            "The trailer dictionary doesn't contain ID"));
        }
        else if (trailer.getCOSArray(COSName.ID) == null)
        {
            addValidationError(ctx,
                    new ValidationError(PreflightConstants.ERROR_SYNTAX_TRAILER_TYPE_INVALID,
                            "The trailer dictionary contains an id but it isn't an array"));
        }

        // PDF/A Trailer dictionary mustn't contain the Encrypt key
        if (trailer.containsKey(COSName.ENCRYPT))
        {
            addValidationError(ctx, new ValidationError(PreflightConstants.ERROR_SYNTAX_TRAILER_ENCRYPT,
                    "The trailer dictionary contains Encrypt"));
        }

        // PDF Trailer dictionary must contain the Size key
        if (!trailer.containsKey(COSName.SIZE))
        {
            addValidationError(ctx,
                    new ValidationError(PreflightConstants.ERROR_SYNTAX_TRAILER_MISSING_SIZE,
                            "The trailer dictionary doesn't contain Size"));
        }
        else if (!(trailer.getDictionaryObject(COSName.SIZE) instanceof COSInteger))
        {
            addValidationError(ctx,
                    new ValidationError(PreflightConstants.ERROR_SYNTAX_TRAILER_TYPE_INVALID,
                            "The trailer dictionary contains a size but it isn't an integer"));
        }

        // PDF Trailer dictionary must contain the Root key
        if (!trailer.containsKey(COSName.ROOT))
        {
            addValidationError(ctx,
                    new ValidationError(PreflightConstants.ERROR_SYNTAX_TRAILER_MISSING_ROOT,
                            "The trailer dictionary doesn't contain Root"));
        }
        else if (trailer.getCOSDictionary(COSName.ROOT) == null)
        {
            addValidationError(ctx,
                    new ValidationError(PreflightConstants.ERROR_SYNTAX_TRAILER_TYPE_INVALID,
                            "The trailer dictionary contains a root but it isn't a dictionary"));
        }

        // PDF Trailer dictionary may contain the Prev key
        if (trailer.containsKey(COSName.PREV)
                && !(trailer.getDictionaryObject(COSName.PREV) instanceof COSInteger))
        {
            addValidationError(ctx,
                    new ValidationError(PreflightConstants.ERROR_SYNTAX_TRAILER_TYPE_INVALID,
                            "The trailer dictionary contains a prev but it isn't an integer"));
        }

        // PDF Trailer dictionary may contain the Info key
        if (trailer.containsKey(COSName.INFO) && trailer.getCOSDictionary(COSName.INFO) == null)
        {
            addValidationError(ctx,
                    new ValidationError(PreflightConstants.ERROR_SYNTAX_TRAILER_TYPE_INVALID,
                            "The trailer dictionary contains an info but it isn't a dictionary"));
        }
    }

    /**
     * Check if mandatory keys of linearized dictionary are present.
     * 
     * @param ctx the preflight context.
     * @param linearizedDict the linearization dictionary.
     */
    protected void checkLinearizedDictionary(PreflightContext ctx, COSDictionary linearizedDict)
    {
        // ---- check if all keys are authorized in a linearized dictionary
        // ---- Linearized dictionary must contain the lhoent keys
        boolean l = linearizedDict.getItem(COSName.L) != null;
        boolean h = linearizedDict.getItem(COSName.H) != null;
        boolean o = linearizedDict.getItem(COSName.O) != null;
        boolean e = linearizedDict.getItem(COSName.E) != null;
        boolean n = linearizedDict.getItem(COSName.N) != null;
        boolean t = linearizedDict.getItem(COSName.T) != null;

        if (!(l && h && o && e && t && n))
        {
            addValidationError(ctx, new ValidationError(PreflightConstants.ERROR_SYNTAX_DICT_INVALID,
                    "Invalid key in The Linearized dictionary"));
        }
    }

}

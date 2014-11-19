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

import static org.apache.pdfbox.preflight.PreflightConstants.DICTIONARY_KEY_LINEARIZED;
import static org.apache.pdfbox.preflight.PreflightConstants.DICTIONARY_KEY_LINEARIZED_E;
import static org.apache.pdfbox.preflight.PreflightConstants.DICTIONARY_KEY_LINEARIZED_H;
import static org.apache.pdfbox.preflight.PreflightConstants.DICTIONARY_KEY_LINEARIZED_L;
import static org.apache.pdfbox.preflight.PreflightConstants.DICTIONARY_KEY_LINEARIZED_N;
import static org.apache.pdfbox.preflight.PreflightConstants.DICTIONARY_KEY_LINEARIZED_O;
import static org.apache.pdfbox.preflight.PreflightConstants.DICTIONARY_KEY_LINEARIZED_T;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_SYNTAX_TRAILER;
import static org.apache.pdfbox.preflight.PreflightConstants.TRAILER_DICTIONARY_KEY_ENCRYPT;
import static org.apache.pdfbox.preflight.PreflightConstants.TRAILER_DICTIONARY_KEY_ID;
import static org.apache.pdfbox.preflight.PreflightConstants.TRAILER_DICTIONARY_KEY_INFO;
import static org.apache.pdfbox.preflight.PreflightConstants.TRAILER_DICTIONARY_KEY_PREV;
import static org.apache.pdfbox.preflight.PreflightConstants.TRAILER_DICTIONARY_KEY_ROOT;
import static org.apache.pdfbox.preflight.PreflightConstants.TRAILER_DICTIONARY_KEY_SIZE;

import java.io.IOException;
import java.util.List;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.persistence.util.COSObjectKey;
import org.apache.pdfbox.preflight.PreflightConstants;
import org.apache.pdfbox.preflight.PreflightContext;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.preflight.exception.ValidationException;
import org.apache.pdfbox.preflight.utils.COSUtils;

public class TrailerValidationProcess extends AbstractProcess
{

    public void validate(PreflightContext ctx) throws ValidationException
    {
        PDDocument pdfDoc = ctx.getDocument();

        COSDictionary linearizedDict = getLinearizedDictionary(pdfDoc);
        if (linearizedDict != null)
        {
            // it is a linearized PDF, check the linearized dictionary
            checkLinearizedDictionnary(ctx, linearizedDict);

            // if the pdf is a linearized pdf. the first trailer must be checked
            // and it must have the same ID than the last trailer.
            // According to the PDF version, trailers are available by the trailer key word (pdf <= 1.4)
            // or in the dictionary of the XRef stream ( PDF >= 1.5)
            String pdfVersion = pdfDoc.getDocument().getHeaderString();
            if (pdfVersion != null && pdfVersion.matches("%PDF-1\\.[1-4]"))
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
        COSDictionary first = ctx.getXrefTableResolver().getFirstTrailer();
        if (first == null)
        {
            addValidationError(ctx, new ValidationError(ERROR_SYNTAX_TRAILER, "There are no trailer in the PDF file"));
        }
        else
        {
            COSDictionary last = ctx.getXrefTableResolver().getLastTrailer();
            COSDocument cosDoc = new COSDocument();
            checkMainTrailer(ctx, first);
            if (!compareIds(first, last, cosDoc))
            {
                addValidationError(ctx, new ValidationError(PreflightConstants.ERROR_SYNTAX_TRAILER_ID_CONSISTENCY,
                        "ID is different in the first and the last trailer"));
            }
            COSUtils.closeDocumentQuietly(cosDoc);
        }
    }

    /**
     * Accesses and compares First and Last trailers for a PDF version higher than 1.4.
     * 
     * @param ctx the preflight context.
     */
    protected void checkTrailersForLinearizedPDF15(PreflightContext ctx)
    {
        PDDocument pdfDoc = ctx.getDocument();
        try
        {
            COSDocument cosDocument = pdfDoc.getDocument();
            List<COSObject> xrefs = cosDocument.getObjectsByType(COSName.XREF);

            if (xrefs.isEmpty())
            {
                // no XRef CosObject, may by this pdf file used the PDF 1.4 syntaxe
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
                    long offset = cosDocument.getXrefTable().get(new COSObjectKey(co));
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
                if (!compareIds(firstTrailer, lastTrailer, cosDocument))
                {
                    addValidationError(ctx, new ValidationError(PreflightConstants.ERROR_SYNTAX_TRAILER_ID_CONSISTENCY,
                            "ID is different in the first and the last trailer"));
                }
            }
        }
        catch (IOException e)
        {
            addValidationError(ctx, new ValidationError(PreflightConstants.ERROR_SYNTAX_TRAILER,
                    "Unable to check PDF Trailers due to : " + e.getMessage()));
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
    protected boolean compareIds(COSDictionary first, COSDictionary last, COSDocument cosDocument)
    {
        COSBase idFirst = first.getItem(COSName.getPDFName(TRAILER_DICTIONARY_KEY_ID));
        COSBase idLast = last.getItem(COSName.getPDFName(TRAILER_DICTIONARY_KEY_ID));
        // According to the revised PDF/A specification the IDs have to be identical
        // if both are present, otherwise everything is fine
        if (idFirst != null && idLast != null)
        {
    
            // ---- cast two COSBase to COSArray.
            COSArray af = COSUtils.getAsArray(idFirst, cosDocument);
            COSArray al = COSUtils.getAsArray(idLast, cosDocument);
    
            // ---- if one COSArray is null, the PDF/A isn't valid
            if ((af == null) || (al == null))
            {
                return false;
            }
    
            // ---- compare both arrays
            boolean isEqual = true;
            for (Object of : af.toList())
            {
                boolean oneIsEquals = false;
                for (Object ol : al.toList())
                {
                    // ---- according to PDF Reference 1-4, ID is an array containing two
                    // strings
                    if (!oneIsEquals)
                    {
                        oneIsEquals = ((COSString) ol).getString().equals(((COSString) of).getString());
                    }
                    else
                    {
                        break;
                    }
                }
                isEqual = isEqual && oneIsEquals;
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
        boolean id = false;
        boolean root = false;
        boolean size = false;
        boolean prev = false;
        boolean info = false;
        boolean encrypt = false;

        for (Object key : trailer.keySet())
        {
            if (!(key instanceof COSName))
            {
                addValidationError(ctx, new ValidationError(PreflightConstants.ERROR_SYNTAX_DICTIONARY_KEY_INVALID,
                        "Invalid key in The trailer dictionary"));
                return;
            }

            String cosName = ((COSName) key).getName();
            if (cosName.equals(TRAILER_DICTIONARY_KEY_ENCRYPT))
            {
                encrypt = true;
            }
            if (cosName.equals(TRAILER_DICTIONARY_KEY_SIZE))
            {
                size = true;
            }
            if (cosName.equals(TRAILER_DICTIONARY_KEY_PREV))
            {
                prev = true;
            }
            if (cosName.equals(TRAILER_DICTIONARY_KEY_ROOT))
            {
                root = true;
            }
            if (cosName.equals(TRAILER_DICTIONARY_KEY_INFO))
            {
                info = true;
            }
            if (cosName.equals(TRAILER_DICTIONARY_KEY_ID))
            {
                id = true;
            }
        }

        COSDocument cosDocument = ctx.getDocument().getDocument();
        // PDF/A Trailer dictionary must contain the ID key
        if (!id)
        {
            addValidationError(ctx, new ValidationError(PreflightConstants.ERROR_SYNTAX_TRAILER_MISSING_ID,
                    "The trailer dictionary doesn't contain ID"));
        }
        else
        {
            COSBase trailerId = trailer.getItem(TRAILER_DICTIONARY_KEY_ID);
            if (!COSUtils.isArray(trailerId, cosDocument))
            {
                addValidationError(ctx, new ValidationError(PreflightConstants.ERROR_SYNTAX_TRAILER_TYPE_INVALID,
                        "The trailer dictionary contains an id but it isn't an array"));
            }
        }
        // PDF/A Trailer dictionary mustn't contain the Encrypt key
        if (encrypt)
        {
            addValidationError(ctx, new ValidationError(PreflightConstants.ERROR_SYNTAX_TRAILER_ENCRYPT,
                    "The trailer dictionary contains Encrypt"));
        }
        // PDF Trailer dictionary must contain the Size key
        if (!size)
        {
            addValidationError(ctx, new ValidationError(PreflightConstants.ERROR_SYNTAX_TRAILER_MISSING_SIZE,
                    "The trailer dictionary doesn't contain Size"));
        }
        else
        {
            COSBase trailerSize = trailer.getItem(TRAILER_DICTIONARY_KEY_SIZE);
            if (!COSUtils.isInteger(trailerSize, cosDocument))
            {
                addValidationError(ctx, new ValidationError(PreflightConstants.ERROR_SYNTAX_TRAILER_TYPE_INVALID,
                        "The trailer dictionary contains a size but it isn't an integer"));
            }
        }

        // PDF Trailer dictionary must contain the Root key
        if (!root)
        {
            addValidationError(ctx, new ValidationError(PreflightConstants.ERROR_SYNTAX_TRAILER_MISSING_ROOT,
                    "The trailer dictionary doesn't contain Root"));
        }
        else
        {
            COSBase trailerRoot = trailer.getItem(TRAILER_DICTIONARY_KEY_ROOT);
            if (!COSUtils.isDictionary(trailerRoot, cosDocument))
            {
                addValidationError(ctx, new ValidationError(PreflightConstants.ERROR_SYNTAX_TRAILER_TYPE_INVALID,
                        "The trailer dictionary contains a root but it isn't a dictionary"));
            }
        }
        // PDF Trailer dictionary may contain the Prev key
        if (prev)
        {
            COSBase trailerPrev = trailer.getItem(TRAILER_DICTIONARY_KEY_PREV);
            if (!COSUtils.isInteger(trailerPrev, cosDocument))
            {
                addValidationError(ctx, new ValidationError(PreflightConstants.ERROR_SYNTAX_TRAILER_TYPE_INVALID,
                        "The trailer dictionary contains a prev but it isn't an integer"));
            }
        }
        // PDF Trailer dictionary may contain the Info key
        if (info)
        {
            COSBase trailerInfo = trailer.getItem(TRAILER_DICTIONARY_KEY_INFO);
            if (!COSUtils.isDictionary(trailerInfo, cosDocument))
            {
                addValidationError(ctx, new ValidationError(PreflightConstants.ERROR_SYNTAX_TRAILER_TYPE_INVALID,
                        "The trailer dictionary contains an info but it isn't a dictionary"));
            }
        }
    }

    /**
     * According to the PDF Reference, A linearized PDF contain a dictionary as first object (linearized dictionary) and
     * only this one in the first section.
     * 
     * @param document the document to validate.
     * @return the linearization dictionary or null.
     */
    protected COSDictionary getLinearizedDictionary(PDDocument document)
    {
        // ---- Get Ref to obj
        COSDocument cDoc = document.getDocument();
        List<?> lObj = cDoc.getObjects();
        for (Object object : lObj)
        {
            COSBase curObj = ((COSObject) object).getObject();
            if (curObj instanceof COSDictionary
                    && ((COSDictionary) curObj).keySet().contains(COSName.getPDFName(DICTIONARY_KEY_LINEARIZED)))
            {
                return (COSDictionary) curObj;
            }
        }
        return null;
    }

    /**
     * Check if mandatory keys of linearized dictionary are present.
     * 
     * @param ctx the preflight context.
     * @param linearizedDict the linearization dictionary.
     */
    protected void checkLinearizedDictionnary(PreflightContext ctx, COSDictionary linearizedDict)
    {
        // ---- check if all keys are authorized in a linearized dictionary
        // ---- Linearized dictionary must contain the lhoent keys
        boolean l = false;
        boolean h = false;
        boolean o = false;
        boolean e = false;
        boolean n = false;
        boolean t = false;

        for (Object key : linearizedDict.keySet())
        {
            if (!(key instanceof COSName))
            {
                addValidationError(ctx, new ValidationError(PreflightConstants.ERROR_SYNTAX_DICTIONARY_KEY_INVALID,
                        "Invalid key in The Linearized dictionary"));
                return;
            }

            String cosName = ((COSName) key).getName();
            if (cosName.equals(DICTIONARY_KEY_LINEARIZED_L))
            {
                l = true;
            }
            if (cosName.equals(DICTIONARY_KEY_LINEARIZED_H))
            {
                h = true;
            }
            if (cosName.equals(DICTIONARY_KEY_LINEARIZED_O))
            {
                o = true;
            }
            if (cosName.equals(DICTIONARY_KEY_LINEARIZED_E))
            {
                e = true;
            }
            if (cosName.equals(DICTIONARY_KEY_LINEARIZED_N))
            {
                n = true;
            }
            if (cosName.equals(DICTIONARY_KEY_LINEARIZED_T))
            {
                t = true;
            }
        }

        if (!(l && h && o && e && t && n))
        {
            addValidationError(ctx, new ValidationError(PreflightConstants.ERROR_SYNTAX_DICT_INVALID,
                    "Invalid key in The Linearized dictionary"));
        }

        return;
    }

}

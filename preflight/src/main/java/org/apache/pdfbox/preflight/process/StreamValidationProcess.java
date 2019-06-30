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

import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_SYNTAX_STREAM_FX_KEYS;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_SYNTAX_STREAM_INVALID_FILTER;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_SYNTAX_STREAM_LENGTH_MISSING;

import java.util.List;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.preflight.PreflightContext;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.preflight.exception.ValidationException;
import org.apache.pdfbox.preflight.utils.FilterHelper;

public class StreamValidationProcess extends AbstractProcess
{

    @Override
    public void validate(PreflightContext ctx) throws ValidationException
    {
        PDDocument pdfDoc = ctx.getDocument();

        List<COSObject> lCOSObj = pdfDoc.getDocument().getObjects();
        for (COSObject cObj : lCOSObj)
        {
            // If this object represents a Stream, the Dictionary must contain the Length key
            COSBase cBase = cObj.getObject();
            if (cBase instanceof COSStream)
            {
                validateStreamObject(ctx, cObj);
            }
        }
    }

    public void validateStreamObject(PreflightContext context, COSObject cObj) throws ValidationException
    {
        COSStream streamObj = (COSStream) cObj.getObject();

        // ---- Check dictionary entries
        // ---- Only the Length entry is mandatory
        // ---- In a PDF/A file, F, FFilter and FDecodeParms are forbidden
        checkDictionaryEntries(context, streamObj);
        // ---- Check the Filter value(s)
        checkFilters(streamObj, context);
    }

    /**
     * This method checks if one of declared Filter is LZWdecode. If LZW is found, the result list is updated with an
     * error code.
     * 
     * @param stream the stream to check.
     * @param context the preflight context.
     */
    protected void checkFilters(COSStream stream, PreflightContext context)
    {
        COSBase bFilter = stream.getDictionaryObject(COSName.FILTER);
        if (bFilter != null)
        {
            if (bFilter instanceof COSArray)
            {
                COSArray afName = (COSArray) bFilter;
                for (int i = 0; i < afName.size(); ++i)
                {
                    FilterHelper.isAuthorizedFilter(context, afName.getString(i));
                }
            }
            else if (bFilter instanceof COSName)
            {
                String fName = ((COSName) bFilter).getName();
                FilterHelper.isAuthorizedFilter(context, fName);
            }
            else
            {
                // ---- The filter type is invalid
                addValidationError(context, new ValidationError(ERROR_SYNTAX_STREAM_INVALID_FILTER,
                        "Filter should be a Name or an Array"));
            }
        }
        // else Filter entry is optional
    }

    /**
     * Check dictionary entries. Only the Length entry is mandatory. In a PDF/A file, F, FFilter and FDecodeParms are
     * forbidden
     * 
     * @param context the preflight context.
     * @param streamObj the stream to check.
     */
    protected void checkDictionaryEntries(PreflightContext context, COSStream streamObj)
    {
        boolean len = streamObj.containsKey(COSName.LENGTH);
        boolean f = streamObj.containsKey(COSName.F);
        boolean ffilter = streamObj.containsKey(COSName.F_FILTER);
        boolean fdecParams = streamObj.containsKey(COSName.F_DECODE_PARMS);

        if (!len)
        {
            addValidationError(context, new ValidationError(ERROR_SYNTAX_STREAM_LENGTH_MISSING,
                    "Stream length is missing"));
        }

        if (f || ffilter || fdecParams)
        {
            addValidationError(context, new ValidationError(ERROR_SYNTAX_STREAM_FX_KEYS,
                    "F, FFilter or FDecodeParms keys are present in the stream dictionary"));
        }
    }
    
}

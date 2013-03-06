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

package org.apache.pdfbox.preflight.utils;

import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_SYNTAX_STREAM_INVALID_FILTER;
import static org.apache.pdfbox.preflight.PreflightConstants.ERROR_SYNTAX_STREAM_UNDEFINED_FILTER;
import static org.apache.pdfbox.preflight.PreflightConstants.INLINE_DICTIONARY_VALUE_FILTER_ASCII_85;
import static org.apache.pdfbox.preflight.PreflightConstants.INLINE_DICTIONARY_VALUE_FILTER_ASCII_HEX;
import static org.apache.pdfbox.preflight.PreflightConstants.INLINE_DICTIONARY_VALUE_FILTER_CCITTFF;
import static org.apache.pdfbox.preflight.PreflightConstants.INLINE_DICTIONARY_VALUE_FILTER_DCT;
import static org.apache.pdfbox.preflight.PreflightConstants.INLINE_DICTIONARY_VALUE_FILTER_FLATE_DECODE;
import static org.apache.pdfbox.preflight.PreflightConstants.INLINE_DICTIONARY_VALUE_FILTER_LZW;
import static org.apache.pdfbox.preflight.PreflightConstants.INLINE_DICTIONARY_VALUE_FILTER_RUN;
import static org.apache.pdfbox.preflight.PreflightConstants.STREAM_DICTIONARY_VALUE_FILTER_ASCII_85;
import static org.apache.pdfbox.preflight.PreflightConstants.STREAM_DICTIONARY_VALUE_FILTER_ASCII_HEX;
import static org.apache.pdfbox.preflight.PreflightConstants.STREAM_DICTIONARY_VALUE_FILTER_CCITTFF;
import static org.apache.pdfbox.preflight.PreflightConstants.STREAM_DICTIONARY_VALUE_FILTER_DCT;
import static org.apache.pdfbox.preflight.PreflightConstants.STREAM_DICTIONARY_VALUE_FILTER_FLATE_DECODE;
import static org.apache.pdfbox.preflight.PreflightConstants.STREAM_DICTIONARY_VALUE_FILTER_JBIG;
import static org.apache.pdfbox.preflight.PreflightConstants.STREAM_DICTIONARY_VALUE_FILTER_LZW;
import static org.apache.pdfbox.preflight.PreflightConstants.STREAM_DICTIONARY_VALUE_FILTER_RUN;

import org.apache.pdfbox.preflight.PreflightContext;
import org.apache.pdfbox.preflight.PreflightDocument;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;

public class FilterHelper
{

    /**
     * This method checks if the filter is authorized for the PDF file according to the preflight document specification
     * attribute. For example according to the PDF/A-1 specification, only the LZW filter is forbidden due to Copyright
     * compatibility. Because of the PDF/A is based on the PDF1.4 specification, all filters that aren't declared in the
     * PDF Reference Third Edition are rejected.
     * 
     * @param context
     *            the preflight context
     * @param filter
     *            the filter to checks
     */
    public static void isAuthorizedFilter(PreflightContext context, String filter)
    {
        PreflightDocument preflightDocument = context.getDocument();
        switch (preflightDocument.getSpecification())
        {
        case PDF_A1A:
            isAuthorizedFilterInPDFA(context, filter);
            break;

        default:
            // PDF/A-1b is the default format
            isAuthorizedFilterInPDFA(context, filter);
            break;
        }
    }

    /**
     * This method checks if the filter is authorized for a PDF/A file. According to the PDF/A-1 specification, only the
     * LZW filter is forbidden due to Copyright compatibility. Because of the PDF/A is based on the PDF1.4
     * specification, all filters that aren't declared in the PDF Reference Third Edition are rejected.
     * 
     * @param context
     * @param filter
     */
    public static void isAuthorizedFilterInPDFA(PreflightContext context, String filter)
    {
        if (filter != null)
        {
            // --- LZW is forbidden.
            if (STREAM_DICTIONARY_VALUE_FILTER_LZW.equals(filter) || INLINE_DICTIONARY_VALUE_FILTER_LZW.equals(filter))
            {
                context.addValidationError(new ValidationError(ERROR_SYNTAX_STREAM_INVALID_FILTER,
                        "LZWDecode is forbidden"));
            }

            // --- Filters declared in the PDF Reference for PDF 1.4
            // --- Other Filters are considered as invalid to avoid not consistent behaviour
            boolean definedFilter = STREAM_DICTIONARY_VALUE_FILTER_FLATE_DECODE.equals(filter);
            definedFilter = definedFilter || STREAM_DICTIONARY_VALUE_FILTER_ASCII_HEX.equals(filter);
            definedFilter = definedFilter || STREAM_DICTIONARY_VALUE_FILTER_ASCII_85.equals(filter);
            definedFilter = definedFilter || STREAM_DICTIONARY_VALUE_FILTER_CCITTFF.equals(filter);
            definedFilter = definedFilter || STREAM_DICTIONARY_VALUE_FILTER_DCT.equals(filter);
            definedFilter = definedFilter || STREAM_DICTIONARY_VALUE_FILTER_JBIG.equals(filter);
            definedFilter = definedFilter || STREAM_DICTIONARY_VALUE_FILTER_RUN.equals(filter);

            definedFilter = definedFilter || INLINE_DICTIONARY_VALUE_FILTER_FLATE_DECODE.equals(filter);
            definedFilter = definedFilter || INLINE_DICTIONARY_VALUE_FILTER_ASCII_HEX.equals(filter);
            definedFilter = definedFilter || INLINE_DICTIONARY_VALUE_FILTER_ASCII_85.equals(filter);
            definedFilter = definedFilter || INLINE_DICTIONARY_VALUE_FILTER_CCITTFF.equals(filter);
            definedFilter = definedFilter || INLINE_DICTIONARY_VALUE_FILTER_DCT.equals(filter);
            definedFilter = definedFilter || INLINE_DICTIONARY_VALUE_FILTER_RUN.equals(filter);

            if (!definedFilter)
            {
                context.addValidationError(new ValidationError(ERROR_SYNTAX_STREAM_UNDEFINED_FILTER,
                        "This filter isn't defined in the PDF Reference Third Edition : " + filter));
            }
        }
    }
}

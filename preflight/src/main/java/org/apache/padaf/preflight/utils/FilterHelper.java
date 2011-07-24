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

package org.apache.padaf.preflight.utils;

import static org.apache.padaf.preflight.ValidationConstants.ERROR_SYNTAX_STREAM_INVALID_FILTER;
import static org.apache.padaf.preflight.ValidationConstants.ERROR_SYNTAX_STREAM_UNDEFINED_FILTER;
import static org.apache.padaf.preflight.ValidationConstants.INLINE_DICTIONARY_VALUE_FILTER_ASCII_85;
import static org.apache.padaf.preflight.ValidationConstants.INLINE_DICTIONARY_VALUE_FILTER_ASCII_HEX;
import static org.apache.padaf.preflight.ValidationConstants.INLINE_DICTIONARY_VALUE_FILTER_CCITTFF;
import static org.apache.padaf.preflight.ValidationConstants.INLINE_DICTIONARY_VALUE_FILTER_DCT;
import static org.apache.padaf.preflight.ValidationConstants.INLINE_DICTIONARY_VALUE_FILTER_FLATE_DECODE;
import static org.apache.padaf.preflight.ValidationConstants.INLINE_DICTIONARY_VALUE_FILTER_LZW;
import static org.apache.padaf.preflight.ValidationConstants.INLINE_DICTIONARY_VALUE_FILTER_RUN;
import static org.apache.padaf.preflight.ValidationConstants.STREAM_DICTIONARY_VALUE_FILTER_ASCII_85;
import static org.apache.padaf.preflight.ValidationConstants.STREAM_DICTIONARY_VALUE_FILTER_ASCII_HEX;
import static org.apache.padaf.preflight.ValidationConstants.STREAM_DICTIONARY_VALUE_FILTER_CCITTFF;
import static org.apache.padaf.preflight.ValidationConstants.STREAM_DICTIONARY_VALUE_FILTER_DCT;
import static org.apache.padaf.preflight.ValidationConstants.STREAM_DICTIONARY_VALUE_FILTER_FLATE_DECODE;
import static org.apache.padaf.preflight.ValidationConstants.STREAM_DICTIONARY_VALUE_FILTER_JBIG;
import static org.apache.padaf.preflight.ValidationConstants.STREAM_DICTIONARY_VALUE_FILTER_LZW;
import static org.apache.padaf.preflight.ValidationConstants.STREAM_DICTIONARY_VALUE_FILTER_RUN;

import java.util.List;

import org.apache.padaf.preflight.ValidationResult.ValidationError;


public class FilterHelper {

	/**
	 * This method checks if the filter is authorized for a PDF/A file.
	 * According to the PDF/A-1 specification, only the LZW filter is forbidden due to
	 * Copyright compatibility. Because of the PDF/A is based on the PDF1.4 specification, 
	 * all filters that aren't declared in the PDF Reference Third Edition are rejected. 
	 * 
	 * @param filter the filter to checks
	 * @param errors the list of validation errors
	 * @return true if the filter is authorized, false otherwise.
	 */
	public static boolean isAuthorizedFilter(String filter, List<ValidationError> errors) {
		String errorCode = isAuthorizedFilter(filter);
		if (errorCode != null) {
			// --- LZW is forbidden.
			if ( ERROR_SYNTAX_STREAM_INVALID_FILTER.equals(errorCode) ) {
				errors.add(new ValidationError(ERROR_SYNTAX_STREAM_INVALID_FILTER, "LZWDecode is forbidden"));
				return false;
			} else {
				errors.add(new ValidationError(ERROR_SYNTAX_STREAM_UNDEFINED_FILTER, "This filter isn't defined in the PDF Reference Third Edition : "+filter));
				return false;				
			}
		}
		return true;
	}

	/**
	 * This method checks if the filter is authorized for a PDF/A file.
	 * According to the PDF/A-1 specification, only the LZW filter is forbidden due to
	 * Copyright compatibility. Because of the PDF/A is based on the PDF1.4 specification, 
	 * all filters that aren't declared in the PDF Reference Third Edition are rejected. 
	 * 
	 * @param filter
	 * @return null if validation succeed, the errorCode if the validation failed
	 */
	public static String isAuthorizedFilter(String filter) {
		if (filter != null) {
			// --- LZW is forbidden.
			if (STREAM_DICTIONARY_VALUE_FILTER_LZW.equals(filter) || INLINE_DICTIONARY_VALUE_FILTER_LZW.equals(filter) ) {
				return ERROR_SYNTAX_STREAM_INVALID_FILTER;
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
			
			if (!definedFilter) {
				return ERROR_SYNTAX_STREAM_UNDEFINED_FILTER;
			}
		}
		return null;
	}
}

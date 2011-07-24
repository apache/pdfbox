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

package org.apache.padaf.preflight.helpers;

import java.util.ArrayList;
import java.util.List;


import org.apache.padaf.preflight.DocumentHandler;
import org.apache.padaf.preflight.ValidationConstants;
import org.apache.padaf.preflight.ValidationException;
import org.apache.padaf.preflight.ValidatorConfig;
import org.apache.padaf.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.cos.COSDocument;

/**
 * Check if the number of inderect objects is less great than ValidationConstant.MAX_INDIRECT_OBJ.
 */
public class XRefValidationHelper extends AbstractValidationHelper {

	/**
	 * @param cfg
	 * @throws ValidationException
	 */
	public XRefValidationHelper(ValidatorConfig cfg) throws ValidationException {
		super(cfg);
	}

	/* (non-Javadoc)
	 * @see net.padaf.preflight.helpers.AbstractValidationHelper#innerValidate(net.padaf.preflight.DocumentHandler)
	 */
	@Override
	public List<ValidationError> innerValidate(DocumentHandler handler)
	throws ValidationException {
		List<ValidationError> errors = new ArrayList<ValidationError>();
		COSDocument document = handler.getDocument().getDocument();
		if ( document.getObjects().size() > ValidationConstants.MAX_INDIRECT_OBJ ) {
			errors.add(new ValidationError(ERROR_SYNTAX_INDIRECT_OBJ_RANGE, "Too many indirect objects"));
		}
		return errors;
	}

}
 
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

import java.util.List;

import org.apache.padaf.preflight.DocumentHandler;
import org.apache.padaf.preflight.ValidationException;
import org.apache.padaf.preflight.ValidatorConfig;
import org.apache.padaf.preflight.ValidationResult.ValidationError;
import org.apache.padaf.preflight.font.FontValidator;
import org.apache.padaf.preflight.helpers.FontValidationHelper;


/**
 * Class used by the TestConfiguredValidator class
 */
public class MyFontValidationHelper extends FontValidationHelper {

	public MyFontValidationHelper(ValidatorConfig cfg)
	throws ValidationException {
		super(cfg);
	}

	protected void initFontValidatorFactory () {
		super.initFontValidatorFactory();
		System.out.println("Override the initFontValidatorFactory method");
	}
	
	public void validateFont(DocumentHandler handler, FontValidator fontVal,
				List<ValidationError> result) throws ValidationException {
		System.out.println("Override the validateFont method");
		super.validateFont(handler, fontVal, result);
		result.add(new ValidationError("UNCODEINCONNU"));
	}
}

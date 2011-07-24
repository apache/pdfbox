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
import org.apache.padaf.preflight.ValidationConstants;
import org.apache.padaf.preflight.ValidationException;
import org.apache.padaf.preflight.ValidatorConfig;
import org.apache.padaf.preflight.ValidationResult.ValidationError;
import org.apache.padaf.preflight.actions.ActionManagerFactory;
import org.apache.padaf.preflight.annotation.AnnotationValidatorFactory;


/**
 * Abstract class for ValidationHelper. A validation helper is an object which
 * check the PDF/A-1x compliance of a specific PDF element.
 */
public abstract class AbstractValidationHelper implements ValidationConstants {
  protected ValidatorConfig valConfig = null; 

  protected AnnotationValidatorFactory annotFact = null;
  protected ActionManagerFactory actionFact = null;
  
  public AbstractValidationHelper(ValidatorConfig cfg) throws ValidationException {
	  valConfig = cfg;

	  try {
	    this.actionFact = cfg.getActionFact().newInstance();
	  } catch ( IllegalAccessException e) {
		  throw new ValidationException("Unable to instatiate action factory : " + e.getMessage(), e);
	  } catch ( InstantiationException e) {
		  throw new ValidationException("Unable to instatiate action factory : " + e.getMessage(), e);
	  }
	  
	  try {
		  this.annotFact = cfg.getAnnotFact().newInstance();
		  this.annotFact.setActionFact(this.actionFact);
	  } catch ( IllegalAccessException e) {
		  throw new ValidationException("Unable to instatiate annotation factory : " + e.getMessage(), e);
	  } catch ( InstantiationException e) {
		  throw new ValidationException("Unable to instatiate annotation factory : " + e.getMessage(), e);		  
	  }

  }

  /**
   * Validation process of all inherited classes.
   * 
   * @param handler
   *          the object which contains the PDF Document
   * @return A list of validation error. If there are no error, the list is
   *         empty.
   * @throws ValidationException
   */
  public abstract List<ValidationError> innerValidate(DocumentHandler handler)
      throws ValidationException;

  /**
   * Process the validation of specific elements contained in the PDF document.
   * 
   * @param handler
   *          the object which contains the PDF Document
   * @return A list of validation error. If there are no error, the list is
   *         empty.
   * @throws ValidationException
   */
  public final List<ValidationError> validate(DocumentHandler handler)
      throws ValidationException {
    checkHandler(handler);
    return innerValidate(handler);
  }

  /**
   * Check if the Handler isn't null and if it is complete.
   * 
   * @param handler
   * @throws ValidationException
   */
  protected void checkHandler(DocumentHandler handler)
      throws ValidationException {
    if (handler == null) {
      throw new ValidationException("DocumentHandler can't be null");
    }
    if (!handler.isComplete()) {
      throw new ValidationException("DocumentHandler error : missing element");
    }
  }

}

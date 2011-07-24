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

package org.apache.padaf.preflight;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.activation.DataSource;

import org.apache.commons.io.IOUtils;
import org.apache.padaf.preflight.ValidationResult.ValidationError;
import org.apache.padaf.preflight.helpers.AbstractValidationHelper;

import org.apache.padaf.preflight.javacc.ParseException;

public abstract class AbstractValidator implements PdfAValidator {

	static {
		try {
			InputStream is = AbstractValidator.class.getClassLoader().getResourceAsStream("project.version");
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			IOUtils.copy(is, bos);
			IOUtils.closeQuietly(is);
			IOUtils.closeQuietly(bos);
			fullName = "PADAF - " + new String(bos.toByteArray(),"us-ascii");
		} catch (Exception e) {
			e.printStackTrace();
			fullName = "PADAF - Unknown version";
		}
	}
	
	protected ValidatorConfig config = null;
	
	protected static String fullName;
	

	protected Collection<AbstractValidationHelper> priorHelpers = new ArrayList<AbstractValidationHelper>();
	protected Collection<AbstractValidationHelper> standHelpers = new ArrayList<AbstractValidationHelper>();

	/**
	 * 
	 * @param cfg
	 * @throws ValidationException
	 */
	public AbstractValidator ( ValidatorConfig cfg ) throws ValidationException {
		config = cfg;

		Collection<Class<? extends AbstractValidationHelper>> ph = cfg.getPriorHelpers();
		for (Class<? extends AbstractValidationHelper> priorHlpCls : ph) {
			this.priorHelpers.add(instantiateHelper(priorHlpCls, cfg));
		}

		Collection<Class<? extends AbstractValidationHelper>> sh = cfg.getStandHelpers();
		for (Class<? extends AbstractValidationHelper> standHlpCls : sh) {
			this.priorHelpers.add(instantiateHelper(standHlpCls, cfg));
		}

	}

	/**
	 * Instantiate a ValidationHelper using the given class.
	 * 
	 * @param avhCls
	 * @param cfg
	 * @return
	 * @throws ValidationException
	 */
	private AbstractValidationHelper instantiateHelper(Class<? extends AbstractValidationHelper> avhCls, ValidatorConfig cfg)
	throws ValidationException {
		try {
			Constructor<? extends AbstractValidationHelper> construct = avhCls.getConstructor(ValidatorConfig.class);
			return construct.newInstance(cfg);
		} catch (NoSuchMethodException e) {
			throw new ValidationException("Unable to create an instance of ValidationHelper : " + e.getMessage(), e);
		} catch (InvocationTargetException e) {
			throw new ValidationException("Unable to create an instance of ValidationHelper : " + e.getMessage(), e);			
		} catch (IllegalAccessException e) {
			throw new ValidationException("Unable to create an instance of ValidationHelper : " + e.getMessage(), e);
		} catch (InstantiationException e) {
			throw new ValidationException("Unable to create an instance of ValidationHelper : " + e.getMessage(), e);
		}
	}

	/**
	 * Create an instance of Document Handler.
	 * This method can be override if a inherited class of DocumentHandler 
	 * must be used.
	 * 
	 * @param source
	 * @return
	 */
	protected DocumentHandler createDocumentHandler(DataSource source) {
		return new DocumentHandler(source);
	}

	/**
	 * This method calls the validate method of the given ValidationHelper. A
	 * validation exception will be thrown if the Helper throws a validation
	 * exception and if the list of errors is empty.
	 * 
	 * @param handler
	 *          the document handler which contains elements for the validation
	 * @param helper
	 *          An inherited class of AbstractValidationHelper.
	 * @param errors
	 *          A list of validation errors
	 * @throws ValidationException
	 */
	protected void runValidation(DocumentHandler handler,
			AbstractValidationHelper helper, List<ValidationError> errors)
	throws ValidationException {
		try {
			errors.addAll(helper.validate(handler));
		} catch (ValidationException e) {
			if (errors.size() == 0) {
				// If there are no error, the Exception is thrown because of we can't
				// know if the
				// exception is due to a validation error or to a unexpected cause.
				throw e;
			}
		}
	}

	/**
	 * Create an instance of ValidationResult. This object contains an instance of
	 * ValidationError. If the ParseException is an instance of PdfParseException,
	 * the embedded validation error is initialized with the error code of the
	 * exception, otherwise it is an UnknownError.
	 * 
	 * @param e
	 * @return
	 */
	protected ValidationResult createErrorResult(ParseException e) {
		if (e instanceof PdfParseException) {
			if (e.getCause()==null) {
				return new ValidationResult(new ValidationError(((PdfParseException) e)
						.getErrorCode()));

			} else if (e.getCause().getMessage()==null) {
				return new ValidationResult(new ValidationError(((PdfParseException) e)
						.getErrorCode()));
			} else {
				return new ValidationResult(new ValidationError(((PdfParseException) e)
						.getErrorCode(),e.getCause().getMessage()));

			}
		}
		return createUnknownErrorResult();
	}

	/**
	 * Create an instance of ValidationResult with a
	 * ValidationError(UNKNOWN_ERROR)
	 * 
	 * @return
	 */
	protected ValidationResult createUnknownErrorResult() {
		ValidationError error = new ValidationError(
				ValidationConstants.ERROR_UNKOWN_ERROR);
		ValidationResult result = new ValidationResult(error);
		return result;
	}

	/* (non-Javadoc)
	 * @see net.padaf.preflight.PdfAValidator#getFullName()
	 */
	public String getFullName() {
		return fullName;
	}
	
	
	
	
}
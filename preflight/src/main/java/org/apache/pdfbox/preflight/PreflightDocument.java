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

package org.apache.pdfbox.preflight;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.preflight.exception.ValidationException;
import org.apache.pdfbox.preflight.utils.ContextHelper;

public class PreflightDocument extends PDDocument
{
    private final ValidationResult result = new ValidationResult(true);

    private final PreflightConfiguration config;

    private PreflightContext context;

    private final Format specification;

    /**
     * Create a preflight document based on the COSDocument and load the default configuration for the given format.
     * 
     * @param doc the underlying COSDocument
     * @param format the format used for validation
     */
    public PreflightDocument(COSDocument doc, Format format)
    {
        this(doc, format, null);
    }

    /**
     * Create a preflight document based on the COSDocument that will use the given configuration bean to process the
     * validation. if the configuration is null, a default configuration will be load using the given format.
     * 
     * @param doc the underlying COSDocument
     * @param format the format used for validation
     * @param config the configuration used for validation
     */
    public PreflightDocument(COSDocument doc, Format format, PreflightConfiguration config)
    {
        super(doc);
        this.specification = format;
        // PDF/A1-b is default
        this.config = config == null ? PreflightConfiguration.createPdfA1BConfiguration() : config;
    }

    /**
     * Returns an unmodifiable list of all validation errors.
     * 
     * @return an unmodifiable list of all validation errors
     */
    public List<ValidationError> getValidationErrors()
    {
        return Collections.unmodifiableList(result.getErrorsList());
    }

    /**
     * Add a validation error.
     * 
     * @param error the validation error to be added
     */
    public void addValidationError(ValidationError error)
    {
        if (error != null)
        {
            this.result.addError(error);
        }
    }

    /**
     * Add a list of validation errors.
     * 
     * @param errorList the list of validation errors
     */
    public void addValidationErrors(List<ValidationError> errorList)
    {
        if (errorList != null)
        {
            this.result.addErrors(errorList);
        }
    }

    /**
     * Returns the associated preflight context. It is created after parsing the pdf.
     * 
     * @return the associated preflight context
     */
    public PreflightContext getContext()
    {
        return this.context;
    }

    /**
     * Set the preflight context for this document.
     * 
     * @param context the associated preflight context
     */
    public void setContext(PreflightContext context)
    {
        this.context = context;
    }

    /**
     * Check that PDDocument is a valid file according to the format given during the object creation.
     * 
     * @return the validation result
     * @throws ValidationException
     */
    public ValidationResult validate() throws ValidationException
    {
        context.setConfig(config);
        Collection<String> processes = config.getProcessNames();
        for (String name : processes)
        {
            ContextHelper.validateElement(context, name);
        }
        return result;
    }

    /**
     * Returns the format which is used to validate the pdf document.
     * 
     * @return the format used for validation
     */
    public Format getSpecification()
    {
        return specification;
    }

}

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

import java.io.IOException;
import java.util.Collection;

import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.preflight.exception.ValidationException;
import org.apache.pdfbox.preflight.utils.ContextHelper;

public class PreflightDocument extends PDDocument
{

    private ValidationResult result = new ValidationResult(true);

    private PreflightConfiguration config;

    private PreflightContext context;

    private final Format specification;

    /**
     * Create an empty preflight document and load the default configuration for the given format.
     * 
     * @param format
     * @throws IOException
     */
    public PreflightDocument(Format format) throws IOException
    {
        this(format, null);
    }

    /**
     * Create a preflight document based on the COSDocument and load the default configuration for the given format.
     * 
     * @param doc
     * @param format
     */
    public PreflightDocument(COSDocument doc, Format format)
    {
        this(doc, format, null);
    }

    /**
     * Create an empty preflight document that will use the given configuration bean to process the validation. if the
     * configuration is null, a default configuration will be load using the given format.
     * 
     * @param format
     * @param cfg
     * @throws IOException
     */
    public PreflightDocument(Format format, PreflightConfiguration cfg) throws IOException
    {
        this(new COSDocument(), format, cfg);
    }

    /**
     * Create a preflight document based on the COSDocument that will use the given configuration bean to process the
     * validation. if the configuration is null, a default configuration will be load using the given format.
     * 
     * @param doc
     * @param format
     * @param config
     */
    public PreflightDocument(COSDocument doc, Format format, PreflightConfiguration config)
    {
        super(doc);
        this.specification = format;
        this.config = config;
        if (this.config == null)
        {
            initConfiguration(format);
        }
    }

    private void initConfiguration(Format format)
    {
        switch (format)
        {
        default: // default is PDF/A1-b
            this.config = PreflightConfiguration.createPdfA1BConfiguration();
            break;
        }

    }

    public ValidationResult getResult()
    {
        return result;
    }

    public void setResult(ValidationResult result)
    {
        if (this.result != null)
        {
            this.result.mergeResult(result);
        }
        else if (result != null)
        {
            this.result = result;
        }
        else
        {
            this.result = new ValidationResult(true);
        }
    }

    public void addValidationError(ValidationError error)
    {
        if (error != null)
        {
            if (result == null)
            {
                this.result = new ValidationResult(error.isWarning());
            }
            this.result.addError(error);
        }
    }

    public PreflightContext getContext()
    {
        return this.context;
    }

    public void setContext(PreflightContext context)
    {
        this.context = context;
    }

    /**
     * Check that PDDocument is a valid file according to the format given during the object creation.
     * 
     * @throws ValidationException
     */
    public void validate() throws ValidationException
    {
        // force early class loading to check if people forgot to use --add-modules javax.xml.bind
        // on java 9 & 10, or to add jaxb-api on java 11 and later
        javax.xml.bind.DatatypeConverter.parseInt("0");
        context.setConfig(config);
        Collection<String> processes = config.getProcessNames();
        for (String name : processes)
        {
            ContextHelper.validateElement(context, name);
        }
    }

    public Format getSpecification()
    {
        return specification;
    }

}

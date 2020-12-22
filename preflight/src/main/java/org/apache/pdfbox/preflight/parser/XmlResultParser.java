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

package org.apache.pdfbox.preflight.parser;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.pdfbox.preflight.Format;
import org.apache.pdfbox.preflight.ValidationResult;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class XmlResultParser
{

    /**
     * Validate the given file.
     * 
     * @param file the file to be validated
     * @return the validation results as XML
     * @throws IOException if something went wrong
     */
    public Element validate(final File file) throws IOException
    {
        return validate(file, file.getName());
    }

    /**
     * Validate the given file. Add the validation results to the given XML tree.
     * 
     * @param rdocument XML based validation results of a former run
     * @param file the file to be validated
     * @return the validation results as XML
     * @throws IOException if something went wrong
     */
    public Element validate(final Document rdocument, final File file) throws IOException
    {
        return validate(rdocument, file, file.getName());
    }

    private Element validate(final File file, final String name) throws IOException
    {
        try
        {
            @SuppressWarnings({"squid:S2755"}) final // self-created XML
            Document rdocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            return validate(rdocument, file, name);
        }
        catch (final ParserConfigurationException e)
        {
            throw new IOException("Failed to init document builder", e);
        }
    }


    private Element validate(final Document rdocument, final File file, final String name)
            throws IOException
    {
        final String pdfType = Format.PDF_A1B.getFname();
        final ValidationResult result;
        final long before = System.currentTimeMillis();
        try
        {
            result = PreflightParser.validate(file);
        } 
        catch(final Exception e)
        {
            final long after = System.currentTimeMillis();
            return generateFailureResponse(rdocument, name, after - before, pdfType, e);
        }

        final long after = System.currentTimeMillis();
        if (result.isValid())
        {
            final Element preflight = generateResponseSkeleton(rdocument, name, after - before);
            // valid ?
            final Element valid = rdocument.createElement("isValid");
            valid.setAttribute("type", pdfType);
            valid.setTextContent("true");
            preflight.appendChild(valid);
            return preflight;
        }
        else
        {
            final Element preflight = generateResponseSkeleton(rdocument, name, after - before);
            // valid ?
            createResponseWithError(rdocument, pdfType, result, preflight);
            return preflight;
        }

    }

    protected void createResponseWithError(final Document rdocument, final String pdfType, final ValidationResult result, final Element preflight)
    {
        final Element valid = rdocument.createElement("isValid");
        valid.setAttribute("type", pdfType);
        valid.setTextContent("false");
        preflight.appendChild(valid);
        // errors list
        final Element errors = rdocument.createElement("errors");
        final Map<ValidationError, Integer> cleaned = cleanErrorList(result.getErrorsList());
        preflight.appendChild(errors);
        int totalCount = 0;
        for (final Map.Entry<ValidationError, Integer> entry : cleaned.entrySet())
        {
            final Element error = rdocument.createElement("error");
            final int count = entry.getValue();
            error.setAttribute("count", String.format("%d", count));
            totalCount += count;
            final Element code = rdocument.createElement("code");
            final ValidationError ve = entry.getKey();
            code.setTextContent(ve.getErrorCode());
            error.appendChild(code);
            final Element detail = rdocument.createElement("details");
            detail.setTextContent(ve.getDetails());
            error.appendChild(detail);
            if (ve.getPageNumber() != null)
            {
                final Element page = rdocument.createElement("page");
                page.setTextContent(ve.getPageNumber().toString());
                error.appendChild(page);
            }
            errors.appendChild(error);
        }
        errors.setAttribute("count", String.format("%d", totalCount));
    }

    private Map<ValidationError,Integer> cleanErrorList(final List<ValidationError> errors)
    {
        final Map<ValidationError,Integer> cleaned = new HashMap<>(errors.size());
        for (final ValidationError ve: errors)
        {
            final Integer found = cleaned.get(ve);
            if (found!=null)
            {
                cleaned.put(ve,found+1);
            }
            else
            {
                cleaned.put(ve,1);
            }

        }
         return cleaned;
    }

    private Element generateFailureResponse(final Document rdocument, final String name, final long duration,
                                            final String pdfType, final Exception e)
    {
        final Element preflight = generateResponseSkeleton(rdocument, name, duration);
        // valid ?
        final Element valid = rdocument.createElement("isValid");
        valid.setAttribute("type", pdfType);
        valid.setTextContent("false");
        preflight.appendChild(valid);
        // exception 
        final Element exception = rdocument.createElement("exceptionThrown");
        final Element message = rdocument.createElement("message");
        message.setTextContent(e.getMessage());
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        pw.close();
        final Element stack = rdocument.createElement("stackTrace");
        stack.setTextContent(sw.toString());
        exception.appendChild(message);
        exception.appendChild(stack);
        preflight.appendChild(exception);
        return preflight;
    }

    protected Element generateResponseSkeleton(final Document rdocument, final String name, final long duration)
    {
        final Element preflight = rdocument.createElement("preflight");
        preflight.setAttribute("name", name);
        // duration
        final Element eduration = rdocument.createElement("executionTimeMS");
        eduration.setTextContent(String.format("%d", duration));
        preflight.appendChild(eduration);
        // return skeleton
        return preflight;
    }


}

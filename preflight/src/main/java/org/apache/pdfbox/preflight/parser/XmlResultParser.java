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
    public Element validate(File file) throws IOException
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
    public Element validate(Document rdocument, File file) throws IOException
    {
        return validate(rdocument, file, file.getName());
    }

    private Element validate(File file, String name) throws IOException
    {
        try
        {
            @SuppressWarnings({"squid:S2755"}) // self-created XML
            Document rdocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            return validate(rdocument, file, name);
        }
        catch (ParserConfigurationException e)
        {
            throw new IOException("Failed to init document builder", e);
        }
    }


    private Element validate(Document rdocument, File file, String name)
            throws IOException
    {
        String pdfType = Format.PDF_A1B.getFname();
        ValidationResult result;
        long before = System.currentTimeMillis();
        try
        {
            result = PreflightParser.validate(file);
        } 
        catch(Exception e) 
        {
            long after = System.currentTimeMillis();
            return generateFailureResponse(rdocument, name, after - before, pdfType, e);
        }

        long after = System.currentTimeMillis();
        if (result.isValid())
        {
            Element preflight = generateResponseSkeleton(rdocument, name, after - before);
            // valid ?
            Element valid = rdocument.createElement("isValid");
            valid.setAttribute("type", pdfType);
            valid.setTextContent("true");
            preflight.appendChild(valid);
            return preflight;
        }
        else
        {
            Element preflight = generateResponseSkeleton(rdocument, name, after - before);
            // valid ?
            createResponseWithError(rdocument, pdfType, result, preflight);
            return preflight;
        }

    }

    protected void createResponseWithError(Document rdocument, String pdfType, ValidationResult result, Element preflight)
    {
        Element valid = rdocument.createElement("isValid");
        valid.setAttribute("type", pdfType);
        valid.setTextContent("false");
        preflight.appendChild(valid);
        // errors list
        Element errors = rdocument.createElement("errors");
        Map<ValidationError, Integer> cleaned = cleanErrorList(result.getErrorsList());
        preflight.appendChild(errors);
        int totalCount = 0;
        for (Map.Entry<ValidationError, Integer> entry : cleaned.entrySet())
        {
            Element error = rdocument.createElement("error");
            int count = entry.getValue();
            error.setAttribute("count", String.format("%d", count));
            totalCount += count;
            Element code = rdocument.createElement("code");
            ValidationError ve = entry.getKey();
            code.setTextContent(ve.getErrorCode());
            error.appendChild(code);
            Element detail = rdocument.createElement("details");
            detail.setTextContent(ve.getDetails());
            error.appendChild(detail);
            if (ve.getPageNumber() != null)
            {
                Element page = rdocument.createElement("page");
                page.setTextContent(ve.getPageNumber().toString());
                error.appendChild(page);
            }
            errors.appendChild(error);
        }
        errors.setAttribute("count", String.format("%d", totalCount));
    }

    private Map<ValidationError,Integer> cleanErrorList(List<ValidationError> errors)
    {
        Map<ValidationError,Integer> cleaned = new HashMap<>(errors.size());
        for (ValidationError ve: errors)
        {
            Integer found = cleaned.get(ve);
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

    private Element generateFailureResponse(Document rdocument, String name, long duration,
            String pdfType, Exception e)
    {
        Element preflight = generateResponseSkeleton(rdocument, name, duration);
        // valid ?
        Element valid = rdocument.createElement("isValid");
        valid.setAttribute("type", pdfType);
        valid.setTextContent("false");
        preflight.appendChild(valid);
        // exception 
        Element exception = rdocument.createElement("exceptionThrown");
        Element message = rdocument.createElement("message");
        message.setTextContent(e.getMessage());
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        pw.close();
        Element stack = rdocument.createElement("stackTrace");
        stack.setTextContent(sw.toString());
        exception.appendChild(message);
        exception.appendChild(stack);
        preflight.appendChild(exception);
        return preflight;
    }

    protected Element generateResponseSkeleton(Document rdocument, String name, long duration)
    {
        Element preflight = rdocument.createElement("preflight");
        preflight.setAttribute("name", name);
        // duration
        Element eduration = rdocument.createElement("executionTimeMS");
        eduration.setTextContent(String.format("%d", duration));
        preflight.appendChild(eduration);
        // return skeleton
        return preflight;
    }


}

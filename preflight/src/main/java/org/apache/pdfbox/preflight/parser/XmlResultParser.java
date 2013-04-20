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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.activation.DataSource;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.pdfbox.preflight.PreflightDocument;
import org.apache.pdfbox.preflight.ValidationResult;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.preflight.exception.SyntaxValidationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class XmlResultParser
{


    public Element validate (DataSource source) throws IOException {
        try {
            Document rdocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            return validate(rdocument,source);
        } catch (ParserConfigurationException e) {
            IOException ioe = new IOException("Failed to init document builder");
            ioe.initCause(e);
            throw ioe;
        }
    }


    public Element validate (Document rdocument, DataSource source) throws IOException {
        String pdfType = null;
        ValidationResult result = null;
        long before = System.currentTimeMillis();
        try {
            PreflightParser parser = new PreflightParser(source);
            try
            {
                parser.parse();
                PreflightDocument document = parser.getPreflightDocument();
                document.validate();
                pdfType = document.getSpecification().getFname();
                result = document.getResult();
                document.close();
            }
            catch (SyntaxValidationException e)
            {
                result = e.getResult();
            }
        } 
        catch(Exception e) 
        {
            long after = System.currentTimeMillis();
            return generateFailureResponse(rdocument, source.getName(), after-before, pdfType, e);
        }

        long after = System.currentTimeMillis();
        if (result.isValid()) {
            Element preflight = generateResponseSkeleton(rdocument, source.getName(), after-before);
            // valid ?
            Element valid = rdocument.createElement("isValid");
            valid.setAttribute("type", pdfType);
            valid.setTextContent("true");
            preflight.appendChild(valid);
            return preflight;
        } else {
            Element preflight = generateResponseSkeleton(rdocument, source.getName(), after-before);
            // valid ?
            Element valid = rdocument.createElement("isValid");
            valid.setAttribute("type", pdfType);
            valid.setTextContent("false");
            preflight.appendChild(valid);
            // errors list
            List<ValidationError> cleaned = cleanErrorList(result.getErrorsList());
            Element errors = rdocument.createElement("errors");
            errors.setAttribute("count", String.format("%d", cleaned.size()));
            preflight.appendChild(errors);
            Map<ValidationError, Integer> errorsByType = new HashMap<ValidationError, Integer>();
            for (ValidationError validationError : cleaned)
            {
                Integer count = errorsByType.get(validationError);
                if (count==null) {
                    errorsByType.put(validationError, 1);
                } else {
                    errorsByType.put(validationError, count++);
                }
            }
            
            for (Map.Entry<ValidationError,Integer> entry : errorsByType.entrySet())
            {
                Element error = rdocument.createElement("error");
                error.setAttribute("count", String.format("%d",entry.getValue().intValue()));
                Element code = rdocument.createElement("code");
                code.setTextContent(entry.getKey().getErrorCode());
                error.appendChild(code);
                Element detail = rdocument.createElement("details");
                detail.setTextContent(entry.getKey().getDetails());
                error.appendChild(detail);
                errors.appendChild(error);
            }
            return preflight;
        }

    }

    private List<ValidationError> cleanErrorList (List<ValidationError> errors) {
        List<ValidationError> cleaned = new ArrayList<ValidationResult.ValidationError>(errors.size());
        List<String> already = new ArrayList<String>(errors.size());
        for (ValidationError validationError : errors)
        {
            if (!already.contains(validationError.getErrorCode())) {
                // first time
                cleaned.add(validationError);
                already.add(validationError.getErrorCode());
            } // else already found
        }
        return cleaned;
    }

    private Element generateFailureResponse (Document rdocument, String name,long duration, String pdfType, Exception e) {
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

    private Element generateResponseSkeleton (Document rdocument, String name, long duration) {
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

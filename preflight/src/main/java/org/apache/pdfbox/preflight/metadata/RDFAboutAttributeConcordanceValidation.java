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

package org.apache.pdfbox.preflight.metadata;

import java.util.List;

import org.apache.pdfbox.preflight.exception.ValidationException;
import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.schema.XMPSchema;

/**
 * Class which all elements within an rdf:RDF have the same value for their rdf:about attributes
 * 
 * @author Germain Costenobel
 * 
 */
public class RDFAboutAttributeConcordanceValidation
{

    /**
     * 
     * @param metadata
     * @return
     * @throws DifferentRDFAboutException
     * @throws ValidationException
     */
    public void validateRDFAboutAttributes(XMPMetadata metadata) throws ValidationException, DifferentRDFAboutException
    {

        List<XMPSchema> schemas = metadata.getAllSchemas();
        if (schemas.size() == 0)
        {
            throw new ValidationException("Schemas not found in the given metadata representation");
        }
        
        String about = schemas.get(0).getAboutValue();
       
        // rdf:description must have an rdf:about attribute
        for (XMPSchema xmpSchema : schemas)
        {
            // each rdf:Description must have the same rdf:about (or an empty one)
            String schemaAboutValue = xmpSchema.getAboutValue();
            if (!("".equals(schemaAboutValue) || "".equals(about) || about.equals(schemaAboutValue)))
            {
                throw new DifferentRDFAboutException();
            }
            
            if ("".equals(about)) {
                about = schemaAboutValue;
            }
        }

    }

    public static class DifferentRDFAboutException extends Exception
    {

        private static final long serialVersionUID = 1L;

        public DifferentRDFAboutException()
        {
            super("all rdf:about in RDF:rdf must have the same value");
        }
    }
}

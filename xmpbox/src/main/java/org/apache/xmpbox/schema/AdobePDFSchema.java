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

package org.apache.xmpbox.schema;

import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.type.AbstractField;
import org.apache.xmpbox.type.Cardinality;
import org.apache.xmpbox.type.PropertyType;
import org.apache.xmpbox.type.StructuredType;
import org.apache.xmpbox.type.TextType;
import org.apache.xmpbox.type.Types;

/**
 * Representation of Adobe PDF Schema
 * 
 * @author a183132
 * 
 */
@StructuredType(preferedPrefix = "pdf", namespace = "http://ns.adobe.com/pdf/1.3/")
public class AdobePDFSchema extends XMPSchema
{

    @PropertyType(type = Types.Text, card = Cardinality.Simple)
    public static final String KEYWORDS = "Keywords";

    @PropertyType(type = Types.Text, card = Cardinality.Simple)
    public static final String PDF_VERSION = "PDFVersion";

    @PropertyType(type = Types.Text, card = Cardinality.Simple)
    public static final String PRODUCER = "Producer";

    /**
     * Constructor of an Adobe PDF schema with preferred prefix
     * 
     * @param metadata
     *            The metadata to attach this schema
     */
    public AdobePDFSchema(final XMPMetadata metadata)
    {
        super(metadata);
    }

    /**
     * Constructor of an Adobe PDF schema with specified prefix
     * 
     * @param metadata
     *            The metadata to attach this schema
     * @param ownPrefix
     *            The prefix to assign
     */
    public AdobePDFSchema(final XMPMetadata metadata, final String ownPrefix)
    {
        super(metadata, ownPrefix);
    }

    /**
     * Set the PDF keywords
     * 
     * @param value
     *            Value to set
     */
    public void setKeywords(final String value)
    {
        final TextType keywords;
        keywords = createTextType(KEYWORDS, value);
        addProperty(keywords);
    }

    /**
     * Set the PDF keywords
     * 
     * @param keywords
     *            Property to set
     */
    public void setKeywordsProperty(final TextType keywords)
    {
        addProperty(keywords);
    }

    /**
     * Set the PDFVersion
     * 
     * @param value
     *            Value to set
     */
    public void setPDFVersion(final String value)
    {
        final TextType version;
        version = createTextType(PDF_VERSION, value);
        addProperty(version);

    }

    /**
     * Set the PDFVersion
     * 
     * @param version
     *            Property to set
     */
    public void setPDFVersionProperty(final TextType version)
    {
        addProperty(version);
    }

    /**
     * Set the PDFProducer
     * 
     * @param value
     *            Value to set
     */
    public void setProducer(final String value)
    {
        final TextType producer;
        producer = createTextType(PRODUCER, value);
        addProperty(producer);
    }

    /**
     * Set the PDFProducer
     * 
     * @param producer
     *            Property to set
     */
    public void setProducerProperty(final TextType producer)
    {
        addProperty(producer);
    }

    /**
     * Give the PDF Keywords property
     * 
     * @return The property object
     */
    public TextType getKeywordsProperty()
    {
        final AbstractField tmp = getProperty(KEYWORDS);
        if (tmp instanceof TextType)
        {
            return (TextType) tmp;
        }
        return null;
    }

    /**
     * Give the PDF Keywords property value (string)
     * 
     * @return The property value
     */
    public String getKeywords()
    {
        final AbstractField tmp = getProperty(KEYWORDS);
        if (tmp instanceof TextType)
        {
            return ((TextType) tmp).getStringValue();
        }
        return null;
    }

    /**
     * Give the PDFVersion property
     * 
     * @return The property object
     */
    public TextType getPDFVersionProperty()
    {
        final AbstractField tmp = getProperty(PDF_VERSION);
        if (tmp instanceof TextType)
        {
            return (TextType) tmp;
        }
        return null;
    }

    /**
     * Give the PDFVersion property value (string)
     * 
     * @return The property value
     */
    public String getPDFVersion()
    {
        final AbstractField tmp = getProperty(PDF_VERSION);
        if (tmp instanceof TextType)
        {
            return ((TextType) tmp).getStringValue();
        }
        return null;
    }

    /**
     * Give the producer property
     * 
     * @return The property object
     */
    public TextType getProducerProperty()
    {
        final AbstractField tmp = getProperty(PRODUCER);
        if (tmp instanceof TextType)
        {
            return (TextType) tmp;
        }
        return null;
    }

    /**
     * Give the producer property value (string)
     * 
     * @return The property value
     */
    public String getProducer()
    {
        final AbstractField tmp = getProperty(PRODUCER);
        if (tmp instanceof TextType)
        {
            return ((TextType) tmp).getStringValue();
        }
        return null;
    }

}

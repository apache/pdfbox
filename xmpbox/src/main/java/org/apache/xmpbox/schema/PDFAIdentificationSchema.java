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
import org.apache.xmpbox.type.Attribute;
import org.apache.xmpbox.type.BadFieldValueException;
import org.apache.xmpbox.type.Cardinality;
import org.apache.xmpbox.type.IntegerType;
import org.apache.xmpbox.type.PropertyType;
import org.apache.xmpbox.type.StructuredType;
import org.apache.xmpbox.type.TextType;
import org.apache.xmpbox.type.Types;

/**
 * Representation of PDF/A Identification Schema
 * 
 * @author a183132
 * 
 */
@StructuredType(preferedPrefix = "pdfaid", namespace = "http://www.aiim.org/pdfa/ns/id/")
public class PDFAIdentificationSchema extends XMPSchema
{

    @PropertyType(type = Types.Integer, card = Cardinality.Simple)
    public static final String PART = "part";

    @PropertyType(type = Types.Text, card = Cardinality.Simple)
    public static final String AMD = "amd";

    @PropertyType(type = Types.Text, card = Cardinality.Simple)
    public static final String CONFORMANCE = "conformance";

    /*
     * <rdf:Description rdf:about="" xmlns:pdfaid="http://www.aiim.org/pdfa/ns/id/">
     * <pdfaid:conformance>B</pdfaid:conformance> <pdfaid:part>1</pdfaid:part> </rdf:Description>
     */

    /**
     * Constructor of a PDF/A Identification schema
     * 
     * @param metadata
     *            The metadata to attach this schema
     */
    public PDFAIdentificationSchema(XMPMetadata metadata)
    {
        super(metadata);
    }

    public PDFAIdentificationSchema(XMPMetadata metadata, String prefix)
    {
        super(metadata, prefix);
    }

    /**
     * Set the PDFA Version identifier (with string)
     * 
     * @param value
     *            The version Id value to set
     * 
     */
    public void setPartValueWithString(String value)
    {
        IntegerType part = (IntegerType) instanciateSimple(PART, value);
        addProperty(part);
    }

    /**
     * Set the PDFA Version identifier (with an int)
     * 
     * @param value
     *            The version Id value to set
     */
    public void setPartValueWithInt(int value)
    {
        IntegerType part = (IntegerType) instanciateSimple(PART, value);
        addProperty(part);
    }

    /**
     * Set the PDF/A Version identifier (with an int)
     * 
     * @param value
     *            The version Id property to set
     */
    public void setPart(Integer value)
    {
        setPartValueWithInt(value.intValue());
    }

    /**
     * Set the PDF/A Version identifier
     * 
     * @param part
     *            set the PDF/A Version id property
     */
    public void setPartProperty(IntegerType part)
    {
        addProperty(part);
    }

    /**
     * Set the PDF/A amendment identifier
     * 
     * @param value
     *            The amendment identifier value to set
     */
    public void setAmd(String value)
    {
        TextType amd = createTextType(AMD, value);
        addProperty(amd);
    }

    /**
     * Set the PDF/A amendment identifier
     * 
     * @param amd
     *            The amendment identifier property to set
     */
    public void setAmdProperty(TextType amd)
    {
        addProperty(amd);
    }

    /**
     * Set the PDF/A conformance level
     * 
     * @param value
     *            The conformance level value to set
     * @throws BadFieldValueException
     *             If Conformance Value not 'A' or 'B'
     */
    public void setConformance(String value) throws BadFieldValueException
    {
        if (value.equals("A") || value.equals("B"))
        {
            TextType conf = createTextType(CONFORMANCE, value);
            addProperty(conf);

        }
        else
        {
            throw new BadFieldValueException(
                    "The property given not seems to be a PDF/A conformance level (must be A or B)");
        }
    }

    /**
     * Set the PDF/A conformance level
     * 
     * @param conf
     *            The conformance level property to set
     * @throws BadFieldValueException
     *             If Conformance Value not 'A' or 'B'
     */
    public void setConformanceProperty(TextType conf) throws BadFieldValueException
    {
        String value = conf.getStringValue();
        if (value.equals("A") || value.equals("B"))
        {
            addProperty(conf);
        }
        else
        {
            throw new BadFieldValueException(
                    "The property given not seems to be a PDF/A conformance level (must be A or B)");
        }
    }

    /**
     * Give the PDFAVersionId (as an integer)
     * 
     * @return Part value (Integer)
     */
    public Integer getPart()
    {
        AbstractField tmp = getPartProperty();
        if (tmp instanceof IntegerType)
        {
            return ((IntegerType) tmp).getValue();
        }
        else
        {
            for (Attribute attribute : getAllAttributes())
            {
                if (attribute.getName().equals(PART))
                {
                    return Integer.valueOf(attribute.getValue());
                }
            }
            return null;
        }
    }

    /**
     * Give the property corresponding to the PDFA Version id
     * 
     * @return Part property
     */
    public IntegerType getPartProperty()
    {
        AbstractField tmp = getProperty(PART);
        if (tmp instanceof IntegerType)
        {
            return (IntegerType) tmp;
        }
        return null;
    }

    /**
     * Give the PDFAAmendmentId (as an String)
     * 
     * @return Amendment value
     */
    public String getAmendment()
    {
        AbstractField tmp = getProperty(AMD);
        if (tmp instanceof TextType)
        {
            return ((TextType) tmp).getStringValue();
        }
        return null;
    }

    /**
     * Give the property corresponding to the PDFA Amendment id
     * 
     * @return Amendment property
     */
    public TextType getAmdProperty()
    {
        AbstractField tmp = getProperty(AMD);
        if (tmp instanceof TextType)
        {
            return (TextType) tmp;
        }
        return null;
    }

    /**
     * Give the PDFA Amendment Id (as an String)
     * 
     * @return Amendment Value
     */
    public String getAmd()
    {
        TextType tmp = getAmdProperty();
        if (tmp == null)
        {
            for (Attribute attribute : getAllAttributes())
            {
                if (attribute.getName().equals(AMD))
                {
                    return attribute.getValue();
                }
            }
            return null;
        }
        else
        {
            return tmp.getStringValue();
        }
    }

    /**
     * Give the property corresponding to the PDFA Conformance id
     * 
     * @return conformance property
     */
    public TextType getConformanceProperty()
    {
        AbstractField tmp = getProperty(CONFORMANCE);
        if (tmp instanceof TextType)
        {
            return (TextType) tmp;
        }
        return null;
    }

    /**
     * Give the Conformance id
     * 
     * @return conformance id value
     */
    public String getConformance()
    {
        TextType tt = getConformanceProperty();
        if (tt == null)
        {
            for (Attribute attribute : getAllAttributes())
            {
                if (attribute.getName().equals(CONFORMANCE))
                {
                    return attribute.getValue();
                }
            }
            return null;
        }
        else
        {
            return tt.getStringValue();
        }
    }

}

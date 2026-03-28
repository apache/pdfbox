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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
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
    private static final Set<String> VALID_VALUES =
            new HashSet<>(Arrays.asList("A", "B", "U", "e", "f"));

    @PropertyType(type = Types.Integer, card = Cardinality.Simple)
    public static final String PART = "part";

    @PropertyType(type = Types.Text, card = Cardinality.Simple)
    public static final String AMD = "amd";

    @PropertyType(type = Types.Text, card = Cardinality.Simple)
    public static final String CONFORMANCE = "conformance";

    // PDFBOX-6088 https://pdfa.org/future-proofing-xmp-identification-schema/
    @PropertyType(type = Types.Integer, card = Cardinality.Simple)
    public static final String REV = "rev";

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
     * Set the PDF/A Version identifier (with string)
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
     * Set the PDF/A Version identifier (with an int)
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
        setPartValueWithInt(value);
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
     * @param value The conformance level value to set
     * @throws BadFieldValueException If conformance value not 'A', 'B', 'U' (PDF/A-2 and PDF/A-3),
     * 'e', 'f' (PDF/A-4)
     */
    public void setConformance(String value) throws BadFieldValueException
    {
        TextType conf = createTextType(CONFORMANCE, value);
        setConformanceProperty(conf);
    }

    /**
     * Set the PDF/A conformance level
     *
     * @param conf The conformance level property to set
     * @throws BadFieldValueException If conformance value not 'A', 'B', 'U' (PDF/A-2 and PDF/A-3),
     * 'e', 'f' (PDF/A-4)
     */
    public void setConformanceProperty(TextType conf) throws BadFieldValueException
    {
        String value = conf.getStringValue();
        if (VALID_VALUES.contains(value))
        {
            addProperty(conf);
        }
        else
        {
            throw new BadFieldValueException(
                    "The value '" + value + "' isn't a valid PDF/A conformance level (must be A, B, U, e or f)");
        }
    }

    /**
     * Give the PDFAVersionId (as an integer)
     * 
     * @return Part value (Integer) or null if it is missing
     */
    public Integer getPart()
    {
        IntegerType tmp = getPartProperty();
        if (tmp == null)
        {
            return null;
        }
        return tmp.getValue();
    }

    /**
     * Give the property corresponding to the PDF/A Version id
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
     * Give the property corresponding to the PDF/A Amendment id
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
     * Give the PDF/A Amendment Id (as an String)
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
     * Give the property corresponding to the PDF/A Conformance id
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

    /**
     * Set the PDF/A revision (with string)
     *
     * @param value The revision value to set
     *
     */
    public void setRevValueWithString(String value)
    {
        IntegerType rev = (IntegerType) instanciateSimple(REV, value);
        addProperty(rev);
    }

    /**
     * Set the PDF/A revision (with an int)
     *
     * @param value The revision value to set
     */
    public void setRevValueWithInt(int value)
    {
        IntegerType rev = (IntegerType) instanciateSimple(REV, value);
        addProperty(rev);
    }

    /**
     * Set the PDF/A revision identifier (with an int)
     *
     * @param value The revision property to set
     */
    public void setRev(Integer value)
    {
        setRevValueWithInt(value);
    }

    /**
     * Set the PDF/A revision identifier
     *
     * @param rev set the PDF/A revision id property
     */
    public void setRevProperty(IntegerType rev)
    {
        addProperty(rev);
    }

    /**
     * Give the property corresponding to the PDF/A revision
     * 
     * @return revision property
     */
    public IntegerType getRevProperty()
    {
        AbstractField tmp = getProperty(REV);
        if (tmp instanceof IntegerType)
        {
            return (IntegerType) tmp;
        }
        return null;
    }

    /**
     * Give the PDF/A revision (as an integer)
     * 
     * @return revision value (Integer) or null if it is missing
     */
    public Integer getRev()
    {
        IntegerType tmp = getRevProperty();
        if (tmp == null)
        {
            return null;
        }
        return tmp.getValue();
    }
}

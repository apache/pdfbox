/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pdfbox.pdmodel.interactive.form;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSeedValue;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A signature field is a form field that contains a digital signature.
 *
 * @author Ben Litchfield
 * @author Thomas Chojecki
 */
public class PDSignatureField extends PDField
{
    /**
     * Constructor.
     * 
     * @param theAcroForm The form that this field is part of.
     * @param field the PDF object to represent as a field.
     * @param parentNode the parent node of the node to be created
     */
    public PDSignatureField(PDAcroForm theAcroForm, COSDictionary field, PDFieldTreeNode parentNode)
    {
        super(theAcroForm, field, parentNode);
        // dirty hack to avoid npe caused through getWidget() method
        getDictionary().setItem( COSName.TYPE, COSName.ANNOT );
        getDictionary().setName( COSName.SUBTYPE, PDAnnotationWidget.SUB_TYPE);
    }

    /**
     * @see PDField#PDField(PDAcroForm)
     *
     * @param theAcroForm The acroForm for this field.
     * @throws IOException If there is an error while resolving partial name for the signature field
     *         or getting the widget object.
     */
    public PDSignatureField(PDAcroForm theAcroForm) throws IOException
    {
        super( theAcroForm );
        getDictionary().setItem(COSName.FT, COSName.SIG);
        getWidget().setLocked(true);
        getWidget().setPrinted(true);
        setPartialName(generatePartialName());
        getDictionary().setItem( COSName.TYPE, COSName.ANNOT );
        getDictionary().setName( COSName.SUBTYPE, PDAnnotationWidget.SUB_TYPE);
    }
    
    /**
     * Generate a unique name for the signature.
     * @return the signature's unique name
     */
    private String generatePartialName()
    {
        PDAcroForm acroForm = getAcroForm();
        List<PDFieldTreeNode> fields = acroForm.getFields();
        String fieldName = "Signature";
        Set<String> sigNames = new HashSet<String>();
        for ( PDFieldTreeNode field : fields )
        {
            if(field instanceof PDSignatureField)
            {
                sigNames.add(field.getPartialName());
            }
        }
        int i = 1;
        while(sigNames.contains(fieldName+i))
        {
            ++i;
        }
        return fieldName+i;
    }
    
    /**
     * Return a string rep of this object.
     *
     * @return A string rep of this object.
     */
    @Override
    public String toString()
    {
        return "PDSignatureField";
    }
    
    /**
     * Add a signature dictionary to the signature field.
     * 
     * @param value is the PDSignatureField
     */
    public void setSignature(PDSignature value)
    {
        setValue(value);
    }
    
    /**
     * Get the signature dictionary.
     * 
     * @return the signature dictionary
     * 
     */
    public PDSignature getSignature()
    {
        return getValue();
    }

    /**
     * Add a signature dictionary to the signature field.
     * 
     * @param value is the PDSignatureField
     */
    public void setValue(Object value)
    {
        if (value == null)
        {
            getDictionary().removeItem(COSName.V);
        }
        else if (value instanceof PDSignature)
        {
            getDictionary().setItem(COSName.V, (PDSignature)value);
        }
    }
    
    /**
     * Get the signature dictionary.
     * 
     * @return the signature dictionary
     * 
     */
    public PDSignature getValue()
    {
        COSBase dictionary = getDictionary().getDictionaryObject(COSName.V);
        if (dictionary == null)
        {
            return null;
        }
        return new PDSignature((COSDictionary)dictionary);
    }

    /**
     * <p>(Optional; PDF 1.5) A seed value dictionary containing information
     * that constrains the properties of a signature that is applied to the
     * field.</p>
     *
     * @return the seed value dictionary as PDSeedValue
     */
    public PDSeedValue getSeedValue()
    {
        COSDictionary dict = (COSDictionary)getDictionary().getDictionaryObject(COSName.SV);
        PDSeedValue sv = null;
        if (dict != null)
        {
            sv = new PDSeedValue(dict);
        }
        return sv;
    }

    /**
     * <p>(Optional; PDF 1.) A seed value dictionary containing information
     * that constrains the properties of a signature that is applied to the
     * field.</p>
     *
     * @param sv is the seed value dictionary as PDSeedValue
     */
    public void setSeedValue(PDSeedValue sv)
    {
        if (sv != null)
        {
            getDictionary().setItem(COSName.SV, sv.getCOSObject());
        }
    }
    
    @Override
    public Object getDefaultValue()
    {
        // Signature fields don't support the "DV" entry.
        return null;
    }

    @Override
    public void setDefaultValue(Object value)
    {
        // Signature fields don't support the "DV" entry.
        throw new RuntimeException( "Signature fields don't support the \"DV\" entry." );
    }

}

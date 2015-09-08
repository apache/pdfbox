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
package org.apache.pdfbox_ai2.pdmodel.interactive.form;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.apache.pdfbox_ai2.cos.COSBase;
import org.apache.pdfbox_ai2.cos.COSDictionary;
import org.apache.pdfbox_ai2.cos.COSName;
import org.apache.pdfbox_ai2.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox_ai2.pdmodel.interactive.digitalsignature.PDSeedValue;
import org.apache.pdfbox_ai2.pdmodel.interactive.digitalsignature.PDSignature;

/**
 * A signature field is a form field that contains a digital signature.
 *
 * @author Ben Litchfield
 * @author Thomas Chojecki
 */
public class PDSignatureField extends PDTerminalField
{
    /**
     * @see PDTerminalField#PDTerminalField(PDAcroForm)
     *
     * @param acroForm The acroForm for this field.
     * @throws IOException If there is an error while resolving partial name for the signature field
     *         or getting the widget object.
     */
    public PDSignatureField(PDAcroForm acroForm) throws IOException
    {
        super(acroForm);
        dictionary.setItem(COSName.FT, COSName.SIG);
        getWidgets().get(0).setLocked(true);
        getWidgets().get(0).setPrinted(true);
        setPartialName(generatePartialName());
    }
    
    /**
     * Constructor.
     * 
     * @param acroForm The form that this field is part of.
     * @param field the PDF object to represent as a field.
     * @param parent the parent node of the node to be created
     */
    PDSignatureField(PDAcroForm acroForm, COSDictionary field, PDNonTerminalField parent)
    {
        super(acroForm, field, parent);
    }
    
    /**
     * Generate a unique name for the signature.
     * 
     * @return the signature's unique name
     */
    private String generatePartialName()
    {
        String fieldName = "Signature";
        Set<String> sigNames = new HashSet<String>();
        // fixme: this ignores non-terminal fields, so will miss any descendant signatures
        for (PDField field : acroForm.getFields())
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
     * Add a signature dictionary to the signature field.
     * 
     * @param value is the PDSignatureField
     * @deprecated Use {@link #setValue(PDSignature)} instead.
     */
    @Deprecated
    public void setSignature(PDSignature value) throws IOException
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
     * Sets the value of this field to be the given signature.
     * 
     * @param value is the PDSignatureField
     */
    public void setValue(PDSignature value) throws IOException
    {
        dictionary.setItem(COSName.V, value);
        applyChange();
    }

    /**
     * Sets the default value of this field to be the given signature.
     *
     * @param value is the PDSignatureField
     */
    public void setDefaultValue(PDSignature value) throws IOException
    {
        dictionary.setItem(COSName.DV, value);
    }

    /**
     * Returns the signature contained in this field.
     * 
     * @return A signature dictionary.
     */
    public PDSignature getValue()
    {
        COSBase value = dictionary.getDictionaryObject(COSName.V);
        if (value == null)
        {
            return null;
        }
        return new PDSignature((COSDictionary)value);
    }

    /**
     * Returns the default value, if any.
     *
     * @return A signature dictionary.
     */
    public PDSignature getDefaultValue()
    {
        COSBase value = dictionary.getDictionaryObject(COSName.DV);
        if (value == null)
        {
            return null;
        }
        return new PDSignature((COSDictionary)value);
    }
    
    @Override
    public String getValueAsString()
    {
        PDSignature signature = getValue();
        return signature != null ? signature.toString() : "";
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
        COSDictionary dict = (COSDictionary) dictionary.getDictionaryObject(COSName.SV);
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
            dictionary.setItem(COSName.SV, sv);
        }
    }

    @Override
    void constructAppearances() throws IOException
    {
        PDAnnotationWidget widget = this.getWidgets().get(0);
        if (widget != null)
        {
            // check if the signature is visible
            if (widget.getRectangle() == null ||
                widget.getRectangle().getHeight() == 0 && widget.getRectangle().getWidth() == 0 ||
                widget.isNoView() ||  widget.isHidden())
            {
                return;
            }

            // TODO: implement appearance generation for signatures
            throw new UnsupportedOperationException("not implemented");
        }
    }
}

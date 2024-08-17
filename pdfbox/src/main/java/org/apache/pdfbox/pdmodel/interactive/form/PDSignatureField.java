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

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSeedValue;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;

/**
 * A signature field is a form field that contains a digital signature.
 *
 * @author Ben Litchfield
 * @author Thomas Chojecki
 */
public class PDSignatureField extends PDTerminalField
{
    private static final Logger LOG = LogManager.getLogger(PDSignatureField.class);
    
    /**
     * @see PDTerminalField#PDTerminalField(PDAcroForm)
     *
     * @param acroForm The acroForm for this field.
     */
    public PDSignatureField(PDAcroForm acroForm)
    {
        super(acroForm);
        getCOSObject().setItem(COSName.FT, COSName.SIG);
        PDAnnotationWidget firstWidget = getWidgets().get(0);
        firstWidget.setLocked(true);
        firstWidget.setPrinted(true);
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
        Set<String> nameSet = new HashSet<>();
        getAcroForm().getFieldTree().forEach(field -> nameSet.add(field.getPartialName()));
        int i = 1;
        while (nameSet.contains(fieldName + i))
        {
            ++i;
        }
        return fieldName+i;
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
     * 
     * @throws IOException if the new value could not be applied
     */
    public void setValue(PDSignature value) throws IOException
    {
        getCOSObject().setItem(COSName.V, value);
        applyChange();
    }
    
    /**
     * <b>This will throw an UnsupportedOperationException if used as the signature fields
     * value can't be set using a String</b>
     * 
     * @param value the plain text value.
     * 
     * @throws UnsupportedOperationException in all cases!
     */
    @Override
    public void setValue(String value)
    {
        throw new UnsupportedOperationException("Signature fields don't support setting the value as String "
                + "- use setValue(PDSignature value) instead");
    }
    

    /**
     * Sets the default value of this field to be the given signature.
     *
     * @param value is the PDSignatureField
     */
    public void setDefaultValue(PDSignature value)
    {
        getCOSObject().setItem(COSName.DV, value);
    }

    /**
     * Returns the signature contained in this field.
     * 
     * @return A signature dictionary.
     */
    public PDSignature getValue()
    {
        COSDictionary value = getCOSObject().getCOSDictionary(COSName.V);
        return value != null ? new PDSignature(value) : null;
    }

    /**
     * Returns the default value, if any.
     *
     * @return A signature dictionary.
     */
    public PDSignature getDefaultValue()
    {
        COSDictionary value = getCOSObject().getCOSDictionary(COSName.DV);
        return value != null ? new PDSignature(value) : null;
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
        COSDictionary dict = getCOSObject().getCOSDictionary(COSName.SV);
        return dict != null ? new PDSeedValue(dict) : null;
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
            getCOSObject().setItem(COSName.SV, sv);
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
                Float.compare(widget.getRectangle().getHeight(), 0) == 0 && Float.compare(widget.getRectangle().getWidth(), 0) == 0 ||
                widget.isNoView() ||  widget.isHidden())
            {
                return;
            }

            // TODO: implement appearance generation for signatures (PDFBOX-3524)
            LOG.warn("Appearance generation for signature fields not implemented here. "
                    + "You need to generate/update that manually, see the "
                    + "CreateVisibleSignature*.java files in the examples subproject "
                    + "of the source code download");
        }
    }
}

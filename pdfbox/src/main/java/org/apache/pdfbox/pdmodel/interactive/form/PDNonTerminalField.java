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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.common.COSArrayList;
import org.apache.pdfbox.pdmodel.common.COSObjectable;
import org.apache.pdfbox.pdmodel.fdf.FDFField;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;

/**
 * A non terminal field in an interactive form.
 * 
 * A non terminal field is a node in the fields tree node whose descendants
 * are fields. 
 * 
 * The attributes such as FT (field type) or V (field value) do not logically
 * belong to the non terminal field but are inheritable attributes
 * for descendant terminal fields.
 */
public class PDNonTerminalField extends PDField
{
    /**
     * Constructor.
     * 
     * @param acroForm The form that this field is part of.
     */
    public PDNonTerminalField(PDAcroForm acroForm)
    {
        super(acroForm);
    }

    /**
     * Constructor.
     * 
     * @param acroForm The form that this field is part of.
     * @param field the PDF object to represent as a field.
     * @param parent the parent node of the node to be created
     */
    PDNonTerminalField(PDAcroForm acroForm, COSDictionary field, PDNonTerminalField parent)
    {
        super(acroForm, field, parent);
    }
    
    @Override
    public int getFieldFlags()
    {
        int retval = 0;
        COSInteger ff = (COSInteger) getCOSObject().getDictionaryObject(COSName.FF);
        if (ff != null)
        {
            retval = ff.intValue();
        }
        // There is no need to look up the parent hierarchy within a non terminal field
        return retval;
    }

    @Override
    void importFDF(FDFField fdfField) throws IOException
    {
        super.importFDF(fdfField);
        
        List<FDFField> fdfKids = fdfField.getKids();
        List<PDField> children = getChildren();
        for (int i = 0; fdfKids != null && i < fdfKids.size(); i++)
        {
            for (COSObjectable pdKid : children)
            {
                if (pdKid instanceof PDField)
                {
                    PDField pdChild = (PDField) pdKid;
                    FDFField fdfChild = fdfKids.get(i);
                    String fdfName = fdfChild.getPartialFieldName();
                    if (fdfName != null && fdfName.equals(pdChild.getPartialName()))
                    {
                        pdChild.importFDF(fdfChild);
                    }
                }
            }
        }
    }
    
    @Override
    FDFField exportFDF() throws IOException
    {
        FDFField fdfField = new FDFField();
        fdfField.setPartialFieldName(getPartialName());
        fdfField.setValue(getValue());

        List<PDField> children = getChildren();
        List<FDFField> fdfChildren = new ArrayList<>();
        for (PDField child : children)
        {
            fdfChildren.add(child.exportFDF());
        }
        fdfField.setKids(fdfChildren);
        
        return fdfField;
    }
    
    /**
     * Returns this field's children. These may be either terminal or non-terminal fields.
     *
     * @return the list of child fields. Be aware that this list is <i>not</i> backed by the
     * children of the field, so adding or deleting has no effect on the PDF document until you call
     * {@link #setChildren(java.util.List) setChildren()} with the modified list.
     */
    public List<PDField> getChildren()
    {
        //TODO: why not return a COSArrayList like in PDPage.getAnnotations() ?
 
        List<PDField> children = new ArrayList<>();
        COSArray kids = (COSArray)getCOSObject().getDictionaryObject(COSName.KIDS);
        for (int i = 0; i < kids.size(); i++)
        {
            COSBase kid = kids.getObject(i);
            if (kid instanceof COSDictionary)
            {
                PDField field = PDField.fromDictionary(getAcroForm(), (COSDictionary) kid, this);
                if (field != null)
                {
                    children.add(field);
                }
            }
        }
        return children;
    }
    
    /**
     * Sets the child fields.
     *
     * @param children The list of child fields.
     */
    public void setChildren(List<PDField> children)
    {
        COSArray kidsArray = COSArrayList.converterToCOSArray(children);
        getCOSObject().setItem(COSName.KIDS, kidsArray);
    }

    /**
     * {@inheritDoc}
     *
     * <p><b>Note:</b> while non-terminal fields <b>do</b> inherit field values, this method returns
     * the local value, without inheritance.
     */
    @Override
    public String getFieldType()
    {
        return getCOSObject().getNameAsString(COSName.FT);
    }

    /**
     * {@inheritDoc}
     *
     * <p><b>Note:</b> while non-terminal fields <b>do</b> inherit field values, this method returns
     * the local value, without inheritance.
     */
    public COSBase getValue()
    {
        return getCOSObject().getDictionaryObject(COSName.V);
    }

    /**
     * {@inheritDoc}
     *
     * <p><b>Note:</b> while non-terminal fields <b>do</b> inherit field values, this method returns
     * the local value, without inheritance.
     */
    @Override
    public String getValueAsString()
    {
        COSBase fieldValue = getCOSObject().getDictionaryObject(COSName.V);
        return fieldValue != null ? fieldValue.toString() : "";
    }

    /**
     * Sets the value of this field. This may be of any kind which is valid for this field's
     * children.
     *
     * <p><b>Note:</b> while non-terminal fields <b>do</b> inherit field values, this method returns
     * the local value, without inheritance.
     * @param object
     * @throws java.io.IOException
     */
    public void setValue(COSBase object) throws IOException
    {
        getCOSObject().setItem(COSName.V, object);
        // todo: propagate change event to children?
        // todo: construct appearances of children?
    }
    
   /**
     * Sets the plain text value of this field.
     * 
     * @param value Plain text
     * @throws IOException if the value could not be set
     */
    @Override
    public void setValue(String value) throws IOException
    {
        getCOSObject().setString(COSName.V, value);
        // todo: propagate change event to children?
        // todo: construct appearances of children?
    }

    /**
     * Returns the default value of this field. This may be of any kind which is valid for this field's
     * children.
     *
     * <p><b>Note:</b> while non-terminal fields <b>do</b> inherit field values, this method returns
     * the local value, without inheritance.
     */
    public COSBase getDefaultValue()
    {
        return getCOSObject().getDictionaryObject(COSName.DV);
    }

    /**
     * Sets the default of this field. This may be of any kind which is valid for this field's
     * children.
     *
     * <p><b>Note:</b> while non-terminal fields <b>do</b> inherit field values, this method returns
     * the local value, without inheritance.
     * @param value
     */
    public void setDefaultValue(COSBase value)
    {
        getCOSObject().setItem(COSName.V, value);
    }
    
    @Override
    public List<PDAnnotationWidget> getWidgets()
    {
        //TODO shouldn't we return a non modifiable list?
        return Collections.emptyList();
    }
}

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
import java.util.List;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.common.COSArrayList;
import org.apache.pdfbox.pdmodel.common.COSObjectable;
import org.apache.pdfbox.pdmodel.common.PDTextStream;
import org.apache.pdfbox.pdmodel.fdf.FDFField;
import org.apache.pdfbox.pdmodel.interactive.action.PDFormFieldAdditionalActions;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.util.BitFlagHelper;

/**
 * This is the superclass for a Field element in a PDF. Based on the COS object model from PDFBox.
 * 
 * @author sug
 * 
 */
public abstract class PDField implements COSObjectable
{
    /**
     * A Ff flag.
     */
    public static final int FLAG_READ_ONLY = 1;
    /**
     * A Ff flag.
     */
    public static final int FLAG_REQUIRED = 1 << 1;
    /**
     * A Ff flag.
     */
    public static final int FLAG_NO_EXPORT = 1 << 2;

    private PDAcroForm acroForm;

    private COSDictionary dictionary;

    /**
     * Constructor.
     * 
     * @param theAcroForm The form that this field is part of.
     */
    public PDField(PDAcroForm theAcroForm)
    {
        acroForm = theAcroForm;
        dictionary = new COSDictionary();
        // no required fields in base field class
    }

    /**
     * Creates a COSField from a COSDictionary, expected to be a correct object definition for a field in PDF.
     * 
     * @param theAcroForm The form that this field is part of.
     * @param field the PDF objet to represent as a field.
     */
    public PDField(PDAcroForm theAcroForm, COSDictionary field)
    {
        acroForm = theAcroForm;
        dictionary = field;
    }

    /**
     * Returns the partial name of the field.
     * 
     * @return the name of the field
     */
    public String getPartialName()
    {
        return getDictionary().getString(COSName.T);
    }

    /**
     * This will set the partial name of the field.
     * 
     * @param name The new name for the field.
     */
    public void setPartialName(String name)
    {
        getDictionary().setString(COSName.T, name);
    }

    /**
     * Returns the fully qualified name of the field, which is a concatenation of the names of all the parents fields.
     * 
     * @return the name of the field
     * 
     * @throws IOException If there is an error generating the fully qualified name.
     */
    public String getFullyQualifiedName() throws IOException
    {
        PDField parent = getParent();
        String parentName = null;
        if (parent != null)
        {
            parentName = parent.getFullyQualifiedName();
        }
        String finalName = getPartialName();
        if (parentName != null)
        {
            finalName = parentName + "." + finalName;
        }
        return finalName;
    }

    /**
     * Gets the alternate name of the field.
     * 
     * @return the alternate name of the field
     */
    public String getAlternateFieldName()
    {
        return this.getDictionary().getString(COSName.TU);
    }

    /**
     * This will set the alternate name of the field.
     * 
     * @param alternateFieldName the alternate name of the field
     */
    public void setAlternateFieldName(String alternateFieldName)
    {
        this.getDictionary().setString(COSName.TU, alternateFieldName);
    }

    /**
     * Get the FT entry of the field. This is a read only field and is set depending on the actual type. The field type
     * is an inheritable attribute. This method will return only the direct value on this object. Use the findFieldType
     * for an upward recursive search.
     * 
     * @return The Field type.
     * 
     * @see PDField#findFieldType()
     */
    public String getFieldType()
    {
        return getDictionary().getNameAsString(COSName.FT);
    }

    /**
     * Find the field type and optionally do a recursive upward search. Sometimes the fieldtype will be specified on the
     * parent instead of the direct object. This will look at this object for the field type, if none is specified then
     * it will look to the parent if there is a parent. If there is no parent and no field type has been found then this
     * will return null.
     * 
     * @return The field type or null if none was found.
     */
    public String findFieldType()
    {
        return findFieldType(getDictionary());
    }

    private String findFieldType(COSDictionary dic)
    {
        String retval = dic.getNameAsString(COSName.FT);
        if (retval == null)
        {
            COSDictionary parent = (COSDictionary) dic.getDictionaryObject(COSName.PARENT, COSName.P);
            if (parent != null)
            {
                retval = findFieldType(parent);
            }
        }
        return retval;

    }

    /**
     * setValue sets the fields value to a given string.
     * 
     * @param value the string value
     * 
     * @throws IOException If there is an error creating the appearance stream.
     */
    public abstract void setValue(String value) throws IOException;

    /**
     * getValue gets the fields value to as a string.
     * 
     * @return The string value of this field.
     * 
     * @throws IOException If there is an error getting the value.
     */
    public abstract String getValue() throws IOException;

    /**
     * sets the field to be read-only.
     * 
     * @param readonly The new flag for readonly.
     */
    public void setReadonly(boolean readonly)
    {
        BitFlagHelper.setFlag(getDictionary(), COSName.FF, FLAG_READ_ONLY, readonly);
    }

    /**
     * 
     * @return true if the field is readonly
     */
    public boolean isReadonly()
    {
        return BitFlagHelper.getFlag(getDictionary(), COSName.FF, FLAG_READ_ONLY);
    }

    /**
     * sets the field to be required.
     * 
     * @param required The new flag for required.
     */
    public void setRequired(boolean required)
    {
        BitFlagHelper.setFlag(getDictionary(), COSName.FF, FLAG_REQUIRED, required);
    }

    /**
     * 
     * @return true if the field is required
     */
    public boolean isRequired()
    {
        return BitFlagHelper.getFlag(getDictionary(), COSName.FF, FLAG_REQUIRED);
    }

    /**
     * sets the field to be not exported..
     * 
     * @param noExport The new flag for noExport.
     */
    public void setNoExport(boolean noExport)
    {
        BitFlagHelper.setFlag(getDictionary(), COSName.FF, FLAG_NO_EXPORT, noExport);
    }

    /**
     * 
     * @return true if the field is not to be exported.
     */
    public boolean isNoExport()
    {
        return BitFlagHelper.getFlag(getDictionary(), COSName.FF, FLAG_NO_EXPORT);
    }

    /**
     * This will get the flags for this field.
     * 
     * @return flags The set of flags.
     */
    public int getFieldFlags()
    {
        int retval = 0;
        COSInteger ff = (COSInteger) getDictionary().getDictionaryObject(COSName.FF);
        if (ff != null)
        {
            retval = ff.intValue();
        }
        return retval;
    }

    /**
     * This will set the flags for this field.
     * 
     * @param flags The new flags.
     */
    public void setFieldFlags(int flags)
    {
        getDictionary().setInt(COSName.FF, flags);
    }

    /**
     * This will import a fdf field from a fdf document.
     * 
     * @param fdfField The fdf field to import.
     * 
     * @throws IOException If there is an error importing the data for this field.
     */
    public void importFDF(FDFField fdfField) throws IOException
    {
        Object fieldValue = fdfField.getValue();
        int fieldFlags = getFieldFlags();

        if (fieldValue != null)
        {
            if (fieldValue instanceof String)
            {
                setValue((String) fieldValue);
            }
            else if (fieldValue instanceof PDTextStream)
            {
                setValue(((PDTextStream) fieldValue).getAsString());
            }
            else
            {
                throw new IOException("Unknown field type:" + fieldValue.getClass().getName());
            }
        }
        Integer ff = fdfField.getFieldFlags();
        if (ff != null)
        {
            setFieldFlags(ff.intValue());
        }
        else
        {
            // these are suppose to be ignored if the Ff is set.
            Integer setFf = fdfField.getSetFieldFlags();

            if (setFf != null)
            {
                int setFfInt = setFf.intValue();
                fieldFlags = fieldFlags | setFfInt;
                setFieldFlags(fieldFlags);
            }

            Integer clrFf = fdfField.getClearFieldFlags();
            if (clrFf != null)
            {
                // we have to clear the bits of the document fields for every bit that is
                // set in this field.
                //
                // Example:
                // docFf = 1011
                // clrFf = 1101
                // clrFfValue = 0010;
                // newValue = 1011 & 0010 which is 0010
                int clrFfValue = clrFf.intValue();
                clrFfValue ^= 0xFFFFFFFF;
                fieldFlags = fieldFlags & clrFfValue;
                setFieldFlags(fieldFlags);
            }
        }

        PDAnnotationWidget widget = getWidget();
        if (widget != null)
        {
            int annotFlags = widget.getAnnotationFlags();
            Integer f = fdfField.getWidgetFieldFlags();
            if (f != null && widget != null)
            {
                widget.setAnnotationFlags(f.intValue());
            }
            else
            {
                // these are suppose to be ignored if the F is set.
                Integer setF = fdfField.getSetWidgetFieldFlags();
                if (setF != null)
                {
                    annotFlags = annotFlags | setF.intValue();
                    widget.setAnnotationFlags(annotFlags);
                }

                Integer clrF = fdfField.getClearWidgetFieldFlags();
                if (clrF != null)
                {
                    // we have to clear the bits of the document fields for every bit that is
                    // set in this field.
                    //
                    // Example:
                    // docF = 1011
                    // clrF = 1101
                    // clrFValue = 0010;
                    // newValue = 1011 & 0010 which is 0010
                    int clrFValue = clrF.intValue();
                    clrFValue ^= 0xFFFFFFFFL;
                    annotFlags = annotFlags & clrFValue;
                    widget.setAnnotationFlags(annotFlags);
                }
            }
        }
        List<FDFField> fdfKids = fdfField.getKids();
        List<COSObjectable> pdKids = getKids();
        for (int i = 0; fdfKids != null && i < fdfKids.size(); i++)
        {
            FDFField fdfChild = fdfKids.get(i);
            String fdfName = fdfChild.getPartialFieldName();
            for (int j = 0; j < pdKids.size(); j++)
            {
                Object pdChildObj = pdKids.get(j);
                if (pdChildObj instanceof PDField)
                {
                    PDField pdChild = (PDField) pdChildObj;
                    if (fdfName != null && fdfName.equals(pdChild.getPartialName()))
                    {
                        pdChild.importFDF(fdfChild);
                    }
                }
            }
        }
    }

    /**
     * This will get the single associated widget that is part of this field. This occurs when the Widget is embedded in
     * the fields dictionary. Sometimes there are multiple sub widgets associated with this field, in which case you
     * want to use getKids(). If the kids entry is specified, then the first entry in that list will be returned.
     * 
     * @return The widget that is associated with this field.
     * @throws IOException If there is an error getting the widget object.
     */
    public PDAnnotationWidget getWidget() throws IOException
    {
        PDAnnotationWidget retval = null;
        List<COSObjectable> kids = getKids();
        if (kids == null)
        {
            retval = new PDAnnotationWidget(getDictionary());
        }
        else if (kids.size() > 0)
        {
            Object firstKid = kids.get(0);
            if (firstKid instanceof PDAnnotationWidget)
            {
                retval = (PDAnnotationWidget) firstKid;
            }
            else
            {
                retval = ((PDField) firstKid).getWidget();
            }
        }
        else
        {
            retval = null;
        }
        return retval;
    }

    /**
     * Get the parent field to this field, or null if none exists.
     * 
     * @return The parent field.
     * 
     * @throws IOException If there is an error creating the parent field.
     */
    public PDField getParent() throws IOException
    {
        PDField parent = null;
        COSDictionary parentDic = (COSDictionary) getDictionary().getDictionaryObject(COSName.PARENT, COSName.P);
        if (parentDic != null)
        {
            parent = PDFieldFactory.createField(getAcroForm(), parentDic);
        }
        return parent;
    }

    /**
     * Set the parent of this field.
     * 
     * @param parent The parent to this field.
     */
    public void setParent(PDField parent)
    {
        getDictionary().setItem("Parent", parent);
    }

    /**
     * This will find one of the child elements. The name array are the components of the name to search down the tree
     * of names. The nameIndex is where to start in that array. This method is called recursively until it finds the end
     * point based on the name array.
     * 
     * @param name An array that picks the path to the field.
     * @param nameIndex The index into the array.
     * @return The field at the endpoint or null if none is found.
     * @throws IOException If there is an error creating the field.
     */
    public PDField findKid(String[] name, int nameIndex) throws IOException
    {
        PDField retval = null;
        COSArray kids = (COSArray) getDictionary().getDictionaryObject(COSName.KIDS);
        if (kids != null)
        {
            for (int i = 0; retval == null && i < kids.size(); i++)
            {
                COSDictionary kidDictionary = (COSDictionary) kids.getObject(i);
                if (name[nameIndex].equals(kidDictionary.getString("T")))
                {
                    retval = PDFieldFactory.createField(acroForm, kidDictionary);
                    if (name.length > nameIndex + 1)
                    {
                        retval = retval.findKid(name, nameIndex + 1);
                    }
                }
            }
        }
        return retval;
    }

    /**
     * This will get all the kids of this field. The values in the list will either be PDWidget or PDField. Normally
     * they will be PDWidget objects unless this is a non-terminal field and they will be child PDField objects.
     * 
     * @return A list of either PDWidget or PDField objects.
     * @throws IOException If there is an error retrieving the kids.
     */
    public List<COSObjectable> getKids() throws IOException
    {
        List<COSObjectable> retval = null;
        COSArray kids = (COSArray) getDictionary().getDictionaryObject(COSName.KIDS);
        if (kids != null)
        {
            List<COSObjectable> kidsList = new ArrayList<COSObjectable>();
            for (int i = 0; i < kids.size(); i++)
            {
                COSDictionary kidDictionary = (COSDictionary) kids.getObject(i);
                if (kidDictionary == null)
                {
                    continue;
                }
                COSDictionary parent = (COSDictionary) kidDictionary.getDictionaryObject(COSName.PARENT, COSName.P);
                if (kidDictionary.getDictionaryObject(COSName.FT) != null
                        || (parent != null && parent.getDictionaryObject(COSName.FT) != null))
                {
                    kidsList.add(PDFieldFactory.createField(acroForm, kidDictionary));
                }
                else if ("Widget".equals(kidDictionary.getNameAsString(COSName.SUBTYPE)))
                {
                    kidsList.add(new PDAnnotationWidget(kidDictionary));
                }
                else
                {
                    //
                    kidsList.add(PDFieldFactory.createField(acroForm, kidDictionary));
                }
            }
            retval = new COSArrayList<COSObjectable>(kidsList, kids);
        }
        return retval;
    }

    /**
     * This will set the list of kids.
     * 
     * @param kids The list of child widgets.
     */
    public void setKids(List<COSObjectable> kids)
    {
        COSArray kidsArray = COSArrayList.converterToCOSArray(kids);
        getDictionary().setItem(COSName.KIDS, kidsArray);
    }

    /**
     * This will return a string representation of this field.
     * 
     * @return A string representation of this field.
     */
    @Override
    public String toString()
    {
        return "" + getDictionary().getDictionaryObject(COSName.V);
    }

    /**
     * This will get the acroform that this field is part of.
     * 
     * @return The form this field is on.
     */
    public PDAcroForm getAcroForm()
    {
        return acroForm;
    }

    /**
     * This will set the form this field is on.
     * 
     * @param value The new form to use.
     */
    public void setAcroForm(PDAcroForm value)
    {
        acroForm = value;
    }

    /**
     * This will get the dictionary associated with this field.
     * 
     * @return The dictionary that this class wraps.
     */
    public COSDictionary getDictionary()
    {
        return dictionary;
    }

    /**
     * Convert this standard java object to a COS object.
     * 
     * @return The cos object that matches this Java object.
     */
    public COSBase getCOSObject()
    {
        return dictionary;
    }

    /**
     * Get the additional actions for this field. This will return null if there are no additional actions for this
     * field.
     * 
     * @return The actions of the field.
     */
    public PDFormFieldAdditionalActions getActions()
    {
        COSDictionary aa = (COSDictionary) dictionary.getDictionaryObject(COSName.AA);
        PDFormFieldAdditionalActions retval = null;
        if (aa != null)
        {
            retval = new PDFormFieldAdditionalActions(aa);
        }
        return retval;
    }

    /**
     * Set the actions of the field.
     * 
     * @param actions The field actions.
     */
    public void setActions(PDFormFieldAdditionalActions actions)
    {
        dictionary.setItem(COSName.AA, actions);
    }
}

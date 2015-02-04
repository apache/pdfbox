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
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.common.COSArrayList;
import org.apache.pdfbox.pdmodel.common.COSObjectable;
import org.apache.pdfbox.pdmodel.common.PDTextStream;
import org.apache.pdfbox.pdmodel.fdf.FDFField;
import org.apache.pdfbox.pdmodel.interactive.action.PDFormFieldAdditionalActions;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;

/**
 * A field in an interactive form.
 *
 * @author Andreas Lehmkühler
 * 
 */
public abstract class PDFieldTreeNode implements COSObjectable
{
    /**
     * Ff flags.
     */
    private static final int FLAG_READ_ONLY = 1;
    private static final int FLAG_REQUIRED = 1 << 1;
    private static final int FLAG_NO_EXPORT = 1 << 2;

    /**
     * Field type Text.
     */
    private static final String FIELD_TYPE_TEXT = "Tx";
    /**
     * Field type Button.
     */
    private static final String FIELD_TYPE_BUTTON = "Btn";
    /**
     * Field type Button.
     */
    private static final String FIELD_TYPE_CHOICE = "Ch";
    /**
     * Field type Button.
     */
    private static final String FIELD_TYPE_SIGNATURE = "Sig";

    private PDAcroForm acroForm;

    private COSDictionary dictionary;

    private PDFieldTreeNode parent = null;

    /**
     * Constructor.
     * 
     * @param theAcroForm The form that this field is part of.
     */
    protected PDFieldTreeNode(PDAcroForm theAcroForm)
    {
        this(theAcroForm, new COSDictionary(), null);
    }

    /**
     * Constructor.
     * 
     * @param theAcroForm The form that this field is part of.
     * @param field the PDF object to represent as a field.
     * @param parentNode the parent node of the node to be created
     */
    protected PDFieldTreeNode(PDAcroForm theAcroForm, COSDictionary field, PDFieldTreeNode parentNode)
    {
        acroForm = theAcroForm;
        dictionary = field;
        parent = parentNode;
    }

    /**
     * Returns the node in the field tree from which a specific attribute might be inherited.
     *
     * @param field the field from which to look for the attribute
     * @param key the key to look for
     * @return PDFieldTreeNode the node from which the attribute will be inherited or null
     */    
    public PDFieldTreeNode getInheritableAttributesNode(PDFieldTreeNode field, COSName key)
    {
        if (field.getDictionary().containsKey(key))
        {
            return field;
        }
        else
        {
            PDFieldTreeNode parentField = field.getParent();
            if (parentField != null)
            {
                getInheritableAttributesNode(parentField, key);
            }
        }
        return null;
    }    
    
    /**
     * Returns the given attribute, inheriting from parent nodes if necessary.
     *
     * @param key the key to look up
     * @return COS value for the given key
     */
    protected COSBase getInheritableAttribute(COSName key)
    {
        PDFieldTreeNode attributesNode = getInheritableAttributesNode(this,key);
        if (attributesNode != null)
        {
            return attributesNode.getDictionary().getDictionaryObject(key);
        }
        return null;
    }    
    
    /**
     * Sets the given attribute, inheriting from parent nodes if necessary.
     *
     * @param key the key to look up
     * @param value the new attributes value
     */
    protected void setInheritableAttribute(COSName key, COSBase value)
    {
        if (value == null)
        {
            removeInheritableAttribute(key);
        } 
        else 
        {
            PDFieldTreeNode attributesNode = getInheritableAttributesNode(this,key);
            if (attributesNode != null)
            {
                attributesNode.getDictionary().setItem(key, value);
            } 
            else
            {
                getDictionary().setItem(key, value);
            }
        }
    }  
    
    /**
     * Removes the given attribute, inheriting from parent nodes if necessary.
     *
     * @param key the key to look up
     */
    protected void removeInheritableAttribute(COSName key)
    {
        PDFieldTreeNode attributesNode = getInheritableAttributesNode(this,key);
        if (attributesNode != null)
        {
            attributesNode.getDictionary().removeItem(key);
        }
    }      
    
    /**
     * Get a text as text stream.
     * 
     * Some dictionary entries allow either a text or a text stream.
     * 
     * @param cosBaseEntry the potential text or text stream
     * @return the text stream
     * @throws IOException if the field dictionary entry is not a text type
     */
    protected PDTextStream getAsTextStream(COSBase cosBaseEntry) throws IOException
    {
        if (cosBaseEntry == null)
        {
            return null;
        }
        else
        {
            PDTextStream textStream = PDTextStream.createTextStream(cosBaseEntry);
            // This will happen if the entry was not a COSString or COSStream
            if (textStream == null)
            {
                throw new IOException("Invalid field value. Unexpected type " + cosBaseEntry.getClass().getName());
            }
            else
            {
                return textStream;
            }
        }
    }
    
    
    /**
     * Get the FT entry of the field. This is a read only field and is set depending on the actual type. The field type
     * is an inheritable attribute.
     * 
     * @return The Field type.
     * 
     */
    public abstract String getFieldType();
    
    /**
     * Get the value of the "DV" entry. The "DV" entry is an inheritable attribute.
     * 
     * This will return null if the "DV" entry doesn't exist or if it has no value assigned.
     * 
     * The different field types do require specific object types for their value
     * e.g. for RadioButtons the DV entry needs to be a name object.
     * If the value doesn't match the expected type an IOException is thrown. Such a wrong entry might
     * have been set with a different library or by using PDFBox low level COS model.
     * 
     * To get the value in such cases the lower level COS model can be used.
     * 
     * @return The value of this field.
     * @throws IOException If there is an error reading the data for this field
     *      or the type is not in line with the fields required type.
     * 
     */
    public abstract Object getDefaultValue() throws IOException;
    

    /**
     * Set the value of the "DV" entry. The "DV" entry is an inheritable attribute.
     * 
     * The different field types do require specific object types for their value
     * e.g. for RadioButtons the DV entry needs to be a name object. This needs to be handled by the
     * individual classes.
     * 
     * Trying to set the default value for a {@link PDPushButton} field will lead to an 
     * {@link IllegalArgumentException} as PDPushButton fields do not support setting the 
     * entry although, common to all field types, the DV entry shall not be set.
     * 
     * As a result it might be necessary to check the type of the value before
     * reusing it.
     * 
     * @param defaultValue The new default field value.
     */    
    public abstract void setDefaultValue(String defaultValue);
    

    /**
     * Get the value of the "V" entry. The "V" entry is an inheritable attribute.
     * 
     * This will return null if the "V" entry doesn't exist or if it has no value assigned.
     * 
     * The different field types do require specific object types for their value
     * e.g. for RadioButtons the V entry needs to be a name object.
     * If the value doesn't match the expected type an IOException is thrown. Such a wrong entry might
     * have been set with a different library or by using PDFBox low level COS model.
     * 
     * To get the value in such cases the lower level COS model can be used.
     * 
     * As a result it might be necessary to check the type of the value before
     * reusing it.
     * 
     * @return The value of this entry.
     * @throws IOException If there is an error reading the data for this field
     *      or the type is not in line with the fields required type.
     * 
     */
    public abstract Object getValue() throws IOException;
    
    /**
     * Set the value of the "V" entry. The "V" entry is an inheritable attribute.
     * 
     * The different field types do require specific object types for their value
     * e.g. for RadioButtons the V entry needs to be a name object. This needs to be handled by the
     * individual classes.
     * 
     * Trying to set the value for a {@link PDPushButton} field will lead to an
     * {@link IllegalArgumentException} as PDPushButton fields do not support setting the
     * entry although, common to all field types, the DV entry shall not be set.
     * 
     * As a result it might be necessary to check the type of the value before
     * reusing it.
     * 
     * @param fieldValue The new field value.
     * @throws IOException if there is an error setting the field value.
     */    
    public abstract void setValue(String fieldValue) throws IOException;
    

    /**
     * sets the field to be read-only.
     * 
     * @param readonly The new flag for readonly.
     */
    public void setReadonly(boolean readonly)
    {
        getDictionary().setFlag(COSName.FF, FLAG_READ_ONLY, readonly);
    }

    /**
     * 
     * @return true if the field is readonly
     */
    public boolean isReadonly()
    {
        return getDictionary().getFlag(COSName.FF, FLAG_READ_ONLY);
    }

    /**
     * sets the field to be required.
     * 
     * @param required The new flag for required.
     */
    public void setRequired(boolean required)
    {
        getDictionary().setFlag(COSName.FF, FLAG_REQUIRED, required);
    }

    /**
     * 
     * @return true if the field is required
     */
    public boolean isRequired()
    {
        return getDictionary().getFlag(COSName.FF, FLAG_REQUIRED);
    }

    /**
     * sets the field to be not exported.
     * 
     * @param noExport The new flag for noExport.
     */
    public void setNoExport(boolean noExport)
    {
        getDictionary().setFlag(COSName.FF, FLAG_NO_EXPORT, noExport);
    }

    /**
     * 
     * @return true if the field is not to be exported.
     */
    public boolean isNoExport()
    {
        return getDictionary().getFlag(COSName.FF, FLAG_NO_EXPORT);
    }

    /**
     * This will get the flags for this field.
     * 
     * @return flags The set of flags.
     */
    public abstract int getFieldFlags();

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
     * Get the additional actions for this field. This will return null if there
     * are no additional actions for this field.
     *
     * @return The actions of the field.
     */
    public PDFormFieldAdditionalActions getActions()
    {
        COSDictionary aa = (COSDictionary) getDictionary().getDictionaryObject(COSName.AA);
        PDFormFieldAdditionalActions retval = null;
        if (aa != null)
        {
            retval = new PDFormFieldAdditionalActions(aa);
        }
        return retval;
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
                fdfField.setValue((String) fieldValue);
            }
            else if (fieldValue instanceof PDTextStream)
            {
                fdfField.setValue(((PDTextStream) fieldValue).getAsString());
            }
            else
            {
                throw new IOException("Unknown field type:" + fieldValue.getClass().getName());
            }
        }
        Integer ff = fdfField.getFieldFlags();
        if (ff != null)
        {
            setFieldFlags(ff);
        }
        else
        {
            // these are suppose to be ignored if the Ff is set.
            Integer setFf = fdfField.getSetFieldFlags();

            if (setFf != null)
            {
                int setFfInt = setFf;
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
                int clrFfValue = clrFf;
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
            if (f != null)
            {
                widget.setAnnotationFlags(f);
            }
            else
            {
                // these are suppose to be ignored if the F is set.
                Integer setF = fdfField.getSetWidgetFieldFlags();
                if (setF != null)
                {
                    annotFlags = annotFlags | setF;
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
                    int clrFValue = clrF;
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
            for (COSObjectable pdKid : pdKids)
            {
                if (pdKid instanceof PDFieldTreeNode)
                {
                    PDFieldTreeNode pdChild = (PDFieldTreeNode) pdKid;
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
     */
    public PDAnnotationWidget getWidget()
    {
        PDAnnotationWidget retval = null;
        List<COSObjectable> kids = getKids();
        if (kids == null)
        {
            retval = new PDAnnotationWidget(getDictionary());
        }
        else if (!kids.isEmpty())
        {
            Object firstKid = kids.get(0);
            if (firstKid instanceof PDAnnotationWidget)
            {
                retval = (PDAnnotationWidget) firstKid;
            }
            else
            {
                retval = ((PDFieldTreeNode) firstKid).getWidget();
            }
        }
        return retval;
    }

    /**
     * Get the parent field to this field, or null if none exists.
     * 
     * @return The parent field.
     * 
     */
    public PDFieldTreeNode getParent()
    {
        return parent;
    }

    /**
     * Set the parent of this field.
     * 
     * @param parentNode The parent to this field.
     */
    public void setParent(PDFieldTreeNode parentNode)
    {
        parent = parentNode;
        if (parentNode != null)
        {
            getDictionary().setItem(COSName.PARENT, parent.getDictionary());
        }
        else
        {
            getDictionary().removeItem(COSName.PARENT);
        }
    }

    /**
     * This will find one of the child elements. The name array are the components of the name to search down the tree
     * of names. The nameIndex is where to start in that array. This method is called recursively until it finds the end
     * point based on the name array.
     * 
     * @param name An array that picks the path to the field.
     * @param nameIndex The index into the array.
     * @return The field at the endpoint or null if none is found.
     */
    public PDFieldTreeNode findKid(String[] name, int nameIndex)
    {
        PDFieldTreeNode retval = null;
        COSArray kids = (COSArray) getDictionary().getDictionaryObject(COSName.KIDS);
        if (kids != null)
        {
            for (int i = 0; retval == null && i < kids.size(); i++)
            {
                COSDictionary kidDictionary = (COSDictionary) kids.getObject(i);
                if (name[nameIndex].equals(kidDictionary.getString(COSName.T)))
                {
                    retval = (PDFieldTreeNode) PDFieldTreeNode.createField(acroForm, kidDictionary, this);
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
     */
    public List<COSObjectable> getKids()
    {
        List<COSObjectable> retval = null;
        COSArray kids = (COSArray) dictionary.getDictionaryObject(COSName.KIDS);
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
                
                // Decide if the kid is field or a widget annotation.
                // A field dictionary that does not have a partial field name (T entry)
                // of its own shall not be considered a field but simply a Widget annotation. 
                if (kidDictionary.getDictionaryObject(COSName.T) != null)
                {
                    PDFieldTreeNode field = PDFieldTreeNode.createField(acroForm, kidDictionary, this);
                    if (field != null)
                    {
                        kidsList.add(field);
                    }
                }
                else
                {
                    kidsList.add(new PDAnnotationWidget(kidDictionary));
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
    @Override
    public COSBase getCOSObject()
    {
        return dictionary;
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
        String finalName = getPartialName();
        String parentName = getParent() != null ? getParent().getFullyQualifiedName() : null;
        if (parentName != null)
        {
            if (finalName != null)
            {
                finalName = parentName + "." + finalName;
            }
            else
            {
                finalName = parentName;
            }
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
     * Gets the mapping name of the field.
     * 
     * The mapping name shall be used when exporting interactive form field
     * data from the document.
     * 
     * @return the mapping name of the field
     */
    public String getMappingName()
    {
        return this.getDictionary().getString(COSName.TM);
    }

    /**
     * This will set the mapping name of the field.
     * 
     * @param mappingName the mapping name of the field
     */
    public void setMappingName(String mappingName)
    {
        this.getDictionary().setString(COSName.TM, mappingName);
    }    
    
    

    /**
     * Creates a COSField subclass from the given field.
     * @param form the form that the field is part of
     * @param field the dictionary representing a field element
     * @param parentNode the parent node of the node to be created 
     * @return the corresponding PDField instance
     */
    public static PDFieldTreeNode createField(PDAcroForm form, COSDictionary field, PDFieldTreeNode parentNode)
    {
        String fieldType = findFieldType(field);
        if (FIELD_TYPE_CHOICE.equals(fieldType))
        {
            int flags = field.getInt(COSName.FF, 0);
            if ((flags & PDChoice.FLAG_COMBO) != 0)
            {
                return new PDComboBox(form, field, parentNode);
            }
            else
            {
                return new PDListBox(form, field, parentNode);
            }
        }
        else if (FIELD_TYPE_TEXT.equals(fieldType))
        {
            return new PDTextField(form, field, parentNode);
        }
        else if (FIELD_TYPE_SIGNATURE.equals(fieldType))
        {
            return new PDSignatureField(form, field, parentNode);
        }
        else if (FIELD_TYPE_BUTTON.equals(fieldType))
        {
            int flags = field.getInt(COSName.FF, 0);
            // BJL: I have found that the radio flag bit is not always set
            // and that sometimes there is just a kids dictionary.
            // so, if there is a kids dictionary then it must be a radio button group.
            if ((flags & PDButton.FLAG_RADIO) != 0)
            {
                return new PDRadioButton(form, field, parentNode);
            }
            else if ((flags & PDButton.FLAG_PUSHBUTTON) != 0)
            {
                return new PDPushButton(form, field, parentNode);
            }
            else
            {
                return new PDCheckbox(form, field, parentNode);
            }
        }
        else
        {
            return new PDNonTerminalField(form, field, parentNode); 
        }
    }

    private static String findFieldType(COSDictionary dic)
    {
        String retval = dic.getNameAsString(COSName.FT);
        if (retval == null)
        {
            COSDictionary parent = (COSDictionary) dic.getDictionaryObject(COSName.PARENT,
                    COSName.P);
            if (parent != null)
            {
                retval = findFieldType(parent);
            }
        }
        return retval;
    }
}

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
 * A class for handling the PDF field as a signature.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @author Thomas Chojecki
 * @version $Revision: 1.5 $
 */
public class PDSignatureField extends PDField
{

    /**
     * @see PDField#PDField(PDAcroForm,COSDictionary)
     *
     * @param theAcroForm The acroForm for this field.
     * @param field The dictionary for the signature.
     * @throws IOException If there is an error while resolving partital name for the signature field
     */
    public PDSignatureField( PDAcroForm theAcroForm, COSDictionary field) throws IOException
    {
        super(theAcroForm,field);
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
    public PDSignatureField( PDAcroForm theAcroForm) throws IOException
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
     * @return
     * @throws IOException If there is an error while getting the list of fields.
     */
    private String generatePartialName() throws IOException
    {
      PDAcroForm acroForm = getAcroForm();
      List fields = acroForm.getFields();
      
      String fieldName = "Signature";
      int i = 1;
      
      Set<String> sigNames = new HashSet<String>();
      
      for ( Object object : fields )
      {
        if(object instanceof PDSignatureField)
        {
          sigNames.add(((PDSignatureField)object).getPartialName());
        }
      }

      while(sigNames.contains(fieldName+i))
      {
        ++i;
      }
      return fieldName+i;
    }
    
    /**
     * @see PDField#setValue(java.lang.String)
     *
     * @param value The new value for the field.
     *
     * @throws IOException If there is an error creating the appearance stream.
     * @deprecated use setSignature(PDSignature) instead
     */
    @Override
    @Deprecated
    public void setValue(String value) throws IOException
    {
        throw new RuntimeException( "Can't set signature as String, use setSignature(PDSignature) instead" );
    }

    /**
     * @see PDField#setValue(java.lang.String)
     *
     * @return The string value of this field.
     *
     * @throws IOException If there is an error creating the appearance stream.
     * @deprecated use getSignature() instead
     */
    @Override
    @Deprecated
    public String getValue() throws IOException
    {
      throw new RuntimeException( "Can't get signature as String, use getSignature() instead." );
    }

    /**
     * Return a string rep of this object.
     *
     * @return A string rep of this object.
     */
    @Override
    public String toString()
    {
        return "PDSignature";
    }
    
    /**
     * Add a signature dictionary to the signature field.
     * 
     * @param value is the PDSignature 
     */
    public void setSignature(PDSignature value) 
    {
      getDictionary().setItem(COSName.V, value);
    }
    
    /**
     * Get the signature dictionary.
     * 
     * @return the signature dictionary
     * 
     */
    public PDSignature getSignature()
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
}

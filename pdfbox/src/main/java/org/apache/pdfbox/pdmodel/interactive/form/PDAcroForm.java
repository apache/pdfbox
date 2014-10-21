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

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.COSArrayList;
import org.apache.pdfbox.pdmodel.common.COSObjectable;
import org.apache.pdfbox.pdmodel.fdf.FDFDictionary;
import org.apache.pdfbox.pdmodel.fdf.FDFDocument;
import org.apache.pdfbox.pdmodel.fdf.FDFCatalog;
import org.apache.pdfbox.pdmodel.fdf.FDFField;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * An interactive form, also known as an AcroForm.
 *
 * @author Ben Litchfield
 */
public final class PDAcroForm implements COSObjectable
{
    private static final int FLAG_SIGNATURES_EXIST = 1;
    private static final int FLAG_APPEND_ONLY = 1;
    
    private COSDictionary acroForm;
    private PDDocument document;
    private COSString defaultAppearance;

    private Map<String,PDFieldTreeNode> fieldCache;

    /**
     * Constructor.
     *
     * @param doc The document that this form is part of.
     */
    public PDAcroForm(PDDocument doc)
    {
        document = doc;
        acroForm = new COSDictionary();
        COSArray fields = new COSArray();
        acroForm.setItem( COSName.FIELDS, fields );
    }

    /**
     * Constructor.
     *
     * @param doc The document that this form is part of.
     * @param form The existing acroForm.
     */
    public PDAcroForm(PDDocument doc, COSDictionary form)
    {
        document = doc;
        acroForm = form;
    }

    /**
     * This will get the document associated with this form.
     *
     * @return The PDF document.
     */
    public PDDocument getDocument()
    {
        return document;
    }

    /**
     * This will get the dictionary that this form wraps.
     *
     * @return The dictionary for this form.
     */
    public COSDictionary getDictionary()
    {
        return acroForm;
    }

    /**
     * This method will import an entire FDF document into the PDF document
     * that this acroform is part of.
     *
     * @param fdf The FDF document to import.
     *
     * @throws IOException If there is an error doing the import.
     */
    public void importFDF( FDFDocument fdf ) throws IOException
    {
        List<?> fields = fdf.getCatalog().getFDF().getFields();
        if( fields != null )
        {
            for (Object field : fields)
            {
                FDFField fdfField = (FDFField) field;
                PDFieldTreeNode docField = getField( fdfField.getPartialFieldName() );
                if( docField != null )
                {
                    docField.importFDF( fdfField );
                }
            }
        }
    }

    /**
     * This will export all FDF form data.
     *
     * @return An FDF document used to export the document.
     * @throws IOException If there is an error when exporting the document.
     */
    public FDFDocument exportFDF() throws IOException
    {
        FDFDocument fdf = new FDFDocument();
        FDFCatalog catalog = fdf.getCatalog();
        FDFDictionary fdfDict = new FDFDictionary();
        catalog.setFDF( fdfDict );

        List<FDFField> fdfFields = new ArrayList<FDFField>();
        List<PDFieldTreeNode> fields = getFields();
        Iterator<PDFieldTreeNode> fieldIter = fields.iterator();
        while( fieldIter.hasNext() )
        {
            PDFieldTreeNode docField = fieldIter.next();
            addFieldAndChildren( docField, fdfFields );
        }
        fdfDict.setID( document.getDocument().getDocumentID() );
        if( fdfFields.size() > 0 )
        {
            fdfDict.setFields( fdfFields );
        }
        return fdf;
    }

    private void addFieldAndChildren( PDFieldTreeNode docField, List<FDFField> fdfFields ) throws IOException
    {
        Object fieldValue = docField.getValue();
        FDFField fdfField = new FDFField();
        fdfField.setPartialFieldName( docField.getPartialName() );
        fdfField.setValue( fieldValue );
        List<COSObjectable> kids = docField.getKids();
        List<FDFField> childFDFFields = new ArrayList<FDFField>();
        if( kids != null )
        {
            for (COSObjectable kid : kids)
            {
                addFieldAndChildren((PDFieldTreeNode) kid, childFDFFields);
            }
            if( childFDFFields.size() > 0 )
            {
                fdfField.setKids( childFDFFields );
            }
        }
        if( fieldValue != null || childFDFFields.size() > 0 )
        {
            fdfFields.add( fdfField );
        }
    }

    /**
     * This will return all of the fields in the document.  The type
     * will be a org.apache.pdfbox.pdmodel.field.PDField.
     *
     * @return A list of all the fields.
     * 
     */
    public List<PDFieldTreeNode> getFields()
    {
        COSArray cosFields = (COSArray) acroForm.getDictionaryObject(COSName.FIELDS);
        if( cosFields == null )
        {
            return null;
        }
        List<PDFieldTreeNode> pdFields = new ArrayList<PDFieldTreeNode>();
        for (int i = 0; i < cosFields.size(); i++)
        {
            COSDictionary element = (COSDictionary) cosFields.getObject(i);
            if (element != null)
            {
                PDFieldTreeNode field = PDFieldTreeNode.createField( this, element, null );
                if( field != null )
                {
                    pdFields.add(field);
                }
            }
        }
        return new COSArrayList<PDFieldTreeNode>( pdFields, cosFields );
    }

    /**
     * Set the fields that are part of this AcroForm.
     *
     * @param fields The fields that are part of this form.
     */
    public void setFields( List<PDFieldTreeNode> fields )
    {
        acroForm.setItem( COSName.FIELDS, COSArrayList.converterToCOSArray( fields ));
    }

    /**
     * This will tell this form to cache the fields into a Map structure
     * for fast access via the getField method.  The default is false.  You would
     * want this to be false if you were changing the COSDictionary behind the scenes,
     * otherwise setting this to true is acceptable.
     *
     * @param cache A boolean telling if we should cache the fields.
     * @throws IOException If there is an error while caching the fields.
     */
    public void setCacheFields( boolean cache ) throws IOException
    {
        if( cache )
        {
            fieldCache = new HashMap<String,PDFieldTreeNode>();
            List<PDFieldTreeNode> fields = getFields();
            Iterator<PDFieldTreeNode> fieldIter = fields.iterator();
            while( fieldIter.hasNext() )
            {
                PDFieldTreeNode next = fieldIter.next();
                fieldCache.put( next.getFullyQualifiedName(), next );
            }
        }
        else
        {
            fieldCache = null;
        }
    }

    /**
     * This will tell if this acro form is caching the fields.
     *
     * @return true if the fields are being cached.
     */
    public boolean isCachingFields()
    {
        return fieldCache != null;
    }

    /**
     * This will get a field by name, possibly using the cache if setCache is true.
     *
     * @param name The name of the field to get.
     *
     * @return The field with that name of null if one was not found.
     *
     * @throws IOException If there is an error getting the field type.
     */
    public PDFieldTreeNode getField( String name ) throws IOException
    {
        PDFieldTreeNode retval = null;
        if( fieldCache != null )
        {
            retval = fieldCache.get( name );
        }
        else
        {
            String[] nameSubSection = name.split( "\\." );
            COSArray fields = (COSArray) acroForm.getDictionaryObject(COSName.FIELDS);

            for (int i = 0; i < fields.size() && retval == null; i++)
            {
                COSDictionary element = (COSDictionary) fields.getObject(i);
                if( element != null )
                {
                    COSString fieldName =
                        (COSString)element.getDictionaryObject( COSName.T );
                    if( fieldName.getString().equals( name ) ||
                        fieldName.getString().equals( nameSubSection[0] ) )
                    {
                        PDFieldTreeNode root = PDFieldTreeNode.createField( this, element, null );

                        if( nameSubSection.length > 1 )
                        {
                            PDFieldTreeNode kid = root.findKid( nameSubSection, 1 );
                            if( kid != null )
                            {
                                retval = kid;
                            }
                            else
                            {
                                retval = root;
                            }
                        }
                        else
                        {
                            retval = root;
                        }
                    }
                }
            }
        }
        return retval;
    }

    /**
     * Get the default appearance.
     * 
     * @return the DA element of the dictionary object
     */
    public COSString getDefaultAppearance()
    {
        if (defaultAppearance == null)
        {
            COSBase daValue =  getDictionary().getItem(COSName.DA);
            if (daValue != null)
            {
                defaultAppearance = (COSString)daValue;
            }
        }
        return defaultAppearance;
    }

    /**
     * Set the default appearance.
     * 
     * @param daValue a string describing the default appearance
     */
    public void setDefaultAppearance(String daValue)
    {
        if (daValue != null)
        {
            defaultAppearance = new COSString(daValue);
            getDictionary().setItem(COSName.DA, defaultAppearance);
        }
        else
        {
            defaultAppearance = null;
            getDictionary().removeItem(COSName.DA);
        }
    }

    /**
     * Get the value of NeedAppearances.
     * 
     * @return the value of NeedAppearances, false if the value isn't set
     */
    public boolean isNeedAppearances()
    {
        return getDictionary().getBoolean(COSName.NEED_APPEARANCES, false);
    }

    /**
     * Set the NeedAppearances value.
     * 
     * @param value the value for NeedAppearances
     */
    public void setNeedAppearances(Boolean value)
    {
        if (value != null)
        {
            getDictionary().setBoolean(COSName.NEED_APPEARANCES, value);
        }
        else
        {
            getDictionary().removeItem(COSName.NEED_APPEARANCES);
        }
    }
    
    /**
     * This will get the default resources for the acro form.
     *
     * @return The default resources.
     */
    public PDResources getDefaultResources()
    {
        PDResources retval = null;
        COSDictionary dr = (COSDictionary)acroForm.getDictionaryObject( COSName.DR );
        if( dr != null )
        {
            retval = new PDResources( dr );
        }
        return retval;
    }

    /**
     * This will set the default resources for the acroform.
     *
     * @param dr The new default resources.
     */
    public void setDefaultResources( PDResources dr )
    {
        COSDictionary drDict = null;
        if( dr != null )
        {
            drDict = dr.getCOSObject();
        }
        acroForm.setItem( COSName.DR, drDict );
    }

    @Override
    public COSBase getCOSObject()
    {
        return acroForm;
    }

    /**
     * Get the XFA resource, the XFA resource is only used for PDF 1.5+ forms.
     *
     * @return The xfa resource or null if it does not exist.
     */
    public PDXFAResource getXFA()
    {
        PDXFAResource xfa = null;
        COSBase base = acroForm.getDictionaryObject( COSName.XFA );
        if( base != null )
        {
            xfa = new PDXFAResource( base );
        }
        return xfa;
    }

    /**
     * Set the XFA resource, this is only used for PDF 1.5+ forms.
     *
     * @param xfa The xfa resource.
     */
    public void setXFA( PDXFAResource xfa )
    {
        acroForm.setItem( COSName.XFA, xfa );
    }
    
    /**
     * This will get the 'quadding' or justification of the text to be displayed.
     * 0 - Left(default)<br/>
     * 1 - Centered<br />
     * 2 - Right<br />
     * Please see the QUADDING_CONSTANTS.
     *
     * @return The justification of the text strings.
     */
    public int getQ()
    {
        int retval = 0;
        COSNumber number = (COSNumber)getDictionary().getDictionaryObject( COSName.Q );
        if( number != null )
        {
            retval = number.intValue();
        }
        return retval;
    }

    /**
     * This will set the quadding/justification of the text.  See QUADDING constants.
     *
     * @param q The new text justification.
     */
    public void setQ( int q )
    {
        getDictionary().setInt( COSName.Q, q );
    }

    /**
     * Determines if SignaturesExist is set.
     * 
     * @return true if the document contains at least one signature.
     */
    public boolean isSignaturesExist()
    {
        return getDictionary().getFlag( COSName.SIG_FLAGS, FLAG_SIGNATURES_EXIST );
    }

    /**
     * Set the SignaturesExist bit.
     *
     * @param signaturesExist The value for SignaturesExist.
     */
    public void setSignaturesExist( boolean signaturesExist )
    {
        getDictionary().setFlag( COSName.SIG_FLAGS, FLAG_SIGNATURES_EXIST, signaturesExist );
    }

    /**
     * Determines if AppendOnly is set.
     * 
     * @return true if the document contains signatures that may be invalidated if the file is saved.
     */
    public boolean isAppendOnly()
    {
        return getDictionary().getFlag( COSName.SIG_FLAGS, FLAG_APPEND_ONLY );
    }

    /**
     * Set the AppendOnly bit.
     *
     * @param appendOnly The value for AppendOnly.
     */
    public void setAppendOnly( boolean appendOnly )
    {
        getDictionary().setFlag( COSName.SIG_FLAGS, FLAG_APPEND_ONLY, appendOnly );
    }
}

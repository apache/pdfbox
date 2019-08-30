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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.COSArrayList;
import org.apache.pdfbox.pdmodel.common.COSObjectable;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.fdf.FDFCatalog;
import org.apache.pdfbox.pdmodel.fdf.FDFDictionary;
import org.apache.pdfbox.pdmodel.fdf.FDFDocument;
import org.apache.pdfbox.pdmodel.fdf.FDFField;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceStream;
import org.apache.pdfbox.util.Matrix;

/**
 * An interactive form, also known as an AcroForm.
 *
 * @author Ben Litchfield
 */
public final class PDAcroForm implements COSObjectable
{
    private static final Log LOG = LogFactory.getLog(PDAcroForm.class);
    
    private static final int FLAG_SIGNATURES_EXIST = 1;
    private static final int FLAG_APPEND_ONLY = 1 << 1;

    private final PDDocument document;
    private final COSDictionary dictionary;
    
    private Map<String, PDField> fieldCache;

    /**
     * Constructor.
     *
     * @param doc The document that this form is part of.
     */
    public PDAcroForm(PDDocument doc)
    {
        document = doc;
        dictionary = new COSDictionary();
        dictionary.setItem(COSName.FIELDS, new COSArray());
    }

    /**
     * Constructor. Side effect: /Helv and /ZaDb fonts added with update mark.
     *
     * @param doc The document that this form is part of.
     * @param form The existing acroForm.
     */
    public PDAcroForm(PDDocument doc, COSDictionary form)
    {
        document = doc;
        dictionary = form;
        verifyOrCreateDefaults();
    }
    
    /*
     * Verify that there are default entries for required 
     * properties.
     * 
     * If these are missing create default entries similar to
     * Adobe Reader / Adobe Acrobat
     *  
     */
    private void verifyOrCreateDefaults()
    {
        final String adobeDefaultAppearanceString = "/Helv 0 Tf 0 g ";

        // DA entry is required
        if (getDefaultAppearance().length() == 0)
        {
            setDefaultAppearance(adobeDefaultAppearanceString);
            dictionary.setNeedToBeUpdated(true);
        }

        // DR entry is required
        PDResources defaultResources = getDefaultResources();
        if (defaultResources == null)
        {
            defaultResources = new PDResources();
            setDefaultResources(defaultResources);
            dictionary.setNeedToBeUpdated(true);
        }

        // PDFBOX-3732: Adobe Acrobat uses Helvetica as a default font and 
        // stores that under the name '/Helv' in the resources dictionary
        // Zapf Dingbats is included per default for check boxes and 
        // radio buttons as /ZaDb.
        // PDFBOX-4393: the two fonts are added by Adobe when signing
        // and this breaks a previous signature. (Might be an Adobe bug)
        COSDictionary fontDict = defaultResources.getCOSObject().getCOSDictionary(COSName.FONT);
        if (fontDict == null)
        {
            fontDict = new COSDictionary();
            defaultResources.getCOSObject().setItem(COSName.FONT, fontDict);
        }
        if (!fontDict.containsKey(COSName.HELV))
        {
            defaultResources.put(COSName.HELV, PDType1Font.HELVETICA);
            defaultResources.getCOSObject().setNeedToBeUpdated(true);
            fontDict.setNeedToBeUpdated(true);
        }
        if (!fontDict.containsKey(COSName.ZA_DB))
        {
            defaultResources.put(COSName.ZA_DB, PDType1Font.ZAPF_DINGBATS);
            defaultResources.getCOSObject().setNeedToBeUpdated(true);
            fontDict.setNeedToBeUpdated(true);
        }
    }
    

    /**
     * This will get the document associated with this form.
     *
     * @return The PDF document.
     */
    PDDocument getDocument()
    {
        return document;
    }
    
    @Override
    public COSDictionary getCOSObject()
    {
        return dictionary;
    }

    /**
     * This method will import an entire FDF document into the PDF document
     * that this acroform is part of.
     *
     * @param fdf The FDF document to import.
     *
     * @throws IOException If there is an error doing the import.
     */
    public void importFDF(FDFDocument fdf) throws IOException
    {
        List<FDFField> fields = fdf.getCatalog().getFDF().getFields();
        if (fields != null)
        {
            for (FDFField field : fields)
            {
                FDFField fdfField = field;
                PDField docField = getField(fdfField.getPartialFieldName());
                if (docField != null)
                {
                    docField.importFDF(fdfField);
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
        catalog.setFDF(fdfDict);

        List<FDFField> fdfFields = new ArrayList<FDFField>();
        List<PDField> fields = getFields();
        for (PDField field : fields)
        {
            fdfFields.add(field.exportFDF());
        }
        
        fdfDict.setID(document.getDocument().getDocumentID());
        
        if (!fdfFields.isEmpty())
        {
            fdfDict.setFields(fdfFields);
        }
        return fdf;
    }

    /**
     * This will flatten all form fields.
     * 
     * <p>Flattening a form field will take the current appearance and make that part
     * of the pages content stream. All form fields and annotations associated are removed.</p>
     * 
     * <p>Invisible and hidden fields will be skipped and will not become part of the
     * page content stream</p>
     * 
     * <p>The appearances for the form fields widgets will <strong>not</strong> be generated<p>
     * 
     * @throws IOException 
     */
    public void flatten() throws IOException
    {
        // for dynamic XFA forms there is no flatten as this would mean to do a rendering
        // from the XFA content into a static PDF.
        if (xfaIsDynamic())
        {
            LOG.warn("Flatten for a dynamix XFA form is not supported");
            return;
        }
        
        List<PDField> fields = new ArrayList<PDField>();
        for (PDField field: getFieldTree())
        {
            fields.add(field);
        }
        flatten(fields, false);
    }
    
    
    /**
     * This will flatten the specified form fields.
     * 
     * <p>Flattening a form field will take the current appearance and make that part
     * of the pages content stream. All form fields and annotations associated are removed.</p>
     * 
     * <p>Invisible and hidden fields will be skipped and will not become part of the
     * page content stream</p>
     * 
     * @param fields
     * @param refreshAppearances if set to true the appearances for the form field widgets will be updated
     * @throws IOException 
     */
    public void flatten(List<PDField> fields, boolean refreshAppearances) throws IOException
    {
        // Nothing to flatten if there are no fields provided
        if (fields.isEmpty())
        {
            return;
        }
        
        if (!refreshAppearances && getNeedAppearances())
        {
            LOG.warn("acroForm.getNeedAppearances() returns true, " +
                     "visual field appearances may not have been set");
            LOG.warn("call acroForm.refreshAppearances() or " +
                     "use the flatten() method with refreshAppearances parameter");
        }

        // for dynamic XFA forms there is no flatten as this would mean to do a rendering
        // from the XFA content into a static PDF.
        if (xfaIsDynamic())
        {
            LOG.warn("Flatten for a dynamix XFA form is not supported");
            return;
        }
        
        // refresh the appearances if set
        if (refreshAppearances)
        {
            refreshAppearances(fields);
        }

        // the content stream to write to
        PDPageContentStream contentStream;

        // get the widgets per page
        Map<COSDictionary,Set<COSDictionary>> pagesWidgetsMap = buildPagesWidgetsMap(fields);
        
        // preserve all non widget annotations
        for (PDPage page : document.getPages())
        {
            Set<COSDictionary> widgetsForPageMap = pagesWidgetsMap.get(page.getCOSObject());

            // indicates if the original content stream
            // has been wrapped in a q...Q pair.
            boolean isContentStreamWrapped = false;
            
            List<PDAnnotation> annotations = new ArrayList<PDAnnotation>();
            
            for (PDAnnotation annotation: page.getAnnotations())
            {
                if (widgetsForPageMap != null && !widgetsForPageMap.contains(annotation.getCOSObject()))
                {
                    annotations.add(annotation);                 
                }
                else if (!annotation.isInvisible() && !annotation.isHidden() &&
                         annotation.getNormalAppearanceStream() != null && 
                         annotation.getNormalAppearanceStream().getBBox() != null)
                {
                    contentStream = new PDPageContentStream(document, page, AppendMode.APPEND, true, !isContentStreamWrapped);
                    isContentStreamWrapped = true;
                    
                    PDAppearanceStream appearanceStream = annotation.getNormalAppearanceStream();
                    
                    PDFormXObject fieldObject = new PDFormXObject(appearanceStream.getCOSObject());
                    
                    contentStream.saveGraphicsState();
                    
                    // translate the appearance stream to the widget location if there is 
                    // not already a transformation in place
                    boolean needsTranslation = resolveNeedsTranslation(appearanceStream);

                    // scale the appearance stream - mainly needed for images
                    // in buttons and signatures
                    boolean needsScaling = resolveNeedsScaling(annotation, page.getRotation());

                    Matrix transformationMatrix = new Matrix();
                    boolean transformed = false;
                    
                    if (needsTranslation)
                    {
                        transformationMatrix.translate(annotation.getRectangle().getLowerLeftX(),
                                annotation.getRectangle().getLowerLeftY());
                        transformed = true;
                    }

                    if (needsScaling)
                    {
                        PDRectangle bbox = appearanceStream.getBBox();
                        PDRectangle fieldRect = annotation.getRectangle();

                        float xScale;
                        float yScale;
                        if (page.getRotation() == 90 || page.getRotation() == 270)
                        {
                            xScale = fieldRect.getWidth() / bbox.getHeight();
                            yScale = fieldRect.getHeight() / bbox.getWidth();
                        }
                        else
                        {
                            xScale = fieldRect.getWidth() / bbox.getWidth();
                            yScale = fieldRect.getHeight() / bbox.getHeight();
                        }
                        Matrix scalingMatrix = Matrix.getScaleInstance(xScale, yScale);
                        transformationMatrix.concatenate(scalingMatrix);
                        transformed = true;
                    }

                    if (transformed)
                    {
                        contentStream.transform(transformationMatrix);
                    }
                    
                    contentStream.drawForm(fieldObject);
                    contentStream.restoreGraphicsState();
                    contentStream.close();
                }
            }
            page.setAnnotations(annotations);
        }
        
        // remove the fields
        removeFields(fields);
        
        // remove XFA for hybrid forms
        dictionary.removeItem(COSName.XFA);
        
    }    

    /**
     * Refreshes the appearance streams and appearance dictionaries for 
     * the widget annotations of all fields.
     * 
     * @throws IOException
     */
    public void refreshAppearances() throws IOException
    {
        for (PDField field : getFieldTree())
        {
            if (field instanceof PDTerminalField)
            {
                ((PDTerminalField) field).constructAppearances();
            }
        }
    }

    /**
     * Refreshes the appearance streams and appearance dictionaries for 
     * the widget annotations of the specified fields.
     * 
     * @param fields
     * @throws IOException
     */
    public void refreshAppearances(List<PDField> fields) throws IOException
    {
        for (PDField field : fields)
        {
            if (field instanceof PDTerminalField)
            {
                ((PDTerminalField) field).constructAppearances();
            }
        }
    }
    
    
    /**
     * This will return all of the documents root fields.
     * 
     * A field might have children that are fields (non-terminal field) or does not
     * have children which are fields (terminal fields).
     * 
     * The fields within an AcroForm are organized in a tree structure. The documents root fields 
     * might either be terminal fields, non-terminal fields or a mixture of both. Non-terminal fields
     * mark branches which contents can be retrieved using {@link PDNonTerminalField#getChildren()}.
     * 
     * @return A list of the documents root fields, never null. If there are no fields then this
     * method returns an empty list.
     */
    public List<PDField> getFields()
    {
        COSArray cosFields = (COSArray) dictionary.getDictionaryObject(COSName.FIELDS);
        if (cosFields == null)
        {
            return Collections.emptyList();
        }
        List<PDField> pdFields = new ArrayList<PDField>();
        for (int i = 0; i < cosFields.size(); i++)
        {
            COSDictionary element = (COSDictionary) cosFields.getObject(i);
            if (element != null)
            {
                PDField field = PDField.fromDictionary(this, element, null);
                if (field != null)
                {
                    pdFields.add(field);
                }
            }
        }
        return new COSArrayList<PDField>(pdFields, cosFields);
    }

    /**
     * Set the documents root fields.
     *
     * @param fields The fields that are part of the documents root fields.
     */
    public void setFields(List<PDField> fields)
    {
        dictionary.setItem(COSName.FIELDS, COSArrayList.converterToCOSArray(fields));
    }
    
    /**
     * Returns an iterator which walks all fields in the field tree, in order.
     */
    public Iterator<PDField> getFieldIterator()
    {
        return new PDFieldTree(this).iterator();
    }

    /**
     * Return the field tree representing all form fields
     */
    public PDFieldTree getFieldTree()
    {
        return new PDFieldTree(this);
    }    
    
    /**
     * This will tell this form to cache the fields into a Map structure
     * for fast access via the getField method.  The default is false.  You would
     * want this to be false if you were changing the COSDictionary behind the scenes,
     * otherwise setting this to true is acceptable.
     *
     * @param cache A boolean telling if we should cache the fields.
     */
    public void setCacheFields(boolean cache)
    {
        if (cache)
        {
            fieldCache = new HashMap<String, PDField>();

            for (PDField field : getFieldTree())
            {
                fieldCache.put(field.getFullyQualifiedName(), field);
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
     * @param fullyQualifiedName The name of the field to get.
     * @return The field with that name of null if one was not found.
     */
    public PDField getField(String fullyQualifiedName)
    {
        // get the field from the cache if there is one.
        if (fieldCache != null)
        {
            return fieldCache.get(fullyQualifiedName);
        }

        // get the field from the field tree
        for (PDField field : getFieldTree())
        {
            if (field.getFullyQualifiedName().equals(fullyQualifiedName))
            {
                return field;
            }
        }
        
        return null;
    }

    /**
     * Get the default appearance.
     * 
     * @return the DA element of the dictionary object
     */
    public String getDefaultAppearance()
    {
        return dictionary.getString(COSName.DA,"");
    }

    /**
     * Set the default appearance.
     * 
     * @param daValue a string describing the default appearance
     */
    public void setDefaultAppearance(String daValue)
    {
        dictionary.setString(COSName.DA, daValue);
    }

    /**
     * True if the viewing application should construct the appearances of all field widgets.
     * The default value is false.
     * 
     * @return the value of NeedAppearances, false if the value isn't set
     */
    public boolean getNeedAppearances()
    {
        return dictionary.getBoolean(COSName.NEED_APPEARANCES, false);
    }

    /**
     * Set the NeedAppearances value. If this is false, PDFBox will create appearances for all field
     * widget.
     * 
     * @param value the value for NeedAppearances
     */
    public void setNeedAppearances(Boolean value)
    {
        dictionary.setBoolean(COSName.NEED_APPEARANCES, value);
    }
    
    /**
     * This will get the default resources for the AcroForm.
     *
     * @return The default resources or null if there is none.
     */
    public PDResources getDefaultResources()
    {
        PDResources retval = null;
        COSBase base = dictionary.getDictionaryObject(COSName.DR);
        if (base instanceof COSDictionary)
        {
            retval = new PDResources((COSDictionary) base, document.getResourceCache());
        }
        return retval;
    }

    /**
     * This will set the default resources for the acroform.
     *
     * @param dr The new default resources.
     */
    public void setDefaultResources(PDResources dr)
    {
        dictionary.setItem(COSName.DR, dr);
    }

    /**
     * This will tell if the AcroForm has XFA content.
     *
     * @return true if the AcroForm is an XFA form
     */
    public boolean hasXFA()
    {
        return dictionary.containsKey(COSName.XFA);
    }

    /**
     * This will tell if the AcroForm is a dynamic XFA form.
     *
     * @return true if the AcroForm is a dynamic XFA form
     */
    public boolean xfaIsDynamic()
    {
        return hasXFA() && getFields().isEmpty();
    }
    
    /**
     * Get the XFA resource, the XFA resource is only used for PDF 1.5+ forms.
     *
     * @return The xfa resource or null if it does not exist.
     */
    public PDXFAResource getXFA()
    {
        PDXFAResource xfa = null;
        COSBase base = dictionary.getDictionaryObject(COSName.XFA);
        if (base != null)
        {
            xfa = new PDXFAResource(base);
        }
        return xfa;
    }

    /**
     * Set the XFA resource, this is only used for PDF 1.5+ forms.
     *
     * @param xfa The xfa resource.
     */
    public void setXFA(PDXFAResource xfa)
    {
        dictionary.setItem(COSName.XFA, xfa);
    }
    
    /**
     * This will get the document-wide default value for the quadding/justification of variable text
     * fields. 
     * <p>
     * 0 - Left(default)<br>
     * 1 - Centered<br>
     * 2 - Right<br>
     * See the QUADDING constants of {@link PDVariableText}.
     *
     * @return The justification of the variable text fields.
     */
    public int getQ()
    {
        int retval = 0;
        COSNumber number = (COSNumber)dictionary.getDictionaryObject(COSName.Q);
        if (number != null)
        {
            retval = number.intValue();
        }
        return retval;
    }

    /**
     * This will set the document-wide default value for the quadding/justification of variable text
     * fields. See the QUADDING constants of {@link PDVariableText}.
     *
     * @param q The justification of the variable text fields.
     */
    public void setQ(int q)
    {
        dictionary.setInt(COSName.Q, q);
    }

    /**
     * Determines if SignaturesExist is set.
     * 
     * @return true if the document contains at least one signature.
     */
    public boolean isSignaturesExist()
    {
        return dictionary.getFlag(COSName.SIG_FLAGS, FLAG_SIGNATURES_EXIST);
    }

    /**
     * Set the SignaturesExist bit.
     *
     * @param signaturesExist The value for SignaturesExist.
     */
    public void setSignaturesExist(boolean signaturesExist)
    {
        dictionary.setFlag(COSName.SIG_FLAGS, FLAG_SIGNATURES_EXIST, signaturesExist);
    }

    /**
     * Determines if AppendOnly is set.
     * 
     * @return true if the document contains signatures that may be invalidated if the file is saved.
     */
    public boolean isAppendOnly()
    {
        return dictionary.getFlag(COSName.SIG_FLAGS, FLAG_APPEND_ONLY);
    }

    /**
     * Set the AppendOnly bit.
     *
     * @param appendOnly The value for AppendOnly.
     */
    public void setAppendOnly(boolean appendOnly)
    {
        dictionary.setFlag(COSName.SIG_FLAGS, FLAG_APPEND_ONLY, appendOnly);
    }
    
    /**
     * Check if there is a translation needed to place the annotations content.
     * 
     * @param appearanceStream
     * @return the need for a translation transformation.
     */
    private boolean resolveNeedsTranslation(PDAppearanceStream appearanceStream)
    {
        boolean needsTranslation = true;

        PDResources resources = appearanceStream.getResources();
        if (resources != null && resources.getXObjectNames().iterator().hasNext())
        {
            Iterator<COSName> xObjectNames = resources.getXObjectNames().iterator();

            while (xObjectNames.hasNext())
            {
                try
                {
                    // if the BBox of the PDFormXObject does not start at 0,0
                    // there is no need do translate as this is done by the BBox definition.
                    PDXObject xObject = resources.getXObject(xObjectNames.next());
                    if (xObject instanceof PDFormXObject)
                    {
                        PDRectangle bbox = ((PDFormXObject)xObject).getBBox();
                        float llX = bbox.getLowerLeftX();
                        float llY = bbox.getLowerLeftY();
                        if (Float.compare(llX, 0) != 0 && Float.compare(llY, 0) != 0)
                        {
                            needsTranslation = false;
                        }
                    }
                }
                catch (IOException e)
                {
                    // we can safely ignore the exception here
                    // as this might only cause a misplacement
                }
            }
            return needsTranslation;
        }
        
        return true;
    }
    
    /**
     * Check if there needs to be a scaling transformation applied.
     * 
     * @param annotation
     * @param rotation 
     * @return the need for a scaling transformation.
     */    
    private boolean resolveNeedsScaling(PDAnnotation annotation, int rotation)
    {
        PDAppearanceStream appearanceStream = annotation.getNormalAppearanceStream();
        // Check if there is a transformation within the XObjects content
        PDResources resources = appearanceStream.getResources();
        if (resources != null && resources.getXObjectNames().iterator().hasNext())
        {
            return true;
        }
        PDRectangle bbox = appearanceStream.getBBox();
        PDRectangle fieldRect = annotation.getRectangle();
        if (rotation == 90 || rotation == 270)
        {
            return Float.compare(bbox.getWidth(),  fieldRect.getHeight()) != 0 ||
                   Float.compare(bbox.getHeight(), fieldRect.getWidth()) != 0;
        }
        else
        {
            return Float.compare(bbox.getWidth(),  fieldRect.getWidth()) != 0 ||
                   Float.compare(bbox.getHeight(), fieldRect.getHeight()) != 0;
        }
    }

    private Map<COSDictionary,Set<COSDictionary>> buildPagesWidgetsMap(List<PDField> fields) throws IOException
    {
        Map<COSDictionary,Set<COSDictionary>> pagesAnnotationsMap =
                new HashMap<COSDictionary, Set<COSDictionary>>();
        boolean hasMissingPageRef = false;
        
        for (PDField field : fields)
        {
            List<PDAnnotationWidget> widgets = field.getWidgets();
            for (PDAnnotationWidget widget : widgets)
            {
                PDPage page = widget.getPage();
                if (page != null)
                {
                    fillPagesAnnotationMap(pagesAnnotationsMap, page, widget);
                }
                else
                {
                    hasMissingPageRef = true;
                }
            }
        }

        if (!hasMissingPageRef)
        {
            return pagesAnnotationsMap;
        }

        // If there is a widget with a missing page reference we need to build the map reverse i.e. 
        // from the annotations to the widget.
        LOG.warn("There has been a widget with a missing page reference, will check all page annotations");
        for (PDPage page : document.getPages())
        {
            for (PDAnnotation annotation : page.getAnnotations())
            {
                if (annotation instanceof PDAnnotationWidget)
                {
                    fillPagesAnnotationMap(pagesAnnotationsMap, page, (PDAnnotationWidget) annotation);
                }
            }
        }

        return pagesAnnotationsMap;
    }

    private void fillPagesAnnotationMap(Map<COSDictionary, Set<COSDictionary>> pagesAnnotationsMap,
            PDPage page, PDAnnotationWidget widget)
    {
        if (pagesAnnotationsMap.get(page.getCOSObject()) == null)
        {
            Set<COSDictionary> widgetsForPage = new HashSet<COSDictionary>();
            widgetsForPage.add(widget.getCOSObject());
            pagesAnnotationsMap.put(page.getCOSObject(), widgetsForPage);
        }
        else
        {
            Set<COSDictionary> widgetsForPage = pagesAnnotationsMap.get(page.getCOSObject());
            widgetsForPage.add(widget.getCOSObject());
        }
    }

    private void removeFields(List<PDField> fields)
    {
        for (PDField field : fields)
        {
            COSArray array;
            if (field.getParent() == null)
            {
                // if the field has no parent, assume it is at root level list, remove it from there
                array = (COSArray) dictionary.getDictionaryObject(COSName.FIELDS);
            }
            else
            {
                // if the field has a parent, then remove from the list there
                array = (COSArray) field.getParent().getCOSObject().getDictionaryObject(COSName.KIDS);
            }
            array.removeObject(field.getCOSObject());
        }
    }
}

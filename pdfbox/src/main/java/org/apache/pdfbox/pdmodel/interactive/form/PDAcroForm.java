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

import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;

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

    private ScriptingHandler scriptingHandler;

    /**
     * Constructor.
     *
     * @param doc The document that this form is part of.
     */
    public PDAcroForm(final PDDocument doc)
    {
        document = doc;
        dictionary = new COSDictionary();
        dictionary.setItem(COSName.FIELDS, new COSArray());
    }

    /**
     * Constructor.
     *
     * @param doc The document that this form is part of.
     * @param form The existing acroForm.
     */
    public PDAcroForm(final PDDocument doc, final COSDictionary form)
    {
        document = doc;
        dictionary = form;
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
    public void importFDF(final FDFDocument fdf) throws IOException
    {
        final List<FDFField> fields = fdf.getCatalog().getFDF().getFields();
        if (fields != null)
        {
            for (final FDFField field : fields)
            {
                final FDFField fdfField = field;
                final PDField docField = getField(fdfField.getPartialFieldName());
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
        final FDFDocument fdf = new FDFDocument();
        final FDFCatalog catalog = fdf.getCatalog();
        final FDFDictionary fdfDict = new FDFDictionary();
        catalog.setFDF(fdfDict);

        final List<FDFField> fdfFields = new ArrayList<>();
        final List<PDField> fields = getFields();
        for (final PDField field : fields)
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
        
        final List<PDField> fields = new ArrayList<>();
        for (final PDField field: getFieldTree())
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
    public void flatten(final List<PDField> fields, final boolean refreshAppearances) throws IOException
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

        final Map<COSDictionary,Set<COSDictionary>> pagesWidgetsMap = buildPagesWidgetsMap(fields);
        
        // preserve all non widget annotations
        for (final PDPage page : document.getPages())
        {
            final Set<COSDictionary> widgetsForPageMap = pagesWidgetsMap.get(page.getCOSObject());

            // indicates if the original content stream
            // has been wrapped in a q...Q pair.
            boolean isContentStreamWrapped = false;

            final List<PDAnnotation> annotations = new ArrayList<>();
                       
            for (final PDAnnotation annotation: page.getAnnotations())
            {                
                if (widgetsForPageMap == null || !widgetsForPageMap.contains(annotation.getCOSObject()))
                {
                    annotations.add(annotation);                 
                }
                else if (isVisibleAnnotation(annotation))
                {
                    contentStream = new PDPageContentStream(document, page, AppendMode.APPEND, true, !isContentStreamWrapped);
                    isContentStreamWrapped = true;

                    final PDAppearanceStream appearanceStream = annotation.getNormalAppearanceStream();
                    
                    final PDFormXObject fieldObject = new PDFormXObject(appearanceStream.getCOSObject());
                    
                    contentStream.saveGraphicsState();

                    // see https://stackoverflow.com/a/54091766/1729265 for an explanation
                    // of the steps required
                    // this will transform the appearance stream form object into the rectangle of the
                    // annotation bbox and map the coordinate systems
                    final Matrix transformationMatrix = resolveTransformationMatrix(annotation, appearanceStream);

                    contentStream.transform(transformationMatrix);
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

    private boolean isVisibleAnnotation(final PDAnnotation annotation)
    {
        if (annotation.isInvisible() || annotation.isHidden())
        {
            return false;
        }
        final PDAppearanceStream normalAppearanceStream = annotation.getNormalAppearanceStream();
        if (normalAppearanceStream == null)
        {
            return false;
        }
        final PDRectangle bbox = normalAppearanceStream.getBBox();
        return bbox != null && bbox.getWidth() > 0 && bbox.getHeight() > 0;
    }

    /**
     * Refreshes the appearance streams and appearance dictionaries for 
     * the widget annotations of all fields.
     * 
     * @throws IOException
     */
    public void refreshAppearances() throws IOException
    {
        for (final PDField field : getFieldTree())
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
    public void refreshAppearances(final List<PDField> fields) throws IOException
    {
        for (final PDField field : fields)
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
        final COSArray cosFields = dictionary.getCOSArray(COSName.FIELDS);
        if (cosFields == null)
        {
            return Collections.emptyList();
        }
        final List<PDField> pdFields = new ArrayList<>();
        for (int i = 0; i < cosFields.size(); i++)
        {
            final COSDictionary element = (COSDictionary) cosFields.getObject(i);
            if (element != null)
            {
                final PDField field = PDField.fromDictionary(this, element, null);
                if (field != null)
                {
                    pdFields.add(field);
                }
            }
        }
        return new COSArrayList<>(pdFields, cosFields);
    }

    /**
     * Set the documents root fields.
     *
     * @param fields The fields that are part of the documents root fields.
     */
    public void setFields(final List<PDField> fields)
    {
        dictionary.setItem(COSName.FIELDS, new COSArray(fields));
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
    public void setCacheFields(final boolean cache)
    {
        if (cache)
        {
            fieldCache = new HashMap<>();

            for (final PDField field : getFieldTree())
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
    public PDField getField(final String fullyQualifiedName)
    {
        // get the field from the cache if there is one.
        if (fieldCache != null)
        {
            return fieldCache.get(fullyQualifiedName);
        }

        // get the field from the field tree
        for (final PDField field : getFieldTree())
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
    public void setDefaultAppearance(final String daValue)
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
    public void setNeedAppearances(final Boolean value)
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
        final COSBase base = dictionary.getDictionaryObject(COSName.DR);
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
    public void setDefaultResources(final PDResources dr)
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
        final COSBase base = dictionary.getDictionaryObject(COSName.XFA);
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
    public void setXFA(final PDXFAResource xfa)
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
        final COSNumber number = (COSNumber)dictionary.getDictionaryObject(COSName.Q);
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
    public void setQ(final int q)
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
    public void setSignaturesExist(final boolean signaturesExist)
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
     * Set a handler to support JavaScript actions in the form.
     * 
     * @return scriptingHandler
     */
    public ScriptingHandler getScriptingHandler()
    {
        return scriptingHandler;
    }

    /**
     * Set a handler to support JavaScript actions in the form.
     * 
     * @param scriptingHandler
     */
    public void setScriptingHandler(final ScriptingHandler scriptingHandler)
    {
        this.scriptingHandler = scriptingHandler;
    }

    /**
     * Set the AppendOnly bit.
     *
     * @param appendOnly The value for AppendOnly.
     */
    public void setAppendOnly(final boolean appendOnly)
    {
        dictionary.setFlag(COSName.SIG_FLAGS, FLAG_APPEND_ONLY, appendOnly);
    }

    private Matrix resolveTransformationMatrix(final PDAnnotation annotation, final PDAppearanceStream appearanceStream)
    {
        // 1st step transform appearance stream bbox with appearance stream matrix
        final Rectangle2D transformedAppearanceBox = getTransformedAppearanceBBox(appearanceStream);
        final PDRectangle annotationRect = annotation.getRectangle();

        // 2nd step caclulate matrix to transform calculated rectangle into the annotation Rect boundaries
        final Matrix transformationMatrix = new Matrix();
        transformationMatrix.translate((float) (annotationRect.getLowerLeftX()-transformedAppearanceBox.getX()), (float) (annotationRect.getLowerLeftY()-transformedAppearanceBox.getY()));
        transformationMatrix.scale((float) (annotationRect.getWidth()/transformedAppearanceBox.getWidth()), (float) (annotationRect.getHeight()/transformedAppearanceBox.getHeight()));
        return transformationMatrix;
    }

    /**
     * Calculate the transformed appearance box.
     * 
     * Apply the Matrix (or an identity transform) to the BBox of
     * the appearance stream
     * 
     * @param appearanceStream
     * @return the transformed rectangle
     */
    private Rectangle2D getTransformedAppearanceBBox(final PDAppearanceStream appearanceStream)
    {
        final Matrix appearanceStreamMatrix = appearanceStream.getMatrix();
        final PDRectangle appearanceStreamBBox = appearanceStream.getBBox();
        final GeneralPath transformedAppearanceBox = appearanceStreamBBox.transform(appearanceStreamMatrix);
        return transformedAppearanceBox.getBounds2D();
    }
    
    private Map<COSDictionary,Set<COSDictionary>> buildPagesWidgetsMap(final List<PDField> fields) throws IOException
    {
        final Map<COSDictionary,Set<COSDictionary>> pagesAnnotationsMap = new HashMap<>();
        boolean hasMissingPageRef = false;
        
        for (final PDField field : fields)
        {
            final List<PDAnnotationWidget> widgets = field.getWidgets();
            for (final PDAnnotationWidget widget : widgets)
            {
                final PDPage page = widget.getPage();
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
        for (final PDPage page : document.getPages())
        {
            for (final PDAnnotation annotation : page.getAnnotations())
            {
                if (annotation instanceof PDAnnotationWidget)
                {
                    fillPagesAnnotationMap(pagesAnnotationsMap, page, (PDAnnotationWidget) annotation);
                }
            }
        }

        return pagesAnnotationsMap;
    }

    private void fillPagesAnnotationMap(final Map<COSDictionary, Set<COSDictionary>> pagesAnnotationsMap,
                                        final PDPage page, final PDAnnotationWidget widget)
    {
        if (pagesAnnotationsMap.get(page.getCOSObject()) == null)
        {
            final Set<COSDictionary> widgetsForPage = new HashSet<>();
            widgetsForPage.add(widget.getCOSObject());
            pagesAnnotationsMap.put(page.getCOSObject(), widgetsForPage);
        }
        else
        {
            final Set<COSDictionary> widgetsForPage = pagesAnnotationsMap.get(page.getCOSObject());
            widgetsForPage.add(widget.getCOSObject());
        }
    }

    private void removeFields(final List<PDField> fields)
    {
        for (final PDField field : fields)
        {
            final COSArray array;
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

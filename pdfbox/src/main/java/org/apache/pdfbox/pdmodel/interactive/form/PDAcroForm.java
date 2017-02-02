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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
     * Constructor.
     *
     * @param doc The document that this form is part of.
     * @param form The existing acroForm.
     */
    public PDAcroForm(PDDocument doc, COSDictionary form)
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
    	
        // indicates if the original content stream
        // has been wrapped in a q...Q pair.
        boolean isContentStreamWrapped = false;
        
        // the content stream to write to
        PDPageContentStream contentStream;
        
        // Hold a reference between the annotations and the page they are on.
        // This will only be used in case a PDAnnotationWidget doesn't contain
        // a /P entry specifying the page it's on as the /P entry is optional
        Map<COSDictionary, Integer> annotationToPageRef = null;
        
        // Iterate over all form fields and their widgets and create a
        // FormXObject at the page content level from that
        for (PDField field : fields)
        {
            for (PDAnnotationWidget widget : field.getWidgets())
            {
                if (!widget.isInvisible() && !widget.isHidden() && widget.getNormalAppearanceStream() != null)
                {
                    PDPage page = widget.getPage();

                    // resolve the page from looking at the annotations
                    if (widget.getPage() == null) {
                    	if (annotationToPageRef == null) {
                    		annotationToPageRef = buildAnnotationToPageRef();
                    	}
                    	Integer pageRef = annotationToPageRef.get(widget.getCOSObject());
                    	if (pageRef != null) {
                    		page = document.getPage(pageRef);
                    	}
                    }

                    if (!isContentStreamWrapped)
                    {
                        contentStream = new PDPageContentStream(document, page, AppendMode.APPEND, true);
                        isContentStreamWrapped = true;
                    }
                    else
                    {
                        contentStream = new PDPageContentStream(document, page, AppendMode.APPEND, true);
                    }
                    
                    PDAppearanceStream appearanceStream = widget.getNormalAppearanceStream();
                    
                    PDFormXObject fieldObject = new PDFormXObject(appearanceStream.getCOSObject());
                    
                    contentStream.saveGraphicsState();
                    
                    // translate the appearance stream to the widget location if there is 
                    // not already a transformation in place
                    boolean needsTranslation = resolveNeedsTranslation(appearanceStream);
                                        
                    // scale the appearance stream - mainly needed for images
                    // in buttons and signatures
                    boolean needsScaling = resolveNeedsScaling(appearanceStream);
                    
                    Matrix transformationMatrix = new Matrix();
                    boolean transformed = false;
                    
                    if (needsTranslation)
                    {
                    	transformationMatrix.translate(widget.getRectangle().getLowerLeftX(),
                                widget.getRectangle().getLowerLeftY());
                    	transformed = true;
                    }

                    if (needsScaling)
                    {                    
	                    PDRectangle bbox = appearanceStream.getBBox();
	                    PDRectangle fieldRect = widget.getRectangle();
	                    
	                    if (bbox.getWidth() - fieldRect.getWidth() != 0 && bbox.getHeight() - fieldRect.getHeight() != 0)
	                    {
	                    	float xScale = fieldRect.getWidth() / bbox.getWidth();
	                    	float yScale = fieldRect.getHeight() / bbox.getHeight();
	                    	Matrix scalingMatrix = Matrix.getScaleInstance(xScale, yScale);
	                    	transformationMatrix.concatenate(scalingMatrix);
	                    	transformed = true;
	                    }
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
        }

        // preserve all non widget annotations
        for (PDPage page : document.getPages())
        {
            List<PDAnnotation> annotations = new ArrayList<PDAnnotation>();
            
            for (PDAnnotation annotation: page.getAnnotations())
            {
                if (!(annotation instanceof PDAnnotationWidget))
                {
                    annotations.add(annotation);                 
                }
            }
            page.setAnnotations(annotations);
        }
        
        // remove the fields
        setFields(Collections.<PDField>emptyList());
        
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
     * This will get the default resources for the acro form.
     *
     * @return The default resources.
     */
    public PDResources getDefaultResources()
    {
        PDResources retval = null;
        COSDictionary dr = (COSDictionary) dictionary.getDictionaryObject(COSName.DR);
        if (dr != null)
        {
            retval = new PDResources(dr, document.getResourceCache());
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
     * This will get the 'quadding' or justification of the text to be displayed.
     * 0 - Left(default)<br>
     * 1 - Centered<br>
     * 2 - Right<br>
     * Please see the QUADDING_CONSTANTS.
     *
     * @return The justification of the text strings.
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
     * This will set the quadding/justification of the text.  See QUADDING constants.
     *
     * @param q The new text justification.
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
    
    private Map<COSDictionary, Integer> buildAnnotationToPageRef() {
    	Map<COSDictionary, Integer> annotationToPageRef = new HashMap<COSDictionary, Integer>();
    	
    	int idx = 0;
    	for (PDPage page : document.getPages()) {
    		try {
				for (PDAnnotation annotation : page.getAnnotations()) {
					if (annotation instanceof PDAnnotationWidget) {
						annotationToPageRef.put(annotation.getCOSObject(), idx);
					}
				}
			} catch (IOException e) {
				LOG.warn("Can't retriev annotations for page " + idx);
			}
    		idx++;
    	}    	
    	return annotationToPageRef;
    }
    
    /**
     * Check if there is a translation needed to place the annotations content.
     * 
     * @param appearanceStream
     * @return the need for a translation transformation.
     */
    private boolean resolveNeedsTranslation(PDAppearanceStream appearanceStream)
    {
        boolean needsTranslation = false;
        
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
                        if (llX == 0 && llY == 0)
                        {
                            needsTranslation = true;
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
     * @param appearanceStream
     * @return the need for a scaling transformation.
     */    
    private boolean resolveNeedsScaling(PDAppearanceStream appearanceStream)
    {
        // Check if there is a transformation within the XObjects content
    	PDResources resources = appearanceStream.getResources();
        return resources != null && resources.getXObjectNames().iterator().hasNext();
    }
    
}

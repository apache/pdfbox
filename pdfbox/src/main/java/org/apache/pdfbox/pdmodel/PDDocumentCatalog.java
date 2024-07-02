/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pdfbox.pdmodel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.common.COSArrayList;
import org.apache.pdfbox.pdmodel.common.COSObjectable;
import org.apache.pdfbox.pdmodel.common.PDDestinationOrAction;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.pdfbox.pdmodel.common.PDPageLabels;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDMarkInfo;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDStructureTreeRoot;
import org.apache.pdfbox.pdmodel.fixup.AcroFormDefaultFixup;
import org.apache.pdfbox.pdmodel.fixup.PDDocumentFixup;
import org.apache.pdfbox.pdmodel.graphics.color.PDOutputIntent;
import org.apache.pdfbox.pdmodel.graphics.optionalcontent.PDOptionalContentProperties;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionFactory;
import org.apache.pdfbox.pdmodel.interactive.action.PDDocumentCatalogAdditionalActions;
import org.apache.pdfbox.pdmodel.interactive.action.PDURIDictionary;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDNamedDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.pagenavigation.PDThread;
import org.apache.pdfbox.pdmodel.interactive.viewerpreferences.PDViewerPreferences;

/**
 * The Document Catalog of a PDF.
 *
 * @author Ben Litchfield
 */
public class PDDocumentCatalog implements COSObjectable
{
    private static final Logger LOG = LogManager.getLogger(PDDocumentCatalog.class);
    
    private final COSDictionary root;
    private final PDDocument document;
    private PDDocumentFixup acroFormFixupApplied;
    private PDAcroForm cachedAcroForm;

    /**
     * Constructor. Internal PDFBox use only! If you need to get the document catalog, call
     * {@link PDDocument#getDocumentCatalog()}.
     *
     * @param doc The document that this catalog is part of.
     */
    protected PDDocumentCatalog(PDDocument doc)
    {
        document = doc;
        root = new COSDictionary();
        root.setItem(COSName.TYPE, COSName.CATALOG);
        document.getDocument().getTrailer().setItem(COSName.ROOT, root);
    }

    /**
     * Constructor. Internal PDFBox use only! If you need to get the document catalog, call
     * {@link PDDocument#getDocumentCatalog()}.
     *
     * @param doc The document that this catalog is part of.
     * @param rootDictionary The root dictionary that this object wraps.
     */
    protected PDDocumentCatalog(PDDocument doc, COSDictionary rootDictionary)
    {
        document = doc;
        root = rootDictionary;
    }

    /**
     * Convert this standard java object to a COS object.
     *
     * @return The cos object that matches this Java object.
     */
    @Override
    public COSDictionary getCOSObject()
    {
        return root;
    }

    /**
     * Get the documents AcroForm. This will return null if no AcroForm is part of the document.
     *
     * @return The document's AcroForm.
     */
    public PDAcroForm getAcroForm()
    {
        return getAcroForm(new AcroFormDefaultFixup(document));
    }

    /**
     * Get the documents AcroForm. This will return null if no AcroForm is part of the document.
     *
     * Dependent on setting <code>acroFormFixup</code> some fixing/changes will be done to the AcroForm.
     * If you need to ensure that there are no fixes applied call <code>getAcroForm</code> with <code>null</code>.
     * 
     * Using <code>getAcroForm(PDDocumentFixup acroFormFixup)</code> might change the original content and
     * subsequent calls with <code>getAcroForm(null)</code> will return the changed content.
     * 
     * @param acroFormFixup the fix up action or null
     * @return The document's AcroForm.
     */
    public PDAcroForm getAcroForm(PDDocumentFixup acroFormFixup)
    {
        if (acroFormFixup != null && acroFormFixup != acroFormFixupApplied)
        {
            acroFormFixup.apply();
            cachedAcroForm = null;
            acroFormFixupApplied =  acroFormFixup;
        }
        else if (acroFormFixupApplied != null)
        {
            LOG.debug("AcroForm content has already been retrieved with fixes applied - original content changed because of that");
        }
        if (cachedAcroForm == null)
        {
            COSDictionary dict = root.getCOSDictionary(COSName.ACRO_FORM);
            cachedAcroForm = dict == null ? null : new PDAcroForm(document, dict);
        }
        return cachedAcroForm;
    }

    /**
     * Sets the AcroForm for this catalog.
     *
     * @param acroForm The new AcroForm.
     */
    public void setAcroForm(PDAcroForm acroForm)
    {
        root.setItem(COSName.ACRO_FORM, acroForm);
        cachedAcroForm = null;
    }

    /**
     * Returns all pages in the document, as a page tree.
     * 
     * @return PDPageTree providing all pages of the document
     */
    public PDPageTree getPages()
    {
        // todo: cache me?
        return new PDPageTree(root.getCOSDictionary(COSName.PAGES), document);
    }

    /**
     * Get the viewer preferences associated with this document or null if they do not exist.
     *
     * @return The document's viewer preferences.
     */
    public PDViewerPreferences getViewerPreferences()
    {
        COSDictionary viewerPref = root.getCOSDictionary(COSName.VIEWER_PREFERENCES);
        return viewerPref != null ? new PDViewerPreferences(viewerPref) : null;
    }

    /**
     * Sets the viewer preferences.
     *
     * @param prefs The new viewer preferences.
     */
    public void setViewerPreferences(PDViewerPreferences prefs)
    {
        root.setItem(COSName.VIEWER_PREFERENCES, prefs);
    }

    /**
     * Get the outline associated with this document or null if it does not exist.
     *
     * @return The document's outline.
     */
    public PDDocumentOutline getDocumentOutline()
    {
        COSDictionary outlineDict = root.getCOSDictionary(COSName.OUTLINES);
        return outlineDict != null ? new PDDocumentOutline(outlineDict) : null;
    }

    /**
     * Sets the document outlines.
     *
     * @param outlines The new document outlines.
     */
    public void setDocumentOutline(PDDocumentOutline outlines)
    {
        root.setItem(COSName.OUTLINES, outlines);
    }

    /**
     * Returns the document's article threads.
     * 
     * @return a list of all threads of the document
     */
    public List<PDThread> getThreads()
    {
        COSArray array = root.getCOSArray(COSName.THREADS);
        if (array == null)
        {
            array = new COSArray();
            array.setDirect(false);
            root.setItem(COSName.THREADS, array);
        }
        List<PDThread> pdObjects = new ArrayList<>(array.size());
        for (int i = 0; i < array.size(); i++)
        {
            pdObjects.add(new PDThread((COSDictionary)array.getObject(i)));
        }
        return new COSArrayList<>(pdObjects, array);
    }

    /**
     * Sets the list of threads for this pdf document.
     *
     * @param threads The list of threads, or null to clear it.
     */
    public void setThreads(List<PDThread> threads)
    {
        COSArray threadsArray = new COSArray(threads);
        threadsArray.setDirect(false);
        root.setItem(COSName.THREADS, threadsArray);
    }

    /**
     * Get the metadata that is part of the document catalog. This will return null if there is no
     * meta data for this object.
     *
     * @return The metadata for this object.
     */
    public PDMetadata getMetadata()
    {
        COSStream metaObj = root.getCOSStream(COSName.METADATA);
        return metaObj != null ? new PDMetadata(metaObj) : null;
    }

    /**
     * Sets the metadata for this object. This can be null.
     *
     * @param meta The meta data for this object.
     */
    public void setMetadata(PDMetadata meta)
    {
        root.setItem(COSName.METADATA, meta);
    }

    /**
     * Sets the Document Open Action for this object.
     *
     * @param action The action you want to perform.
     */
    public void setOpenAction(PDDestinationOrAction action)
    {
        root.setItem(COSName.OPEN_ACTION, action);
    }

    /**
     * Get the Document Open Action for this object.
     *
     * @return The action to perform when the document is opened.
     * @throws IOException If there is an error creating the destination or action.
     */
    public PDDestinationOrAction getOpenAction() throws IOException
    {
        COSBase openAction = root.getDictionaryObject(COSName.OPEN_ACTION);
        if (openAction instanceof COSDictionary)
        {
            return PDActionFactory.createAction((COSDictionary)openAction);
        }
        else if (openAction instanceof COSArray)
        {
            return PDDestination.create(openAction);
        }
        else
        {
            return null;
        }
    }
    /**
     * @return The Additional Actions for this Document
     */
    public PDDocumentCatalogAdditionalActions getActions()
    {
        COSDictionary addAction = root.getCOSDictionary(COSName.AA);
        if (addAction == null)
        {
            addAction = new COSDictionary();
            root.setItem(COSName.AA, addAction);
        }
        return new PDDocumentCatalogAdditionalActions(addAction);
    }

    /**
     * Sets the additional actions for the document.
     *
     * @param actions The actions that are associated with this document.
     */
    public void setActions(PDDocumentCatalogAdditionalActions actions)
    {
        root.setItem(COSName.AA, actions);
    }

    /**
     * @return The names dictionary for this document or null if none exist.
     */
    public PDDocumentNameDictionary getNames()
    {
        COSDictionary names = root.getCOSDictionary(COSName.NAMES);
        return names == null ? null : new PDDocumentNameDictionary(this, names);
    }

    /**
     * @return The named destinations dictionary for this document or null if none exists.
     */
    public PDDocumentNameDestinationDictionary getDests()
    {
        COSDictionary dests = root.getCOSDictionary(COSName.DESTS);
        return dests != null ? new PDDocumentNameDestinationDictionary(dests) : null;
    }
    
    /**
     * Find the page destination from a named destination.
     * @param namedDest the named destination.
     * @return a PDPageDestination object or null if not found.
     * @throws IOException if there is an error creating the PDPageDestination object.
     */
    public PDPageDestination findNamedDestinationPage(PDNamedDestination namedDest)
            throws IOException
    {
        PDPageDestination pageDestination = null;
        PDDocumentNameDictionary namesDict = getNames();
        if (namesDict != null)
        {
            PDDestinationNameTreeNode destsTree = namesDict.getDests();
            if (destsTree != null)
            {
                pageDestination = destsTree.getValue(namedDest.getNamedDestination());
            }
        }
        if (pageDestination == null)
        {
            // Look up /Dests dictionary from catalog
            PDDocumentNameDestinationDictionary nameDestDict = getDests();
            if (nameDestDict != null)
            {
                String name = namedDest.getNamedDestination();
                pageDestination = (PDPageDestination) nameDestDict.getDestination(name);
            }
        }
        return pageDestination;
    }
    
    /**
     * Sets the names dictionary for the document.
     *
     * @param names The names dictionary that is associated with this document.
     */
    public void setNames(PDDocumentNameDictionary names)
    {
        root.setItem(COSName.NAMES, names);
    }

    /**
     * Get info about doc's usage of tagged features. This will return null if there is no
     * information.
     *
     * @return The new mark info.
     */
    public PDMarkInfo getMarkInfo()
    {
        COSDictionary dic = root.getCOSDictionary(COSName.MARK_INFO);
        return dic == null ? null : new PDMarkInfo(dic);
    }

    /**
     * Set information about the doc's usage of tagged features.
     *
     * @param markInfo The new MarkInfo data.
     */
    public void setMarkInfo(PDMarkInfo markInfo)
    {
        root.setItem(COSName.MARK_INFO, markInfo);
    }

    /**
     * Get the list of OutputIntents defined in the document.
     *
     * @return The list of PDOutputIntent, never null.
     */
    public List<PDOutputIntent> getOutputIntents()
    {
        List<PDOutputIntent> retval = new ArrayList<>();
        COSArray array = root.getCOSArray(COSName.OUTPUT_INTENTS);
        if (array != null)
        {
            for (COSBase cosBase : array)
            {
                if (cosBase instanceof COSObject)
                {
                    cosBase = ((COSObject)cosBase).getObject();
                }
                PDOutputIntent oi = new PDOutputIntent((COSDictionary) cosBase);
                retval.add(oi);
            }
        }
        return retval;
    }

    /**
     * Add an OutputIntent to the list.  If there is not OutputIntent, the list is created and the
     * first  element added.
     *
     * @param outputIntent the OutputIntent to add.
     */
    public void addOutputIntent(PDOutputIntent outputIntent)
    {
        COSArray array = root.getCOSArray(COSName.OUTPUT_INTENTS);
        if (array == null)
        {
            array = new COSArray();
            root.setItem(COSName.OUTPUT_INTENTS, array);
        }
        array.add(outputIntent.getCOSObject());
    }

    /**
     * Replace the list of OutputIntents of the document.
     *
     * @param outputIntents the list of OutputIntents, if the list is empty all OutputIntents are
     * removed.
     */
    public void setOutputIntents(List<PDOutputIntent> outputIntents) 
    {
        COSArray array = new COSArray();
        for (PDOutputIntent intent : outputIntents)
        {
            array.add(intent.getCOSObject());
        }
        root.setItem(COSName.OUTPUT_INTENTS, array);
    }

    /**
     * Returns the page display mode.
     * 
     * @return the PageMode of the document, if not present PageMode.USE_NONE is returned
     */
    public PageMode getPageMode()
    {
        String mode = root.getNameAsString(COSName.PAGE_MODE);
        if (mode != null)
        {
            try
            {
                return PageMode.fromString(mode);
            }
            catch (IllegalArgumentException e)
            {
                LOG.debug(() -> "Invalid PageMode used '" + mode + "' - setting to PageMode.USE_NONE", e);
                return PageMode.USE_NONE;
            }
        }
        else
        {
            return PageMode.USE_NONE;
        }
    }

    /**
     * Sets the page mode.
     *
     * @param mode The new page mode.
     */
    public void setPageMode(PageMode mode)
    {
        root.setName(COSName.PAGE_MODE, mode.stringValue());
    }

    /**
     * Returns the page layout.
     * 
     * @return the PageLayout of the document, if not present PageLayout.SINGLE_PAGE is returned
     */
    public PageLayout getPageLayout()
    {
        String mode = root.getNameAsString(COSName.PAGE_LAYOUT);
        if (mode != null && !mode.isEmpty())
        {
            try
            {
                return PageLayout.fromString(mode);
            }
            catch (IllegalArgumentException e)
            {
                LOG.warn(() -> "Invalid PageLayout used '" + mode + "' - returning PageLayout.SINGLE_PAGE",
                        e);
            }
        }
        return PageLayout.SINGLE_PAGE;
    }

    /**
     * Sets the page layout.
     *
     * @param layout The new page layout.
     */
    public void setPageLayout(PageLayout layout)
    {
        root.setName(COSName.PAGE_LAYOUT, layout.stringValue());
    }

    /**
     * Returns the document-level URI.
     * 
     * @return the document level URI if present, otherwise null
     */
    public PDURIDictionary getURI()
    {
        COSDictionary uri = root.getCOSDictionary(COSName.URI);
        return uri == null ? null : new PDURIDictionary(uri);
    }

    /**
     * Sets the document level URI.
     *
     * @param uri The new document level URI.
     */
    public void setURI(PDURIDictionary uri)
    {
        root.setItem(COSName.URI, uri);
    }

    /**
     * Get the document's structure tree root, or null if none exists.
     * 
     * @return the structure tree root if present, otherwise null
     */
    public PDStructureTreeRoot getStructureTreeRoot()
    {
        COSDictionary dict = root.getCOSDictionary(COSName.STRUCT_TREE_ROOT);
        return dict == null ? null : new PDStructureTreeRoot(dict);
    }

    /**
     * Sets the document's structure tree root.
     *
     * @param treeRoot The new structure tree.
     */
    public void setStructureTreeRoot(PDStructureTreeRoot treeRoot)
    {
        root.setItem(COSName.STRUCT_TREE_ROOT, treeRoot);
    }

    /**
     * Returns the language for the document, or null.
     * 
     * @return the language of the document if present, otherwise null
     */
    public String getLanguage()
    {
        return root.getString(COSName.LANG);
    }

    /**
     * Sets the Language for the document.
     *
     * @param language The new document language.
     */
    public void setLanguage(String language)
    {
        root.setString(COSName.LANG, language);
    }

    /**
     * Returns the PDF specification version this document conforms to.
     *
     * @return the PDF version (e.g. "1.4")
     */
    public String getVersion()
    {
        return root.getNameAsString(COSName.VERSION);
    }

    /**
     * Sets the PDF specification version this document conforms to.
     *
     * @param version the PDF version (e.g. "1.4")
     */
    public void setVersion(String version)
    {
        root.setName(COSName.VERSION, version);
    }

    /**
     * Returns the page labels descriptor of the document.
     *
     * @return the page labels descriptor of the document.
     * @throws IOException If there is a problem retrieving the page labels.
     */
    public PDPageLabels getPageLabels() throws IOException
    {
        COSDictionary dict = root.getCOSDictionary(COSName.PAGE_LABELS);
        return dict == null ? null : new PDPageLabels(document, dict);
    }

    /**
     * Sets the page label descriptor for the document.
     *
     * @param labels the new page label descriptor to set.
     */
    public void setPageLabels(PDPageLabels labels)
    {
        root.setItem(COSName.PAGE_LABELS, labels);
    }

    /**
     * Get the optional content properties dictionary associated with this document.
     *
     * @return the optional properties dictionary or null if it is not present
     */
    public PDOptionalContentProperties getOCProperties()
    {
        COSDictionary dict = root.getCOSDictionary(COSName.OCPROPERTIES);
        return dict == null ? null : new PDOptionalContentProperties(dict);
    }

    /**
     * Sets the optional content properties dictionary. The document version is incremented to 1.5
     * if lower.
     *
     * @param ocProperties the optional properties dictionary
     */
    public void setOCProperties(PDOptionalContentProperties ocProperties)
    {
        root.setItem(COSName.OCPROPERTIES, ocProperties);

        // optional content groups require PDF 1.5
        if (ocProperties != null && document.getVersion() < 1.5)
        {
            document.setVersion(1.5f);
        }
    }
}

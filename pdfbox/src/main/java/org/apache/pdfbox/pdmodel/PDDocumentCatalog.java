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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
    private static final Log LOG = LogFactory.getLog(PDDocumentCatalog.class);
    
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
    protected PDDocumentCatalog(final PDDocument doc)
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
    protected PDDocumentCatalog(final PDDocument doc, final COSDictionary rootDictionary)
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
    public PDAcroForm getAcroForm(final PDDocumentFixup acroFormFixup)
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
            final COSDictionary dict = (COSDictionary)root.getDictionaryObject(COSName.ACRO_FORM);
            cachedAcroForm = dict == null ? null : new PDAcroForm(document, dict);
        }
        return cachedAcroForm;
    }

    /**
     * Sets the AcroForm for this catalog.
     *
     * @param acroForm The new AcroForm.
     */
    public void setAcroForm(final PDAcroForm acroForm)
    {
        root.setItem(COSName.ACRO_FORM, acroForm);
        cachedAcroForm = null;
    }

    /**
     * Returns all pages in the document, as a page tree.
     */
    public PDPageTree getPages()
    {
        // todo: cache me?
        return new PDPageTree((COSDictionary)root.getDictionaryObject(COSName.PAGES), document);
    }

    /**
     * Get the viewer preferences associated with this document or null if they do not exist.
     *
     * @return The document's viewer preferences.
     */
    public PDViewerPreferences getViewerPreferences()
    {
        final COSBase base = root.getDictionaryObject(COSName.VIEWER_PREFERENCES);
        return base instanceof COSDictionary ? new PDViewerPreferences((COSDictionary) base) : null;
    }

    /**
     * Sets the viewer preferences.
     *
     * @param prefs The new viewer preferences.
     */
    public void setViewerPreferences(final PDViewerPreferences prefs)
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
        final COSBase cosObj = root.getDictionaryObject(COSName.OUTLINES);
        return cosObj instanceof COSDictionary ? new PDDocumentOutline((COSDictionary)cosObj) : null;
    }

    /**
     * Sets the document outlines.
     *
     * @param outlines The new document outlines.
     */
    public void setDocumentOutline(final PDDocumentOutline outlines)
    {
        root.setItem(COSName.OUTLINES, outlines);
    }

    /**
     * Returns the document's article threads.
     */
    public List<PDThread> getThreads()
    {
        COSArray array = (COSArray)root.getDictionaryObject(COSName.THREADS);
        if (array == null)
        {
            array = new COSArray();
            root.setItem(COSName.THREADS, array);
        }
        final List<PDThread> pdObjects = new ArrayList<>();
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
    public void setThreads(final List<PDThread> threads)
    {
        root.setItem(COSName.THREADS, new COSArray(threads));
    }

    /**
     * Get the metadata that is part of the document catalog. This will return null if there is no
     * meta data for this object.
     *
     * @return The metadata for this object.
     */
    public PDMetadata getMetadata()
    {
        final COSBase metaObj = root.getDictionaryObject(COSName.METADATA);
        if (metaObj instanceof COSStream)
        {
            return new PDMetadata((COSStream) metaObj);
        }
        return null;
    }

    /**
     * Sets the metadata for this object. This can be null.
     *
     * @param meta The meta data for this object.
     */
    public void setMetadata(final PDMetadata meta)
    {
        root.setItem(COSName.METADATA, meta);
    }

    /**
     * Sets the Document Open Action for this object.
     *
     * @param action The action you want to perform.
     */
    public void setOpenAction(final PDDestinationOrAction action)
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
        final COSBase openAction = root.getDictionaryObject(COSName.OPEN_ACTION);
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
        COSDictionary addAction = (COSDictionary) root.getDictionaryObject(COSName.AA);
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
    public void setActions(final PDDocumentCatalogAdditionalActions actions)
    {
        root.setItem(COSName.AA, actions);
    }

    /**
     * @return The names dictionary for this document or null if none exist.
     */
    public PDDocumentNameDictionary getNames()
    {
        final COSDictionary names = (COSDictionary) root.getDictionaryObject(COSName.NAMES);
        return names == null ? null : new PDDocumentNameDictionary(this, names);
    }

    /**
     * @return The named destinations dictionary for this document or null if none exists.
     */
    public PDDocumentNameDestinationDictionary getDests()
    {
        PDDocumentNameDestinationDictionary nameDic = null;
        final COSDictionary dests = (COSDictionary) root.getDictionaryObject(COSName.DESTS);
        if (dests != null)
        {
            nameDic = new PDDocumentNameDestinationDictionary(dests);
        }
        return nameDic;
    }
    
    /**
     * Find the page destination from a named destination.
     * @param namedDest the named destination.
     * @return a PDPageDestination object or null if not found.
     * @throws IOException if there is an error creating the PDPageDestination object.
     */
    public PDPageDestination findNamedDestinationPage(final PDNamedDestination namedDest)
            throws IOException
    {
        PDPageDestination pageDestination = null;
        final PDDocumentNameDictionary namesDict = getNames();
        if (namesDict != null)
        {
            final PDDestinationNameTreeNode destsTree = namesDict.getDests();
            if (destsTree != null)
            {
                pageDestination = destsTree.getValue(namedDest.getNamedDestination());
            }
        }
        if (pageDestination == null)
        {
            // Look up /Dests dictionary from catalog
            final PDDocumentNameDestinationDictionary nameDestDict = getDests();
            if (nameDestDict != null)
            {
                final String name = namedDest.getNamedDestination();
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
    public void setNames(final PDDocumentNameDictionary names)
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
        final COSDictionary dic = (COSDictionary)root.getDictionaryObject(COSName.MARK_INFO);
        return dic == null ? null : new PDMarkInfo(dic);
    }

    /**
     * Set information about the doc's usage of tagged features.
     *
     * @param markInfo The new MarkInfo data.
     */
    public void setMarkInfo(final PDMarkInfo markInfo)
    {
        root.setItem(COSName.MARK_INFO, markInfo);
    }

    /**
     * Get the list of OutputIntents defined in the document.
     *
     * @return The list of PDOutputIntent
     */
    public List<PDOutputIntent> getOutputIntents()
    {
        final List<PDOutputIntent> retval = new ArrayList<>();
        final COSArray array = (COSArray)root.getDictionaryObject(COSName.OUTPUT_INTENTS);
        if (array != null)
        {
            for (COSBase cosBase : array)
            {
                if (cosBase instanceof COSObject)
                {
                    cosBase = ((COSObject)cosBase).getObject();
                }
                final PDOutputIntent oi = new PDOutputIntent((COSDictionary) cosBase);
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
    public void addOutputIntent(final PDOutputIntent outputIntent)
    {
        COSArray array = (COSArray)root.getDictionaryObject(COSName.OUTPUT_INTENTS);
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
    public void setOutputIntents(final List<PDOutputIntent> outputIntents)
    {
        final COSArray array = new COSArray();
        for (final PDOutputIntent intent : outputIntents)
        {
            array.add(intent.getCOSObject());
        }
        root.setItem(COSName.OUTPUT_INTENTS, array);
    }

    /**
     * Returns the page display mode.
     */
    public PageMode getPageMode()
    {
        final String mode = root.getNameAsString(COSName.PAGE_MODE);
        if (mode != null)
        {
            try
            {
                return PageMode.fromString(mode);
            }
            catch (IllegalArgumentException e)
            {
                LOG.debug("Invalid PageMode used '" + mode + "' - setting to PageMode.USE_NONE", e);
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
    public void setPageMode(final PageMode mode)
    {
        root.setName(COSName.PAGE_MODE, mode.stringValue());
    }

    /**
     * Returns the page layout.
     */
    public PageLayout getPageLayout()
    {
        final String mode = root.getNameAsString(COSName.PAGE_LAYOUT);
        if (mode != null)
        {
            return PageLayout.fromString(mode);
        }
        else
        {
            return PageLayout.SINGLE_PAGE;
        }
    }

    /**
     * Sets the page layout.
     *
     * @param layout The new page layout.
     */
    public void setPageLayout(final PageLayout layout)
    {
        root.setName(COSName.PAGE_LAYOUT, layout.stringValue());
    }

    /**
     * Returns the document-level URI.
     */
    public PDURIDictionary getURI()
    {
        final COSDictionary uri = (COSDictionary)root.getDictionaryObject(COSName.URI);
        return uri == null ? null : new PDURIDictionary(uri);
    }

    /**
     * Sets the document level URI.
     *
     * @param uri The new document level URI.
     */
    public void setURI(final PDURIDictionary uri)
    {
        root.setItem(COSName.URI, uri);
    }

    /**
     * Get the document's structure tree root, or null if none exists.
     */
    public PDStructureTreeRoot getStructureTreeRoot()
    {
        final COSDictionary dict = root.getCOSDictionary(COSName.STRUCT_TREE_ROOT);
        return dict == null ? null : new PDStructureTreeRoot(dict);
    }

    /**
     * Sets the document's structure tree root.
     *
     * @param treeRoot The new structure tree.
     */
    public void setStructureTreeRoot(final PDStructureTreeRoot treeRoot)
    {
        root.setItem(COSName.STRUCT_TREE_ROOT, treeRoot);
    }

    /**
     * Returns the language for the document, or null.
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
    public void setLanguage(final String language)
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
    public void setVersion(final String version)
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
        final COSDictionary dict = (COSDictionary) root.getDictionaryObject(COSName.PAGE_LABELS);
        return dict == null ? null : new PDPageLabels(document, dict);
    }

    /**
     * Sets the page label descriptor for the document.
     *
     * @param labels the new page label descriptor to set.
     */
    public void setPageLabels(final PDPageLabels labels)
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
        final COSDictionary dict = (COSDictionary)root.getDictionaryObject(COSName.OCPROPERTIES);
        return dict == null ? null : new PDOptionalContentProperties(dict);
    }

    /**
     * Sets the optional content properties dictionary. The document version is incremented to 1.5
     * if lower.
     *
     * @param ocProperties the optional properties dictionary
     */
    public void setOCProperties(final PDOptionalContentProperties ocProperties)
    {
        root.setItem(COSName.OCPROPERTIES, ocProperties);

        // optional content groups require PDF 1.5
        if (ocProperties != null && document.getVersion() < 1.5)
        {
            document.setVersion(1.5f);
        }
    }
}

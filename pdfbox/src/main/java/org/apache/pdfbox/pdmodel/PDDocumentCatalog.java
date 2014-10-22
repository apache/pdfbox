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

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.common.COSArrayList;
import org.apache.pdfbox.pdmodel.common.COSObjectable;
import org.apache.pdfbox.pdmodel.common.PDDestinationOrAction;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.pdfbox.pdmodel.common.PDPageLabels;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDMarkInfo;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDStructureTreeRoot;
import org.apache.pdfbox.pdmodel.graphics.color.PDOutputIntent;
import org.apache.pdfbox.pdmodel.graphics.optionalcontent.PDOptionalContentProperties;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionFactory;
import org.apache.pdfbox.pdmodel.interactive.action.PDDocumentCatalogAdditionalActions;
import org.apache.pdfbox.pdmodel.interactive.action.PDURIDictionary;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDDestination;
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
    /**
     * Page mode where neither the outline nor the thumbnails are displayed.
     */
    public static final String PAGE_MODE_USE_NONE = "UseNone";
    /**
     * Show bookmarks when pdf is opened.
     */
    public static final String PAGE_MODE_USE_OUTLINES = "UseOutlines";
    /**
     * Show thumbnails when pdf is opened.
     */
    public static final String PAGE_MODE_USE_THUMBS = "UseThumbs";
    /**
     * Full screen mode with no menu bar, window controls.
     */
    public static final String PAGE_MODE_FULL_SCREEN = "FullScreen";
    /**
     * Optional content group panel is visible when opened.
     */
    public static final String PAGE_MODE_USE_OPTIONAL_CONTENT = "UseOC";
    /**
     * Attachments panel is visible.
     */
    public static final String PAGE_MODE_USE_ATTACHMENTS = "UseAttachments";

    /**
     * Display one page at a time.
     */
    public static final String PAGE_LAYOUT_SINGLE_PAGE = "SinglePage";
    /**
     * Display the pages in one column.
     */
    public static final String PAGE_LAYOUT_ONE_COLUMN = "OneColumn";
    /**
     * Display the pages in two columns, with odd numbered pagse on the left.
     */
    public static final String PAGE_LAYOUT_TWO_COLUMN_LEFT = "TwoColumnLeft";
    /**
     * Display the pages in two columns, with odd numbered pagse on the right.
     */
    public static final String PAGE_LAYOUT_TWO_COLUMN_RIGHT ="TwoColumnRight";
    /**
     * Display the pages two at a time, with odd-numbered pages on the left.
     */
    public static final String PAGE_LAYOUT_TWO_PAGE_LEFT = "TwoPageLeft";
    /**
     * Display the pages two at a time, with odd-numbered pages on the right.
     */
    public static final String PAGE_LAYOUT_TWO_PAGE_RIGHT = "TwoPageRight";

    private final COSDictionary root;
    private final PDDocument document;
    private PDAcroForm cachedAcroForm;

    /**
     * Constructor.
     *
     * @param doc The document that this catalog is part of.
     */
    public PDDocumentCatalog(PDDocument doc)
    {
        document = doc;
        root = new COSDictionary();
        root.setItem(COSName.TYPE, COSName.CATALOG);
        document.getDocument().getTrailer().setItem(COSName.ROOT, root);
    }

    /**
     * Constructor.
     *
     * @param doc The document that this catalog is part of.
     * @param rootDictionary The root dictionary that this object wraps.
     */
    public PDDocumentCatalog(PDDocument doc, COSDictionary rootDictionary)
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
    public COSBase getCOSObject()
    {
        return root;
    }

    /**
     * Convert this standard java object to a COS object.
     *
     * @return The cos object that matches this Java object.
     */
    public COSDictionary getCOSDictionary()
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
        if(cachedAcroForm == null)
        {
            COSDictionary acroFormDic =
                (COSDictionary)root.getDictionaryObject(COSName.ACRO_FORM);
            if(acroFormDic != null)
            {
                cachedAcroForm = new PDAcroForm(document, acroFormDic);
            }
        }
        return cachedAcroForm;
    }

    /**
     * Sets the acro form for this catalog.
     *
     * @param acro The new acro form.
     */
    public void setAcroForm(PDAcroForm acro)
    {
        root.setItem(COSName.ACRO_FORM, acro);
    }

    /**
     * This will get the root node for the pages.
     *
     * @return The parent page node.
     */
    public PDPageNode getPages()
    {
        return new PDPageNode((COSDictionary)root.getDictionaryObject(COSName.PAGES));
    }

    /**
     * The PDF document contains a hierarchical structure of PDPageNode and PDPages, which is mostly
     * just a way to store this information. This method will return a flat list of all PDPage
     * objects in this document.
     *
     * @return A list of PDPage objects.
     */
    public List getAllPages()
    {
        List retval = new ArrayList();
        PDPageNode rootNode = getPages();
        //old (slower):
        //getPageObjects(rootNode, retval);
        rootNode.getAllKids(retval);
        return retval;
    }

    /**
     * Get the viewer preferences associated with this document or null if they do not exist.
     *
     * @return The document's viewer preferences.
     */
    public PDViewerPreferences getViewerPreferences()
    {
        PDViewerPreferences retval = null;
        COSDictionary dict = (COSDictionary)root.getDictionaryObject(COSName.VIEWER_PREFERENCES);
        if(dict != null)
        {
            retval = new PDViewerPreferences(dict);
        }

        return retval;
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
        PDDocumentOutline retval = null;
        COSDictionary dict = (COSDictionary)root.getDictionaryObject(COSName.OUTLINES);
        if(dict != null)
        {
            retval = new PDDocumentOutline(dict);
        }

        return retval;
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
     * Get the list threads for this pdf document.
     *
     * @return A list of PDThread objects.
     */
    public List getThreads()
    {
        COSArray array = (COSArray)root.getDictionaryObject(COSName.THREADS);
        if(array == null)
        {
            array = new COSArray();
            root.setItem(COSName.THREADS, array);
        }
        List pdObjects = new ArrayList();
        for(int i=0; i<array.size(); i++)
        {
            pdObjects.add(new PDThread((COSDictionary)array.getObject(i)));
        }
        return new COSArrayList(pdObjects, array);
    }

    /**
     * Sets the list of threads for this pdf document.
     *
     * @param threads The list of threads, or null to clear it.
     */
    public void setThreads(List threads)
    {
        root.setItem(COSName.THREADS, COSArrayList.converterToCOSArray(threads));
    }

    /**
     * Get the metadata that is part of the document catalog. This will return null if there is no
     * meta data for this object.
     *
     * @return The metadata for this object.
     */
    public PDMetadata getMetadata()
    {
        PDMetadata retval = null;
        COSBase metaObj = root.getDictionaryObject(COSName.METADATA);
        if (metaObj instanceof COSStream)
        {
            retval = new PDMetadata((COSStream) metaObj);
        }
        return retval;
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
        PDDestinationOrAction action = null;
        COSBase actionObj = root.getDictionaryObject(COSName.OPEN_ACTION);

        if(actionObj == null)
        {
            //no op
        }
        else if(actionObj instanceof COSDictionary)
        {
            action = PDActionFactory.createAction((COSDictionary)actionObj);
        }
        else if(actionObj instanceof COSArray)
        {
            action = PDDestination.create(actionObj);
        }
        else
        {
            throw new IOException("Unknown OpenAction " + actionObj);
        }

        return action;
    }
    /**
     * @return The Additional Actions for this Document
     */
    public PDDocumentCatalogAdditionalActions getActions()
    {
        COSDictionary addAct = (COSDictionary) root.getDictionaryObject(COSName.AA);
        if (addAct == null)
        {
            addAct = new COSDictionary();
            root.setItem(COSName.AA, addAct);
        }
        return new PDDocumentCatalogAdditionalActions(addAct);
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
        PDDocumentNameDictionary nameDic = null;
        COSDictionary names = (COSDictionary) root.getDictionaryObject(COSName.NAMES);
        if(names != null)
        {
            nameDic = new PDDocumentNameDictionary(this,names);
        }
        return nameDic;
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
        PDMarkInfo retval = null;
        COSDictionary dic = (COSDictionary)root.getDictionaryObject(COSName.MARK_INFO);
        if(dic != null)
        {
            retval = new PDMarkInfo(dic);
        }
        return retval;
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
     * @return The list of PDOutputIntent
     */
    public List<PDOutputIntent> getOutputIntent () {
        List<PDOutputIntent> retval = new ArrayList<PDOutputIntent>();
        COSArray array = (COSArray)root.getItem(COSName.OUTPUT_INTENTS);
        if (array!=null) {
            for (COSBase cosBase : array)
            {
                PDOutputIntent oi = new PDOutputIntent((COSStream)cosBase);
                retval.add(oi);
            }
        }
        return retval;
    }

    /**
     * Add an OutputIntent to the list.
     *
     * If there is not OutputIntent, the list is created and the first  element added.
     *
     * @param outputIntent the OutputIntent to add.
     */
    public void addOutputIntent (PDOutputIntent outputIntent) {
        COSArray array = (COSArray)root.getItem(COSName.OUTPUT_INTENTS);
        if (array==null) {
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
    public void setOutputIntents (List<PDOutputIntent> outputIntents) {
        COSArray array = new COSArray();
        for (PDOutputIntent intent : outputIntents)
        {
            array.add(intent.getCOSObject());
        }
        root.setItem(COSName.OUTPUT_INTENTS, array);
    }

    /**
     * Sets the page display mode, see the PAGE_MODE_XXX constants.
     *
     * @return A string representing the page mode.
     */
    public String getPageMode()
    {
        return root.getNameAsString(COSName.PAGE_MODE, PAGE_MODE_USE_NONE);
    }

    /**
     * Sets the page mode. See the PAGE_MODE_XXX constants for valid values.
     *
     * @param mode The new page mode.
     */
    public void setPageMode(String mode)
    {
        root.setName(COSName.PAGE_MODE, mode);
    }

    /**
     * Sets the page layout, see the PAGE_LAYOUT_XXX constants.
     *
     * @return A string representing the page layout.
     */
    public String getPageLayout()
    {
        return root.getNameAsString(COSName.PAGE_LAYOUT, PAGE_LAYOUT_SINGLE_PAGE);
    }

    /**
     * Sets the page layout. See the PAGE_LAYOUT_XXX constants for valid values.
     *
     * @param layout The new page layout.
     */
    public void setPageLayout(String layout)
    {
        root.setName(COSName.PAGE_LAYOUT, layout);
    }

    /**
     * Document level information in the URI.
     *
     * @return Document level URI.
     */
    public PDURIDictionary getURI()
    {
        PDURIDictionary retval = null;
        COSDictionary uri = (COSDictionary)root.getDictionaryObject(COSName.URI);
        if(uri != null)
        {
            retval = new PDURIDictionary(uri);
        }
        return retval;
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
     * Get the document's structure tree root.
     *
     * @return The document's structure tree root or null if none exists.
     */
    public PDStructureTreeRoot getStructureTreeRoot()
    {
        PDStructureTreeRoot treeRoot = null;
        COSDictionary dic = (COSDictionary)root.getDictionaryObject(COSName.STRUCT_TREE_ROOT);
        if(dic != null)
        {
            treeRoot = new PDStructureTreeRoot(dic);
        }
        return treeRoot;
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
     * The language for the document.
     *
     * @return The language for the document.
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
     * @return The PDF version.
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
        PDPageLabels labels = null;
        COSDictionary dict = (COSDictionary) root.getDictionaryObject(COSName.PAGE_LABELS);
        if (dict != null)
        {
            labels = new PDPageLabels(document, dict);
        }
        return labels;
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
        COSDictionary dict = (COSDictionary)root.getDictionaryObject(COSName.OCPROPERTIES);
        if (dict != null)
        {
            return new PDOptionalContentProperties(dict);
        }
        return null;
    }

    /**
     * Sets the optional content properties dictionary.
     *
     * @param ocProperties the optional properties dictionary
     */
    public void setOCProperties(PDOptionalContentProperties ocProperties)
    {
        root.setItem(COSName.OCPROPERTIES, ocProperties);
    }
}

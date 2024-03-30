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
package org.apache.pdfbox.multipdf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.io.RandomAccessStreamCache.StreamCacheCreateFunction;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.PDStructureElementNameTreeNode;
import org.apache.pdfbox.pdmodel.common.COSObjectable;
import org.apache.pdfbox.pdmodel.common.PDNameTreeNode;
import org.apache.pdfbox.pdmodel.common.PDNumberTreeNode;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDParentTreeValue;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDStructureElement;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDStructureTreeRoot;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.action.PDAction;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionFactory;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionGoTo;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceStream;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageDestination;

/**
 * Split a document into several other documents.
 *
 * @author Mario Ivankovits
 * @author Ben Litchfield
 * @author Tilman Hausherr
 */
public class Splitter
{
    private static final Log LOG = LogFactory.getLog(Splitter.class);

    private PDDocument sourceDocument;
    private PDDocument currentDestinationDocument;

    private int splitLength = 1;
    private int startPage = Integer.MIN_VALUE;
    private int endPage = Integer.MAX_VALUE;
    private List<PDDocument> destinationDocuments;
    private Map<COSDictionary, COSDictionary> pageDictMap;
    private Map<COSDictionary, COSDictionary> structDictMap;
    private Map<COSDictionary, COSDictionary> annotDictMap;
    private Map<PDPageDestination,PDPage> destToFixMap;
    private Set<String> idSet;
    private Set<COSName> roleSet;

    private int currentPageNumber;

    private StreamCacheCreateFunction streamCacheCreateFunction = null;

    /**
     * @return the current function to be used to create an instance of stream cache.
     */
    public StreamCacheCreateFunction getStreamCacheCreateFunction()
    {
        return streamCacheCreateFunction;
    }

    /**
     * Set the current function to be used to create an instance of stream cache.
     * 
     * @param streamCacheCreateFunction the current function to be used to create an instance of stream cache.
     */
    public void setStreamCacheCreateFunction(StreamCacheCreateFunction streamCacheCreateFunction)
    {
        this.streamCacheCreateFunction = streamCacheCreateFunction;
    }

    /**
     * This will take a document and split into several other documents.
     *
     * @param document The document to split.
     *
     * @return A list of all the split documents. These should all be saved before closing any
     * documents, including the source document. Any further operations should be made after
     * reloading them, to avoid problems due to resource sharing. For the same reason, they should
     * not be saved with encryption.
     *
     * @throws IOException If there is an IOError
     */
    public List<PDDocument> split(PDDocument document) throws IOException
    {
        // reset the currentPageNumber for a case if the split method will be used several times
        currentPageNumber = 0;
        destinationDocuments = new ArrayList<>();
        sourceDocument = document;
        pageDictMap = new HashMap<>();
        destToFixMap = new HashMap<>();
        annotDictMap = new HashMap<>();
        idSet = new HashSet<>();
        roleSet = new HashSet<>();

        processPages();

        for (PDDocument destinationDocument : destinationDocuments)
        {
            cloneStructureTree(destinationDocument);
            fixDestinations(destinationDocument);
        }

        return destinationDocuments;
    }

    /**
     * Replace the page destinations, if the source and destination pages are in the target
     * document. This must be called after all pages (and its annotations) are processed.
     *
     * @param destinationDocument
     */
    private void fixDestinations(PDDocument destinationDocument)
    {
        PDPageTree pageTree = destinationDocument.getPages();
        for (Map.Entry<PDPageDestination,PDPage> entry : destToFixMap.entrySet())
        {
            PDPageDestination pageDestination = entry.getKey();
            // Find whether source page is inside or outside
            PDPage srcPage = entry.getValue();
            if (pageTree.indexOf(srcPage) < 0)
            {
                continue;
            }
            COSDictionary srcPageDict = pageDestination.getPage().getCOSObject();
            COSDictionary dstPageDict = pageDictMap.get(srcPageDict);
            PDPage dstPage = new PDPage(dstPageDict);
            // Find whether destination page is inside or outside
            if (pageTree.indexOf(dstPage) >= 0)
            {
                pageDestination.setPage(dstPage);
            }
            else
            {
                pageDestination.setPage(null);
            }
        }
    }

    /**
     * Clone the structure tree from the source to the current destination document.
     *
     * @param destinationDocument
     * @throws IOException 
     */
    private void cloneStructureTree(PDDocument destinationDocument) throws IOException
    {
        PDStructureTreeRoot srcStructureTreeRoot = sourceDocument.getDocumentCatalog().getStructureTreeRoot();
        if (srcStructureTreeRoot == null)
        {
            return;
        }
        structDictMap = new HashMap<>();
        PDStructureTreeRoot dstStructureTreeRoot = new PDStructureTreeRoot();
        PDPageTree dstPageTree = destinationDocument.getPages();

        // clone /K, also fills dictMap
        COSBase k1 = srcStructureTreeRoot.getK();
        COSBase k2 = new KCloner(dstPageTree).createClone(k1, dstStructureTreeRoot.getCOSObject(), null);
        dstStructureTreeRoot.setK(k2);

        // transfer ParentTree using the map because the dictionaries are all found in the /K structure.
        PDNumberTreeNode srcParentTree = srcStructureTreeRoot.getParentTree();
        Map<Integer, COSObjectable> srcNumberTreeAsMap = PDFMergerUtility.getNumberTreeAsMap(srcParentTree);
        Map<Integer, COSObjectable> dstNumberTreeAsMap = new LinkedHashMap<>();
        for (int p = 0; p < dstPageTree.getCount(); ++p)
        {
            PDPage page = dstPageTree.get(p);
            int sp1 = page.getStructParents();
            if (sp1 != -1)
            {
                cloneTreeElement(srcNumberTreeAsMap, dstNumberTreeAsMap, sp1);
            }
            for (PDAnnotation ann : page.getAnnotations())
            {
                int sp2 = ann.getStructParent();
                if (sp2 != -1)
                {
                    cloneTreeElement(srcNumberTreeAsMap, dstNumberTreeAsMap, sp2);
                }
                PDAppearanceStream normalAppearanceStream = ann.getNormalAppearanceStream();
                if (normalAppearanceStream != null)
                {
                    processResources(normalAppearanceStream.getResources(), srcNumberTreeAsMap, dstNumberTreeAsMap, new HashSet<>());
                }
            }
            processResources(page.getResources(), srcNumberTreeAsMap, dstNumberTreeAsMap, new HashSet<>());
        }
        PDNumberTreeNode dstNumberTreeNode = new PDNumberTreeNode(PDParentTreeValue.class);
        dstNumberTreeNode.setNumbers(dstNumberTreeAsMap);
        dstStructureTreeRoot.setParentTree(dstNumberTreeNode);

        dstStructureTreeRoot.setParentTreeNextKey(srcStructureTreeRoot.getParentTreeNextKey());
        dstStructureTreeRoot.setClassMap(srcStructureTreeRoot.getClassMap());
        cloneRoleMap(srcStructureTreeRoot, dstStructureTreeRoot);
        cloneIDTree(srcStructureTreeRoot, dstStructureTreeRoot);

        destinationDocument.getDocumentCatalog().setStructureTreeRoot(dstStructureTreeRoot);
    }

    private void cloneIDTree(PDStructureTreeRoot srcStructTree, PDStructureTreeRoot destStructTree)
            throws IOException
    {
        PDNameTreeNode<PDStructureElement> srcIDTree = srcStructTree.getIDTree();
        if (srcIDTree == null)
        {
            return;
        }
        Map<String, PDStructureElement> srcIDTreeAsMap = PDFMergerUtility.getIDTreeAsMap(srcIDTree);
        Map<String, PDStructureElement> destNames = new HashMap<>();
        srcIDTreeAsMap.forEach((key, val) ->
        {
            if (!idSet.contains(key))
            {
                return;
            }
            COSDictionary dstDict = structDictMap.get(val.getCOSObject());
            if (dstDict != null)
            {
                destNames.put(key, new PDStructureElement(dstDict));
            }
        });
        PDNameTreeNode<PDStructureElement> destIDTree = new PDStructureElementNameTreeNode();
        destIDTree.setNames(destNames);
        destStructTree.setIDTree(destIDTree);
        // See comment at the end of PDFMergerUtility.mergeIDTree()
    }

    // needed because getRoleMap() and setRoleMap() habe different map types?!
    private void cloneRoleMap(PDStructureTreeRoot srcStructTree, PDStructureTreeRoot destStructTree)
    {
        COSDictionary srcDict = srcStructTree.getCOSObject().getCOSDictionary(COSName.ROLE_MAP);
        if (srcDict == null)
        {
            return;
        }
        COSDictionary dstDict = new COSDictionary();
        for (Map.Entry<COSName, COSBase> entry : srcDict.entrySet())
        {
            if (roleSet.contains(entry.getKey()))
            {
                dstDict.setItem(entry.getKey(), entry.getValue());
            }
        }
        destStructTree.getCOSObject().setItem(COSName.ROLE_MAP, dstDict);
    }

    // clone tree element using the map so that structure elements are replaced
    private void cloneTreeElement(
            Map<Integer, COSObjectable> srcNumberTreeAsMap, 
            Map<Integer, COSObjectable> dstNumberTreeAsMap, 
            int sp)
    {
        COSObjectable srcObj = srcNumberTreeAsMap.get(sp); // this is a PDParentTreeValue class
        COSObjectable dstObj = null;
        if (srcObj != null)
        {
            COSBase actualSrcObj = srcObj.getCOSObject();
            // structure element or array
            if (actualSrcObj instanceof COSArray)
            {
                // create a clone of the array
                COSArray srcArray = (COSArray) actualSrcObj;
                COSArray dstArray = new COSArray();
                for (int i = 0; i < srcArray.size(); ++i)
                {
                    COSBase srcElement = srcArray.getObject(i);
                    dstArray.add(structDictMap.get(srcElement)); // may be null
                }
                dstObj = dstArray;
            }
            else if (actualSrcObj instanceof COSDictionary)
            {
                // get the clone from the map
                dstObj = structDictMap.get(actualSrcObj);
                if (dstObj == null)
                {
                    // 164421.pdf, structure tree is weird.
                    // also 250052.pdf, 250198.pdf, 257012.pdf, 271459.pdf (multiple), 
                    // 670045.pdf (multiple)
                    // In 71459.pdf annotations on page 1 have StructParent numbers
                    // that point to structure elements in the /ParentTree that point to
                    // a different page.
                    LOG.warn("ParentTree index " + sp + " dictionary not found in /K");
                }
            }
            else
            {
                LOG.warn("tree element neither dictionary nor array, but " + 
                        (actualSrcObj == null ? "(null)" : actualSrcObj.getClass().getSimpleName()));
            }
            if (dstObj != null)
            {
                dstNumberTreeAsMap.put(sp, dstObj);
            }
        }
    }

    /**
     * Class to help clone the /K tree. It clones structure elements and fills the structure
     * elements map. Pages are replaced with the help of the page map. Elements with pages that
     * don't belong to the destination are removed from the clone.
     */
    private class KCloner
    {
        PDPageTree dstPageTree;

        public KCloner(PDPageTree dstPageTree)
        {
            this.dstPageTree = dstPageTree;
        }

        /**
         * Creates a clone of the source.
         *
         * @param src source dictionary or array.
         * @param dstParent for the /P entry; parameter needed because arrays don't keep a parent.
         * @param currentPageDict used to remember whether we have a page parent somewhere or not.
         * Starts with null.
         * @return a clone, or null if source is null or if there is no clone because it belongs to a
         * different page or to no page.
         */
        COSBase createClone(COSBase src, COSBase dstParent, COSDictionary currentPageDict)
        {
            if (src instanceof COSArray)
            {
                return createArrayClone(src, dstParent, currentPageDict);
            }
            else if (src instanceof COSDictionary)
            {
                return createDictionaryClone(src, dstParent, currentPageDict);
            }
            else
            {
                return src;
            }
        }

        private COSBase createArrayClone(COSBase src, COSBase dstParent, COSDictionary currentPageDict)
        {
            COSArray dst = new COSArray();
            for (COSBase base2 : (COSArray) src)
            {
                COSBase rc;
                if (base2 instanceof COSObject)
                {
                    rc = createClone(((COSObject) base2).getObject(), dstParent, currentPageDict);
                }
                else
                {
                    rc = createClone(base2, dstParent, currentPageDict);
                }
                // if this is null then they don't belong to the destination document
                if (rc != null)
                {
                    dst.add(rc);
                }
            }
            return dst.size() > 0 ? dst : null;
        }

        private COSBase createDictionaryClone(COSBase src, COSBase dstParent, COSDictionary currentPageDict)
        {
            COSDictionary srcDict = (COSDictionary) src;
            COSDictionary dstDict = structDictMap.get(srcDict);
            if (dstDict != null)
            {
                return dstDict;
            }
            COSDictionary dstPageDict = null;
            if (srcDict.containsKey(COSName.PG))
            {
                COSDictionary srcPageDict = srcDict.getCOSDictionary(COSName.PG);
                if (srcPageDict == null)
                {
                    return null;
                }
                dstPageDict = pageDictMap.get(srcPageDict);
                if (dstPageDict == null)
                {
                    return null;
                }
                PDPage dstPage = new PDPage(dstPageDict);
                if (dstPageTree.indexOf(dstPage) == -1)
                {
                    return null;
                }
            }

            // Create and fill clone
            dstDict = new COSDictionary();
            structDictMap.put(srcDict, dstDict);
            for (Map.Entry<COSName,COSBase> entry : srcDict.entrySet())
            {
                COSName key = entry.getKey();
                if (!COSName.K.equals(key) &&
                    !COSName.PG.equals(key) &&
                    !COSName.P.equals(key))
                {
                    dstDict.setItem(key, entry.getValue());
                }
            }

            // special handling for OBJR items ("object reference dictionary")
            // see e.g. file 488300.pdf and Root/StructTreeRoot/K/K/[2]/K/[1]/K/[0]/Obj
            COSName type = srcDict.getCOSName(COSName.TYPE);
            if (COSName.OBJR.equals(type))
            {
                COSDictionary srcObj = srcDict.getCOSDictionary(COSName.OBJ);
                COSDictionary dstObj = annotDictMap.get(srcObj);
                if (dstObj != null)
                {
                    // replace annotation with clone
                    dstDict.setItem(COSName.OBJ, dstObj);
                }
            }
            else
            {
                // /P not needed for OBJR items
                dstDict.setItem(COSName.P, dstParent);
            }

            dstDict.setItem(COSName.PG, dstPageDict);
            COSBase kid = srcDict.getDictionaryObject(COSName.K);
            
            // stack overflow here with 207658.pdf, too complex
            COSBase cloneKid = createClone(kid, dstDict, dstPageDict != null ? dstPageDict : currentPageDict);
            if (cloneKid == null && kid != null)
            {
                return null; // kids array wasn't empty, but is empty now => ignore
            }
            
            // removes orphan nodes, example:
            // Root/StructTreeRoot/K/[7]/K/[3]/K/[5]/K/[2] in 271459.pdf
            // decide about keeping source dictionaries with no /K and no /PG
            if (dstPageDict == null && cloneKid == null && currentPageDict == null)
            {
                // if no parent page and no page here and no kids, assume this is an orphan
                return null;
            }
            dstDict.setItem(COSName.K, cloneKid);
            String id = dstDict.getString(COSName.ID);
            if (id != null)
            {
                idSet.add(id);
            }
            COSName s = dstDict.getCOSName(COSName.S);
            if (s != null)
            {
                roleSet.add(s);
            }
            return dstDict;
        }
    }

    // Look for /StructParent and /StructParents and add them to the destination tree
    private void processResources(PDResources res, 
            Map<Integer, COSObjectable> srcNumberTreeAsMap, 
            Map<Integer, COSObjectable> dstNumberTreeAsMap,
            Set<COSDictionary> visited) throws IOException
    {
        if (res == null)
        {
            return;
        }
        if (visited.contains(res.getCOSObject()))
        {
            // avoid endless recursion, e.g. with 002874.pdf
            return;
        }
        visited.add(res.getCOSObject());

        for (COSName name : res.getXObjectNames())
        {
            PDXObject xObject = res.getXObject(name);
            int sp2 = -1;
            if (xObject instanceof PDFormXObject)
            {
                sp2 = ((PDFormXObject) xObject).getStructParents();
                processResources(((PDFormXObject) xObject).getResources(), srcNumberTreeAsMap, dstNumberTreeAsMap, visited);
            }
            else if (xObject instanceof PDImageXObject)
            {
                sp2 = ((PDImageXObject) xObject).getStructParent();
            }
            if (sp2 != -1)
            {
                cloneTreeElement(srcNumberTreeAsMap, dstNumberTreeAsMap, sp2);
            }
        }
    }

    /**
     * This will tell the splitting algorithm where to split the pages.  The default
     * is 1, so every page will become a new document.  If it was two then each document would
     * contain 2 pages.  If the source document had 5 pages it would split into
     * 3 new documents, 2 documents containing 2 pages and 1 document containing one
     * page.
     *
     * @param split The number of pages each split document should contain.
     * @throws IllegalArgumentException if the page is smaller than one.
     */
    public void setSplitAtPage(int split)
    {
        if(split <= 0)
        {
            throw new IllegalArgumentException("Number of pages is smaller than one");
        }
        splitLength = split;
    }

    /**
     * This will set the start page.
     *
     * @param start the 1-based start page
     * @throws IllegalArgumentException if the start page is smaller than one.
     */
    public void setStartPage(int start)
    {
        if(start <= 0)
        {
            throw new IllegalArgumentException("Start page is smaller than one");
        }
        startPage = start;
    }

    /**
     * This will set the end page.
     *
     * @param end the 1-based end page
     * @throws IllegalArgumentException if the end page is smaller than one.
     */
    public void setEndPage(int end)
    {
        if(end <= 0)
        {
            throw new IllegalArgumentException("End page is smaller than one");
        }
        endPage = end;
    }

    /**
     * Interface method to handle the start of the page processing.
     *
     * @throws IOException If an IO error occurs.
     */
    private void processPages() throws IOException
    {
        for (PDPage page : sourceDocument.getPages())
        {
            if (currentPageNumber + 1 >= startPage && currentPageNumber + 1 <= endPage)
            {
                processPage(page);
                currentPageNumber++;
            }
            else
            {
                if (currentPageNumber > endPage)
                {
                    break;
                }
                else
                {
                    currentPageNumber++;
                }
            }
        }
    }

    /**
     * Helper method for creating new documents at the appropriate pages.
     *
     * @throws IOException If there is an error creating the new document.
     */
    private void createNewDocumentIfNecessary() throws IOException
    {
        if (splitAtPage(currentPageNumber) || currentDestinationDocument == null)
        {
            currentDestinationDocument = createNewDocument();
            destinationDocuments.add(currentDestinationDocument);
        }
    }

    /**
     * Check if it is necessary to create a new document.
     * By default a split occurs at every page.  If you wanted to split
     * based on some complex logic then you could override this method.  For example.
     * <code>
     * protected void splitAtPage()
     * {
     *     // will split at pages with prime numbers only
     *     return isPrime(pageNumber);
     * }
     * </code>
     * @param pageNumber the 0-based page number to be checked as splitting page
     * 
     * @return true If a new document should be created.
     */
    protected boolean splitAtPage(int pageNumber)
    {
        return (pageNumber + 1 - Math.max(1, startPage)) % splitLength == 0;
    }

    /**
     * Create a new document to write the split contents to.
     *
     * @return the newly created PDDocument. 
     * @throws IOException If there is an problem creating the new document.
     */
    protected PDDocument createNewDocument() throws IOException
    {
        PDDocument document = streamCacheCreateFunction != null ? new PDDocument(streamCacheCreateFunction) : new PDDocument();
        document.getDocument().setVersion(getSourceDocument().getVersion());
        PDDocumentInformation sourceDocumentInformation = getSourceDocument().getDocumentInformation();
        if (sourceDocumentInformation != null)
        {
            // PDFBOX-5317: Image Capture Plus files where /Root and /Info share the same dictionary
            // Only copy simple elements to avoid huge files
            COSDictionary sourceDocumentInformationDictionary = sourceDocumentInformation.getCOSObject();
            COSDictionary destDocumentInformationDictionary = new COSDictionary();
            for (COSName key : sourceDocumentInformationDictionary.keySet())
            {
                COSBase value = sourceDocumentInformationDictionary.getDictionaryObject(key);
                if (value instanceof COSDictionary)
                {
                    LOG.warn("Nested entry for key '" + key.getName()
                            + "' skipped in document information dictionary");
                    if (sourceDocument.getDocumentCatalog().getCOSObject() ==
                            sourceDocument.getDocumentInformation().getCOSObject())
                    {
                        LOG.warn("/Root and /Info share the same dictionary");
                    }
                    continue;
                }
                if (COSName.TYPE.equals(key))
                {
                    continue; // there is no /Type in the document information dictionary
                }
                destDocumentInformationDictionary.setItem(key, value);
            }
            document.setDocumentInformation(new PDDocumentInformation(destDocumentInformationDictionary));
        }
        document.getDocumentCatalog().setViewerPreferences(
                getSourceDocument().getDocumentCatalog().getViewerPreferences());
        document.getDocumentCatalog().setLanguage(
                getSourceDocument().getDocumentCatalog().getLanguage());
        document.getDocumentCatalog().setMarkInfo(
                getSourceDocument().getDocumentCatalog().getMarkInfo());
        document.getDocumentCatalog().setMetadata(
                getSourceDocument().getDocumentCatalog().getMetadata());
        return document;
    }

    /**
     * Interface to start processing a new page.
     *
     * @param page The page that is about to get processed.
     *
     * @throws IOException If there is an error creating the new document.
     */
    protected void processPage(PDPage page) throws IOException
    {
        createNewDocumentIfNecessary();
        
        PDPage imported = getDestinationDocument().importPage(page);
        if (page.getResources() != null && !page.getCOSObject().containsKey(COSName.RESOURCES))
        {
            imported.setResources(page.getResources());
            LOG.info("Resources imported in Splitter"); // follow-up to warning in importPage
        }
        // remove page links to avoid copying not needed resources 
        processAnnotations(imported);

        pageDictMap.put(page.getCOSObject(), imported.getCOSObject());
    }

    private void processAnnotations(PDPage imported) throws IOException
    {
        List<PDAnnotation> annotations = imported.getAnnotations();
        if (annotations.isEmpty())
        {
            return;
        }
        List<PDAnnotation> clonedAnnotations = new ArrayList<>(annotations.size());
        for (PDAnnotation annotation : annotations)
        {
            // create a shallow clone
            COSDictionary clonedDict = new COSDictionary(annotation.getCOSObject());
            PDAnnotation annotationClone = PDAnnotation.createAnnotation(clonedDict);
            annotDictMap.put(annotation.getCOSObject(), clonedDict);
            clonedAnnotations.add(annotationClone);
            if (annotationClone instanceof PDAnnotationLink)
            {
                PDAnnotationLink link = (PDAnnotationLink) annotationClone;   
                PDDestination srcDestination = link.getDestination();
                PDAction action = null;
                if (srcDestination == null)
                {
                    action = link.getAction();
                    if (action instanceof PDActionGoTo)
                    {
                        srcDestination = ((PDActionGoTo) action).getDestination();
                    }
                }
                if (srcDestination instanceof PDPageDestination)
                {
                    // preserve links to pages within the split result:
                    // not fully possible here because we don't have the full target document yet.
                    // However we're cloning as needed and remember what to do later.
                    PDPage destinationPage = ((PDPageDestination) srcDestination).getPage();
                    if (destinationPage != null)
                    {
                        // clone destination
                        COSArray clonedDestinationArray =
                                new COSArray(((PDPageDestination) srcDestination).getCOSObject().toList());
                        PDPageDestination dstDestination =
                                (PDPageDestination) PDDestination.create(clonedDestinationArray);

                        // remember the destination to adjust / remove page later
                        destToFixMap.put(dstDestination, imported);

                        if (action != null)
                        {
                            // if action is not null, then the destination came from an action,
                            // thus clone action as well, then assign destination clone, then action
                            COSDictionary clonedActionDict = new COSDictionary(action.getCOSObject());
                            PDActionGoTo dstAction =
                                    (PDActionGoTo) PDActionFactory.createAction(clonedActionDict);
                            dstAction.setDestination(dstDestination);
                            link.setAction(dstAction);
                        }
                        else
                        {
                            // just assign destination clone
                            link.setDestination(dstDestination);
                        }
                    }
                }
            }
            if (annotation.getPage() != null)
            {
                annotationClone.setPage(imported);
            }
        }
        imported.setAnnotations(clonedAnnotations);
    }

    /**
     * The source PDF document.
     * 
     * @return the pdf to be split
     */
    protected final PDDocument getSourceDocument()
    {
        return sourceDocument;
    }

    /**
     * The source PDF document.
     * 
     * @return current destination pdf
     */
    protected final PDDocument getDestinationDocument()
    {
        return currentDestinationDocument;
    }
}

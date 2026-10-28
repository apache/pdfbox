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
import java.util.Collections;
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
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.io.RandomAccessStreamCache.StreamCacheCreateFunction;
import org.apache.pdfbox.pdmodel.PDDestinationNameTreeNode;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDDocumentNameDestinationDictionary;
import org.apache.pdfbox.pdmodel.PDDocumentNameDictionary;
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
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationMarkup;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationPopup;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceStream;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDNamedDestination;
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
    private Map<COSDictionary, COSDictionary> pageDictMap; // map old page => new page for the current destination document
    private List<Map<COSDictionary, COSDictionary>> pageDictMaps; // list of these maps for all destination documents
    private Map<PDPage,Integer> pageToDocNumMap; // map old page => index of destination document
    private Map<COSDictionary, COSDictionary> structDictMap;
    private List<Map<COSDictionary, COSDictionary>> annotDictMaps; // list of these maps for all destination documents
    private Map<COSDictionary, COSDictionary> annotDictMap; // map old annotation => new annotation for the current destination document
    private Map<PDPageDestination,PDPage> destToFixMap;
    private Set<String> idSet;
    private Set<COSName> roleSet;

    private int currentPageNumber; // 0-based

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
     * @throws IllegalArgumentException if the end page is smaller than the start page.
     */
    public List<PDDocument> split(PDDocument document) throws IOException
    {
        if (endPage < startPage)
        {
            throw new IllegalArgumentException("End page is smaller than startPage");
        }
        // reset the currentPageNumber for a case if the split method will be used several times
        currentPageNumber = 0;
        currentDestinationDocument = null;
        destinationDocuments = new ArrayList<>();
        sourceDocument = document;
        pageDictMaps = new ArrayList<>();
        pageToDocNumMap = new HashMap<>();
        annotDictMaps = new ArrayList<>();
        destToFixMap = new HashMap<>();

        try
        {
            for (PDPage page : sourceDocument.getPages())
            {
                if (currentPageNumber + 1 >= startPage && currentPageNumber + 1 <= endPage)
                {
                    processPage(page);
                }
                else if (currentPageNumber >= endPage)
                {
                    break;
                }
                currentPageNumber++;
            }
            for (int i = 0; i < destinationDocuments.size(); ++i)
            {
                idSet = new HashSet<>();
                roleSet = new HashSet<>();
                PDDocument destinationDocument = destinationDocuments.get(i);
                pageDictMap = pageDictMaps.get(i);
                annotDictMap = annotDictMaps.get(i);
                cloneStructureTree(destinationDocument);
                fixDestinations(destinationDocument);
            }
            fixNameDestinations();
            fixNameDestinations2();
        }
        catch (IOException ex)
        {
            for (PDDocument doc : destinationDocuments)
            {
                IOUtils.closeQuietly(doc);
            }
            throw ex;
        }
        return destinationDocuments;
    }

    /**
     * PDFBOX-6283: Fix the destinations in /Root/Names/Dests by creating clones only for
     * the destinations that are in the destination documents.
     *
     * @throws IOException 
     */
    void fixNameDestinations() throws IOException
    {
        PDDocumentNameDictionary srcNames = sourceDocument.getDocumentCatalog().getNames();
        if (srcNames == null)
        {
            return;
        }
        PDDestinationNameTreeNode srcDests = srcNames.getDests();
        if (srcDests == null)
        {
            return;
        }

        List<Map<String, PDPageDestination>> dstTreeAsMapList =
                new ArrayList<>(destinationDocuments.size());
        for (int i = 0; i < destinationDocuments.size(); ++i)
        {
            dstTreeAsMapList.add(new HashMap<>());
        }

        // map all named destinations to the pages in the new documents
        Map<String, PDPageDestination> srcTreeAsMap = srcDests.getTreeAsMap();
        for (Map.Entry<String, PDPageDestination> entry : srcTreeAsMap.entrySet())
        {
            PDPageDestination value = entry.getValue();
            if (value == null)
            {
                // happens with file 000671.pdf, entry "Raff1"
                continue;
            }
            PDPage pageInTreeDest = value.getPage();
            // find out in which destination document this should be added
            Integer docIndex = pageToDocNumMap.get(pageInTreeDest);
            if (docIndex == null)
            {
                // wasn't part of the split
                continue;
            }
            pageDictMap = pageDictMaps.get(docIndex); // get appropriate mapping
            COSDictionary dstPageDict = pageDictMap.get(pageInTreeDest.getCOSObject());

            // clone destination
            COSArray clonedDestinationArray = new COSArray(value.getCOSObject().toList());
            PDPageDestination dstDestination = (PDPageDestination) PDDestination.create(clonedDestinationArray);
            dstDestination.setPage(new PDPage(dstPageDict));

            // add to the treeMap of the appropriate destination document
            Map<String, PDPageDestination> dstTreeAsMap = dstTreeAsMapList.get(docIndex);
            dstTreeAsMap.put(entry.getKey(), dstDestination);
        }

        // Assign all non empty treemaps to the new documents
        for (int i = 0; i < destinationDocuments.size(); ++i)
        {
            Map<String, PDPageDestination> dstTreeAsMap = dstTreeAsMapList.get(i);
            if (dstTreeAsMap.isEmpty())
            {
                continue;
            }
            PDDocument destinationDocument = destinationDocuments.get(i);
            PDDocumentCatalog dstDocumentCatalog = destinationDocument.getDocumentCatalog();
            PDDocumentNameDictionary dstDocumentNameDictionary = new PDDocumentNameDictionary(dstDocumentCatalog);
            PDDestinationNameTreeNode dstDests = new PDDestinationNameTreeNode();
            dstDocumentNameDictionary.setDests(dstDests);
            dstDocumentCatalog.setNames(dstDocumentNameDictionary);
            dstDests.setNames(dstTreeAsMap);
        }
    }

    /**
     * PDFBOX-6283: Fix the destinations in /Root/Dests by creating clones only for
     * the destinations that are in the destination documents. Can be tested on file 503088.pdf.
     *
     * @throws IOException 
     */
    void fixNameDestinations2() throws IOException
    {
        PDDocumentNameDestinationDictionary nameDestinationDictionary = sourceDocument.getDocumentCatalog().getDests();
        if (nameDestinationDictionary == null || nameDestinationDictionary.getCOSObject().size() == 0)
        {
            return;
        }

        List<COSDictionary> dstDests = new ArrayList<>(destinationDocuments.size());
        for (int i = 0; i < destinationDocuments.size(); ++i)
        {
            dstDests.add(new COSDictionary());
        }

        // map all named destinations to the pages in the new documents
        for (COSName key : nameDestinationDictionary.getCOSObject().keySet())
        {
            PDDestination dest = nameDestinationDictionary.getDestination(key.getName());
            if (dest instanceof PDPageDestination)
            {
                PDPageDestination pageDest = (PDPageDestination) dest;
                PDPage pageInTreeDest = pageDest.getPage();
                // find out in which destination document this should be added
                Integer docIndex = pageToDocNumMap.get(pageInTreeDest);
                if (docIndex == null)
                {
                    // wasn't part of the split
                    continue;
                }
                pageDictMap = pageDictMaps.get(docIndex); // get appropriate mapping
                COSDictionary dstPageDict = pageDictMap.get(pageInTreeDest.getCOSObject());

                // clone destination
                COSArray clonedDestinationArray = new COSArray(pageDest.getCOSObject().toList());
                PDPageDestination dstDestination = (PDPageDestination) PDDestination.create(clonedDestinationArray);
                dstDestination.setPage(new PDPage(dstPageDict));

                // add to the dictionary of the appropriate destination document
                COSDictionary dstDict = dstDests.get(docIndex);
                dstDict.setItem(key, dstDestination);
            }
            else if (dest != null)
            {
                // add it everywhere, it doesn't reference a specific page
                dstDests.forEach(dict -> dict.setItem(key, dest));
            }
        }

        // Assign all non empty dictionaries to the new documents
        for (int i = 0; i < destinationDocuments.size(); ++i)
        {
            COSDictionary dstDict = dstDests.get(i);
            if (dstDict.size() == 0)
            {
                continue;
            }
            PDDocument destinationDocument = destinationDocuments.get(i);
            PDDocumentCatalog dstDocumentCatalog = destinationDocument.getDocumentCatalog();
            // catalog.setDests() doesn't exist, maybe because it feels obsolete
            dstDocumentCatalog.getCOSObject().setItem(COSName.DESTS, new PDDocumentNameDestinationDictionary(dstDict));
        }
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
            // Find whether destination page is inside or outside
            if (dstPageDict != null)
            {
                pageDestination.setPage(new PDPage(dstPageDict));
            }
            else
            {
                pageDestination.setPage(null);
            }
        }
    }

    /**
     * Clone the structure tree from the source to the current destination document. This must be
     * called after all pages are processed.
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
        COSBase k2 = new KCloner().createClone(k1, dstStructureTreeRoot.getCOSObject(), null);
        dstStructureTreeRoot.setK(k2);

        // transfer ParentTree using the map because the dictionaries are all found in the /K structure.
        PDNumberTreeNode srcParentTree = srcStructureTreeRoot.getParentTree();
        Map<Integer, COSObjectable> srcNumberTreeAsMap = Collections.emptyMap();
        if (srcParentTree != null)
        {
            srcNumberTreeAsMap = srcParentTree.getTreeAsMap();
        }
        Map<Integer, COSObjectable> dstNumberTreeAsMap = new LinkedHashMap<>();
        int dstPageTreeCount = dstPageTree.getCount();
        for (int p = 0; p < dstPageTreeCount; ++p)
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
        Integer upperLimit = dstNumberTreeNode.getUpperLimit();
        if (upperLimit != null)
        {
            dstStructureTreeRoot.setParentTreeNextKey(upperLimit + 1);
        }
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
        Map<String, PDStructureElement> srcIDTreeAsMap = srcIDTree.getTreeAsMap();
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

    // needed because getRoleMap() and setRoleMap() have different map types?!
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
                    // In 271459.pdf annotations on page 1 have StructParent numbers
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
            COSDictionary srcPageDict = srcDict.getCOSDictionary(COSName.PG);
            COSDictionary dstPageDict = null;
            COSBase kid = srcDict.getDictionaryObject(COSName.K);
            COSName type = srcDict.getCOSName(COSName.TYPE);
            if (srcPageDict != null)
            {
                dstPageDict = pageDictMap.get(srcPageDict);
                if (dstPageDict == null)
                {
                    // PDFBOX-6009: "wrong" /Pg entry
                    // quit if MCIDs because these need a /Pg entry
                    // or if MCR/OBJR dicts
                    if (COSName.MCR.equals(type) || COSName.OBJR.equals(type) || hasMCIDs(kid))
                    {
                        return null;
                    }
                    // else keep this as an intermediate element for now
                }
            }

            // special handling for MCR items ("marked-content reference dictionary")
            if (COSName.MCR.equals(type) && dstPageDict == null && 
                dstParent instanceof COSDictionary && ((COSDictionary) dstParent).getCOSDictionary(COSName.PG) == null)
            {
                // PAC: Pg entry of marked-content reference and of parent structure is null
                return null;
            }

            // Create and fill clone
            dstDict = new COSDictionary();
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
            if (COSName.OBJR.equals(type))
            {
                COSDictionary srcObj = srcDict.getCOSDictionary(COSName.OBJ);
                COSDictionary dstObj = annotDictMap.get(srcObj);
                if (dstObj != null)
                {
                    // replace annotation with clone
                    dstDict.setItem(COSName.OBJ, dstObj);
                }
                else if (srcObj != null) // 079177.pdf
                {
                    removePossibleOrphanAnnotation(srcObj, srcDict, currentPageDict, dstDict);
                }
                if (dstDict.size() == 1)
                {
                    return null;
                }
                if (dstPageDict == null &&
                    dstParent instanceof COSDictionary && ((COSDictionary) dstParent).getCOSDictionary(COSName.PG) == null)
                {
                    // Pg entry of object reference dictionary and of parent structure is null
                    return null;
                }
            }

            if (!COSName.OBJR.equals(type) && !COSName.MCR.equals(type))
            {
                // /P not needed for OBJR or MCR items
                dstDict.setItem(COSName.P, dstParent);
            }

            dstDict.setItem(COSName.PG, dstPageDict);

            structDictMap.put(srcDict, dstDict);
            
            // stack overflow here with 207658.pdf, too complex
            COSBase cloneKid = createClone(kid, dstDict, dstPageDict != null ? dstPageDict : currentPageDict);
            if (cloneKid == null && kid != null)
            {
                structDictMap.remove(srcDict);
                return null; // kids array wasn't empty, but is empty now => ignore
            }
            
            // removes orphan nodes, example:
            // Root/StructTreeRoot/K/[7]/K/[3]/K/[5]/K/[2] in 271459.pdf
            // decide about keeping source dictionaries with no /K and no /PG
            if (dstPageDict == null && cloneKid == null && currentPageDict == null)
            {
                // if no parent page and no page here and no kids, assume this is an orphan
                structDictMap.remove(srcDict);
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

        private boolean hasMCIDs(COSBase kid)
        {
            if (kid instanceof COSInteger)
            {
                return true;
            }
            if (kid instanceof COSArray)
            {
                COSArray ar = (COSArray) kid;
                for (int i = 0; i < ar.size(); ++i)
                {
                    if (ar.getObject(i) instanceof COSInteger)
                    {
                        return true;
                    }
                }
            }
            return false;
        }

        private void removePossibleOrphanAnnotation(COSDictionary srcObj, COSDictionary srcDict,
                COSDictionary currentPageDict, COSDictionary dstDict)
        {
            // PDFBOX-5929: Check whether this is an "orphan" annotation that isn't in the page
            COSBase objType = srcObj.getDictionaryObject(COSName.TYPE);
            COSBase objSubtype = srcObj.getDictionaryObject(COSName.SUBTYPE);
            if (COSName.ANNOT.equals(objType) || COSName.LINK.equals(objSubtype))
            {
                COSDictionary srcPageDict = srcDict.getCOSDictionary(COSName.PG);
                if (srcPageDict == null)
                {
                    // /Pg entry is not always on this level
                    srcPageDict = currentPageDict;
                }
                if (srcPageDict != null)
                {
                    COSArray annotationArray = srcPageDict.getCOSArray(COSName.ANNOTS);
                    if (annotationArray == null || annotationArray.indexOfObject(srcObj) == -1)
                    {
                        // Ideally the entire OBJR entry should be removed.
                        // Removing the OBJ entry is done to avoid potential page orphans
                        // from the annotation destination.
                        LOG.warn("An annotation OBJ that isn't in the page has been removed from the structure tree");
                        dstDict.removeItem(COSName.OBJ);
                    }
                }
            }
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
     * Sets the number of pages each split document should contain. The default is 1, so every page
     * becomes a new document. With 2, each document contains 2 pages; a 5-page source is split into
     * 3 documents, two with 2 pages and one with 1 page. If a start page is set, counting begins
     * there.
     *
     * @param split The number of pages each split document should contain.
     * @throws IllegalArgumentException if the number of pages is smaller than one.
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
            pageDictMap = new HashMap<>();
            pageDictMaps.add(pageDictMap);
            annotDictMap = new HashMap<>();
            annotDictMaps.add(annotDictMap);
        }
    }

    /**
     * Check if it is necessary to create a new document. By default a split occurs at every page.
     * If you wanted to split based on some complex logic then you could override this method like
     * this:
     * <pre>
     * &#64;Override
     * protected boolean splitAtPage(int pageNumber)
     * {
     *     // will split at pages with prime numbers only
     *     return isPrime(pageNumber);
     * }
     * </pre>
     * Note that this method is called only for pages within the start..end range. An
     * override replaces the built-in logic, so {@link #setSplitAtPage(int)} and
     * {@link #setStartPage(int)} have no effect on it. The first processed page always gets a new
     * document, even if the override returns false for it.
     *
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
     * @throws IOException If there is a problem creating the new document.
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
        PDDocumentCatalog destCatalog = document.getDocumentCatalog();
        PDDocumentCatalog sourceCatalog = getSourceDocument().getDocumentCatalog();
        destCatalog.setViewerPreferences(sourceCatalog.getViewerPreferences());
        destCatalog.setLanguage(sourceCatalog.getLanguage());
        destCatalog.setMarkInfo(sourceCatalog.getMarkInfo());
        destCatalog.setMetadata(sourceCatalog.getMetadata());
        // reset reused object keys to avoid gaps in the xref table
        destCatalog.getCOSObject().resetImportedObjectKeys();
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
        if (imported.getCOSObject().containsKey(COSName.B))
        {
            imported.getCOSObject().removeItem(COSName.B);
            LOG.warn("/B entry (beads) removed by splitter");
        }
        // remove page links to avoid copying not needed resources 
        processAnnotations(imported);

        pageDictMap.put(page.getCOSObject(), imported.getCOSObject());
        pageToDocNumMap.put(page, destinationDocuments.size() - 1);
    }

    /**
     * Clone all annotations because of changes possibly made, and because the structure tree is
     * cloned.
     *
     * @param imported
     * @throws IOException 
     */
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
                PDDestination srcDestination = null;
                try
                {
                    srcDestination = link.getDestination();
                }
                catch (IOException ex)
                {
                    LOG.warn("Incorrect destination in link annotation on page " +
                            (currentPageNumber + 1) + " is removed", ex);
                    link.setDestination(null);
                }
                PDAction action = null;
                if (srcDestination == null)
                {
                    action = link.getAction();
                    if (action instanceof PDActionGoTo)
                    {
                        PDActionGoTo goToAction = (PDActionGoTo) action;
                        try
                        {
                            srcDestination = goToAction.getDestination();
                        }
                        catch (IOException ex)
                        {
                            LOG.warn("GoToAction with incorrect destination in link annotation on page " +
                                    (currentPageNumber + 1) + " is removed", ex);
                            link.setAction(null);
                        }
                    }
                }
                if (srcDestination instanceof PDNamedDestination)
                {
                    // PDFBOX-5840 / PDFBOX-6283: In a few cases, e.g. in PDFBOX-5840-410609.pdf
                    // 0xAD becomes 0 so the name would no longer match the entry in the new
                    // name tree, so we replace the name with the actual destination.
                    String namedDestinationString = ((PDNamedDestination) srcDestination).getNamedDestination();
                    if (namedDestinationString.contains("\000"))
                    {
                        srcDestination = sourceDocument.getDocumentCatalog().
                                findNamedDestinationPage((PDNamedDestination) srcDestination);
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
            if (annotationClone instanceof PDAnnotationWidget &&
                annotationClone.getCOSObject().containsKey(COSName.PARENT))
            {
                // remove non-terminal field /Parent reference, because this may lead to orphan pages
                annotationClone.getCOSObject().removeItem(COSName.PARENT);
            }
            if (annotation.getPage() != null)
            {
                annotationClone.setPage(imported);
            }
        }
        // Second loop for markup and popup annotations, which reference annotations themselves
        for (PDAnnotation annotation : clonedAnnotations)
        {
            if (annotation instanceof PDAnnotationMarkup)
            {
                PDAnnotationPopup annotationPopup = ((PDAnnotationMarkup) annotation).getPopup();
                if (annotationPopup == null)
                {
                    continue;
                }
                COSDictionary clonedPopupDict = annotDictMap.get(annotationPopup.getCOSObject());
                if (clonedPopupDict != null)
                {
                    annotation.getCOSObject().setItem(COSName.POPUP, clonedPopupDict);
                }
                else
                {
                    // orphan popup (not in annotation list); clone it and fix references 
                    clonedPopupDict = new COSDictionary(annotationPopup.getCOSObject());
                    annotDictMap.put(annotationPopup.getCOSObject(), clonedPopupDict);
                    PDAnnotationPopup annotationPopupClone =
                            (PDAnnotationPopup) PDAnnotation.createAnnotation(clonedPopupDict);
                    annotationPopupClone.setParent((PDAnnotationMarkup) annotation);
                    ((PDAnnotationMarkup) annotation).setPopup(annotationPopupClone);
                    if (annotationPopupClone.getPage() != null)
                    {
                        annotationPopupClone.setPage(imported);
                    }
                }
            }
            if (annotation instanceof PDAnnotationPopup)
            {
                PDAnnotationMarkup annotationMarkup = ((PDAnnotationPopup) annotation).getParent();
                if (annotationMarkup == null)
                {
                    continue;
                }
                COSDictionary clonedMarkupDict = annotDictMap.get(annotationMarkup.getCOSObject());
                // clonedMarkupDict will be null if markup annotation is an orphan (not in annotation list)
                annotation.getCOSObject().setItem(COSName.PARENT, clonedMarkupDict);
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
     * The current destination PDF document.
     * 
     * @return current destination pdf
     */
    protected final PDDocument getDestinationDocument()
    {
        return currentDestinationDocument;
    }
}

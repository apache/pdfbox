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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.pdfwriter.compress.CompressParameters;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDDocumentNameDestinationDictionary;
import org.apache.pdfbox.pdmodel.PDDocumentNameDictionary;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.PDStructureElementNameTreeNode;
import org.apache.pdfbox.pdmodel.PageMode;
import org.apache.pdfbox.pdmodel.common.COSObjectable;
import org.apache.pdfbox.pdmodel.common.PDDestinationOrAction;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.pdfbox.pdmodel.common.PDNameTreeNode;
import org.apache.pdfbox.pdmodel.common.PDNumberTreeNode;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDMarkInfo;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDParentTreeValue;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDStructureElement;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDStructureTreeRoot;
import org.apache.pdfbox.pdmodel.graphics.color.PDOutputIntent;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionGoTo;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.viewerpreferences.PDViewerPreferences;

/**
 * This class will take a list of pdf documents and merge them, saving the
 * result in a new document.
 *
 * @author Ben Litchfield
 */
public class PDFMergerUtility
{
    /**
     * Log instance.
     */
    private static final Log LOG = LogFactory.getLog(PDFMergerUtility.class);

    private final List<Object> sources;
    private String destinationFileName;
    private OutputStream destinationStream;
    private boolean ignoreAcroFormErrors = false;
    private PDDocumentInformation destinationDocumentInformation = null;
    private PDMetadata destinationMetadata = null;

    private DocumentMergeMode documentMergeMode = DocumentMergeMode.PDFBOX_LEGACY_MODE;
    private AcroFormMergeMode acroFormMergeMode = AcroFormMergeMode.PDFBOX_LEGACY_MODE;
    
    /**
     * The mode to use when merging documents:
     * 
     * <ul>
     * <li>{@link DocumentMergeMode#OPTIMIZE_RESOURCES_MODE} Optimizes resource handling such as
     *      closing documents early. <strong>Not all document elements are merged</strong> compared to
     *      the PDFBOX_LEGACY_MODE. Currently supported are:
     *      <ul>
     *          <li>Page content and resources
     *      </ul>  
     * <li>{@link DocumentMergeMode#PDFBOX_LEGACY_MODE} Keeps all files open until the
     *      merge has been completed. This is  currently necessary to merge documents
     *      containing a Structure Tree.
     * </ul>
     */
    public enum DocumentMergeMode
    {
        OPTIMIZE_RESOURCES_MODE,
        PDFBOX_LEGACY_MODE
    }
    
    /**
     * The mode to use when merging AcroForm between documents:
     * 
     * <ul>
     * <li>{@link AcroFormMergeMode#JOIN_FORM_FIELDS_MODE} fields with the same fully qualified name
     *      will be merged into one with the widget annotations of the merged fields 
     *      becoming part of the same field.
     * <li>{@link AcroFormMergeMode#PDFBOX_LEGACY_MODE} fields with the same fully qualified name
     *      will be renamed and treated as independent. This mode was used in versions
     *      of PDFBox up to 2.x.
     * </ul>
     */
    public enum AcroFormMergeMode
    {
        JOIN_FORM_FIELDS_MODE,
        PDFBOX_LEGACY_MODE
    }

    /**
     * Instantiate a new PDFMergerUtility.
     */
    public PDFMergerUtility()
    {
        sources = new ArrayList<>();
    }

    /**
     * Get the merge mode to be used for merging AcroForms between documents
     * 
     * {@link AcroFormMergeMode}
     */
    public AcroFormMergeMode getAcroFormMergeMode()
    {
        return acroFormMergeMode;
    }

    /**
     * Set the merge mode to be used for merging AcroForms between documents
     * 
     * {@link AcroFormMergeMode}
     */
    public void setAcroFormMergeMode(final AcroFormMergeMode theAcroFormMergeMode)
    {
        this.acroFormMergeMode = theAcroFormMergeMode;
    }
    
    /**
     * Get the merge mode to be used for merging documents
     * 
     * {@link DocumentMergeMode}
     */
    public DocumentMergeMode getDocumentMergeMode()
    {
        return documentMergeMode;
    }

    /**
     * Set the merge mode to be used for merging documents
     * 
     * {@link DocumentMergeMode}
     */
    public void setDocumentMergeMode(final DocumentMergeMode theDocumentMergeMode)
    {
        this.documentMergeMode = theDocumentMergeMode;
    }

    
    /**
     * Set the mode to be used for merging the documents
     * 
     * {@link DocumentMergeMode}
     */
    public void setAcroFormMergeMode(final DocumentMergeMode theDocumentMergeMode)
    {
        this.documentMergeMode = theDocumentMergeMode;
    }

    /**
     * Get the name of the destination file.
     *
     * @return Returns the destination.
     */
    public String getDestinationFileName()
    {
        return destinationFileName;
    }

    /**
     * Set the name of the destination file.
     *
     * @param destination The destination to set.
     */
    public void setDestinationFileName(final String destination)
    {
        destinationFileName = destination;
    }

    /**
     * Get the destination OutputStream.
     *
     * @return Returns the destination OutputStream.
     */
    public OutputStream getDestinationStream()
    {
        return destinationStream;
    }

    /**
     * Set the destination OutputStream.
     *
     * @param destStream The destination to set.
     */
    public void setDestinationStream(final OutputStream destStream)
    {
        destinationStream = destStream;
    }

    /**
     * Get the destination document information that is to be set in {@link #mergeDocuments(org.apache.pdfbox.io.MemoryUsageSetting)
     * }. The default is null, which means that it is ignored.
     *
     * @return The destination document information.
     */
    public PDDocumentInformation getDestinationDocumentInformation()
    {
        return destinationDocumentInformation;
    }

    /**
     * Set the destination document information that is to be set in {@link #mergeDocuments(org.apache.pdfbox.io.MemoryUsageSetting)
     * }. The default is null, which means that it is ignored.
     *
     * @param info The destination document information.
     */
    public void setDestinationDocumentInformation(final PDDocumentInformation info)
    {
        destinationDocumentInformation = info;
    }

    /**
     * Set the destination metadata that is to be set in {@link #mergeDocuments(org.apache.pdfbox.io.MemoryUsageSetting)
     * }. The default is null, which means that it is ignored.
     *
     * @return The destination metadata.
     */
    public PDMetadata getDestinationMetadata()
    {
        return destinationMetadata;
    }

    /**
     * Set the destination metadata that is to be set in {@link #mergeDocuments(org.apache.pdfbox.io.MemoryUsageSetting)
     * }. The default is null, which means that it is ignored.
     *
     * @param meta The destination metadata.
     */
    public void setDestinationMetadata(final PDMetadata meta)
    {
        destinationMetadata = meta;
    }

    /**
     * Add a source file to the list of files to merge.
     *
     * @param source Full path and file name of source document.
     * 
     * @throws FileNotFoundException If the file doesn't exist
     */
    public void addSource(final String source) throws FileNotFoundException
    {
        addSource(new File(source));
    }

    /**
     * Add a source file to the list of files to merge.
     *
     * @param source File representing source document
     * 
     * @throws FileNotFoundException If the file doesn't exist
     */
    public void addSource(final File source) throws FileNotFoundException
    {
        sources.add(source);
    }

    /**
     * Add a source to the list of documents to merge.
     *
     * @param source InputStream representing source document
     */
    public void addSource(final InputStream source)
    {
        sources.add(source);
    }

    /**
     * Add a list of sources to the list of documents to merge.
     *
     * @param sourcesList List of InputStream objects representing source
     * documents
     */
    public void addSources(final List<InputStream> sourcesList)
    {
        sources.addAll(sourcesList);
    }

    /**
     * Merge the list of source documents, saving the result in the destination
     * file.
     *
     * @param memUsageSetting defines how memory is used for buffering PDF streams;
     *                        in case of <code>null</code> unrestricted main memory is used 
     * 
     * @throws IOException If there is an error saving the document.
     */
    public void mergeDocuments(final MemoryUsageSetting memUsageSetting) throws IOException
    {
        mergeDocuments(memUsageSetting, CompressParameters.DEFAULT_COMPRESSION);
    }

    /**
     * Merge the list of source documents, saving the result in the destination file.
     *
     * @param memUsageSetting defines how memory is used for buffering PDF streams; in case of <code>null</code>
     * unrestricted main memory is used
     * @param compressParameters defines if compressed object streams are enabled
     * 
     * @throws IOException If there is an error saving the document.
     */
    public void mergeDocuments(final MemoryUsageSetting memUsageSetting, final CompressParameters compressParameters) throws IOException
    {
        if (documentMergeMode == DocumentMergeMode.PDFBOX_LEGACY_MODE)
        {
            legacyMergeDocuments(memUsageSetting, compressParameters);
        }
        else if (documentMergeMode == DocumentMergeMode.OPTIMIZE_RESOURCES_MODE)
        {
            optimizedMergeDocuments(memUsageSetting, compressParameters);
        }
    }
    
    private void optimizedMergeDocuments(final MemoryUsageSetting memUsageSetting,
                                         final CompressParameters compressParameters) throws IOException
    {
        try (PDDocument destination = new PDDocument(memUsageSetting))
        {
            final PDFCloneUtility cloner = new PDFCloneUtility(destination);
            for (final Object sourceObject : sources)
            {
                PDDocument sourceDoc = null;
                try
                {
                    if (sourceObject instanceof File)
                    {
                        sourceDoc = Loader.loadPDF((File) sourceObject, memUsageSetting);
                    }
                    else
                    {
                        sourceDoc = Loader.loadPDF((InputStream) sourceObject, memUsageSetting);
                    }
                    for (final PDPage page : sourceDoc.getPages())
                    {
                        final PDPage newPage = new PDPage(
                                (COSDictionary) cloner.cloneForNewDocument(page.getCOSObject()));
                        newPage.setCropBox(page.getCropBox());
                        newPage.setMediaBox(page.getMediaBox());
                        newPage.setRotation(page.getRotation());
                        final PDResources resources = page.getResources();
                        if (resources != null)
                        {
                            // this is smart enough to just create references for resources that are used on multiple
                            // pages
                            newPage.setResources(new PDResources(
                                    (COSDictionary) cloner.cloneForNewDocument(resources)));
                        }
                        else
                        {
                            newPage.setResources(new PDResources());
                        }
                        destination.addPage(newPage);
                    }
                }
                finally
                {
                    IOUtils.closeQuietly(sourceDoc);
                }
            }
            
            if (destinationStream == null)
            {
                destination.save(destinationFileName, compressParameters);
            }
            else
            {
                destination.save(destinationStream, compressParameters);
            }
        }
    }
    
    
    /**
     * Merge the list of source documents, saving the result in the destination
     * file.
     *
     * @param memUsageSetting defines how memory is used for buffering PDF streams;
     *                        in case of <code>null</code> unrestricted main memory is used 
     * 
     * @throws IOException If there is an error saving the document.
     */
    private void legacyMergeDocuments(final MemoryUsageSetting memUsageSetting,
                                      final CompressParameters compressParameters) throws IOException
    {
        if (sources != null && !sources.isEmpty())
        {
            // Make sure that:
            // - first Exception is kept
            // - all PDDocuments are closed
            // - all FileInputStreams are closed
            // - there's a way to see which errors occurred

            final List<PDDocument> tobeclosed = new ArrayList<>();
            final MemoryUsageSetting partitionedMemSetting = memUsageSetting != null ?
                    memUsageSetting.getPartitionedCopy(sources.size()+1) :
                    MemoryUsageSetting.setupMainMemoryOnly();
            try (PDDocument destination = new PDDocument(partitionedMemSetting))
            {
                for (final Object sourceObject : sources)
                {
                    PDDocument sourceDoc = null;
                    if (sourceObject instanceof File)
                    {
                        sourceDoc = Loader.loadPDF((File) sourceObject, partitionedMemSetting);
                    }
                    else
                    {
                        sourceDoc = Loader.loadPDF((InputStream) sourceObject,
                                partitionedMemSetting);
                    }
                    tobeclosed.add(sourceDoc);
                    appendDocument(destination, sourceDoc);
                }
                
                // optionally set meta data
                if (destinationDocumentInformation != null)
                {
                    destination.setDocumentInformation(destinationDocumentInformation);
                }
                if (destinationMetadata != null)
                {
                    destination.getDocumentCatalog().setMetadata(destinationMetadata);
                }
                
                if (destinationStream == null)
                {
                    destination.save(destinationFileName, compressParameters);
                }
                else
                {
                    destination.save(destinationStream, compressParameters);
                }
            }
            finally
            {
                for (final PDDocument doc : tobeclosed)
                {
                    IOUtils.closeAndLogException(doc, LOG, "PDDocument", null);
                }
            }
        }
    }

    /**
     * append all pages from source to destination.
     *
     * @param destination the document to receive the pages
     * @param source the document originating the new pages
     *
     * @throws IOException If there is an error accessing data from either
     * document.
     */
    public void appendDocument(final PDDocument destination, final PDDocument source) throws IOException
    {
        if (source.getDocument().isClosed())
        {
            throw new IOException("Error: source PDF is closed.");
        }
        if (destination.getDocument().isClosed())
        {
            throw new IOException("Error: destination PDF is closed.");
        }

        final PDDocumentCatalog destCatalog = destination.getDocumentCatalog();
        final PDDocumentCatalog srcCatalog = source.getDocumentCatalog();
        
        if (isDynamicXfa(srcCatalog.getAcroForm()))
        {
            throw new IOException("Error: can't merge source document containing dynamic XFA form content.");
        }   
        
        final PDDocumentInformation destInfo = destination.getDocumentInformation();
        final PDDocumentInformation srcInfo = source.getDocumentInformation();
        mergeInto(srcInfo.getCOSObject(), destInfo.getCOSObject(), Collections.<COSName>emptySet());

        // use the highest version number for the resulting pdf
        final float destVersion = destination.getVersion();
        final float srcVersion = source.getVersion();

        if (destVersion < srcVersion)
        {
            destination.setVersion(srcVersion);
        }

        int pageIndexOpenActionDest = -1;
        if (destCatalog.getOpenAction() == null)
        {
            // PDFBOX-3972: get local dest page index, it must be reassigned after the page cloning
            PDDestinationOrAction openAction = null;
            try
            {
                openAction = srcCatalog.getOpenAction();
            }
            catch (IOException ex)
            {
                // PDFBOX-4223
                LOG.error("Invalid OpenAction ignored", ex);
            }
            PDDestination openActionDestination = null;
            if (openAction instanceof PDActionGoTo)
            {
                openActionDestination = ((PDActionGoTo) openAction).getDestination();
            }
            else if (openAction instanceof PDDestination)
            {
                openActionDestination = (PDDestination) openAction;
            }
            // note that it can also be something else, e.g. PDActionJavaScript, then do nothing

            if (openActionDestination instanceof PDPageDestination)
            {
                final PDPage page = ((PDPageDestination) openActionDestination).getPage();
                if (page != null)
                {
                    pageIndexOpenActionDest = srcCatalog.getPages().indexOf(page);
                }
            }

            destCatalog.setOpenAction(openAction);
        }

        final PDFCloneUtility cloner = new PDFCloneUtility(destination);

        mergeAcroForm(cloner, destCatalog, srcCatalog);

        final COSArray destThreads = (COSArray) destCatalog.getCOSObject().getDictionaryObject(COSName.THREADS);
        final COSArray srcThreads = (COSArray) cloner.cloneForNewDocument(destCatalog.getCOSObject().getDictionaryObject(
                COSName.THREADS));
        if (destThreads == null)
        {
            destCatalog.getCOSObject().setItem(COSName.THREADS, srcThreads);
        }
        else
        {
            destThreads.addAll(srcThreads);
        }

        final PDDocumentNameDictionary destNames = destCatalog.getNames();
        final PDDocumentNameDictionary srcNames = srcCatalog.getNames();
        if (srcNames != null)
        {
            if (destNames == null)
            {
                destCatalog.getCOSObject().setItem(COSName.NAMES, cloner.cloneForNewDocument(srcNames));
            }
            else
            {
                cloner.cloneMerge(srcNames, destNames);
            }
        }
        
        if (destNames != null)
        {
            // found in 054080.pdf from PDFBOX-4417 and doesn't belong there
            destNames.getCOSObject().removeItem(COSName.ID_TREE);
            LOG.warn("Removed /IDTree from /Names dictionary, doesn't belong there");
        }

        final PDDocumentNameDestinationDictionary destDests = destCatalog.getDests();
        final PDDocumentNameDestinationDictionary srcDests = srcCatalog.getDests();
        if (srcDests != null)
        {
            if (destDests == null)
            {
                destCatalog.getCOSObject().setItem(COSName.DESTS, cloner.cloneForNewDocument(srcDests));
            }
            else
            {
                cloner.cloneMerge(srcDests, destDests);
            }
        }

        final PDDocumentOutline destOutline = destCatalog.getDocumentOutline();
        final PDDocumentOutline srcOutline = srcCatalog.getDocumentOutline();
        if (srcOutline != null)
        {
            if (destOutline == null || destOutline.getFirstChild() == null)
            {
                final PDDocumentOutline cloned = new PDDocumentOutline((COSDictionary) cloner.cloneForNewDocument(srcOutline));
                destCatalog.setDocumentOutline(cloned);
            }
            else
            {
                // search last sibling for dest, because /Last entry is sometimes wrong
                PDOutlineItem destLastOutlineItem = destOutline.getFirstChild();
                while (destLastOutlineItem.getNextSibling() != null)
                {
                    destLastOutlineItem = destLastOutlineItem.getNextSibling();
                }
                for (final PDOutlineItem item : srcOutline.children())
                {
                    // get each child, clone its dictionary, remove siblings info,
                    // append outline item created from there
                    final COSDictionary clonedDict = (COSDictionary) cloner.cloneForNewDocument(item);
                    clonedDict.removeItem(COSName.PREV);
                    clonedDict.removeItem(COSName.NEXT);
                    final PDOutlineItem clonedItem = new PDOutlineItem(clonedDict);
                    destLastOutlineItem.insertSiblingAfter(clonedItem);
                    destLastOutlineItem = destLastOutlineItem.getNextSibling();
                }
            }
        }

        final PageMode destPageMode = destCatalog.getPageMode();
        final PageMode srcPageMode = srcCatalog.getPageMode();
        if (destPageMode == null)
        {
            destCatalog.setPageMode(srcPageMode);
        }

        COSDictionary destLabels = destCatalog.getCOSObject().getCOSDictionary(COSName.PAGE_LABELS);
        final COSDictionary srcLabels = srcCatalog.getCOSObject().getCOSDictionary(COSName.PAGE_LABELS);
        if (srcLabels != null)
        {
            final int destPageCount = destination.getNumberOfPages();
            final COSArray destNums;
            if (destLabels == null)
            {
                destLabels = new COSDictionary();
                destNums = new COSArray();
                destLabels.setItem(COSName.NUMS, destNums);
                destCatalog.getCOSObject().setItem(COSName.PAGE_LABELS, destLabels);
            }
            else
            {
                destNums = (COSArray) destLabels.getDictionaryObject(COSName.NUMS);
            }
            final COSArray srcNums = (COSArray) srcLabels.getDictionaryObject(COSName.NUMS);
            if (srcNums != null)
            {
                final int startSize = destNums.size();
                for (int i = 0; i < srcNums.size(); i += 2)
                {
                    final COSBase base = srcNums.getObject(i);
                    if (!(base instanceof COSNumber))
                    {
                        LOG.error("page labels ignored, index " + i + " should be a number, but is " + base);
                        // remove what we added
                        while (destNums.size() > startSize)
                        {
                            destNums.remove(startSize);
                        }
                        break;
                    }
                    final COSNumber labelIndex = (COSNumber) base;
                    final long labelIndexValue = labelIndex.intValue();
                    destNums.add(COSInteger.get(labelIndexValue + destPageCount));
                    destNums.add(cloner.cloneForNewDocument(srcNums.getObject(i + 1)));
                }
            }
        }

        final COSStream destMetadata = destCatalog.getCOSObject().getCOSStream(COSName.METADATA);
        final COSStream srcMetadata = srcCatalog.getCOSObject().getCOSStream(COSName.METADATA);
        if (destMetadata == null && srcMetadata != null)
        {
            try
            {
                final PDStream newStream = new PDStream(destination, srcMetadata.createInputStream(), (COSName) null);
                mergeInto(srcMetadata, newStream.getCOSObject(),
                        new HashSet<>(Arrays.asList(COSName.FILTER, COSName.LENGTH)));                
                destCatalog.getCOSObject().setItem(COSName.METADATA, newStream);
            }
            catch (IOException ex)
            {
                // PDFBOX-4227 cleartext XMP stream with /Flate 
                LOG.error("Metadata skipped because it could not be read", ex);
            }
        }

        final COSDictionary destOCP = destCatalog.getCOSObject().getCOSDictionary(COSName.OCPROPERTIES);
        final COSDictionary srcOCP = srcCatalog.getCOSObject().getCOSDictionary(COSName.OCPROPERTIES);
        if (destOCP == null && srcOCP != null)
        {
            destCatalog.getCOSObject().setItem(COSName.OCPROPERTIES, cloner.cloneForNewDocument(srcOCP));
        }
        else if (destOCP != null && srcOCP != null)
        {
            cloner.cloneMerge(srcOCP, destOCP);
        }

        mergeOutputIntents(cloner, srcCatalog, destCatalog);

        // merge logical structure hierarchy
        boolean mergeStructTree = false;
        int destParentTreeNextKey = -1;
        Map<Integer, COSObjectable> srcNumberTreeAsMap = null;
        Map<Integer, COSObjectable> destNumberTreeAsMap = null;
        final PDStructureTreeRoot srcStructTree = srcCatalog.getStructureTreeRoot();
        PDStructureTreeRoot destStructTree = destCatalog.getStructureTreeRoot();
        if (destStructTree == null && srcStructTree != null)
        {
            // create a dummy structure tree in the destination, so that the source
            // tree is cloned. (We can't just copy the tree reference due to PDFBOX-3999)
            destStructTree = new PDStructureTreeRoot();
            destCatalog.setStructureTreeRoot(destStructTree);
            destStructTree.setParentTree(new PDNumberTreeNode(PDParentTreeValue.class));
            // PDFBOX-4429: remove bogus StructParent(s)
            for (final PDPage page : destCatalog.getPages())
            {
                page.getCOSObject().removeItem(COSName.STRUCT_PARENTS);
                for (final PDAnnotation ann : page.getAnnotations())
                {
                    ann.getCOSObject().removeItem(COSName.STRUCT_PARENT);
                }
            }
        }
        if (destStructTree != null)
        {
            final PDNumberTreeNode destParentTree = destStructTree.getParentTree();
            destParentTreeNextKey = destStructTree.getParentTreeNextKey();
            if (destParentTree != null)
            {
                destNumberTreeAsMap = getNumberTreeAsMap(destParentTree);
                if (destParentTreeNextKey < 0)
                {
                    if (destNumberTreeAsMap.isEmpty())
                    {
                        destParentTreeNextKey = 0;
                    }
                    else
                    {
                        destParentTreeNextKey = Collections.max(destNumberTreeAsMap.keySet()) + 1;
                    }
                }
                if (destParentTreeNextKey >= 0 && srcStructTree != null)
                {
                    final PDNumberTreeNode srcParentTree = srcStructTree.getParentTree();
                    if (srcParentTree != null)
                    {
                        srcNumberTreeAsMap = getNumberTreeAsMap(srcParentTree);
                        if (!srcNumberTreeAsMap.isEmpty())
                        {
                            mergeStructTree = true;
                        }
                    }
                }
            }
        }

        final Map<COSDictionary, COSDictionary> objMapping = new HashMap<>();
        int pageIndex = 0;
        for (final PDPage page : srcCatalog.getPages())
        {
            final PDPage newPage = new PDPage((COSDictionary) cloner.cloneForNewDocument(page.getCOSObject()));
            if (!mergeStructTree)
            {
                // PDFBOX-4429: remove bogus StructParent(s)
                newPage.getCOSObject().removeItem(COSName.STRUCT_PARENTS);
                for (final PDAnnotation ann : newPage.getAnnotations())
                {
                    ann.getCOSObject().removeItem(COSName.STRUCT_PARENT);
                }
            }
            newPage.setCropBox(page.getCropBox());
            newPage.setMediaBox(page.getMediaBox());
            newPage.setRotation(page.getRotation());
            final PDResources resources = page.getResources();
            if (resources != null)
            {
                // this is smart enough to just create references for resources that are used on multiple pages
                newPage.setResources(new PDResources((COSDictionary) cloner.cloneForNewDocument(resources)));
            }
            else
            {
                newPage.setResources(new PDResources());
            }
            if (mergeStructTree)
            {
                // add the value of the destination ParentTreeNextKey to every source element 
                // StructParent(s) value so that these don't overlap with the existing values
                updateStructParentEntries(newPage, destParentTreeNextKey);
                objMapping.put(page.getCOSObject(), newPage.getCOSObject());
                final List<PDAnnotation> oldAnnots = page.getAnnotations();
                final List<PDAnnotation> newAnnots = newPage.getAnnotations();
                for (int i = 0; i < oldAnnots.size(); i++)
                {
                    objMapping.put(oldAnnots.get(i).getCOSObject(), newAnnots.get(i).getCOSObject());
                }
                // TODO update mapping for XObjects
            }
            destination.addPage(newPage);
            
            if (pageIndex == pageIndexOpenActionDest)
            {
                // PDFBOX-3972: reassign the page.
                // The openAction is either a PDActionGoTo or a PDPageDestination
                final PDDestinationOrAction openAction = destCatalog.getOpenAction();
                final PDPageDestination pageDestination;
                if (openAction instanceof PDActionGoTo)
                {
                    pageDestination = (PDPageDestination) ((PDActionGoTo) openAction).getDestination();
                }
                else
                {
                    pageDestination = (PDPageDestination) openAction;
                }
                pageDestination.setPage(newPage);
            }
            ++pageIndex;
        }
        if (mergeStructTree)
        {
            updatePageReferences(cloner, srcNumberTreeAsMap, objMapping);
            int maxSrcKey = -1;
            for (final Map.Entry<Integer, COSObjectable> entry : srcNumberTreeAsMap.entrySet())
            {
                final int srcKey = entry.getKey();
                maxSrcKey = Math.max(srcKey, maxSrcKey);
                destNumberTreeAsMap.put(destParentTreeNextKey + srcKey, cloner.cloneForNewDocument(entry.getValue()));
            }
            destParentTreeNextKey += maxSrcKey + 1;
            final PDNumberTreeNode newParentTreeNode = new PDNumberTreeNode(PDParentTreeValue.class);

            // Note that all elements are stored flatly. This could become a problem for large files
            // when these are opened in a viewer that uses the tagging information.
            // If this happens, then ​PDNumberTreeNode should be improved with a convenience method that
            // stores the map into a B+Tree, see https://en.wikipedia.org/wiki/B+_tree
            newParentTreeNode.setNumbers(destNumberTreeAsMap);

            destStructTree.setParentTree(newParentTreeNode);
            destStructTree.setParentTreeNextKey(destParentTreeNextKey);

            mergeKEntries(cloner, srcStructTree, destStructTree);
            mergeRoleMap(srcStructTree, destStructTree);
            mergeIDTree(cloner, srcStructTree, destStructTree);
            mergeMarkInfo(destCatalog, srcCatalog);
            mergeLanguage(destCatalog, srcCatalog);
            mergeViewerPreferences(destCatalog, srcCatalog);
        }
    }

    private void mergeViewerPreferences(final PDDocumentCatalog destCatalog, final PDDocumentCatalog srcCatalog)
    {
        final PDViewerPreferences srcViewerPreferences = srcCatalog.getViewerPreferences();
        if (srcViewerPreferences == null)
        {
            return;
        }
        PDViewerPreferences destViewerPreferences = destCatalog.getViewerPreferences();
        if (destViewerPreferences == null)
        {
            destViewerPreferences = new PDViewerPreferences(new COSDictionary());
            destCatalog.setViewerPreferences(destViewerPreferences);
        }
        mergeInto(srcViewerPreferences.getCOSObject(), destViewerPreferences.getCOSObject(),
                  Collections.<COSName>emptySet());

        // check the booleans - set to true if one is set and true
        if (srcViewerPreferences.hideToolbar() || destViewerPreferences.hideToolbar())
        {
            destViewerPreferences.setHideToolbar(true);
        }
        if (srcViewerPreferences.hideMenubar() || destViewerPreferences.hideMenubar())
        {
            destViewerPreferences.setHideMenubar(true);
        }
        if (srcViewerPreferences.hideWindowUI() || destViewerPreferences.hideWindowUI())
        {
            destViewerPreferences.setHideWindowUI(true);
        }
        if (srcViewerPreferences.fitWindow() || destViewerPreferences.fitWindow())
        {
            destViewerPreferences.setFitWindow(true);
        }
        if (srcViewerPreferences.centerWindow() || destViewerPreferences.centerWindow())
        {
            destViewerPreferences.setCenterWindow(true);
        }
        if (srcViewerPreferences.displayDocTitle() || destViewerPreferences.displayDocTitle())
        {
            destViewerPreferences.setDisplayDocTitle(true);
        }
    }

    private void mergeLanguage(final PDDocumentCatalog destCatalog, final PDDocumentCatalog srcCatalog)
    {
        if (destCatalog.getLanguage() == null && srcCatalog.getLanguage() != null)
        {
            destCatalog.setLanguage(srcCatalog.getLanguage());
        }
    }

    private void mergeMarkInfo(final PDDocumentCatalog destCatalog, final PDDocumentCatalog srcCatalog)
    {
        PDMarkInfo destMark = destCatalog.getMarkInfo();
        PDMarkInfo srcMark = srcCatalog.getMarkInfo();
        if (destMark == null)
        {
            destMark = new PDMarkInfo();
        }
        if (srcMark == null)
        {
            srcMark = new PDMarkInfo();
        }
        destMark.setMarked(true);
        destMark.setSuspect(srcMark.isSuspect() || destMark.isSuspect());
        destMark.setSuspect(srcMark.usesUserProperties() || destMark.usesUserProperties());
        destCatalog.setMarkInfo(destMark);
    }

    private void mergeKEntries(final PDFCloneUtility cloner,
                               final PDStructureTreeRoot srcStructTree,
                               final PDStructureTreeRoot destStructTree) throws IOException
    {
        // make new /K with array that has the input /K entries
        final COSArray newKArray = new COSArray();
        if (destStructTree.getK() != null)
        {
            final COSBase base = destStructTree.getK();
            if (base instanceof COSArray)
            {
                newKArray.addAll((COSArray) base);
            }
            else
            {
                newKArray.add(base);
            }
        }
        if (srcStructTree.getK() != null)
        {
            final COSBase base = cloner.cloneForNewDocument(srcStructTree.getK());
            if (base instanceof COSArray)
            {
                newKArray.addAll((COSArray) base);
            }
            else
            {
                newKArray.add(base);
            }
        }
        if (newKArray.size() > 0)
        {
            final COSDictionary kDictLevel0 = new COSDictionary();
            updateParentEntry(newKArray, kDictLevel0);
            kDictLevel0.setItem(COSName.K, newKArray);
            kDictLevel0.setItem(COSName.P, destStructTree);
            kDictLevel0.setItem(COSName.S, COSName.DOCUMENT);
            destStructTree.setK(kDictLevel0);
        }
    }

    private void mergeIDTree(final PDFCloneUtility cloner,
                             final PDStructureTreeRoot srcStructTree,
                             final PDStructureTreeRoot destStructTree) throws IOException
    {
        final PDNameTreeNode<PDStructureElement> srcIDTree = srcStructTree.getIDTree();
        PDNameTreeNode<PDStructureElement> destIDTree = destStructTree.getIDTree();
        if (srcIDTree == null)
        {
            return;
        }
        if (destIDTree == null)
        {
            destIDTree = new PDStructureElementNameTreeNode();
        }
        final Map<String, PDStructureElement> srcNames = getIDTreeAsMap(srcIDTree);
        final Map<String, PDStructureElement> destNames = getIDTreeAsMap(destIDTree);
        for (final Map.Entry<String, PDStructureElement> entry : srcNames.entrySet())
        {
            if (destNames.containsKey(entry.getKey()))
            {
                LOG.warn("key " + entry.getKey() + " already exists in destination IDTree");
            }
            else
            {
                destNames.put(entry.getKey(),
                              new PDStructureElement((COSDictionary) cloner.cloneForNewDocument(entry.getValue().getCOSObject())));
            }
        }
        destIDTree = new PDStructureElementNameTreeNode();
        destIDTree.setNames(destNames);
        destStructTree.setIDTree(destIDTree);
        // Note that all elements are stored flatly. This could become a problem for large files
        // when these are opened in a viewer that uses the tagging information.
        // If this happens, then PDNameTreeNode should be improved with a convenience method that
        // stores the map into a B+Tree, see https://en.wikipedia.org/wiki/B+_tree
    }

    // PDNameTreeNode.getNames() only brings one level, this is why we need this
    // might be made public at a later time, or integrated into PDNameTreeNode with template.
    static Map<String, PDStructureElement> getIDTreeAsMap(final PDNameTreeNode<PDStructureElement> idTree)
            throws IOException
    {
        Map<String, PDStructureElement> names = idTree.getNames();
        if (names == null)
        {
            names = new LinkedHashMap<>();
        }
        else
        {
            // must copy because the map is read only
            names = new LinkedHashMap<>(names);
        }
        final List<PDNameTreeNode<PDStructureElement>> kids = idTree.getKids();
        if (kids != null)
        {
            for (final PDNameTreeNode<PDStructureElement> kid : kids)
            {
                names.putAll(getIDTreeAsMap(kid));
            }
        }
        return names;
    }

    // PDNumberTreeNode.getNumbers() only brings one level, this is why we need this
    // might be made public at a later time, or integrated into PDNumberTreeNode.
    static Map<Integer, COSObjectable> getNumberTreeAsMap(final PDNumberTreeNode tree)
            throws IOException
    {
        Map<Integer, COSObjectable> numbers = tree.getNumbers();
        if (numbers == null)
        {
            numbers = new LinkedHashMap<>();
        }
        else
        {
            // must copy because the map is read only
            numbers = new LinkedHashMap<>(numbers);
        }
        final List<PDNumberTreeNode> kids = tree.getKids();
        if (kids != null)
        {
            for (final PDNumberTreeNode kid : kids)
            {
                numbers.putAll(getNumberTreeAsMap(kid));
            }
        }
        return numbers;
    }

    private void mergeRoleMap(final PDStructureTreeRoot srcStructTree, final PDStructureTreeRoot destStructTree)
    {
        final COSDictionary srcDict = srcStructTree.getCOSObject().getCOSDictionary(COSName.ROLE_MAP);
        final COSDictionary destDict = destStructTree.getCOSObject().getCOSDictionary(COSName.ROLE_MAP);
        if (srcDict == null)
        {
            return;
        }
        if (destDict == null)
        {
            destStructTree.getCOSObject().setItem(COSName.ROLE_MAP, srcDict); // clone not needed
            return;
        }
        for (final Map.Entry<COSName, COSBase> entry : srcDict.entrySet())
        {
            final COSBase destValue = destDict.getDictionaryObject(entry.getKey());
            if (destValue != null && destValue.equals(entry.getValue()))
            {
                // already exists, but identical
                continue;
            }
            if (destDict.containsKey(entry.getKey()))
            {
                LOG.warn("key " + entry.getKey() + " already exists in destination RoleMap");
            }
            else
            {
                destDict.setItem(entry.getKey(), entry.getValue());
            }
        }
    }

    private void mergeAcroForm(final PDFCloneUtility cloner, final PDDocumentCatalog destCatalog,
                               final PDDocumentCatalog srcCatalog ) throws IOException
    {
        try
        {
            final PDAcroForm destAcroForm = destCatalog.getAcroForm();
            final PDAcroForm srcAcroForm = srcCatalog.getAcroForm();
            
            if (destAcroForm == null && srcAcroForm != null)
            {
                destCatalog.getCOSObject().setItem(COSName.ACRO_FORM,
                        cloner.cloneForNewDocument(srcAcroForm.getCOSObject()));       
                
            }
            else
            {
                if (srcAcroForm != null)
                {
                    if (acroFormMergeMode == AcroFormMergeMode.PDFBOX_LEGACY_MODE)
                    {
                        acroFormLegacyMode(cloner, destAcroForm, srcAcroForm);
                    }
                    else if (acroFormMergeMode == AcroFormMergeMode.JOIN_FORM_FIELDS_MODE)
                    {
                        acroFormJoinFieldsMode(cloner, destAcroForm, srcAcroForm);
                    }
                }
            }
        }
        catch (IOException e)
        {
            // if we are not ignoring exceptions, we'll re-throw this
            if (!ignoreAcroFormErrors)
            {
                throw new IOException(e);
            }
        }
    }
    
    /*
     * Merge the contents of the source form into the destination form for the
     * destination file.
     *
     * @param cloner the object cloner for the destination document
     * @param destAcroForm the destination form
     * @param srcAcroForm the source form
     * @throws IOException If an error occurs while adding the field.
     */
    private void acroFormJoinFieldsMode(final PDFCloneUtility cloner, final PDAcroForm destAcroForm, final PDAcroForm srcAcroForm)
            throws IOException
    {
        acroFormLegacyMode(cloner, destAcroForm, srcAcroForm);
    }
    
    /*
     * Merge the contents of the source form into the destination form for the
     * destination file.
     *
     * @param cloner the object cloner for the destination document
     * @param destAcroForm the destination form
     * @param srcAcroForm the source form
     * @throws IOException If an error occurs while adding the field.
     */
    private void acroFormLegacyMode(final PDFCloneUtility cloner, final PDAcroForm destAcroForm, final PDAcroForm srcAcroForm)
            throws IOException
    {
        final List<PDField> srcFields = srcAcroForm.getFields();
        final COSArray destFields;

        if (srcFields != null && !srcFields.isEmpty())
        {
            // if a form is merged multiple times using PDFBox the newly generated
            // fields starting with dummyFieldName may already exist. We need to determine the last unique 
            // number used and increment that.
            final String prefix = "dummyFieldName";
            final int prefixLength = prefix.length();

            for (final PDField destField : destAcroForm.getFieldTree())
            {
                final String fieldName = destField.getPartialName();
                if (fieldName.startsWith(prefix))
                {
                    nextFieldNum = Math.max(nextFieldNum, Integer.parseInt(fieldName.substring(prefixLength, fieldName.length())) + 1);
                }
            }

            // get the destinations root fields. Could be that the entry doesn't exist
            // or is of wrong type
            final COSBase base = destAcroForm.getCOSObject().getItem(COSName.FIELDS);
            if (base instanceof COSArray)
            {
                destFields = (COSArray) base;
            }
            else
            {
                destFields = new COSArray();
            }
            
            for (final PDField srcField : srcAcroForm.getFields())
            {
                final COSDictionary dstField = (COSDictionary) cloner.cloneForNewDocument(srcField.getCOSObject());
                // if the form already has a field with this name then we need to rename this field
                // to prevent merge conflicts.
                if (destAcroForm.getField(srcField.getFullyQualifiedName()) != null)
                {
                    dstField.setString(COSName.T, prefix + nextFieldNum++);
                }
                destFields.add(dstField);
            }
            destAcroForm.getCOSObject().setItem(COSName.FIELDS,destFields);
        }
    }


    // copy outputIntents to destination, but avoid duplicate OutputConditionIdentifier,
    // except when it is missing or is named "Custom".
    private void mergeOutputIntents(final PDFCloneUtility cloner,
                                    final PDDocumentCatalog srcCatalog, final PDDocumentCatalog destCatalog) throws IOException
    {
        final List<PDOutputIntent> srcOutputIntents = srcCatalog.getOutputIntents();
        final List<PDOutputIntent> dstOutputIntents = destCatalog.getOutputIntents();
        for (final PDOutputIntent srcOI : srcOutputIntents)
        {
            final String srcOCI = srcOI.getOutputConditionIdentifier();
            if (srcOCI != null && !"Custom".equals(srcOCI))
            {
                // is that identifier already there?
                boolean skip = false;
                for (final PDOutputIntent dstOI : dstOutputIntents)
                {
                    if (dstOI.getOutputConditionIdentifier().equals(srcOCI))
                    {
                        skip = true;
                        break;
                    }
                }
                if (skip)
                {
                    continue;
                }
            }
            destCatalog.addOutputIntent(new PDOutputIntent((COSDictionary) cloner.cloneForNewDocument(srcOI)));
            dstOutputIntents.add(srcOI);
        }
    }

    private int nextFieldNum = 1;


    /**
     * Indicates if acroform errors are ignored or not.
     *
     * @return true if acroform errors are ignored
     */
    public boolean isIgnoreAcroFormErrors()
    {
        return ignoreAcroFormErrors;
    }

    /**
     * Set to true to ignore acroform errors.
     *
     * @param ignoreAcroFormErrorsValue true if acroform errors should be
     * ignored
     */
    public void setIgnoreAcroFormErrors(final boolean ignoreAcroFormErrorsValue)
    {
        ignoreAcroFormErrors = ignoreAcroFormErrorsValue;
    }

    /**
     * Update the Pg and Obj references to the new (merged) page.
     */
    private void updatePageReferences(final PDFCloneUtility cloner,
                                      final Map<Integer, COSObjectable> numberTreeAsMap,
                                      final Map<COSDictionary, COSDictionary> objMapping) throws IOException
    {
        for (final COSObjectable obj : numberTreeAsMap.values())
        {
            if (obj == null)
            {
                continue;
            }
            final PDParentTreeValue val = (PDParentTreeValue) obj;
            final COSBase base = val.getCOSObject();
            if (base instanceof COSArray)
            {
                updatePageReferences(cloner, (COSArray) base, objMapping);
            }
            else
            {
                updatePageReferences(cloner, (COSDictionary) base, objMapping);
            }
        }
    }

    /**
     * Update the Pg and Obj references to the new (merged) page.
     *
     * @param parentTreeEntry
     * @param objMapping mapping between old and new references
     */
    private void updatePageReferences(final PDFCloneUtility cloner,
                                      final COSDictionary parentTreeEntry, final Map<COSDictionary, COSDictionary> objMapping)
            throws IOException
    {
        final COSDictionary pageDict = parentTreeEntry.getCOSDictionary(COSName.PG);
        if (objMapping.containsKey(pageDict))
        {
            parentTreeEntry.setItem(COSName.PG, objMapping.get(pageDict));
        }
        final COSBase obj = parentTreeEntry.getDictionaryObject(COSName.OBJ);
        if (obj instanceof COSDictionary)
        {
            final COSDictionary objDict = (COSDictionary) obj;
            if (objMapping.containsKey(objDict))
            {
                parentTreeEntry.setItem(COSName.OBJ, objMapping.get(objDict));
            }
            else
            {
                // PDFBOX-3999: clone objects that are not in mapping to make sure that
                // these don't remain attached to the source document
                final COSBase item = parentTreeEntry.getItem(COSName.OBJ);
                if (item instanceof COSObject)
                {
                    LOG.debug("clone potential orphan object in structure tree: " + item +
                            ", Type: " + objDict.getNameAsString(COSName.TYPE) +
                            ", Subtype: " + objDict.getNameAsString(COSName.SUBTYPE) +
                            ", T: " + objDict.getNameAsString(COSName.T));
                }
                else
                {
                    // don't display in full because of stack overflow
                    LOG.debug("clone potential orphan object in structure tree" +
                            ", Type: " + objDict.getNameAsString(COSName.TYPE) +
                            ", Subtype: " + objDict.getNameAsString(COSName.SUBTYPE) +
                            ", T: " + objDict.getNameAsString(COSName.T));
                }
                parentTreeEntry.setItem(COSName.OBJ, cloner.cloneForNewDocument(obj));
            }
        }
        final COSBase kSubEntry = parentTreeEntry.getDictionaryObject(COSName.K);
        if (kSubEntry instanceof COSArray)
        {
            updatePageReferences(cloner, (COSArray) kSubEntry, objMapping);
        }
        else if (kSubEntry instanceof COSDictionary)
        {
            updatePageReferences(cloner, (COSDictionary) kSubEntry, objMapping);
        }
    }

    private void updatePageReferences(final PDFCloneUtility cloner,
                                      final COSArray parentTreeEntry, final Map<COSDictionary, COSDictionary> objMapping)
            throws IOException
    {
        for (int i = 0; i < parentTreeEntry.size(); i++)
        {
            final COSBase subEntry = parentTreeEntry.getObject(i);
            if (subEntry instanceof COSArray)
            {
                updatePageReferences(cloner, (COSArray) subEntry, objMapping);
            }
            else if (subEntry instanceof COSDictionary)
            {
                updatePageReferences(cloner, (COSDictionary) subEntry, objMapping);
            }
        }
    }

    /**
     * Update the P reference to the new parent dictionary.
     *
     * @param kArray the kids array
     * @param newParent the new parent
     */
    private void updateParentEntry(final COSArray kArray, final COSDictionary newParent)
    {
        for (int i = 0; i < kArray.size(); i++)
        {
            final COSBase subEntry = kArray.getObject(i);
            if (subEntry instanceof COSDictionary)
            {
                final COSDictionary dictEntry = (COSDictionary) subEntry;
                if (dictEntry.getDictionaryObject(COSName.P) != null)
                {
                    dictEntry.setItem(COSName.P, newParent);
                }
            }
        }
    }

    /**
     * Update the StructParents and StructParent values in a PDPage.
     *
     * @param page the new page
     * @param structParentOffset the offset which should be applied
     */
    private void updateStructParentEntries(final PDPage page, final int structParentOffset) throws IOException
    {
        if (page.getStructParents() >= 0)
        {
            page.setStructParents(page.getStructParents() + structParentOffset);
        }
        final List<PDAnnotation> annots = page.getAnnotations();
        final List<PDAnnotation> newannots = new ArrayList<>();
        for (final PDAnnotation annot : annots)
        {
            if (annot.getStructParent() >= 0)
            {
                annot.setStructParent(annot.getStructParent() + structParentOffset);
            }
            newannots.add(annot);
        }
        page.setAnnotations(newannots);
    }
    
    /**
     * Test for dynamic XFA content.
     * 
     * @param acroForm the AcroForm
     * @return true if there is a dynamic XFA form.
     */
    private boolean isDynamicXfa(final PDAcroForm acroForm)
    {
        return acroForm != null && acroForm.xfaIsDynamic();
    }

    /**
     * This will add all of the dictionaries keys/values to this dictionary, but
     * only if they are not in an exclusion list and if they don't already
     * exist. If a key already exists in this dictionary then nothing is
     * changed.
     *
     * @param src The source dictionary to get the keys/values from.
     * @param dst The destination dictionary to merge the keys/values into.
     * @param exclude Names of keys that shall be skipped.
     */
    private void mergeInto(final COSDictionary src, final COSDictionary dst, final Set<COSName> exclude)
    {
        for (final Map.Entry<COSName, COSBase> entry : src.entrySet())
        {
            if (!exclude.contains(entry.getKey()) && !dst.containsKey(entry.getKey()))
            {
                dst.setItem(entry.getKey(), entry.getValue());
            }
        }
    }
}

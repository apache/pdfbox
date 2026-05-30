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
package org.apache.pdfbox.pdfwriter.compress;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.pdfbox.pdfparser.PDFXRefStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.cos.COSObjectKey;
import org.apache.pdfbox.cos.COSStream;

/**
 * An instance of this class compresses the contents of a given {@link PDDocument}.
 *
 * @author Christian Appl
 */
public class COSWriterCompressionPool
{

    public static final float MINIMUM_SUPPORTED_VERSION = 1.6f;

    private final PDDocument document;
    private final CompressParameters parameters;

    private final COSObjectPool objectPool;

    // A list containing all objects, that shall be directly appended to the document's top level container.
    private final List<COSObjectKey> topLevelObjects = new ArrayList<>();
    // A list containing all objects, that may be appended to an object stream.
    private final List<COSObjectKey> objectStreamObjects = new ArrayList<>();
    // A list of all direct objects
    private final Set<COSBase> allDirectObjects = new HashSet<>();

    /**
     * <p>
     * Constructs an object that can be used to compress the contents of a given {@link PDDocument}. It provides the
     * means to:
     * </p>
     * <ul>
     * <li>Compress the COSStructure of the document, by streaming {@link COSBase}s to compressed
     * {@link COSWriterObjectStream}s</li>
     * </ul>
     *
     * @param document The document, that shall be compressed.
     * @param parameters The configuration of the compression operations, that shall be applied.
     */
    public COSWriterCompressionPool(PDDocument document, CompressParameters parameters)
    {
        this.document = document;
        this.parameters = parameters != null ? parameters : new CompressParameters();
        objectPool = new COSObjectPool(document.getDocument().getHighestXRefObjectNumber());

        // Initialize object pool.
        COSDictionary trailer = document.getDocument().getTrailer();
        List<COSBase> cosBaseList = new ArrayList<>();
        COSDictionary root = trailer.getCOSDictionary(COSName.ROOT);
        if (root != null)
        {
            cosBaseList.add(root);
        }
        COSDictionary info = trailer.getCOSDictionary(COSName.INFO);
        if (info != null)
        {
            cosBaseList.add(info);
        }
        while (!cosBaseList.isEmpty())
        {
            cosBaseList = addStructure(cosBaseList);
        }
        allDirectObjects.clear();

        Collections.sort(objectStreamObjects);
        Collections.sort(topLevelObjects);
    }

    /**
     * Adds the given {@link COSBase} to this pool, using the given {@link COSObjectKey} as it's referencable ID. This
     * method shall determine an appropriate key, for yet unregistered objects, to register them. Depending on the type
     * of object, it shall either be appended as-is or shall be appended to a compressed {@link COSWriterObjectStream}.
     *
     * @param key The {@link COSObjectKey} that shall be used as the {@link COSBase}s ID, if possible.
     * @param base The {@link COSBase}, that shall be registered in this pool.
     */
    private COSBase addObjectToPool(COSObjectKey key, COSBase base)
    {
        // Drop hollow objects.
        COSBase current = base instanceof COSObject ? ((COSObject) base).getObject() : base;
        // to avoid to mixup indirect COSInteger objects holding the same value we have to check
        // if the given key is the same than the key which is stored for the "same" base object within the object pool
        // the same is always true for COSFloat, COSBoolean and COSName and under certain circumstances for the
        // remaining types as well
        if (current == null || (key == null && objectPool.contains(current)))
        {
            return current;
        }
        if (key != null && objectPool.contains(key))
        {
            COSBase cosObject = objectPool.getObject(key);
            // check if the key belongs to the same object
            if (cosObject == current || cosObject == base)
            {
                return current;
            }
        }

        // Check whether the object can not be appended to an object stream.
        // An objectStream shall only contain generation 0 objects.
        // It shall never contain the encryption dictionary.
        // It shall never contain the document's root dictionary. (relevant for document encryption)
        // It shall never contain other streams.
        if ((key != null && key.getGeneration() != 0)
                || current instanceof COSStream
                || (document.getEncryption() != null
                        && current == document.getEncryption().getCOSObject())
                || current == this.document.getDocument().getTrailer()
                        .getCOSDictionary(COSName.ROOT))
        {
            COSObjectKey actualKey = objectPool.put(key, current);
            if (actualKey == null)
            {
                return current;
            }
            // check if the key of the indirect object matches the key of the referenced object
            // otherwise update the key
            if (!actualKey.equals(key) && base instanceof COSObject)
            {
                base.setKey(actualKey);
            }
            topLevelObjects.add(actualKey);
            return current;
        }

        // Determine the object key.
        COSObjectKey actualKey = objectPool.put(key, current);
        if (actualKey == null)
        {
            return current;
        }
        // check if the key of the indirect object matches the key of the referenced object
        // otherwise update the key
        if (!actualKey.equals(key) && base instanceof COSObject)
        {
            base.setKey(actualKey);
        }
        // Append it to an object stream.
        this.objectStreamObjects.add(actualKey);
        return current;
    }

    /**
     * Attempts to find yet unregistered streams and dictionaries in the given structure.
     *
     * @param cosBaseList A list of objects to be added for compressing.
     */
    private List<COSBase> addStructure(List<COSBase> cosBaseList)
    {
        List<COSBase> cosBaseListNext = new ArrayList<>();
        for (COSBase cosBase : cosBaseList)
        {
            cosBaseListNext.addAll(addStructure(cosBase));
        }
        cosBaseList.clear();
        return cosBaseListNext;
    }

    /**
     * Attempts to find yet unregistered streams and dictionaries in the given structure.
     *
     * @param current The object to be added for compressing.
     */
    private List<COSBase> addStructure(COSBase current)
    {
        COSBase base = current;
        if (!current.isDirect() && (current instanceof COSDictionary || current instanceof COSArray))
        {
            base = addObjectToPool(base.getKey(), current);
        }
        else if (current instanceof COSObject)
        {
            base = ((COSObject) current).getObject();
            if (base != null)
            {
                base = addObjectToPool(current.getKey(), current);
            }
        }
        if (base instanceof COSArray)
        {
            return getElements(((COSArray) base).toList());
        }
        else if (base instanceof COSDictionary)
        {
            return getElements(((COSDictionary) base).getValues());
        }
        return Collections.emptyList();
    }

    /**
     * Collect all relevant objects from a COSDictionary/COSArray.
     * 
     * @param elements collection of all elements of a COSDictionary/COSArray.
     * 
     * @return a collection containing the relevant objects within the given Collection.
     */
    private List<COSBase> getElements(Collection<? extends COSBase> elements)
    {
        List<COSBase> relevantElements = new ArrayList<>();
        for (COSBase element : elements)
        {
            if (filterElement(element))
            {
                relevantElements.add(element);
            }
        }
        return relevantElements;
    }

    private boolean filterElement(COSBase element)
    {
        if (element instanceof COSObject)
        {
            COSObject cosObject = (COSObject) element;
            COSObjectKey objectKey = cosObject.getKey();
            COSBase object = cosObject.getObject();
            if (objectKey != null && objectPool.contains(objectKey))
            {
                // check if the stored object matches the referenced object otherwise replace the key with a new one
                // there may differences if some imported content uses the same object numbers than the target pdf
                if (objectPool.getObject(objectKey).equals(object))
                {
                    return false;
                }
                cosObject.setKey(null);
            }
            return object != null;
        }
        else if (element instanceof COSArray
                || (element instanceof COSDictionary && !allDirectObjects.contains(element)))
        {
            allDirectObjects.add(element);
            return true;
        }
        return false;
    }

    /**
     * Returns all {@link COSBase}s, that must be added to the document's top level container. Those objects are not
     * valid to be added to an object stream.
     *
     * @return A list of all top level {@link COSBase}s.
     */
    public List<COSObjectKey> getTopLevelObjects()
    {
        return topLevelObjects;
    }

    /**
     * Returns all {@link COSBase}s that can be appended to an object stream. This list is only provided to enable
     * reflections. Contained objects should indeed be added to a compressed document via an object stream, as can be
     * created via calling: {@link COSWriterCompressionPool#createObjectStreams()}
     *
     * @return A list of all {@link COSBase}s, that can be added to an object stream.
     */
    public List<COSObjectKey> getObjectStreamObjects()
    {
        return objectStreamObjects;
    }

    /**
     * Returns true, if the given {@link COSBase} is a registered object of this compression pool.
     *
     * @param object The object, that shall be checked.
     * @return True, if the given {@link COSBase} is a registered object of this compression pool.
     */
    public boolean contains(COSBase object)
    {
        return objectPool.contains(object);
    }

    /**
     * Returns the {@link COSObjectKey}, that is registered for the given {@link COSBase} in this compression pool.
     *
     * @param object The {@link COSBase} a {@link COSObjectKey} is registered for in this compression pool.
     * @return The {@link COSObjectKey}, that is registered for the given {@link COSBase} in this compression pool, if
     * such an object is contained.
     */
    public COSObjectKey getKey(COSBase object)
    {
        return objectPool.getKey(object);
    }

    /**
     * Returns the {@link COSBase}, that is registered for the given {@link COSObjectKey} in this compression pool.
     *
     * @param key The {@link COSObjectKey} a {@link COSBase} is registered for in this compression pool.
     * @return The {@link COSBase}, that is registered for the given {@link COSObjectKey} in this compression pool, if
     * such an object is contained.
     */
    public COSBase getObject(COSObjectKey key)
    {
        return objectPool.getObject(key);
    }

    /**
     * Returns the highest object number, that is registered in this compression pool.
     *
     * @return The highest object number, that is registered in this compression pool.
     */
    public long getHighestXRefObjectNumber()
    {
        return objectPool.getHighestXRefObjectNumber();
    }

    /**
     * Creates {@link COSWriterObjectStream}s for all currently registered objects of this pool, that have been marked
     * as fit for being compressed in this manner. Such object streams may be added to a PDF document and shall be
     * declared in a document's {@link PDFXRefStream} accordingly. The objects contained in such a stream must not be
     * added to the document separately.
     *
     * @return The created {@link COSWriterObjectStream}s for all currently registered compressible objects.
     */
    public List<COSWriterObjectStream> createObjectStreams()
    {
        List<COSWriterObjectStream> objectStreams = new ArrayList<>();
        COSWriterObjectStream objectStream = null;
        for (int i = 0; i < objectStreamObjects.size(); i++)
        {
            COSObjectKey key = objectStreamObjects.get(i);
            if (objectStream == null || (i % parameters.getObjectStreamSize()) == 0)
            {
                objectStream = new COSWriterObjectStream(this);
                objectStreams.add(objectStream);
            }
            objectStream.prepareStreamObject(key, objectPool.getObject(key));
        }
        return objectStreams;
    }
}

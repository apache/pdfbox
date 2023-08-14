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
package org.apache.pdfbox.pdfparser.xref;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.cos.COSObjectKey;
import org.apache.pdfbox.pdfparser.PDFXRefStream;

/**
 * A class representing a reference to an object stream entry in a PDF's crossreference stream ({@link PDFXRefStream}).
 * 
 * @author Christian Appl
 */
public class ObjectStreamXReference extends AbstractXReference
{

    private final int objectStreamIndex;
    private final COSObjectKey key;
    private final COSBase object;
    private final COSObjectKey parentKey;

    /**
     * Prepares a object stream entry reference for the given {@link COSObject} in a PDF's crossreference stream
     * ({@link PDFXRefStream}).
     *
     * @param objectStreamIndex The index of the {@link COSObject} in the containing object stream.
     * @param key The {@link COSObjectKey}, that is represented by this entry.
     * @param object The {@link COSObject}, that is represented by this entry.
     * @param parentKey The {@link COSObjectKey} of the object stream, that is containing the object.
     */
    public ObjectStreamXReference(int objectStreamIndex, COSObjectKey key, COSBase object,
            COSObjectKey parentKey)
    {
        super(XReferenceType.OBJECT_STREAM_ENTRY);
        this.objectStreamIndex = objectStreamIndex;
        this.key = key;
        this.object = object;
        this.parentKey = parentKey;
    }

    /**
     * Returns the index of the {@link COSObject} in it's containing object stream.
     *
     * @return The index of the {@link COSObject} in it's containing object stream.
     */
    public int getObjectStreamIndex()
    {
        return objectStreamIndex;
    }

    /**
     * Returns the {@link COSObjectKey} of the object, that is described by this crossreference stream entry.
     *
     * @return The {@link COSObjectKey} of the object, that is described by this crossreference stream entry.
     */
    @Override
    public COSObjectKey getReferencedKey()
    {
        return key;
    }

    /**
     * Returns the {@link COSObject}, that is described by this crossreference stream entry.
     *
     * @return The {@link COSObject}, that is described by this crossreference stream entry.
     */
    public COSBase getObject()
    {
        return object;
    }

    /**
     * Returns the {@link COSObjectKey} of the object stream, that is containing the object.
     *
     * @return The {@link COSObjectKey} of the object stream, that is containing the object.
     */
    public COSObjectKey getParentKey()
    {
        return parentKey;
    }

    /**
     * Returns the value for the second column of the crossreference stream entry. (This is object number from the
     * {@link COSObjectKey} of the object stream, that is containing the object represented by this entry - for entries
     * of this type..)
     *
     * @return The value for the second column of the crossreference stream entry.
     */
    @Override
    public long getSecondColumnValue()
    {
        return getParentKey().getNumber();
    }

    /**
     * Returns the value for the third column of the crossreference stream entry. (This is index of the
     * {@link COSObject} in the containing object stream - for entries of this type.)
     *
     * @return The value for the third column of the crossreference stream entry.
     */
    @Override
    public long getThirdColumnValue()
    {
        return getObjectStreamIndex();
    }

    /**
     * Returns a string representation of this crossreference stream entry.
     *
     * @return A string representation of this crossreference stream entry.
     */
    @Override
    public String toString()
    {
        return "ObjectStreamEntry{" + " key=" + key + ", type=" + getType().getNumericValue()
                + ", objectStreamIndex=" + objectStreamIndex + ", parent=" + parentKey + " }";
    }
}

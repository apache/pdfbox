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
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.cos.COSObjectKey;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdfparser.PDFXRefStream;

/**
 * A class representing a normal reference in a PDF's crossreference stream ({@link PDFXRefStream}).
 * 
 * @author Christian Appl
 */
public class NormalXReference extends AbstractXReference
{

    private final long byteOffset;
    private final COSObjectKey key;
    private final COSBase object;
    private final boolean objectStream;

    /**
     * Prepares a normal reference for the given {@link COSObject} in a PDF's crossreference stream
     * ({@link PDFXRefStream}).
     *
     * @param byteOffset The byte offset of the {@link COSObject} in the PDF file.
     * @param key The {@link COSObjectKey}, that is represented by this entry.
     * @param object The {@link COSObject}, that is represented by this entry.
     */
    public NormalXReference(long byteOffset, COSObjectKey key, COSBase object)
    {
        super(XReferenceType.NORMAL);
        this.byteOffset = byteOffset;
        this.key = key;
        this.object = object;
        COSBase base = object instanceof COSObject ? ((COSObject) object).getObject() : object;
        if (base instanceof COSStream)
        {
            objectStream = COSName.OBJ_STM.equals(((COSStream) base).getCOSName(COSName.TYPE));
        }
        else
        {
            objectStream = false;
        }
    }

    /**
     * Returns the byte offset of the {@link COSObject} in the PDF file.
     *
     * @return The byte offset of the {@link COSObject} in the PDF file.
     */
    public long getByteOffset()
    {
        return byteOffset;
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
     * Returns true, if the referenced object is an object stream.
     *
     * @return True, if the referenced object is an object stream.
     */
    public boolean isObjectStream()
    {
        return objectStream;
    }

    /**
     * Returns the value for the second column of the crossreference stream entry. (This is byte offset of the
     * {@link COSObject} in the PDF file - for entries of this type.)
     *
     * @return The value for the second column of the crossreference stream entry.
     */
    @Override
    public long getSecondColumnValue()
    {
        return getByteOffset();
    }

    /**
     * Returns the value for the third column of the crossreference stream entry. (This is the generation number of the
     * set {@link COSObjectKey} - for entries of this type.)
     *
     * @return The value for the third column of the crossreference stream entry.
     */
    @Override
    public long getThirdColumnValue()
    {
        return getReferencedKey().getGeneration();
    }

    /**
     * Returns a string representation of this crossreference stream entry.
     *
     * @return A string representation of this crossreference stream entry.
     */
    @Override
    public String toString()
    {
        return (isObjectStream() ? "ObjectStreamParent{" : "NormalReference{") + " key=" + key
                + ", type=" + getType().getNumericValue() + ", byteOffset=" + byteOffset + " }";
    }
}

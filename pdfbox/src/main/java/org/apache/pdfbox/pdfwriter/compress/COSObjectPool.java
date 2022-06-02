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

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.cos.COSObjectKey;

import java.util.HashMap;
import java.util.Map;

/**
 * An instance of this class maps {@link COSBase} instances to {@link COSObjectKey}s and allows for a bidirectional
 * lookup.
 * 
 * @author Christian Appl
 */
public class COSObjectPool
{
    private final Map<COSObjectKey, COSBase> keyPool = new HashMap<>();
    private final Map<COSBase, COSObjectKey> objectPool = new HashMap<>();

    private long highestXRefObjectNumber = 0;

    /**
     * Creates a map of {@link COSBase} instances to {@link COSObjectKey}s, allowing bidirectional lookups. This
     * constructor can be used for pre - initialized structures to start the assignment of new object numbers starting
     * from the hereby given offset.
     *
     * @param highestXRefObjectNumber The highest known object number.
     */
    public COSObjectPool(long highestXRefObjectNumber)
    {
        this.highestXRefObjectNumber = Math.max(this.highestXRefObjectNumber,
                highestXRefObjectNumber);
    }

    /**
     * Update the key and object maps.
     *
     * @param key The key, that shall be added.
     * @param object The object, that shall be added.
     * @return The actual key, the object has been added for.
     */
    public COSObjectKey put(COSObjectKey key, COSBase object)
    {
        // to avoid to mixup indirect COSInteger objects holding the same value we have to check
        // if the given key is the same than the key which is stored for the "same" base object wihtin the object pool
        // the same is always true for COSFloat, COSBoolean and COSName and under certain circumstances for the remainig
        // types as well
        if (object == null || (contains(object) && getKey(object).equals(key)))
        {
            return null;
        }
        COSObjectKey actualKey = key;
        if (actualKey == null || contains(actualKey))
        {
            highestXRefObjectNumber++;
            actualKey = new COSObjectKey(highestXRefObjectNumber, 0);
            object.setKey(actualKey);
        }
        else
        {
            highestXRefObjectNumber = Math.max(key.getNumber(), highestXRefObjectNumber);
        }
        keyPool.put(actualKey, object);
        objectPool.put(object, actualKey);
        return actualKey;
    }

    /**
     * Returns the {@link COSObjectKey} for a given registered {@link COSBase}. Returns null if such an object is not
     * registered.
     *
     * @param object The {@link COSBase} a {@link COSObjectKey} shall be determined for.
     * @return key The {@link COSObjectKey}, that matches the registered {@link COSBase}, or null if such an object is
     * not registered.
     */
    public COSObjectKey getKey(COSBase object)
    {
        COSObjectKey key = null;
        if (object instanceof COSObject)
        {
            key = objectPool.get(((COSObject) object).getObject());
        }
        if (key == null)
        {
            return objectPool.get(object);
        }
        return key;
    }

    /**
     * Returns true, if a {@link COSBase} is registered for the given {@link COSObjectKey}.
     *
     * @param key The {@link COSObjectKey} that shall be checked for a registered {@link COSBase}.
     * @return True, if a {@link COSBase} is registered for the given {@link COSObjectKey}.
     */
    public boolean contains(COSObjectKey key)
    {
        return keyPool.containsKey(key);
    }

    /**
     * Returns the {@link COSBase}, that is registered for the given {@link COSObjectKey}, or null if no object is
     * registered for that key.
     *
     * @param key The {@link COSObjectKey} a registered {@link COSBase} shall be found for.
     * @return The {@link COSBase}, that is registered for the given {@link COSObjectKey}, or null if no object is
     * registered for that key.
     */
    public COSBase getObject(COSObjectKey key)
    {
        return keyPool.get(key);
    }

    /**
     * Returns true, if the given {@link COSBase} is a registered object of this pool.
     *
     * @param object The {@link COSBase} that shall be checked.
     * @return True, if such a {@link COSBase} is registered in this pool.
     */
    public boolean contains(COSBase object)
    {
        return (object instanceof COSObject
                && objectPool.containsKey(((COSObject) object).getObject()))
                || objectPool.containsKey(object);
    }

    /**
     * Returns the highest known object number (see: {@link COSObjectKey} for further information), that is currently
     * registered in this pool.
     *
     * @return The highest known object number (see: {@link COSObjectKey} for further information), that is currently
     * registered in this pool.
     */
    public long getHighestXRefObjectNumber()
    {
        return highestXRefObjectNumber;
    }
}

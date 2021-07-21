package org.apache.pdfbox.pdfwriter;

import org.apache.pdfbox.cos.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Bidirectional COSObject-COSObjectKey Storage for COSWriter.
 */
class COSWriterObjectStorage
{
    private final Map<COSBase, COSObjectKey> objectToKeyMap;

    private final Map<COSObjectKey,COSBase> keyToObjectMap;

    private COSDocument document;

    public COSWriterObjectStorage() {
        this.objectToKeyMap = new HashMap<>();
        this.keyToObjectMap = new HashMap<>();
    }

    public void setDocument(COSDocument document) {
        this.document = document;
    }

    public COSBase getObject(COSObjectKey key)
    {
        if(key == null)
        {
            throw new IllegalArgumentException("The key must not be null.");
        }

        if(document != null && document.getXrefTable().containsKey(key))
        {
            COSBase object = document.getObjectFromPool(key);
            return convertToActual(object);
        }

        return keyToObjectMap.get(key);
    }

    public COSObjectKey getKey(COSBase object)
    {
        if(object == null)
        {
            throw new IllegalArgumentException("The object must not be null.");
        }

        if(document != null && object.getReferencedObject() != null)
        {
            COSObject referencedObject = object.getReferencedObject();
            COSObjectKey key = new COSObjectKey(referencedObject);

            if(referencedObject.equals(keyToObjectMap.get(key)))
            {
                return key;
            }
            else if(
                document.getXrefTable().containsKey(key) &&
                document.getObjectFromPool(key).equals(referencedObject)
            )
            {
                return key;
            }

            return null;
        }

        return objectToKeyMap.get(object);
    }

    public void put(COSObjectKey key, COSBase object)
    {
        if(key == null)
        {
            throw new IllegalArgumentException("The key must not be null.");
        }
        if(object == null)
        {
            throw new IllegalArgumentException("The object must not be null.");
        }

        if(document != null && object.getReferencedObject() != null)
        {
            object = object.getReferencedObject();
        }

        objectToKeyMap.put(object, key);
        keyToObjectMap.put(key, object);
    }

    public COSBase convertToActual(COSBase object)
    {
        return object instanceof COSObject ?
            ((COSObject) object).getObjectWithoutCaching() :
            object;
    }
}

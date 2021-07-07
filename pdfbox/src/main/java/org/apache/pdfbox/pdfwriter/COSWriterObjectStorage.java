package org.apache.pdfbox.pdfwriter;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSObjectKey;

import java.util.HashMap;
import java.util.Map;


abstract class COSWriterObjectStorage
{

    private final Map<COSBase, COSObjectKey> objectToKeyMap;

    private final Map<COSObjectKey,COSBase> keyToObjectMap;

    private COSDocument document;

    public COSWriterObjectStorage() {
        this.objectToKeyMap = new HashMap<>();
        this.keyToObjectMap = new HashMap<>();
    }

    public void setDocument(COSDocument document)
    {
        if(document == null)
        {
            throw new IllegalArgumentException("The document must not be null.");
        }

        this.document = document;
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

        objectToKeyMap.put(object, key);
        keyToObjectMap.put(key, object);
    }

    public COSBase getObject(COSObjectKey key)
    {
        if(key == null)
        {
            throw new IllegalArgumentException("The key must not be null.");
        }

        return keyToObjectMap.get(key);
    }

    public COSObjectKey getKey(COSBase object)
    {
        if(object == null)
        {
            throw new IllegalArgumentException("The object must not be null.");
        }

        return objectToKeyMap.get(object);
    }

    protected final COSDocument getDocument()
    {
        return document;
    }

    public abstract COSBase toActual(COSBase object);
}

package org.apache.pdfbox.pdfwriter;

import org.apache.pdfbox.cos.*;


class LazyCOSWriterObjectStorage extends COSWriterObjectStorage
{

    @Override
    public COSBase getObject(COSObjectKey key)
    {
        if(getDocument() == null) {
            return super.getObject(key);
        }

        if(getDocument().getXrefTable().containsKey(key))
        {
            COSBase object = getDocument().getObjectFromPool(key);
            return toActual(object);
        }

        return null;
    }

    @Override
    public COSObjectKey getKey(COSBase object)
    {
        if(getDocument() == null) {
            return super.getKey(object);
        }

        if(object != null && object.getReferencedObject() != null)
        {
            COSObject referencedObject = object.getReferencedObject();
            COSObjectKey key = new COSObjectKey(referencedObject);

            if(referencedObject.equals(super.getObject(key)))
            {
                return key;
            }
            else if(
                getDocument().getXrefTable().containsKey(key) &&
                getDocument().getObjectFromPool(key).equals(referencedObject)
            ) {
                return key;
            }

            return null;
        }

        return super.getKey(object);
    }

    @Override
    public void put(COSObjectKey key, COSBase object)
    {
        if(getDocument() != null && object != null && object.getReferencedObject() != null)
        {
            object = object.getReferencedObject();
        }

        super.put(key, object);
    }

    @Override
    public COSBase toActual(COSBase object)
    {
        return object instanceof COSObject ?
            ((COSObject) object).getObjectWithoutCaching() :
            object;
    }
}

package org.apache.pdfbox.pdfwriter;

import org.apache.pdfbox.cos.*;
import java.util.Set;


class EagerCOSWriterObjectStorage extends COSWriterObjectStorage
{

    @Override
    public void setDocument(COSDocument document)
    {
        super.setDocument(document);

        Set<COSObjectKey> keySet = document.getXrefTable().keySet();

        for (COSObjectKey key : keySet)
        {
            COSBase object = getDocument().getObjectFromPool(key).getObject();

            if (object != null && key != null && !(object instanceof COSNumber))
            {
                // FIXME see PDFBOX-4997: objectKeys is (theoretically) risky because a COSName in
                // different objects would appear only once. Rev 1092855 considered this
                // but only for COSNumber.
                put(key, object);
            }
        }
    }

    @Override
    public COSBase toActual(COSBase object)
    {
        return object instanceof COSObject ?
            ((COSObject) object).getObject() : object;
    }
}

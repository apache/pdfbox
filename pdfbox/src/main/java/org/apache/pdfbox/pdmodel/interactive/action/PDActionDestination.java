package org.apache.pdfbox.pdmodel.interactive.action;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageDestination;

import java.io.IOException;

public class PDActionDestination extends PDAction{


    public PDActionDestination() {
    }

    public PDActionDestination(COSDictionary a) {
        super(a);
    }

    /**
     * This will get the destination to jump to.
     *
     * @return The D entry of the specific go-to action dictionary.
     *
     * @throws IOException If there is an error creating the destination.
     */
    public PDDestination getDestination() throws IOException
    {
        return PDDestination.create(getCOSObject().getDictionaryObject(COSName.D));
    }

    /**
     * This will set the destination to jump to.
     *
     * @param d The destination.
     *
     * @throws IllegalArgumentException if the destination is not a page dictionary object.
     */
    public void setDestination( PDDestination d , String subtype )
    {
        if (d instanceof PDPageDestination)
        {
            PDPageDestination pageDest = (PDPageDestination) d;
            COSArray destArray = pageDest.getCOSObject();
            if (destArray.size() >= 1)
            {
                COSBase page = destArray.getObject(0);
                if (!(page instanceof COSDictionary))
                {
                    throw new IllegalArgumentException("Destination of a " + subtype +" action must be "
                            + "a page dictionary object");
                }
            }
        }
        getCOSObject().setItem(COSName.D, d);
    }
}

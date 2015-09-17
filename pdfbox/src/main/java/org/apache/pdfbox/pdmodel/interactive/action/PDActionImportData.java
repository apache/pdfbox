package org.apache.pdfbox.pdmodel.interactive.action;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.pdmodel.common.filespecification.PDFileSpecification;

import java.io.IOException;
import org.apache.pdfbox.cos.COSName;

/**
 * @author Timur Kamalov
 */
public class PDActionImportData extends PDAction
{

    /**
     * This type of action this object represents.
     */
    public static final String SUB_TYPE = "ImportData";

    /**
     * Default constructor.
     */
    public PDActionImportData()
    {
        action = new COSDictionary();
        setSubType(SUB_TYPE);
    }

    /**
     * Constructor.
     *
     * @param a The action dictionary.
     */
    public PDActionImportData(COSDictionary a)
    {
        super(a);
    }

    /**
     * This will get the file in which the destination is located.
     *
     * @return The F entry of the specific Submit-From action dictionary.
     * @throws IOException If there is an error creating the file spec.
     */
    public PDFileSpecification getFile() throws IOException
    {
        return PDFileSpecification.createFS(action.getDictionaryObject(COSName.F));
    }

    /**
     * This will set the file in which the destination is located.
     *
     * @param fs The file specification.
     */
    public void setFile(PDFileSpecification fs)
    {
        action.setItem(COSName.F, fs);
    }

}

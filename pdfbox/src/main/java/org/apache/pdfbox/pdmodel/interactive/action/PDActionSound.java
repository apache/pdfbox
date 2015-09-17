package org.apache.pdfbox.pdmodel.interactive.action;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;

/**
 * This represents a Sound action that can be executed in a PDF document
 *
 * @author Timur Kamalov
 */
public class PDActionSound extends PDAction
{

    /**
     * This type of action this object represents.
     */
    public static final String SUB_TYPE = "Sound";

    /**
     * Default constructor.
     */
    public PDActionSound()
    {
        action = new COSDictionary();
        setSubType(SUB_TYPE);
    }

    /**
     * Constructor.
     *
     * @param a The action dictionary.
     */
    public PDActionSound(COSDictionary a)
    {
        super(a);
    }

    /**
     * This will get the type of action that the actions dictionary describes. It must be Sound for
     * a Sound action.
     *
     * @return The S entry of the specific Sound action dictionary.
     */
    public String getS()
    {
        return action.getNameAsString(COSName.S);
    }

    /**
     * This will set the type of action that the actions dictionary describes. It must be Sound for
     * a Sound action.
     *
     * @param s The Sound action.
     */
    public void setS(String s)
    {
        action.setName(COSName.S, s);
    }

}

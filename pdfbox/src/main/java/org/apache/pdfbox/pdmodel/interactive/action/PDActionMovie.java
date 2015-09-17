package org.apache.pdfbox.pdmodel.interactive.action;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;

/**
 * @author Timur Kamalov
 */
public class PDActionMovie extends PDAction
{

    /**
     * This type of action this object represents.
     */
    public static final String SUB_TYPE = "Movie";

    /**
     * Default constructor.
     */
    public PDActionMovie()
    {
        action = new COSDictionary();
        setSubType(SUB_TYPE);
    }

    /**
     * Constructor.
     *
     * @param a The action dictionary.
     */
    public PDActionMovie(COSDictionary a)
    {
        super(a);
    }

    /**
     * This will get the type of action that the actions dictionary describes. It must be Movie for
     * a Movie action.
     *
     * @return The S entry of the specific Movie action dictionary.
     */
    public String getS()
    {
        return action.getNameAsString(COSName.S);
    }

    /**
     * This will set the type of action that the actions dictionary describes. It must be Movie for
     * a Movie action.
     *
     * @param s The Movie action.
     */
    public void setS(String s)
    {
        action.setName(COSName.S, s);
    }

}

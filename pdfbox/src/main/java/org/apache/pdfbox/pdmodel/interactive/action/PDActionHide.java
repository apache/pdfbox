package org.apache.pdfbox.pdmodel.interactive.action;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSBoolean;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;

/**
 * This represents a thread action that can be executed in a PDF document.
 *
 * @author Evgeniy Muravitskiy
 */
public class PDActionHide extends PDAction
{

    /**
     * This type of action this object represents.
     */
    public static final String SUB_TYPE = "Hide";

    /**
     * Default Constructor
     */
    public PDActionHide()
    {
        setSubType(SUB_TYPE);
    }

    /**
     * Constructor
     *
     * @param a the action dictionary
     */
    public PDActionHide(COSDictionary a)
    {
        super(a);
    }

    /**
     * The annotation or annotations to be hidden or shown
     *
     * @return The T entry of the specific thread action dictionary.
     */
    public COSBase getT()
    {
        // Dictionary, String or Array
        return this.action.getDictionaryObject(COSName.T);
    }

    /**
     * @param t annotation or annotations
     */
    public void setT(COSBase t)
    {
        this.action.setItem(COSName.T, t);
    }

    /**
     * A flag indicating whether to hide the annotation or show it
     *
     * @return true if annotation is hidden
     */
    public boolean getH()
    {
        return this.action.getBoolean(COSName.H, true);
    }

    /**
     * @param h hide flag
     */
    public void setH(boolean h)
    {
        this.action.setItem(COSName.H, COSBoolean.getBoolean(h));
    }

}

package org.apache.pdfbox.pdmodel.interactive.action;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;

/**
 * @author Timur Kamalov
 */
public class PDActionResetForm extends PDAction
{

    /**
     * This type of action this object represents.
     */
    public static final String SUB_TYPE = "ResetForm";

    /**
     * Default constructor.
     */
    public PDActionResetForm()
    {
        action = new COSDictionary();
        setSubType(SUB_TYPE);
    }

    /**
     * Constructor.
     *
     * @param a The action dictionary.
     */
    public PDActionResetForm(COSDictionary a)
    {
        super(a);
    }

    /**
     * An array identifying which fields to include in the submission or which to exclude, depending
     * on the setting of the Include/Exclude flag in the Flags entry
     *
     * @return the array of fields
     */
    public COSArray getFields()
    {
        COSBase retval = this.action.getDictionaryObject(COSName.FIELDS);
        return retval instanceof COSArray ? (COSArray) retval : null;
    }

    /**
     * @param array the array of fields
     */
    public void setFields(COSArray array)
    {
        this.action.setItem(COSName.FIELDS, array);
    }

    /**
     * A set of flags specifying various characteristics of the action
     *
     * @return the flags
     */
    public int getFlags()
    {
        return this.action.getInt(COSName.FLAGS, 0);
    }

    /**
     * @param flags the flags
     */
    public void setFlags(int flags)
    {
        this.action.setInt(COSName.FLAGS, flags);
    }

}

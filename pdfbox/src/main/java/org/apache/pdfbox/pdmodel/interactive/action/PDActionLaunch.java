/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pdfbox.pdmodel.interactive.action;

import java.io.IOException;
import org.apache.pdfbox.cos.COSBoolean;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;

import org.apache.pdfbox.pdmodel.common.filespecification.PDFileSpecification;

/**
 * This represents a launch action that can be executed in a PDF document.
 *
 * @author Ben Litchfield
 * @author Panagiotis Toumasis
 */
public class PDActionLaunch extends PDAction
{

    /**
     * This type of action this object represents.
     */
    public static final String SUB_TYPE = "Launch";

    /**
     * Default constructor.
     */
    public PDActionLaunch()
    {
        setSubType( SUB_TYPE );
    }

    /**
     * Constructor.
     *
     * @param a The action dictionary.
     */
    public PDActionLaunch( COSDictionary a )
    {
        super( a );
    }

    /**
     * This will get the application to be launched or the document
     * to be opened or printed. It is required if none of the entries
     * Win, Mac or Unix is present. If this entry is absent and the
     * viewer application does not understand any of the alternative
     * entries it should do nothing.
     *
     * @return The F entry of the specific launch action dictionary.
     *
     * @throws IOException If there is an error creating the file spec.
     */
    public PDFileSpecification getFile() throws IOException
    {
        return PDFileSpecification.createFS(getCOSObject().getDictionaryObject(COSName.F));
    }

    /**
     * This will set the application to be launched or the document
     * to be opened or printed. It is required if none of the entries
     * Win, Mac or Unix is present. If this entry is absent and the
     * viewer application does not understand any of the alternative
     * entries it should do nothing.
     *
     * @param fs The file specification.
     */
    public void setFile( PDFileSpecification fs )
    {
        getCOSObject().setItem(COSName.F, fs);
    }

    /**
     * This will get a dictionary containing Windows-specific launch parameters.
     *
     * @return The Win entry of of the specific launch action dictionary.
     */
    public PDWindowsLaunchParams getWinLaunchParams()
    {
        COSDictionary win = action.getCOSDictionary(COSName.WIN);
        return win != null ? new PDWindowsLaunchParams(win) : null;
    }

    /**
     * This will set a dictionary containing Windows-specific launch parameters.
     *
     * @param win The action to be performed.
     */
    public void setWinLaunchParams( PDWindowsLaunchParams win )
    {
        action.setItem(COSName.WIN, win);
    }

    /**
     * This will get the file name to be launched or the document to be opened
     * or printed, in standard Windows pathname format. If the name string includes
     * a backslash character (\), the backslash must itself be preceded by a backslash.
     * This value must be a single string; it is not a file specification.
     *
     * @return The F entry of the specific Windows launch parameter dictionary.
     */
    public String getF()
    {
        return action.getString(COSName.F);
    }

    /**
     * This will set the file name to be launched or the document to be opened
     * or printed, in standard Windows pathname format. If the name string includes
     * a backslash character (\), the backslash must itself be preceded by a backslash.
     * This value must be a single string; it is not a file specification.
     *
     * @param f The file name to be launched.
     */
    public void setF( String f )
    {
        action.setString(COSName.F, f );
    }

    /**
     * This will get the string specifying the default directory in standard DOS syntax.
     *
     * @return The D entry of the specific Windows launch parameter dictionary.
     */
    public String getD()
    {
        return action.getString(COSName.D);
    }

    /**
     * This will set the string specifying the default directory in standard DOS syntax.
     *
     * @param d The default directory.
     */
    public void setD( String d )
    {
        action.setString(COSName.D, d );
    }

    /**
     * This will get the string specifying the operation to perform:
     * open to open a document
     * print to print a document
     * If the F entry designates an application instead of a document, this entry
     * is ignored and the application is launched. Default value: open.
     *
     * @return The O entry of the specific Windows launch parameter dictionary.
     */
    public String getO()
    {
        return action.getString(COSName.O);
    }

    /**
     * This will set the string specifying the operation to perform:
     * open to open a document
     * print to print a document
     * If the F entry designates an application instead of a document, this entry
     * is ignored and the application is launched. Default value: open.
     *
     * @param o The operation to perform.
     */
    public void setO( String o )
    {
        action.setString(COSName.O, o );
    }

    /**
     * This will get a parameter string to be passed to the application designated by the F entry.
     * This entry should be omitted if F designates a document.
     *
     * @return The P entry of the specific Windows launch parameter dictionary.
     */
    public String getP()
    {
        return action.getString(COSName.P);
    }

    /**
     * This will set a parameter string to be passed to the application designated by the F entry.
     * This entry should be omitted if F designates a document.
     *
     * @param p The parameter string.
     */
    public void setP( String p )
    {
        action.setString(COSName.P, p );
    }

    /**
     * This will specify whether to open the destination document in a new window, in the same
     * window, or behave in accordance with the current user preference.
     *
     * @return A flag specifying how to open the destination document.
     */
    public OpenMode getOpenInNewWindow()
    {
        if (getCOSObject().getDictionaryObject(COSName.NEW_WINDOW) instanceof COSBoolean)
        {
            COSBoolean b = (COSBoolean) getCOSObject().getDictionaryObject(COSName.NEW_WINDOW);
            return b.getValue() ? OpenMode.NEW_WINDOW : OpenMode.SAME_WINDOW;
        }
        return OpenMode.USER_PREFERENCE;
    }

    /**
     * This will specify whether to open the destination document in a new window.
     *
     * @param value The flag value.
     */
    public void setOpenInNewWindow(OpenMode value)
    {
        if (null == value)
        {
            getCOSObject().removeItem(COSName.NEW_WINDOW);
            return;
        }
        switch (value)
        {
            case USER_PREFERENCE:
                getCOSObject().removeItem(COSName.NEW_WINDOW);
                break;
            case SAME_WINDOW:
                getCOSObject().setBoolean(COSName.NEW_WINDOW, false);
                break;
            case NEW_WINDOW:
                getCOSObject().setBoolean(COSName.NEW_WINDOW, true);
                break;
            default:
                // shouldn't happen unless the enum type is changed
                break;
        }
    }
}

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
package org.apache.pdfbox.pdmodel.interactive.action.type;

import java.io.IOException;

import org.apache.pdfbox.cos.COSDictionary;

import org.apache.pdfbox.pdmodel.common.filespecification.PDFileSpecification;

/**
 * This represents a launch action that can be executed in a PDF document.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @author Panagiotis Toumasis (ptoumasis@mail.gr)
 * @version $Revision: 1.5 $
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
        super();
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
        return PDFileSpecification.createFS( getCOSDictionary().getDictionaryObject( "F" ) );
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
        getCOSDictionary().setItem( "F", fs );
    }

    /**
     * This will get a dictionary containing Windows-specific launch parameters.
     *
     * @return The Win entry of of the specific launch action dictionary.
     */
    public PDWindowsLaunchParams getWinLaunchParams()
    {
        COSDictionary win = (COSDictionary)action.getDictionaryObject( "Win" );
        PDWindowsLaunchParams retval = null;
        if( win != null )
        {
            retval = new PDWindowsLaunchParams( win );
        }
        return retval;
    }

    /**
     * This will set a dictionary containing Windows-specific launch parameters.
     *
     * @param win The action to be performed.
     */
    public void setWinLaunchParams( PDWindowsLaunchParams win )
    {
        action.setItem( "Win", win );
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
        return action.getString( "F" );
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
        action.setString( "F", f );
    }

    /**
     * This will get the string specifying the default directory in standard DOS syntax.
     *
     * @return The D entry of the specific Windows launch parameter dictionary.
     */
    public String getD()
    {
        return action.getString( "D" );
    }

    /**
     * This will set the string specifying the default directory in standard DOS syntax.
     *
     * @param d The default directory.
     */
    public void setD( String d )
    {
        action.setString( "D", d );
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
        return action.getString( "O" );
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
        action.setString( "O", o );
    }

    /**
     * This will get a parameter string to be passed to the application designated by the F entry.
     * This entry should be omitted if F designates a document.
     *
     * @return The P entry of the specific Windows launch parameter dictionary.
     */
    public String getP()
    {
        return action.getString( "P" );
    }

    /**
     * This will set a parameter string to be passed to the application designated by the F entry.
     * This entry should be omitted if F designates a document.
     *
     * @param p The parameter string.
     */
    public void setP( String p )
    {
        action.setString( "P", p );
    }

    /**
     * This will specify whether to open the destination document in a new window.
     * If this flag is false, the destination document will replace the current
     * document in the same window. If this entry is absent, the viewer application
     * should behave in accordance with the current user preference. This entry is
     * ignored if the file designated by the F entry is not a PDF document.
     *
     * @return A flag specifying whether to open the destination document in a new window.
     */
    public boolean shouldOpenInNewWindow()
    {
        return action.getBoolean( "NewWindow", true );
    }

    /**
     * This will specify the destination document to open in a new window.
     *
     * @param value The flag value.
     */
    public void setOpenInNewWindow( boolean value )
    {
        action.setBoolean( "NewWindow", value );
    }
}

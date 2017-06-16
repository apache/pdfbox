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

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;

/**
 * This class will take a dictionary and determine which type of action to create.
 *
 * @author Ben Litchfield
 * 
 */
public final class PDActionFactory
{
    /**
     * Utility Class.
     */
    private PDActionFactory()
    {
        //utility class
    }

    /**
     * This will create the correct type of action based on the type specified
     * in the dictionary.
     *
     * @param action An action dictionary.
     *
     * @return An action of the correct type.
     */
    public static PDAction createAction( COSDictionary action )
    {
        PDAction retval = null;
        if( action != null)
        {
            String type = action.getNameAsString(COSName.S);
            if (type != null)
            {
                switch (type)
                {
                    case PDActionJavaScript.SUB_TYPE:
                        retval = new PDActionJavaScript(action);
                        break;
                    case PDActionGoTo.SUB_TYPE:
                        retval = new PDActionGoTo(action);
                        break;
                    case PDActionLaunch.SUB_TYPE:
                        retval = new PDActionLaunch(action);
                        break;
                    case PDActionRemoteGoTo.SUB_TYPE:
                        retval = new PDActionRemoteGoTo(action);
                        break;
                    case PDActionURI.SUB_TYPE:
                        retval = new PDActionURI(action);
                        break;
                    case PDActionNamed.SUB_TYPE:
                        retval = new PDActionNamed(action);
                        break;
                    case PDActionSound.SUB_TYPE:
                        retval = new PDActionSound(action);
                        break;
                    case PDActionMovie.SUB_TYPE:
                        retval = new PDActionMovie(action);
                        break;
                    case PDActionImportData.SUB_TYPE:
                        retval = new PDActionImportData(action);
                        break;
                    case PDActionResetForm.SUB_TYPE:
                        retval = new PDActionResetForm(action);
                        break;
                    case PDActionHide.SUB_TYPE:
                        retval = new PDActionHide(action);
                        break;
                    case PDActionSubmitForm.SUB_TYPE:
                        retval = new PDActionSubmitForm(action);
                        break;
                    case PDActionThread.SUB_TYPE:
                        retval = new PDActionThread(action);
                        break;
                    default:
                        break;
                }
            }
        }
        return retval;
    }

}

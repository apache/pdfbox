/*****************************************************************************
 * 
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * 
 ****************************************************************************/

package org.apache.pdfbox.preflight.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.cos.COSObjectKey;
import org.apache.pdfbox.pdmodel.interactive.action.PDAction;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionGoTo;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionHide;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionImportData;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionJavaScript;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionLaunch;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionMovie;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionNamed;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionRemoteGoTo;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionResetForm;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionSound;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionSubmitForm;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionThread;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionURI;
import org.apache.pdfbox.preflight.PreflightConstants;
import org.apache.pdfbox.preflight.PreflightContext;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.preflight.exception.ValidationException;


import static org.apache.pdfbox.preflight.PreflightConstants.ACTION_DICTIONARY_VALUE_ATYPE_NOOP;
import static org.apache.pdfbox.preflight.PreflightConstants.ACTION_DICTIONARY_VALUE_ATYPE_SETSTATE;

public class ActionManagerFactory
{
    /**
     * This method extract actions from the given dictionary. An action is identified by the following entries :
     * <UL>
     * <li>A (Action) : Available in Annotations, Outline items
     * <li>OpenAction (OpenAction) : Available in the Catalog dictionary
     * <li>AA (Additional Action) : Available in the Catalog dictionary, Annotations, Pages
     * </UL>
     * 
     * If there are no action, an empty list is returned.
     * 
     * @param ctx the preflight context.
     * @param dictionary the dictionary to retrieve the actions from.
     * @return the list of actions from the given dictionary.
     * @throws ValidationException
     */
    public final List<AbstractActionManager> getActionManagers(PreflightContext ctx, COSDictionary dictionary)
            throws ValidationException
    {
        List<AbstractActionManager> result = new ArrayList<>(0);
        Map<COSObjectKey, Boolean> alreadyCreated = new HashMap<>();

        COSBase aDict = dictionary.getDictionaryObject(COSName.A);
        if (aDict != null)
        {
            callCreateAction(aDict, ctx, result, alreadyCreated);
        }

        COSBase oaDict = dictionary.getDictionaryObject(COSName.OPEN_ACTION);
        if (oaDict != null && !(oaDict instanceof COSArray))
        {
            callCreateAction(oaDict, ctx, result, alreadyCreated);
        }
        // else nothing to do because an array contains a Destination and not an Action.

        COSDictionary aaDict = dictionary.getCOSDictionary(COSName.AA);
        if (aaDict != null)
        {
            for (COSName name : aaDict.keySet())
            {
                callCreateAction(aaDict.getDictionaryObject(name), ctx, result, name.getName(),
                        alreadyCreated);
            }
        }
        return result;
    }

    /**
     * Call the callCreateAction(COSBase, COSDocument, List<ActionManager>, String) method with null as isAA parameter.
     * 
     * @param aDict
     *            a COSBase object (COSObject or COSDictionary) which represent the action dictionary.
     * @param ctx
     *            the preflight context.
     * @param result
     *            the list of ActionManager to updated if the aDict parameter is valid.
     * @param alreadyCreated
     *            This map is used to know if an Action has already been validated. It is useful to avoid infinite loop
     *            in an action which has a Next entry.
     * @throws ValidationException
     */
    private void callCreateAction(COSBase aDict, PreflightContext ctx, List<AbstractActionManager> result,
            Map<COSObjectKey, Boolean> alreadyCreated) throws ValidationException
    {
        callCreateAction(aDict, ctx, result, null, alreadyCreated);
    }

    /**
     * Call the create action to add the ActionManager to the result list. If the aDict parameter isn't an instance of
     * COSDictionary, this method throws a ValidationException. If the aDict parameter is a reference to a
     * COSDictionary, the action manager is create only if the linked COSObjectKey is missing from the "alreadyCreated"
     * map, in this case the action is added to the map. If the aDict parameter is an instance of COSDictionary, it is
     * impossible to check if the ActionManager already exists in the "alreadyCreated" map.
     * 
     * @param aDict
     *            a COSBase object (COSObject or COSDictionary) which represent the action dictionary.
     * @param ctx
     *            the preflight validation context.
     * @param result
     *            the list of ActionManager to updated if the aDict parameter is valid.
     * @param additionActionKey
     *            the Action identifier if it is an additional action
     * @param alreadyCreated
     *            This map is used to know if an Action has already been validated. It is useful to avoid infinite loop
     *            in an action which has a Next entry.
     * @throws ValidationException
     */
    private void callCreateAction(COSBase aDict, PreflightContext ctx, List<AbstractActionManager> result,
            String additionActionKey, Map<COSObjectKey, Boolean> alreadyCreated) throws ValidationException
    {
        if (aDict instanceof COSDictionary || aDict instanceof COSObject
                && ((COSObject) aDict).getObject() instanceof COSDictionary)
        {
            if (aDict instanceof COSObject)
            {
                COSObject cosObj = (COSObject) aDict;
                COSObjectKey cok = cosObj.getKey();
                COSDictionary indirectDict = (COSDictionary) cosObj.getObject();
                if (!alreadyCreated.containsKey(cok))
                {
                    result.add(createActionManager(ctx, indirectDict, additionActionKey));
                    alreadyCreated.put(cok, true);
                }
            }
            else
            {
                result.add(createActionManager(ctx, (COSDictionary) aDict,
                        additionActionKey));
            }
        }
        else
        {
            ctx.addValidationError(new ValidationError(PreflightConstants.ERROR_ACTION_INVALID_TYPE, "Action entry isn't an instance of COSDictionary"));
        }
    }

    /**
     * Returns all actions contained by the Next entry. If the action dictionary doesn't have Next action, the result is
     * an empty list.
     * 
     * @param ctx the preflight context.
     * @param actionDictionary the dictionary to retrieve the actions from.
     * @return the list of actions from the given dictionary.
     * @throws ValidationException
     */
    public final List<AbstractActionManager> getNextActions(PreflightContext ctx, COSDictionary actionDictionary)
            throws ValidationException
    {
        List<AbstractActionManager> result = new ArrayList<>(0);
        Map<COSObjectKey, Boolean> alreadyCreated = new HashMap<>();

        COSBase nextDict = actionDictionary.getDictionaryObject(COSName.NEXT);
        if (nextDict != null)
        {
            if (nextDict instanceof COSArray)
            {
                COSArray array = (COSArray) nextDict;
                // ---- Next may contains an array of Action dictionary
                for (int i = 0; i < array.size(); ++i)
                {
                    callCreateAction(array.getObject(i), ctx, result, alreadyCreated);
                }
            }
            else
            {
                // ---- Next field contains a Dictionary or a reference to a Dictionary
                callCreateAction(nextDict, ctx, result, alreadyCreated);
            }
        }
        return result;
    }

    /**
     * Create an instance of ActionManager according to the value of the S entry. If the type entry isn't Action, a
     * ValidationException will be thrown.
     * 
     * If the action type isn't authorized in a PDF/A file, an instance of InvalidAction is returned.
     * 
     * @param ctx the preflight context.
     * @param action
     *            the action dictionary used to instantiate the ActionManager
     * @param aaKey
     *            the Action identifier if it is an additional action
     * @return the ActionManager instance.
     * @throws ValidationException
     */
    protected AbstractActionManager createActionManager(PreflightContext ctx, COSDictionary action, String aaKey)
            throws ValidationException
    {

        String type = action.getNameAsString(COSName.TYPE);
        if (type != null && !PDAction.TYPE.equals(type))
        {
            throw new ValidationException("The given dictionary isn't the dictionary of an Action");
        }

        // ---- S is a mandatory fields. If S entry is missing, the return will
        // return the InvalidAction manager
        String s = action.getNameAsString(COSName.S);

        // --- Here is authorized actions
        if (PDActionGoTo.SUB_TYPE.equals(s))
        {
            return new GoToAction(this, action, ctx, aaKey);
        }

        if (PDActionRemoteGoTo.SUB_TYPE.equals(s))
        {
            return new GoToRemoteAction(this, action, ctx, aaKey);
        }

        if (PDActionThread.SUB_TYPE.equals(s))
        {
            return new ThreadAction(this, action, ctx, aaKey);
        }

        if (PDActionURI.SUB_TYPE.equals(s))
        {
            return new UriAction(this, action, ctx, aaKey);
        }

        if (PDActionHide.SUB_TYPE.equals(s))
        {
            return new HideAction(this, action, ctx, aaKey);
        }

        if (PDActionNamed.SUB_TYPE.equals(s))
        {
            return new NamedAction(this, action, ctx, aaKey);
        }

        if (PDActionSubmitForm.SUB_TYPE.equals(s))
        {
            return new SubmitAction(this, action, ctx, aaKey);
        }

        // --- Here is forbidden actions
        if (PDActionLaunch.SUB_TYPE.equals(s) || PDActionSound.SUB_TYPE.equals(s)
                || PDActionMovie.SUB_TYPE.equals(s) || PDActionResetForm.SUB_TYPE.equals(s)
                || PDActionImportData.SUB_TYPE.equals(s)
                || PDActionJavaScript.SUB_TYPE.equals(s)
                || ACTION_DICTIONARY_VALUE_ATYPE_SETSTATE.equals(s)
                || ACTION_DICTIONARY_VALUE_ATYPE_NOOP.equals(s))
        {
            return new InvalidAction(this, action, ctx, aaKey, s);
        }

        // ---- The default ActionManager is the undefined one.
        // Actions defined in a PDF Reference greater than 1.4 will be considered as
        // Undefined actions, here the list of new actions until the PDF 1.6 :
        // # GoToE (1.6) : Not PDF/A, uses EmbeddedFiles.
        // # SetOCGState (1.5) : Not PDF/A, uses optional content.
        // # Rendition (1.5) : Not PDF/A, use multimedia content.
        // # Trans (1.5) : ??
        // # GoTo3DView (1.6) : ??
        return new UndefAction(this, action, ctx, aaKey, s);
    }
}

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

import static org.apache.pdfbox.preflight.PreflightConstants.ACTION_DICTIONARY_KEY_NEXT;
import static org.apache.pdfbox.preflight.PreflightConstants.ACTION_DICTIONARY_VALUE_ATYPE_GOTO;
import static org.apache.pdfbox.preflight.PreflightConstants.ACTION_DICTIONARY_VALUE_ATYPE_GOTOR;
import static org.apache.pdfbox.preflight.PreflightConstants.ACTION_DICTIONARY_VALUE_ATYPE_HIDE;
import static org.apache.pdfbox.preflight.PreflightConstants.ACTION_DICTIONARY_VALUE_ATYPE_IMPORT;
import static org.apache.pdfbox.preflight.PreflightConstants.ACTION_DICTIONARY_VALUE_ATYPE_JAVASCRIPT;
import static org.apache.pdfbox.preflight.PreflightConstants.ACTION_DICTIONARY_VALUE_ATYPE_LAUNCH;
import static org.apache.pdfbox.preflight.PreflightConstants.ACTION_DICTIONARY_VALUE_ATYPE_MOVIE;
import static org.apache.pdfbox.preflight.PreflightConstants.ACTION_DICTIONARY_VALUE_ATYPE_NAMED;
import static org.apache.pdfbox.preflight.PreflightConstants.ACTION_DICTIONARY_VALUE_ATYPE_NOOP;
import static org.apache.pdfbox.preflight.PreflightConstants.ACTION_DICTIONARY_VALUE_ATYPE_RESET;
import static org.apache.pdfbox.preflight.PreflightConstants.ACTION_DICTIONARY_VALUE_ATYPE_SETSTATE;
import static org.apache.pdfbox.preflight.PreflightConstants.ACTION_DICTIONARY_VALUE_ATYPE_SOUND;
import static org.apache.pdfbox.preflight.PreflightConstants.ACTION_DICTIONARY_VALUE_ATYPE_SUBMIT;
import static org.apache.pdfbox.preflight.PreflightConstants.ACTION_DICTIONARY_VALUE_ATYPE_THREAD;
import static org.apache.pdfbox.preflight.PreflightConstants.ACTION_DICTIONARY_VALUE_ATYPE_URI;
import static org.apache.pdfbox.preflight.PreflightConstants.ACTION_DICTIONARY_VALUE_TYPE;
import static org.apache.pdfbox.preflight.PreflightConstants.DICTIONARY_KEY_ADDITIONAL_ACTION;
import static org.apache.pdfbox.preflight.PreflightConstants.DICTIONARY_KEY_OPEN_ACTION;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.persistence.util.COSObjectKey;
import org.apache.pdfbox.preflight.PreflightConstants;
import org.apache.pdfbox.preflight.PreflightContext;
import org.apache.pdfbox.preflight.exception.ValidationException;
import org.apache.pdfbox.preflight.utils.COSUtils;

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
     * @param dictionary
     * @param cDoc
     * @return
     * @throws ValidationException
     */
    public final List<AbstractActionManager> getActionManagers(PreflightContext ctx, COSDictionary dictionary)
            throws ValidationException
    {
        List<AbstractActionManager> result = new ArrayList<AbstractActionManager>(0);
        Map<COSObjectKey, Boolean> alreadyCreated = new HashMap<COSObjectKey, Boolean>();

        COSBase aDict = dictionary.getDictionaryObject(COSName.A);
        if (aDict != null)
        {
            callCreateAction(aDict, ctx, result, alreadyCreated);
        }

        COSDocument cosDocument = ctx.getDocument().getDocument();
        COSBase oaDict = dictionary.getDictionaryObject(DICTIONARY_KEY_OPEN_ACTION);
        if (oaDict != null)
        {
            if (!COSUtils.isArray(oaDict, cosDocument))
            {
                callCreateAction(oaDict, ctx, result, alreadyCreated);
            }
            // else Nothing to do because of an array contains a Destination not an
            // action.
        }

        COSBase aa = dictionary.getDictionaryObject(DICTIONARY_KEY_ADDITIONAL_ACTION);
        if (aa != null)
        {
            COSDictionary aaDict = COSUtils.getAsDictionary(aa, cosDocument);
            if (aaDict != null)
            {
                for (Object key : aaDict.keySet())
                {
                    COSName name = (COSName) key;
                    callCreateAction(aaDict.getItem(name), ctx, result, name.getName(), alreadyCreated);
                }
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
     * COSDictionary, this method throws a ValdiationException. If the aDict parameter is a reference to a
     * COSDicitonary, the action manager is create only if the linked COSObjectKey is missing from the "alreadyCreated"
     * map, in this case the action is added to the map. If the aDict parameter is an instance of COSDIctionary, it is
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
        COSDocument cosDocument = ctx.getDocument().getDocument();
        if (COSUtils.isDictionary(aDict, cosDocument))
        {
            if (aDict instanceof COSObject)
            {
                COSObjectKey cok = new COSObjectKey((COSObject) aDict);
                if (!alreadyCreated.containsKey(cok))
                {
                    result.add(createActionManager(ctx, COSUtils.getAsDictionary(aDict, cosDocument), additionActionKey));
                    alreadyCreated.put(cok, true);
                }
            }
            else
            {
                result.add(createActionManager(ctx, COSUtils.getAsDictionary(aDict, cosDocument), additionActionKey));
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
     * @param actionDictionary
     *            the action dictionary which contains Next entry
     * @param cDoc
     *            the COSDocument which contains actions.
     * @return
     * @throws ValidationException
     */
    public final List<AbstractActionManager> getNextActions(PreflightContext ctx, COSDictionary actionDictionary)
            throws ValidationException
    {
        List<AbstractActionManager> result = new ArrayList<AbstractActionManager>(0);
        Map<COSObjectKey, Boolean> alreadyCreated = new HashMap<COSObjectKey, Boolean>();

        COSBase nextDict = actionDictionary.getDictionaryObject(ACTION_DICTIONARY_KEY_NEXT);
        if (nextDict != null)
        {
            COSDocument cosDocument = ctx.getDocument().getDocument();
            if (COSUtils.isArray(nextDict, cosDocument))
            {
                COSArray array = COSUtils.getAsArray(nextDict, cosDocument);
                // ---- Next may contains an array of Action dictionary
                for (int i = 0; i < array.size(); ++i)
                {
                    callCreateAction(array.get(i), ctx, result, alreadyCreated);
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
     * @param ctx
     * @param action
     *            the action dictionary used to instantiate the ActionManager
     * @param isAA
     *            the Action identifier if it is an additional action
     * @return
     * @throws ValidationException
     */
    protected AbstractActionManager createActionManager(PreflightContext ctx, COSDictionary action, String aaKey)
            throws ValidationException
    {

        String type = action.getNameAsString(COSName.TYPE);
        if (type != null && !ACTION_DICTIONARY_VALUE_TYPE.equals(type))
        {
            throw new ValidationException("The given dictionary isn't the dictionary of an Action");
        }

        // ---- S is a mandatory fields. If S entry is missing, the return will
        // return the InvalidAction manager
        String s = action.getNameAsString(COSName.S);

        // --- Here is authorized actions
        if (ACTION_DICTIONARY_VALUE_ATYPE_GOTO.equals(s))
        {
            return new GoToAction(this, action, ctx, aaKey);
        }

        if (ACTION_DICTIONARY_VALUE_ATYPE_GOTOR.equals(s))
        {
            return new GoToRemoteAction(this, action, ctx, aaKey);
        }

        if (ACTION_DICTIONARY_VALUE_ATYPE_THREAD.equals(s))
        {
            return new ThreadAction(this, action, ctx, aaKey);
        }

        if (ACTION_DICTIONARY_VALUE_ATYPE_URI.equals(s))
        {
            return new UriAction(this, action, ctx, aaKey);
        }

        if (ACTION_DICTIONARY_VALUE_ATYPE_HIDE.equals(s))
        {
            return new HideAction(this, action, ctx, aaKey);
        }

        if (ACTION_DICTIONARY_VALUE_ATYPE_NAMED.equals(s))
        {
            return new NamedAction(this, action, ctx, aaKey);
        }

        if (ACTION_DICTIONARY_VALUE_ATYPE_SUBMIT.equals(s))
        {
            return new SubmitAction(this, action, ctx, aaKey);
        }

        // --- Here is forbidden actions
        if (ACTION_DICTIONARY_VALUE_ATYPE_LAUNCH.equals(s) || ACTION_DICTIONARY_VALUE_ATYPE_SOUND.equals(s)
                || ACTION_DICTIONARY_VALUE_ATYPE_MOVIE.equals(s) || ACTION_DICTIONARY_VALUE_ATYPE_RESET.equals(s)
                || ACTION_DICTIONARY_VALUE_ATYPE_IMPORT.equals(s) || ACTION_DICTIONARY_VALUE_ATYPE_JAVASCRIPT.equals(s)
                || ACTION_DICTIONARY_VALUE_ATYPE_SETSTATE.equals(s) || ACTION_DICTIONARY_VALUE_ATYPE_NOOP.equals(s))
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

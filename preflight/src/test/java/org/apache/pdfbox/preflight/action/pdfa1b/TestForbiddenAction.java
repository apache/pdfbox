/*

 Licensed to the Apache Software Foundation (ASF) under one
 or more contributor license agreements.  See the NOTICE file
 distributed with this work for additional information
 regarding copyright ownership.  The ASF licenses this file
 to you under the Apache License, Version 2.0 (the
 "License"); you may not use this file except in compliance
 with the License.  You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.

 */

package org.apache.pdfbox.preflight.action.pdfa1b;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.preflight.PreflightConstants;
import org.junit.jupiter.api.Test;

class TestForbiddenAction extends AbstractTestAction
{

    protected COSDictionary createAction(final String type)
    {
        final COSDictionary action = new COSDictionary();
        action.setItem(COSName.TYPE, COSName.getPDFName("Action"));
        action.setItem(COSName.S, COSName.getPDFName(type));
        return action;
    }

    @Test
    void testLaunch() throws Exception
    {
        final COSDictionary action = createAction("Launch");
        valid(action, false, PreflightConstants.ERROR_ACTION_FORBIDDEN_ACTIONS_EXPLICITLY_FORBIDDEN);
    }

    @Test
    void testSound() throws Exception
    {
        final COSDictionary action = createAction("Sound");
        valid(action, false, PreflightConstants.ERROR_ACTION_FORBIDDEN_ACTIONS_EXPLICITLY_FORBIDDEN);
    }

    @Test
    void testMovie() throws Exception
    {
        final COSDictionary action = createAction("Movie");
        valid(action, false, PreflightConstants.ERROR_ACTION_FORBIDDEN_ACTIONS_EXPLICITLY_FORBIDDEN);
    }

    @Test
    void testImportData() throws Exception
    {
        final COSDictionary action = createAction("ImportData");
        valid(action, false, PreflightConstants.ERROR_ACTION_FORBIDDEN_ACTIONS_EXPLICITLY_FORBIDDEN);
    }

    @Test
    void testResetForm() throws Exception
    {
        final COSDictionary action = createAction("ResetForm");
        valid(action, false, PreflightConstants.ERROR_ACTION_FORBIDDEN_ACTIONS_EXPLICITLY_FORBIDDEN);
    }

    @Test
    void testJS() throws Exception
    {
        final COSDictionary action = createAction("JavaScript");
        valid(action, false, PreflightConstants.ERROR_ACTION_FORBIDDEN_ACTIONS_EXPLICITLY_FORBIDDEN);
    }
}

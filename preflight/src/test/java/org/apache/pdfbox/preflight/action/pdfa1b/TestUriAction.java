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

import java.util.Arrays;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.interactive.action.PDAction;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionJavaScript;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionURI;
import org.apache.pdfbox.preflight.PreflightConstants;
import org.junit.jupiter.api.Test;

class TestUriAction extends AbstractTestAction
{

    protected PDActionURI createAction()
    {
        final PDActionURI action = new PDActionURI();
        action.setURI("http://www.apache.org");
        return action;
    }

    @Test
    void test() throws Exception
    {
        final PDAction action = createAction();
        valid(action, true);
    }

    @Test
    void testMissingURI() throws Exception
    {
        final PDActionURI action = new PDActionURI();
        valid(action, false, PreflightConstants.ERROR_ACTION_MISING_KEY);
    }

    @Test
    void testInvalidURI() throws Exception
    {
        final PDActionURI action = new PDActionURI();
        action.getCOSObject().setBoolean(COSName.URI, true);
        valid(action, false, PreflightConstants.ERROR_ACTION_INVALID_TYPE);
    }

    @Test
    void testNextValid() throws Exception
    {
        final PDActionURI action = createAction();
        action.setNext(Arrays.asList(createAction()));
        valid(action, true);
    }

    @Test
    void testNextInvalid() throws Exception
    {
        final PDActionURI action = createAction();
        action.setNext(Arrays.asList(new PDActionJavaScript()));
        valid(action, false, PreflightConstants.ERROR_ACTION_FORBIDDEN_ACTIONS_EXPLICITLY_FORBIDDEN);
    }
}

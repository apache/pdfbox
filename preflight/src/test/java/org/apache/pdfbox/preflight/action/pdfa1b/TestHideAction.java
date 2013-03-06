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

package org.apache.pdfbox.preflight.action.pdfa1b;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.preflight.PreflightConstants;
import org.junit.Test;

public class TestHideAction extends AbstractTestAction
{

    protected COSDictionary createHideAction()
    {
        COSDictionary hideAction = new COSDictionary();
        hideAction.setItem(COSName.TYPE, COSName.getPDFName("Action"));
        hideAction.setItem(COSName.S, COSName.getPDFName("Hide"));
        hideAction.setBoolean(COSName.H, false);
        hideAction.setString(COSName.T, "avalue");
        return hideAction;
    }

    @Test
    public void testHideAction() throws Exception
    {
        COSDictionary action = createHideAction();
        valid(action, true);
    }

    @Test
    public void testHideAction_InvalideH() throws Exception
    {
        COSDictionary action = createHideAction();
        action.setBoolean(COSName.H, true);
        valid(action, false, PreflightConstants.ERROR_ACTION_HIDE_H_INVALID);
    }

    @Test
    public void testHideAction_InvalideT() throws Exception
    {
        COSDictionary action = createHideAction();
        action.setBoolean(COSName.T, true);
        valid(action, false, PreflightConstants.ERROR_ACTION_INVALID_TYPE);
    }

    @Test
    public void testHideAction_MissingT() throws Exception
    {
        COSDictionary action = createHideAction();
        action.removeItem(COSName.T);
        valid(action, false, PreflightConstants.ERROR_ACTION_MISING_KEY);
    }
}

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
import org.junit.jupiter.api.Test;

class TestThreadAction extends AbstractTestAction
{

    protected COSDictionary createSubmitAction()
    {
        final COSDictionary action = new COSDictionary();
        action.setItem(COSName.TYPE, COSName.getPDFName("Action"));
        action.setItem(COSName.S, COSName.getPDFName("Thread"));
        action.setInt(COSName.D, 1);
        return action;
    }

    @Test
    void test() throws Exception
    {
        final COSDictionary action = createSubmitAction();
        valid(action, true);
    }

    @Test
    void testMissingD() throws Exception
    {
        final COSDictionary action = createSubmitAction();
        action.removeItem(COSName.D);
        valid(action, false, PreflightConstants.ERROR_ACTION_MISING_KEY);
    }

    @Test
    void testInvalidD() throws Exception
    {
        final COSDictionary action = createSubmitAction();
        action.setBoolean(COSName.D, false);
        valid(action, false, PreflightConstants.ERROR_ACTION_INVALID_TYPE);
    }
}

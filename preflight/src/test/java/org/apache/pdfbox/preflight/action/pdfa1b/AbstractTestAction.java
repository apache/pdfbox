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

import static org.junit.Assert.*;

import java.util.List;

import javax.activation.DataSource;
import javax.activation.FileDataSource;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.action.type.PDAction;
import org.apache.pdfbox.preflight.Format;
import org.apache.pdfbox.preflight.PreflightContext;
import org.apache.pdfbox.preflight.PreflightDocument;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.preflight.action.AbstractActionManager;
import org.apache.pdfbox.preflight.action.ActionManagerFactory;

public abstract class AbstractTestAction
{

    /**
     * Read a simple PDF/A to create a valid Context
     * 
     * @return
     * @throws Exception
     */
    protected PreflightContext createContext() throws Exception
    {
        DataSource ds = new FileDataSource("src/test/resources/pdfa-with-annotations-square.pdf");
        PDDocument doc = PDDocument.load(ds.getInputStream());
        PreflightDocument preflightDocument = new PreflightDocument(doc.getDocument(), Format.PDF_A1B);
        PreflightContext ctx = new PreflightContext(ds);
        ctx.setDocument(preflightDocument);
        preflightDocument.setContext(ctx);
        return ctx;
    }

    /**
     * Run the Action validation and check the result.
     * 
     * @param action
     *            action to check
     * @param valid
     *            true if the Action must be valid, false if the action contains mistakes
     * @throws Exception
     */
    protected void valid(PDAction action, boolean valid) throws Exception
    {
        valid(action, valid, null);
    }

    protected void valid(COSDictionary action, boolean valid) throws Exception
    {
        valid(action, valid, null);
    }

    /**
     * Run the Action validation and check the result.
     * 
     * @param action
     *            action to check
     * @param valid
     *            true if the Action must be valid, false if the action contains mistakes
     * @param expectedCode
     *            the expected error code (can be null)
     * @throws Exception
     */
    protected void valid(PDAction action, boolean valid, String expectedCode) throws Exception
    {
        valid(action.getCOSDictionary(), valid, expectedCode);
    }

    protected void valid(COSDictionary action, boolean valid, String expectedCode) throws Exception
    {
        ActionManagerFactory fact = new ActionManagerFactory();
        PreflightContext ctx = createContext();
        COSDictionary dict = new COSDictionary();
        dict.setItem(COSName.A, action);

        // process the action validation
        List<AbstractActionManager> actions = fact.getActionManagers(ctx, dict);
        for (AbstractActionManager abstractActionManager : actions)
        {
            abstractActionManager.valid();
        }

        // check the result
        if (!valid)
        {
            List<ValidationError> errors = ctx.getDocument().getResult().getErrorsList();
            assertFalse(errors.isEmpty());
            if (expectedCode != null || !"".equals(expectedCode))
            {
                boolean found = false;
                for (ValidationError err : errors)
                {
                    if (err.getErrorCode().equals(expectedCode))
                    {
                        found = true;
                        break;
                    }
                }
                assertTrue(found);
            }
        }
        else
        {
            if (ctx.getDocument().getResult() != null)
            {
                List<ValidationError> errors = ctx.getDocument().getResult().getErrorsList();
                assertTrue(errors.isEmpty());
            }
        }
    }
}

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
package org.apache.pdfbox.preflight;

import org.apache.pdfbox.preflight.exception.ValidationException;
import org.apache.pdfbox.preflight.process.BookmarkValidationProcess;
import org.apache.pdfbox.preflight.process.EmptyValidationProcess;
import org.apache.pdfbox.preflight.process.ValidationProcess;
import org.apache.pdfbox.preflight.process.reflect.ResourcesValidationProcess;
import org.junit.Assert;
import org.junit.Test;

public class TestPreflightConfiguration
{

    @Test
    public void testGetValidationProcess() throws Exception {
        PreflightConfiguration confg = PreflightConfiguration.createPdfA1BConfiguration();
        ValidationProcess vp = confg.getInstanceOfProcess(PreflightConfiguration.BOOKMARK_PROCESS);
        Assert.assertNotNull(vp);
        Assert.assertTrue(vp instanceof BookmarkValidationProcess);
    }
    
    @Test
    public void testGetValidationPageProcess() throws Exception {
        PreflightConfiguration confg = PreflightConfiguration.createPdfA1BConfiguration();
        ValidationProcess vp = confg.getInstanceOfProcess(PreflightConfiguration.RESOURCES_PROCESS);
        Assert.assertNotNull(vp);
        Assert.assertTrue(vp instanceof ResourcesValidationProcess);
    }
    
    @Test
    public void testGetValidationProcess_noError() throws Exception {
        PreflightConfiguration confg = PreflightConfiguration.createPdfA1BConfiguration();
        confg.setErrorOnMissingProcess(false);
        confg.removeProcess(PreflightConfiguration.BOOKMARK_PROCESS);
        ValidationProcess vp = confg.getInstanceOfProcess(PreflightConfiguration.BOOKMARK_PROCESS);
        Assert.assertNotNull(vp);
        Assert.assertTrue(vp instanceof EmptyValidationProcess);
    }
    
    @Test
    public void testGetValidationPageProcess_noError() throws Exception {
        PreflightConfiguration confg = PreflightConfiguration.createPdfA1BConfiguration();
        confg.setErrorOnMissingProcess(false);
        confg.removePageProcess(PreflightConfiguration.RESOURCES_PROCESS);
        ValidationProcess vp = confg.getInstanceOfProcess(PreflightConfiguration.RESOURCES_PROCESS);
        Assert.assertNotNull(vp);
        Assert.assertTrue(vp instanceof EmptyValidationProcess);
    }

    @Test(expected=ValidationException.class)
    public void testGetMissingValidationProcess() throws Exception {
        PreflightConfiguration confg = PreflightConfiguration.createPdfA1BConfiguration();
        confg.removeProcess(PreflightConfiguration.BOOKMARK_PROCESS);
        confg.getInstanceOfProcess(PreflightConfiguration.BOOKMARK_PROCESS);
        Assert.fail();
    }

    @Test(expected=ValidationException.class)
    public void testGetMissingValidationPageProcess() throws Exception {
        PreflightConfiguration confg = PreflightConfiguration.createPdfA1BConfiguration();
        confg.removePageProcess(PreflightConfiguration.RESOURCES_PROCESS);
        confg.getInstanceOfProcess(PreflightConfiguration.RESOURCES_PROCESS);
        Assert.fail();
    }
    
    @Test(expected=ValidationException.class)
    public void testGetMissingValidationProcess2() throws Exception {
        PreflightConfiguration confg = PreflightConfiguration.createPdfA1BConfiguration();
        confg.replaceProcess(PreflightConfiguration.BOOKMARK_PROCESS, null);
        confg.getInstanceOfProcess(PreflightConfiguration.BOOKMARK_PROCESS);
        Assert.fail();
    }

    @Test(expected=ValidationException.class)
    public void testGetMissingValidationPageProcess2() throws Exception {
        PreflightConfiguration confg = PreflightConfiguration.createPdfA1BConfiguration();
        confg.replacePageProcess(PreflightConfiguration.RESOURCES_PROCESS, null);
        confg.getInstanceOfProcess(PreflightConfiguration.RESOURCES_PROCESS);
        Assert.fail();
    }
}

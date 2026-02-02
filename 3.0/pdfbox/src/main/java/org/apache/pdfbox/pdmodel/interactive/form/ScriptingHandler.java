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
package org.apache.pdfbox.pdmodel.interactive.form;

import org.apache.pdfbox.pdmodel.interactive.action.PDActionJavaScript;

public interface ScriptingHandler
{
    /**
     * Handle the fields keyboard event action.
     * 
     * @param javaScriptAction the keyboard event action script
     * @param value the current field value
     * @return the resulting field value
     */
    String keyboard(PDActionJavaScript javaScriptAction, String value);

    /**
     * Handle the fields format event action.
     * 
     * @param javaScriptAction the format event action script
     * @param value the current field value
     * @return the formatted field value
     */
    String format(PDActionJavaScript javaScriptAction, String value);

    /**
     * Handle the fields validate event action.
     * 
     * @param javaScriptAction the validate event action script
     * @param value the current field value
     * @return the result of the validity check
     */
    boolean validate(PDActionJavaScript javaScriptAction, String value);

    /**
     * Handle the fields calculate event action.
     * 
     * @param javaScriptAction the calculate event action script
     * @param value the current field value
     * @return the result of the field calculation
     */    
    String calculate(PDActionJavaScript javaScriptAction, String value);
}

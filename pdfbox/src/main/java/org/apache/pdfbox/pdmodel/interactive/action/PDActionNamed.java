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

/**
 * This represents a named action in a PDF document.
 */
public class PDActionNamed extends PDAction
{
    /**
     * This type of action this object represents.
     */
    public static final String SUB_TYPE = "Named";

    /**
     * Default constructor.
     */
    public PDActionNamed()
    {
        action = new COSDictionary();
        setSubType(SUB_TYPE);
    }

    /**
     * Constructor.
     *
     * @param a The action dictionary.
     */
    public PDActionNamed(COSDictionary a)
    {
        super(a);
    }

    /**
     * This will get the name of the action to be performed.
     *
     * @return The name of the action to be performed.
     */
    public String getN()
    {
        return action.getNameAsString("N");
    }

    /**
     * This will set the name of the action to be performed.
     *
     * @param name The name of the action to be performed.
     */
    public void setN(String name)
    {
        action.setName("N", name);
    }
}

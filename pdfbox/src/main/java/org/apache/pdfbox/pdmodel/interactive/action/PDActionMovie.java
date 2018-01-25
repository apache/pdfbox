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
import org.apache.pdfbox.cos.COSName;

/**
 * @author Timur Kamalov
 */
public class PDActionMovie extends PDAction
{

    /**
     * This type of action this object represents.
     */
    public static final String SUB_TYPE = "Movie";

    /**
     * Default constructor.
     */
    public PDActionMovie()
    {
        setSubType(SUB_TYPE);
    }

    /**
     * Constructor.
     *
     * @param a The action dictionary.
     */
    public PDActionMovie(COSDictionary a)
    {
        super(a);
    }

    /**
     * This will get the type of action that the actions dictionary describes. It must be Movie for
     * a Movie action.
     *
     * @return The S entry of the specific Movie action dictionary.
     * @deprecated use {@link #getSubType() }.
     */
    @Deprecated
    public String getS()
    {
        return action.getNameAsString(COSName.S);
    }

    /**
     * This will set the type of action that the actions dictionary describes. It must be Movie for
     * a Movie action.
     *
     * @param s The Movie action.
     * @deprecated use {@link #setSubType(java.lang.String) }.
     */
    @Deprecated
    public void setS(String s)
    {
        action.setName(COSName.S, s);
    }

}

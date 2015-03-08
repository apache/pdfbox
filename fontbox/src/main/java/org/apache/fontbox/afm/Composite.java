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
package org.apache.fontbox.afm;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents composite character data.
 *
 * @author Ben Litchfield
 */
public class Composite
{
    private String name;
    private List<CompositePart> parts = new ArrayList<CompositePart>();

    /** Getter for property name.
     * @return Value of property name.
     */
    public String getName()
    {
        return name;
    }

    /** Setter for property name.
     * @param nameValue New value of property name.
     */
    public void setName(String nameValue)
    {
        this.name = nameValue;
    }

    /**
     * This will add a composite part.
     *
     * @param part The composite part to add.
     */
    public void addPart( CompositePart part )
    {
        parts.add( part );
    }

    /** Getter for property parts.
     * @return Value of property parts.
     */
    public List<CompositePart> getParts()
    {
        return parts;
    }

    /** Setter for property parts.
     * @param partsList New value of property parts.
     */
    public void setParts(List<CompositePart> partsList)
    {
        this.parts = partsList;
    }

}
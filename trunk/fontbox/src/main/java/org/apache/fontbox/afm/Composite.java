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
import java.util.Collections;
import java.util.List;

/**
 * This class represents composite character data.
 *
 * @author Ben Litchfield
 */
public class Composite
{
    private final String name;
    private final List<CompositePart> parts = new ArrayList<>();

    public Composite(String name)
    {
        this.name = name;
    }

    /** Getter for property name.
     * @return Value of property name.
     */
    public String getName()
    {
        return name;
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
        return Collections.unmodifiableList(parts);
    }
}
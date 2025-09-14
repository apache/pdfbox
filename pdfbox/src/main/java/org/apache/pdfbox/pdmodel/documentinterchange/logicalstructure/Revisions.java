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
package org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * 
 * @author Johannes Koch

 * @param <T> the type of object to store the revision numbers with
 */
public class Revisions<T>
{
    private final List<T> objects;
    private final List<Integer> revisionNumbers;

    /**
     * Constructor.
     */
    public Revisions()
    {
        objects = new ArrayList<>();
        revisionNumbers = new ArrayList<>();
    }

    /**
     * Returns the object at the specified position.
     * 
     * @param index the position
     * @return the object
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    public T getObject(int index)
    {
        return objects.get(index);
    }

    /**
     * Returns the revision number at the specified position.
     * 
     * @param index the position
     * @return the revision number
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    public int getRevisionNumber(int index)
    {
        return revisionNumbers.get(index);
    }

    /**
     * Adds an object with a specified revision number.
     * 
     * @param object the object
     * @param revisionNumber the revision number
     */
    public void addObject(T object, int revisionNumber)
    {
        objects.add(object);
        revisionNumbers.add(revisionNumber);
    }

    /**
     * Sets the revision number of a specified object.
     * 
     * @param object the object
     * @param revisionNumber the revision number
     */
    protected void setRevisionNumber(T object, int revisionNumber)
    {
        int index = objects.indexOf(object);
        if (index > -1)
        {
            revisionNumbers.set(index, revisionNumber);
        }
    }

    /**
     * Returns the size.
     * 
     * @return the size
     */
    public int size()
    {
        return objects.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        StringJoiner sj = new StringJoiner("; ", "{", "}");
        for (int i = 0; i < objects.size(); i++)
        {
            sj.add("object=" + objects.get(i) + ", revisionNumber=" + getRevisionNumber(i));
        }
        return sj.toString();
    }

}

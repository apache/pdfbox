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
package org.apache.pdfbox.cos;

/**
 * Object representing the physical reference to an indirect pdf object.
 *
 * @author Michael Traut
 * 
 */
public class COSObjectKey implements Comparable<COSObjectKey>
{
    private final long number;
    private int generation;
    
    /**
     * Constructor.
     *
     * @param object The object that this key will represent.
     */
    public COSObjectKey(COSObject object)
    {
        this(object.getObjectNumber(), object.getGenerationNumber());
    }

    /**
     * Constructor.
     *
     * @param num The object number.
     * @param gen The object generation number.
     */
    public COSObjectKey(long num, int gen)
    {
        number = num;
        generation = gen;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj)
    {
        COSObjectKey objToBeCompared = obj instanceof COSObjectKey ? (COSObjectKey)obj : null;
        return objToBeCompared != null &&
                objToBeCompared.getNumber() == getNumber() &&
                objToBeCompared.getGeneration() == getGeneration();
    }

    /**
     * This will get the generation number.
     *
     * @return The objects generation number.
     */
    public int getGeneration()
    {
        return generation;
    }

    /**
     * This will set the generation number. It is intended for fixes only.
     * 
     * @param genNumber the new generation number.
     * 
     * @deprecated will be removed in the next major release
     */
    public void fixGeneration(int genNumber)
    {
        generation = genNumber;
    }

    /**
     * This will get the objects id.
     *
     * @return The object's id.
     */
    public long getNumber()
    {
        return number;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        // most likely generation is 0. Shift number 4 times (fast as multiply)
        // to support generation numbers up to 15
        return Long.valueOf((number << 4) + generation).hashCode();
    }

    @Override
    public String toString()
    {
        return number + " " + generation + " R";
    }

    @Override
    public int compareTo(COSObjectKey other)
    {
        if (getNumber() < other.getNumber())
        {
            return -1;
        }
        else if (getNumber() > other.getNumber())
        {
            return 1;
        }
        else
        {
            if (getGeneration() < other.getGeneration())
            {
                return -1;
            }
            else if (getGeneration() > other.getGeneration())
            {
                return 1;
            }
            else
            {
                return 0;
            }
        }
    }

}

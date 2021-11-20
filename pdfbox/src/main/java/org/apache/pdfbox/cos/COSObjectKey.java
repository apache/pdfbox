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
public final class COSObjectKey implements Comparable<COSObjectKey>
{
    private static final int NUMBER_OFFSET = Short.SIZE;
    private static final long GENERATION_MASK = (long) Math.pow(2, NUMBER_OFFSET) - 1;
    // combined number and generation
    // The lowest 16 bits hold the generation 0-65535
    // The rest is used for the number (even though 34 bit are sufficient for 10 digits)
    private final long numberAndGeneration;
    
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
        if (num < 0)
        {
            throw new IllegalArgumentException("Object number must not be a negative value");
        }
        if (gen < 0)
        {
            throw new IllegalArgumentException("Generation number must not be a negative value");
        }
        numberAndGeneration = num << NUMBER_OFFSET | (gen & GENERATION_MASK);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj)
    {
        COSObjectKey objToBeCompared = obj instanceof COSObjectKey ? (COSObjectKey)obj : null;
        return objToBeCompared != null
                && objToBeCompared.numberAndGeneration == numberAndGeneration;
    }

    /**
     * This will get the object generation number.
     *
     * @return The object generation number.
     */
    public int getGeneration()
    {
        return (int) (numberAndGeneration & GENERATION_MASK);
    }

    /**
     * This will get the object number.
     *
     * @return The object number.
     */
    public long getNumber()
    {
        return numberAndGeneration >>> NUMBER_OFFSET;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        return Long.hashCode(numberAndGeneration);
    }

    @Override
    public String toString()
    {
        return getNumber() + " " + getGeneration() + " R";
    }

    @Override
    public int compareTo(COSObjectKey other)
    {
        return Long.compare(numberAndGeneration, other.numberAndGeneration);
    }

}

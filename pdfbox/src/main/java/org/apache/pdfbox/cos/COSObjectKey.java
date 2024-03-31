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
    // index within a compressed object stream if applicable otherwise -1
    private final int streamIndex;
    
    /**
     * Constructor.
     *
     * @param num The object number.
     * @param gen The object generation number.
     */
    public COSObjectKey(long num, int gen)
    {
        this(num, gen, -1);
    }

    /**
     * Constructor.
     *
     * @param num The object number.
     * @param gen The object generation number.
     * @param index The index within a compressed object stream
     */
    public COSObjectKey(long num, int gen, int index)
    {
        if (num < 0)
        {
            throw new IllegalArgumentException("Object number must not be a negative value");
        }
        if (gen < 0)
        {
            throw new IllegalArgumentException("Generation number must not be a negative value");
        }
        numberAndGeneration = computeInternalHash(num, gen);
        this.streamIndex = index;
    }

    /**
     * Calculate the internal hash value for the given object number and generation number.
     * 
     * @param num the object number
     * @param gen the generation number
     * @return the internal hash for the given values
     */
    public static final long computeInternalHash(long num, int gen)
    {
        return num << NUMBER_OFFSET | (gen & GENERATION_MASK);
    }

    /**
     * Return the internal hash value which is based on the number and the generation.
     * 
     * @return the internal hash value
     */
    public long getInternalHash()
    {
        return numberAndGeneration;
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
     * The index within a compressed object stream.
     * 
     * @return the index within a compressed object stream if applicable otherwise -1
     */
    public int getStreamIndex()
    {
        return streamIndex;
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

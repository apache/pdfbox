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
package org.apache.pdfbox.pdfparser;

/**
 * This class represents a PDF xref.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.4 $
 */
public class PDFXref
{

    private long count;
    private long start;

    /**
     * constructor.
     *
     * @param startValue The start attribute.
     * @param countValue The count attribute.
     */
    public PDFXref( long startValue, long countValue )
    {
        setStart( startValue );
        setCount( countValue );
    }

    /**
     * This will get the count attribute.
     *
     * @return The count.
     */
    public long getCount()
    {
        return count;
    }

    /**
     * This will get the start attribute.
     *
     * @return The start.
     */
    public long getStart()
    {
        return start;
    }

    /**
     * This will set the count attribute.
     *
     * @param newCount The new count.
     */
    private void setCount(long newCount)
    {
        count = newCount;
    }

    /**
     * This will set the start attribute.
     *
     * @param newStart The new start attribute.
     */
    private void setStart(long newStart)
    {
        start = newStart;
    }
}

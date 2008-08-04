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
package org.pdfbox.pdmodel.graphics;

import org.pdfbox.cos.COSArray;
import org.pdfbox.cos.COSBase;
import org.pdfbox.cos.COSInteger;
import org.pdfbox.cos.COSNumber;

import org.pdfbox.pdmodel.common.COSArrayList;
import org.pdfbox.pdmodel.common.COSObjectable;

import java.util.List;

/**
 * This class represents the line dash pattern for a graphics state.  See PDF
 * Reference 1.5 section 4.3.2
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.7 $
 */
public class PDLineDashPattern implements COSObjectable, Cloneable
{
    private COSArray lineDashPattern = null;

    /**
     * Creates a blank line dash pattern.  With no dashes and a phase of 0.
     */
    public PDLineDashPattern()
    {
        lineDashPattern = new COSArray();
        lineDashPattern.add( new COSArray() );
        lineDashPattern.add( new COSInteger( 0 ) );
    }

    /**
     * Constructs a line dash pattern from an existing array.
     *
     * @param ldp The existing line dash pattern.
     */
    public PDLineDashPattern( COSArray ldp )
    {
        lineDashPattern = ldp;
    }
    
    /**
     * Constructs a line dash pattern from an existing array.
     *
     * @param ldp The existing line dash pattern.
     * @param phase The phase for the line dash pattern.
     */
    public PDLineDashPattern( COSArray ldp, int phase )
    {
        lineDashPattern = new COSArray();
        lineDashPattern.add( ldp );
        lineDashPattern.add( new COSInteger( phase ) );
    }
    
    /**
     * {@inheritDoc}
     */
    public Object clone()
    {
        COSArray dash = getCOSDashPattern();
        COSArray copy = new COSArray();
        copy.addAll(dash);
        PDLineDashPattern pattern = new PDLineDashPattern(copy,getPhaseStart() );
        return pattern;
    }

    /**
     * {@inheritDoc}
     */
    public COSBase getCOSObject()
    {
        return lineDashPattern;
    }

    /**
     * This will get the line dash pattern phase.  The dash phase specifies the
     * distance into the dash pattern at which to start the dash.
     *
     * @return The line dash pattern phase.
     */
    public int getPhaseStart()
    {
        COSNumber phase = (COSNumber)lineDashPattern.get( 1 );
        return phase.intValue();
    }

    /**
     * This will set the line dash pattern phase.
     *
     * @param phase The new line dash patter phase.
     */
    public void setPhaseStart( int phase )
    {
        lineDashPattern.set( 1, new COSInteger( phase ) );
    }

    /**
     * This will return a list of java.lang.Integer objects that represent the line
     * dash pattern appearance.
     *
     * @return The line dash pattern.
     */
    public List getDashPattern()
    {
        COSArray dashPatterns = (COSArray)lineDashPattern.get( 0 );
        return COSArrayList.convertIntegerCOSArrayToList( dashPatterns );
    }
    
    /**
     * Get the line dash pattern as a COS object.
     * 
     * @return The cos array line dash pattern.
     */
    public COSArray getCOSDashPattern()
    {
        return (COSArray)lineDashPattern.get( 0 );
    }

    /**
     * This will replace the existing line dash pattern.
     *
     * @param dashPattern A list of java.lang.Integer objects.
     */
    public void setDashPattern( List dashPattern )
    {
        lineDashPattern.set( 0, COSArrayList.converterToCOSArray( dashPattern ) );
    }
}
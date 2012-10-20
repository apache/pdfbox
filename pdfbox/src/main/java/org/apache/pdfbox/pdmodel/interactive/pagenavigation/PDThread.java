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
package org.apache.pdfbox.pdmodel.interactive.pagenavigation;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;

import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.common.COSObjectable;

/**
 * This a single thread in a PDF document.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.2 $
 */
public class PDThread implements COSObjectable
{


    private COSDictionary thread;

    /**
     * Constructor that is used for a preexisting dictionary.
     *
     * @param t The underlying dictionary.
     */
    public PDThread( COSDictionary t )
    {
        thread = t;
    }

    /**
     * Default constructor.
     *
     */
    public PDThread()
    {
        thread = new COSDictionary();
        thread.setName( "Type", "Thread" );
    }

    /**
     * This will get the underlying dictionary that this object wraps.
     *
     * @return The underlying info dictionary.
     */
    public COSDictionary getDictionary()
    {
        return thread;
    }

    /**
     * Convert this standard java object to a COS object.
     *
     * @return The cos object that matches this Java object.
     */
    public COSBase getCOSObject()
    {
        return thread;
    }

    /**
     * Get info about the thread, or null if there is nothing.
     *
     * @return The thread information.
     */
    public PDDocumentInformation getThreadInfo()
    {
        PDDocumentInformation retval = null;
        COSDictionary info = (COSDictionary)thread.getDictionaryObject( "I" );
        if( info != null )
        {
            retval = new PDDocumentInformation( info );
        }

        return retval;
    }

    /**
     * Set the thread info, can be null.
     *
     * @param info The info dictionary about this thread.
     */
    public void setThreadInfo( PDDocumentInformation info )
    {
        thread.setItem( "I", info );
    }

    /**
     * Get the first bead in the thread, or null if it has not been set yet.  This
     * is a required field for this object.
     *
     * @return The first bead in the thread.
     */
    public PDThreadBead getFirstBead()
    {
        PDThreadBead retval = null;
        COSDictionary bead = (COSDictionary)thread.getDictionaryObject( "F" );
        if( bead != null )
        {
            retval = new PDThreadBead( bead );
        }

        return retval;
    }

    /**
     * This will set the first bead in the thread.  When this is set it will
     * also set the thread property of the bead object.
     *
     * @param bead The first bead in the thread.
     */
    public void setFirstBead( PDThreadBead bead )
    {
        if( bead != null )
        {
            bead.setThread( this );
        }
        thread.setItem( "F", bead );
    }


}

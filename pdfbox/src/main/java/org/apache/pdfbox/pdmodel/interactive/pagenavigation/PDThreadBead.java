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

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;

import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.COSObjectable;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

/**
 * This a single bead in a thread in a PDF document.
 *
 * @author Ben Litchfield
 */
public class PDThreadBead implements COSObjectable
{


    private final COSDictionary bead;

    /**
     * Constructor that is used for a preexisting dictionary.
     *
     * @param b The underlying dictionary.
     */
    public PDThreadBead( COSDictionary b )
    {
        bead = b;
    }

    /**
     * Default constructor.
     *
     */
    public PDThreadBead()
    {
        bead = new COSDictionary();
        bead.setItem(COSName.TYPE, COSName.BEAD);
        setNextBead( this );
        setPreviousBead( this );
    }

    /**
     * This will get the underlying dictionary that this object wraps.
     *
     * @return The underlying info dictionary.
     */
    @Override
    public COSDictionary getCOSObject()
    {
        return bead;
    }

    /**
     * This will get the thread that this bead is part of.  This is only required
     * for the first bead in a thread, so other beads 'may' return null.
     *
     * @return The thread that this bead is part of.
     */
    public PDThread getThread()
    {
        COSDictionary dic = bead.getCOSDictionary(COSName.T);
        return dic != null ? new PDThread(dic) : null;
    }

    /**
     * Set the thread that this bead is part of.  This is only required for the
     * first bead in a thread.  Note: This property is set for you by the PDThread.setFirstBead() method.
     *
     * @param thread The thread that this bead is part of.
     */
    public void setThread( PDThread thread )
    {
        bead.setItem(COSName.T, thread);
    }

    /**
     * This will get the next bead.  If this bead is the last bead in the list then this
     * will return the first bead.
     *
     * @return The next bead in the list or the first bead if this is the last bead.
     */
    public PDThreadBead getNextBead()
    {
        return new PDThreadBead(bead.getCOSDictionary(COSName.N));
    }

    /**
     * Set the next bead in the thread.
     *
     * @param next The next bead.
     */
    protected final void setNextBead( PDThreadBead next )
    {
        bead.setItem(COSName.N, next);
    }

    /**
     * This will get the previous bead.  If this bead is the first bead in the list then this
     * will return the last bead.
     *
     * @return The previous bead in the list or the last bead if this is the first bead.
     */
    public PDThreadBead getPreviousBead()
    {
        return new PDThreadBead(bead.getCOSDictionary(COSName.V));
    }

    /**
     * Set the previous bead in the thread.
     *
     * @param previous The previous bead.
     */
    protected final void setPreviousBead( PDThreadBead previous )
    {
        bead.setItem(COSName.V, previous);
    }

    /**
     * Append a bead after this bead.  This will correctly set the next/previous beads in the
     * linked list.
     *
     * @param append The bead to insert.
     */
    public void appendBead( PDThreadBead append )
    {
        PDThreadBead nextBead = getNextBead();
        nextBead.setPreviousBead( append );
        append.setNextBead( nextBead );
        setNextBead( append );
        append.setPreviousBead( this );
    }

    /**
     * Get the page that this bead is part of.
     *
     * @return The page that this bead is part of.
     */
    public PDPage getPage()
    {
        COSDictionary dic = bead.getCOSDictionary(COSName.P);
        return dic != null ? new PDPage(dic) : null;
    }

    /**
     * Set the page that this bead is part of.  This is a required property and must be
     * set when creating a new bead.  The PDPage object also has a list of beads in the natural
     * reading order.  It is recommended that you add this object to that list as well.
     *
     * @param page The page that this bead is on.
     */
    public void setPage( PDPage page )
    {
        bead.setItem(COSName.P, page);
    }

    /**
     * The rectangle on the page that this bead is part of.
     *
     * @return The part of the page that this bead covers.
     */
    public PDRectangle getRectangle()
    {
        COSArray array = bead.getCOSArray(COSName.R);
        return array != null ? new PDRectangle(array) : null;
    }

    /**
     * Set the rectangle on the page that this bead covers.
     *
     * @param rect The portion of the page that this bead covers.
     */
    public void setRectangle( PDRectangle rect )
    {
        bead.setItem( COSName.R, rect );
    }
}

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.persistence.util.COSObjectKey;

/**
 * This class will collect all XRef/trailer objects and creates correct
 * xref/trailer information after all objects are read using startxref
 * and 'Prev' information (unused XRef/trailer objects are discarded).
 *
 * In case of missing startxref or wrong startxref pointer all
 * XRef/trailer objects are used to create xref table / trailer dictionary
 * in order they occur.
 *
 * For each new xref object/XRef stream method {@link #nextXrefObj(int)}
 * must be called with start byte position. All following calls to
 * {@link #setXRef(COSObjectKey, int)} or {@link #setTrailer(COSDictionary)}
 * will add the data for this byte position.
 *
 * After all objects are parsed the startxref position must be provided
 * using {@link #setStartxref(int)}. This is used to build the chain of
 * active xref/trailer objects used for creating document trailer and xref table.
 *
 * @author Timo Böhme (timo.boehme at ontochem.com)
 */
public class XrefTrailerResolver
{

    /**
     * A class which represents a xref/trailer object.
     */
    class XrefTrailerObj
    {
        private COSDictionary trailer = null;
        private final Map<COSObjectKey, Integer> xrefTable = new HashMap<COSObjectKey, Integer>();
    }

    private final Map<Integer, XrefTrailerObj> bytePosToXrefMap = new HashMap<Integer, XrefTrailerObj>();
    private XrefTrailerObj curXrefTrailerObj   = null;
    private XrefTrailerObj resolvedXrefTrailer = null;

    /** Log instance. */
    private static final Log log = LogFactory.getLog( XrefTrailerResolver.class );

    /**
     * Signals that a new XRef object (table or stream) starts.
     * @param startBytePos the offset to start at
     *
     */
    public void nextXrefObj( final int startBytePos )
    {
        bytePosToXrefMap.put( startBytePos, curXrefTrailerObj = new XrefTrailerObj() );
    }

    /**
     * Populate XRef HashMap of current XRef object.
     * Will add an Xreftable entry that maps ObjectKeys to byte offsets in the file.
     * @param objKey The objkey, with id and gen numbers
     * @param offset The byte offset in this file
     */
    public void setXRef( COSObjectKey objKey, int offset )
    {
        if ( curXrefTrailerObj == null )
        {
            // should not happen...
            log.warn( "Cannot add XRef entry for '" + objKey.getNumber() + "' because XRef start was not signalled." );
            return;
        }
        curXrefTrailerObj.xrefTable.put( objKey, offset );
    }

    /**
     * Adds trailer information for current XRef object.
     *
     * @param trailer the current document trailer dictionary
     */
    public void setTrailer( COSDictionary trailer )
    {
        if ( curXrefTrailerObj == null )
        {
            // should not happen...
            log.warn( "Cannot add trailer because XRef start was not signalled." );
            return;
        }
        curXrefTrailerObj.trailer = trailer;
    }

    /**
     * Sets the byte position of the first XRef
     * (has to be called after very last startxref was read).
     * This is used to resolve chain of active XRef/trailer.
     *
     * In case startxref position is not found we output a
     * warning and use all XRef/trailer objects combined
     * in byte position order.
     * Thus for incomplete PDF documents with missing
     * startxref one could call this method with parameter value -1.
     */
    public void setStartxref( int startxrefBytePos )
    {
        if ( resolvedXrefTrailer != null )
        {
            log.warn( "Method must be called only ones with last startxref value." );
            return;
        }

        resolvedXrefTrailer = new XrefTrailerObj();
        resolvedXrefTrailer.trailer = new COSDictionary();

        XrefTrailerObj curObj = bytePosToXrefMap.get( startxrefBytePos );
        List<Integer>  xrefSeqBytePos = new ArrayList<Integer>();

        if ( curObj == null )
        {
            // no XRef at given position
            log.warn( "Did not found XRef object at specified startxref position " + startxrefBytePos );

            // use all objects in byte position order (last entries overwrite previous ones)
            xrefSeqBytePos.addAll( bytePosToXrefMap.keySet() );
            Collections.sort( xrefSeqBytePos );
        }
        else
        {
            // found starting Xref object
            // add this and follow chain defined by 'Prev' keys
            xrefSeqBytePos.add( startxrefBytePos );
            while ( curObj.trailer != null )
            {
                int prevBytePos = curObj.trailer.getInt( COSName.PREV, -1 );
                if ( prevBytePos == -1 )
                {
                    break;
                }

                curObj = bytePosToXrefMap.get( prevBytePos );
                if ( curObj == null )
                {
                    log.warn( "Did not found XRef object pointed to by 'Prev' key at position " + prevBytePos );
                    break;
                }
                xrefSeqBytePos.add( prevBytePos );

                // sanity check to prevent infinite loops
                if ( xrefSeqBytePos.size() >= bytePosToXrefMap.size() )
                {
                    break;
                }
            }
            // have to reverse order so that later XRefs will overwrite previous ones
            Collections.reverse( xrefSeqBytePos );
        }

        // merge used and sorted XRef/trailer
        for ( Integer bPos : xrefSeqBytePos )
        {
            curObj = bytePosToXrefMap.get( bPos );
            if ( curObj.trailer != null )
            {
                resolvedXrefTrailer.trailer.addAll( curObj.trailer );
            }
            resolvedXrefTrailer.xrefTable.putAll( curObj.xrefTable );
        }

    }

    /**
     * Gets the resolved trailer. Might return <code>null</code> in case
     * {@link #setStartxref(int)} was not called before.
     *
     */
    public COSDictionary getTrailer()
    {
        return ( resolvedXrefTrailer == null ) ? null : resolvedXrefTrailer.trailer;
    }

    /**
     * Gets the resolved xref table. Might return <code>null</code> in case
     *  {@link #setStartxref(int)} was not called before.
     *
     */
    public Map<COSObjectKey, Integer> getXrefTable()
    {
        return ( resolvedXrefTrailer == null ) ? null : resolvedXrefTrailer.xrefTable;
    }
}

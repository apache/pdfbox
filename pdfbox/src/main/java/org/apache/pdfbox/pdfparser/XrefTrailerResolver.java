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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.io.IOUtils;
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
    private class XrefTrailerObj
    {
        protected COSDictionary trailer = null;
        private final Map<COSObjectKey, Long> xrefTable = new HashMap<COSObjectKey, Long>();
        
        /**
         *  Default constructor.
         */
        private XrefTrailerObj()
        {
        }
        
        /**
         * Release all used resources.
         */
        public void clearResources()
        {
        	if (trailer != null)
        	{
        		trailer.clear();
        		trailer = null;
        	}
        	if (xrefTable != null)
        	{
        		xrefTable.clear();
        	}
        }
    }

    private final Map<Long, XrefTrailerObj> bytePosToXrefMap = new HashMap<Long, XrefTrailerObj>();
    private XrefTrailerObj curXrefTrailerObj   = null;
    private XrefTrailerObj resolvedXrefTrailer = null;

    /** Log instance. */
    private static final Log LOG = LogFactory.getLog( XrefTrailerResolver.class );

    public final COSDictionary getFirstTrailer() {
    	if (bytePosToXrefMap.isEmpty()) return null;
    	
    	Set<Long> offsets = bytePosToXrefMap.keySet();
    	SortedSet<Long> sortedOffset = new TreeSet<Long>(offsets);
    	return bytePosToXrefMap.get(sortedOffset.first()).trailer;
    }
    
    public final COSDictionary getLastTrailer() {
    	if (bytePosToXrefMap.isEmpty()) return null;
    	
    	Set<Long> offsets = bytePosToXrefMap.keySet();
    	SortedSet<Long> sortedOffset = new TreeSet<Long>(offsets);
    	return bytePosToXrefMap.get(sortedOffset.last()).trailer;
    }
    
    /**
     * Signals that a new XRef object (table or stream) starts.
     * @param startBytePos the offset to start at
     *
     */
    public void nextXrefObj( final long startBytePos )
    {
        bytePosToXrefMap.put( startBytePos, curXrefTrailerObj = new XrefTrailerObj() );
    }

    /**
     * Populate XRef HashMap of current XRef object.
     * Will add an Xreftable entry that maps ObjectKeys to byte offsets in the file.
     * @param objKey The objkey, with id and gen numbers
     * @param offset The byte offset in this file
     */
    public void setXRef( COSObjectKey objKey, long offset )
    {
        if ( curXrefTrailerObj == null )
        {
            // should not happen...
            LOG.warn( "Cannot add XRef entry for '" + objKey.getNumber() + "' because XRef start was not signalled." );
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
            LOG.warn( "Cannot add trailer because XRef start was not signalled." );
            return;
        }
        curXrefTrailerObj.trailer = trailer;
    }

    /**
     * Returns the trailer last set by {@link #setTrailer(COSDictionary)}.
     * 
     * @return the current trailer.
     * 
     */
    public COSDictionary getCurrentTrailer() 
    {
        return curXrefTrailerObj.trailer;
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
     * 
     * @param startxrefBytePosValue starting position of the first XRef
     * 
     */
    public void setStartxref( long startxrefBytePosValue )
    {
        if ( resolvedXrefTrailer != null )
        {
            LOG.warn( "Method must be called only ones with last startxref value." );
            return;
        }

        resolvedXrefTrailer = new XrefTrailerObj();
        resolvedXrefTrailer.trailer = new COSDictionary();

        XrefTrailerObj curObj = bytePosToXrefMap.get( startxrefBytePosValue );
        List<Long>  xrefSeqBytePos = new ArrayList<Long>();

        if ( curObj == null )
        {
            // no XRef at given position
            LOG.warn( "Did not found XRef object at specified startxref position " + startxrefBytePosValue );

            // use all objects in byte position order (last entries overwrite previous ones)
            xrefSeqBytePos.addAll( bytePosToXrefMap.keySet() );
            Collections.sort( xrefSeqBytePos );
        }
        else
        {
            // found starting Xref object
            // add this and follow chain defined by 'Prev' keys
            xrefSeqBytePos.add( startxrefBytePosValue );
            while ( curObj.trailer != null )
            {
                long prevBytePos = curObj.trailer.getLong( COSName.PREV, -1L );
                if ( prevBytePos == -1 )
                {
                    break;
                }

                curObj = bytePosToXrefMap.get( prevBytePos );
                if ( curObj == null )
                {
                    LOG.warn( "Did not found XRef object pointed to by 'Prev' key at position " + prevBytePos );
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
        for ( Long bPos : xrefSeqBytePos )
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
     * @return the trailer if available
     */
    public COSDictionary getTrailer()
    {
        return ( resolvedXrefTrailer == null ) ? null : resolvedXrefTrailer.trailer;
    }

    /**
     * Gets the resolved xref table. Might return <code>null</code> in case
     *  {@link #setStartxref(int)} was not called before.
     *
     * @return the xrefTable if available
     */
    public Map<COSObjectKey, Long> getXrefTable()
    {
        return ( resolvedXrefTrailer == null ) ? null : resolvedXrefTrailer.xrefTable;
    }
    
    /** Returns object numbers which are referenced as contained
     *  in object stream with specified object number.
     *  
     *  This will scan resolved xref table for all entries having negated
     *  stream object number as value.
     *
     *  @param objstmObjNr  object number of object stream for which contained object numbers
     *                      should be returned
     *                       
     *  @return set of object numbers referenced for given object stream
     *          or <code>null</code> if {@link #setStartxref(long)} was not
     *          called before so that no resolved xref table exists
     */
    public Set<Long> getContainedObjectNumbers( final int objstmObjNr ) 
    {
        if ( resolvedXrefTrailer == null )
        {
            return null;
        }
        final Set<Long> refObjNrs = new HashSet<Long>();
        final int       cmpVal    = - objstmObjNr;
        
        for ( Entry<COSObjectKey,Long> xrefEntry : resolvedXrefTrailer.xrefTable.entrySet() ) 
        {
            if ( xrefEntry.getValue() == cmpVal )
            {
                refObjNrs.add( xrefEntry.getKey().getNumber() );
            }
        }
        return refObjNrs;
    }
    
    /**
     * Release all used resources.
     */
    public void clearResources()
    {
    	if (curXrefTrailerObj != null)
    	{
    		curXrefTrailerObj.clearResources();
    		curXrefTrailerObj = null;
    	}
    	if (resolvedXrefTrailer != null)
    	{
    		resolvedXrefTrailer.clearResources();
    		resolvedXrefTrailer = null;
    	}
    	if (bytePosToXrefMap != null)
    	{
    		bytePosToXrefMap.clear();
    	}
    }

}

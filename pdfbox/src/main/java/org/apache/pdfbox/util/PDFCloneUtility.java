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
package org.apache.pdfbox.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.COSObjectable;
import org.apache.pdfbox.pdmodel.common.PDStream;

/**
 * Utility class used to clone PDF objects. It keeps track of objects it has already cloned.
 *
 * @version $Revision$
 */
public class PDFCloneUtility
{

    private PDDocument destination;
    private Map<Object,COSBase> clonedVersion = new HashMap<Object,COSBase>();

    /**
     * Creates a new instance for the given target document.
     * @param dest the destination PDF document that will receive the clones
     */
    public PDFCloneUtility(PDDocument dest)
    {
        this.destination = dest;
    }

    /**
     * Returns the destination PDF document this cloner instance is set up for.
     * @return the destination PDF document
     */
    public PDDocument getDestination()
    {
        return this.destination;
    }

    /**
     * Deep-clones the given object for inclusion into a different PDF document identified by
     * the destination parameter.
     * @param base the initial object as the root of the deep-clone operation
     * @return the cloned instance of the base object
     * @throws IOException if an I/O error occurs
     */
      public COSBase cloneForNewDocument( Object base ) throws IOException
      {
          if( base == null )
          {
              return null;
          }
          COSBase retval = (COSBase)clonedVersion.get( base );
          if( retval != null )
          {
              //we are done, it has already been converted.
          }
          else if( base instanceof List )
          {
              COSArray array = new COSArray();
              List list = (List)base;
              for( int i=0; i<list.size(); i++ )
              {
                  array.add( cloneForNewDocument( list.get( i ) ) );
              }
              retval = array;
          }
          else if( base instanceof COSObjectable && !(base instanceof COSBase) )
          {
              retval = cloneForNewDocument( ((COSObjectable)base).getCOSObject() );
              clonedVersion.put( base, retval );
          }
          else if( base instanceof COSObject )
          {
              COSObject object = (COSObject)base;
              retval = cloneForNewDocument( object.getObject() );
              clonedVersion.put( base, retval );
          }
          else if( base instanceof COSArray )
          {
              COSArray newArray = new COSArray();
              COSArray array = (COSArray)base;
              for( int i=0; i<array.size(); i++ )
              {
                  newArray.add( cloneForNewDocument( array.get( i ) ) );
              }
              retval = newArray;
              clonedVersion.put( base, retval );
          }
          else if( base instanceof COSStream )
          {
              COSStream originalStream = (COSStream)base;
              PDStream stream = new PDStream( destination, originalStream.getFilteredStream(), true );
              clonedVersion.put( base, stream.getStream() );
              for( Map.Entry<COSName, COSBase> entry :  originalStream.entrySet() )
              {
                  stream.getStream().setItem(
                          entry.getKey(),
                          cloneForNewDocument(entry.getValue()));
              }
              retval = stream.getStream();
          }
          else if( base instanceof COSDictionary )
          {
              COSDictionary dic = (COSDictionary)base;
              retval = new COSDictionary();
              clonedVersion.put( base, retval );
              for( Map.Entry<COSName, COSBase> entry : dic.entrySet() )
              {
                  ((COSDictionary)retval).setItem(
                          entry.getKey(),
                          cloneForNewDocument(entry.getValue()));
              }
          }
          else
          {
              retval = (COSBase)base;
          }
          clonedVersion.put( base, retval );
          return retval;
      }


      /**
       * Merges two objects of the same type by deep-cloning its members.
       * <br/>
       * Base and target must be instances of the same class.
       * @param base the base object to be cloned
       * @param target the merge target
       * @throws IOException if an I/O error occurs
       */
      public void cloneMerge( COSObjectable base, COSObjectable target) throws IOException
      {
          if( base == null )
          {
              return;
          }
          COSBase retval = (COSBase)clonedVersion.get( base );
          if( retval != null )
          {
              return;
              //we are done, it has already been converted. // ### Is that correct for cloneMerge???
          }
          else if( base instanceof List )
          {
              COSArray array = new COSArray();
              List list = (List)base;
              for( int i = 0; i < list.size(); i++ )
              {
                  array.add( cloneForNewDocument( list.get( i ) ) );
              }
              ((List)target).add(array);
          }
          else if( base instanceof COSObjectable && !(base instanceof COSBase) )
          {
              cloneMerge(((COSObjectable)base).getCOSObject(), ((COSObjectable)target).getCOSObject() );
              clonedVersion.put( base, retval );
          }
          else if( base instanceof COSObject )
          {
              if(target instanceof COSObject)
              {
                  cloneMerge(((COSObject) base).getObject(),((COSObject) target).getObject() );
              }
              else if(target instanceof COSDictionary)
              {
                  cloneMerge(((COSObject)base).getObject(), ((COSDictionary)target));
              }
              clonedVersion.put( base, retval );
          }
          else if( base instanceof COSArray )
          {
              COSArray array = (COSArray)base;
              for( int i=0; i<array.size(); i++ )
              {
                  ((COSArray)target).add( cloneForNewDocument( array.get( i ) ) );
              }
              clonedVersion.put( base, retval );
          }
          else if( base instanceof COSStream )
          {
            // does that make sense???
              COSStream originalStream = (COSStream)base;
              PDStream stream = new PDStream( destination, originalStream.getFilteredStream(), true );
              clonedVersion.put( base, stream.getStream() );
              for( Map.Entry<COSName, COSBase> entry : originalStream.entrySet() )
              {
                  stream.getStream().setItem(
                          entry.getKey(),
                          cloneForNewDocument(entry.getValue()));
              }
              retval = stream.getStream();
              target = retval;
          }
          else if( base instanceof COSDictionary )
          {
              COSDictionary dic = (COSDictionary)base;
              clonedVersion.put( base, retval );
              for( Map.Entry<COSName, COSBase> entry : dic.entrySet() )
              {
                  COSName key = entry.getKey();
                  COSBase value = entry.getValue();
                  if (((COSDictionary)target).getItem(key) != null)
                  {
                      cloneMerge(value, ((COSDictionary)target).getItem(key));
                  }
                  else
                  {
                      ((COSDictionary)target).setItem( key, cloneForNewDocument(value));
                  }
              }
          }
          else
          {
              retval = (COSBase)base;
          }
          clonedVersion.put( base, retval );

      }

}

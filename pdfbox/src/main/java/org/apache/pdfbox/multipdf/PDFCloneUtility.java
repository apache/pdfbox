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
package org.apache.pdfbox.multipdf;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.COSObjectable;

/**
 * Utility class used to clone PDF objects. It keeps track of objects it has
 * already cloned.
 *
 */
class PDFCloneUtility
{
    private final PDDocument destination;
    private final Map<Object,COSBase> clonedVersion = new HashMap<>();
    private final Set<COSBase> clonedValues = new HashSet<>();
    // It might be useful to use IdentityHashMap like in PDFBOX-4477 for speed,
    // but we need a really huge file to test this. A test with the file from PDFBOX-4477
    // did not show a noticeable speed difference.

    /**
     * Creates a new instance for the given target document.
     * @param dest the destination PDF document that will receive the clones
     */
    PDFCloneUtility(PDDocument dest)
    {
        this.destination = dest;
    }

    /**
     * Returns the destination PDF document this cloner instance is set up for.
     * @return the destination PDF document
     */
    PDDocument getDestination()
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
      COSBase cloneForNewDocument( Object base ) throws IOException
      {
          if( base == null )
          {
              return null;
          }
          COSBase retval = clonedVersion.get(base);
          if( retval != null )
          {
              //we are done, it has already been converted.
              return retval;
          }
          if (base instanceof COSBase && clonedValues.contains(base))
          {
              // Don't clone a clone
              return (COSBase) base;
          }
          if (base instanceof List)
          {
              COSArray array = new COSArray();
              List<?> list = (List<?>) base;
              for (Object obj : list)
              {
                  array.add(cloneForNewDocument(obj));
              }
              retval = array;
          }
          else if( base instanceof COSObjectable && !(base instanceof COSBase) )
          {
              retval = cloneForNewDocument( ((COSObjectable)base).getCOSObject() );
          }
          else if( base instanceof COSObject )
          {
              COSObject object = (COSObject)base;
              retval = cloneForNewDocument( object.getObject() );
          }
          else if( base instanceof COSArray )
          {
              COSArray newArray = new COSArray();
              COSArray array = (COSArray)base;
              for( int i=0; i<array.size(); i++ )
              {
                  COSBase value = array.get(i);
                  checkForRecursion(base, value);
                  newArray.add(cloneForNewDocument(value));
              }
              retval = newArray;
          }
          else if( base instanceof COSStream )
          {
              COSStream originalStream = (COSStream)base;
              COSStream stream = destination.getDocument().createCOSStream();
              try (OutputStream output = stream.createRawOutputStream();
                   InputStream input = originalStream.createRawInputStream())
              {
                  IOUtils.copy(input, output);
              }
              clonedVersion.put( base, stream );
              for( Map.Entry<COSName, COSBase> entry :  originalStream.entrySet() )
              {
                  COSBase value = entry.getValue();
                  checkForRecursion(base, value);
                  stream.setItem(entry.getKey(), cloneForNewDocument(value));
              }
              retval = stream;
          }
          else if( base instanceof COSDictionary )
          {
              COSDictionary dic = (COSDictionary)base;
              retval = new COSDictionary();
              clonedVersion.put( base, retval );
              for( Map.Entry<COSName, COSBase> entry : dic.entrySet() )
              {
                  COSBase value = entry.getValue();
                  checkForRecursion(base, value);
                  ((COSDictionary) retval).setItem(entry.getKey(), cloneForNewDocument(value));
              }
          }
          else
          {
              retval = (COSBase)base;
          }
          clonedVersion.put( base, retval );
          clonedValues.add(retval);
          return retval;
      }

      /**
       * Merges two objects of the same type by deep-cloning its members.
       * <br>
       * Base and target must be instances of the same class.
       * @param base the base object to be cloned
       * @param target the merge target
       * @throws IOException if an I/O error occurs
       */
      void cloneMerge( final COSObjectable base, COSObjectable target) throws IOException
      {
          if (base == null || base == target)
          {
              return;
          }
          COSBase retval = clonedVersion.get( base );
          if( retval != null )
          {
              return;
              //we are done, it has already been converted. // ### Is that correct for cloneMerge???
          }
          //TODO what when clone-merging a clone? Does it ever happen?
          if (!(base instanceof COSBase))
          {
              cloneMerge(base.getCOSObject(), target.getCOSObject());
          }
          else if( base instanceof COSObject )
          {
              if(target instanceof COSObject)
              {
                  cloneMerge(((COSObject) base).getObject(),((COSObject) target).getObject() );
              }
              else if (target instanceof COSDictionary || target instanceof COSArray)
              {
                  cloneMerge(((COSObject) base).getObject(), target);
              }
          }
          else if( base instanceof COSArray )
          {
              if (target instanceof COSObject)
              {
                  cloneMerge(base, ((COSObject) target).getObject());
              }
              else
              {
                  COSArray array = (COSArray) base;
                  for (int i = 0; i < array.size(); i++)
                  {
                      ((COSArray) target).add(cloneForNewDocument(array.get(i)));
                  }
              }
          }
          else if( base instanceof COSStream )
          {
            // does that make sense???
              COSStream originalStream = (COSStream)base;
              COSStream stream = destination.getDocument().createCOSStream();
              try (OutputStream output = stream.createOutputStream(originalStream.getFilters()))
              {
                  IOUtils.copy(originalStream.createInputStream(), output);
              }
              clonedVersion.put( base, stream );
              for( Map.Entry<COSName, COSBase> entry : originalStream.entrySet() )
              {
                  stream.setItem(entry.getKey(), cloneForNewDocument(entry.getValue()));
              }
              retval = stream;
          }
          else if( base instanceof COSDictionary )
          {
              if (target instanceof COSObject)
              {
                  cloneMerge(base, ((COSObject) target).getObject());
              }
              else
              {
                  COSDictionary dic = (COSDictionary) base;
                  clonedVersion.put(base, retval);
                  for (Map.Entry<COSName, COSBase> entry : dic.entrySet())
                  {
                      COSName key = entry.getKey();
                      COSBase value = entry.getValue();
                      if (((COSDictionary) target).getItem(key) != null)
                      {
                          cloneMerge(value, ((COSDictionary) target).getItem(key));
                      }
                      else
                      {
                          ((COSDictionary) target).setItem(key, cloneForNewDocument(value));
                      }
                  }
              }
          }
          else
          {
              retval = (COSBase)base;
          }
          clonedVersion.put( base, retval );
          clonedValues.add(retval);
      }

    /**
     * Check whether an element (of an array or a dictionary) points to its parent.
     *
     * @param parent COSArray or COSDictionary
     * @param value an element
     * @throws IOException if value is an object reference to the parent
     */
    private void checkForRecursion(Object parent, COSBase value) throws IOException
    {
        if (value instanceof COSObject)
        {
            COSBase actual = ((COSObject) value).getObject();
            if (actual == parent)
            {
                throw new IOException("Loop within object " + value);
            }
        }
    }
}

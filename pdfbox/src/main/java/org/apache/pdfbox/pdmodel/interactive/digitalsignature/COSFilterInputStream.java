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
package org.apache.pdfbox.pdmodel.interactive.digitalsignature;

import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.io.IOUtils;

/**
 * A filtered stream that includes the bytes that are in the (begin,length) intervals passed in the
 * constructor.
 */
public class COSFilterInputStream extends FilterInputStream
{
  /**
  * Log instance.
   */
  private static final Log LOG = LogFactory.getLog(COSFilterInputStream.class);

  private final int[] byteRange;
  private long position = 0;
  
  public COSFilterInputStream(InputStream in, int[] byteRange)
  {
    super(in);
    this.byteRange = byteRange;
  }

  public COSFilterInputStream(byte[] in, int offset, int length, int[] byteRange)
  {
    super(new ByteArrayInputStream(in, offset, length));
    this.byteRange = byteRange;
  }

  public COSFilterInputStream(byte[] in, int[] byteRange)
  {
    super(new ByteArrayInputStream(in));
    this.byteRange = byteRange;
  }

  @Override
  public int read() throws IOException
  {
    nextAvailable();
    int i = super.read();
    if (i>-1)
    {
      ++position;
    }
    return i;
  }
  
  @Override
  public int read(byte[] b) throws IOException
  {
    return read(b,0,b.length);
  }
  
  @Override
  public int read(byte[] b, int off, int len) throws IOException
  {
    if (len == 0)
    {
        return 0;
    }
    
    int c = read();
    if (c == -1)
    {
        return -1;
    }
    b[off] = (byte)c;
  
    int i = 1;
    try
    {
        for (; i < len; i++)
        {
            c = read();
            if (c == -1)
            {
                break;
            }
            b[off + i] = (byte)c;
        }
    }
    catch (IOException ee) 
    {
      LOG.debug("An exception occured while trying to fill byte[] - ignoring", ee);
    }
    return i;
  }

  private boolean inRange() throws IOException
  {
    long pos = position;
    for (int i = 0; i<byteRange.length/2;++i)
    {
      if(byteRange[i*2] <= pos &&  byteRange[i*2]+byteRange[i*2+1]>pos)
      {
        return true;
      }
    }
    return false;
  }

  private void nextAvailable() throws IOException
  {
    while (!inRange())
    {
      ++position;
      if(super.read()<0)
      {
        break;
      }
    }
  }
  
  public byte[] toByteArray() throws IOException 
  {
      return IOUtils.toByteArray(this);
  }
}

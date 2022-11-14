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

package org.apache.pdfbox.pdmodel.font;

/**
 * FontMapper factory class.
 *
 * @author John Hewson
 */
public final class FontMappers
{
    private static FontMapper instance;

    private FontMappers()
    {
    }
    
    // lazy thread safe singleton
    private static class DefaultFontMapper
    {
        private static final FontMapper INSTANCE = new FontMapperImpl();
    }
    
    /**
     * Returns the singleton FontMapper instance.
     * 
     * @return a singleton FontMapper instance
     */
    public static FontMapper instance()
    {
        if (instance == null)
        {
            instance = DefaultFontMapper.INSTANCE;
        }
        return instance;
    }
    
    /**
     * Sets the singleton FontMapper instance.
     * 
     * @param fontMapper the singleton FontMapper instance to be stored
     */
    public static synchronized void set(FontMapper fontMapper)
    {
        instance = fontMapper;
    }
}

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
package org.apache.fontbox.afm;

/**
 * This class represents a ligature, which is an entry of the CharMetrics.
 *
 * @author Ben Litchfield
 */
public class Ligature
{
    private String successor;
    private String ligature;

    /** Getter for property ligature.
     * @return Value of property ligature.
     */
    public String getLigature()
    {
        return ligature;
    }

    /** Setter for property ligature.
     * @param lig New value of property ligature.
     */
    public void setLigature(String lig)
    {
        ligature = lig;
    }

    /** Getter for property successor.
     * @return Value of property successor.
     */
    public String getSuccessor()
    {
        return successor;
    }

    /** Setter for property successor.
     * @param successorValue New value of property successor.
     */
    public void setSuccessor(String successorValue)
    {
        successor = successorValue;
    }

}
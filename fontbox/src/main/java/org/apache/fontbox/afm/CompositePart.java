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
 * This class represents a part of composite character data.
 *
 * @author Ben Litchfield
 */
public class CompositePart
{
    private String name;
    private int xDisplacement;
    private int yDisplacement;

    /** Getter for property name.
     * @return Value of property name.
     */
    public java.lang.String getName()
    {
        return name;
    }

    /** Setter for property name.
     * @param nameValue New value of property name.
     */
    public void setName(String nameValue)
    {
        name = nameValue;
    }

    /** Getter for property xDisplacement.
     * @return Value of property xDisplacement.
     */
    public int getXDisplacement()
    {
        return xDisplacement;
    }

    /** Setter for property xDisplacement.
     * @param xDisp New value of property xDisplacement.
     */
    public void setXDisplacement(int xDisp)
    {
        xDisplacement = xDisp;
    }

    /** Getter for property yDisplacement.
     * @return Value of property yDisplacement.
     */
    public int getYDisplacement()
    {
        return yDisplacement;
    }

    /** Setter for property yDisplacement.
     * @param yDisp New value of property yDisplacement.
     */
    public void setYDisplacement(int yDisp)
    {
        yDisplacement = yDisp;
    }

}
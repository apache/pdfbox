/*****************************************************************************
/*****************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * 
 ****************************************************************************/

package org.apache.xmpbox.type;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

public abstract class AbstractTypeTester
{

    private static final long COUNTER_SEED = 0;

    private static final long MAX_COUNTER = Long.MAX_VALUE;

    public static final int RAND_LOOP_COUNT = 50;


    private Random counterRandom = new Random(COUNTER_SEED);

    protected void initializeSeed(Random rand)
    {
        this.counterRandom = rand;
    }

    public String calculateSimpleGetter(String name)
    {
        StringBuilder sb = new StringBuilder(3 + name.length());
        sb.append("get").append(calculateFieldNameForMethod(name));
        return sb.toString();
    }

    public String calculateArrayGetter(String name)
    {
        StringBuilder sb = new StringBuilder(4 + name.length());
        String fn = calculateFieldNameForMethod(name);
        sb.append("get").append(fn);
        if (!fn.endsWith("s"))
        {
            sb.append("s");
        }
        return sb.toString();
    }

    public String calculateSimpleSetter(String name)
    {
        StringBuilder sb = new StringBuilder(3 + name.length());
        sb.append("set").append(calculateFieldNameForMethod(name));
        return sb.toString();
    }

    public String calculateFieldNameForMethod(String name)
    {
        StringBuilder sb = new StringBuilder(name.length());
        sb.append(name.substring(0, 1).toUpperCase()).append(name.substring(1));
        return sb.toString();
    }

    public Class<?> getJavaType(Types type)
    {
        if (type.getImplementingClass() == TextType.class)
        {
            return String.class;
        }
        else if (type.getImplementingClass() == DateType.class)
        {
            return Calendar.class;
        }
        else if (type.getImplementingClass() == IntegerType.class)
        {
            return Integer.class;
        }
        else if (TextType.class.isAssignableFrom(type.getImplementingClass()))
        {
            return String.class;
        }
        else
        {
            throw new IllegalArgumentException("Type not expected in test : " + type.getImplementingClass());
        }
    }

    public Object getJavaValue(Types type)
    {
        if (TextType.class.isAssignableFrom(type.getImplementingClass()))
        {
            return "Text_String_"+ counterRandom.nextLong()%MAX_COUNTER;
        }
        else if (type.getImplementingClass() == DateType.class)
        {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(counterRandom.nextLong()%MAX_COUNTER);
            return calendar;
        }
        else if (type.getImplementingClass() == IntegerType.class)
        {
            return counterRandom.nextInt();
        }
        else
        {
            throw new IllegalArgumentException("Type not expected in test : " + type.getImplementingClass());
        }
    }

    public List<Field> getXmpFields(Class<?> clz)
    {
        Field[] fields = clz.getFields();
        List<Field> result = new ArrayList<Field>(fields.length);
        for (Field field : fields)
        {
            if (field.getAnnotation(PropertyType.class) != null)
            {
                result.add(field);
            }
        }
        return result;
    }

}

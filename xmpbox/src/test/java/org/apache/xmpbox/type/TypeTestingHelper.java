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
import java.util.UUID;

public final class TypeTestingHelper
{

    public static String calculateSimpleGetter(String name)
    {
        StringBuilder sb = new StringBuilder(3 + name.length());
        sb.append("get").append(calculateFieldNameForMethod(name));
        return sb.toString();
    }

    public static String calculateArrayGetter(String name)
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

    public static String calculateSimpleSetter(String name)
    {
        StringBuilder sb = new StringBuilder(3 + name.length());
        sb.append("set").append(calculateFieldNameForMethod(name));
        return sb.toString();
    }

    public static String calculateFieldNameForMethod(String name)
    {
        StringBuilder sb = new StringBuilder(name.length());
        sb.append(name.substring(0, 1).toUpperCase()).append(name.substring(1));
        return sb.toString();
    }

    public static Class<?> getJavaType(Types type)
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

    public static Object getJavaValue(Types type)
    {
        if (type.getImplementingClass() == TextType.class)
        {
            return UUID.randomUUID().toString();
        }
        else if (type.getImplementingClass() == DateType.class)
        {
            // use random because test are too fast (generate same calendar
            // twice)
            Calendar calendar = Calendar.getInstance();
            Random rand = new Random();
            calendar.setTimeInMillis(rand.nextLong());
            return calendar;
        }
        else if (type.getImplementingClass() == IntegerType.class)
        {
            return new Integer(14);
        }
        else if (TextType.class.isAssignableFrom(type.getImplementingClass()))
        {
            // all derived from TextType
            return UUID.randomUUID().toString();
        }
        else
        {
            throw new IllegalArgumentException("Type not expected in test : " + type.getImplementingClass());
        }
    }

    public static List<Field> getXmpFields(Class<?> clz)
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

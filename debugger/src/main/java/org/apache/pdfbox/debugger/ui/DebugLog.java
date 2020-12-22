/*
 * Copyright 2016 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pdfbox.debugger.ui;

import org.apache.commons.logging.Log;

/**
 * Custom Log implementation which forwards to LogDialog.
 *
 * @author John Hewson
 */
public class DebugLog implements Log
{
    private final String name;
    
    // hardcoded, but kept to aid with debugging custom builds
    private static final boolean INFO = true;
    private static final boolean TRACE = false;
    private static final boolean DEBUG = false;

    public DebugLog(final String name)
    {
        this.name = name;
    }

    @Override
    public void debug(final Object o)
    {
        if (DEBUG)
        {
            LogDialog.instance().log(name, "debug", o, null);
        }
    }

    @Override
    public void debug(final Object o, final Throwable throwable)
    {
        if (DEBUG)
        {
            LogDialog.instance().log(name, "debug", o, throwable);
        }
    }

    @Override
    public void error(final Object o)
    {
        LogDialog.instance().log(name, "error", o, null);
    }

    @Override
    public void error(final Object o, final Throwable throwable)
    {
        LogDialog.instance().log(name, "error", o, throwable);
    }

    @Override
    public void fatal(final Object o)
    {
        LogDialog.instance().log(name, "fatal", o, null);
    }

    @Override
    public void fatal(final Object o, final Throwable throwable)
    {
        LogDialog.instance().log(name, "fatal", o, throwable);
    }

    @Override
    public void info(final Object o)
    {
        if (INFO)
        {
            LogDialog.instance().log(name, "info", o, null);
        }
    }

    @Override
    public void info(final Object o, final Throwable throwable)
    {
        if (INFO)
        {
            LogDialog.instance().log(name, "info", o, throwable);
        }
    }

    @Override
    public boolean isDebugEnabled()
    {
        return DEBUG;
    }

    @Override
    public boolean isErrorEnabled()
    {
        return true;
    }

    @Override
    public boolean isFatalEnabled()
    {
        return true;
    }

    @Override
    public boolean isInfoEnabled()
    {
        return INFO;
    }

    @Override
    public boolean isTraceEnabled()
    {
        return TRACE;
    }

    @Override
    public boolean isWarnEnabled()
    {
        return true;
    }

    @Override
    public void trace(final Object o)
    {
        if (TRACE)
        {
            LogDialog.instance().log(name, "trace", o, null);
        }
    }

    @Override
    public void trace(final Object o, final Throwable throwable)
    {
        if (TRACE)
        {
            LogDialog.instance().log(name, "trace", o, throwable);
        }
    }

    @Override
    public void warn(final Object o)
    {
        LogDialog.instance().log(name, "warn", o, null);
    }

    @Override
    public void warn(final Object o, final Throwable throwable)
    {
        LogDialog.instance().log(name, "warn", o, throwable);
    }
}

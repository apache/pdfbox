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
package org.apache.pdfbox.debugger.ui;

import org.apache.logging.log4j.*;
import org.apache.logging.log4j.core.*;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.*;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;

import java.io.Serializable;

public class DebugLogAppender extends AbstractAppender
{
    protected DebugLogAppender(String name, Filter filter, Layout<? extends Serializable> layout,
            final boolean ignoreExceptions)
    {
        super(name, filter, layout, ignoreExceptions);
    }

    @Override
    public void append(LogEvent event)
    {
        // forward to your logging dialog box here.
        LogDialog.instance().log(event.getLoggerName(), event.getLevel().name(),
                event.getMessage().getFormattedMessage(), event.getThrown());
    }

    public static void setupCustomLogger()
    {
        ConfigurationBuilder<BuiltConfiguration> builder = ConfigurationBuilderFactory
                .newConfigurationBuilder();

        AppenderComponentBuilder appenderBuilder = builder.newAppender("Stdout", "CONSOLE")
                .addAttribute("target", ConsoleAppender.Target.SYSTEM_OUT);
        appenderBuilder.add(builder.newLayout("PatternLayout").addAttribute("pattern",
                "%d [%t] %-5level: %msg%n%throwable"));
        builder.add(appenderBuilder);

        appenderBuilder = builder.newAppender("Custom", "org.package.CustomAppender");
        appenderBuilder.add(builder.newLayout("PatternLayout").addAttribute("pattern",
                "%d [%t] %-5level: %msg%n%throwable"));
        builder.add(appenderBuilder);

        builder.add(builder.newRootLogger(Level.ERROR).add(builder.newAppenderRef("Stdout"))
                .add(builder.newAppenderRef("Custom")));
        Configurator.initialize(builder.build());
    }
}

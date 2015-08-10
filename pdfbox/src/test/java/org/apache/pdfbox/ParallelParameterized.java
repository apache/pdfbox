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

package org.apache.pdfbox;

import org.junit.runners.Parameterized;
import org.junit.runners.model.RunnerScheduler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Runs Parameterized JUnit tests in parallel.
 *
 * @see {http://goo.gl/lkigES}
 */
public class ParallelParameterized extends Parameterized
{
    private static class FixedThreadPoolScheduler implements RunnerScheduler
    {
        private final ExecutorService executorService;
        private final long timeoutSeconds;

        FixedThreadPoolScheduler(long timeoutSeconds)
        {
            this.timeoutSeconds = timeoutSeconds;
            int cores = Runtime.getRuntime().availableProcessors();

            // for debugging
            System.out.println("JDK: " + System.getProperty("java.runtime.name"));
            System.out.println("Version: " + System.getProperty("java.specification.version"));

            // workaround Open JDK 6 bug which causes CMMException: Invalid profile data
            if (System.getProperty("java.runtime.name").equals("OpenJDK Runtime Environment") &&
                System.getProperty("java.specification.version").equals("1.6"))
            {
                cores = 1;
            }

            executorService = Executors.newFixedThreadPool(cores);
        }

        @Override 
        public void finished()
        {
            executorService.shutdown();
            try
            {
                executorService.awaitTermination(timeoutSeconds, TimeUnit.SECONDS);
            }
            catch (InterruptedException exc)
            {
                throw new RuntimeException(exc);
            }
        }

        @Override public void schedule(Runnable childStatement)
        {
            executorService.submit(childStatement);
        }
    }

    public ParallelParameterized(Class c) throws Throwable
    {
        super(c);
        long timeoutSeconds = Long.MAX_VALUE;
        if (c.getSimpleName().equals("TestRendering"))
        {
            timeoutSeconds = 30;
        }
        setScheduler(new FixedThreadPoolScheduler(timeoutSeconds));
    }
}

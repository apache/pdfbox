package org.apache.fontbox.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * *INTERNAL* class to avoid finalizers. When running on JDK <= 8 it will still use finalizers, from JDK 9+ it will use
 * java.lang.Cleaner
 * <p>
 * Do not use, PDFBox internal use only! ALWAYS use java.lang.ref.Cleaner.Cleanable directly.
 * <p>
 * Note: You have to store a reference to the Cleanable object in the class which had a finalizer. Otherwise, this won't
 * work.
 */
public class PDFBoxInternalCleaner
{

    /**
     * @see java.lang.ref.Cleaner.Cleanable
     */
    public interface Cleanable
    {
        void clean();
    }

    private interface CleanerImpl
    {
        Cleanable register(Object obj, CleaningRunnable action);
    }

    /**
     * @see java.lang.ref.Cleaner.Cleanable
     */
    public interface CleaningRunnable
    {
        /**
         * Perform the cleanup action. It is ensured that this method will run only once.
         */
        void run() throws Throwable;
    }

    private CleanerImpl impl;

    /**
     * Simple implementation for JDK <= 8 using finalizers.
     */
    private static class CleanerImplJDK8 implements CleanerImpl
    {

        static class ObjectFinalizer implements Cleanable
        {
            final String simpleClassName;
            final CleaningRunnable action;
            boolean didRun;

            ObjectFinalizer(String simpleClassName, CleaningRunnable action)
            {
                this.simpleClassName = simpleClassName;
                this.action = action;
            }

            static final Log LOG = LogFactory.getLog(PDFBoxInternalCleaner.class);

            @Override
            protected void finalize() throws Throwable
            {
                synchronized (this)
                {
                    if (!didRun)
                    {
                        LOG.debug(simpleClassName + " not closed!");
                    }
                }
                clean();
            }

            @Override
            public void clean()
            {
                try
                {
                    synchronized (this)
                    {
                        if (didRun)
                            return;
                        didRun = true;
                        action.run();
                    }
                }
                catch (Throwable t)
                {
                    throw new RuntimeException(t);
                }
            }
        }

        @Override
        public Cleanable register(Object obj, CleaningRunnable action)
        {
            return new ObjectFinalizer(obj.getClass().getSimpleName(), action);
        }
    }

    /**
     * Uses the JDK9 Cleaner using reflection
     */
    private static class CleanerImplJDK9 implements CleanerImpl
    {
        private final Object cleanerImpl;
        private final Method register;
        private final Method clean;

        CleanerImplJDK9()
                throws ClassNotFoundException, InvocationTargetException, IllegalAccessException,
                NoSuchMethodException
        {
            Class<?> cleaner = getClass().getClassLoader().loadClass("java.lang.ref.Cleaner");
            Class<?> cleanable = getClass().getClassLoader()
                    .loadClass("java.lang.ref.Cleaner$Cleanable");
            Method create = cleaner.getDeclaredMethod("create");
            cleanerImpl = create.invoke(null);
            register = cleaner.getDeclaredMethod("register", Object.class, Runnable.class);
            clean = cleanable.getDeclaredMethod("clean");
        }

        @Override
        public Cleanable register(Object obj, CleaningRunnable action)
        {
            try
            {
                Object cleanable = register.invoke(cleanerImpl, obj, (Runnable) () -> {
                    try
                    {
                        action.run();
                    }
                    catch (Throwable e)
                    {
                        throw new RuntimeException(e);
                    }
                });
                return () -> {
                    try
                    {
                        clean.invoke(cleanable);
                    }
                    catch (Exception e)
                    {
                        throw new RuntimeException(e);
                    }
                };
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    private static final boolean TRY_USING_JDK9_CLEANER = true;

    private PDFBoxInternalCleaner()
    {
        try
        {
            if (TRY_USING_JDK9_CLEANER)
                impl = new CleanerImplJDK9();
            else
                impl = new CleanerImplJDK8();
        }
        catch (Throwable throwable)
        {
            impl = new CleanerImplJDK8();
        }
    }

    /**
     * Register a cleanup action for the given object. You have to store the returned Cleanable instance in
     * the object to have this all work correctly.
     *
     * @param obj    the object which will trigger the cleanup action after it is GCed.
     * @param action the cleanup action to perform
     * @return a Cleanable instance. Call cleanup() on it if possible.
     */
    public Cleanable register(Object obj, CleaningRunnable action)
    {
        return impl.register(obj, action);
    }

    /**
     * Create a new cleaner instance.
     *
     * @return a new cleaner instance
     */
    public static PDFBoxInternalCleaner create()
    {
        return new PDFBoxInternalCleaner();
    }

}

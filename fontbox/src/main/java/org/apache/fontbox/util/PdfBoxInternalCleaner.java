package org.apache.fontbox.util;

import java.awt.geom.Line2D;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * *INTERNAL* class to avoid finalizers. When running on JDK <= 8 it will still use finalizers, from JDK 9+ it will use
 * java.lang.Cleaner
 * 
 * Do not use, ALWAYS use java.lang.ref.Cleaner.Cleanable directly.
 * 
 * Note: You have to store a reference to the Cleanable object in the class which had a finalizer. Otherwise, this won't
 * work.
 */
public class PdfBoxInternalCleaner
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

    public interface CleaningRunnable
    {
        void run() throws Throwable;
    }

    private CleanerImpl impl;

    /**
     * Simple implementation for JDK <= 8
     */
    private static class CleanerImplJDK8 implements CleanerImpl
    {

        static class ObjectFinalizer implements Cleanable
        {
            final CleaningRunnable action;
            boolean didRun;

            ObjectFinalizer(CleaningRunnable action)
            {
                this.action = action;
            }

            @Override
            protected void finalize() throws Throwable
            {
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
            return new ObjectFinalizer(action);
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

        CleanerImplJDK9() throws ClassNotFoundException, InvocationTargetException,
                IllegalAccessException, NoSuchMethodException
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

    private PdfBoxInternalCleaner()
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

    public Cleanable register(Object obj, CleaningRunnable action)
    {
        return impl.register(obj, action);
    }

    public static PdfBoxInternalCleaner create()
    {
        return new PdfBoxInternalCleaner();
    }

}

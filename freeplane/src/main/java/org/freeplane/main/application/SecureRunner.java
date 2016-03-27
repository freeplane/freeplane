package org.freeplane.main.application;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.Policy;
import java.security.ProtectionDomain;
import java.util.concurrent.Callable;

public class SecureRunner {
    private static final Permissions NO_PERMISSIONS = new Permissions();
    private static final boolean DISABLE_CHECKS = Boolean.getBoolean("org.freeplane.main.application.FreeplaneSecurityManager.disable");

    static {
        RestrictingClassLoader.class.getClassLoader();
        Policy.setPolicy(new RestrictingPolicy());
        System.setSecurityManager(new SecurityManager());
    }

    private static class RestrictingPolicy extends Policy {
        @Override
        public PermissionCollection getPermissions(CodeSource codesource) {
            return UNSUPPORTED_EMPTY_COLLECTION;
        }

        @Override
        public PermissionCollection getPermissions(ProtectionDomain domain) {
            return UNSUPPORTED_EMPTY_COLLECTION;
        }

        @Override
        public boolean implies(ProtectionDomain domain, Permission permission) {
        	if(DISABLE_CHECKS)
        		return true;
            ClassLoader classLoader = domain.getClassLoader();
            if (classLoader instanceof RestrictingClassLoader) {
                return ((RestrictingClassLoader) classLoader).implies(permission);
            } else {
                return true;
            }
        }
    }

    public static class Caller {
        public static Object call(Callable<?> callable) throws Exception {
            return callable.call();
        }
    }

    private static class RestrictingClassLoader extends URLClassLoader {
        private final Permissions whiteList;

        private final Permissions blackList;

        private final static URL CLASS_CONTAINING_RESOURCE_URL;

        static {
            String path = SecureRunner.class.getName().replace('.', '/').concat("$Caller.class");
            String callerPath = RestrictingClassLoader.class.getClassLoader().getResource(path)
                    .toString();
            String bundleElementPath = callerPath.substring(0, callerPath.length() - path.length());
            try {
                CLASS_CONTAINING_RESOURCE_URL = new URL(bundleElementPath);
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }

        public RestrictingClassLoader(final Permissions whiteList, final Permissions blackList) {
            super(new URL[0], SecureRunner.class.getClassLoader());
            this.whiteList = whiteList;
            this.blackList = blackList;
            addURL(CLASS_CONTAINING_RESOURCE_URL);
        }

        public Class<?> createCaller() throws ClassNotFoundException {
            Class<?> callerClass = findClass(SecureRunner.class.getName() + "$Caller");
            return callerClass;
        }

        public boolean implies(Permission permission) {
            return whiteList.implies(permission) || !blackList.implies(permission);
        }

    }

    public Object callWithBlackList(Permissions blackList, Callable<?> callable) throws Exception {
        return call(NO_PERMISSIONS, blackList, callable);
    }

    public Object callWithWhiteList(Permissions whiteList, Callable<?> callable) throws Exception {
        return call(whiteList, NO_PERMISSIONS, callable);
    }

    public Object call(Permissions whiteList, Permissions blackList, Callable<?> callable)
            throws Exception {
        try (RestrictingClassLoader restrictingClassLoader = new RestrictingClassLoader(whiteList,
                blackList)) {
            Class<?> callerClass = restrictingClassLoader.createCaller();
            return callerClass.getDeclaredMethods()[0].invoke(null, callable);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof Exception) {
                throw (Exception) e.getCause();
            }
            if (e.getCause() instanceof Error) {
                throw (Error) e.getCause();
            }
            throw e;
        }
    }

}
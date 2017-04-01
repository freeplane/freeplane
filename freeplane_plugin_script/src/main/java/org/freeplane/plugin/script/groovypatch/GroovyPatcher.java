package org.freeplane.plugin.script.groovypatch;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.pool.TypePool;

public class GroovyPatcher {
	private static final String CACHED_FIELD_CLASS = "org.codehaus.groovy.reflection.CachedField";
	private static final String CACHED_METHOD_CLASS = "org.codehaus.groovy.reflection.CachedMethod";
	static{
		final ClassLoader classLoader = GroovyPatcher.class.getClassLoader();
		final ByteBuddy byteBuddy = new ByteBuddy();
		TypePool typePool = TypePool.Default.of(classLoader);
		final ClassFileLocator classFileLocator = ClassFileLocator.ForClassLoader.of(classLoader);

		final MethodDelegation cachedFieldInterceptor = MethodDelegation.to(CachedFieldInterceptor.class);
		byteBuddy.rebase(typePool.describe(CACHED_FIELD_CLASS).resolve(), //
				classFileLocator)
		.method(ElementMatchers.named("getProperty")).intercept(cachedFieldInterceptor)
		.method(ElementMatchers.named("setProperty")).intercept(cachedFieldInterceptor)
		.make()
		.load(classLoader);

		final MethodDelegation cachedMethodInterceptor = MethodDelegation.to(CachedMethodInterceptor.class);
		final MethodDelegation cachedMethodInvocationInterceptor = MethodDelegation.to(CachedMethodInvocationInterceptor.class);

		byteBuddy.rebase(typePool.describe(CACHED_METHOD_CLASS).resolve(), //
				classFileLocator)
		.method(ElementMatchers.named("setAccessible")).intercept(cachedMethodInterceptor)
		.method(ElementMatchers.named("getCachedMethod")).intercept(cachedMethodInterceptor)
		.method(ElementMatchers.named("invoke")).intercept(cachedMethodInvocationInterceptor)
		.make()
		.load(classLoader);
	}
	
	public static void apply(){
		// patch happens only once in class static block 
	}
}

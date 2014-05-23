package thebombzen.mods.generictweaks;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

public class ReflectionHelper {
	/**
	 * Get the value of a private field. This one allows you to pass multiple
	 * field names (useful for obfuscation).
	 * 
	 * @param instance
	 *            The object whose field we're retrieving.
	 * @param declaringClass
	 *            The declaring class of the private field. Use a class literal
	 *            and not getClass(). This might be a superclass of the class of
	 *            the object.
	 * @param name
	 *            The multiple field names of the field to retrieve.
	 * @return The value of the field.
	 * @throws SecurityException If a security Error occurred
	 * @throws FieldNotFoundException If some other error occurred
	 */
	@SuppressWarnings("unchecked")
	public static <T, E> T getPrivateField(E instance, Class<? super E> declaringClass,
			String... names) throws FieldNotFoundException {
		for (String name : names) {
			try {
				Field field = declaringClass.getDeclaredField(name);
				field.setAccessible(true);
				try {
					return (T) field.get(instance);
				} catch (Exception e) {
					throw new FieldNotFoundException("Cannot get value of field", e);
				}
			} catch (NoSuchFieldException nsfe) {
				continue;
			}
		}
		throw new FieldNotFoundException("Names not found: " + Arrays.toString(names));
	}
	/**
	 * Invokes a private method, arranged conveniently. Tip: Use class literals.
	 * 
	 * @param instance
	 *            This is the object whose method we're invoking.
	 * @param declaringClass
	 *            This is the declaring class of the method we want. Use a class
	 *            literal and not getClass(). This argument is necessary because
	 *            Class.getMethods() only returns public methods, and
	 *            Class.getDeclaredMethods() requires the declaring class.
	 * @param name
	 *            The name of the method we want to invoke
	 * @param parameterTypes
	 *            The types of the parameters of the method (because
	 *            overloading).
	 * @param args
	 *            The arguments we want to pass to the method.
	 * @return Whatever the method returns
	 * @throws SecurityException if a security error occurs
	 * @throws MethodNotFoundException if another error occurs
	 */
	public static <T, E> T invokePrivateMethod(E instance, Class<? super E> declaringClass,
			String name, Class<?>[] parameterTypes, Object... args) throws MethodNotFoundException {
		return invokePrivateMethod(instance, declaringClass, new String[] { name },
				parameterTypes, args);
	}
	
	/**
	 * Invokes a private method, arranged conveniently. This one allows you to
	 * pass multiple possible method names, useful for obfuscation.
	 * 
	 * @param instance
	 *            This is the object whose method we're invoking.
	 * @param declaringClass
	 *            This is the declaring class of the method we want. Use a class
	 *            literal and not getClass(). This argument is necessary because
	 *            Class.getMethods() only returns public methods, and
	 *            Class.getDeclaredMethods() requires the declaring class.
	 * @param names
	 *            The multiple possible method names of the method we want to
	 *            invoke
	 * @param parameterTypes
	 *            The types of the parameters of the method (because
	 *            overloading).
	 * @param args
	 *            The arguments we want to pass to the method.
	 * @return Whatever the invoked method returns
	 * @throws SecurityException if a security error occurs
	 * @throws MethodNotFoundException if another error occurs
	 */
	@SuppressWarnings("unchecked")
	public static <T, E> T invokePrivateMethod(E instance, Class<? super E> declaringClass, String[] names, Class<?>[] parameterTypes, Object... args) throws MethodNotFoundException {
		for (String name : names) {
			try {
				Method method = declaringClass.getDeclaredMethod(name, parameterTypes);
				method.setAccessible(true);
				try {
					
					return (T) method.invoke(instance, args);
				} catch (Exception e) {
					throw new MethodNotFoundException("Error invoking private method", e);
				}
			} catch (NoSuchMethodException nsme) {
				continue;
			}
		}
		throw new MethodNotFoundException("Cannot find method: " + Arrays.toString(names));
	}
	/**
	 * Set the value of a private field. This one allows you to pass multiple
	 * field names (useful for obfuscation).
	 * 
	 * @param arg
	 *            The object whose field we're setting.
	 * @param clazz
	 *            The declaring class of the private field. Use a class literal
	 *            and not getClass(). This might be a superclass of the class of
	 *            the object.
	 * @param value
	 *            The value we're assigning to the field.
	 * @param name
	 *            The field name.
	 * @throws SecurityException If a security error occurred
	 * @throws FieldNotFoundException If some other error occurred
	 */
	public static <E> void setPrivateField(E instance, Class<? super E> declaringClass, Object value, String... names){
		for (String name : names) {
			try {
				Field field = declaringClass.getDeclaredField(name);
				field.setAccessible(true);
				try {
					field.set(instance, value);
					return;
				} catch (Exception e) {
					throw new FieldNotFoundException("Error setting field", e);
				}
			} catch (NoSuchFieldException nsfe) {
				continue;
			}
		}
		throw new FieldNotFoundException("Names not found: " + Arrays.toString(names));
	}
}

package assignment7;

/*  EE422C Project 7 submission by
 *  2018/Dec/03
 *  <Daniel Schmekel>  
 *  <ds52427>    
 *  <Cole Morgan>  
 *  <cm55332>  
 *  Slip days used: <0>*
 *  Github: https://github.com/EE422C-Fall-2018/project-7-chat-project-7-pair-10/tree/master
 *  
 *  Fall 2018
 *  
 *  Describe here known bugs or issues in this file. 
 *  If your issue spans multiplefiles, or you are not sure about details, add comments to the README.txt file.*/

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * The request class for invoking methods from other objects.
 * @author Daniel
 * 
 */
public class Request implements Serializable{
	private String function;
	private static Long serialVersionUID = 2L;
	private Object[] params;
	private Class<?>[] args;
	
	/**
	 * 
	 * @param functionName name of the function called.
	 * @param parameters an array of parameters for the function.
	 * @param the types of arguments the function accecpts.
	 */
	public Request(String functionName, Object[] parameters, Class<?>[] args) {
		function = functionName;
		params = parameters;
		this.args = args;
		
	}
	
	public Request(String functionName, Object[] parameters) {
		this(functionName, parameters, toArgs(parameters));
	}
	
	public String getFunctionName() {
		return function;
	}
	
	/**
	 * Invokes the specified function of the object sent in.
	 * @param o The object which has the method which should be invoked.
	 * @return Will return what the function returned.
	 * @throws NoSuchMethodException 
	 * @throws SecurityException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	public Object invoke(Object o) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Method m  = o.getClass().getMethod(function, args);
		//Find the correct method.
		return m.invoke(o, params);	
	}
	
	private static Class<?>[] toArgs(Object[] params) {
		Class<?>[] args = new Class[params.length];
		for(int i = 0; i < params.length; ++i) {
			try{
				args[i] = params[i].getClass();
			}
			catch (NullPointerException e) {
				throw new NullPointerException("A class array must be passed as well to the constructor if an entry in the object array is null");
			}
		}
		return args;
	}
}
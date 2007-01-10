/*
 * Created on Sep 2, 2004
 */
package org.cip4.elk;

/**
 * Thrown when a JDFElementFactory cannot be loaded.
 * 
 * @author Claes Buckwalter (clabu@itn.liu.se)
 */
public class JDFElementFactoryLoaderException extends RuntimeException
{
    public JDFElementFactoryLoaderException(String message, Throwable cause)
    {
        super(message, cause);
    }
}

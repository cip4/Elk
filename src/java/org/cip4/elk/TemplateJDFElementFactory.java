/*
 * Created on Sep 3, 2004
 */
package org.cip4.elk;

import java.io.InputStream;

import org.cip4.jdflib.core.JDFElement;
import org.cip4.jdflib.core.JDFParser;

/**
 * An implementation of <code>JDFElementFactory</code> that uses template XML
 * files to create JDF elements. To be recognized by this factory the template
 * XML files must be in a package "jdf_templates" located in the classpath.
 * 
 * @author Claes Buckwalter (clabu@itn.liu.se)
 */
public class TemplateJDFElementFactory extends DefaultJDFElementFactory {

    protected TemplateJDFElementFactory() {
        super();
    }

    /**
     * Creates a new JDF element from a template file. If no template file is
     * found that matches the specified element name then the super class's
     * implementation of this method is used, see
     * {@link DefaultJDFElementFactory#createJDFElement(String) DefaultJDFElementFactory.createJDFElement(String)}.
     * <p>
     * When creating a JDF element the path to the template file used is
     * constructed from the specified element name using the following pattern:
     * </p>
     * <p>
     * <code>jdf_templates/<em>elementName</em>.xml</code>
     * </p>
     * For example, the template file used for creating an element of type "JDF"
     * would be "jdf_templates/JDF.xml".
     * 
     * @see DefaultJDFElementFactory#createJDFElement(String)
     */
    public JDFElement createJDFElement(String elementName) {
        // Build the template path and load the template from the classpath
        String templateFile = "jdf_templates/" + elementName + ".xml";
        InputStream stream = this.getClass().getClassLoader()
                .getResourceAsStream(templateFile);
        // If no template exists, use the default implementation
        if (stream == null) {
            return super.createJDFElement(elementName);
        }
        return new JDFParser().parseStream(stream).getRoot();
    }

}
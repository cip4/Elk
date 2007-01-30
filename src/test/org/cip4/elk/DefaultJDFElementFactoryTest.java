/*
 * Created on Sep 2, 2004
 */
package org.cip4.elk;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.cip4.jdflib.core.ElementName;
import org.cip4.jdflib.core.JDFElement;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFStopPersChParams;
import org.cip4.jdflib.node.JDFNode;
import org.cip4.jdflib.resource.JDFNotification;
import org.w3c.dom.Node;

/**
 * @author clabu
 */
public class DefaultJDFElementFactoryTest extends TestCase
{

    private static Logger log = Logger.getLogger(DefaultJDFElementFactoryTest.class);
    
    public void setUp() throws Exception
    {
        super.setUp();
        //System.setProperty("org.cip4.elk.JDFElementFactory", "org.cip4.elk.DefaultJDFElementFactory");
    }
    
    
    public void testCreateJDFElement()
    {
        JDFElementFactory factory = JDFElementFactory.getInstance();
        // Creates JDF
        JDFElement element = factory.createJDFElement(ElementName.JDF);        
        log.debug(element);
        assertTrue(element instanceof JDFNode);
        // Creates JMF
        element = factory.createJDFElement(ElementName.JMF);        
        log.debug(element);
        assertTrue(element instanceof JDFJMF);
        // Creates Notification
        JDFNotification notification = (JDFNotification) factory.createJDFElement(ElementName.NOTIFICATION);        
        log.debug(notification);
    }

    public void testCreateJDF()
    {
        JDFElementFactory factory = JDFElementFactory.getInstance();
        Node jdf = factory.createJDF();
        log.debug(jdf);        
        assertTrue(jdf instanceof JDFNode);
        assertTrue( ((JDFNode)jdf).isValid() );
    }

    public void testCreateJMF()
    {
        JDFElementFactory factory = JDFElementFactory.getInstance();
        Node jmf = factory.createJMF();
        log.debug(jmf);
        assertTrue(jmf instanceof JDFJMF);
        assertTrue( ((JDFJMF)jmf).isValid() );
    }

    public void testGetInstance()
    {        
        JDFElementFactory factory = JDFElementFactory.getInstance();
        assertTrue(factory instanceof JDFElementFactory);
        JDFElementFactory factory1 = JDFElementFactory.getInstance();
        JDFElementFactory factory2 = JDFElementFactory.getInstance();
        assertEquals(factory1, factory2);
        assertSame(factory1, factory2);
    }
    
    public void testCreateStopPersChParams()
    {        
        JDFElementFactory factory = JDFElementFactory.getInstance();
        assertTrue(factory instanceof JDFElementFactory);
        Object o = factory.createJDFElement("StopPersChParams");
        assertTrue(o instanceof JDFStopPersChParams);
        JDFStopPersChParams stopParams = (JDFStopPersChParams) o;
        stopParams.setChannelID("12134");
        stopParams.setURL("http://elk.itn.liu.se");
        System.out.println(stopParams);
    }
}

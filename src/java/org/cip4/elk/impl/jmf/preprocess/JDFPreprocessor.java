/*
 * Created on 2005-apr-20
 */
package org.cip4.elk.impl.jmf.preprocess;

import java.util.List;

import org.cip4.jdflib.core.JDFElement;
import org.cip4.jdflib.jmf.JDFCommand;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.node.JDFNode;

/**
 * An interface for a JDFPreprocessor. The intended use for classes implementing
 * this interface is to preprocess the JDF-file related to a
 * <em>SubmitQueueEntry</em> Command. The returned <em>Response</em> message
 * of an implementing class should be the resulting response <em>after</em> 
 * preprocessing.
 * 
 * TODO Better documentation
 * 
 * @author Ola Stering (olst6875@student.uu.se)
 * @version $Id: JDFPreprocessor.java 1512 2006-08-15 08:53:16Z prosi $
 */
public interface JDFPreprocessor {

    /**
     * Preprocesses the JDF file referenced in the incoming
     * <em>SubmitQueueEntry</em> command. It is up to the implementor if this
     * method sends any Acknowledge messages during the preprocessing. The
     * <em>Response</em> message should be complete, including return code
     * (indicating if the preprocessing was successful). 
     * 
     * @param command
     *            The incoming <em>SubmitQueueEntry</em> command.
     * @return the <em>Response</em> message after preprocessing.
     */
    public JDFResponse preProcessJDF(JDFCommand command);

    /**
     * Returns a list of all JDF process nodes of the specified process
     * <em>Type</em> with the specified <em>Status</em>.
     * 
     * @param jdf the JDFNode to get child Process nodes from.
     * @param status the status the node must have <code>null</code> if no
     *            restriction should be placed on node <em>Status</em>
     * @return a List of JDFNode elements representing process nodes of the
     *         specified <em>Type</em> (i.e. Approval)
     */
    public List getProcessNodes(JDFNode jdf, JDFElement.EnumNodeStatus status);
    /**
     * Checks if the <code>JDFNode</code> is ready to be executed. The method
     * checks the following:
     * <ul>
     * <li><em>JDF/Status</em> (also checks <em>JDF/StatusPool</em>) is '<em>Waiting</em>'.
     * </li>
     * <li><code>JDFNode</code>'s <em>Resources</em> are
     * <em>Available</em></li>
     * <li><em>JDF/@Activation</em> is Active </li>
     * </ul>
     * A <em>Notification</em> will be appended to the <em>Response</em>
     * (alternatively to the <em>JDF/AutditPool</em>) if any of these are
     * requirements above are not fulfilled.
     * 
     * @param jdf the jdf to be checked.
     * @param response The <em>Response</em> to which the error <em>Notification</em>
     *            will be appended. Use <code>null</code> if the error
     *            <em>Notification</em> should be appended to
     *            <em>JDF/AuditPool</em> instead.
     * @return <code>true</code> if the JDF ready to execute,
     *         <code>false</code> otherwise.
     */
    public boolean isExecutableAndAvailbleResources(JDFNode jdf,JDFResponse response) ;

}

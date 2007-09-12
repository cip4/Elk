/*
 * Created on Aug 29, 2006
 */
package org.cip4.elk.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.cip4.elk.JDFElementFactory;
import org.cip4.jdflib.auto.JDFAutoNotification.EnumClass;
import org.cip4.jdflib.core.ElementName;
import org.cip4.jdflib.core.JDFComment;
import org.cip4.jdflib.core.JDFElement;
import org.cip4.jdflib.core.JDFResourceLink;
import org.cip4.jdflib.core.JDFElement.EnumNodeStatus;
import org.cip4.jdflib.datatypes.JDFAttributeMap;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.node.JDFNode;
import org.cip4.jdflib.resource.JDFNotification;

/**
 * A utility class from extracting process-related information from a JDF instance.
 * 
 * @version $Id$
 * @author Claes Buckwalter (clabu@itn.liu.se)
 */
public final class JDFUtil {

    private static Logger log = Logger.getLogger("JDFUtil");

    private JDFUtil() {
    }

    /**
     * Checks if the <code>JDFNode</code> is ready to be executed and appends
     * a <em>Notification</em> to the specified JMF <em>Response\</em>. The
     * intention is that the response can be used as a reply to a
     * <em>SubmitQueueEntry</em> JMF <em>Command</em>.
     * 
     * The following checks are performed:
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
     * @param jdf
     *            the jdf to be checked.
     * @param response
     *            The <em>Response</em> to which the error
     *            <em>Notification</em> will be appended. Use
     *            <code>null</code> if the error <em>Notification</em>
     *            should be appended to <em>JDF/AuditPool</em> instead.
     * @return <code>true</code> if the JDF ready to execute,
     *         <code>false</code> otherwise.
     */
    public static boolean isExecutableAndAvailbleResources(final JDFNode jdf,
            final JDFResponse response) {
        String msg = null;
        boolean isExecutable = true;

        JDFNotification notification = (JDFNotification) JDFElementFactory
                .getInstance().createJDFElement(ElementName.NOTIFICATION);
        notification.setClass(EnumClass.Error); // Required attribute
        // notification.setAuthor(this.getClass().getName());
        notification.setTimeStamp(null);

        String activationStr = jdf.getActivation(false).getName();
        if (activationStr.length() != 0
                && !activationStr.equals(JDFNode.EnumActivation.Active
                        .getName())) {
            msg = "The Activation of the JDFNode must be 'Active' or not "
                    + "given in order to execute. The Activation was '"
                    + jdf.getActivation(true).getName() + "'. ";
            log.info(msg);
            notification.appendComment().appendText(msg);
            isExecutable = false;
        }
        EnumNodeStatus status = jdf.getPartStatus(null);
        if (!status.equals(EnumNodeStatus.Waiting)) {
            msg = "The Status of the must be JDFNode 'Waiting' in order to"
                    + " execute. The Status was '" + jdf.getStatus().getName()
                    + "'. ";
            log.info(msg);
            notification.appendComment().appendText(msg);
            isExecutable = false;
        }

        if (false) { // XXXif (!jdf.isExecutable(emptyAttributeMap, false)) {
            msg = "The JDFNode with id '" + jdf.getID()
                    + "' is NOT executable due to 'Unavailable' resources. ";
            List l = getNonExecutableResources(jdf);
            msg += "The resources that are not executable are " + l;
            log.info(msg);
            notification.appendComment().appendText(msg);
            isExecutable = false;
        }

        if (!isExecutable) {
            if (response == null) {
                msg = "The Status of the JDFNode was set to 'Aborted'.";
                notification.appendComment().appendText(msg);
                jdf.setStatus(EnumNodeStatus.Aborted);
                jdf.getCreateAuditPool().copyElement(notification, null);
            } else {
                response.copyElement(notification, null);
            }
        }
        return isExecutable;
    }

    /**
     * Returns a list of non-executable Input <em>Resource</em>s for the
     * specified <code>JDFNode</code>, empty list if all Input
     * <em>Resource</em>s are executable.
     * 
     * TODO Add support for Notification messages in the method.
     * 
     * @param jdf
     *            The JDF Node which <em>Resource</em>s are being checked.
     * @return a list of non-executable <em>Resource</em>s for the specified
     *         <code>JDFNode</code>, empty list if all <em>Resource</em>s
     *         are executable.
     * @throws NullPointerException
     *             if jdf is <code>null</code>.
     */
    public static List getNonExecutableResources(final JDFNode jdf) {
        JDFAttributeMap m = new JDFAttributeMap();
        List l = new Vector();
        m.put("Usage", "Input");
        List inputResources = jdf.getResourceLinks(m);

        for (int i = 0, imax = inputResources.size(); i < imax; i++) {
            JDFResourceLink res = (JDFResourceLink) inputResources.get(i);
            if (res.isExecutable(new JDFAttributeMap(), false)) {
                log.debug("ResourceLink " + (i + 1)
                        + " allows the node to execute");
            } else {
                l.add(res);
                if (log.isDebugEnabled()) {
                    log.debug("ResourceLink " + (i + 1)
                            + " disallows the node to execute");
                    log.debug("Resource " + res);
                }
            }
        }
        return l;
    }

    /**
     * Returns a list of all JDF process nodes of the specified process
     * <em>Type</em> with the specified <em>Status</em>.
     * 
     * @param processType
     *            the process type
     * @param jdf
     *            the JDFNode to get child Process nodes from.
     * @param status
     *            the status the node must have <code>null</code> if no
     *            restriction should be placed on node <em>Status</em>
     * @return a List of JDFNode elements representing process nodes of the
     *         specified <em>Type</em> (i.e. Approval)
     */
    public static List getProcessNodes(final String processType,
            final JDFNode jdf, final JDFElement.EnumNodeStatus status) {
        log.debug("Searching for '" + processType + "' in JDF '" + jdf.getID()
                + "'...");
        List processNodes = new ArrayList();
        if (jdf.getAttribute("Type").equals(processType)) {
            // The JDF was a leaf node.
            if (status == null || jdf.getStatus().equals(status)) {
                processNodes.add(jdf);
            }
        } else {
            JDFAttributeMap attr = new JDFAttributeMap();
            attr.put("Type", processType);
            if (status != null) {
                attr.put("Status", status.getName());
            }
            processNodes = jdf.getChildrenByTagName(ElementName.JDF, null,
                    attr, false, true, 0);
        }
        return processNodes;
    }

    /**
     * Return 0 (success) if at least one <em>JDFNode</em> can be handled by
     * the specified Process and it is executable (see
     * {@link PreFlightJDF#isExecutableAndAvailbleResources(JDFNode, JDFResponse)})
     * AND the JDFNode/Template="false". If the <em>JDFNode</em> contains no
     * processable nodes or no executable nodes or it is a Template, a
     * <em>Notification</em> will be appended to the incoming
     * <em>Response</em> message.
     * 
     * Clarification: A Node is processable if it contains a JDF/@Type that this
     * process can handle. A Node is executable if it is in an executable state
     * (see
     * {@link PreFlightJDF#isExecutableAndAvailbleResources(JDFNode, JDFResponse)}).
     * 
     * @param processType
     *            the process type
     * @param jdf
     *            The <em>JDFNode</em> that is being checked for executable
     *            nodes.
     * @param response
     *            The Response message to which a <em>Notification</em> will
     *            be appended on failure, on success the <em>Response</em> is
     *            unmodified.
     * @return 0 on success, 6 otherwise (Invalid parameter)
     */
    public static int processHandles(final String processType,
            final JDFNode jdf, final JDFResponse response) {
        int returnCode = 0;
        List processNodes = JDFUtil.getProcessNodes(processType, jdf, null);

        if (jdf.getTemplate()) {
            String msg = "The JDFNode is a Template. It is not being executed.";
            returnCode = 1; // Invalid parameter
            appendNotification(response, EnumClass.Warning,
                    returnCode, msg);
            log.warn(msg);
            returnCode = 1; // General error
        } else if (processNodes == null || processNodes.size() == 0) {
            String msg = "Could not execute process because there were no process nodes of type '"
                    + processType + "' to execute.";
            returnCode = 6; // Invalid parameter
            appendNotification(response, EnumClass.Warning,
                    returnCode, msg);
            log.warn(msg);
        } else { // At least one Node of correct Type.
            boolean continueExecution = false;
            for (int i = 0; i < processNodes.size(); i++) {
                if (isExecutableAndAvailbleResources((JDFNode) processNodes
                        .get(i), response)) {
                    continueExecution = true;
                }
            }

            if (!continueExecution) { // No executable Node.
                log.info("No process Nodes for JDFNode with id '" + jdf.getID()
                        + "' ready for execution, processing aborted."
                        + " For details see Response/Notification.");
                returnCode = 1; // General error.
            }
        }
        return returnCode;
    }

    /**
     * Appends a <em>Notification</em> element to a <em>Response</em>
     * element. If there already exists a <em>Notification</em> a
     * <em>Comment</em> containing the specified message is added to the
     * <em>Notification</em>.
     * 
     * @param response
     *            the response to append the notification to
     * @param notClass
     *            the class of the notification
     * @param returnCode
     *            the return code
     * @param msg
     *            a message that will be appended as a comment to the
     *            notification
     */
    public static void appendNotification(JDFResponse response,
            JDFNotification.EnumClass notClass, int returnCode, String msg) {
        response.setReturnCode(returnCode);
        final JDFNotification notification; 
        if (response.getNotification() == null) {
            notification = response.appendNotification();
        } else {
            notification = response.getNotification();            
        }
        notification.setClass(notClass);
        final JDFComment comment = notification.appendComment();
        comment.appendText(msg);
    }
    
}

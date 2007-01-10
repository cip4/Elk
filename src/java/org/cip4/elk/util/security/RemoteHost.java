/**
 * Created on Apr 5, 2006, 6:47:20 PM
 * org.cip4.elk.util.security.RemoteHost.java
 * Project Name: Elk
 */
package org.cip4.elk.util.security;

import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;

/**
 * This class is used to pass remote host information to the
 * authentication handler.
 * 
 * Implemented to allow Host name verification at the
 * AuthenticationHandler level.
 * 
 * Should be modified to incllude relevant information
 *
 * @author Markus Nyman, (markus@myman.se)
 * 
 */
public class RemoteHost implements Serializable {

	private static final long serialVersionUID = 938271532325L;
	
	private String _hostname;
	
	public RemoteHost(String hostname) {
		_hostname = hostname;
	}
	
	public RemoteHost(HttpServletRequest request) {
		_hostname = request.getRemoteHost() + " [" + request.getRemoteUser()
		+ "@" + request.getRemoteAddr() + ":"  + request.getRemotePort() + "]";
	}
	
	public String getHostName() {
		return _hostname;
	}
	
	public String toString() {
		return _hostname;
	}
	
	public boolean equals(RemoteHost otherHost) {
		return otherHost.getHostName().equals(_hostname);
	}
}

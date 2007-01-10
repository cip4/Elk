/**
 * Created on Mar 22, 2006, 5:18:34 PM
 * org.cip4.elk.util.security.DefaultAuthenticationGUI.java
 * Project Name: Elk
 */
package org.cip4.elk.util.security;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * TODO JAVADOC
 *
 * @author Markus Nyman, (markus@myman.se)
 * 
 */
public class DefaultAuthenticationGUI extends JFrame implements AuthenticationUI, ActionListener {

	private static final long serialVersionUID = 92749271L;
	
	private Log log = LogFactory.getLog(this.getClass().getName());
	
	private JTextArea certArea;
	private JTextArea messageArea;
	private Vector _trustEntries;
	private int currentCert = 0;
	private AuthenticationHandler _authHandler = null;
	
	
	public DefaultAuthenticationGUI(String name) {
		super("Authentication GUI - " + name);
		log.debug("This should be the Swing thread: " + Thread.currentThread().getName());
		//trustEntries = new LinkedList();
	}
	
	
	public void setAuthenticationHandler(AuthenticationHandler authHandler) {
		_authHandler = authHandler;
	}
	
	public void notifyMessage(String message) {
		log.debug("NM: This should be the Swing thread: " + Thread.currentThread().getName());
		messageArea.append("\n" + message);
	}
	
	// TODO synchronization!!!
	public void notifyTrustEntry(TrustEntry trustEntry) {
		log.debug("NTE: This should be the Swing thread: " + Thread.currentThread().getName());
//		trustEntries.add(trustEntry);
//		if (trustEntries.size() <= 1) {
//			certArea.setText(trustEntry.toString());
//		}
		currentCert = _trustEntries.size();
		updateCertField();
	}
	
	// TODO make sure called on startup!
	public void setTrustEntries(Vector trustEntries) {
		_trustEntries = trustEntries;
	}
	
	public void updateCertField() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				log.debug("This should be the Swing thread: INIT " + Thread.currentThread().getName());
				if (_trustEntries.size() < 1) {
					certArea.setText("No certificates pending.");
				} else {
					TrustEntry trustEntry = (TrustEntry)_trustEntries.get(currentCert-1);
					certArea.setText(trustEntry.toString());
					messageArea.append(getStatusString());
				}
			}
		});

	}
	
	public String getStatusString() {
		StringBuffer buf = new StringBuffer();
		buf.append("\nStatus:");
		buf.append(" Vector size: " + _trustEntries.size());
		buf.append(" Current cert: " + currentCert);
		//buf.append("\n");
		return buf.toString();
	}
	
	public void clear() {
		log.debug("This should be the Swing thread: " + Thread.currentThread().getName());

		certArea.setText("Nothing pending");
	}
	
	public void init() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				log.debug("This should be the Swing thread: INIT " + Thread.currentThread().getName());
				createAndShowGUI();
			}
		});

	}
	
	
	
	public void acceptCertificate() {
		TrustEntry trustEntry = (TrustEntry)_trustEntries.get(currentCert-1);
		trustEntry.setLocalStatus(TrustEntry.CERT_ACCEPTED);
		refresh();
	}
	
	public void rejectCertificate() {
		TrustEntry trustEntry = (TrustEntry)_trustEntries.get(currentCert-1);
		trustEntry.setLocalStatus(TrustEntry.CERT_REVOKED);
		refresh();		
	}
	
	public void certificateExpired() {
		TrustEntry trustEntry = (TrustEntry)_trustEntries.get(currentCert-1);
		trustEntry.setLocalStatus(TrustEntry.CERT_EXPIRED);
		refresh();				
	}
	
	public void hostNameMismatch() {
		TrustEntry trustEntry = (TrustEntry)_trustEntries.get(currentCert-1);
		trustEntry.setLocalStatus(TrustEntry.CERT_HOSTNAME_MISMATCH);
		refresh();					
	}
	
	public void delete() {
		if (_trustEntries.size() > 0) {
			_authHandler.deleteTrustEntry((TrustEntry)_trustEntries.get(currentCert-1));
			currentCert = _trustEntries.size();
			refresh();
		}
	}
	
	
	public void refresh() {
		//TrustEntry trustEntry = (TrustEntry)_trustEntries.get(currentCert-1);
		//((DefaultTrustEntry)trustEntry).sendRemoteQuery();
		updateCertField();
	}
	
	public void previous() {
		messageArea.append(getStatusString());
		if (currentCert > 1) {
			currentCert--;
			updateCertField();
		}
	}
	
	public void next() {
		messageArea.append(getStatusString());
		if (currentCert < _trustEntries.size()) {
			currentCert++;
			updateCertField();
		}		
	}
	
	// TODO are these thread safe?
	public void showGUI() {
		log.debug("Showing GUI.");
		if (!this.isShowing())
			this.setVisible(true);
	}
	
	public void hideGUI() {
		log.debug("Hiding GUI.");
		if (this.isShowing())
			this.setVisible(false);
	}
	
	

	
	public void actionPerformed(ActionEvent event) {

		log.debug("This should be the Swing thread: " + Thread.currentThread().getName());

		if (_trustEntries.size() < 1) {
			log.debug("No cert in progress.");
			return;
		}
		
		if(event.getActionCommand().equals("accept")) {
			acceptCertificate();		
		}		
		else if (event.getActionCommand().equals("revoke")) {
			rejectCertificate();
		}
		else if (event.getActionCommand().equals("expired")) {
			certificateExpired();
		}
		else if (event.getActionCommand().equals("mismatch")) {
			hostNameMismatch();
		}
		else if (event.getActionCommand().equals("delete")) {
			delete();
		}
		else if (event.getActionCommand().equals("refresh")) {
			refresh();
		}
		else if (event.getActionCommand().equals("prev")) {
			previous();
		}
		else if (event.getActionCommand().equals("next")) {
			next();
		}
		else
			log.debug("Unknown action command");
	}

	
	
	public void createAndShowGUI() {
		log.debug("This should be the Swing thread: " + Thread.currentThread().getName());
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		JPanel mainpane = new JPanel(new BorderLayout());
		JPanel buttonpane = new JPanel(new FlowLayout());
		certArea = new JTextArea(20,80);
		messageArea = new JTextArea(2,80);
		JScrollPane certPane = new JScrollPane(certArea);
		JScrollPane messagePane = new JScrollPane(messageArea);
		// TODO Check fonts!!!
		certArea.setFont(new Font("Courier", Font.PLAIN, 10));
		messageArea.setFont(new Font("Courier", Font.PLAIN, 10));
		
		JButton yesButton = 	new JButton(" Accept ");
		JButton noButton = 		new JButton(" Revoke ");
		JButton expiredButton = new JButton("Expired ");
		JButton hostMMButton =	new JButton("Mismatch");
		JButton deleteButton =  new JButton(" Delete ");
		JButton refreshButton = new JButton("Refresh ");
		JButton previousButton =new JButton("  Prev  ");
		JButton nextButton = 	new JButton("  Next  ");

		yesButton.setActionCommand("accept");
		noButton.setActionCommand("revoke");
		expiredButton.setActionCommand("expired");
		hostMMButton.setActionCommand("mismatch");
		deleteButton.setActionCommand("delete");
		refreshButton.setActionCommand("refresh");
		previousButton.setActionCommand("prev");
		nextButton.setActionCommand("next");
		
		yesButton.addActionListener(this);
		noButton.addActionListener(this);
		expiredButton.addActionListener(this);
		hostMMButton.addActionListener(this);
		deleteButton.addActionListener(this);
		refreshButton.addActionListener(this);
		previousButton.addActionListener(this);
		nextButton.addActionListener(this);

		buttonpane.add(yesButton);
		buttonpane.add(noButton);
		buttonpane.add(expiredButton);
		buttonpane.add(hostMMButton);
		buttonpane.add(deleteButton);
		buttonpane.add(refreshButton);
		buttonpane.add(previousButton);
		buttonpane.add(nextButton);
		
		mainpane.add(messagePane, BorderLayout.NORTH);
		mainpane.add(certPane, BorderLayout.CENTER);
		mainpane.add(buttonpane, BorderLayout.SOUTH);
		
		getContentPane().add(mainpane);
		
		pack();
		setVisible(false);
		
	}

}

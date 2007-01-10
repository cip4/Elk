package org.cip4.elk.helk.servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cip4.elk.impl.spring.ElkSpringConfiguration;
import org.cip4.elk.queue.Queue;
import org.cip4.jdflib.jmf.JDFQueue;
import org.springframework.beans.factory.BeanFactory;

/**
 * Servlet implementation class for Servlet: QueueServlet
 * 
 */
public class QueueServlet extends javax.servlet.http.HttpServlet implements
        javax.servlet.Servlet {

    private static final long serialVersionUID = 2618389753635137602L;
    protected BeanFactory _factory;
    protected Queue _queue;

    public QueueServlet() {
        super();
    }

    public void init() throws ServletException {
        super.init();

        initBeanFactory();
        initQueue();
    }

    private void initQueue() {
        _queue = (Queue) _factory.getBean("queue");
    }

    private void initBeanFactory() {
        _factory = ElkSpringConfiguration.getBeanFactory();
    }

    /*
     * (non-Java-doc)
     * 
     * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest request,
     *      HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        String command = request.getParameter("cmd");

        if (command == null || command.length() == 0) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        if (command.equals("showQueue")) {
            showQueue(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unkonwn command.");
        }
    }

    private void showQueue(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        JDFQueue q = _queue.getQueue();
        req.setAttribute("queue", q);
        req.getRequestDispatcher("/queue/showQueue.jsp").forward(req, res);
    }

    /*
     * (non-Java-doc)
     * 
     * @see javax.servlet.http.HttpServlet#doPost(HttpServletRequest request,
     *      HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        // TODO Auto-generated method stub
    }
}
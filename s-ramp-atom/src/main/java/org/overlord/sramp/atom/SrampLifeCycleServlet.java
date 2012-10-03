package org.overlord.sramp.atom;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.overlord.sramp.repository.PersistenceFactory;

/**
 * Servlet implementation class SrampLifeCycleServlet
 */
public class SrampLifeCycleServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SrampLifeCycleServlet() {
        super();
        // TODO Auto-generated constructor stub
    }
    
    @Override
    public void destroy() {
        PersistenceFactory.newInstance().shutdown();
        super.destroy();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}
	
	public String getBindingInfo() {
	    return getServletContext().getServerInfo();
	}

}

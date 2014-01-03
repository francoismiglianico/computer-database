package com.formation.computerdb.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.formation.computerdb.service.DataService;

/**
 * Servlet called for deletion of a computer
 * @author F. Miglianico
 *
 */
@WebServlet("/deleteComputer")
public class DeleteComputerServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {

		// Id of computer
		Long id = Long.valueOf(request.getParameter("id"));
		if(id == null) {
			response.sendRedirect("dashboard");
			return;
		}
		
		DataService ds = DataService.getInstance();
		
		ds.deleteComputer(id.intValue());

		response.sendRedirect("dashboard?message=deleteOK");

	}
}
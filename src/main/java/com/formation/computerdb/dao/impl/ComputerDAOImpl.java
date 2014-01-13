package com.formation.computerdb.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.formation.computerdb.common.OrderByColumn;
import com.formation.computerdb.dao.ComputerDAO;
import com.formation.computerdb.dao.DAOFactory;
import com.formation.computerdb.domain.Company;
import com.formation.computerdb.domain.Computer;
import com.formation.computerdb.domain.Page;

/**
 * Implementation of the interface @ComputerDAO
 * @author F. Miglianico
 *
 */
@Repository
public class ComputerDAOImpl implements ComputerDAO {
	
	private static Logger log = LoggerFactory.getLogger(ComputerDAOImpl.class);
	
	@Autowired
	private DAOFactory daoFactory;
	
	protected ComputerDAOImpl() {
	}

	/**
	 * Get a computer
	 * @param id the id
	 */
	public Computer get(int id) {

		Computer computer = null;
		ResultSet rs = null;
		
		Connection conn = daoFactory.getConn();

		try {

			// Build the query
			StringBuilder query = new StringBuilder("SELECT cr.id, cr.name, cr.introduced, cr.discontinued, cy.id, cy.name ");
			query.append("FROM computer AS cr ");
			query.append("LEFT JOIN company AS cy ON cr.company_id = cy.id ");
			query.append("WHERE cr.id = ").append(id);

			// Create hte statement
			Statement statement = conn.createStatement();
			rs = statement.executeQuery(query.toString());

			// Process the result
			if(rs.next())
				computer = mapComputer(rs);

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (rs != null)
					rs.close();
			} catch (SQLException e) {
				log.error("Error in finally: " + e.getMessage());
				e.printStackTrace();
			}
		}

		return computer;
	}
	
	/**
	 * Create a computer in DB
	 */
	public void create(Computer computer) throws SQLException{
		
		Connection conn = daoFactory.getConn();

		String query = "INSERT INTO computer (name, company_id, introduced, discontinued) VALUES (?, ?, ?, ?)";
		
		PreparedStatement statement = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
		statement.setString(1, computer.getName());
		if(computer.getCompany() != null)
			statement.setLong(2, computer.getCompany().getId());
		else
			statement.setNull(2, java.sql.Types.BIGINT);
		
		if(computer.getIntroduced() != null)
			statement.setTimestamp(3, new Timestamp(computer.getIntroduced().getMillis()));
		else
			statement.setNull(3, java.sql.Types.TIMESTAMP);
		
		if(computer.getDiscontinued() != null)
			statement.setTimestamp(4, new Timestamp(computer.getDiscontinued().getMillis()));
		else
			statement.setNull(4, java.sql.Types.TIMESTAMP);
		
		statement.executeUpdate();
		
		ResultSet rs = statement.getGeneratedKeys();
		Long id = null;
		
		if (rs.next()) {
	        id = rs.getLong(1);
	        computer.setId(id);
	    } else {
	    	log.error("Could not retrieve id after creating a computer");
	    }
	    rs.close();
	    rs = null;
	}
	
	/**
	 * Updates the computer in DB.
	 * The id needs to be set.
	 */
	public void update(Computer computer) throws SQLException {

		Connection conn = daoFactory.getConn();
		
		if(computer.getId() == null) {
			log.error("Trying to update a computer without id : " + computer.toString());
			return;
		}
			
		// Build query
		StringBuilder query = new StringBuilder("UPDATE computer SET name = ?, company_id = ?, introduced = ?, discontinued = ? ");
		query.append("WHERE id = ").append(computer.getId());
		
		// Create prepared statement
		PreparedStatement statement = conn.prepareStatement(query.toString());
		statement.setString(1, computer.getName());
		
		// Set variables
		if(computer.getCompany() != null)
			statement.setLong(2, computer.getCompany().getId());
		else
			statement.setNull(2, java.sql.Types.BIGINT);

		if(computer.getIntroduced() != null)
			statement.setTimestamp(3, new Timestamp(computer.getIntroduced().getMillis()));
		else
			statement.setNull(3, java.sql.Types.TIMESTAMP);
		
		if(computer.getDiscontinued() != null)
			statement.setTimestamp(4, new Timestamp(computer.getDiscontinued().getMillis()));
		else
			statement.setNull(4, java.sql.Types.TIMESTAMP);
	
		
		// Execute query
		statement.executeUpdate();
	}
	
	/**
	 * Delete computer with id set as parameter
	 * @param id the id
	 */
	public void delete(int id) throws SQLException {
		
		Connection conn = daoFactory.getConn();
			
		String query = "DELETE FROM computer WHERE id = " + id;

		Statement statement = conn.createStatement();
		statement.executeUpdate(query.toString());
	}

	/**
	 * {@inheritDoc}
	 */
	public void fill(Page page) {
		
		Connection conn = daoFactory.getConn();

		List<Computer> computers = new ArrayList<Computer>();
		ResultSet rs = null;

		try {
			
			// Build query, depends on the page parameters
			StringBuilder query = new StringBuilder("SELECT cr.id, cr.name, cr.introduced, cr.discontinued, cy.id, cy.name ");
			query.append("FROM computer AS cr ");
			query.append("LEFT JOIN company AS cy ON cr.company_id = cy.id ");
			
				// Search parameter
			String search = page.getSearch();
			
			if(search != null) {
				query.append("WHERE cr.name LIKE '%").append(search).append("%' ");
				query.append("OR cy.name LIKE '%").append(search).append("%' ");
			}
			
				// Order By parameter
			OrderByColumn orderBy = page.getOrderBy();
			if(orderBy != null)
				query.append("ORDER BY ").append(orderBy.getColNumber()).append(" ").append(orderBy.getDir()).append(" ");
			
				// Number of rows parameter
			Integer nbRows = page.getNbRows();
			if(nbRows != null) {
				query.append("LIMIT ");
				Integer currPage = page.getCurrPage();
				if(currPage != null) {
					int offset = (currPage - 1) * nbRows;
					query.append(offset).append(", ");
				}
				query.append(nbRows);
			}

			// Create statement
			Statement statement = conn.createStatement();
			
			// Execute statement
			rs = statement.executeQuery(query.toString());

			// Traitement a faire ici sur le resultset
			while(rs.next())
				computers.add(mapComputer(rs));

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (rs != null)
					rs.close();
			} catch (SQLException e) {
				log.error("Error in finally: " + e.getMessage());
				e.printStackTrace();
			}
		}
		
		// Store the resulting list of computers in the page
		page.setList(computers);
	}
	
	/**
	 * Counts the number of computers matching the search criteria.
	 * If search is empty, counts all the computers
	 */
	public int count(String search) {

		ResultSet rs = null;
		Long count = null;
		
		Connection conn = daoFactory.getConn();

		try {

			// Build query
			StringBuilder query = new StringBuilder();
			if(search == null) {
				query.append("SELECT COUNT(id) FROM computer");
			} else {
				query.append("SELECT COUNT(cr.id) ");
				query.append("FROM computer AS cr ");
				query.append("LEFT JOIN company AS cy ON cr.company_id = cy.id ");
				query.append("WHERE cr.name LIKE '%").append(search).append("%' ");
				query.append("OR cy.name LIKE '%").append(search).append("%'");
			}

			// Create statement
			Statement statement = conn.createStatement();
			
			// Execute query
			rs = statement.executeQuery(query.toString());

			// Traitement a faire ici sur le resultsets
			if(rs.next())
				count = rs.getLong(1);

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (rs != null)
					rs.close();
			} catch (SQLException e) {
				log.error("Error in finally: " + e.getMessage());
				e.printStackTrace();
			}
		}
		return (count == null) ? 0 : count.intValue();
	}

	/**
	 * Map a resultset to a computer
	 * @param resultSet the resultset
	 * @return the computer
	 */
	private Computer mapComputer( ResultSet resultSet ) {
		
		if(resultSet == null)
			return null;
		
		Computer computer = new Computer();

		try {
			computer.setId(resultSet.getLong("cr.id"));
			computer.setName(resultSet.getString("cr.name"));
			Timestamp ts = resultSet.getTimestamp("cr.introduced");
			if(ts != null)
				computer.setIntroduced(new DateTime(ts));
			ts = resultSet.getTimestamp("cr.discontinued");
			if(ts != null)
				computer.setDiscontinued(new DateTime(ts));
			
			Company company = new Company();

			company.setId(resultSet.getLong("cy.id"));
			company.setName(resultSet.getString("cy.name"));
			
			computer.setCompany(company);

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return computer;
	}
}

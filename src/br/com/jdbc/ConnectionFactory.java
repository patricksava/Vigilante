package br.com.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionFactory {

	public Connection getConnection() {
	     try {
	         return DriverManager.getConnection(
	 "jdbc:postgresql://54.232.252.20:5432/vigilante", "postgres", "prajaa");
	     } catch (SQLException e) {
	         throw new RuntimeException(e);
	     }
	 }
	
}

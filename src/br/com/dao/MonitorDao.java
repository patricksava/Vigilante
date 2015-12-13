package br.com.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import br.com.jdbc.ConnectionFactory;

public class MonitorDao {

	private Connection connection;
	
    public MonitorDao() {
		   this.connection = new ConnectionFactory().getConnection();
	}

	public void CriarRegistro() {
		String sql = "insert into monitor_monitor " +
                "(" +
                "moment_capture," +
                "is_true," +
                "camera_id," +
                "is_verified"+
                ")" +
                " values (now(),false,1,false)";

        try {

            PreparedStatement stmt = connection.prepareStatement(sql);
           
            stmt.execute();
            stmt.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
	}
}

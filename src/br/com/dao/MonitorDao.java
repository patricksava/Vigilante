package br.com.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import br.com.jdbc.*;
import br.com.vigilante.*;

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
                "camera_id" +
                ")" +
                " values (now(),false,1)";

        try {

            PreparedStatement stmt = connection.prepareStatement(sql);
           
            stmt.execute();
            stmt.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
	}
}

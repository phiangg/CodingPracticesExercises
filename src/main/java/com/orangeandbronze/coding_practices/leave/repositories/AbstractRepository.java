package com.orangeandbronze.coding_practices.leave.repositories;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Connection;

public class AbstractRepository {

    protected final Connection conn;

    public AbstractRepository(){
        try{
            conn = DriverManager.getConnection("jdbc:hsqldb:leaves", "sa", "");
        } catch (SQLException e){
            throw new DataAccessException("Failed to connect to database", e);
        }
    }
}

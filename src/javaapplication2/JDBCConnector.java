package javaapplication2;

// Acknowledgments: This example is a modification of code provided 
// by Dimitri Rakitine.

// Usage from command line on key.csc.ncsu.edu: 
// see instructions in FAQ
// Website for Oracle setup at NCSU : http://www.csc.ncsu.edu/techsupport/technotes/oracle.php

//Note: If you run the program more than once, it will not be able to create the COFFEES table anew after the first run; 
//	you can remove the COFFEES tables between the runs by typing "drop table COFFEES;" in SQL*Plus.


import java.io.UnsupportedEncodingException;
import java.sql.*;

public class JDBCConnector {

	
    static final String jdbcURL 
	= "jdbc:oracle:thin:@ora.csc.ncsu.edu:1521:orcl";

    public  Connection conn;
    public  Statement stmt;
    public  ResultSet rs;
    

/*
    public static ResultSet select(String table) throws SQLException
    {
 	//  try{ connect();} catch (SQLException e){ e.printStackTrace();}
 	   rs = stmt.executeQuery("SELECT COF_NAME, PRICE FROM "+table);

 		// Now rs contains the rows of coffees and prices from
 		// the COFFEES table. To access the data, use the method
 		// NEXT to access all rows in rs, one row at a time

 		while (rs.next()) {
 		    String s = rs.getString("COF_NAME");
 		    float n = rs.getFloat("PRICE");
 		    System.out.println(s + "   " + n);
 		}
 		return rs;
    }
    */
    public  void connect() throws  SQLException, UnsupportedEncodingException
    {
            try {
				Class.forName("oracle.jdbc.driver.OracleDriver");
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            String user = "mkadaba";	// For example, "jsmith"
            String passwd = new String(hexStringToByteArray("73797374656d"),"ASCII");	;	// Your 9 digit student ID number
            conn = null;
            stmt = null;
            rs = null;
           	conn = DriverManager.getConnection(jdbcURL, user, passwd);
           	stmt = conn.createStatement();
            
    }
    

    
    
    //To crypt Password
    private static byte[] hexStringToByteArray(String s) {
		
	   int len = s.length();
	    byte[] data = new byte[len / 2];
	    for (int i = 0; i < len; i += 2) {
	        data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
	                             + Character.digit(s.charAt(i+1), 16));
	    }
	    return data;
	}
   public  void close()
   {
	   close(rs);
       close(stmt);
       close(conn);
   }
    static void close(Connection conn) {
        if(conn != null) {
            try { conn.close(); } catch(Throwable whatever) {}
        }
    }

    static void close(Statement st) {
        if(st != null) {
            try { st.close(); } catch(Throwable whatever) {}
        }
    }

    static void close(ResultSet rs) {
        if(rs != null) {
            try { rs.close(); } catch(Throwable whatever) {}
        }
    }
}


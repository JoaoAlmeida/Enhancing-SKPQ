package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DBConnection extends Object {

	private static Connection con;

	private static Statement stmt;

	private static boolean status = false;

	/*
	 * Iniciar conexao
	 */

	public static String getMsqlStringConnection() {
		String url = null;
		try {
			
			//Class.forName(IDatabaseConstants.DB_DRIVER);
			String more = "?verifyServerCertificate=false&useSSL=false&requireSSL=false&autoReconnect=true&failOverReadOnly=false&maxReconnects=1";
			
			url = "server=".concat(IDatabaseConstants.DB_IP_ADDRESS).concat(";")+
					     "user=".concat(IDatabaseConstants.DB_USERNAME).concat(";")+
					     "database=".concat(IDatabaseConstants.DB_SCHEMA).concat(";")+
					     "port=".concat(IDatabaseConstants.DB_PORT).concat(";")+
					     "password=".concat(IDatabaseConstants.DB_PASSWORD).concat(";");
					
		} catch (Exception e) {
			e.printStackTrace();
		}
		return url;
	}
	
	public static Connection getConnection() {
		try {
			status = true;
			Class.forName(IDatabaseConstants.DB_DRIVER);
			String more = "?verifyServerCertificate=false&useSSL=false&requireSSL=false&autoReconnect=true&failOverReadOnly=false&maxReconnects=1";
			String url = "jdbc:mysql://".concat(IDatabaseConstants.DB_IP_ADDRESS).concat("/")
					.concat(IDatabaseConstants.DB_SCHEMA)
					+ more;
			//Lodica.print(USERNAME);
			//Lodica.print(PASSWORD);
			//Lodica.print(url);
			con = DriverManager.getConnection(url, IDatabaseConstants.DB_USERNAME,  IDatabaseConstants.DB_PASSWORD);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return con;
	}

	public static void finalizarConexaoBD() {
		try {
			stmt.close();
			con.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Statement getStatement() {
		return stmt;
	}

	public static boolean getStatus() {
		return status;
	}

}
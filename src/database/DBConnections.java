package database;



public enum DBConnections {
	
	LOD {
		
		@Override
		public String getDbDriver() {
			return "com.mysql.jdbc.Driver";
		}
		
		@Override
		public String getDbServer() {
			return "mysql";
		}
		
		@Override
		public String getDbIpAddress() {
			return "localhost";
		}
		@Override
		public String getDbSchema() {
			return "lod";
		}
		
		@Override
		public String getDbUserName() {
			return "root";
		}
		
		@Override
		public String getDbPassword() {
			return "durao";
		}

	},;
	
	
	
	public abstract String getDbDriver();
	public abstract String getDbServer();
	public abstract String getDbIpAddress();
	public abstract String getDbSchema();
	public abstract String getDbUserName();
	public abstract String getDbPassword();
	
	
	public String DB_LOGGER_PROPERTY_CONFIGURATOR = "src/Database/logger.properties";

}


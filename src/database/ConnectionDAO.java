package database;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import node.IConstants;
import node.Node;
import similarity.LDSD;

public class ConnectionDAO implements Serializable {

	private static final long serialVersionUID = 1L;

	public static void main(String[] args) throws ClassNotFoundException, SQLException {
		ConnectionDAO conexaoDAO = new ConnectionDAO();
		conexaoDAO.calculateSimilarity("" + 1);
	}

	/**
	 * @return
	 * @throws ClassNotFoundException
	 */
	public boolean checkSemantics(String uri1, String uri2, String sim) throws ClassNotFoundException {
		boolean noexist = false;

		try {
			String query = "SELECT EXISTS(select * from `lod`.`semantic` as b where" + " (b.uri1 =  \"" + uri1
					+ "\" and b.uri2 =  \"" + uri2 + "\" and b.sim =  \"" + sim + "\") " + "  OR   " + "(b.uri1 =  \""
					+ uri2 + "\" and b.uri2 =  \"" + uri1 + "\" and b.sim =  \"" + sim + "\"))  ";

			Connection conn = DBConnection.getConnection();
			PreparedStatement ps = conn.prepareStatement(query);

			ResultSet rs = ps.executeQuery();
			while (rs != null && rs.next()) {
				noexist = rs.getBoolean(1);
			}
			ps.close();
			conn.close();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		return noexist;
	}

	public Set<Node> getBooks(String userId) throws ClassNotFoundException {
		Set<Node> nodes = new HashSet<Node>();
		Connection conn = null;
		try {
			String query = "select b.id, b.uri from `lod`.`book` as b  ";

			conn = DBConnection.getConnection();
			PreparedStatement ps = conn.prepareStatement(query);

			ResultSet rs = ps.executeQuery();
			while (rs != null && rs.next()) {
				Node node = new Node(String.valueOf(rs.getInt(1)), IConstants.LIKE, rs.getString(2).trim());
				nodes.add(node);
			}

			ps.close();
			conn.close();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}

		return nodes;
	}

	public void calculateSimilarity(String userId) throws SQLException, ClassNotFoundException {

		Set<Node> books = getBooks(userId);

		for (Node node1 : books) {
			for (Node node2 : books) {
				if (node1.getURI() != node2.getURI()
						&& !checkSemantics(node1.getURI(), node2.getURI(), IConstants.LDSD)) {
					Connection conn = null;
					String string = "INSERT INTO `lod`.`semantic` (`uri1`, `uri2`, `sim`, `score`) VALUES ( \""
							+ node1.getURI() + "\" , \"" + node2.getURI() + "\", \"" + IConstants.LDSD + "\", "
							+ LDSD.LDSDweighted(node1.getURI(), node2.getURI()) + " )";
					// Lodica.print(string);
					conn = DBConnection.getConnection();
					PreparedStatement ps = conn.prepareStatement(string);
					/*
					 * ps.setInt(1, modelo.getNumeracao()); ps.setString(2,
					 * modelo.getDescricao());
					 */
					ps.execute();
					ps.close();
					conn.close();
				}
			}
		}
	}

	/*
	 * public void insertModelo1( PersistenceModel modelo )throws SQLException,
	 * ClassNotFoundException{
	 * 
	 * Connection conn = null; StringBuilder sb = new StringBuilder();
	 * sb.append(" INSERT INTO modelo ") .append("(") .append(" numeracao ")
	 * .append(" , descricao) ") .append("  VALUES (?,?) ");
	 * 
	 * 
	 * 
	 * conn = DBConnection.getConnection(); PreparedStatement ps =
	 * conn.prepareStatement(sb.toString());
	 * 
	 * ps.setInt(1, modelo.getNumeracao()); ps.setString(2,
	 * modelo.getDescricao());
	 * 
	 * ps.execute(); ps.close(); conn.close(); }
	 */

	/*
	 * public int insertModelo2( Collection<PersistenceModel> modelos )throws
	 * SQLException, ClassNotFoundException{
	 * 
	 * Connection conn = null; StringBuilder sb = new StringBuilder();
	 * sb.append(" INSERT INTO Modelo ")
	 * 
	 * .append("(") .append(" numeracao ") .append(" , descricao) ") .append(
	 * "  VALUES (?,?) ");
	 * 
	 * 
	 * int count = 0;
	 * 
	 * 
	 * conn = DBConnection.getConnection(); PreparedStatement ps =
	 * conn.prepareStatement(sb.toString());
	 * 
	 * 
	 * for (PersistenceModel modelo : modelos) { ps.setInt(1,
	 * modelo.getNumeracao()); ps.setString(2, modelo.getDescricao());
	 * ps.execute(); count++; } ps.close();
	 * 
	 * 
	 * conn.close();
	 * 
	 * return count;
	 * 
	 * 
	 * }
	 */

}

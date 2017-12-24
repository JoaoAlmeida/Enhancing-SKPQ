
package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import node.Evaluation;
import node.IConstants;
import node.Node;
import node.NodePrediction;
import node.NodeUtil;
import node.SparqlWalk;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.ResourceImpl;

import util.StringUtilsNode;

/**
 *
 * @author freddurao
 */
public class DBFunctions {
	
// select count(*) as total, userid  FROM ((select distinct m.id, m.uri, ml.userid from music as m, music_like as ml where  m.id = ml.musicid )UNION ALL(select distinct m.id, m.uri,ml.userid from movie as m, movie_like as ml where  m.id = ml.movieid )UNION ALL(select distinct m.id, m.uri, ml.userid from book  as m, book_like  as ml where  m.id = ml.bookid  )  ) as x  group by userid order by total desc;
//	select sum(total) as total, userid  FROM ((select count(*) as total, ml.userid from music as m, music_like as ml where  m.id = ml.musicid group by userid order by total desc )  UNION ALL (select count(*) as total, ml.userid from movie as m, movie_like as ml where  m.id = ml.movieid group by userid order by total desc )   UNION ALL (select count(*) as total, ml.userid from book  as m, book_like  as ml where  m.id = ml.bookid group by userid order by total desc )  ) as x  group by userid order by total desc;

	private static boolean hasBefore = false;

	private static Connection conn;
	
	public static Connection getConnection(){
		conn = DBConnection.getConnection();
		return conn;
	}
	
/*	public static MysqlConnection getMySqlConnection(){
		MysqlConnection mysqlConnection = new MysqlConnection()
		conn = DBConnection.getConnection();
		return conn;
	}	*/
	
	public DBFunctions() {
    	if(StringUtilsNode.getMachineName().equalsIgnoreCase("pcdochefe")){
    		IDatabaseConstants.DB_PASSWORD =  "durao";
    	}
		conn = DBConnection.getConnection();
    }	
	
	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		for (int retries = 0;; retries++) {
			try {
				DBFunctions dbFunctions = new DBFunctions();
				dbFunctions.collectDomainStatitics();
			} catch (Exception e) {
				if (retries < 10000) {
					NodeUtil.print("restarting #"+retries);
					continue;
				} else {
					throw e;
				}
			}
		}
	}	
	
	
	
	public void collectDomainStatitics() {
		for (Integer userId : getUserIdsForLikedMoviesMusicsBooks()) {
			for (Node node : getUnionLikedMoviesMusicsBooksByUserIdAndConvertToNode(userId)) {
				List<Resource> resources = new ArrayList<Resource>();
				resources = SparqlWalk.getDBpediaObjecstBySubject(node.getURI());
				resources.addAll(SparqlWalk.getDBpediaSubjectsByObjects(node.getURI()));
				getStatisticsDomain(node, 1, resources);
			}
		}
	}

	/**
	 * Collects domain of neighbours
	 * 
	 * @param nodeOriginal
	 * @param iteration
	 * @param resources
	 */
	public void getStatisticsDomain(Node nodeOriginal, int iteration, List<Resource> resources) {
		// SELECT count(*) as total, domain FROM lod.domain group by domain order by total desc
		for (Resource resource : resources) {
			if (nodeOriginal.getURI().length()>255 || resource.getURI().length()>255) {
				NodeUtil.print(nodeOriginal.getURI());
				NodeUtil.print(resource.getURI());
				continue;
			}
			if (!checkDomain(nodeOriginal.getURI(), resource.getURI())) {
				String domain = SparqlWalk.getMostSpecificSubclasseOfDbpediaResource(resource.getURI()).getURI();
				insertDomain(nodeOriginal.getURI(), resource.getURI(), domain, (iteration + 1));
			}
		}
	}

	/**
	 * @param conn
	 * @param query
	 * @return
	 */
	public static int executeAndClose(Connection conn, String query) {
		boolean exec = false;
		PreparedStatement ps = null;
		try{
			try {
				ps = conn.prepareStatement(query);
				exec = ps.execute();
				ps.close();
				conn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		
		} finally {
			if (ps != null) {
				try {
				ps.close();
				} catch (SQLException e) {e.printStackTrace();}
			}			
			if (conn != null) {
				try {
				conn.close();
				} catch (SQLException e) {e.printStackTrace();}
			}
		}		
		
		if (exec) {
			return 1;
		} else {
			return 0;
		}
	}
	
	/**
	 * @param conn
	 * @param query
	 * @return
	 */
	public static void closeQuery(Connection conn,PreparedStatement ps) {
		try {
			ps.close();
			conn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}	
	
	
	/**
	 * @return
	 */
	public int insertOrUpdateEvaluation(String uri, int correct, int incorrect, boolean usePredictedClassification, int userId) {
		String sim = checkSimilarityMethod(usePredictedClassification);
		Connection conn  = DBConnection.getConnection();
		String query = "REPLACE INTO `lod`.`evaluation` (`uri`,`correct`,`incorrect`, `sim`,`userid`) "
		   		+ " VALUES (\"" + uri + "\", " + correct + ", " + incorrect + " ,\"" + sim + "\", " + userId + ")";
	       return executeAndClose(conn,query);
	}		
	
	/**
	 * @return
	 */
	public int insertPrediction(String seed, String uri, String evaluationlabel, String predictedlabel, boolean usePredictedClass, Double score,  int userid) {
			String sim = checkSimilarityMethod(usePredictedClass);
		   Connection conn  = DBConnection.getConnection(); String query = 
				   "INSERT INTO `lod`.`prediction` (`seed`, `uri`, `evaluationlabel`,`predictedlabel`, `sim`, `score`, `userid`) "
		   		+ " VALUES (\"" + seed + "\" , \"" + uri + "\" , \"" + evaluationlabel + "\", \"" + predictedlabel + "\" ,\"" + sim + "\", "+ score +", "+ userid +" )";
	       return executeAndClose(conn,query);	 
	}
	
	/**
	 * @return
	 */
	public int insertLink(String uri1, String uri2) {
		   Connection conn  = DBConnection.getConnection(); String query = 
				   "INSERT INTO `lod`.`link` (`uri1`, `uri2`) "
		   		+ " VALUES (\"" + uri1 + "\" , \"" + uri2 + "\")";
	       return executeAndClose(conn,query);	 
	}
	
	
	
	/**
	 * @return
	 */
	public void insertLinksOld(String uri1, List<Resource> resources) {
			for (Resource resource : resources) {
				if (!checkLink(uri1,resource.getURI())) {
					insertLink(uri1, resource.getURI());	
				}
			}
	}	
	
	
	/**
	 * @return
	 */
	public void insertLinks(String uri1, List<Resource> resources) {
		boolean noexist = false;
		Connection conn = DBConnection.getConnection();
		PreparedStatement ps = null;
		try {
			try {
				for (Resource resource : resources) {
					String query = "SELECT EXISTS(select * from `lod`.`link` as b where b.uri1 =  \"" + uri1 + "\" and  b.uri2 =  \"" + resource.getURI() + "\")";
		            //NodeUtil.print(getConnection().Query);
		            ps = conn.prepareStatement(query);ResultSet rs = ps.executeQuery();
		            while (rs != null && rs.next()) {
		            	noexist = rs.getBoolean(1);
		            }
		            ps.close();
		            if (!noexist) {
		            	query = "INSERT INTO `lod`.`link` (`uri1`, `uri2`) " + " VALUES (\"" + uri1 + "\" , \"" + resource.getURI() + "\")";
						ps = conn.prepareStatement(query);
						ps.execute();
					}
				}
				
				ps.close();
				conn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}

		} finally {
			if (ps != null) {
				try {
					ps.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}			
	
	/**
	 * @return
	 */
	public int insertOrUpdatePrediction2(String seed, String uri, String evaluationlabel, String predictedlabel, boolean usePredictedClass, Double score,  int userid) {
			String sim = checkSimilarityMethod(usePredictedClass);
		   Connection conn  = DBConnection.getConnection(); 
		   String query = 
				   "REPLACE INTO `lod`.`prediction` (`seed`, `uri`, `evaluationlabel`,`predictedlabel`, `sim`, `score`, `userid`) "
		   		+ " VALUES (\"" + seed + "\" , \"" + uri + "\" , \"" + evaluationlabel + "\", \"" + predictedlabel + "\" ,\"" + sim + "\", "+ score +", "+ userid +" )";
	       return executeAndClose(conn,query);	 
	}
	
	
	/**
	 * @return
	 */
	public void insertOrUpdatePredictions(List<NodePrediction> predictions) {
		Connection conn = DBConnection.getConnection();
		PreparedStatement ps = null;
		try {
			try {
				for (NodePrediction prediction : predictions) {
					String sim = checkSimilarityMethod(IConstants.USE_ICA);

					String query = "REPLACE INTO `lod`.`prediction` (`seed`, `uri`, `evaluationlabel`,`predictedlabel`, `sim`, `score`, `userid`) "
							+ " VALUES (\"" + prediction.getSeed() + "\" , \"" + prediction.getNode().getURI()
							+ "\" , \"" + prediction.getEvaluationLabel() + "\", \"" + prediction.getPredictedLabel()
							+ "\" ,\"" + sim + "\", " + prediction.getPredictionScore() + ", " + prediction.getUserId()
							+ " )";

					ps = conn.prepareStatement(query);
					ps.execute();

				}

				ps.close();
				conn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}

		} finally {
			if (ps != null) {
				try {
					ps.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}

	}	
	
	/**
	 * @return
	 */
	public int insertDomain(String seed, String uri, String domain, int iteration) {
		   Connection conn  = DBConnection.getConnection(); 
		   String query = 
				   "INSERT INTO `lod`.`domain` (`seed`, `uri`, `domain`,`iteration`) "
		   		+ " VALUES (\"" + seed + "\" , \"" + uri + "\" , \"" + domain + "\", "+ iteration +" )";
	       return executeAndClose(conn,query);	 
	}	

	public static String checkSimilarityMethod(boolean usePredictedClass) {
		String sim = IConstants.LDSD;
		if (usePredictedClass) {
			sim = IConstants.LDSD_LOD;	
		}
		return sim;
	}		
	
	/**
	 * @return
	 */
	public static int insertRelated(String uri1,String uri2, int userid) {
		   Connection conn  = DBConnection.getConnection(); String query = "INSERT INTO `lod`.`related` (`uri1`, `uri2`, `userid`) VALUES ( \"" + uri1 + "\" , \"" + uri2 + "\", "+ userid +" )";
	       return executeAndClose(conn,query);	 
	}	
	
	/**
	 * @return
	 */
	public int insertSemanticDistance(String uri1, String uri2, String sim, Double score) {
		   Connection conn  = DBConnection.getConnection(); 
		   String query = "INSERT INTO `lod`.`semantic` (`uri1`, `uri2`, `sim`, `score`) VALUES ( \"" + uri1 + "\" , \"" + uri2 + "\", \"" + sim + "\", "+ score +" )";
	       return executeAndClose(conn,query);	 
	}
	
	/**
	 * @return
	 */
	public boolean checkPrediction(String seed, String uri, int userId, String sim) {
		
		    boolean noexist = false;
	        try {
	            Connection conn  = DBConnection.getConnection(); String query = "SELECT EXISTS(select * from `lod`.`prediction` as b where b.seed =  \"" + seed + "\" and b.uri =  \"" + uri + "\" and b.sim =  \"" + sim + "\" and b.userid =  " + userId +")";
	            //NodeUtil.print(getConnection().Query);
	            PreparedStatement ps = conn.prepareStatement(query);ResultSet rs = ps.executeQuery();
	            while (rs != null && rs.next()) {
	            	noexist = rs.getBoolean(1);
	            }
	            closeQuery(conn,ps);	            
	        } catch (SQLException ex) {
	        	ex.printStackTrace();
	        }
			return noexist;
	}	
	
	/**
	 * @return
	 */
	public boolean checkLink(String uri1, String uri2) {
		
		    boolean noexist = false;
	        try {
	            Connection conn  = DBConnection.getConnection(); 
	            String query = "SELECT EXISTS(select * from `lod`.`link` as b where b.uri1 =  \"" + uri1 + "\" and  b.uri2 =  \"" + uri2 + "\")";
	            //NodeUtil.print(getConnection().Query);
	            PreparedStatement ps = conn.prepareStatement(query);ResultSet rs = ps.executeQuery();
	            while (rs != null && rs.next()) {
	            	noexist = rs.getBoolean(1);
	            }
	            closeQuery(conn,ps);	            
	        } catch (SQLException ex) {
	        	ex.printStackTrace();
	        }
			return noexist;
	}		
	

	/**
	 * @return
	 */
	public boolean checkPrediction(String seed, String uri, int userId, boolean usePredictedClassification) {
		String sim = checkSimilarityMethod(usePredictedClassification);
		    boolean noexist = false;
	        try {
	            Connection conn  = DBConnection.getConnection();
	            String query = "SELECT EXISTS(select * from `lod`.`prediction` as b where b.seed =  \"" + seed + "\" and b.uri =  \"" + uri + "\" and b.sim =  \"" + sim + "\" and b.userid =  " + userId +")";
	            //NodeUtil.print(getConnection().Query);
	            PreparedStatement ps = conn.prepareStatement(query);
	            ResultSet rs = ps.executeQuery();
	            while (rs != null && rs.next()) {
	            	noexist = rs.getBoolean(1);
	            }
	            closeQuery(conn,ps);	            
	        } catch (SQLException ex) {
	        	ex.printStackTrace();
	        }
			return noexist;
	}
	
	/**
	 * 		//SELECT count(*) as x, domain FROM lod.domain group by domain order by x desc
	 * @return
	 */
	public boolean checkDomain(String seed, String uri) {
		    boolean noexist = false;
	        try {
	            Connection conn  = DBConnection.getConnection();
	            String query = "SELECT EXISTS(select * from `lod`.`domain` as b where b.seed =  \"" + seed + "\" and b.uri =  \"" + uri + "\")";
	            //NodeUtil.print(getConnection().Query);
	            PreparedStatement ps = conn.prepareStatement(query);ResultSet rs = ps.executeQuery();
	            while (rs != null && rs.next()) {
	            	noexist = rs.getBoolean(1);
	            }
	            closeQuery(conn,ps);	            
	        } catch (SQLException ex) {
	        	ex.printStackTrace();
	        }
			return noexist;
	}
	
	

	/**
	 * @return
	 */
	public NodePrediction getPrediction(String seed, String uri, int userId, boolean usePredictedClassification) {
			String sim = checkSimilarityMethod(usePredictedClassification);
			NodePrediction nodePrediction = null;
	        try {
	            Connection conn  = DBConnection.getConnection();
	            String query = "select * from `lod`.`prediction` as b where b.seed =  \"" + seed + "\" and b.uri =  \"" + uri + "\" and b.sim =  \"" + sim + "\" and b.userid =  " + userId ;
	            //NodeUtil.print(getConnection().Query);
	            PreparedStatement ps = conn.prepareStatement(query);ResultSet rs = ps.executeQuery();
	            while (rs != null && rs.next()) {
          			Node nodeP = new Node();
	            	nodeP.setURI(rs.getString(2));
	            	nodePrediction = new NodePrediction(rs.getString(1),nodeP, rs.getString(3), rs.getString(4),rs.getString(5),rs.getDouble(6),rs.getInt(7));
	            }
	            closeQuery(conn,ps);	            
	        } catch (SQLException ex) {
	        	ex.printStackTrace();
	        }
			return nodePrediction;
	}
	
	
	/**
	 * @return
	 */
	public NodePrediction getPredictionEvaluation(String uri, int userId) {
			NodePrediction prediction = null;
	        try {
	            Connection conn  = DBConnection.getConnection();
	            String query = "select * from `lod`.`prediction` as b where b.seed =  \"" + IConstants.SEED_EVALUATION + "\" and b.uri =  \"" + uri + "\" and b.userid =  " + userId ;
	            //NodeUtil.print(getConnection().Query);
	            PreparedStatement ps = conn.prepareStatement(query);ResultSet rs = ps.executeQuery();
	            while (rs != null && rs.next()) {
          			Node nodeP = new Node();
	            	nodeP.setURI(rs.getString(2));
	            	prediction = new NodePrediction(rs.getString(1),nodeP, rs.getString(3), rs.getString(4),rs.getString(5),rs.getDouble(6),rs.getInt(7));
	            }
	            closeQuery(conn,ps);	            
	        } catch (SQLException ex) {
	        	ex.printStackTrace();
	        }
			return prediction;
	}	
	
	/**
	 * @return
	 */
	public List<NodePrediction> getPredictions(String uri, int userId) {
			List<NodePrediction> predictions = new ArrayList<NodePrediction>();
	        try {
	            Connection conn  = DBConnection.getConnection();
	            String query = "select * from `lod`.`prediction` as b where b.uri =  \"" + uri + "\" and b.userid =  " + userId ;
	            //NodeUtil.print(getConnection().Query);
	            PreparedStatement ps = conn.prepareStatement(query);ResultSet rs = ps.executeQuery();
	            while (rs != null && rs.next()) {
          			Node nodeP = new Node();
	            	nodeP.setURI(rs.getString(2));
	            	predictions.add(new NodePrediction(rs.getString(1),nodeP, rs.getString(3), rs.getString(4),rs.getString(5),rs.getDouble(6),rs.getInt(7)));
	            }
	            closeQuery(conn,ps);	            
	        } catch (SQLException ex) {
	        	ex.printStackTrace();
	        }
			return predictions;
	}	
	
	/**
	 * @return
	 */
	public List<Resource> getLinksIncomingAndOutcoming(String uri1) {
			List<Resource> links = new ArrayList<Resource>();
	        try {
	            Connection conn  = DBConnection.getConnection();
	            String query = "select uri2 from `lod`.`link` as b where b.uri1 =  \"" + uri1 + "\"";
	            //NodeUtil.print(query);
	            PreparedStatement ps = conn.prepareStatement(query);
	            ResultSet rs = ps.executeQuery();
	            while (rs != null && rs.next()) {
	            	links.add(new ResourceImpl(rs.getString(1)));
	            }
	            closeQuery(conn,ps);	            
	        } catch (SQLException ex) {
	        	ex.printStackTrace();
	        }
			return links;
	}
	
	/**
	 * @return
	 */
	public Set<Resource> getLinksIncomingAndOutcoming(String uri1, String uri2) {
			Set<Resource> links = new HashSet<Resource>();
	        try {
	            Connection conn  = DBConnection.getConnection();
	            String query = "select uri2 from `lod`.`link` as b where b.uri1 =  \"" + uri1 + "\" and b.uri2 =  \"" + uri2 + "\"";
	            //NodeUtil.print(query);
	            PreparedStatement ps = conn.prepareStatement(query);
	            ResultSet rs = ps.executeQuery();
	            while (rs != null && rs.next()) {
	            	links.add(new ResourceImpl(rs.getString(1)));
	            }
	            closeQuery(conn,ps);	            
	        } catch (SQLException ex) {
	        	ex.printStackTrace();
	        }
			return links;
	}
	
	public List<Resource> getLinksIncomingAndOutcoming(String uri1, Set<String> uris) {

		Set<Resource> links = new HashSet<Resource>();
		
        try {
        	Connection conn  = DBConnection.getConnection();
        	String query = "select uri2 from `lod`.`link` as b where b.uri1 =  \"" + uri1 + "\"  "+buildEqualStrings(true,"b.uri2",uris);
            //System.out.println(query);
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            while (rs != null && rs.next()) {
            	links.add(new ResourceImpl(rs.getString(1)));
            }
            closeQuery(conn,ps);	            
        } catch (SQLException ex) {
        	ex.printStackTrace();
        }
        hasBefore = false;
		return new ArrayList<Resource>(links);
	}
	
	
	
	/**
	 * @return
	 */
	public List<Resource> getLinksIncomingAndOutcoming2(String uri1, Set<String> uris) {
			Set<Resource> links = new HashSet<Resource>();
			for (String uri2 : uris) {
				links.addAll(getLinksIncomingAndOutcoming(uri1,uri2));
			}
			return new ArrayList<Resource>(links);
	}	
	
	
	/**
	 * @return
	 */
	public Evaluation getEvaluation(String uri, boolean usePredictedClassification,  int userId) {
			Evaluation evaluation = null;
			String sim = checkSimilarityMethod(usePredictedClassification);
	        try {
	            Connection conn  = DBConnection.getConnection();
	            String query = "select * from `lod`.`evaluation` as b where b.uri =  \"" + uri + "\"  and b.sim =  \"" + sim + "\" and  b.userid =  " + userId ;
	            //NodeUtil.print(getConnection().Query);
	            PreparedStatement ps = conn.prepareStatement(query);ResultSet rs = ps.executeQuery();
	            while (rs != null && rs.next()) {
	            	evaluation = new Evaluation(rs.getString(1),rs.getInt(2), rs.getInt(3),rs.getString(4),rs.getInt(5));
	            }
	            closeQuery(conn,ps);	            
	        } catch (SQLException ex) {
	        	ex.printStackTrace();
	        }
			return evaluation;
	}
	
	/**
	 * @return
	 */
	public List<Evaluation> getEvaluation(int userId) {
			List<Evaluation> evaluationList = new ArrayList<Evaluation>();
			
	        try {
	            Connection conn  = DBConnection.getConnection();
	            String query = "select * from `lod`.`evaluation` as b where b.userid =  " + userId ;
	            //NodeUtil.print(getConnection().Query);
	            PreparedStatement ps = conn.prepareStatement(query);ResultSet rs = ps.executeQuery();
	            while (rs != null && rs.next()) {
	            	evaluationList.add(new Evaluation(rs.getString(1),rs.getInt(2), rs.getInt(3),rs.getString(4),rs.getInt(5)));
	            }
	            closeQuery(conn,ps);	            
	        } catch (SQLException ex) {
	        	ex.printStackTrace();
	        }
			return evaluationList;
	}	
	
	
	/**
	 * @return
	 */
	public boolean checkEvaluation(int userId, boolean usePredictedClassification) {
			String sim = checkSimilarityMethod(usePredictedClassification);
		    boolean noexist = false;
	        try {
	            Connection conn  = DBConnection.getConnection(); String query = "SELECT EXISTS(select * from `lod`.`evaluation` as b where b.sim =  \"" + sim + "\" and  b.userid =  " + userId +")";
	            PreparedStatement ps = conn.prepareStatement(query);ResultSet rs = ps.executeQuery();
	            while (rs != null && rs.next()) {
	            	noexist = rs.getBoolean(1);
	            }
	            closeQuery(conn,ps);	            
	        } catch (SQLException ex) {
	        	ex.printStackTrace();
	        }
			return noexist;
	}	
	
	/**
	 * @return
	 */
	public boolean checkEvaluation(int userId) {
		    boolean noexist = false;
	        try {
	            Connection conn  = DBConnection.getConnection(); String query = "SELECT EXISTS(select * from `lod`.`evaluation` as b where b.userid =  " + userId +")";
	            PreparedStatement ps = conn.prepareStatement(query);ResultSet rs = ps.executeQuery();
	            while (rs != null && rs.next()) {
	            	noexist = rs.getBoolean(1);
	            }
	            closeQuery(conn,ps);	            
	        } catch (SQLException ex) {
	        	ex.printStackTrace();
	        }
			return noexist;
	}	
	
	
	/**
	 * @return
	 */
	public Double getLDSD(String uri1, String uri2) {
			Double score = null;
	        try {
	            Connection conn  = DBConnection.getConnection(); 
	            String query = "SELECT distinct b.score from `lod`.`semantic` as b where "
	            		+ "((b.uri1 =  \"" + uri1 + "\" and b.uri2 =  \"" + uri2 + "\" and b.sim = \"LDSD\") "
	            		+ " OR "
	            		+ "(b.uri1 =  \"" + uri2 + "\"  and b.uri2 =  \"" + uri1 + "\" and b.sim = \"LDSD\")) ";
	            PreparedStatement ps = conn.prepareStatement(query);
	            ResultSet rs = ps.executeQuery();
	            while (rs != null && rs.next()) {
	            	score = rs.getDouble(1);
	            }
	            closeQuery(conn,ps);
	        } catch (SQLException ex) {
	        	ex.printStackTrace();
	        }
			return score;
	}
	
	/**
	 * @return
	 */
	public Double getLDSD_LOD(String uri1, String uri2) {
		    Double score = null;
	        try {
	            Connection conn  = DBConnection.getConnection(); 
	            String query = "SELECT distinct b.score from `lod`.`semantic` as b where "
	            		+ "((b.uri1 =  \"" + uri1 + "\" and b.uri2 =  \"" + uri2 + "\" and b.sim = \"LDSD_LOD\") "
	            		+ " OR "
	            		+ "(b.uri1 =  \"" + uri2 + "\"  and b.uri2 =  \"" + uri1 + "\" and b.sim = \"LDSD_LOD\")) ";
	            PreparedStatement ps = conn.prepareStatement(query);
	            ResultSet rs = ps.executeQuery();
	            while (rs != null && rs.next()) {
	            	score = rs.getDouble(1);
	            }
	            closeQuery(conn,ps);	
	        } catch (SQLException ex) {
	        	ex.printStackTrace();
	        }
			return score;
	}	
	
	
	/**
	 * @return
	 */
	public boolean checkSemantics(String uri1,String uri2, String sim) {
		    boolean noexist = false;
	        try {
	            Connection conn  = DBConnection.getConnection(); String query = "SELECT EXISTS(select * from `lod`.`semantic` as b where"
	            		+ " (b.uri1 =  \"" + uri1 + "\" and b.uri2 =  \"" + uri2 + "\" and b.sim =  \"" + sim + "\") "
	            		+ "  OR   "
	            		+ "(b.uri1 =  \"" + uri2 + "\" and b.uri2 =  \"" + uri1 + "\" and b.sim =  \"" + sim + "\"))  ";
	            PreparedStatement ps = conn.prepareStatement(query);ResultSet rs = ps.executeQuery();
	            while (rs != null && rs.next()) {
	            	noexist = rs.getBoolean(1);
	            }
	            closeQuery(conn,ps);	            
	        } catch (SQLException ex) {
	        	ex.printStackTrace();
	        }
			return noexist;
	}
	
	/**
	 * @return
	 */
	public boolean checkRelated(String uri1, String uri2, int userid) {
		    boolean noexist = false;
	        try {
	            Connection conn  = DBConnection.getConnection(); String query = "SELECT EXISTS(select * from `lod`.`related` as b where"
	            		+ " (b.uri1 =  \"" + uri1 + "\" and b.uri2 =  \"" + uri2 + "\" and b.userid =  " + userid + ") "
	            		+ "  OR   "
	            		+ " (b.uri1 =  \"" + uri2 + "\" and b.uri2 =  \"" + uri1 + "\" and b.userid =  " + userid + "))  ";
	            PreparedStatement ps = conn.prepareStatement(query);ResultSet rs = ps.executeQuery();
	            while (rs != null && rs.next()) {
	            	noexist = rs.getBoolean(1);
	            }
	            closeQuery(conn,ps);	            
	        } catch (SQLException ex) {
	        	ex.printStackTrace();
	        }
			return noexist;
	}
	
	/**
	 * @return
	 */
	public boolean checkRelatedByUserId(int userid) {
		    boolean noexist = false;
	        try {
	            Connection conn  = DBConnection.getConnection(); String query = "SELECT EXISTS (select * from `lod`.`related` as b where (b.userid =  " + userid +" ))";
	            PreparedStatement ps = conn.prepareStatement(query);ResultSet rs = ps.executeQuery();
	            while (rs != null && rs.next()) {
	            	noexist = rs.getBoolean(1);
	            }
	            closeQuery(conn,ps);	            
	        } catch (SQLException ex) {
	        	ex.printStackTrace();
	        }
			return noexist;
	}	
	
	/**
	 * @return
	 */
	public Set<String> getMusics() {
		   Set<String> bookURIs = new HashSet<String>();
	        try {
	            Connection conn  = DBConnection.getConnection(); String query = "select b.uri from music as b";
	            PreparedStatement ps = conn.prepareStatement(query);ResultSet rs = ps.executeQuery();
	            while (rs != null && rs.next()) {
	            	bookURIs.add(rs.getString(1));
	            }  
	            closeQuery(conn,ps);
	        } catch (SQLException ex) {
	        	ex.printStackTrace();
	        }
			return bookURIs;
	}	
	
	/**
	 * @return
	 */
	public Set<String> getMovies() {
		   Set<String> bookURIs = new HashSet<String>();
	        try {
	            Connection conn  = DBConnection.getConnection(); String query = "select b.uri from movie as b";
	            PreparedStatement ps = conn.prepareStatement(query);ResultSet rs = ps.executeQuery();
	            while (rs != null && rs.next()) {
	            	bookURIs.add(rs.getString(1));
	            }
	            closeQuery(conn,ps);
	        } catch (SQLException ex) {
	        	ex.printStackTrace();
	        }
			return bookURIs;
	}	
	
	/**
	 * @return
	 */
	public Set<String> getBooks() {
		   Set<String> bookURIs = new HashSet<String>();
	        try {
	            Connection conn  = DBConnection.getConnection(); String query = "select b.uri from book as b";
	            PreparedStatement ps = conn.prepareStatement(query);ResultSet rs = ps.executeQuery();
	            while (rs != null && rs.next()) {
	            	bookURIs.add(rs.getString(1));
	            }
	            closeQuery(conn,ps);
	        } catch (SQLException ex) {
	        	ex.printStackTrace();
	        }
			return bookURIs;
	}
	
	/**
	 * @return
	 */
	public String getMusicURIByID(int id) {
		   String uri = null;
	        try {
	            Connection conn  = DBConnection.getConnection(); String query = "select b.uri from music as b where b.id =  " + id ;
	            PreparedStatement ps = conn.prepareStatement(query);ResultSet rs = ps.executeQuery();
	            while (rs != null && rs.next()) {
	            	uri = rs.getString(1);
	            }
	            closeQuery(conn,ps);
	        } catch (SQLException ex) {
	        	ex.printStackTrace();
	        }
			return uri;
	}	
	
	/**
	 * @return
	 */
	public String getMovieURIByID(int id) {
		   String uri = null;
	        try {
	            Connection conn  = DBConnection.getConnection(); String query = "select b.uri from movie as b where b.id =  " + id ;
	            PreparedStatement ps = conn.prepareStatement(query);ResultSet rs = ps.executeQuery();
	            while (rs != null && rs.next()) {
	            	uri = rs.getString(1);
	            }  
	            closeQuery(conn,ps);
	        } catch (SQLException ex) {
	        	ex.printStackTrace();
	        }
			return uri;
	}	
	
	
	/**
	 * @return
	 */
	public String getBookURIByID(int id) {
		   String uri = null;
	        try {
	            Connection conn  = DBConnection.getConnection(); String query = "select b.uri from book as b where b.id =  " + id ;
	            PreparedStatement ps = conn.prepareStatement(query);ResultSet rs = ps.executeQuery();
	            while (rs != null && rs.next()) {
	            	uri = rs.getString(1);
	            }  
	            closeQuery(conn,ps);
	        } catch (SQLException ex) {
	        	ex.printStackTrace();
	        }
			return uri;
	}	
	
	/**
	 * @return
	 */
	public Set<String> getBooksByURI(String uri) {
		   Set<String> bookURIs = new HashSet<String>();
	        try {
	            Connection conn  = DBConnection.getConnection(); String query = "select b.uri from book as b where b.uri =  \"" + uri + "\"";
	            PreparedStatement ps = conn.prepareStatement(query);ResultSet rs = ps.executeQuery();
	            while (rs != null && rs.next()) {
	            	bookURIs.add(rs.getString(1));
	            } 
	            closeQuery(conn,ps);
	        } catch (SQLException ex) {
	        	ex.printStackTrace();
	        }
			return bookURIs;
	}
	
	
	/**
	 * @return
	 */
	public Node getBookByURIAndConvertToNode(String uri) {
		   Node node = null;
	        try {
	            Connection conn  = DBConnection.getConnection(); String query = "select b.id, b.uri from book as b where b.uri =  \"" + uri + "\"";
	            PreparedStatement ps = conn.prepareStatement(query);ResultSet rs = ps.executeQuery();
	            while (rs != null && rs.next()) {
	            	node = new Node(String.valueOf(rs.getInt(1)),IConstants.NO_LABEL,rs.getString(2).trim());
	            } 
	            closeQuery(conn,ps);
	        } catch (SQLException ex) {
	        	ex.printStackTrace();
	        }
			return node;
	}
	
	/**
	 * @return
	 */
	public Node getMusicByURIAndConvertToNode(String uri) {
		   Node node = null;
	        try {
	            Connection conn  = DBConnection.getConnection(); String query = "select b.id, b.uri from music as b where b.uri =  \"" + uri + "\"";
	            PreparedStatement ps = conn.prepareStatement(query);ResultSet rs = ps.executeQuery();
	            while (rs != null && rs.next()) {
	            	node = new Node(String.valueOf(rs.getInt(1)),IConstants.NO_LABEL,rs.getString(2).trim());
	            } 
	            closeQuery(conn,ps);
	        } catch (SQLException ex) {
	        	ex.printStackTrace();
	        }
			return node;
	}	
	
	/**
	 * @return
	 */
	public Node getMovieByURIAndConvertToNode(String uri) {
		   Node node = null;
	        try {
	            Connection conn  = DBConnection.getConnection(); String query = "select b.id, b.uri from movie as b where b.uri =  \"" + uri + "\"";
	            PreparedStatement ps = conn.prepareStatement(query);ResultSet rs = ps.executeQuery();
	            while (rs != null && rs.next()) {
	            	node = new Node(String.valueOf(rs.getInt(1)),IConstants.NO_LABEL,rs.getString(2).trim());
	            }   
	            closeQuery(conn,ps);
	        } catch (SQLException ex) {
	        	ex.printStackTrace();
	        }
			return node;
	}		
	
	public static String buildEqual(boolean addAND,String field, List<Resource> resources){
		StringBuilder stringBuilder = new StringBuilder();
		int count = 0;
		if (resources!=null && !resources.isEmpty() && addAND) {
			stringBuilder.append(" and (");
		}
		for (Resource resource : resources) {
			stringBuilder.append("  " + field + " = ");
			stringBuilder.append("\"" + resource.getURI() + "\"");
			count++;
			if (count!=resources.size()) {
				stringBuilder.append(" OR ");	
			}
		}
		
		if (resources!=null && !resources.isEmpty() && addAND) {
			stringBuilder.append(")");
		}		
		
		if (!stringBuilder.toString().isEmpty()) {
			hasBefore=true;	
		}
		
		return stringBuilder.toString();
	}
	
	
	public static String buildEqualStrings(boolean addAND, String field, Set<String> resources){
		StringBuilder stringBuilder = new StringBuilder();
		int count = 0;
		if (resources!=null && !resources.isEmpty() && addAND) {
			stringBuilder.append(" and (");
		}
		for (String resource : resources) {
			stringBuilder.append("  " + field + " = ");
			stringBuilder.append("\"" + resource + "\"");
			count++;
			if (count!=resources.size()) {
				stringBuilder.append(" OR ");	
			}
		}
		
		if (resources!=null && !resources.isEmpty() && addAND) {
			stringBuilder.append(")");
		}		
		
		if (!stringBuilder.toString().isEmpty()) {
			hasBefore=true;	
		}
		
		return stringBuilder.toString();
	}	
	
	
	public static String buildEqual(boolean addAND,String field, String uri){
		StringBuilder stringBuilder = new StringBuilder();
		
		if (uri!=null && !uri.isEmpty() && addAND) {
			stringBuilder.append(" and (");
		}
		
			stringBuilder.append("  " + field + " = ");
			stringBuilder.append("\"" + uri + "\"");			
			
			if (uri!=null && !uri.isEmpty() && addAND) {
				stringBuilder.append(")");
			}			
			
		if (!stringBuilder.toString().isEmpty()) {
			hasBefore=true;	
		}
		
		return stringBuilder.toString();
	}	
	
	
	public static String buildNOT_IN(boolean addAND, String field, Set<Node> nodesNotIn){
		StringBuilder stringBuilder = new StringBuilder();
		if (nodesNotIn==null || nodesNotIn.isEmpty()) {
			return "";
		}
		int count = 0;
		if (nodesNotIn!=null && !nodesNotIn.isEmpty() && addAND) {
			stringBuilder.append(" and ");	
		}		
		if (nodesNotIn!=null && !nodesNotIn.isEmpty()) {
			stringBuilder.append( field + " NOT IN ( ");	
		}
		for (Node node : nodesNotIn) {
			stringBuilder.append(node.getId().trim());
			//stringBuilder.append("\"" + node.getUri() + "%\"");			
			count++;
			if (count!=nodesNotIn.size()) {
				stringBuilder.append(",");	
			}
		}
		if (nodesNotIn!=null && !nodesNotIn.isEmpty() ) {
			stringBuilder.append(")");	
		}		
		
		return stringBuilder.toString();
	}
	
	public Set<Node> getMusicsNotInConvertToNode(List<Resource> resources, Set<Node> nodesNotIn) {
		if (resources.isEmpty()&&nodesNotIn.isEmpty()) {
			return new HashSet<Node>();
		}
		Set<Node> nodes = new HashSet<Node>();
		
        try {
            Connection conn  = DBConnection.getConnection(); String query = "select m.id, m.uri from music as m where  "+buildEqual(false,"m.uri",resources) +" "+ buildNOT_IN(hasBefore, "m.id",nodesNotIn);
            
            //System.out.println(getConnection().Query);
            PreparedStatement ps = conn.prepareStatement(query);ResultSet rs = ps.executeQuery();
            while (rs != null && rs.next()) {
            	nodes.add(new Node(String.valueOf(rs.getInt(1)),IConstants.NO_LABEL,rs.getString(2).trim()));
            } 
            closeQuery(conn,ps);   	        	
        } catch (SQLException ex) {
        	ex.printStackTrace();
        }
        hasBefore = false;
		return nodes;
	}
	
	
	public Set<Node> getBooksNotInConvertToNode(List<Resource> resources, Set<Node> nodesNotIn) {
		if (resources.isEmpty()&&nodesNotIn.isEmpty()) {
			return new HashSet<Node>();
		}
		Set<Node> nodes = new HashSet<Node>();
		
        try {
            Connection conn  = DBConnection.getConnection(); String query = "select m.id, m.uri from book as m where  "+buildEqual(false, "m.uri",resources) +" "+ buildNOT_IN(hasBefore,"m.id",nodesNotIn);
            //System.out.println(getConnection().Query);
            PreparedStatement ps = conn.prepareStatement(query);ResultSet rs = ps.executeQuery();
            while (rs != null && rs.next()) {
            	nodes.add(new Node(String.valueOf(rs.getInt(1)),IConstants.NO_LABEL,rs.getString(2).trim()));
            }
            closeQuery(conn,ps);
        } catch (SQLException ex) {
        	ex.printStackTrace();
        }
        hasBefore = false;
		return nodes;
	}
	
	
	public Set<Node> getUnionMoviesMusicsBooksNotInConvertToNode(List<Resource> resources, Set<Node> nodesNotIn) {
		if (resources.isEmpty()&&nodesNotIn.isEmpty()) {
			return new HashSet<Node>();
		}
		Set<Node> nodes = new HashSet<Node>();
		
        try {
        	Connection conn  = DBConnection.getConnection(); String query = "select distinct *  FROM ("+
            "(select distinct m.id, m.uri from movie as m where  "+buildEqual(false,"m.uri",resources) +" "+ buildNOT_IN(hasBefore, "m.id",nodesNotIn)+")  "
            +" UNION ALL "+
            "(select distinct m.id, m.uri from music as m where  "+buildEqual(false,"m.uri",resources) +" "+ buildNOT_IN(hasBefore, "m.id",nodesNotIn)+")  "
            +" UNION ALL "+
            "(select distinct m.id, m.uri from book as m where   "+buildEqual(false,"m.uri",resources) +" "+ buildNOT_IN(hasBefore, "m.id",nodesNotIn)+")  "
            + ") as x";            
            //System.out.println(getConnection().Query);
            PreparedStatement ps = conn.prepareStatement(query);ResultSet rs = ps.executeQuery();
            while (rs != null && rs.next()) {
            	nodes.add(new Node(String.valueOf(rs.getInt(1)),IConstants.NO_LABEL,rs.getString(2).trim()));
            }
            closeQuery(conn,ps);
        } catch (SQLException ex) {
        	ex.printStackTrace();
        }
        hasBefore = false;
		return nodes;
	}
	
	
	public Set<Node> getUnionMoviesMusicsBooksNotInConvertToNode(String uri) {

		Set<Node> nodes = new HashSet<Node>();
		
        try {
        	Connection conn  = DBConnection.getConnection(); String query = "select distinct *  FROM ("+
            "(select distinct m.id, m.uri from movie as m where  "+buildEqual(false,"m.uri",uri) +" )  "
            +" UNION ALL "+
            "(select distinct m.id, m.uri from music as m where  "+buildEqual(false,"m.uri",uri) +" )  "
            +" UNION ALL "+
            "(select distinct m.id, m.uri from book as m where   "+buildEqual(false,"m.uri",uri) +" )  "
            + ") as x";            
            //System.out.println(getConnection().Query);
            PreparedStatement ps = conn.prepareStatement(query);ResultSet rs = ps.executeQuery();
            while (rs != null && rs.next()) {
            	nodes.add(new Node(String.valueOf(rs.getInt(1)),IConstants.NO_LABEL,rs.getString(2).trim()));
            }
            closeQuery(conn,ps);
        } catch (SQLException ex) {
        	ex.printStackTrace();
        }
        hasBefore = false;
		return nodes;
	}	
	
	
	public Set<Node> getMoviesNotInConvertToNode(List<Resource> resources, Set<Node> nodesNotIn) {
		if (resources.isEmpty()&&nodesNotIn.isEmpty()) {
			return new HashSet<Node>();
		}
		Set<Node> nodes = new HashSet<Node>();
		
        try {
            Connection conn  = DBConnection.getConnection(); String query = "select m.id, m.uri from movie as m where  "+buildEqual(false,"m.uri",resources) +" "+ buildNOT_IN(hasBefore, "m.id",nodesNotIn);
            //System.out.println(getConnection().Query);
            PreparedStatement ps = conn.prepareStatement(query);ResultSet rs = ps.executeQuery();
            while (rs != null && rs.next()) {
            	nodes.add(new Node(String.valueOf(rs.getInt(1)),IConstants.NO_LABEL,rs.getString(2).trim()));
            }  
            closeQuery(conn,ps);
        } catch (SQLException ex) {
        	ex.printStackTrace();
        }
        hasBefore = false;
		return nodes;
	}	
	
	public Set<Node> getMusicsConvertToNode(List<Resource> resources) {
		Set<Node> nodes = new HashSet<Node>();
        try {
            Connection conn  = DBConnection.getConnection(); String query = "select m.id, m.uri from music as m where  "+buildEqual(false,"m.uri",resources);
            //System.out.println(getConnection().Query);
            PreparedStatement ps = conn.prepareStatement(query);ResultSet rs = ps.executeQuery();
            while (rs != null && rs.next()) {
            	nodes.add(new Node(String.valueOf(rs.getInt(1)),IConstants.NO_LABEL,rs.getString(2).trim()));
            }  
            closeQuery(conn,ps);
        } catch (SQLException ex) {
        	ex.printStackTrace();
        }
        hasBefore = false;
		return nodes;
	}
	
	/**
	 * @return
	 */
	public Set<Node> getMoviesConvertToNode(List<Resource> resources) {
		Set<Node> nodes = new HashSet<Node>();
        try {
	            Connection conn  = DBConnection.getConnection(); String query = "select m.id, m.uri from movie as m where "+buildEqual(false,"m.uri",resources);
	            //System.out.println(getConnection().Query);
	            PreparedStatement ps = conn.prepareStatement(query);ResultSet rs = ps.executeQuery();
	            while (rs != null && rs.next()) {
	            	nodes.add(new Node(String.valueOf(rs.getInt(1)),IConstants.NO_LABEL,rs.getString(2).trim()));
	            }  
	            closeQuery(conn,ps);            
	        } catch (SQLException ex) {
	        	ex.printStackTrace();
	        }
        	hasBefore = false;
			return nodes;
		}		
	
	/**
	 * @return
	 */
	public Set<Node> getBooksConvertToNode(List<Resource> resources) {
		Set<Node> nodes = new HashSet<Node>();
	        try {
	            Connection conn  = DBConnection.getConnection(); String query = "select b.id, b.uri from book as b where "+buildEqual(false, "b.uri",resources);
	            //System.out.println(getConnection().Query);
	            PreparedStatement ps = conn.prepareStatement(query);ResultSet rs = ps.executeQuery();
	            while (rs != null && rs.next()) {
	            	nodes.add(new Node(String.valueOf(rs.getInt(1)),IConstants.NO_LABEL,rs.getString(2).trim()));
	            } 
	            closeQuery(conn,ps);	            
	        } catch (SQLException ex) {
	        	ex.printStackTrace();
	        }
	        hasBefore = false;
			return nodes;
	}	
	
	
	public Set<Node> getUnionLikedMoviesMusicsBooksUserIdAndConvertToNode(int userId, List<Resource> resources) {
		Set<Node> nodes = new HashSet<Node>();
        try {
            //Connection conn  = BDConexao.getConexao(); String query = "select m.id as id, m.uri as uri FROM ("+
        	Connection conn  = DBConnection.getConnection(); String query = "select distinct *  FROM ("+
            "(select distinct m.id, m.uri from music as m, music_like as ml where  m.id = ml.musicid and ml.userid = " + userId +" "+buildEqual(true,"m.uri",resources)+")  "
            +" UNION ALL "+
            "(select distinct m.id, m.uri from movie as m, movie_like as ml where  m.id = ml.movieid and ml.userid = " + userId +" "+buildEqual(true,"m.uri",resources)+")  "
            +" UNION ALL "+
            "(select distinct m.id, m.uri from book  as m, book_like  as ml where  m.id = ml.bookid  and ml.userid = " + userId +" "+buildEqual(true,"m.uri",resources)+")  "
            + ") as x";
            //System.out.println(getConnection().Query);
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            while (rs != null && rs.next()) {
            	nodes.add(new Node(String.valueOf(rs.getInt(1)),IConstants.LIKE,rs.getString(2).trim()));
            }  
            closeQuery(conn,ps);
        } catch (SQLException ex) {
        	ex.printStackTrace();
        }
        hasBefore = false;
		return nodes;
	}
	
	
	public Set<Node> getUnionLikedMoviesMusicsBooksByUserIdAndConvertToNode(int userId) {
		Set<Node> nodes = new HashSet<Node>();
        try {
            //Connection conn  = BDConexao.getConexao(); String query = "select m.id as id, m.uri as uri FROM ("+
        	Connection conn  = DBConnection.getConnection(); String query = "select distinct *  FROM ("+
            "(select distinct m.id, m.uri from music as m, music_like as ml where  m.id = ml.musicid and ml.userid = " + userId +" )  "
            +" UNION ALL "+
            "(select distinct m.id, m.uri from movie as m, movie_like as ml where  m.id = ml.movieid and ml.userid = " + userId +" )  "
            +" UNION ALL "+
            "(select distinct m.id, m.uri from book  as m, book_like  as ml where  m.id = ml.bookid  and ml.userid = " + userId +" )  "
            + ") as x";
            //System.out.println(query);
            PreparedStatement ps = conn.prepareStatement(query);ResultSet rs = ps.executeQuery();
            while (rs != null && rs.next()) {
            	nodes.add(new Node(String.valueOf(rs.getInt(1)),IConstants.LIKE,rs.getString(2).trim()));
            } 
            closeQuery(conn,ps);
        } catch (SQLException ex) {
        	ex.printStackTrace();
        }
		return nodes;
	}
	
	
	public Set<Node> getUnionLikedMoviesMusicsBooksAndConvertToNode() {
		Set<Node> nodes = new HashSet<Node>();
        try {
            //Connection conn  = BDConexao.getConexao(); String query = "select m.id as id, m.uri as uri FROM ("+
        	Connection conn  = DBConnection.getConnection(); String query = "select distinct *  FROM ("+
            "(select distinct m.id, m.uri, ml.userid from music as m, music_like as ml where  m.id = ml.musicid  )  "
            +" UNION ALL "+
            "(select distinct m.id, m.uri, ml.userid from movie as m, movie_like as ml where  m.id = ml.movieid  )  "
            +" UNION ALL "+
            "(select distinct m.id, m.uri, ml.userid from book  as m, book_like  as ml where  m.id = ml.bookid   )  "
            + ") as x";
            
            PreparedStatement ps = conn.prepareStatement(query);ResultSet rs = ps.executeQuery();
            while (rs != null && rs.next()) {
            	nodes.add(new Node(String.valueOf(rs.getInt(1)),IConstants.LIKE,rs.getString(2).trim(),rs.getInt(1)));
            } 
            closeQuery(conn,ps);
        } catch (SQLException ex) {
        	ex.printStackTrace();
        }
        hasBefore = false;
		return nodes;
	}	
	
	
	public Set<Integer> getUserIdsForLikedMoviesMusicsBooks() {
		Set<Integer> nodes = new HashSet<Integer>();
        try {
            //Connection conn  = BDConexao.getConexao(); String query = "select m.id as id, m.uri as uri FROM ("+
        	Connection conn  = DBConnection.getConnection(); String query = "select distinct *  FROM ("+
            "(select distinct ml.userid from music as m, music_like as ml where  m.id = ml.musicid  )  "
            +" UNION ALL "+
            "(select distinct ml.userid from movie as m, movie_like as ml where  m.id = ml.movieid  )  "
            +" UNION ALL "+
            "(select distinct ml.userid from book  as m, book_like  as ml where  m.id = ml.bookid   )  "
            + ") as x";
            
            PreparedStatement ps = conn.prepareStatement(query);ResultSet rs = ps.executeQuery();
            while (rs != null && rs.next()) {
            	nodes.add(rs.getInt(1));
            } 
            closeQuery(conn,ps);
        } catch (SQLException ex) {
        	ex.printStackTrace();
        }
        hasBefore = false;
		return nodes;
	}
	
	public Set<Node> getUnionLikedMoviesMusicsBooksUserIdAndConvertToNode(String uri) {
		Set<Node> nodes = new HashSet<Node>();
        try {
            //Connection conn  = BDConexao.getConexao(); String query = "select m.id as id, m.uri as uri FROM ("+
        	Connection conn  = DBConnection.getConnection(); String query = "select distinct *  FROM ("+
            "(select distinct m.id, m.uri from music as m, music_like as ml where  m.uri = \"" + uri + "\" )  "
            +" UNION ALL "+
            "(select distinct m.id, m.uri from movie as m, movie_like as ml where  m.uri = \"" + uri + "\"  "
            +" UNION ALL "+
            "(select distinct m.id, m.uri from book  as m, book_like  as ml where  m.uri = \"" + uri + "\"  "
            + ") as x";
            //System.out.println(getConnection().Query);
            PreparedStatement ps = conn.prepareStatement(query);ResultSet rs = ps.executeQuery();
            while (rs != null && rs.next()) {
            	nodes.add(new Node(String.valueOf(rs.getInt(1)),IConstants.NO_LABEL,rs.getString(2).trim().trim()));
            }   
            closeQuery(conn,ps);
        } catch (SQLException ex) {
        	ex.printStackTrace();
        }
        hasBefore = false;
		return nodes;
	}
	
	


	

	
	public Set<Node> getLikedMusicsUserIdAndConvertToNode(String userId, List<Resource> resources) {
		Set<Node> nodes = new HashSet<Node>();
        try {
            Connection conn  = DBConnection.getConnection(); String query = "select m.id, m.uri from music as m, music_like as ml where  m.id = ml.musicid and ml.userid = " + userId +" "+buildEqual(true,"m.uri",resources);
            //System.out.println(getConnection().Query);
            PreparedStatement ps = conn.prepareStatement(query);ResultSet rs = ps.executeQuery();
            while (rs != null && rs.next()) {
            	nodes.add(new Node(String.valueOf(rs.getInt(1)),IConstants.LIKE,rs.getString(2).trim().trim()));
            }
            closeQuery(conn,ps);
        } catch (SQLException ex) {
        	ex.printStackTrace();
        }
        hasBefore = false;
		return nodes;
	}
	
	/**
	 * @return
	 */
	public Set<Node> getLikedMoviesUserIdAndConvertToNode(String userId, List<Resource> resources) {
		Set<Node> nodes = new HashSet<Node>();
        try {
	            Connection conn  = DBConnection.getConnection(); String query = "select m.id, m.uri from movie as m, movie_like as ml where m.id = ml.movieid and ml.userid = "+ userId +" "+buildEqual(true,"m.uri",resources);
	            //System.out.println(getConnection().Query);
	            PreparedStatement ps = conn.prepareStatement(query);ResultSet rs = ps.executeQuery();
	            while (rs != null && rs.next()) {
	            	nodes.add(new Node(String.valueOf(rs.getInt(1)),IConstants.LIKE,rs.getString(2).trim()));
	            }  
	            closeQuery(conn,ps);
	        } catch (SQLException ex) {
	        	ex.printStackTrace();
	        }
        	hasBefore = false;
			return nodes;
		}		
	
	/**
	 * @return
	 */
	public Set<Node> getLikedBooksUserIdAndConvertToNode(String userId, List<Resource> resources) {
		Set<Node> nodes = new HashSet<Node>();
	        try {
	            Connection conn  = DBConnection.getConnection(); String query = "select b.id, b.uri from book as b, book_like as bl where b.id = bl.bookid and bl.userid = " + userId +" "+buildEqual(true,"b.uri",resources);
	            //System.out.println(getConnection().Query);
	            PreparedStatement ps = conn.prepareStatement(query);ResultSet rs = ps.executeQuery();
	            while (rs != null && rs.next()) {
	            	nodes.add(new Node(String.valueOf(rs.getInt(1)),IConstants.LIKE,rs.getString(2).trim()));
	            } 
	            closeQuery(conn,ps);
	        } catch (SQLException ex) {
	        	ex.printStackTrace();
	        }
	        hasBefore = false;
			return nodes;
	}	
	
	
	public Node getLikedMusicUserIdAndConvertToNode(String uri, String userId, Set<Resource> resources) {
		Node node = null;
        try {
            Connection conn  = DBConnection.getConnection(); String query = "select m.id, m.uri from music as m, music_like as ml where m.id = ml.musicid and ml.userid = " + userId +" and m.uri =  \"" + uri + "\"";
            PreparedStatement ps = conn.prepareStatement(query);ResultSet rs = ps.executeQuery();
            while (rs != null && rs.next()) {
            	node = new Node(String.valueOf(rs.getInt(1)),IConstants.LIKE,rs.getString(2).trim());
            } 
            closeQuery(conn,ps);
        } catch (SQLException ex) {
        	ex.printStackTrace();
        }
        
		return node;
	}	
	
	
	/**
	 * @return
	 */
	public Node getLikedBookUserIdAndConvertToNode(String uri, String userId) {
			Node node = null;
	        try {
	            Connection conn  = DBConnection.getConnection(); String query = "select b.id, b.uri from book as b, book_like as bl where b.id = bl.bookid and bl.userid = " + userId + " and b.uri =  \"" + uri + "\"";
	            PreparedStatement ps = conn.prepareStatement(query);ResultSet rs = ps.executeQuery();
	            while (rs != null && rs.next()) {
	            	node = new Node(String.valueOf(rs.getInt(1)),IConstants.LIKE,rs.getString(2).trim());
	            }
	            closeQuery(conn,ps);
	        } catch (SQLException ex) {
	        	ex.printStackTrace();
	        }
	        
			return node;
	}

	
	/**
	 * @return
	 */
	public Node getLikedMusicUserIdAndConvertToNode(String uri, String userId) {
			Node node = null;
	        try {
	            Connection conn  = DBConnection.getConnection(); String query = "select m.id, m.uri from music as m, music_like as ml where m.id = ml.musicid and ml.userid = " + userId +" and m.uri =  \"" + uri + "\"";
	            PreparedStatement ps = conn.prepareStatement(query);ResultSet rs = ps.executeQuery();
	            while (rs != null && rs.next()) {
	            	node = new Node(String.valueOf(rs.getInt(1)),IConstants.LIKE,rs.getString(2).trim());
	            } 
	            closeQuery(conn,ps);
	        } catch (SQLException ex) {
	        	ex.printStackTrace();
	        }
	        
			return node;
	}
	
	/**
	 * @return
	 */
	public Node getLikedMovieUserIdAndConvertToNode(String uri, String userId) {
		Node node = null;
	        try {
	            Connection conn  = DBConnection.getConnection(); String query = "select m.id, m.uri from movie as m, movie_like as ml where m.id = ml.movieid and ml.userid = " + userId +" and m.uri =  \"" + uri + "\"";
	            //System.out.println(getConnection().Query);
	            PreparedStatement ps = conn.prepareStatement(query);ResultSet rs = ps.executeQuery();
	            while (rs != null && rs.next()) {
	            	node = new Node(String.valueOf(rs.getInt(1)),IConstants.LIKE,rs.getString(2).trim());
	            } 
	            closeQuery(conn,ps);
	        } catch (SQLException ex) {
	        	ex.printStackTrace();
	        }
	        
			return node;
	}	
	

		
	
 /**
	 * @return
	 */
	public Set<String> getBooksURI() {
		   Set<String> bookURIs = new HashSet<String>();
	        try {
	            Connection conn  = DBConnection.getConnection(); String query = "select * from book";
	            PreparedStatement ps = conn.prepareStatement(query);ResultSet rs = ps.executeQuery();
	            while (rs != null && rs.next()) {
	            	bookURIs.add(rs.getString(2).trim());
	            }
	            closeQuery(conn,ps);
	        } catch (SQLException ex) {
	        	ex.printStackTrace();
	        }
			return bookURIs;
 } 
	
	
	/**
	 * @return
	 */
	public Set<String> getBooksURILikedByUser(String userId) {
		   Set<String> bookURIs = new HashSet<String>();
	        try {
	            Connection conn  = DBConnection.getConnection(); String query = "select b.uri from book as b, book_like as bl where b.id = bl.bookid and bl.userid = " + userId ;
	            PreparedStatement ps = conn.prepareStatement(query);ResultSet rs = ps.executeQuery();
	            while (rs != null && rs.next()) {
	            	bookURIs.add(rs.getString(1));
	            }  
	            closeQuery(conn,ps);
	        } catch (SQLException ex) {
	        	ex.printStackTrace();
	        }
	        
			return bookURIs;
  }
	
	
	/**
	 * @return
	 */
	public Set<Node> getBooksLikedByUserAsNodes(String userId) {
			Set<Node> nodes = new HashSet<Node>();
	        try {
	            Connection conn  = DBConnection.getConnection(); String query = "select b.id, b.uri from book as b, book_like as bl where b.id = bl.bookid and bl.userid = " + userId ;
	            PreparedStatement ps = conn.prepareStatement(query);ResultSet rs = ps.executeQuery();
	            while (rs != null && rs.next()) {
	            	Node node = new Node(String.valueOf(rs.getInt(1)),IConstants.LIKE,rs.getString(2).trim());
	            	nodes.add(node);
	            } 
	            closeQuery(conn,ps);
	        } catch (SQLException ex) {
	        	ex.printStackTrace();
	        }
	        
			return nodes;
  }	
	
	/**
	 * @return
	 */
	public Set<Node> getMusicsLikedByUserAsNodes(String userId) {
		Set<Node> nodes = new HashSet<Node>();
	        try {
	            Connection conn  = DBConnection.getConnection(); String query = "select m.id, m.uri from music as m, music_like as ml where m.id = ml.musicid and ml.userid = " + userId ;
	            PreparedStatement ps = conn.prepareStatement(query);ResultSet rs = ps.executeQuery();
	            while (rs != null && rs.next()) {
	            	Node node = new Node(String.valueOf(rs.getInt(1)),IConstants.LIKE,rs.getString(2).trim());
	            	nodes.add(node);
	            }
	            closeQuery(conn,ps);
	        } catch (SQLException ex) {
	        	ex.printStackTrace();
	        }
	        
			return nodes;
	}
	
	/**
	 * @return
	 */
	public Set<Node> getMoviesURILikedByUserAsNodes(String userId) {
		Set<Node> nodes = new HashSet<Node>();
	        try {
	            Connection conn  = DBConnection.getConnection(); String query = "select m.id, m.uri from movie as m, movie_like as ml where m.id = ml.movieid and ml.userid = " + userId ;
	            PreparedStatement ps = conn.prepareStatement(query);ResultSet rs = ps.executeQuery();
	            while (rs != null && rs.next()) {
	            	Node node = new Node(String.valueOf(rs.getInt(1)),IConstants.LIKE,rs.getString(2).trim());
	            	nodes.add(node);
	            }
	            closeQuery(conn,ps);
	        } catch (SQLException ex) {
	        	ex.printStackTrace();
	        }
	        
			return nodes;
	}	
	
	
	/**
	 * @return
	 */
	public Set<String> getMusicsURILikedByUser(String userId) {
		   Set<String> uris = new HashSet<String>();
	        try {
	            Connection conn  = DBConnection.getConnection(); String query = "select m.uri from music as m, music_like as ml where m.id = ml.musicid and ml.userid = " + userId ;
	            PreparedStatement ps = conn.prepareStatement(query);ResultSet rs = ps.executeQuery();
	            while (rs != null && rs.next()) {
	            	uris.add(rs.getString(1).trim());
	            }
	            closeQuery(conn,ps);
	        } catch (SQLException ex) {
	        	ex.printStackTrace();
	        }

			return uris;
	}	
	
	
	/**
	 * @return
	 */
	public Set<String> getMoviesURILikedByUser(String userId) {
		   Set<String> uris = new HashSet<String>();
	        try {
	        	 
	        
	            Connection conn  = DBConnection.getConnection(); String query = "select m.uri from movie as m, movie_like as ml where m.id = ml.movieid and ml.userid = " + userId ;
	            PreparedStatement ps = conn.prepareStatement(query);ResultSet rs = ps.executeQuery();
	            while (rs != null && rs.next()) {
	            	uris.add(rs.getString(1).trim());
	            }
	            closeQuery(conn,ps);
	        } catch (SQLException ex) {
	        	ex.printStackTrace();
	        }
	        
			return uris;
	}
	
	
	/**
	 * @return
	 */
	public Set<String> getMoviesFilmsBooksURILikedByUser(String userId) {
		   Set<String> uris = new HashSet<String>();
	        try {
	            Connection conn  = DBConnection.getConnection(); 
	            String query = " select distinct mo.uri from movie as mo , movie_like as mol, book as b, book_like as bl, "
	            		+ " music as mu, music_like as mul where mo.id = mol.movieid and b.id = bl.bookid and mu.id = mul.musicid "
	            		+ " and mol.userid = " + userId
	            		+ " and  bl.userid  = " + userId
	            		+ " and mul.userid = " + userId;
	            PreparedStatement ps = conn.prepareStatement(query);ResultSet rs = ps.executeQuery();
	            while (rs != null && rs.next()) {
	            	uris.add(rs.getString(1));
	            }
	            closeQuery(conn,ps);
	        } catch (SQLException ex) {
	        	ex.printStackTrace();
	        }
			return uris;
	}
	
	public void checkDistance(int limitIteration) throws Exception {

		Set<Integer> ids = getUserIdsForLikedMoviesMusicsBooks();
		
		for (Integer id : ids) {

			Set<Node> userProfile = new HashSet<Node>();
			
			userProfile.addAll(getUnionLikedMoviesMusicsBooksByUserIdAndConvertToNode(id));
			
			List<String> control = new ArrayList<String>();
			int limit = limitIteration;
			
			for (Node node1 : userProfile) {
				for (Node node2 : userProfile) {
					if (node1!=node2 && (!control.contains(node1.getURI()+node2.getURI()+id) || !control.contains(node2.getURI()+node1.getURI()+id))   && !checkRelated(node1.getURI(), node2.getURI(),id)) {
						NodeUtil.print("Testing uiser id"+ id+"  for  "+node1.getURI()+" versus "+node2.getURI());
												int dist = SparqlWalk.findPathsBetween2Resources(node1.getURI(),node2.getURI(),limit);
						//int dist = SparqlWalk.findPathsBetween2Resources("http://dbpedia.org/resource/Katy_Perry","http://dbpedia.org/resource/Jones_Leandro",limit);
						//if (dist==0 && (!(control.contains(node1.getURI()+node2.getURI())||control.contains(node2.getURI()+node1.getURI())))) {
							//print("User id"+ id+" with  "+node1.getURI()+" is distant of "+node2.getURI()+" at least "+ limit);	
						//}else
						if (dist!=0) {
							insertRelated(node1.getURI(), node2.getURI(),id);
							NodeUtil.print("User id"+ id+" with  "+node1.getURI()+" is distant of "+node2.getURI()+" = "+ dist);
						} else if (SparqlWalk.countIndirectIncomingLinksFromResourceAndLink(node1.getURI(), node2.getURI())>0) {
							insertRelated(node1.getURI(), node2.getURI(),id);
							NodeUtil.print("User id"+ id+" with  "+node1.getURI()+" is distant of "+node2.getURI()+" = "+ dist);
						} else if (SparqlWalk.countIndirectOutgoingLinksFromResourceAndLink(node1.getURI(), node2.getURI())>0) {
							insertRelated(node1.getURI(), node2.getURI(),id);
							NodeUtil.print("User id"+ id+" with  "+node1.getURI()+" is distant of "+node2.getURI()+" = "+ dist);
						} 
						
						control.add(node1.getURI()+node2.getURI()+id);
						control.add(node2.getURI()+node1.getURI()+id);
						
					}
				}
			}
			
			NodeUtil.print("User id"+ id+" items are not related");			
		}
	}	
	


	/**
	 * @param userId
	 * @param uri
	 * @return
	 */
	public Set<Node> getLikedItemsFromDatabase(int userId, String uri) {
		List<Resource> resources = new ArrayList<Resource>();
		Resource resource = new ResourceImpl(uri);
		resources.add(resource);
		return getNodesFromDatabase(userId,resources);
	}
	
	/**
	 * @param userId
	 * @param uri
	 * @return
	 */
	public Set<Node> getNodesFromDatabase(int userId, String uri) {
		List<Resource> resources = new ArrayList<Resource>();
		Resource resource = new ResourceImpl(uri);
		resources.add(resource);
		return getNodesFromDatabase(userId,resources);
	}	
	
	/**
	 * @param userId
	 * @param resources
	 * @return
	 */
	public Set<Node> getNodesFromDatabase(int userId, List<Resource> resources) {
		Set<Node> databaseNodes = new HashSet<Node>();
		//resources = resources.subList(0,1);
	    Set<Node> nodes = getUnionLikedMoviesMusicsBooksUserIdAndConvertToNode(userId,resources);
		if (!nodes.isEmpty()) {
			databaseNodes.addAll(nodes);
		}
		nodes = getUnionMoviesMusicsBooksNotInConvertToNode(resources,nodes);
		if (!nodes.isEmpty()) {
			databaseNodes.addAll(nodes);
		}
		return databaseNodes;
	}
	
	/**
	 * @param nodesDB
	 * @param nodesToGraph
	 */
	private void normalizeBDtoGraph(Set<Node> nodesDB, Set<Node> nodesToGraph) {
		for (Node node : nodesDB) {
			Node nodeUpdate = NodeUtil.getNodeByURI(node.getURI(),nodesToGraph);
			if (nodeUpdate!=null) {
				nodeUpdate.setLabel(node.getLabel());
				NodeUtil.printNode(nodeUpdate);
			}
		}
	}
	
	/**
	 * It gets user profiles from database
	 * @param userId
	 * @param resources
	 * @return
	 */
	public Set<Node> getLikedNodesFromDatabase(int userId, List<Resource> resources) {
		Set<Node> databaseNodes = new HashSet<Node>();
		//resources = resources.subList(0,1);
	    Set<Node> nodes = getUnionLikedMoviesMusicsBooksUserIdAndConvertToNode(userId,resources);
		if (!nodes.isEmpty()) {
			databaseNodes.addAll(nodes);
		}
		return databaseNodes;
	}	
	
	public Set<Integer> getUserIdsForLikedMovies() {
		Set<Integer> nodes = new HashSet<Integer>();
        try {
        	Connection conn  = DBConnection.getConnection(); String query = "select distinct ml.userid from movie as m, movie_like as ml where  m.id = ml.movieid";
            PreparedStatement ps = conn.prepareStatement(query);ResultSet rs = ps.executeQuery();
            while (rs != null && rs.next()) {
            	nodes.add(rs.getInt(1));
            } 
            closeQuery(conn,ps);
        } catch (SQLException ex) {
        	ex.printStackTrace();
        }
        hasBefore = false;
		return nodes;
	}	
	
	public Set<Integer> getUserIdsForLikedMusics() {
		Set<Integer> nodes = new HashSet<Integer>();
        try {
        	Connection conn  = DBConnection.getConnection(); String query = "select distinct ml.userid from music as m, music_like as ml where  m.id = ml.musicid";
            PreparedStatement ps = conn.prepareStatement(query);ResultSet rs = ps.executeQuery();
            while (rs != null && rs.next()) {
            	nodes.add(rs.getInt(1));
            } 
            closeQuery(conn,ps);
        } catch (SQLException ex) {
        	ex.printStackTrace();
        }
        hasBefore = false;
		return nodes;
	}	
	
	public Set<Integer> getUserIdsForLikedBooks() {
		Set<Integer> nodes = new HashSet<Integer>();
        try {
        	Connection conn  = DBConnection.getConnection(); String query = "select distinct ml.userid from book as m, book_like as ml where  m.id = ml.bookid";
            PreparedStatement ps = conn.prepareStatement(query);ResultSet rs = ps.executeQuery();
            while (rs != null && rs.next()) {
            	nodes.add(rs.getInt(1));
            } 
            closeQuery(conn,ps);
        } catch (SQLException ex) {
        	ex.printStackTrace();
        }
        hasBefore = false;
		return nodes;
	}	
	
}
	

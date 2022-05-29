package sum01;
import java.sql.*;

public class DataBase {
	String driver = "org.mariadb.jdbc.Driver";
	Connection conn;
	PreparedStatement pstmt;
	ResultSet rs;

	public DataBase() {
		try {
			Class.forName(driver);
			conn = DriverManager.getConnection("URL", "ID", "PASSWD");
			
			if (conn != null) {
				System.out.println("DB ���� ����");
			}
		} catch (Exception e) {
			System.err.println("DB ���� ����");
			System.err.println(e.getMessage());
		}
	}
	
	public int findFeedNum() {
		int num = 0;
		try {
			String query = "SELECT MAX(feeding_PortNum) AS num FROM tb_ultima_feeding where feeding_Order !=0 ";
			pstmt = conn.prepareStatement(query);
			rs = pstmt.executeQuery();

			while (rs.next()) {
				num= Integer.parseInt(rs.getString("num"));
			}
		} catch (SQLException e) {
			System.err.print(e);
		} finally {
			try {
				rs.close();
				pstmt.close();
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return num;
	}
	
	public int findSortNum() {
		int num = 0;
		try {
			String query = "SELECT maintenance_Channel AS num FROM group_ultima_maintenance";
			pstmt = conn.prepareStatement(query);
			rs = pstmt.executeQuery();

			while (rs.next()) {
				num= rs.getInt("num");
			}
		} catch (SQLException e) {
			System.err.print(e);
		} finally {
			try {
				rs.close();
				pstmt.close();
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return num;
	}
	
	public String lastMode() {
		String ModeName = null;
		try {
			String query = "SELECT maintenance_FinalUseMode FROM group_ultima_maintenance";
			pstmt = conn.prepareStatement(query);
			rs = pstmt.executeQuery();

			while (rs.next()) {
				ModeName= rs.getString("maintenance_FinalUseMode");
			}
		} catch (SQLException e) {
			System.err.print(e);
		} finally {
			try {
				rs.close();
				pstmt.close();
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return ModeName;
	}
	
	public int modeNum(String Name) {
		int num=0;
		try {
			String query = "SELECT mode_ID FROM tb_mode WHERE mode_Name=?";
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, Name);
			rs = pstmt.executeQuery();
			rs.next();
			num =rs.getInt("mode_ID");
		} catch (SQLException e) {
			System.err.print(e);
		} finally {
			try {
				rs.close();
				pstmt.close();
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return num;
	}
	public Connection returnConn() {
		return conn;
	}	
}
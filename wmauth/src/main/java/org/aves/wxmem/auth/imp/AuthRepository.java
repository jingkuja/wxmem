/**
 *  Created on  2015年12月28日
 */
package org.aves.wxmem.auth.imp;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.aves.wxmem.auth.api.Account;
import org.aves.wxmem.auth.tool.JdbcKit;

/**
 * @author nikin
 *
 */
public class AuthRepository {

	/**
	 * CREATE TABLE idgenerator( scheme VARCHAR(32) NOT NULL, id INT NOT NULL,
	 * PRIMARY KEY (id, scheme) ); CREATE TABLE auth( id VARCHAR(16) NOT NULL
	 * PRIMARY KEY, email VARCHAR(16) NOT NULL , password VARCHAR(64) NOT NULL,
	 * role VARCHAR(16) , aliasname VARCHAR(32) , extra VARCHAR(64) , status
	 * VARCHAR(2) NOT NULL, activetime TIMESTAMP(14) , lastLogintime
	 * TIMESTAMP(14) , createdtime TIMESTAMP(14) NOT NULL );
	 */

	private static final Logger logger = Logger.getLogger(AuthRepository.class
			.getName());

	private static final String initSequenceNumber = "insert into idgenerator(scheme,id) values('auth',1001)";

	private static final String nextSequenceNumber = "select id from idgenerator where scheme='auth' ";

	private static final String updateSequenceNumber = "update idgenerator set id=? where scheme='auth' ";

	private static final String insertAuth = "insert into auth(id,email,password,role,aliasname,extra,activetime,lastLogintime,createdtime,status) "
			+ "values(?,?,?,?,?,?,?,?,?,?)";

	private static final String updateAuth = "update auth set aliasname=?, status=?,activetime=?,lastLogintime=? "
			+ "where id=?";

	private static final String resetAuth = "update auth set password=? "
			+ "where id=?";

	private static final String suspendAuth = "update auth set status=? "
			+ "where id=?";

	private static final String Authbyid = "select id,email,password,role,aliasname,extra,activetime,lastLogintime,createdtime,status from auth   where id=? ";

	private static final String Authbyemail = "select id,email,password,role,aliasname,extra,activetime,lastLogintime,createdtime,status from auth  where email=?    and status=? ";

	private static final String listAuth = " select id,email,password,role,aliasname,extra,activetime,lastLogintime,createdtime,status from auth   where  1=1 ";
	private static final String totalAuth = "select count(*) from auth   where  1=1";

	private DataSource dataSource;

	/**
	 *
	 */
	public AuthRepository() {
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public String initSequenceNumber() throws SQLException {
		Connection conn = null;
		try {
			conn = dataSource.getConnection();
			PreparedStatement pstmt = conn.prepareStatement(initSequenceNumber);
			int affected = pstmt.executeUpdate();
			pstmt.close();
			if (affected > 0)
				return "1";
		} catch (SQLException e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		} finally {
			JdbcKit.close(conn, null, null);
		}
		return null;
	}

	public String updateSequenceNumber(int id) throws SQLException {
		Connection conn = null;
		try {
			conn = dataSource.getConnection();
			PreparedStatement pstmt = conn
					.prepareStatement(updateSequenceNumber);
			pstmt.setInt(1, id);
			int affected = pstmt.executeUpdate();
			pstmt.close();
			if (affected > 0)
				return "1";
		} catch (SQLException e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		} finally {
			JdbcKit.close(conn, null, null);
		}
		return null;
	}

	public Integer nextSequenceNumber() {
		Integer id = null;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			conn = dataSource.getConnection();
			pstmt = conn.prepareStatement(nextSequenceNumber);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				id = rs.getInt(1);
			}
		} catch (SQLException e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
			} catch (Exception e) {

			}
			try {
				if (pstmt != null) {
					pstmt.close();
				}
			} catch (Exception e) {

			}
			JdbcKit.close(conn, null, null);
		}
		return id;
	}

	public String insertAuth(Account account) throws SQLException {
		Connection conn = null;
		try {
			conn = dataSource.getConnection();
			PreparedStatement pstmt = conn.prepareStatement(insertAuth);
			pstmt.setString(1, account.getId());
			pstmt.setString(2, account.getEmail());
			pstmt.setString(3, account.getPassword());
			pstmt.setString(4, account.getRole());
			pstmt.setString(5, account.getAliasname());
			pstmt.setString(6, account.getExtra());
			pstmt.setTimestamp(7, getTimestamp(account.getActivetime()));
			pstmt.setTimestamp(8, getTimestamp(account.getLastLogintime()));
			pstmt.setTimestamp(9, getTimestamp(account.getCreatedtime()));
			pstmt.setString(10, account.getStatus());
			int affected = pstmt.executeUpdate();
			pstmt.close();
			if (affected > 0)
				return "1";
		} finally {
			JdbcKit.close(conn, null, null);
		}
		return null;
	}

	public String updateAuth(Account account) throws SQLException {
		Connection conn = null;
		try {
			conn = dataSource.getConnection();
			PreparedStatement pstmt = conn.prepareStatement(updateAuth);
			pstmt.setString(1, account.getAliasname());
			pstmt.setString(2, account.getStatus());
			pstmt.setTimestamp(3, getTimestamp(account.getActivetime()));
			pstmt.setTimestamp(4, getTimestamp(account.getLastLogintime()));
			pstmt.setString(5, account.getId());
			int affected = pstmt.executeUpdate();
			pstmt.close();
			if (affected > 0)
				return "1";
		} finally {
			JdbcKit.close(conn, null, null);
		}
		return null;
	}

	public String resetAuth(String password, String id) throws SQLException {
		Connection conn = null;
		try {
			conn = dataSource.getConnection();
			PreparedStatement pstmt = conn.prepareStatement(resetAuth);
			pstmt.setString(1, password);
			pstmt.setString(2, id);
			int affected = pstmt.executeUpdate();
			pstmt.close();
			if (affected > 0)
				return "1";
		} finally {
			JdbcKit.close(conn, null, null);
		}
		return null;
	}

	public String suspendAuth(String id) throws SQLException {
		Connection conn = null;
		try {
			conn = dataSource.getConnection();
			PreparedStatement pstmt = conn.prepareStatement(suspendAuth);
			pstmt.setString(1, "0");
			pstmt.setString(2, id);
			int affected = pstmt.executeUpdate();
			pstmt.close();
			if (affected > 0)
				return "1";
		} finally {
			JdbcKit.close(conn, null, null);
		}
		return null;
	}

	public Account Authbyid(String id) {
		Account account = null;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			conn = dataSource.getConnection();
			pstmt = conn.prepareStatement(Authbyid);
			pstmt.setString(1, id);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				account = new Account();
				account.setId(rs.getString(1));
				account.setEmail(rs.getString(2));
				account.setPassword(rs.getString(3));
				account.setRole(rs.getString(4));
				account.setAliasname(rs.getString(5));
				account.setExtra(rs.getString(6));
				account.setActivetime(rs.getTimestamp(7));
				account.setLastLogintime(rs.getTimestamp(8));
				account.setCreatedtime(rs.getTimestamp(9));
				account.setStatus(rs.getString(10));
			}
		} catch (SQLException e) {
			e.printStackTrace();
			logger.log(Level.SEVERE, e.getMessage(), e);
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
			} catch (Exception e) {

			}
			try {
				if (pstmt != null) {
					pstmt.close();
				}
			} catch (Exception e) {

			}
			JdbcKit.close(conn, null, null);
		}
		return account;
	}

	public Account Authbyemail(String email, String status) {
		Account account = null;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			conn = dataSource.getConnection();
			pstmt = conn.prepareStatement(Authbyemail);
			pstmt.setString(1, email);
			pstmt.setString(2, status);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				account = new Account();
				account.setId(rs.getString(1));
				account.setEmail(rs.getString(2));
				account.setPassword(rs.getString(3));
				account.setRole(rs.getString(4));
				account.setAliasname(rs.getString(5));
				account.setExtra(rs.getString(6));
				account.setActivetime(rs.getTimestamp(7));
				account.setLastLogintime(rs.getTimestamp(8));
				account.setCreatedtime(rs.getTimestamp(9));
				account.setStatus(rs.getString(10));
			}
		} catch (SQLException e) {
			e.printStackTrace();
			logger.log(Level.SEVERE, e.getMessage(), e);
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
			} catch (Exception e) {

			}
			try {
				if (pstmt != null) {
					pstmt.close();
				}
			} catch (Exception e) {

			}
			JdbcKit.close(conn, null, null);
		}
		return account;
	}

	public List<Map<String, Object>> listAuth(Map<String, String> para,
			int start, int limit) {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		Connection conn = null;
		Statement pstmt = null;
		ResultSet rs = null;
		try {
			conn = dataSource.getConnection();
			StringBuffer sb = new StringBuffer(listAuth);
			if (!getStirng(para.get("status")).equals(""))
				sb.append("  AND status=  '" + getStirng(para.get("status"))
						+ "'");
			pstmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			pstmt.setMaxRows(start + limit);
			sb.append("order by id ");
			rs = pstmt.executeQuery(sb.toString());
			rs.beforeFirst();
			if (start != 0)
				rs.absolute(start);
			while (rs.next()) {
				String id = rs.getString(1);
				String email = rs.getString(2);
				String role = rs.getString(4);
				String aliasname = rs.getString(5);
				String extra = rs.getString(6);
				String activetime = getTimeString(rs.getTimestamp(7));
				String lastLogintime = getTimeString(rs.getTimestamp(8));
				String createdtime = getTimeString(rs.getTimestamp(9));
				String status = rs.getString(10);
				Map<String, Object> cm = new HashMap<String, Object>();
				cm.put("id", id);
				cm.put("email", email);
				cm.put("role", role);
				cm.put("aliasname", aliasname);
				cm.put("extra", extra);
				cm.put("activetime", activetime);
				cm.put("lastLogintime", lastLogintime);
				cm.put("createdtime", createdtime);
				cm.put("status", status);
				list.add(cm);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			logger.log(Level.SEVERE, e.getMessage(), e);
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
			} catch (Exception e) {

			}
			try {
				if (pstmt != null) {
					pstmt.close();
				}
			} catch (Exception e) {

			}
			JdbcKit.close(conn, null, null);
		}
		return list;
	}

	public int totalAuth(Map<String, String> para) {
		Connection conn = null;
		Statement pstmt = null;
		ResultSet rs = null;
		int count = 0;
		try {
			conn = dataSource.getConnection();
			StringBuffer sb = new StringBuffer(totalAuth);
			if (!getStirng(para.get("status")).equals(""))
				sb.append("  AND status=  '" + getStirng(para.get("status"))
						+ "'");
			pstmt = conn.createStatement();
			rs = pstmt.executeQuery(sb.toString());
			while (rs.next()) {
				count = rs.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			logger.log(Level.SEVERE, e.getMessage(), e);
			return 1;
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
			} catch (Exception e) {

			}
			try {
				if (pstmt != null) {
					pstmt.close();
				}
			} catch (Exception e) {

			}
			JdbcKit.close(conn, null, null);
		}
		return count;
	}

	private String getStirng(String ob) {
		if (ob == null)
			return "";
		else
			return ob;
	}

	private String getTimeString(Date date) {
		if (date == null)
			return "";

		SimpleDateFormat sm = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

		return sm.format(date);
	}

	private Timestamp getTimestamp(Date date) {
		if (date == null)
			return null;
		return new Timestamp(date.getTime());
	}
}

package com.bytechainx.psi.common;

import java.sql.Connection;
import java.sql.SQLException;

import com.jfinal.plugin.activerecord.ActiveRecordException;
import com.jfinal.plugin.activerecord.Config;
import com.jfinal.plugin.activerecord.DbKit;

/**
 * 数据库事务
 * @author defier
 * 2015年1月6日 上午12:07:35
 * @version 1.0
 */
public class TransactionService {

	private Boolean autoCommit = null;
	private Connection conn = null;
	private Config config = null;
			
	public void startTransaction() {
		config = DbKit.getConfig();
		conn = config.getThreadLocalConnection();
		if (conn != null) {	// Nested transaction support
			try {
				if (conn.getTransactionIsolation() < config.getTransactionLevel())
					conn.setTransactionIsolation(config.getTransactionLevel());
				return ;
			} catch (SQLException e) {
				throw new ActiveRecordException(e);
			}
		}
		
		try {
			conn = config.getConnection();
			autoCommit = conn.getAutoCommit();
			config.setThreadLocalConnection(conn);
			conn.setTransactionIsolation(config.getTransactionLevel());	// conn.setTransactionIsolation(transactionLevel);
			conn.setAutoCommit(false);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		
	}
	
	public void commitTransaction() {
		try {
			conn.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void rollbackTransaction() {
		if (conn != null) 
			try {
				conn.rollback();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
	}
	
	
	public void closeTransaction() {
		try {
			if (conn != null) {
				if (autoCommit != null)
					conn.setAutoCommit(autoCommit);
				conn.close();
			}
		} catch (Exception e) {
			e.printStackTrace();	// can not throw exception here, otherwise the more important exception in previous catch block can not be thrown
		} finally {
			config.removeThreadLocalConnection();	// prevent memory leak
		}
	}
	
}

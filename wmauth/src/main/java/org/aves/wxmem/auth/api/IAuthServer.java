/**
 *  2013-6-5  上午10:59:38  IAuthServer.java
 */
package org.aves.wxmem.auth.api;

import java.util.List;
import java.util.Map;

/**
 * @author nikin
 * 
 */
public interface IAuthServer {

	/**
	 * generate uni id
	 * 
	 * @return
	 */
	public String nextSequenceNumber();

	/**
	 * 
	 * @param Account
	 * @return
	 * @throws AuthServerException
	 */
	public Account createAccount(Account acount) throws AuthServerException;

	/**
	 * 
	 * @param condition
	 * @param start
	 * @param limit
	 * @return
	 * @throws AuthServerException
	 */
	public List<Map<String, Object>> listAccounts(
			Map<String, String> condition, int start, int limit)
			throws AuthServerException;

	/**
	 * 
	 * @param condition
	 * @return
	 * @throws AuthServerException
	 */
	public int totalAccounts(Map<String, String> condition)
			throws AuthServerException;

	/**
	 * 
	 * @param account
	 * @return
	 * @throws AuthServerException
	 */
	public Account accountByEmail(String account, String status)
			throws AuthServerException;

	/**
	 * 
	 * @param account
	 * @return
	 * @throws AuthServerException
	 */
	public Account accountById(String id) throws AuthServerException;

	/**
	 * 
	 * @param Account
	 * @throws AuthServerException
	 */
	public void updateAccount(Account acount) throws AuthServerException;

	/**
	 * 
	 * @param Account
	 * @throws AuthServerException
	 */
	public void activeAccount(Account acount) throws AuthServerException;

	/**
	 * 
	 * @param Account
	 * @throws AuthServerException
	 */
	public void resetAccount(Account acount) throws AuthServerException;

	/**
	 * 
	 * @param account
	 * @throws AuthServerException
	 */
	public void suspendAccount(Account account) throws AuthServerException;

	public String getSession(String token) throws AuthServerException;

	public void setSession(String token, String store)
			throws AuthServerException;

	public void outSession(String token) throws AuthServerException;

}

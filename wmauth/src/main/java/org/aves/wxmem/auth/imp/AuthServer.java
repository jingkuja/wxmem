/**
 *  Created on  2015年12月28日
 */
package org.aves.wxmem.auth.imp;

import java.util.List;
import java.util.Map;

import org.aves.iora.redis.RedisSource;
import org.aves.wxmem.auth.api.Account;
import org.aves.wxmem.auth.api.AuthServerException;
import org.aves.wxmem.auth.api.IAuthServer;

/**
 * @author nikin
 *
 */
public class AuthServer implements IAuthServer {

	private AuthRepository authRepository;

	private RedisSource redisSource;

	public void setRedisSource(RedisSource redisSource) {
		this.redisSource = redisSource;
	}

	public void setAuthRepository(AuthRepository authRepository) {
		this.authRepository = authRepository;
	}

	public String nextSequenceNumber() {
		try {
			Integer id = authRepository.nextSequenceNumber();
			if (null == id) {
				authRepository.initSequenceNumber();
				id = 1000;
			} else {
				authRepository.updateSequenceNumber(id + 1);
			}
			return String.format("%d", id);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public Account createAccount(Account acount) throws AuthServerException {
		try {
			Account account = acount;
			account.setStatus("2");
			account.setId(nextSequenceNumber());
			Account accountd = authRepository.Authbyemail(acount.getEmail(),
					"1");
			if (accountd != null) {
				throw new AuthServerException("该邮箱账号已被激活使用，请检查修改!");
			}
			authRepository.insertAuth(account);
			return account;
		} catch (AuthServerException e) {
			throw new AuthServerException(e.getMessage());
		} catch (Exception e) {
			throw new AuthServerException("创建账号失败!");
		}
	}

	public List<Map<String, Object>> listAccounts(
			Map<String, String> condition, int start, int limit)
			throws AuthServerException {
		try {
			List<Map<String, Object>> ml = authRepository.listAuth(condition,
					start, limit);
			return ml;
		} catch (Exception e) {
			throw new AuthServerException("获取账号列表失败!");
		}
	}

	public int totalAccounts(Map<String, String> condition)
			throws AuthServerException {
		try {
			int i = authRepository.totalAuth(condition);
			return i;
		} catch (Exception e) {
			throw new AuthServerException("获取总数失败!");
		}
	}

	public Account accountByEmail(String account, String status)
			throws AuthServerException {
		try {
			Account acct = authRepository.Authbyemail(account, status);
			return acct;
		} catch (Exception e) {
			throw new AuthServerException("获取账号信息出错!");
		}
	}

	public Account accountById(String id) throws AuthServerException {
		try {
			Account acct = authRepository.Authbyid(id);
			return acct;
		} catch (Exception e) {
			throw new AuthServerException("获取账号信息出错!");
		}
	}

	public void updateAccount(Account acount) throws AuthServerException {
		try {
			authRepository.updateAuth(acount);
		} catch (Exception e) {
			throw new AuthServerException("修改账号信息出错!");
		}

	}

	public void activeAccount(Account acount) throws AuthServerException {
		try {
			authRepository.updateAuth(acount);
		} catch (Exception e) {
			throw new AuthServerException("激活账号信息出错!");
		}
	}

	public void resetAccount(Account acount) throws AuthServerException {
		try {
			authRepository.resetAuth(acount.getPassword(), acount.getId());
		} catch (Exception e) {
			throw new AuthServerException("重置密码出错!");
		}

	}

	public void suspendAccount(Account account) throws AuthServerException {
		try {
			authRepository.suspendAuth(account.getId());
		} catch (Exception e) {
			throw new AuthServerException("暂停账号信息出错!");
		}
	}

	public String getSession(String token) throws AuthServerException {
		return redisSource.get(token);
	}

	public void setSession(String token, String store)
			throws AuthServerException {
		redisSource.setOneDayValue(token, store);
	}

	public void outSession(String token) throws AuthServerException {
		redisSource.unset(token);
	}

}

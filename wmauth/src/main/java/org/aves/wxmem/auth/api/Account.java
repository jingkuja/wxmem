/**
 *  Created on  2015年12月24日
 */
package org.aves.wxmem.auth.api;

import java.util.Date;

/**
 * @author nikin
 *
 */
public class Account {

	public Account() {

	}

	private String id;

	private String email;

	private String password;

	private String role;

	private String aliasname;

	// 0删除 1正常 2代激活
	private String status;

	private Date createdtime;

	private Date lastLogintime;

	private Date activetime;

	private String extra;

	public boolean isExpired() {
		if (createdtime == null)
			return true;
		long dif = createdtime.getTime() + 24 * 60 * 60 * 1000;
		long now = System.currentTimeMillis();
		return now > dif;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getAliasname() {
		return aliasname;
	}

	public void setAliasname(String aliasname) {
		this.aliasname = aliasname;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Date getCreatedtime() {
		return createdtime;
	}

	public void setCreatedtime(Date createdtime) {
		this.createdtime = createdtime;
	}

	public Date getLastLogintime() {
		return lastLogintime;
	}

	public void setLastLogintime(Date lastLogintime) {
		this.lastLogintime = lastLogintime;
	}

	public String getExtra() {
		return extra;
	}

	public void setExtra(String extra) {
		this.extra = extra;
	}

	public Date getActivetime() {
		return activetime;
	}

	public void setActivetime(Date activetime) {
		this.activetime = activetime;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((createdtime == null) ? 0 : createdtime.hashCode());
		result = prime * result + ((email == null) ? 0 : email.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((role == null) ? 0 : role.hashCode());
		result = prime * result + ((status == null) ? 0 : status.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Account other = (Account) obj;
		if (createdtime == null) {
			if (other.createdtime != null)
				return false;
		} else if (!createdtime.equals(other.createdtime))
			return false;
		if (email == null) {
			if (other.email != null)
				return false;
		} else if (!email.equals(other.email))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (role == null) {
			if (other.role != null)
				return false;
		} else if (!role.equals(other.role))
			return false;
		if (status == null) {
			if (other.status != null)
				return false;
		} else if (!status.equals(other.status))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Account [email=" + email + ", aliasname=" + aliasname
				+ ", createdtime=" + createdtime + "]";
	}

}

/**
 *  Created on  2015年12月29日
 */
package org.aves.wxmem.auth.portal;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriBuilder;

import org.aves.iora.util.StringUtil;
import org.aves.wxmem.auth.api.Account;
import org.aves.wxmem.auth.api.AuthServerException;
import org.aves.wxmem.auth.api.IAuthServer;
import org.aves.wxmem.auth.email.EmailTask;
import org.aves.wxmem.auth.email.Emailbean;
import org.aves.wxmem.auth.json.JSONArray;
import org.aves.wxmem.auth.json.JSONObject;
import org.aves.wxmem.auth.tool.Digesttool;
import org.aves.wxmem.auth.tool.VertifyCodetool;

/**
 * @author nikin
 *
 */
@Path("auth")
public class AuthResource {

	private static final Logger logger = Logger.getLogger(AuthResource.class
			.getName());

	private IAuthServer authServer;

	private String sakey;

	private String emailaccont;

	private String emailaddress;

	private String emailpass;

	private String smtp;

	public String getSakey() {
		return sakey;
	}

	public void setSakey(String sakey) {
		this.sakey = sakey;
	}

	public String getEmailaccont() {
		return emailaccont;
	}

	public void setEmailaccont(String emailaccont) {
		this.emailaccont = emailaccont;
	}

	public String getEmailaddress() {
		return emailaddress;
	}

	public void setEmailaddress(String emailaddress) {
		this.emailaddress = emailaddress;
	}

	public String getEmailpass() {
		return emailpass;
	}

	public void setEmailpass(String emailpass) {
		this.emailpass = emailpass;
	}

	public String getSmtp() {
		return smtp;
	}

	public void setSmtp(String smtp) {
		this.smtp = smtp;
	}

	public IAuthServer getAuthServer() {
		return authServer;
	}

	public AuthResource() {

	}

	public void setAuthServer(IAuthServer authServer) {
		this.authServer = authServer;
	}

	@GET
	@Path("veimg")
	public Response veimg() {
		try {
			logger.info("1111dddd1");
			StringUtil.isEmpty("");
			logger.info("2222dddd22");

			VertifyCodetool vt = new VertifyCodetool();
			final Map<String, Object> resu = vt.geecode();
			String code = (String) resu.get("code");
			String tk = code + new Date().getTime() + "";
			String cookies = Digesttool.SHA1(tk);
			authServer.setSession(cookies, code);
			NewCookie cookie = new NewCookie(new Cookie("code", cookies, "/",
					null));
			return Response.ok(200)
					.header("Content-Type", "image/jpeg;Expires=0")
					.header("Pragma", "no-cache")
					.header("Cache-Control", "no-cache").cookie(cookie)
					.entity(new StreamingOutput() {
						public void write(OutputStream outputStream)
								throws IOException {
							BufferedImage buffImg = (BufferedImage) resu
									.get("img");
							ImageIO.write(buffImg, "jpeg", outputStream);
						}
					}).build();
		} catch (Exception e) {
			String re = "{\"status\":\"4\",\"msg\":\"" + e.getMessage() + "\"}";
			return Response.ok().entity(re).build();
		}
	}

	@POST
	@Path("login")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response login(String jsonStringContent,
			@CookieParam("code") String code) {
		JSONObject js;
		try {
			String msg = "登录!";
			String result = "1";
			js = new JSONObject(jsonStringContent);
			String email = js.optString("email", "");
			String pass = js.optString("pass", "");
			String veryCode = js.optString("veryCode", "");
			String sessioncode = authServer.getSession(code);
			if (sessioncode == null)
				throw new AuthServerException("验证码过期！");
			Account acctd = authServer.accountByEmail(email, "1");
			String repass = Digesttool.MD5(pass);
			if (acctd == null)
				throw new AuthServerException("该账号不存在！");
			if (!acctd.getPassword().equals(repass))
				throw new AuthServerException("密码错误！");
			if (!sessioncode.equals(veryCode.toUpperCase()))
				throw new AuthServerException("验证码输入错误！");
			authServer.outSession(code);
			if (acctd.getRole().equals("1"))
				result = "1";
			if (acctd.getRole().equals("2"))
				result = "2";
			StringBuffer sb = new StringBuffer(acctd.getId());
			sb.append("*");
			sb.append(acctd.getEmail());
			sb.append("*");
			sb.append(acctd.getRole());
			String auth = Digesttool.MD5(sb.toString());
			NewCookie cookie = new NewCookie(new Cookie("pinfo", sb.toString(),
					"/", null));
			NewCookie cookied = new NewCookie(new Cookie("auth", auth, "/",
					null));
			authServer.setSession(auth, sb.toString());
			return Response.ok().cookie(cookie).cookie(cookied)
					.entity(restemplet(result, msg)).build();
		} catch (AuthServerException e) {
			String re = restemplet("0", e.getMessage());
			return Response.ok().entity(re).build();
		} catch (Exception e) {
			String re = restemplet("0", "系统出错，请重试");
			return Response.ok().entity(re).build();
		}
	}

	@POST
	@Path("createAcct")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createAcct(String jsonStringContent) {
		JSONObject js;
		try {
			js = new JSONObject(jsonStringContent);
			Account acct = new Account();
			acct.setEmail(js.optString("email", ""));
			acct.setAliasname(js.optString("aliasname", ""));
			acct.setRole("2");
			acct.setPassword(Digesttool.MD5(js.optString("pass", "")));
			Date date = new Date();
			acct.setCreatedtime(date);
			acct = authServer.createAccount(acct);
			String re = restemplet("1", "创建成功");
			sendeactivemail(acct);
			return Response.ok().entity(re).build();
		} catch (AuthServerException e) {
			String re = restemplet("0", e.getMessage());
			return Response.ok().entity(re).build();
		} catch (Exception e) {
			String re = restemplet("0", "创建账户失败");
			return Response.ok().entity(re).build();
		}
	}

	@GET
	@Path("active")
	public Response active(@QueryParam("tk") String tk) {
		UriBuilder builder = null;
		String status = "a402";
		try {

			builder = UriBuilder
					.fromUri("http://api.abacus.com.cn/console/wmauth/index.html");
			String info[] = tk.split("_");
			Account acct = authServer.accountById(info[0]);
			if (acct == null) {
				status = "a403";
				throw new AuthServerException("该激活连接出错，请联系管理员。");
			}
			if (!acct.getStatus().equals("2")) {
				status = "a404";
				throw new AuthServerException("该账号已激活或者已经删除，请联系管理员!");
			}
			if (acct.isExpired()) {
				status = "a405";
				throw new AuthServerException("该激活连接有效时间已过，请重新操作!");
			}
			StringBuffer db = new StringBuffer(acct.getEmail());
			db.append(acct.getPassword());
			String rk = Digesttool.SHA1(db.toString());
			if (!info[1].equals(rk)) {
				status = "a403";
				throw new AuthServerException("该激活连接出错，请联系管理员。");
			}
			acct.setActivetime(new Date());
			acct.setStatus("1");
			authServer.activeAccount(acct);
			return Response.seeOther(
					builder.fragment("register/status/{sc}&{em}")
							.buildFromEncoded("201", acct.getEmail())).build();
		} catch (AuthServerException e) {
			return Response
					.seeOther(
							builder.fragment("error/{status}")
									.buildFromEncoded(status)).build();
		} catch (Exception e) {
			return Response.seeOther(
					builder.fragment("error/{status}").buildFromEncoded("401"))
					.build();
		}
	}

	@POST
	@Path("reset")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response reset(String jsonStringContent) {
		JSONObject js;
		try {
			js = new JSONObject(jsonStringContent);
			Account acct = authServer.accountByEmail(js.optString("email", ""),
					"1");
			if (acct == null)
				throw new AuthServerException("该邮箱未注册用户或该账号还没没激活，请检查!");
			sendresetemail(acct);
			String re = restemplet("1", "重置密码邮件已发送，请登录注册邮箱操作");
			return Response.ok().entity(re).build();
		} catch (AuthServerException e) {
			String re = restemplet("0", e.getMessage());
			return Response.ok().entity(re).build();
		} catch (Exception e) {
			String re = restemplet("0", "系统出错，请重试");
			return Response.ok().entity(re).build();
		}
	}

	@GET
	@Path("resetu")
	public Response resetu(@QueryParam("tk") String tk) {
		UriBuilder builder = null;
		String status = "r402";
		try {

			builder = UriBuilder
					.fromUri("http://api.abacus.com.cn/console/wmauth/index.html");
			String info[] = tk.split("_");
			Account acct = authServer.accountById(info[0]);
			if (acct == null) {
				status = "r403";
				throw new AuthServerException("该重置密码连接有误，请重试或联系管理员！");
			}
			long pt = new Date().getTime() - Long.valueOf(info[2]) - 24 * 60
					* 60 * 1000;
			StringBuffer db = new StringBuffer(acct.getEmail());
			db.append(acct.getCreatedtime().getTime() + "");
			db.append(info[2]);
			String rk = Digesttool.SHA1(db.toString());
			if (pt > 0) {
				status = "r404";
				throw new AuthServerException("该重置密码连接有效时间已过，请重新操作!");
			}
			if (!info[1].equals(rk)) {
				status = "r403";
				throw new AuthServerException("该重置密码操作存在错误，请重试或联系管理员!");
			}
			return Response.seeOther(
					builder.fragment("repass?em={em}&tk={tk}")
							.buildFromEncoded(acct.getEmail(), tk)).build();
		} catch (AuthServerException e) {
			return Response
					.seeOther(
							builder.fragment("error/{status}")
									.buildFromEncoded(status)).build();
		} catch (Exception e) {
			return Response
					.seeOther(
							builder.fragment("error/{status}")
									.buildFromEncoded("a401")).build();
		}
	}

	@POST
	@Path("repass")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response repass(String jsonStringContent) {
		JSONObject js;
		try {
			js = new JSONObject(jsonStringContent);
			String tk = js.optString("tk", "");
			String info[] = tk.split("_");
			Account acct = authServer.accountById(info[0]);
			if (acct == null)
				throw new AuthServerException("该次重置密码操作出错，请重试或联系管理员！");
			StringBuffer db = new StringBuffer(acct.getEmail());
			db.append(acct.getCreatedtime().getTime() + "");
			db.append(info[2]);
			String rk = Digesttool.SHA1(db.toString());
			if (!info[1].equals(rk))
				throw new AuthServerException("该次重置密码操作存在错误，请重试或联系管理员!");
			acct.setPassword(Digesttool.MD5(js.optString("pass", "")));
			authServer.resetAccount(acct);
			String re = restemplet("1", "重置密码邮件已发送，请登录注册邮箱操作");
			return Response.ok().entity(re).build();
		} catch (AuthServerException e) {
			String re = restemplet("0", e.getMessage());
			return Response.ok().entity(re).build();
		} catch (Exception e) {
			String re = restemplet("0", "系统出错，请重试");
			return Response.ok().entity(re).build();
		}
	}

	@POST
	@Path("suspend")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response suspend(String jsonStringContent) {
		try {
			Account acct = authServer.accountById(jsonStringContent);
			authServer.suspendAccount(acct);
			String re = restemplet("1", "注销账号成功!");
			return Response.ok().entity(re).build();
		} catch (Exception e) {
			String re = restemplet("0", e.getMessage());
			return Response.ok().entity(re).build();
		}
	}

	@POST
	@Path("listacct")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response listacct(String jsonStringContent) {
		try {
			Map<String, String> condition = new HashMap<String, String>();

			JSONObject js = new JSONObject(jsonStringContent);

			condition.put("status", "1");

			int s = 0;
			int l = 2;
			try {
				s = Integer.valueOf(js.optString("st", "0"));
				l = Integer.valueOf(js.optString("li", "0"));
			} catch (Exception e) {
			}
			List<Map<String, Object>> ml = authServer.listAccounts(condition,
					s, l);
			JSONObject da = new JSONObject();
			JSONArray ja = new JSONArray(ml);
			da.put("total", authServer.totalAccounts(condition));
			da.put("ne", s);
			da.put("con", ja);
			return Response.ok().entity(da.toString()).build();
		} catch (Exception e) {
			return Response.ok().entity("").build();
		}
	}

	private void sendeactivemail(Account acct) throws Exception {
		ExecutorService service = Executors.newSingleThreadExecutor();
		StringBuffer sb = new StringBuffer(acct.getId() + "_");
		StringBuffer db = new StringBuffer(acct.getEmail());
		db.append(acct.getPassword());
		sb.append(Digesttool.SHA1(db.toString()));
		String url = "http://api.abacus.com.cn/sei/wxmem/auth/active?tk="
				+ sb.toString();
		StringBuffer content = new StringBuffer("感谢您的注册，请点击以下链接以激活账号:");
		content.append(url);
		Emailbean em = new Emailbean(smtp, emailaddress, acct.getEmail(), null,
				"abacus微信会员系统账号激活", content.toString(), emailaccont, emailpass,
				null);
		service.execute(new EmailTask(em));
		service.shutdown();
	}

	private void sendresetemail(Account acct) throws Exception {
		ExecutorService service = Executors.newSingleThreadExecutor();
		String now = String.valueOf(new Date().getTime());
		StringBuffer sb = new StringBuffer(acct.getId() + "_");
		StringBuffer db = new StringBuffer(acct.getEmail());
		db.append(acct.getCreatedtime().getTime() + "");
		db.append(now);
		sb.append(Digesttool.SHA1(db.toString()));
		sb.append("_");
		sb.append(now);
		String url = "http://api.abacus.com.cn/sei/wxmem/auth/resetu?tk="
				+ sb.toString();
		StringBuffer content = new StringBuffer("请点击以下链接以修改账号密码:");
		content.append(url);
		Emailbean em = new Emailbean(smtp, emailaddress, acct.getEmail(), null,
				"abacus微信会员系统密码", content.toString(), emailaccont, emailpass,
				null);
		service.execute(new EmailTask(em));
		service.shutdown();
	}

	private String restemplet(String status, String msg) {
		String templet = "{\"status\":\"%1$s\",\"msg\":\"%2$s\"}";
		return String.format(templet, status, msg);
	}
}

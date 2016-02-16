package org.aves.wxmem.auth.api;

import org.aves.wxmem.auth.tool.Digesttool;

public class Test {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		System.out.println(Digesttool.MD5("wxmemaves"));

		String templet = "{\"status\":\"%1$s\",\"msg\":\"%2$s\"}";
		System.out.println(String.format(templet, "1", "da"));

	}
}

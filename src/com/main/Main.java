package com.main;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.bean.User;
import com.utils.MySQLUtil;

public class Main {

	MySQLUtil db;

	String host = "127.0.0.1";
	String dbName = "mysql";
	String user = "root";
	String paw = "root";

	public Main() {
		db = new MySQLUtil(host, dbName, user, paw);
	}

	@Path(id = 0, value = "", info = "所有接口信息")
	public List<JsonInfo> index(HttpServletRequest request){
		return MainServlet.jsonInfo(request);
	}

	@Path(id = 1, value = "HelloJson.json", info="Hello JSON")
	public String helloJson(){
		return "Hello JSON";
	}

	@Path(id = 2, value = "HelloPostJson.json", method="POST", info = "Hello POST JSON")
	public String helloPostJson(){
		return "Hello POST JSON";
	}

	@Path(id = 3, value = "getUser.json", info = "获得所有用户信息")
	public ArrayList<?> getUser() {
		return db.getListAll(User.class);
	}

}

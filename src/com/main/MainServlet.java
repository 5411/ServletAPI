package com.main;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;

@WebServlet("/*")
public class MainServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		response.setContentType("text/json;charset=UTF-8");
		response.addHeader("name", "JiangTao");
		response.setHeader("Access-Control-Allow-Origin", "*");

		// 字符流
		response.getWriter().write(JSON.toJSONString(MainServlet.getResult(request)));
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/json;charset=UTF-8");
		response.addHeader("name", "JiangTao");
		response.setHeader("Access-Control-Allow-Origin", "*");

		response.getWriter().write(JSON.toJSONString(MainServlet.getPostResult(request)));
	}

	/**
	 * 获得GET请求结果
	 */
	public static Object getResult(HttpServletRequest request) {

		String path = request.getRequestURI();

		Main obj = new Main();
		Class<?> c = obj.getClass();
		Method[] methods = c.getDeclaredMethods();
		for (Method f : methods) {
			if (f.isAnnotationPresent(Path.class)) {
				if (path.equals("/" + f.getAnnotation(Path.class).value())) {
					if(f.getAnnotation(Path.class).method().toUpperCase().startsWith("GET")){
						try {
							f.setAccessible(true);
							Object body = f.invoke(obj, request);
							return new ResultEntity(200, "OK", body == null ? "" : body);
						} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
							//	e.printStackTrace();
							try {
								f.setAccessible(true);
								Object body = f.invoke(obj);
								return new ResultEntity(200, "OK", body == null ? "" : body);
							} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e1) {
								//	e1.printStackTrace();
								return new ResultEntity(500, "接口异常！！！", "");
							}
						}
					}
					return new ResultEntity(405, "无效的请求方法,请尝试 POST请求", null);
				}
			}
		}

		return new ResultEntity(404, "无效的请求", null);
	}

	/**
	 * 获得POST请求结果
	 */
	public static Object getPostResult(HttpServletRequest request) {

		String path = request.getRequestURI();

		Main obj = new Main();
		Class<?> c = obj.getClass();
		Method[] methods = c.getDeclaredMethods();
		for (Method f : methods) {
			if (f.isAnnotationPresent(Path.class)) {
				if (path.equals("/" + f.getAnnotation(Path.class).value())) {
					if(f.getAnnotation(Path.class).method().toUpperCase().endsWith("POST")){
						try {
							f.setAccessible(true);
							Object body = f.invoke(obj, request);
							return new ResultEntity(200, "OK", body == null ? "" : body);
						} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
							//	e.printStackTrace();
							try {
								f.setAccessible(true);
								Object body = f.invoke(obj);
								return new ResultEntity(200, "OK", body == null ? "" : body);
							} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e1) {
								//	e1.printStackTrace();
								return new ResultEntity(500, "接口异常！！！", "");
							}
						}
					}
					return new ResultEntity(405, "无效的请求方法,请尝试 GET请求", null);
				}
			}
		}

		return new ResultEntity(404, "无效的请求", null);
	}

	/**
	 * 首页
	 */
	public static List<JsonInfo> jsonInfo(HttpServletRequest request){

		List<JsonInfo> list = new ArrayList<JsonInfo>();

		Main obj = new Main();
		Class<?> c = obj.getClass();
		//	Method[] methods = c.getMethods();
		Method[] methods = c.getDeclaredMethods();
		for(Method f : methods) {
			if(f.isAnnotationPresent(Path.class)){
				Path ann = f.getAnnotation(Path.class);
				list.add(new JsonInfo(ann.id(), request.getRequestURL() + ann.value(), ann.method(), ann.info()));
			}
		}

		Collections.sort(list, new Comparator<JsonInfo>() {

			@Override
			public int compare(JsonInfo lhs, JsonInfo rhs) {
				return lhs.getId() - rhs.getId();
			}
		});

		return list;
	}

}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface Path {

	int id() default 0;
	String value();
	String method() default "GET/POST";
	String info() default "暂未添加信息";
}


class ResultEntity {

	@JSONField(ordinal = 1)
	int code;
	@JSONField(ordinal = 2)
	String msg;
	@JSONField(ordinal = 3)
	Object body;

	public ResultEntity(int code, String msg, Object body) {
		super();
		this.code = code;
		this.msg = msg;
		this.body = body;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public Object getBody() {
		return body;
	}

	public void setBody(Object body) {
		this.body = body;
	}

}

class JsonInfo{

	@JSONField(ordinal = 1)
	int id;
	@JSONField(ordinal = 2)
	String path;
	@JSONField(ordinal = 3)
	String method;
	@JSONField(ordinal = 4)
	String info;


	public JsonInfo(int id, String path, String method, String info) {
		super();
		this.id = id;
		this.path = path;
		this.method = method;
		this.info = info;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}
}

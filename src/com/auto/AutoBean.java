package com.auto;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class AutoBean {

	static String host = "127.0.0.1";
	static String dbName = "mysql";
	static String user = "root";
	static String paw = "root";

	// 格式 com.bean, 为空则取当前包名
	static String packageName = "com.bean";

	public static void main(String[] args) {

		// 若包名为空,取当前包名
		if (packageName == null || packageName.trim().equals("")) {
			packageName = AutoBean.class.getPackage().toString().substring(8);
		} else {
			// 创建路径
			new File("src/" + packageName.replace('.', '/') + "/").mkdirs();
		}

		AutoBean my = new AutoBean();

		// 表信息
		Map<String, String> tablesInfo = my.getTableName(dbName);

		int tableCount = 0;
		// 遍历表
		for (Map.Entry<String, String> entry : tablesInfo.entrySet()) {

			// 只保留基础表,跳过视图
			if (!entry.getValue().equals("BASE TABLE")) {
				continue;
			}

			boolean b = my.tableToFile(entry.getKey());

			System.out.println(b + " -- " + dbName + "."
					+ my.toOneUpperCase(entry.getKey()) + ".java");

			// 计数
			if (b) {
				tableCount++;
			}

		}
		System.out.println("\nAutoBean Count:" + tableCount);
	}

	/**
	 * 数据类型对应关系
	 */
	public Map<String, String> getTypeMap() {

		Map<String, String> typeMap = new HashMap<String, String>();

		// 数值
		typeMap.put("tinyint", "int");
		typeMap.put("tinyint unsigned", "int");
		typeMap.put("tinyint unsigned zerofill", "int");

		typeMap.put("smallint", "int");
		typeMap.put("smallint unsigned", "int");
		typeMap.put("smallint unsigned zerofill", "int");

		typeMap.put("mediumint", "int");
		typeMap.put("mediumint unsigned", "int");
		typeMap.put("mediumint unsigned zerofill", "int");

		typeMap.put("int", "int");
		typeMap.put("int unsigned", "long");
		typeMap.put("int unsigned zerofill", "long");

		//	typeMap.put("integer", "int");

		typeMap.put("bigint", "long");
		typeMap.put("bigint unsigned", "java.math.BigInteger");
		typeMap.put("bigint unsigned zerofill", "java.math.BigInteger");

		typeMap.put("float", "float");
		typeMap.put("float unsigned", "float");
		typeMap.put("float unsigned zerofill", "float");

		typeMap.put("double", "double");
		typeMap.put("double unsigned", "double");
		typeMap.put("double unsigned zerofill", "double");

		typeMap.put("decimal", "java.math.BigDecimal");
		typeMap.put("decimal unsigned", "java.math.BigDecimal");
		typeMap.put("decimal unsigned zerofill", "java.math.BigDecimal");


		// 字符
		typeMap.put("char", "String");
		typeMap.put("varchar", "String");
		typeMap.put("tinytext", "String");
		typeMap.put("text", "String");
		typeMap.put("mediumtext", "String");
		typeMap.put("longtext", "String");
		typeMap.put("binary", "byte[]");
		typeMap.put("varbinary", "byte[]");
		typeMap.put("tinyblob", "byte[]");
		typeMap.put("blob", "byte[]");
		typeMap.put("mediumblob", "byte[]");
		typeMap.put("longblob", "byte[]");


		// 日期时间
		typeMap.put("date", "java.sql.Date");
		typeMap.put("time", "java.sql.Time");
		typeMap.put("year", "java.sql.Date");
		typeMap.put("datetime", "java.sql.Timestamp");
		typeMap.put("timestamp", "java.sql.Timestamp");


		// 其他
		typeMap.put("bit", "boolean");
		typeMap.put("enum", "String");
		typeMap.put("set", "String");

		// 几何
		typeMap.put("point", "Object");
		typeMap.put("multipoint", "Object");
		typeMap.put("linestring", "Object");
		typeMap.put("multilinestring", "Object");
		typeMap.put("polygon", "Object");
		typeMap.put("multipolygon", "Object");
		typeMap.put("geometry", "Object");
		typeMap.put("geometrycollection", "Object");

		return typeMap;
	}

	/**
	 * 删除字符串中的空格,将首字母大写并返回
	 */
	public String toOneUpperCase(String str) {
		return str.substring(0, 1).toUpperCase() + str.substring(1);
	}

	/**
	 * 获取数据库中的所有表信息
	 */
	public Map<String, String> getTableName(String dbName) {
		String sql = "select table_name, table_type from information_schema.tables where table_schema='"
				+ dbName + "';";
		return select(sql);
	}

	/**
	 * 获取表中所有的列信息
	 */
	public Map<String, String> getColumnName(String dbName, String tableName) {
		String sql = "select column_name, column_type from information_schema.columns where table_schema='"
				+ dbName + "' and table_name='" + tableName + "';";
		return select(sql);
	}

	/**
	 * 查询SQL并返回结果
	 */
	private Map<String, String> select(String sql) {

		Map<String, String> map = new HashMap<String, String>();

		Connection conn = getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				map.put(rs.getString(1), rs.getString(2));

			}
			return map;

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (ps != null) {
					ps.close();
				}
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * 连接到 MySQL服务器
	 */
	private Connection getConnection() {
		return getConnection(host, dbName, user, paw);
	}

	/**
	 * 连接到指定的 MySQL服务器
	 */
	private Connection getConnection(String host, String dbName, String user,
			String paw) {
		Connection conn = null;

		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection("jdbc:mysql://" + host
					+ ":3306/" + dbName, user, paw);
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
		return conn;
	}

	/**
	 * 写到文件
	 */
	private boolean writeFile(String fileName, String date) {

		try {
			FileWriter fw = new FileWriter(fileName);
			fw.write(date);
			fw.flush();
			fw.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 数据表转化为 JAVA Bean
	 */
	private boolean tableToFile(String tableName) {

		AutoBean my = new AutoBean();

		// 表名转换为类名
		String className = my.toOneUpperCase(tableName);

		// 最终字符串
		StringBuffer str = new StringBuffer();
		// 添加包名
		str.append("package " + packageName + ";");
		// 添加类名
		str.append("public class " + className + "{");
		// 构造函数
		StringBuffer constructorStr = new StringBuffer();
		constructorStr.append("public " + className + "() {super();}");
		constructorStr.append("public " + className + "(");

		// 带有所有字段的构造函数
		StringBuffer constructorStr2 = new StringBuffer();

		// toString 方法
		StringBuffer toStringStr = new StringBuffer();
		toStringStr.append("@Override public String toString(){ return \""
				+ className + " [\"");

		// 数据类型对应关系
		Map<String, String> typeMap = my.getTypeMap();

		// 获取当前表的列信息
		Map<String, String> columnsInfo = my.getColumnName(dbName, tableName);
		// 遍历列
		for (Map.Entry<String, String> entry : columnsInfo.entrySet()) {
			// MySQL 类型转换为 JAVA类型
			String type = typeMap.get(entry.getValue().replaceFirst("\\(.*\\)",""));
			String name = entry.getKey();

			// 实例变量
			str.append(type + " " + name + ";");
			// GET方法
			str.append("public " + type + " get" + my.toOneUpperCase(name)
					+ "(){return " + name + ";}");
			// SET方法
			str.append("public void set" + my.toOneUpperCase(name) + "(" + type
					+ " " + name + "){" + "this." + name + "=" + name + ";}");
			// 构造方法
			constructorStr.append(type + " " + name + ", ");
			constructorStr2.append("this." + name + "=" + name + ";");
			// toString方法
			toStringStr.append("+ \"  " + name + "=\" + " + name);
		}
		constructorStr.setLength(constructorStr.length() - 2);
		constructorStr.append("){super();");
		constructorStr.append(constructorStr2);
		constructorStr.append("}");
		// 添加构造方法
		str.append(constructorStr);
		// toString 方法
		toStringStr.append(" + \"  ]\";}");
		str.append(toStringStr);
		str.append("}");

		// 写到文件
		return my.writeFile("src/" + packageName.replace('.', '/') + "/"
				+ className + ".java", str.toString());
	}
}

package com.utils;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;

public class MySQLUtil {

	Connection conn;

	public MySQLUtil(){
		conn = getConnection();
	}

	/**
	 * 连接指定 MySQL服务器
	 */
	public MySQLUtil(String host, String dbName, String user, String paw){
		conn = getConnection(host, dbName, user, paw);
	}

	/**
	 * 默认连接本地 MySQL服务器  127.0.0.0 mysql root root
	 */
	private Connection getConnection(){
		return getConnection("127.0.0.1", "mysql", "root", "root");
	}

	/**
	 * 获得给定主机、数据库、用户名、密码的连接
	 */
	private Connection getConnection(String host, String dbName, String user, String paw){
		Connection conn = null;

		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection("jdbc:mysql://" + host + ":3306/" + dbName, user, paw);
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
		return conn;
	}

	/**
	 * 获得连接
	 */
	public Connection getConn() {
		return conn;
	}

	/**
	 * 设置一个连接
	 */
	public void setConn(Connection conn) {
		this.conn = conn;
	}

	/**
	 * 关闭连接
	 */
	public void closeConn(){
		try {
			if(conn != null){
				conn.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 关闭资源
	 */
	private void closeRes(PreparedStatement ps, ResultSet rs){

		try {
			if(rs != null){
				rs.close();
			}
			if(ps != null){
				ps.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}


	////////////////////////////// 查询   /////////////////////////////////
	/**
	 * 万能查询
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private ArrayList<?> select(Class<?> c, String name, Object value){

		ArrayList list = new ArrayList();

		String sql;
		if(value != null){
			sql = "select * from " + c.getSimpleName() + " where " + name + "='" + value + "'";
		}else{
			sql = "select * from " + c.getSimpleName() + " where " + name;
		}

		PreparedStatement ps = null;
		ResultSet rs = null;
		Field[] fields = c.getDeclaredFields();
		try {
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while(rs.next()){
				Object obj = c.newInstance();
				for(Field field : fields){
					field.setAccessible(true);
					try{
						field.set(obj, rs.getObject(field.getName()));
					}catch(Exception e){
						System.err.println(field.getName() + "字段不存在");
						//	e.printStackTrace();
					}
				}
				list.add(obj);
			}
		} catch (SQLException | InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}finally{
			closeRes(ps, rs);
		}
		return list;
	}

	/**
	 * 根据 ID返回相应 Object
	 */
	public Object getObjById(Class<?> c, int id){
		return select(c, "id", id).get(0);
	}

	/**
	 * 根据 where条件返回对象 ArrayList
	 */
	public ArrayList<?> getListByWhere(Class<?> c, String where){
		return select(c, where, null);
	}

	/**
	 * 根据字段和值返回对象 ArrayList
	 */
	public ArrayList<?> getListByParam(Class<?> c, String name, Object value){
		return select(c, name, value);
	}

	/**
	 * 全表返回 ArrayList
	 */
	public ArrayList<?> getListAll(Class<?> c){
		return select(c, "1", "1");
	}


	//////////////////////////////// 添加  ///////////////////////////////////
	/**
	 * 插入 ArrayList
	 */
	@SuppressWarnings("rawtypes")
	private int insert(ArrayList list, int batchSize, boolean notId){

		int index = 1;
		if(notId){
			index = 0;
		}

		Class<?> c = list.get(0).getClass();
		Field[] fields = c.getDeclaredFields();
		StringBuffer sql = new StringBuffer();
		StringBuffer sqlEnd = new StringBuffer();
		sql.append("insert into " + c.getSimpleName() + " (");
		sqlEnd.append("(");
		for(int i = index; i < fields.length; i++){
			if(i < fields.length - 1){
				sql.append(fields[i].getName() + ", ");
				sqlEnd.append("?, ");
			}else{
				sql.append(fields[i].getName());
				sqlEnd.append("?");
			}
		}
		sql.append(") values");
		sql.append(sqlEnd + ")");
		PreparedStatement ps = null;
		int resultCount = 0;
		try {
			conn.setAutoCommit(false);
			ps = conn.prepareStatement(sql.toString());
			for(int i = 0; i < list.size(); i++){
				for(int j = index; j < fields.length; j++){
					fields[j].setAccessible(true);
					ps.setObject(j + (notId?1:0), fields[j].get(list.get(i)));
				}
				ps.addBatch();
				if((i + 1) % batchSize == 0){
					resultCount += ps.executeBatch().length;
					conn.commit();
					System.out.println("已提交：" + resultCount);
				}
			}
			resultCount += ps.executeBatch().length;
			conn.commit();
			return resultCount;
		} catch (SQLException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		} finally{
			closeRes(ps, null);
		}
		return -1;
	}

	/**
	 * 插入 ArrayList,默认100条数据提交一次
	 */
	public int insertList(ArrayList<?> list){
		return insert(list, 100, false);
	}

	/**
	 * 插入 ArrayList,指定每次提交条数
	 */
	public int insertList(ArrayList<?> list, int batchSize){
		return insert(list, batchSize, false);
	}

	/**
	 * 插入对象 ArrayList 原表不存在id列,默认100条数据提交一次
	 */
	public int insertListNotId(ArrayList<?> list){
		return insert(list, 100, true);
	}

	/**
	 * 插入对象 ArrayList 原表不存在id列,指定每次提交条数
	 */
	public int insertListNotId(ArrayList<?> list, int batchSize){
		return insert(list, batchSize, true);
	}

	/**
	 * 插入一个 Object
	 */
	public int insertObj(Object obj){
		ArrayList<Object> list = new ArrayList<Object>();
		list.add(obj);
		return insert(list, 1, false);
	}

	/**
	 * 插入一个 Object 原表不存在id列
	 */
	public int insertObjNotId(Object obj){
		ArrayList<Object> list = new ArrayList<Object>();
		list.add(obj);
		return insert(list, 1, true);
	}


	////////////////////////////////// 删除  ////////////////////////////////////////
	/**
	 * 删除
	 */
	private int delete(Class<?> c, String name, Object obj){

		String sql;
		if(obj == null){
			sql = "delete from " + c.getSimpleName() + " where " + name;
		}else{
			sql = "delete from " + c.getSimpleName() + " where " + name + "=?";
		}
		PreparedStatement ps = null;

		try {
			ps = conn.prepareStatement(sql);
			ps.setObject(1, obj);
			return ps.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			closeRes(ps, null);
		}

		return -1;
	}

	/**
	 * 根据 ID删除一项
	 */
	public int deleteObjById(Class<?> c, int id){
		return delete(c, "id", id);
	}

	/**
	 * 根据 ID数组删除多项
	 */
	public int deleteArr(Class<?> c, int[] ids){
		int resultCount = 0;
		for(int id : ids){
			int resule = delete(c, "id", id);
			if(resule != -1){
				resultCount += resule;
			}
		}
		return resultCount;
	}

	/**
	 * 删除 ArrayList对象(根据类的第一个字段删除)
	 */
	public int deleteList(ArrayList<?> list){
		int resultCount = 0;
		for(int i = 0; i < list.size(); i++){
			Class<?> c = list.get(i).getClass();
			Field[] fields = c.getDeclaredFields();
			int resule = 0;
			try {
				fields[0].setAccessible(true);
				resule = delete(c, fields[0].getName(), fields[0].get(list.get(i)));
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}

			if(resule != -1){
				resultCount += resule;
			}
		}
		return resultCount;
	}

	/**
	 * 根据参数删除
	 */
	public int deleteByParam(Class<?> c, String name, Object value){
		return delete(c, name, value);
	}

	/**
	 * 根据 WHERE条件删除
	 */
	public int deleteByWhere(Class<?> c, String where){
		return delete(c, where, null);
	}

	////////////////////////////////// 更新  ///////////////////////////////////////////
	/**
	 * 更新
	 */
	private int update(Object obj){

		Class<?> c = obj.getClass();
		StringBuffer sql = new StringBuffer();
		sql.append("update " + c.getSimpleName() + " set ");

		Field[] fields = c.getDeclaredFields();
		for(int i = 1; i < fields.length; i++){
			fields[i].setAccessible(true);
			if(i < fields.length-1){
				sql.append(fields[i].getName() + "=?, ");
			}else{
				sql.append(fields[i].getName() + "=? ");
			}
		}
		sql.append("where id=?");

		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement(sql.toString());
			for(int i = 1; i < fields.length; i++){
				fields[i].setAccessible(true);
				ps.setObject(i, fields[i].get(obj));
			}
			fields[0].setAccessible(true);
			ps.setObject(fields.length, fields[0].get(obj));

			return ps.executeUpdate();

		} catch (SQLException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		} finally{
			closeRes(ps, null);
		}

		return -1;
	}

	/**
	 * 更新一个对象(第一个必须为ID列)
	 */
	public int updateObj(Object obj){
		return update(obj);
	}

	/**
	 * 更新对象 ArrayList
	 */
	public int updateList(ArrayList<?> list){
		int resultCount = 0;
		for(Object obj : list){
			int result = update(obj);
			if(result != -1){
				resultCount += result;
			} else{
				System.err.println("更新错误：" + obj);
			}
		}
		return resultCount;
	}


	////////////////////////////////// SQL  ///////////////////////////////////////////
	/**
	 * 执行SQL以String[][] 的形式返回
	 */
	private String[][] select(String sql, boolean isTHead){

		String[][] result;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();

			rs.last();
			ResultSetMetaData rsmd = rs.getMetaData();
			int row = rs.getRow();
			int line = rsmd.getColumnCount();
			int r = 0;
			if(isTHead){
				result = new String[row + 1][line];
				for(int i = 0; i < line; i++){
					result[0][i] = rsmd.getColumnName(i + 1);
				}
				r = 1;
			}else{
				result = new String[row][line];
			}
			rs.beforeFirst();
			while(rs.next()){
				for(int i = 0; i < line; i++){
					result[r][i] = rs.getString(i + 1);
				}
				r++;
			}

			return result;
		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			closeRes(ps, rs);
		}
		return null;
	}

	/**
	 * 执行SQL以 String[][] 的形式带表头返回
	 */
	public String[][] select(String sql){
		return select(sql, true);
	}

	/**
	 * 执行SQL以 String[][] 的形式不带表头返回
	 */
	public String[][] selectNotTHead(String sql){
		return select(sql, false);
	}

	public static void main(String[] args) {

		String[] ss = "select * from user".split(" ");
		for(String s : ss) {
			System.out.println(s);
		}

	}
}
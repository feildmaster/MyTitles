// Copyright (C) 2011 by Danny de Jong
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
// 
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
// 
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.

package com.dannycrafts.myTitles;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {
	
	private static Connection connection;
	private static String tablePrefix;
	
	protected static void connect( String host, String port, String database, String username, String password, String tablePrefix ) throws SQLException
	{
		try
		{
			connection = DriverManager.getConnection( "jdbc:mysql://" + host + ":" + port + "/" + database + "?user=" + username + "&password=" + password );
		}
		catch ( SQLException e )
		{
			if ( e.getErrorCode() == 1049 )
			{
				connection = DriverManager.getConnection( "jdbc:mysql://" + host + ":" + port + "/?user=" + username + "&password=" + password );
			
				Database.update( "CREATE DATABASE " + database + ";" );
				Database.update( "USE " + database + ";" );
			}
			else
				throw e;
		}
		
		Database.tablePrefix = tablePrefix;
	}
	
	protected static void disconnect() throws SQLException
	{
		connection.close();
	}
	
	protected static String formatString( String string ) 
	{
		if ( string != null )
		{
			string.replace( "\\", "\\\\" );
			string.replace( "\'", "\\\'" );
			string.replace( "\"", "\\\"" );
			
			return "'" + string + "'";
		}
		return "NULL";
	}
	
	protected static String formatTableName( String tableName )
	{
		return Database.tablePrefix + tableName;
	}
	
	protected static int update( String query ) throws SQLException {
		
		Statement statement = connection.createStatement();
		return statement.executeUpdate( query );
	}
	
	protected static ResultSet query( String query ) throws SQLException {
		
		Statement statement = connection.createStatement();
		return statement.executeQuery( query );
	}
}

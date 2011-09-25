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

import java.sql.ResultSet;
import java.sql.SQLException;

public class Variation {
	
	protected Long id;
	protected Title title;
	
	protected Variation( Title title, Long id )
	{
		this.title = title;
		this.id = id;
	}
	
	public Affixes getAffixes() throws SQLException
	{
		ResultSet result = Database.query( "SELECT prefix, suffix FROM " + Database.formatTableName( "title_variations" ) + " WHERE id = " + id + ";" );
		result.first();
		return new Affixes( result.getString( "prefix" ), result.getString( "suffix" ) );
	}
	
	public Info getInfo() throws SQLException
	{
		ResultSet result = Database.query( "SELECT name, prefix, suffix FROM " + Database.formatTableName( "title_variations" ) + " WHERE id = " + id + ";" );
		result.first();
		return new Info( result.getString( "name" ), result.getString( "prefix" ), result.getString( "suffix" ) );
	}
	
	public String getName() throws SQLException
	{
		ResultSet result = Database.query( "SELECT name FROM " + Database.formatTableName( "title_variations" ) + " WHERE id = " + id + ";" );
		result.first();
		return result.getString( "name" );
	}
	
	public String getPrefix() throws SQLException
	{
		ResultSet result = Database.query( "SELECT prefix FROM " + Database.formatTableName( "title_variations" ) + " WHERE id = " + id + ";" );
		result.first();
		return result.getString( "prefix" );
	}
	
	public String getSuffix() throws SQLException
	{
		ResultSet result = Database.query( "SELECT suffix FROM " + Database.formatTableName( "title_variations" ) + " WHERE id = " + id + ";" );
		result.first();
		return result.getString( "suffix" );
	}
	
	public boolean isVanilla()
	{
		return id == 0;
	}
	
	private boolean isValidName( String name )
	{
		for ( int i = 0; i < name.length(); i++ )
		{
			int codePoint = name.codePointAt(i);
			
			if (	codePoint != 45 &&
					codePoint < 48 && codePoint > 57 &&
					codePoint < 65 && codePoint > 90 &&
					codePoint != 95 &&
					codePoint < 97 && codePoint > 122 )
				return false;
		}
		
		return true;
	}
	
	public void setInfo( Info info ) throws SQLException
	{
		Database.update( "UPDATE " + Database.formatTableName( "title_variations" ) + " SET name = " + Database.formatString( info.name ) + ", prefix = " + Database.formatString( info.affixes.prefix ) + ", postfix = " + Database.formatString( info.affixes.suffix ) + " WHERE id = " + id + ";" );
	}
	
	public void setPrefix( Affixes affixes ) throws SQLException
	{
		Database.update( "UPDATE " + Database.formatTableName( "title_variations" ) + " SET prefix = " + Database.formatString( affixes.prefix ) + ", postfix = " + Database.formatString( affixes.suffix ) + " WHERE id = " + id + ";" );
	}
	
	public void setPrefix( String prefix ) throws SQLException
	{
		Database.update( "UPDATE " + Database.formatTableName( "title_variations" ) + " SET prefix = " + Database.formatString( prefix ) + " WHERE id = " + id + ";" );
	}
	
	public void setName( String name ) throws SQLException, Variation.InvalidNameException
	{
		if ( !isValidName( name ) ) throw new Variation.InvalidNameException();
		Database.update( "UPDATE " + Database.formatTableName( "title_variations" ) + " SET prefix = " + Database.formatString( name ) + " WHERE id = " + id + ";" );		
	}
	
	public void setSuffix( String suffix ) throws SQLException
	{
		Database.update( "UPDATE " + Database.formatTableName( "title_variations" ) + " SET suffix = " + Database.formatString( suffix ) + " WHERE id = " + id + ";" );
	}
	
	public static class Affixes
	{
		public String prefix;
		public String suffix;
		
		public Affixes( String prefix, String suffix )
		{
			this.prefix = prefix;
			this.suffix = suffix;
		}
	}
	
	public static class Info
	{
		public String name;
		public Affixes affixes;
		
		public Info( String name, Affixes affixes )
		{
			this.name = name;
			this.affixes = affixes;
		}
		
		public Info( String name, String prefix, String suffix )
		{
			this( name, new Affixes( prefix, suffix ) );
		}
	}

	public static class AlreadyExistsException extends Exception {};
	public static class DoesntExistException extends Exception {};
	public static class InvalidNameException extends Exception {};
	public static class IsVanillaException extends Exception {};
}

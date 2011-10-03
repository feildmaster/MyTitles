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

public class Title {
	
	protected long id;

	protected Title( long id ) {
		
		this.id = id;
	}
	
	public void addVariation( Variation.Info variation ) throws SQLException, Variation.AlreadyExistsException, Variation.InvalidNameException
	{
		try
		{
			if ( !isValidName( variation.name ) ) throw new Variation.InvalidNameException();
			Database.update( "INSERT INTO " + Database.formatTableName( "title_variations" ) + " ( title_id, name, prefix, suffix ) VALUES ( " + id + ", '" + variation.name + "', " + Database.formatString( variation.affixes.prefix ) + ", " + Database.formatString( variation.affixes.suffix ) + ");" );
		}
		catch ( SQLException e )
		{
			if ( e.getErrorCode() == Database.Codes.uniqueDuplicate )
				throw new Variation.AlreadyExistsException();
			throw e;
		}
	}
	
	public boolean equals( Object other )
	{
		if ( !(other instanceof Title) ) return false;
		return this.id == ((Title)other).id;
	}
	
	public Affixes getAffixes() throws SQLException
	{
		ResultSet result = Database.query( "SELECT prefix, suffix FROM " + Database.formatTableName( "titles" ) + " WHERE id = " + id + ";" );
		result.first();
		return new Affixes( result.getString( "prefix" ), result.getString( "suffix" ) );
	}
	
	public Info getInfo() throws SQLException
	{
		ResultSet result = Database.query( "SELECT name, prefix, suffix FROM " + Database.formatTableName( "titles" ) + " WHERE id = " + id + ";" );
		result.first();
		return new Info( result.getString( "name" ), result.getString( "prefix" ), result.getString( "suffix" ) );
	}
	
	public String getName() throws SQLException
	{
		ResultSet result = Database.query( "SELECT name FROM " + Database.formatTableName( "titles" ) + " WHERE id = " + id + ";" );
		result.first();
		return result.getString( "name" );
	}
	
	public String getPrefix() throws SQLException
	{
		ResultSet result = Database.query( "SELECT prefix FROM " + Database.formatTableName( "titles" ) + " WHERE id = " + id + ";" );
		result.first();
		return result.getString( "prefix" );
	}
	
	public String getSuffix() throws SQLException
	{
		ResultSet result = Database.query( "SELECT suffix FROM " + Database.formatTableName( "titles" ) + " WHERE id = " + id + ";" );
		result.first();
		return result.getString( "suffix" );
	}
	
	public Variation getVariation( String variationName ) throws SQLException, Variation.DoesntExistException
	{
		if ( variationName != null )
		{
			ResultSet result = Database.query( "SELECT id FROM " + Database.formatTableName( "title_variations" ) + " WHERE title_id = " + id + " AND name = '" + variationName + "';" );
			if ( result.first() == false ) throw new Variation.DoesntExistException();
			
			return new Variation( this, result.getLong( "id" ) );
		}
		
		return new Variation( this, 0L );
	}
	
	public Variation[] getVariations() throws SQLException
	{
		ResultSet result = Database.query( "SELECT id FROM " + Database.formatTableName( "title_variations" ) + " WHERE title_id = " + id + ";" );
		result.last();
		Variation[] variations = new Variation[result.getRow()];
		result.beforeFirst();
		
		int i = 0;
		while ( result.next() )
		{
			variations[i] = new Variation( this, result.getLong( "id" ) );
			i++;
		}
		
		return variations;
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
	
	public void putVariations( Variation.Info[] variations ) throws SQLException, Exception
	{
		Variation[] existingVariations = getVariations();
		
		for ( int i = 0; i < existingVariations.length; i++ )
		{
			Variation existingVariation = existingVariations[i];
			Variation.Info existingVariationInfo = existingVariation.getInfo();
			Variation.Info variationInfo = null;
			
			boolean variationUsed = false;
			for ( int j = 0; j < variations.length; j++ )
			{
				variationInfo = variations[j];
				
				if ( variationInfo.name.equalsIgnoreCase( existingVariationInfo.name ) )
				{
					variationUsed = true;
					break;
				}
			}
			
			if ( variationUsed == false )
			{
				removeVariation( existingVariation );
				existingVariations[i] = null;;
			}
			else
			{
				if (	( variationInfo.affixes.prefix == null && existingVariationInfo.affixes.prefix != null ) ||
						( variationInfo.affixes.prefix != null && !variationInfo.affixes.prefix.equals( existingVariationInfo.affixes.prefix ) )	)
					existingVariation.setPrefix( variationInfo.affixes.prefix );
				if (	( variationInfo.affixes.suffix == null && existingVariationInfo.affixes.suffix != null ) ||
						( variationInfo.affixes.suffix != null && !variationInfo.affixes.suffix.equals( existingVariationInfo.affixes.suffix ) )	)
					existingVariation.setSuffix( variationInfo.affixes.suffix );
			}
		}
		
		for ( int i = 0; i < variations.length; i++ )
		{
			Variation.Info variationInfo = variations[i];
			
			boolean variationFree = true;
			for ( int j = 0; j < existingVariations.length; j++ )
			{
				if ( existingVariations[j] != null && variationInfo.name.equalsIgnoreCase( existingVariations[j].getName() ) )
				{
					variationFree = false;
					break;
				}
			}
			
			if ( variationFree == true )
				addVariation( variationInfo );
		}
	}
	
	public void removeVariation( String variationName ) throws SQLException, Variation.DoesntExistException
	{
		int affected = Database.update( "DELETE FROM " + Database.formatTableName( "title_variations" ) + " WHERE title_id = " + id + " AND name = '" + variationName + "';" );
		if ( affected == 0 ) throw new Variation.DoesntExistException();
	}
	
	public void removeVariation( Variation variation ) throws SQLException
	{
		Database.update( "DELETE FROM " + Database.formatTableName( "title_variations" ) + " WHERE title_id = " + id + " AND name = '" + variation.getName() + "';" );
	}
	
	public void setAffixes( Affixes affixes ) throws SQLException
	{
		Database.update( "UPDATE " + Database.formatTableName( "titles" ) + " SET prefix = " + Database.formatString( affixes.prefix ) + ", suffix = " + Database.formatString( affixes.suffix ) + " WHERE id = " + id + ";" );
	}
	
	public void setInfo( Info info ) throws SQLException
	{
		Database.update( "UPDATE " + Database.formatTableName( "titles" ) + " SET name = " + Database.formatString( info.name ) + ", prefix = " + Database.formatString( info.affixes.prefix ) + ", suffix = " + Database.formatString( info.affixes.suffix ) + " WHERE id = " + id + ";" );
	}
	
	public void setName( String name ) throws SQLException
	{
		Database.update( "UPDATE " + Database.formatTableName( "titles" ) + " SET name = " + Database.formatString( name ) + " WHERE id = " + id + ";" );
	}
	
	public void setPrefix( String prefix ) throws SQLException
	{
		Database.update( "UPDATE " + Database.formatTableName( "titles" ) + " SET prefix = " + Database.formatString( prefix ) + " WHERE id = " + id + ";" );
	}
	
	public void setSuffix( String suffix ) throws SQLException, Variation.DoesntExistException
	{
		Database.update( "UPDATE " + Database.formatTableName( "titles" ) + " SET suffix = " + Database.formatString( suffix ) + " WHERE id = " + id + ";" );
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
}

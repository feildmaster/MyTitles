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
import java.util.ArrayList;

public class Player {

	private long id;
	private Plugin plugin;
	
	protected Player( Plugin plugin, long id )
	{
		this.id = id;
		this.plugin = plugin;
	}
	
	public String getDisplayName( Title title ) throws SQLException
	{
		Variation var = getTitleVariation( title );
		
		if ( !var.isVanilla() )
		{
			Variation.Affixes affixes = var.getAffixes();
			return getDisplayName( new Title.Affixes( affixes.prefix, affixes.suffix ) );
		}
		
		return getDisplayName( title.getAffixes() );
	}
	
	public String getDisplayName( Title.Affixes fixes ) throws SQLException
	{
		String displayName = getName();
		if ( fixes.prefix != null) displayName = fixes.prefix + displayName;
		if ( fixes.suffix != null ) displayName += fixes.suffix;
		return displayName;
	}
	
	public String getDisplayName() throws SQLException
	{
		ResultSet result = Database.query( "SELECT title_id FROM players WHERE id = " + id + " AND ( SELECT COUNT( * ) FROM collections WHERE player_id = " + id + " AND title_id = players.title_id ) > 0;" );
		result.next();
		long titleId = result.getInt( "title_id" );
		
		return getDisplayName( new Title( titleId ) );
	}
	
	public String getName() throws SQLException
	{
		ResultSet result = Database.query( "SELECT name FROM players WHERE id = " + id + ";" );
		result.next();
		return result.getString( "name" );
	}
	
	public ArrayList<Title> getOwnedTitles() throws SQLException
	{
		ResultSet result = Database.query( "SELECT id FROM titles WHERE id IN ( SELECT title_id FROM collections WHERE player_id = " + id + " );" );
		
		ArrayList<Title> list = new ArrayList<Title>();
		while ( result.next() )
		{
			list.add( new Title( result.getLong( "id" ) ) );
		}
		
		return list;
	}
	
	public Variation getTitleVariation( Title title ) throws SQLException
	{
		ResultSet result = Database.query( "SELECT title_variation_id FROM collections WHERE player_id = " + id + " AND title_id = " + title.id + ";" );
		if ( result.first() == false ) return null;
		return new Variation( title, result.getLong( "title_variation_id" ) );
	}
	
	public void giveTitle( Title title ) throws SQLException, Player.AlreadyOwnsTitleException
	{
		try
		{
			Database.update( "INSERT INTO collections ( player_id, title_id ) VALUES ( " + id + ", " + title.id + " );" );

			org.bukkit.entity.Player onlinePlayer = plugin.getServer().getPlayer( getName() );
			if ( onlinePlayer != null )
				onlinePlayer.sendMessage( "You acquired title \"" + title.getName() + "\", use \"/mytitles\" to view the titles that you own." );
		}
		catch ( SQLException e )
		{
			if ( e.getErrorCode() == 1062 )
				throw new Player.AlreadyOwnsTitleException();
			throw e;
		}
	}
	
	public void resetTitleVariation( Title title ) throws SQLException
	{
		Database.update( "UPDATE collections SET title_variation_id = NULL WHERE player_id = " + id + " AND title_id = " + title.id + ";" );
	}
	
	public void setTitleVariation( Variation variation ) throws SQLException
	{
		Database.update( "UPDATE collections SET title_variation_id = " + variation.id + " WHERE player_id = " + id + " AND title_id = " + variation.title.id + ";" );
	}
	
	public void takeTitle( Title title ) throws SQLException, Player.DoesntOwnTitleException
	{
		String titleName = title.getName();
		
		int affected = Database.update( "DELETE FROM collections WHERE player_id = " + id + " AND title_id = " + title.id + ";" );
		if ( affected == 0 ) throw new DoesntOwnTitleException();
		
		org.bukkit.entity.Player onlinePlayer = plugin.getServer().getPlayer( getName() );
		if ( onlinePlayer != null )
			onlinePlayer.sendMessage( "You lost title \"" + titleName + "\"." );
	}

	public static class DoesntExistException extends Exception {};
	public static class AlreadyOwnsTitleException extends Exception {};
	public static class DoesntOwnTitleException extends Exception {};
}

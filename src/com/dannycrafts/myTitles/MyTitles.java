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

public class MyTitles {
    
	private Plugin plugin;
	private String usagePluginId;
	
	protected MyTitles( Plugin plugin, String usagePluginId )
	{
		this.plugin = plugin;
		this.usagePluginId = usagePluginId;
	}
	
	public Player getPlayer( String playerName ) throws SQLException, Player.DoesntExistException
	{
		ResultSet result = Database.query( "SELECT id FROM players WHERE name = '" + playerName + "';" );
		if ( result.first() == false )
			throw new Player.DoesntExistException();
		
		return new Player( plugin, result.getLong( "id" ) );
	}
	
	public Player getPlayer( org.bukkit.entity.Player player ) throws SQLException, Player.DoesntExistException
	{
		return getPlayer( player.getName() );
	}
	
	public Title getTitle( String titleName ) throws SQLException, Title.DoesntExistException
	{
		ResultSet result = Database.query( "SELECT id FROM titles WHERE name = '" + titleName + "' AND plugin_id = '" + usagePluginId + "';" );
		if ( result.first() == false )
			throw new Title.DoesntExistException();
		
		return new Title( result.getLong( "id" ) );
	}
	
	public ArrayList<Title> getTitles() throws SQLException
	{
		ResultSet result = Database.query( "SELECT id FROM titles WHERE plugin_id = '" + usagePluginId + "';" );
		ArrayList<Title> titles = new ArrayList<Title>();
		while ( result.next() )
		{
			titles.add( new Title( result.getLong( "id" ) ) );
		}
		return titles;
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
	
	public void putTitles( ArrayList<Title.Info> titles ) throws SQLException, Exception
	{
		ArrayList<Title> registeredTitles = getTitles();
		
		for ( int i = 0; i < registeredTitles.size(); i++ )
		{
			Title registeredTitle = registeredTitles.get( i );
			Title.Info registeredTitleInfo = registeredTitle.getInfo();
			Title.Info titleInfo = null;
			
			boolean titleUsed = false;
			for ( int j = 0; j < titles.size(); j++ )
			{
				titleInfo = titles.get( j );
				
				if ( titleInfo.name.equalsIgnoreCase( registeredTitleInfo.name ) )
				{
					titleUsed = true;
					break;
				}
			}
			
			if ( titleUsed == false )
			{
				unregisterTitle( registeredTitle );
				registeredTitles.remove( i );
			}
			else
			{
				if (	( titleInfo.affixes.prefix == null && registeredTitleInfo.affixes.prefix != null ) ||
						( titleInfo.affixes.prefix != null && !titleInfo.affixes.prefix.equals( registeredTitleInfo.affixes.prefix ) )	)
					registeredTitle.setPrefix( titleInfo.affixes.prefix );
				if (	( titleInfo.affixes.suffix == null && registeredTitleInfo.affixes.suffix != null ) ||
						( titleInfo.affixes.suffix != null && !titleInfo.affixes.suffix.equals( registeredTitleInfo.affixes.suffix ) )	)
					registeredTitle.setSuffix( titleInfo.affixes.suffix );
			}
		}
		
		for ( int i = 0; i < titles.size(); i++ )
		{
			Title.Info titleInfo = titles.get( i );
			
			boolean titleFree = true;
			for ( int j = 0; j < registeredTitles.size(); j++ )
			{
				if ( titleInfo.name.equalsIgnoreCase( registeredTitles.get( j ).getName() ) )
				{
					titleFree = false;
					break;
				}
			}
			
			if ( titleFree == true )
				registerTitle( titleInfo );
		}
	}
	
	public void registerTitle( Title.Info titleInfo ) throws Title.AlreadyExistsException, SQLException, Title.InvalidNameException
	{
		try
		{
			if ( !isValidName( titleInfo.name ) ) throw new Title.InvalidNameException();
			Database.update( "INSERT INTO titles ( name, plugin_id, prefix, suffix ) VALUES ( '" + titleInfo.name + "', '" + usagePluginId + "', " + Database.formatString( titleInfo.affixes.prefix ) + ", " + Database.formatString( titleInfo.affixes.suffix ) + " );" );
		}
		catch ( SQLException e )
		{
			if ( e.getErrorCode() == 1062 ) // Duplicate error
				throw new Title.AlreadyExistsException();
			throw e;
		}
	}
	
	public void registerTitle( String name, String prefix, String suffix ) throws Title.AlreadyExistsException, SQLException, Title.InvalidNameException
	{
		registerTitle( new Title.Info( name, prefix, suffix ) );
	}
	
	public void unregisterTitle( String name ) throws SQLException, Title.DoesntExistException {
		
		unregisterTitle( getTitle( name ) );
	}
	
	public void unregisterTitle( Title title ) throws SQLException
	{
		ResultSet result = Database.query( "SELECT player_id FROM collections WHERE title_id = " + title.id + ";" );
		while ( result.next() )
		{
		
			Player player = new Player( plugin, result.getLong( "player_id" ) );
				
			org.bukkit.entity.Player onlinePlayer = plugin.getServer().getPlayer( player.getName() );
			if ( onlinePlayer != null )
				onlinePlayer.sendMessage( "You lost title \"" + title.getName() + "\"." );
		}

		Database.update( "DELETE FROM title_variations WHERE title_id = " + title.id + ";" );
		Database.update( "DELETE FROM collections WHERE title_id = " + title.id + ";" );
		Database.update( "DELETE FROM titles WHERE id = " + title.id + ";" );
	}
}

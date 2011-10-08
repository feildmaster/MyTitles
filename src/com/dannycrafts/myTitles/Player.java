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

import java.io.IOException;

import org.bukkit.ChatColor;

import com.dannycrafts.myTitles.database.*;

public class Player {

	private long id;
	private Plugin plugin;
	
	protected Player( Plugin plugin, long id )
	{
		this.id = id;
		this.plugin = plugin;
	}
	
	public String getDisplayName( Title title ) throws IOException
	{
		Variation var = getTitleVariation( title );
		
		if ( var != null )
		{
			Variation.Affixes affixes = var.getAffixes();
			return getDisplayName( new Title.Affixes( affixes.prefix, affixes.suffix ) );
		}
		
		return getDisplayName( title.getAffixes() );
	}
	
	public String getDisplayName( Title.Affixes fixes ) throws IOException
	{
		String displayName = getName();
		if ( fixes.prefix != null) displayName = fixes.prefix + displayName;
		if ( fixes.suffix != null ) displayName += fixes.suffix;
		return Plugin.joinMessages( Plugin.formatColors( displayName ) );
	}
	
	public String getDisplayName() throws IOException
	{
		Row row = Plugin.playerDatabase.getRow( id );
		long titleId = row.readInt64( 1 );
		if ( titleId < 0 )
			return getDisplayName( Plugin.Config.defaultAffixes );
		
		return getDisplayName( new Title( titleId ) );
	}
	
	public String getName() throws IOException
	{
		Row row = Plugin.playerDatabase.getRow( id );
		return row.readString( 0 );
	}
	
	public Title[] getOwnedTitles() throws IOException
	{
		long[] collectionIds = Plugin.collectionDatabase.findRows( new SearchCriteria( (short)0, id ) );
		
		Title[] list = new Title[collectionIds.length];
		
	    for ( int i = 0; i < list.length; i++ )
	    {
	    	Row row = Plugin.collectionDatabase.getRow( collectionIds[i] );
			list[i] = new Title( row.readInt64( 1 ) );
	    }
		
		return list;
	}
	
	protected void resetTitle() throws IOException
	{
		Plugin.playerDatabase.updateCell( id, (short)1, -1L );
	}
	
	protected void useTitle( Title title ) throws IOException
	{
		Plugin.playerDatabase.updateCell( id, (short)1, title.id );
	}
	
	public Variation getTitleVariation( Title title ) throws IOException
	{
		long collection = Plugin.collectionDatabase.findRow( new SearchCriteria( (short)0, id ), new SearchCriteria( (short)1, title.id ) );
		if ( collection == -1 ) return null;
		
		Row row = Plugin.collectionDatabase.getRow( collection );
		long var = row.readInt64( 2 );
		if ( var == -1 ) return null;
		
		return new Variation( title.id, var );
	}
	
	public boolean giveTitle( Title title ) throws IOException
	{
		long row =  Plugin.collectionDatabase.findRow( new SearchCriteria( (short)0, id ), new SearchCriteria( (short)1, title.id ) );
		if ( row != -1 ) return false;
		Plugin.collectionDatabase.addRow( new Int64Cell( id ), new Int64Cell( title.id ), new Int64Cell( -1 ) );

		org.bukkit.entity.Player onlinePlayer = plugin.getServer().getPlayer( getName() );
		if ( onlinePlayer != null )
			onlinePlayer.sendMessage( "You acquired title \"" + title.getName() + "\", use \"/mytitles\" to view the titles that you own." );
	
		return true;
	}
	
	public boolean resetTitleVariation( Title title ) throws IOException
	{
		long collection = Plugin.collectionDatabase.findRow( new SearchCriteria( (short)0, id ), new SearchCriteria( (short)1, title.id ) );
		if ( collection == -1 ) return false;
		Plugin.collectionDatabase.updateCell( collection, (short)2, -1L );
		return true;
	}
	
	public boolean setTitleVariation( Variation variation ) throws IOException
	{
		long collection = Plugin.collectionDatabase.findRow( new SearchCriteria( (short)0, id ), new SearchCriteria( (short)1, variation.titleId ) );
		if ( collection == -1 ) return false;
		Plugin.collectionDatabase.updateCell( collection, (short)2, variation.id );
		return true;
	}
	
	public boolean takeTitle( Title title ) throws IOException
	{
		String titleName = title.getName();
		
		long collection = Plugin.collectionDatabase.findRow( new SearchCriteria( (short)0, id ), new SearchCriteria( (short)1, title.id ) );
		if ( collection == -1 ) return false;
		
		Plugin.collectionDatabase.removeRow( collection );
		
		org.bukkit.entity.Player onlinePlayer = plugin.getServer().getPlayer( getName() );
		if ( onlinePlayer != null )
			onlinePlayer.sendMessage( "You lost title \"" + titleName + "\"." );
		
		return true;
	}
}

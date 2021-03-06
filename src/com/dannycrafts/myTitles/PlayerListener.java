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
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import com.dannycrafts.myTitles.database.*;

public class PlayerListener extends org.bukkit.event.player.PlayerListener {

	private Plugin plugin;
	
	public PlayerListener( Plugin plugin )
	{
		this.plugin = plugin;
	}
	
	public void onPlayerJoin( PlayerJoinEvent event ) {
		
		try
		{
			Plugin.playerDatabase.addUniqueRow( new SearchCriteria( (short)0, event.getPlayer().getName() ), new StringCell( event.getPlayer().getName() ), new Int64Cell( -1 ) );
			
			event.setJoinMessage( Plugin.formatParameters( Plugin.Template.playerJoin, "player_name", plugin.mainInterface.getPlayer( event.getPlayer() ).getDisplayName() ) );
		}
		catch ( IOException e )
		{
			Plugin.printError( e );
		}
	}
	
	public void onPlayerChat( PlayerChatEvent event )
	{
		try
		{
			Player player = plugin.mainInterface.getPlayer( event.getPlayer() );
			event.setFormat( Plugin.formatParameters( Plugin.formatParameters( Plugin.Template.chat, "player_name", player.getDisplayName() ), "message", event.getMessage() ) );
		}
		catch ( Exception e )
		{
			Plugin.printError( e );
		}
	}
	
	public void onPlayerQuit( PlayerJoinEvent event )
	{
		try
		{
			event.setJoinMessage( Plugin.formatParameters( Plugin.Template.playerQuit, "player_name", plugin.mainInterface.getPlayer( event.getPlayer() ).getDisplayName() ) );
		}
		catch ( Exception e )
		{
			Plugin.printError( e );
		}
	}
}
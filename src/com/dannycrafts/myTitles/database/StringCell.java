package com.dannycrafts.myTitles.database;

import java.io.UnsupportedEncodingException;

public class StringCell extends Cell
{
	private String data;
	
	public StringCell()
	{
		data = "";
	}

	public StringCell( String data )
	{
		this.data = data;
	}
	
	public String getString()
	{
		return this.data;
	}
	
	public byte[] read()
	{
		byte[] stringBuffer = new byte[0];
		try {
			stringBuffer = data.getBytes( "US-ASCII" );
		} catch (UnsupportedEncodingException e) {}
		
		return stringBuffer;
	}
	
	public void write( byte[] data )
	{		
		try { this.data = new String( data, "US-ASCII" ); }
		catch (UnsupportedEncodingException e) {}
		
		for ( int i = 0; i < this.data.length(); i++ )
			if ( this.data.charAt( i ) == '\0' )
				this.data = this.data.substring( 0, i );
	}
}

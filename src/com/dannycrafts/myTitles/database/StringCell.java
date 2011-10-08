package com.dannycrafts.myTitles.database;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class StringCell implements Cell
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
	
	public StringCell( StringCell other )
	{
		this.data = other.data;
	}
	
	public String getString()
	{
		return this.data;
	}

	public boolean matches( byte[] data )
	{
		int dataLength = -1;
		for ( int i = 0; i < data.length; i++ )
			if ( data[i] == 0 )
			{
				dataLength = i;
				break;
			}
		if ( dataLength < 0 )
			dataLength = data.length;
		
		if ( dataLength != this.data.length() )
			return false;
		
		for ( int i = 0; i < dataLength; i++ )
			if ( Character.toLowerCase( this.data.charAt( i ) ) != Character.toLowerCase( (char)data[i] ) )
				return false;
		
		return true;
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
		short dataLength = (short)data.length;
		for ( short i = 0; i < data.length; i++ )
			if ( data[i] == 0 )
			{
				dataLength = i;
				break;
			}
		
		char[] chars = new char[dataLength];
		for ( int i = 0; i < dataLength; i++ )
			chars[i] = (char)data[i];
		
		this.data = new String( chars );
	}
}

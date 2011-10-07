package com.dannycrafts.myTitles.database;

public interface Cell
{
	
	public boolean matches( byte[] data );
	
	public byte[] read();
	
	public void write( byte[] data );
}

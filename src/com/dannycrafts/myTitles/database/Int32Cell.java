package com.dannycrafts.myTitles.database;

public class Int32Cell implements Cell
{
	int data;
	
	public Int32Cell()
	{
		this.data = 0;
	}
	
	public Int32Cell( int data )
	{
		this.data = data;
	}
	
	public Int32Cell( Int32Cell other )
	{
		this.data = other.data;
	}
	
	public int getInt()
	{
		return data;
	}
	
	public boolean matches( byte[] data )
	{
		return fromBytes( data ) == this.data;
	}
	
	public byte[] read()
	{
		return new byte[] {
				(byte)data,
				(byte)(data >> 8 ),
				(byte)(data >> 16 ),
				(byte)(data >> 24 )
		};
	}
	
	public void write( byte[] data )
	{
		this.data = fromBytes( data );
	}
	
	private int fromBytes( byte[] data )
	{
		return	(data[0] & 0xFF) |
				((data[1] & 0xFF) << 8) |
				((data[2] & 0xFF) << 16) |
				((data[3] & 0xFF) << 24);
	}
}

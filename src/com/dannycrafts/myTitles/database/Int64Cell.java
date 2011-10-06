package com.dannycrafts.myTitles.database;

public class Int64Cell extends Cell
{
	long data;
	
	public Int64Cell()
	{
		this.data = 0;
	}
	
	public Int64Cell( long data )
	{
		this.data = data;
	}
	
	public long getInt()
	{
		return data;
	}
	
	public byte[] read()
	{
		return new byte[] {
				(byte)data,
				(byte)(data >> 8 ),
				(byte)(data >> 16 ),
				(byte)(data >> 24 ),
				(byte)(data >> 32 ),
				(byte)(data >> 40 ),
				(byte)(data >> 48 ),
				(byte)(data >> 56 )
		};
	}
	
	public void write( byte[] data )
	{
		this.data = (data[0] & 0xFF) |
					((data[1] & 0xFF) << 8) |
					((data[2] & 0xFF) << 16) |
					((data[3] & 0xFF) << 24) |
					((data[4] & 0xFF) << 32) |
					((data[5] & 0xFF) << 40) |
					((data[6] & 0xFF) << 48) |
					((data[7] & 0xFF) << 56);
	}
}

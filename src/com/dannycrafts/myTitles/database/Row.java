package com.dannycrafts.myTitles.database;

import java.io.UnsupportedEncodingException;

public class Row
{
	private long index;
	private byte[] buffer;
	private Header header;
	
	protected Row( long index, Header header, byte[] buffer )
	{
		this.index = index;
		this.header = header;
		this.buffer = buffer;
	}
	
	public void readCell( int index, Cell cell )
	{
		byte[] cellBuffer = new byte[header.getCellLength( (short)index )];
		System.arraycopy( buffer, header.getCellOffset( (short)index ), cellBuffer, 0, header.getCellLength( (short)index ) );
		cell.write( cellBuffer );
	}
	
	public long readInt32( int index )
	{
		Int32Cell cell = new Int32Cell();
		readCell( index, cell );
		return cell.getInt();
	}
	
	public long readInt64( int index )
	{
		Int64Cell cell = new Int64Cell();
		readCell( index, cell );
		return cell.getInt();
	}
	
	public String readString( int index )
	{
		StringCell cell = new StringCell();
		readCell( index, cell );
		return cell.getString();
	}
}

package com.dannycrafts.myTitles.database;

import java.util.ArrayList;

public class Header
{
	protected ArrayList<Short> cellLengths;
	private ArrayList<Short> cellOffsets;
	private short headerLength;
	
	public Header()
	{
		this.cellLengths = new ArrayList<Short>();
		this.cellOffsets = new ArrayList<Short>();
		this.headerLength = 0;
	}
	
	public Header( short[] cellLengths )
	{
		this.cellLengths = new ArrayList<Short>( cellLengths.length );
		for ( short cellLength : cellLengths )
		{
			this.cellLengths.add( cellLength );
		}
	}
	
	private void addLength( short length )
	{
		int newIndex = cellLengths.size();
		
		if ( cellOffsets.size() != 0 )
			cellOffsets.add( (short)(cellOffsets.get( newIndex - 1 ) + cellLengths.get( newIndex - 1 )) );
		else
			cellOffsets.add( (short)0 );
		
		cellLengths.add( length );
		headerLength += length;
	}
	
	public void addString( short length )
	{
		addLength( length );
	}

	public void addInt32()
	{
		addLength( (short)4 );
	}
	
	public void addInt64()
	{
		addLength( (short)8 );
	}
	
	protected int getCellOffset( int index )
	{
		return cellOffsets.get( index );
	}
	
	protected short getCellLength( int index )
	{
		return cellLengths.get( index );
	}
	
	protected short getHeaderLength()
	{
		return headerLength;
	}
}

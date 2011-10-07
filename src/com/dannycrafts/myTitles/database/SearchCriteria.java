package com.dannycrafts.myTitles.database;

public class SearchCriteria
{
	protected short cellIndex;
	protected Cell cellData;
	
	public SearchCriteria( short cellIndex, Cell cellData )
	{
		this.cellIndex = cellIndex;
		this.cellData = cellData;
	}
	
	public SearchCriteria( short cellIndex, String cellData )
	{
		this.cellIndex = cellIndex;
		this.cellData = new StringCell( cellData );
	}
	
	public SearchCriteria( short cellIndex, int cellData )
	{
		this.cellIndex = cellIndex;
		this.cellData = new Int32Cell( cellData );
	}
	
	public SearchCriteria( short cellIndex, long cellData )
	{
		this.cellIndex = cellIndex;
		this.cellData = new Int64Cell( cellData );
	}
}

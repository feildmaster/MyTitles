package com.dannycrafts.myTitles.database;

public class Row
{
	private long id;
	protected Cell[] cells;
	
	public Row( Cell... cell )
	{
		cells = cell;
	}
}

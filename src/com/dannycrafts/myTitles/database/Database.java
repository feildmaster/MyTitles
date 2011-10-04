package com.dannycrafts.myTitles.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Database
{
	private File databaseFile;
	private short version;
	
	public Database( File databaseFile, short version ) throws IOException, Database.DifferentVersionException
	{
		this.databaseFile = databaseFile;
		
		if ( !databaseFile.exists() )
			createFile( version );
		else
		{
			FileInputStream fis = new FileInputStream( databaseFile );
			byte[] buffer = new byte[2];
			fis.read( buffer );
			fis.close();
			short actualVersion = fromBytesInt16( buffer );
			
			if ( version != actualVersion )
				throw new DifferentVersionException( actualVersion );
		}
		
		this.version = version;
	}
	
	public void addRow( Row row ) throws IOException
	{
		FileOutputStream fos = new FileOutputStream( databaseFile );
		for ( Cell cell : row.cells )
			fos.write( cell.toBytes() );
		fos.flush();
		fos.close();
	}
	
	private void createFile( short version ) throws IOException
	{
		databaseFile.createNewFile();
		FileOutputStream fos = new FileOutputStream( databaseFile, false );
		fos.write( toBytes( version ) );
		fos.flush();
		fos.close();
	}
	
	private static int fromBytesInt32( byte[] bytes ) {
		
		return	(int)(bytes[0] & 0xFF) |
				(int)((bytes[1] & 0xFF) << 8) |
				(int)((bytes[2] & 0xFF) << 16) |
				(int)((bytes[3] & 0xFF) << 24);
	}

	private static short fromBytesInt16( byte[] bytes )
	{
		return (short)(
				(bytes[0] & 0xFF) |
				((bytes[1] & 0xFF) << 8)
		);
	}
	
	private static byte[] toBytes( int integer ) {
		
		return new byte[] {
				(byte)(integer),
				(byte)(integer >> 8 ),
				(byte)(integer >> 16 ),
				(byte)(integer >> 24 )
		};
	}
	
	private static byte[] toBytes( short integer ) {
		
		return new byte[] {
				(byte)(integer),
				(byte)(integer >> 8 )
		};
	}
	
	public static class DifferentVersionException extends Exception
	{
		public short actualVersion;
		
		public DifferentVersionException( short actualVersion )
		{
			this.actualVersion = actualVersion;
		}
	}
	
}

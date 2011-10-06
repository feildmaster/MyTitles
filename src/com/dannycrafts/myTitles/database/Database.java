package com.dannycrafts.myTitles.database;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.SyncFailedException;
import java.util.ArrayList;

public class Database
{
	private File databaseFile;
	private short version;
	private Header header;
	private ArrayList<Long> sockets;
	private long dataOffset;
	private RandomAccessFile stream;
	
	public Database( File databaseFile, short version, Header header ) throws IOException, Database.DifferentVersionException
	{
		this.databaseFile = databaseFile;
		this.version = version;
		this.sockets = new ArrayList<Long>();
		this.header = header;
		
		if ( !databaseFile.exists() )
			createFile();
		else
			openFile();
	}
	
	public long addRow( Cell... cells ) throws IOException
	{
		long socketIndex = findSocket();
		seekRow( socketIndex );
		
		stream.writeByte( 1 ); // Flag indicating the row exists, if set to 0 it is flagged as empty.
		int i = 0;
		for ( Cell cell : cells )
		{
			byte[] cellBufer = cell.read();
			byte[] buffer = new byte[header.getCellLength( i )];
			System.arraycopy( cellBufer, 0, buffer, 0, cellBufer.length );
			stream.write( buffer );
			i++;
		}
		
		return socketIndex;
	}
	
	public void close() throws IOException
	{
		stream.getFD().sync();
		stream.close();
	}
	
	private void createFile() throws IOException
	{
		databaseFile.createNewFile();
		open();
		
		stream.writeShort( version );
		
		close();
		
		this.dataOffset = 2;
	}
	
	public long findSocket() throws IOException
	{
		if ( sockets.size() != 0 )
		{
			long socketIndex = sockets.get( 0 );
			sockets.remove( 0 );
			return socketIndex;
		}
		
		stream.seek( dataOffset );
		long index = 0;
		try
		{
			while ( stream.readBoolean() == true )
			{
				short headerLength = header.getHeaderLength();
				int skipped = stream.skipBytes( headerLength );
				if ( skipped < headerLength )
					return index;
				index++;
			}
		}
		catch ( EOFException e )
		{
			return index;
		}
		
		return -1;
	}
	
	public void flush() throws IOException
	{
		stream.getFD().sync();
	}
	
	public Row getRow( long index ) throws IOException, Database.RowDoesntExistException
	{
		boolean exists = false;
		try
		{
			seekRow( index );
			exists = stream.readBoolean();
		}
		catch ( EOFException e ) { throw new Database.RowDoesntExistException(); }
		if ( !exists ) throw new Database.RowDoesntExistException();
		
		byte[] buffer = new byte[header.getHeaderLength()];
		stream.read( buffer );
		
		return new Row( index, this.header, buffer );
	}
	
	public void open() throws IOException
	{
		stream = new RandomAccessFile( databaseFile, "rw" );
	}
	
	private void openFile() throws IOException, Database.DifferentVersionException
	{
		open();
		
		short dbVersion = stream.readShort();
		if ( dbVersion != this.version )
		{
			close();
			throw new Database.DifferentVersionException( dbVersion );
		}
		
		close();
		
		this.dataOffset = 2;
	}
	
	private void seekRow( long index ) throws IOException
	{
		stream.seek( dataOffset + index * ( header.getHeaderLength() + 1 ) );
	}
	
	public void removeRow( long index ) throws IOException
	{
		seekRow( index );
		stream.writeByte( 0 );
	}
	

	public static class RowDoesntExistException extends Exception {}

	public static class DifferentVersionException extends Exception
	{
		public short actualVersion;
		
		public DifferentVersionException( short actualVersion )
		{
			this.actualVersion = actualVersion;
		}
	}
	
}

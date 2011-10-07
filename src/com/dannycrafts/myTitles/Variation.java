// Copyright (C) 2011 by Danny de Jong
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
// 
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
// 
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.

package com.dannycrafts.myTitles;
import java.io.IOException;

import com.dannycrafts.myTitles.database.*;

public class Variation {
	
	protected long id;
	protected long titleId;
	
	protected Variation( long titleId, long id )
	{
		this.titleId = titleId;
		this.id = id;
	}
	
	public Affixes getAffixes() throws IOException
	{
		Row row = Plugin.titleVariationDatabase.getRow( id );
		
		return new Affixes( row.readString( 2 ), row.readString( 3 ) );
	}
	
	public Info getInfo() throws IOException
	{
		Row row = Plugin.titleVariationDatabase.getRow( id );
		
		return new Info( row.readString(1), row.readString( 2 ), row.readString( 3 ) );
	}
	
	public String getName() throws IOException
	{
		Row row = Plugin.titleVariationDatabase.getRow( id );
		
		return row.readString( 1 );
	}
	
	public String getPrefix() throws IOException
	{
		Row row = Plugin.titleVariationDatabase.getRow( id );
		
		return row.readString( 2 );
	}
	
	public String getSuffix() throws IOException
	{
		Row row = Plugin.titleVariationDatabase.getRow( id );
		
		return row.readString( 3 );
	}
	
	private boolean isValidName( String name )
	{
		for ( int i = 0; i < name.length(); i++ )
		{
			int codePoint = name.codePointAt(i);
			
			if (	codePoint != 45 &&
					codePoint < 48 && codePoint > 57 &&
					codePoint < 65 && codePoint > 90 &&
					codePoint != 95 &&
					codePoint < 97 && codePoint > 122 )
				return false;
		}
		
		return true;
	}
	
	public void setInfo( Info info ) throws IOException
	{
		Plugin.titleDatabase.updateCell( id, (short)1, info.name );
		Plugin.titleDatabase.updateCell( id, (short)2, info.affixes.prefix );
		Plugin.titleDatabase.updateCell( id, (short)3, info.affixes.suffix );
	}
	
	public void setPrefix( Affixes affixes ) throws IOException
	{
		Plugin.titleDatabase.updateCell( id, (short)2, affixes.prefix );
		Plugin.titleDatabase.updateCell( id, (short)3, affixes.suffix );
	}
	
	public void setPrefix( String prefix ) throws IOException
	{
		Plugin.titleDatabase.updateCell( id, (short)2, prefix );
	}
	
	public void setName( String name ) throws IOException, Variation.InvalidNameException
	{
		if ( !isValidName( name ) ) throw new Variation.InvalidNameException();

		Plugin.titleDatabase.updateCell( id, (short)1, name );	
	}
	
	public void setSuffix( String suffix ) throws IOException
	{
		Plugin.titleDatabase.updateCell( id, (short)3, suffix );
	}
	
	public static class Affixes
	{
		public String prefix;
		public String suffix;
		
		public Affixes( String prefix, String suffix )
		{
			this.prefix = prefix;
			this.suffix = suffix;
		}
	}
	
	public static class Info
	{
		public String name;
		public Affixes affixes;
		
		public Info( String name, Affixes affixes )
		{
			this.name = name;
			this.affixes = affixes;
		}
		
		public Info( String name, String prefix, String suffix )
		{
			this( name, new Affixes( prefix, suffix ) );
		}
	}

	public static class InvalidNameException extends Exception {};
}

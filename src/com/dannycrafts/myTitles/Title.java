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

public class Title {
	
	protected long id;

	protected Title( long id ) {
		
		this.id = id;
	}
	
	public boolean addVariation( Variation.Info variation ) throws IOException, Variation.InvalidNameException
	{
		if ( !isValidName( variation.name ) ) throw new Variation.InvalidNameException();
		
		return Plugin.titleVariationDatabase.addRow(
					new Int64Cell( id ), new StringCell( variation.name ),
					new StringCell( variation.affixes.prefix ),
					new StringCell( variation.affixes.suffix )
				) != -1;
	}
	
	public boolean equals( Object other )
	{
		if ( !(other instanceof Title) ) return false;
		return this.id == ((Title)other).id;
	}
	
	public Affixes getAffixes() throws IOException
	{
		Row row = Plugin.titleDatabase.getRow( id );
		
		return new Affixes( row.readString( 2 ), row.readString( 3 ) );
	}
	
	public Info getInfo() throws IOException
	{
		Row row = Plugin.titleDatabase.getRow( id );
		
		return new Info( row.readString( 1 ), row.readString( 2 ), row.readString( 3 ) );
	}
	
	public String getName() throws IOException
	{
		Row row = Plugin.titleDatabase.getRow( id );
		
		return row.readString( 1 );
	}
	
	public String getPrefix() throws IOException
	{
		Row row = Plugin.titleDatabase.getRow( id );
		
		return row.readString( 2 );
	}
	
	public String getSuffix() throws IOException
	{
		Row row = Plugin.titleDatabase.getRow( id );
		
		return row.readString( 3 );
	}
	
	public Variation getVariation( String variationName ) throws IOException
	{
		long variation = Plugin.titleVariationDatabase.findRow( new SearchCriteria( (short)0, id ), new SearchCriteria( (short)1, variationName ) );
		if ( variation == -1 ) return null;
		
		return new Variation( id, variation );
	}
	
	public Variation[] getVariations() throws IOException
	{
		long[] variationIds = Plugin.titleVariationDatabase.findRows( (short)0, id );
		Variation[] variations = new Variation[variationIds.length];
		
		for ( int i = 0; i < variationIds.length; i++ )
			variations[i] = new Variation( id, variationIds[i] );
		
		return variations;
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
	
	public void putVariations( Variation.Info[] variations ) throws IOException, Variation.InvalidNameException
	{
		Variation[] existingVariations = getVariations();
		
		for ( int i = 0; i < existingVariations.length; i++ )
		{
			Variation existingVariation = existingVariations[i];
			Variation.Info existingVariationInfo = existingVariation.getInfo();
			Variation.Info variationInfo = null;
			
			boolean variationUsed = false;
			for ( int j = 0; j < variations.length; j++ )
			{
				variationInfo = variations[j];
				
				if ( variationInfo.name.equalsIgnoreCase( existingVariationInfo.name ) )
				{
					variationUsed = true;
					break;
				}
			}
			
			if ( variationUsed == false )
			{
				removeVariation( existingVariation );
				existingVariations[i] = null;
			}
			else
			{
				if (	( variationInfo.affixes.prefix == null && existingVariationInfo.affixes.prefix != null ) ||
						( variationInfo.affixes.prefix != null && !variationInfo.affixes.prefix.equals( existingVariationInfo.affixes.prefix ) )	)
					existingVariation.setPrefix( variationInfo.affixes.prefix );
				if (	( variationInfo.affixes.suffix == null && existingVariationInfo.affixes.suffix != null ) ||
						( variationInfo.affixes.suffix != null && !variationInfo.affixes.suffix.equals( existingVariationInfo.affixes.suffix ) )	)
					existingVariation.setSuffix( variationInfo.affixes.suffix );
			}
		}
		
		for ( int i = 0; i < variations.length; i++ )
		{
			Variation.Info variationInfo = variations[i];
			
			boolean variationFree = true;
			for ( int j = 0; j < existingVariations.length; j++ )
			{
				if ( existingVariations[j] != null && variationInfo.name.equalsIgnoreCase( existingVariations[j].getName() ) )
				{
					variationFree = false;
					break;
				}
			}
			
			if ( variationFree == true )
				addVariation( variationInfo );
		}
	}
	
	public boolean removeVariation( String variationName ) throws IOException
	{
		long variation = Plugin.titleVariationDatabase.findRow( new SearchCriteria( (short)0, id ), new SearchCriteria( (short)1, variationName ) );
		if ( variation == -1 ) return false;
		
		return Plugin.titleVariationDatabase.removeRow( variation );
	}
	
	public boolean removeVariation( Variation variation ) throws IOException
	{
		return Plugin.titleVariationDatabase.removeRow( variation.id );
	}
	
	public void setAffixes( Affixes affixes ) throws IOException
	{
		Plugin.titleDatabase.updateCell( id, (short)2, affixes.prefix );
		Plugin.titleDatabase.updateCell( id, (short)3, affixes.suffix );
	}
	
	public void setInfo( Info info ) throws IOException
	{
		Plugin.titleDatabase.updateCell( id, (short)1, info.name );
		Plugin.titleDatabase.updateCell( id, (short)2, info.affixes.prefix );
		Plugin.titleDatabase.updateCell( id, (short)3, info.affixes.suffix );
	}
	
	public void setName( String name ) throws IOException
	{
		Plugin.titleDatabase.updateCell( id, (short)1, name );
	}
	
	public void setPrefix( String prefix ) throws IOException
	{
		Plugin.titleDatabase.updateCell( id, (short)2, prefix );
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

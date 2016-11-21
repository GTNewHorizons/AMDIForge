/*
    Copyright 2016 Stefan 'Namikon' Thomanek <sthomanek at gmail dot com>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.usrv.amdiforge.core.graveIO;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.Constants;
import eu.usrv.amdiforge.AMDIForge;
import eu.usrv.yamcore.auxiliary.LogHelper;


public class GraveNBT
{
	public String getPlayerUUID()
	{
		return _mGraveNBT.getString( "PlayerUUID" );
	}

	public String getPlayerName()
	{
		return _mGraveNBT.getString( "PlayerName" );
	}

	public Vec3 getGraveLocation()
	{
		NBTTagCompound graveLocation = _mGraveNBT.getCompoundTag( "GraveLocation" );
		return Vec3.createVectorHelper( graveLocation.getInteger( "X" ), graveLocation.getInteger( "Y" ), graveLocation.getInteger( "Z" ) );
	}

	public Vec3 getPlayerLocation()
	{
		NBTTagCompound playerLocation = _mGraveNBT.getCompoundTag( "PlayerLocation" );
		return Vec3.createVectorHelper( playerLocation.getInteger( "X" ), playerLocation.getInteger( "Y" ), playerLocation.getInteger( "Z" ) );
	}

	public Date getCreated()
	{
		return new Date( _mGraveNBT.getLong( "Created" ) );
	}

	public int getPlacedFlag()
	{
		return _mGraveNBT.getInteger( "Placed" );
	}

	private File _mGraveFile = null;
	NBTTagCompound _mGraveNBT = null;
	IInventory _mMainInventory = null;

	private static LogHelper _mLogger = AMDIForge.Logger;

	public static GraveNBT getGrave( String pFullPath )
	{
		File f = new File( pFullPath );
		if( !f.exists() )
			throw new IllegalArgumentException( pFullPath );
		else
			return new GraveNBT( pFullPath );
	}

	private GraveNBT( String pFullPath )
	{
		_mGraveFile = new File( pFullPath );
		_mGraveNBT = loadGraveFile( _mGraveFile );
		if( _mGraveNBT == null )
			throw new IllegalArgumentException( pFullPath );
		else
			_mMainInventory = loadInventory( _mGraveNBT );
	}

	private static IInventory loadInventory( NBTTagCompound rootTag )
	{
		if( !rootTag.hasKey( "Inventory", Constants.NBT.TAG_COMPOUND ) )
			return null;

		NBTTagCompound tInvTag = rootTag.getCompoundTag( "Inventory" );
		GenericInventory tResult = new GenericInventory( "tmp", false, 0 );
		tResult.readFromNBT( tInvTag );
		return tResult;
	}

	private static NBTTagCompound loadGraveFile( File pGraveFile )
	{
		try
		{
			InputStream tStream = new FileInputStream( pGraveFile );
			try
			{
				return CompressedStreamTools.readCompressed( tStream );
			}
			finally
			{
				tStream.close();
			}
		}
		catch( IOException e )
		{
			_mLogger.warn( "Failed to read data from file %s", pGraveFile.getAbsoluteFile() );
			return null;
		}
	}
}

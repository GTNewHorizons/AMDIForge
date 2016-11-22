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

package eu.usrv.amdiforge.core;


import java.io.File;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import eu.usrv.amdiforge.AMDIForge;
import eu.usrv.amdiforge.core.graveIO.GraveNBT;
import eu.usrv.yamcore.auxiliary.LogHelper;


public class GraveFileHandler
{
	private LogHelper _mLogger = AMDIForge.Logger;
	private String _mConfigFileName;

	private boolean _mInitialized = false;

	public GraveFileHandler( )
	{
	}

	/**
	 * Creates a fake Array of ItemStacks for given LootGroupID
	 * This should only execute on the SERVER thread
	 * 
	 * @param pLootGroupID
	 * @return
	 */
	public ItemStack[] createFakeInventoryFromGrave( String pGraveFile )
	{
		// dumpDebugInfo("createFakeInventoryFromID");
		ItemStack[] tList = new ItemStack[108];
		try
		{
			GraveNBT tGrave = GraveNBT.getGrave( pGraveFile );
			ItemStack[] tmpList = tGrave.getGraveInventory();
			for (int i = 0; i < tmpList.length; i++)
			{
				if (tmpList[i] != null)
					tList[i] = tmpList[i].copy();
			}
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		// _mLogger.info(String.format("fakeInventory contains %d items", i));
		return tList;
	}
}

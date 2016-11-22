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

package eu.usrv.amdiforge;


import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import cpw.mods.fml.common.network.IGuiHandler;
import eu.usrv.amdiforge.client.gui.GuiAMDI;
import eu.usrv.amdiforge.core.ContainerGraveInventory;


public class GuiHandler implements IGuiHandler
{
	public static int GUI_GRAVEVIEW = 1;

	@Override
	public Object getServerGuiElement( int pGuiID, EntityPlayer pPlayer, World pWorld, int pX, int pY, int pZ )
	{
		if( pGuiID == GUI_GRAVEVIEW )
		{
			return new ContainerGraveInventory( pPlayer.inventory, "/home/namikon/Git/AMDIForge/testgrave.dat" );
		}

		return null;
	}

	@Override
	public Object getClientGuiElement( int pGuiID, EntityPlayer pPlayer, World pWorld, int pX, int pY, int pZ )
	{
		if( pGuiID == GUI_GRAVEVIEW )
		{
			return new GuiAMDI( pPlayer.inventory, "" ); // Not required; Inventory Content is serverside
		}

		return null;
	}
}

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

package eu.usrv.amdiforge.client.gui;


import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import eu.usrv.amdiforge.AMDIForge;
import eu.usrv.amdiforge.core.ContainerGraveInventory;


public class GuiAMDI extends GuiContainer
{
	private float mMouseX;
	private float mMouseY;
	private float mGuiMouseX;
	private float mGuiMouseY;
	public static int GUI_RowCount = 9;
	public static int GUI_ColCount = 12;
	public static int GUI_SizeX = 237;
	public static int GUI_SizeY = 255;

	public GuiAMDI( InventoryPlayer pInventoryPlayer, String pGraveFile )
	{
		super( new ContainerGraveInventory( pInventoryPlayer, pGraveFile ) );
		xSize = GUI_SizeX;
		ySize = GUI_SizeY;

		Minecraft mc = Minecraft.getMinecraft();
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer( float pPar1, int pPar2, int pPar3 )
	{
		GL11.glColor4f( 1.0F, 1.0F, 1.0F, 1.0F );
		mc.renderEngine.bindTexture( new ResourceLocation( AMDIForge.MODID, "textures/gui/gravefakeintentory.png" ) );
		int mGuiX = ( width - xSize ) / 2;
		int mGuiY = ( height - ySize ) / 2;
		this.drawTexturedModalRect( mGuiX, mGuiY, 0, 0, xSize, ySize );
	}
}

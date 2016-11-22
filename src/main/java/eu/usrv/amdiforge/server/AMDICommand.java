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

package eu.usrv.amdiforge.server;


import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import eu.usrv.amdiforge.AMDIForge;
import eu.usrv.amdiforge.client.gui.GuiAMDI;
import eu.usrv.yamcore.auxiliary.PlayerChatHelper;


public class AMDICommand implements ICommand
{
	private List aliases;

	public AMDICommand()
	{
		this.aliases = new ArrayList();
		this.aliases.add( "amdi" );
	}

	@Override
	public int compareTo( Object arg0 )
	{
		return 0;
	}

	@Override
	public String getCommandName()
	{
		return "amdiforge";
	}

	@Override
	public String getCommandUsage( ICommandSender p_71518_1_ )
	{
		return "Check the readme for usage";
	}

	@Override
	public List getCommandAliases()
	{
		return this.aliases;
	}

	@Override
	public void processCommand( ICommandSender pCmdSender, String[] pArgs )
	{
		// Minecraft.getMinecraft().displayGuiScreen( new GuiAMDI( ((EntityPlayer)pCmdSender).inventory, pArgs[0] ) );
		Minecraft.getMinecraft().displayGuiScreen( new GuiAMDI( ( (EntityPlayer) pCmdSender ).inventory, "/home/namikon/Git/AMDIForge/testgrave.dat" ) );
	}

	private boolean InGame( ICommandSender pCmdSender )
	{
		if( !( pCmdSender instanceof EntityPlayer ) )
			return false;
		else
			return true;
	}

	private void SendHelpToPlayer( ICommandSender pCmdSender )
	{
		if( !InGame( pCmdSender ) )
		{
			PlayerChatHelper.SendPlain( pCmdSender, "Command can only be executed ingame" );
		}
		else
		{
			PlayerChatHelper.SendInfo( pCmdSender, "Check the readme for usage" );
		}
	}

	@Override
	public boolean canCommandSenderUseCommand( ICommandSender pCommandSender )
	{
		if( pCommandSender instanceof EntityPlayerMP )
		{
			EntityPlayerMP tEP = (EntityPlayerMP) pCommandSender;
			boolean tPlayerOpped = MinecraftServer.getServer().getConfigurationManager().func_152596_g( tEP.getGameProfile() );
			boolean tIncreative = tEP.capabilities.isCreativeMode;
			return tPlayerOpped && tIncreative;
		}
		else if( pCommandSender instanceof MinecraftServer )
			return true;
		else
			return false;
	}

	// amdi gi - GraveInspect - Opens Contents in a GUI
	// amdi gtp - GraveTeleport - TPs to GraveLocation
	// amdi gp - GravePurge - Remove GraveFile from disk
	@Override
	public List addTabCompletionOptions( ICommandSender sender, String[] args )
	{
		if( args.length < 2 )
			return null;

		if( args.length == 2 )
		{
			String prefix = args[1];
			return AMDIForge.GraveHdl.getMatchedDumps( sender.getEntityWorld(), prefix );
		}
		return null;
	}

	@Override
	public boolean isUsernameIndex( String[] p_82358_1_, int p_82358_2_ )
	{
		return false;
	}
}

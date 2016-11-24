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


import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

import org.apache.commons.lang3.StringUtils;

import cpw.mods.fml.common.registry.GameRegistry;

import eu.usrv.amdiforge.AMDIForge;
import eu.usrv.amdiforge.client.gui.GuiAMDI;
import eu.usrv.amdiforge.core.graveIO.GraveNBT;
import eu.usrv.yamcore.auxiliary.PlayerChatHelper;
import eu.usrv.yamcore.auxiliary.TabText;
import eu.usrv.yamcore.auxiliary.classes.JSONChatText;
import eu.usrv.yamcore.auxiliary.classes.JSONHoverEvent;


public class AMDICommand implements ICommand
{
	private static final String PREFIX = "inventory-";
	private JSONChatText jsonHelpG = null;
	private JSONChatText jsonHelpGI = null;
	private JSONChatText jsonHelpGTP = null;
	private JSONChatText jsonHelpGP = null;

	private List aliases;

	public AMDICommand()
	{
		this.aliases = new ArrayList();
		this.aliases.add( "amdi" );
		populateHelp();
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
	public String getCommandUsage( ICommandSender pCommandSender )
	{
		return "Type /amdiforge help for more help";
	}

	@Override
	public List getCommandAliases()
	{
		return this.aliases;
	}

	private static String stripFilename( String name )
	{
		return StringUtils.removeEndIgnoreCase( StringUtils.removeStartIgnoreCase( name, PREFIX ), ".dat" );
	}

	@Override
	public void processCommand( ICommandSender pCmdSender, String[] pArgs )
	{
		if( !InGame( pCmdSender ) )
		{
			PlayerChatHelper.SendPlain( pCmdSender, "Must be online to use this command" );
			return;
		}

		EntityPlayer tEP = (EntityPlayer) pCmdSender;

		if( pArgs.length != 2 )
			SendHelpToPlayer( tEP );
		else
		{
			String tSubCommand = pArgs[0];
			String tGraveArg = pArgs[1];
			File tFullGravePath = tEP.getEntityWorld().getSaveHandler().getMapFileFromName( PREFIX + stripFilename( tGraveArg ) );

			if( tSubCommand.equalsIgnoreCase( "g" ) )
			{
				displayGraveInfo( tEP, tGraveArg, tFullGravePath.getAbsolutePath() );
			}
			else if( tSubCommand.equalsIgnoreCase( "gi" ) )
			{
				Minecraft.getMinecraft().displayGuiScreen( new GuiAMDI( tEP.inventory, tFullGravePath.getAbsolutePath() ) );
			}
			else if( tSubCommand.equalsIgnoreCase( "gtp" ) )
			{
			}
			else if( tSubCommand.equalsIgnoreCase( "gp" ) )
			{
			}
			else
			{
				SendHelpToPlayer( pCmdSender );
			}
		}
		//
		// Minecraft.getMinecraft().displayGuiScreen( new GuiAMDI( ( (EntityPlayer) pCmdSender ).inventory,
		// "/home/namikon/Git/AMDIForge/testgrave.dat" ) );
	}

	private void displayGraveInfo( EntityPlayer pCmdSender, String pGraveFile, String pFullGravePath )
	{
		GraveNBT tGrave = GraveNBT.getGrave( pFullGravePath );

		String tMultilineString = "----------`GraveInfo-\n";
		tMultilineString += "FileName`%s\n";
		tMultilineString += "DeathTime`%s\n";
		tMultilineString += "Placed`%s\n";
		tMultilineString += "Location`%d / %d / %d\n";
		tMultilineString += "No.Items`%d\n";
		
		tMultilineString = String.format( tMultilineString, pGraveFile, 
				tGrave.getCreated().toString(),
				( tGrave.getPlacedFlag() == 1 ? "§2Yes" : "§4No" ),
				(int) tGrave.getGraveLocation().xCoord,
				(int) tGrave.getGraveLocation().yCoord,
				(int) tGrave.getGraveLocation().zCoord,
				tGrave.getGraveInventory().length );
		
		TabText tt = new TabText( tMultilineString );
		tt.setPageHeight( 10 );
		tt.setTabs( 10 );
		String[] tLines = tt.getPage( 0, false ).split( "\n" );
		for (String tLine : tLines)
		{
			PlayerChatHelper.SendPlain( pCmdSender, tLine );
		}

		PlayerChatHelper.SendPlain( pCmdSender, "§4[§3Open Grave§4] [§3Teleport§4]§r" );
		
		//PlayerChatHelper.SendJsonRaw( pCmdSender, JSONChatText.simpleMessage( tt.getPage( 0, false ) ) );

		/*
		 * JSONChatText tGravePathJSON = JSONChatText.simpleMessage("[Hover here]");
		 * tGravePathJSON.HoverEvent = JSONHoverEvent.SimpleText( pFullGravePath );
		 * JSONChatText tGravePlacedFlagJSON = new JSONChatText();
		 * if (tGrave.getPlacedFlag() == 1)
		 * {
		 * tGravePlacedFlagJSON.Color = EnumChatFormatting.DARK_GREEN;
		 * tGravePlacedFlagJSON.Message = "Yes";
		 * }
		 * else
		 * {
		 * tGravePlacedFlagJSON.Color = EnumChatFormatting.DARK_RED;
		 * tGravePlacedFlagJSON.Message = "No";
		 * }
		 * try
		 * {
		 * PlayerChatHelper.SendJsonFormatted( pCmdSender, "File loc : {0}", tGravePathJSON );
		 * PlayerChatHelper.SendJsonFormatted( pCmdSender, "Placed   : {0}", tGravePlacedFlagJSON);
		 * if (tGrave.getPlacedFlag() == 1)
		 * PlayerChatHelper.SendPlain( pCmdSender, "XYZ      : [ %d / %d / %d]",
		 * (int)tGrave.getGraveLocation().xCoord,
		 * (int)tGrave.getGraveLocation().yCoord,
		 * (int)tGrave.getGraveLocation().zCoord);
		 * PlayerChatHelper.SendPlain( pCmdSender, "ItemCount: %d", tGrave.getGraveInventory().length );
		 * }
		 * catch( Exception e )
		 * {
		 * PlayerChatHelper.SendError( pCmdSender,
		 * "Error while processing your command. Please check the Server Console" );
		 * e.printStackTrace();
		 * }
		 */
	}

	private boolean InGame( ICommandSender pCmdSender )
	{
		if( !( pCmdSender instanceof EntityPlayer ) )
			return false;
		else
			return true;
	}

	// amdi g - Grave - Shows various information about a grave
	// amdi gi - GraveInspect - Opens Contents in a GUI
	// amdi gtp - GraveTeleport - TPs to GraveLocation
	// amdi gp - GravePurge - Remove GraveFile from disk

	private void SendHelpToPlayer( ICommandSender pCmdSender )
	{
		if( !InGame( pCmdSender ) )
		{
			PlayerChatHelper.SendPlain( pCmdSender, "Command can only be executed ingame" );
		}
		else
		{
			PlayerChatHelper.SendJsonRaw( (EntityPlayer) pCmdSender, jsonHelpG );
			PlayerChatHelper.SendJsonRaw( (EntityPlayer) pCmdSender, jsonHelpGI );
			PlayerChatHelper.SendJsonRaw( (EntityPlayer) pCmdSender, jsonHelpGP );
			PlayerChatHelper.SendJsonRaw( (EntityPlayer) pCmdSender, jsonHelpGTP );
		}
	}

	private void populateHelp()
	{
		jsonHelpG = new JSONChatText();
		jsonHelpGI = new JSONChatText();
		jsonHelpGP = new JSONChatText();
		jsonHelpGTP = new JSONChatText();

		jsonHelpG.Message = "/amdi g <grave file>";
		jsonHelpG.HoverEvent = JSONHoverEvent.SimpleText( "Displays various Information about a given Grave-File" );

		jsonHelpGI.Message = "/amdi gi <grave file>";
		jsonHelpGI.HoverEvent = JSONHoverEvent.SimpleText( "Opens the Grave-Inspector GUI\nShows the content of the grave" );

		jsonHelpGP.Message = "/amdi gp <grave file>";
		jsonHelpGP.HoverEvent = JSONHoverEvent.SimpleText( "Purge the Grave.\nThis will delete the File, be careful!" );

		jsonHelpGTP.Message = "/amdi gtp <grave file>";
		jsonHelpGTP.HoverEvent = JSONHoverEvent.SimpleText( "Teleport to Grave Location\nTeleports you to the Location the Grave was spawned\nMake sure you are in creative, to avoid surprises..." );
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

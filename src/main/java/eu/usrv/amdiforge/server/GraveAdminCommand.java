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

import net.minecraft.block.Block;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumChatFormatting;

import org.apache.commons.lang3.StringUtils;

import eu.usrv.amdiforge.AMDIForge;
import eu.usrv.amdiforge.GuiHandler;
import eu.usrv.amdiforge.core.graveIO.GraveNBT;
import eu.usrv.amdiforge.net.msg.GraveContentDisplayMessage;
import eu.usrv.yamcore.auxiliary.ItemDescriptor;
import eu.usrv.yamcore.auxiliary.PlayerChatHelper;
import eu.usrv.yamcore.auxiliary.TabText;
import eu.usrv.yamcore.auxiliary.classes.JSONChatText;
import eu.usrv.yamcore.auxiliary.classes.JSONClickEvent;
import eu.usrv.yamcore.auxiliary.classes.JSONHoverEvent;


public class GraveAdminCommand implements ICommand
{
  private static final String PREFIX = "inventory-";
  private JSONChatText jsonHelpG = null;
  private JSONChatText jsonHelpGI = null;
  private JSONChatText jsonHelpGTP = null;
  private JSONChatText jsonHelpGP = null;
  private JSONChatText jsonHelpExp = null;

  private List aliases;

  public GraveAdminCommand()
  {
    this.aliases = new ArrayList();
    this.aliases.add( "gadm" );
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
    return "graveadmin";
  }

  @Override
  public String getCommandUsage( ICommandSender pCommandSender )
  {
    return "Type /graveadmin help for more help";
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
      if( !tFullGravePath.exists() )
      {
        PlayerChatHelper.SendError( tEP, "GraveFile specified could not be found" );
      }
      else
      {
        if( tSubCommand.equalsIgnoreCase( "info" ) || tSubCommand.equalsIgnoreCase( "i" ) )
        {
          displayGraveInfo( tEP, tGraveArg, tFullGravePath.getAbsolutePath() );
        }
        else if( tSubCommand.equalsIgnoreCase( "show" ) || tSubCommand.equalsIgnoreCase( "s" ) )
        {
          GuiHandler.PendingGraveUIs.put( tEP.getUniqueID().toString(), tFullGravePath.getAbsolutePath() );

          AMDIForge.NW.sendTo( new GraveContentDisplayMessage( GraveNBT.loadGraveFile( tFullGravePath.getAbsoluteFile() ) ), (EntityPlayerMP) tEP );
          tEP.openGui( AMDIForge.instance, GuiHandler.GUI_GRAVEVIEW, tEP.worldObj, (int) tEP.posX, (int) tEP.posY, (int) tEP.posZ );
        }
        else if( tSubCommand.equalsIgnoreCase( "teleport" ) || tSubCommand.equalsIgnoreCase( "tp" ) )
        {
          teleportToGrave( tEP, tFullGravePath.getAbsolutePath() );
        }
        else if( tSubCommand.equalsIgnoreCase( "delete" ) || tSubCommand.equalsIgnoreCase( "rm" ) )
        {
          if( tFullGravePath.delete() )
            PlayerChatHelper.SendNotifyPositive( tEP, "GraveFile has been deleted" );
          else
            PlayerChatHelper.SendNotifyWarning( tEP, "GraveFile could not be deleted, something went wrong!" );
        }
        else if( tSubCommand.equalsIgnoreCase( "export" ) || tSubCommand.equalsIgnoreCase( "e" ) )
        {
          exportGraveToChest( tEP, tFullGravePath.getAbsolutePath(), false );
        }
        else if( tSubCommand.equalsIgnoreCase( "forceexport" ) )
        {
          exportGraveToChest( tEP, tFullGravePath.getAbsolutePath(), true );
        }
        else
        {
          SendHelpToPlayer( pCmdSender );
        }
      }
    }
  }

  private void exportGraveToChest( EntityPlayer pEP, String pFullGravePath, boolean pForce )
  {
    try
    {
      GraveNBT tGrave = GraveNBT.getGrave( pFullGravePath );

      if( tGrave.getPlacedFlag() != 0 && !pForce )
      {
        PlayerChatHelper.SendError( pEP, "Grave has been spawned, according to the GraveFile." );
        PlayerChatHelper.SendError( pEP, "Make sure the Grave really is not existing and the" );
        PlayerChatHelper.SendError( pEP, "player is not trying to trick you into duping!" );
        PlayerChatHelper.SendError( pEP, "If you are sure you want to export a copy of the" );
        PlayerChatHelper.SendError( pEP, "grave, type" );
        PlayerChatHelper.SendError( pEP, "   /graveadmin forceexport <gravefile>" );

        return;
      }
      else if( tGrave.getPlacedFlag() != 0 && pForce )
      {
        List<EntityPlayerMP> tAllPlayers = MinecraftServer.getServer().getConfigurationManager().playerEntityList;
        for( EntityPlayerMP tPlayer : tAllPlayers )
        {
          if( tPlayer.canCommandSenderUseCommand( 1, "" ) )
          {
            PlayerChatHelper.SendWarn( tPlayer, "%s just spawned a grave that was reported", pEP.getDisplayName() );
            PlayerChatHelper.SendWarn( tPlayer, "to exist in-world" );
          }
        }
      }

      ItemDescriptor tTargetChest = ItemDescriptor.fromString( "IronChest:BlockIronChest:6", true );
      if( tTargetChest != null )
      {
        int x = (int) pEP.posX;
        int y = (int) pEP.posY;
        int z = (int) pEP.posZ;

        int tMaxIterations = 10;
        Block tExistingBlock = pEP.worldObj.getBlock( x, y, z );
        while( tExistingBlock != Blocks.air && tMaxIterations > 0 )
        {
          x += ( AMDIForge.Rnd.nextInt( 10 ) - 5 );
          y += ( AMDIForge.Rnd.nextInt( 10 ) - 5 );
          z += ( AMDIForge.Rnd.nextInt( 10 ) - 5 );
          tMaxIterations--;
          tExistingBlock = pEP.worldObj.getBlock( x, y, z );
        }
        if( tExistingBlock != Blocks.air )
        {
          PlayerChatHelper.SendError( pEP, "Unable to find some free space at your current location. Please move around and try again" );
          return;
        }

        Block tBlockChest = Block.getBlockFromItem( tTargetChest.getItem() );

        if( tBlockChest != null )
        {
          pEP.worldObj.setBlock( x, y, z, tBlockChest );

          IInventory entityChestInventory = (IInventory) pEP.worldObj.getTileEntity( x, y, z );
          if( entityChestInventory != null )
          {
            for( int i = 0; i < tGrave.getGraveInventory().length; i++ )
            {
              if( i < entityChestInventory.getSizeInventory() )
                entityChestInventory.setInventorySlotContents( i, tGrave.getGraveInventory()[i].copy() );
              else
              {
                PlayerChatHelper.SendNotifyWarning( pEP, "Too many items in grave. Export truncated" );
                break;
              }
            }
            PlayerChatHelper.SendNotifyPositive( pEP, "Grave-Export complete; ChestLocation: %d / %d / %d", x, y, z );
          }
          else
          {
            PlayerChatHelper.SendError( pEP, "Unable to fill grave-contents to chest; chest export not possible" );
          }
        }
        else
        {
          PlayerChatHelper.SendError( pEP, "Unable to find IronChest in the GameRegistry, chest export not possible" );
        }
      }
      else
      {
        PlayerChatHelper.SendError( pEP, "Unable to find IronChest in the GameRegistry, chest export not possible" );
      }
    }
    catch( Exception e )
    {
      PlayerChatHelper.SendError( pEP, "Can't open grave file; chest export not possible" );
    }

  }

  private void teleportToGrave( EntityPlayer pEP, String pFullGravePath )
  {
    GraveNBT tGrave = GraveNBT.getGrave( pFullGravePath );
    PlayerChatHelper.SendNotifyPositive( pEP, "Teleporting to GraveLocation at %d / %d / %d", (int) tGrave.getGraveLocation().xCoord, (int) tGrave.getGraveLocation().yCoord, (int) tGrave.getGraveLocation().zCoord );
    pEP.setPositionAndUpdate( tGrave.getGraveLocation().xCoord, tGrave.getGraveLocation().yCoord, tGrave.getGraveLocation().zCoord );
  }

  private void displayGraveInfo( EntityPlayer pCmdSender, String pGraveFile, String pFullGravePath )
  {
    try
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
          ( tGrave.getPlacedFlag() == 1 ? EnumChatFormatting.DARK_GREEN + "Yes" + EnumChatFormatting.RESET : EnumChatFormatting.DARK_RED + "No" + EnumChatFormatting.RESET ),
          (int) tGrave.getGraveLocation().xCoord,
          (int) tGrave.getGraveLocation().yCoord,
          (int) tGrave.getGraveLocation().zCoord,
          tGrave.getGraveInventory().length );

      TabText tt = new TabText( tMultilineString );
      tt.setPageHeight( 10 );
      tt.setTabs( 10 );
      String[] tLines = tt.getPage( 0, false ).split( "\n" );
      for( String tLine : tLines )
      {
        PlayerChatHelper.SendPlain( pCmdSender, tLine );
      }

      JSONChatText tGraveOpen = JSONChatText.simpleMessage( "[Open]" );
      tGraveOpen.Color = EnumChatFormatting.AQUA;
      tGraveOpen.HoverEvent = JSONHoverEvent.SimpleText( "Opens this Grave in the Grave-Inspector" );
      tGraveOpen.ClickEvent = JSONClickEvent.runCommand( String.format( "/graveadmin show %s", pGraveFile ) );

      JSONChatText tGraveTeleport = JSONChatText.simpleMessage( "[Teleport]" );
      tGraveTeleport.Color = EnumChatFormatting.AQUA;
      tGraveTeleport.HoverEvent = JSONHoverEvent.SimpleText( "Teleports you directly to the recorded spawn-location of this grave" );
      tGraveTeleport.ClickEvent = JSONClickEvent.runCommand( String.format( "/graveadmin teleport %s", pGraveFile ) );

      JSONChatText tGraveExport = JSONChatText.simpleMessage( "[Export]" );
      tGraveExport.Color = EnumChatFormatting.DARK_PURPLE;
      tGraveExport.HoverEvent = JSONHoverEvent.SimpleText( "Exports the grave-contents to a chest spawned at your location" );
      tGraveExport.ClickEvent = JSONClickEvent.suggestCommand( String.format( "/graveadmin export %s", pGraveFile ) );

      JSONChatText tGravePurge = JSONChatText.simpleMessage( "[DELETE]" );
      tGravePurge.Color = EnumChatFormatting.DARK_RED;
      tGravePurge.HoverEvent = JSONHoverEvent.SimpleText( "Deletes the GraveFile from Server.\nThis command will run immediately! Be careful" );
      tGravePurge.ClickEvent = JSONClickEvent.suggestCommand( String.format( "/graveadmin delete %s", pGraveFile ) );

      try
      {
        PlayerChatHelper.SendJsonFormatted( pCmdSender, "Possible actions: {0} {1} {2} {3} ", tGraveOpen, tGraveTeleport, tGraveExport, tGravePurge );
      }
      catch( Exception e )
      {
        e.printStackTrace();
      }
    }
    catch( Exception ex )
    {
      PlayerChatHelper.SendError( pCmdSender, "Unable to open Grave" );
    }
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
      PlayerChatHelper.SendJsonRaw( (EntityPlayer) pCmdSender, jsonHelpG );
      PlayerChatHelper.SendJsonRaw( (EntityPlayer) pCmdSender, jsonHelpGI );
      PlayerChatHelper.SendJsonRaw( (EntityPlayer) pCmdSender, jsonHelpGP );
      PlayerChatHelper.SendJsonRaw( (EntityPlayer) pCmdSender, jsonHelpExp );
      PlayerChatHelper.SendJsonRaw( (EntityPlayer) pCmdSender, jsonHelpGTP );
      PlayerChatHelper.SendPlain( (EntityPlayer) pCmdSender, "Press [TAB] to auto-complete grave-names" );
    }
  }

  private void populateHelp()
  {
    jsonHelpG = new JSONChatText();
    jsonHelpGI = new JSONChatText();
    jsonHelpGP = new JSONChatText();
    jsonHelpExp = new JSONChatText();
    jsonHelpGTP = new JSONChatText();

    jsonHelpG.Message = "/graveadmin [info/i] <grave file>";
    jsonHelpG.HoverEvent = JSONHoverEvent.SimpleText( "Displays various Information about a given Grave-File" );

    jsonHelpGI.Message = "/graveadmin [show/s] <grave file>";
    jsonHelpGI.HoverEvent = JSONHoverEvent.SimpleText( "Opens the Grave-Inspector GUI\nShows the content of the grave" );

    jsonHelpGP.Message = "/graveadmin [delete/rm] <grave file>";
    jsonHelpGP.HoverEvent = JSONHoverEvent.SimpleText( "Purge the Grave.\nThis will delete the File, be careful!" );

    jsonHelpExp.Message = "/graveadmin [export/e] <grave file>";
    jsonHelpExp.HoverEvent = JSONHoverEvent.SimpleText( "Exports the Items in given grave in a chest\nthat will be spawned in front of you" );

    jsonHelpGTP.Message = "/graveadmin [teleport/tp] <grave file>";
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

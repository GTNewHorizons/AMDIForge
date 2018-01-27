package eu.usrv.amdiforge.server;


import eu.usrv.amdiforge.AMDIForge;
import eu.usrv.yamcore.auxiliary.PlayerChatHelper;

import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class GraveLookupRequestCommand implements ICommand
{
  private List aliases;

  public GraveLookupRequestCommand()
  {
    this.aliases = new ArrayList();
    this.aliases.add( "gravehelp" );
  }

  @Override
  public int compareTo( Object arg0 )
  {
    return 0;
  }

  @Override
  public String getCommandName()
  {
    return "gravehelp";
  }

  @Override
  public String getCommandUsage( ICommandSender pCommandSender )
  {
    return "Type /gravehelp help for more help";
  }

  @Override
  public List getCommandAliases()
  {
    return this.aliases;
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
    if( pArgs.length != 1 )
      SendHelpToPlayer( tEP );
    else
    {
      if( pArgs[0].equalsIgnoreCase( "request" ))
      {
        String accessCode = UUID.randomUUID().toString().substring( 26 );

        try
        {
          PreparedStatement ps = AMDIForge.instance.getConnection().prepareStatement( "INSERT INTO `mcdata`.`lookupcodes` (`code`, `targetUID`, `targetName`) VALUES (?,?,?);" );
          ps.setString( 1, accessCode );
          ps.setString( 2, tEP.getUniqueID().toString() );
          ps.setString( 3, tEP.getDisplayName() );
          ps.executeUpdate();

          PlayerChatHelper.SendInfo( tEP, String.format( "Your Code is: %s It is valid for 10 Minutes", accessCode ) );
        }
        catch( SQLException e1 )
        {
          e1.printStackTrace();
          PlayerChatHelper.SendError( tEP, "We're sorry, but something bad has happened on our side. Please contact a Staff Member, thank you!" );
        }
      }
      else
        SendHelpToPlayer( tEP );
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
      PlayerChatHelper.SendPlain( pCmdSender, "Type /gravehelp request to get a lookup token" );
    }
  }


  @Override
  public boolean canCommandSenderUseCommand( ICommandSender pCommandSender )
  {
      return true;
  }

  @Override
  public List addTabCompletionOptions( ICommandSender sender, String[] args )
  {
    return null;
  }

  @Override
  public boolean isUsernameIndex( String[] p_82358_1_, int p_82358_2_ )
  {
    return false;
  }
}

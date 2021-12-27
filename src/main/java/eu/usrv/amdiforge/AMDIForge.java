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


import java.sql.Connection;
import java.sql.SQLException;
import java.util.Random;

import eu.usrv.amdiforge.database.MySQL;
import eu.usrv.amdiforge.server.GraveLookupRequestCommand;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.common.MinecraftForge;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import eu.usrv.amdiforge.config.AMDIConfig;
import eu.usrv.amdiforge.core.GraveFileHandler;
import eu.usrv.amdiforge.events.LivingCheckSpawnEventHandler;
import eu.usrv.amdiforge.net.AMDIDispatcher;
import eu.usrv.amdiforge.proxy.CommonProxy;
import eu.usrv.amdiforge.runnables.RunnableManager;
import eu.usrv.amdiforge.server.GraveAdminCommand;
import eu.usrv.yamcore.auxiliary.IngameErrorLog;
import eu.usrv.yamcore.auxiliary.LogHelper;


@Mod( modid = AMDIForge.MODID, name = AMDIForge.MODNAME, version = AMDIForge.VERSION, dependencies = "required-after:Forge@[10.13.4.1558,);required-after:YAMCore@[0.5.69,);", acceptableRemoteVersions = "*" )
public class AMDIForge
{
  public static final String MODID = "amdiforge";
  public static final String VERSION = "GRADLETOKEN_VERSION";
  public static final String MODNAME = "A.M.D.I. Forge";
  public static final String NICEFOLDERNAME = "AMDI";
  public static AMDIConfig AMDICfg = null;
  public static GraveFileHandler GraveHdl = new GraveFileHandler();
  public static IngameErrorLog AdminLogonErrors = null;
  public static LogHelper Logger = new LogHelper( MODID );
  public static Random Rnd = null;
  public static AMDIDispatcher NW;
  public static LivingCheckSpawnEventHandler SpawnLimiter = null;
  
  private static RunnableManager _RM = null;


  private static MySQL mSQL = null;
  private Connection mCon = null;

  public Connection getConnection()
  {
    try{
      if(!mSQL.checkConnection() || mCon == null)
        mCon = mSQL.openConnection();

    } catch (SQLException e) {
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }

    return mCon;
  }


  @SidedProxy( clientSide = "eu.usrv.amdiforge.proxy.ClientProxy", serverSide = "eu.usrv.amdiforge.proxy.CommonProxy" )
  public static CommonProxy proxy;

  @Instance( MODID )
  public static AMDIForge instance;

  @EventHandler
  public void PreInit( FMLPreInitializationEvent pEvent )
  {
    Rnd = new Random( System.currentTimeMillis() );
    AMDICfg = new AMDIConfig( pEvent.getModConfigurationDirectory(), NICEFOLDERNAME, MODID );
    if( !AMDICfg.LoadConfig() )
      Logger.error( String.format( "%s could not load its config file. Things are going to be weird!", MODID ) );

    AdminLogonErrors = new IngameErrorLog();
    SpawnLimiter = new LivingCheckSpawnEventHandler( this, pEvent.getModConfigurationDirectory() );
    
    NW = new AMDIDispatcher();
    NW.registerPackets();


    mSQL = new MySQL(AMDICfg.MySQL_Server, "3306", AMDICfg.MySQL_DB, AMDICfg.MySQL_User, AMDICfg.MySQL_Password);
    try {
      mCon = mSQL.openConnection();
    } catch (ClassNotFoundException | SQLException e) {
      e.printStackTrace();
      Logger.error("No MYSQL server could be reached! You probably want to remove AMDIForge.jar unless you use it.");
      FMLCommonHandler.instance().exitJava(-99, false);
    }
  }

  @EventHandler
  public void init( FMLInitializationEvent pEvent )
  {
    FMLCommonHandler.instance().bus().register( AdminLogonErrors );
    
    MinecraftForge.EVENT_BUS.register( SpawnLimiter );
    NetworkRegistry.INSTANCE.registerGuiHandler( this, new GuiHandler() );
  }

  /**
   * Do some stuff once the server starts
   * 
   * @param pEvent
   */
  @EventHandler
  public void serverLoad( FMLServerStartingEvent pEvent )
  {
    if (AMDICfg.EnableGraveAdminCommand)
      pEvent.registerServerCommand( new GraveAdminCommand() );
    if (AMDICfg.EnableGraveHelpCommand)
      pEvent.registerServerCommand( new GraveLookupRequestCommand() );
    _RM = RunnableManager.getInstance();
  }

}

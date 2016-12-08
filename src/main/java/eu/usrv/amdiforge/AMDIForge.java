package eu.usrv.amdiforge;

import java.util.Random;

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
import eu.usrv.amdiforge.net.AMDIDispatcher;
import eu.usrv.amdiforge.proxy.CommonProxy;
import eu.usrv.amdiforge.server.AMDICommand;
import eu.usrv.yamcore.auxiliary.IngameErrorLog;
import eu.usrv.yamcore.auxiliary.LogHelper;


@Mod( modid = AMDIForge.MODID, name = AMDIForge.MODNAME, version = AMDIForge.VERSION, dependencies = "required-after:Forge@[10.13.2.1291,);required-after:YAMCore@[0.5.66,);" )
public class AMDIForge 
{
	public static final String MODID = "amdiforge";
	public static final String VERSION = "GRADLETOKEN_VERSION";
	public static final String MODNAME = "A.M.D.I. Forge";
	public static final String NICEFOLDERNAME = "AMDI";
	public static AMDIConfig AMDICfg = null;
	public static GraveFileHandler GraveHdl = new GraveFileHandler( );
	public static IngameErrorLog AdminLogonErrors = null;
	public static LogHelper Logger = new LogHelper( MODID );
	public static Random Rnd = null;
	public static AMDIDispatcher NW;

	@SidedProxy( clientSide = "eu.usrv.amdiforge.proxy.ClientProxy", serverSide = "eu.usrv.amdiforge.proxy.CommonProxy" )
	public static CommonProxy proxy;

	@Instance( MODID )
	public static AMDIForge instance;

	@EventHandler
	public void PreInit( FMLPreInitializationEvent pEvent )
	{
		Rnd = new Random( System.currentTimeMillis() );
		AMDICfg = new AMDIConfig( pEvent.getModConfigurationDirectory(), NICEFOLDERNAME, MODID );
		if (!AMDICfg.LoadConfig())
			Logger.error(String.format("%s could not load its config file. Things are going to be weird!", MODID));
        
		AdminLogonErrors = new IngameErrorLog();
		
//		NW = new AMDIDispatcher();
//		NW.registerPackets();
	}

	@EventHandler
	public void init( FMLInitializationEvent event )
	{
		FMLCommonHandler.instance().bus().register( AdminLogonErrors );
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
		pEvent.registerServerCommand( new AMDICommand() );
	}
	
}

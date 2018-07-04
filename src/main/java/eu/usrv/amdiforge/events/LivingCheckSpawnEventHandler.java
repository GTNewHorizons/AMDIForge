
package eu.usrv.amdiforge.events;


import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingSpawnEvent.CheckSpawn;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import eu.usrv.amdiforge.AMDIForge;
import eu.usrv.amdiforge.runnables.EntityCounter;
import eu.usrv.amdiforge.xml.spawnlimit.SpawnLimitsConfig;
import eu.usrv.amdiforge.xml.spawnlimit.SpawnLimitsConfig.LimitedEntity;
import eu.usrv.amdiforge.xml.spawnlimit.SpawnLimitsConfig.LimitedEntity.SpawnLimitWorld;
import eu.usrv.amdiforge.xml.spawnlimit.SpawnLimitsConfigFactory;
import eu.usrv.yamcore.auxiliary.LogHelper;


public class LivingCheckSpawnEventHandler
{
  private AMDIForge _mMain = null;
  private SpawnLimitsConfig _mSLC = null;
  private SpawnLimitsConfigFactory _mSLCF = new SpawnLimitsConfigFactory();
  private String _mConfigFileName;
  private LogHelper _mLogger = AMDIForge.Logger;
  private boolean _mInitialized = false;

  public LivingCheckSpawnEventHandler( AMDIForge pMain, File pConfigBaseDir )
  {
    _mMain = pMain;

    File tConfDir = new File( pConfigBaseDir, AMDIForge.NICEFOLDERNAME );
    if( !tConfDir.exists() )
      tConfDir.mkdirs();

    _mConfigFileName = new File( tConfDir, "MobSpawnLimits.xml" ).toString();

    LoadConfig();
  }

  private void InitSampleConfig()
  {
    SpawnLimitWorld tWorld = _mSLCF.createLimitedWorld( 0 );
    LimitedEntity tLE = _mSLCF.createLimitedEntity( net.minecraft.entity.monster.EntityCaveSpider.class );
    tLE.getWorldConfig().add( tWorld );

    _mSLC = new SpawnLimitsConfig();
    _mSLC.getEntityList().add( tLE );
  }

  private boolean SaveSpawnLimits()
  {
    try
    {
      JAXBContext tJaxbCtx = JAXBContext.newInstance( SpawnLimitsConfig.class );
      Marshaller jaxMarsh = tJaxbCtx.createMarshaller();
      jaxMarsh.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, true );
      jaxMarsh.marshal( _mSLC, new FileOutputStream( _mConfigFileName, false ) );

      return true;
    }
    catch( Exception e )
    {
      _mLogger.error( "[SpawnLimits] Unable to create new MobSpawnLimits.xml. Is the config directory write protected?" );
      e.printStackTrace();
      return false;
    }
  }

  private void LoadConfig()
  {
    File tConfigFile = new File( _mConfigFileName );
    if( !tConfigFile.exists() )
    {
      InitSampleConfig();
      SaveSpawnLimits();
    }

    if( !ReloadLootGroups() )
    {
      _mLogger.warn( "[SpawnLimits] Configuration File seems to be damaged, nothing will be loaded!" );
      AMDIForge.AdminLogonErrors.AddErrorLogOnAdminJoin( "[SpawnLimits] Config file not loaded due errors" );
      InitSampleConfig();
    }
    _mInitialized = true;
  }

  public boolean reload()
  {
    boolean tState = ReloadLootGroups();
    if( _mInitialized )
      _mLogger.error( "[SpawnLimits] Reload of SpawnLimits file failed" );

    return tState;
  }

  private boolean ReloadLootGroups()
  {
    try
    {
      JAXBContext tJaxbCtx = JAXBContext.newInstance( SpawnLimitsConfig.class );
      Unmarshaller jaxUnmarsh = tJaxbCtx.createUnmarshaller();
      File tConfigFile = new File( _mConfigFileName );
      _mSLC = (SpawnLimitsConfig) jaxUnmarsh.unmarshal( tConfigFile );

      return true;
    }
    catch( Exception e )
    {
      e.printStackTrace();
      return false;
    }
  }

  @SubscribeEvent( receiveCanceled = false, priority = EventPriority.LOWEST )
  public void checkSpawnEvent( CheckSpawn pEvent )
  {
    if( FMLCommonHandler.instance().getEffectiveSide().isClient() )
      return;

    if( !( pEvent.entityLiving instanceof EntityLiving ) )
      return;

    World tTargetWorld = pEvent.world;
    Entity tEntityToSpawn = pEvent.entity;
  
    pEvent.setResult( getEventResult( tTargetWorld, tEntityToSpawn, pEvent.getResult() ) );
    if( pEvent.getResult() == Result.ALLOW ) // Increase
      EntityCounter.getInstance().trackSpawnEvent( tTargetWorld, tEntityToSpawn );
  }

  private Event.Result getEventResult( World pWorld, Entity pEntity, Event.Result pCurrentEventResult )
  {
    Event.Result tReturn = pCurrentEventResult;

    String tEntityClassName = pEntity.getClass().getCanonicalName();
    int tDimensionID = pWorld.provider.dimensionId;

    String tDebugResultTemplate = "[SPAWNLIMITER] Spawn %s. Reason: %s";
    String tResult = "-";

    List<SpawnLimitWorld> tWorldList = null;
    SpawnLimitWorld tTargetWorld = null;
    boolean tGlobalWhitelisted = false;

    // Check if entity is known already
    for( LimitedEntity tEntity : _mSLC.getEntityList() )
    {
      // Exact name match
      if( tEntity.matchNameExact() )
      {
        if( tEntityClassName.equalsIgnoreCase( tEntity.getClassName() ) )
        {
          tWorldList = tEntity.getWorldConfig();
          tGlobalWhitelisted = tEntity.getGlobalWhitelisted();
          break;
        }
      }
      else
      {
        // Part name-match; "Skeleton" matches all entities with Skeleton in their classname
        if( tEntityClassName.toLowerCase().contains( tEntity.getClassName().toLowerCase() ) )
        {
          tWorldList = tEntity.getWorldConfig();
          tGlobalWhitelisted = tEntity.getGlobalWhitelisted();
          break;
        }
      }
    }

    if( tWorldList == null )
    {
      // Entity not found. Create an entry for it
      if( AMDIForge.AMDICfg.TraceAndExportUnknownEntities )
      {
        _mLogger.info( String.format( "Unknown Entity spawn detected: [%s] adding to config file", tEntityClassName ) );
        _mSLC.getEntityList().add( _mSLCF.createLimitedEntity( pEntity.getClass() ) );
        SaveSpawnLimits();
      }
    }

    // If we have a global whitelist (Global force to spawn), set result to allow
    if( tGlobalWhitelisted )
    {
      tResult = "Spawn allowed; Global-Whitelisted";
      tReturn = Result.ALLOW;
    }
    // No global whitelist. Check dimensions defined
    else
    {
      if( tWorldList != null )
      {
        for( SpawnLimitWorld tWorld : tWorldList )
        {
          if( tWorld.getDimID() == tDimensionID )
          {
            tTargetWorld = tWorld;
            break;
          }
        }
      }

      // There is an entry for a dimension
      if( tTargetWorld != null )
      {
        // Whitelisted in this dimension? (Forced Spawn)
        if( tTargetWorld.getWhitelisted() )
        {
          tResult = "Spawn allowed; World-Whitelisted";
          tReturn = Result.ALLOW;
        }
        else
        {
          // Check number of entities already alive and deny if it exceeds the maximum number defined
          int tActiveEntities = EntityCounter.getInstance().getEntityCountForWorld( tEntityClassName, tTargetWorld.getDimID() );
          if( tActiveEntities >= tTargetWorld.getMaxSpawn() )
          {
            tResult = "Spawn denied; Max-Entities reached";
            tReturn = Result.DENY;
          }
          // Else: Do nothing
        }
      }
      // Else: No World defined, do nothing
    }

    if (tReturn != Result.DEFAULT && AMDIForge.AMDICfg.DoDebugMessages)
    {
      _mLogger.info( String.format( "[SPAWNLIMITER] Attempt to spawn an instance of [%s] in World [%d]", tEntityClassName, tDimensionID ) );
      _mLogger.info( String.format( tDebugResultTemplate, tReturn.toString(), tResult ) );
    }
    
    // Now returns the previous Eventstate for this entity, instead of DEFAULT, which re-allowed all entities blocked by Magnumtorch
    // and all other mob-spawn-limiting blocks
    return tReturn;
  }
}

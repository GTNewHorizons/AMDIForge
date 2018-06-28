
package eu.usrv.amdiforge.events;


import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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

    Result oldResult = pEvent.getResult();
    // We check if the result wasn't changed at all,
    //   and change only if it was not changed
    if(oldResult == Result.DEFAULT) {
      Result result = getEventResult(pEvent.world, pEvent.entity);
      if (result != oldResult) {
        pEvent.setResult(result);
        if (result == Result.ALLOW) // Increase
          EntityCounter.getInstance().trackSpawnEvent(pEvent.world, pEvent.entity);
      }
    }
  }

  private Event.Result getEventResult( World pWorld, Entity pEntity )
  {
    Event.Result tReturn = Result.DEFAULT;

    String tEntityClassName = pEntity.getClass().getCanonicalName();
    int tDimensionID = pWorld.provider.dimensionId;

    String tDebugResultTemplate = "[SPAWNLIMITER] Spawn %s. Reason: %s";
    String tResult = "-";

    List<SpawnLimitWorld> tWorldList = null;
    SpawnLimitWorld tTargetWorld = null;
    boolean tGlobalWhitelisted = false;

    for( LimitedEntity tEntity : _mSLC.getEntityList() )
    {
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
        if( tEntityClassName.toLowerCase().contains( tEntityClassName.toLowerCase() ) )
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

    if( tGlobalWhitelisted )
    {
      tResult = "Spawn allowed; Global-Whitelisted";
      tReturn = Result.ALLOW;
    }
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

      if( tTargetWorld != null )
      {
        if( tTargetWorld.getWhitelisted() )
        {
          tResult = "Spawn allowed; World-Whitelisted";
          tReturn = Result.ALLOW;
        }
        else
        {
          int tActiveEntities = EntityCounter.getInstance().getEntityCountForWorld( tEntityClassName, tTargetWorld.getDimID() );
          if( tActiveEntities >= tTargetWorld.getMaxSpawn() )
          {
            tResult = "Spawn denied; Max-Entities reached";
            tReturn = Result.DENY;
          }
          else
          {
            tResult = "Spawn allowed";
            tReturn = Result.ALLOW;
          }
        }
      }
    }

    if (tReturn != Result.DEFAULT && AMDIForge.AMDICfg.DoDebugMessages)
    {
      _mLogger.info( String.format( "[SPAWNLIMITER] Attempt to spawn an instance of [%s] in World [%d]", tEntityClassName, tDimensionID ) );
      _mLogger.info( String.format( tDebugResultTemplate, tReturn.toString(), tResult ) );
    }
    return tReturn;
  }
}

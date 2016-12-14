
package eu.usrv.amdiforge.xml.spawnlimit;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;


@XmlAccessorType( XmlAccessType.FIELD )
@XmlRootElement( name = "LimitedEntities" )
public class SpawnLimitsConfig
{
  @XmlElement( name = "LimitedEntity" )
  private List<SpawnLimitsConfig.LimitedEntity> _mLimitedEntities;

  private void Init()
  {
    if( _mLimitedEntities == null )
      _mLimitedEntities = new ArrayList<SpawnLimitsConfig.LimitedEntity>();
  }

  public List<SpawnLimitsConfig.LimitedEntity> getEntityList()
  {
    Init();
    return _mLimitedEntities;
  }

  public static class LimitedEntity
  {
    @XmlAttribute( name = "EntityClassName" )
    protected String mClassName;

    @XmlAttribute( name = "ExactNameMatch" )
    protected boolean mExactNameMatch;

    @XmlAttribute( name = "GlobalWhitelisted" )
    protected boolean mGlobalWhitelisted;
    
    @XmlElement( name = "World" )
    private List<SpawnLimitsConfig.LimitedEntity.SpawnLimitWorld> mWorldConfig;

    public boolean getGlobalWhitelisted()
    {
      return mGlobalWhitelisted;
    }
    
    public String getClassName()
    {
      return mClassName;
    }

    public boolean matchNameExact()
    {
      return mExactNameMatch;
    }

    private void Init()
    {
      if( mWorldConfig == null )
        mWorldConfig = new ArrayList<SpawnLimitsConfig.LimitedEntity.SpawnLimitWorld>();
    }

    public List<SpawnLimitsConfig.LimitedEntity.SpawnLimitWorld> getWorldConfig()
    {
      Init();
      return mWorldConfig;
    }

    @XmlAccessorType( XmlAccessType.FIELD )
    @XmlType
    public static class SpawnLimitWorld
    {
      @XmlAttribute( name = "DimensionID" )
      protected int mDimID;

      @XmlAttribute( name = "MaximumSpawnCount" )
      protected int mMaxSpawn;

      @XmlAttribute( name = "AlwaysAllowSpawn" )
      protected boolean mWhitelisted;

      public int getDimID()
      {
        return mDimID;
      }

      public int getMaxSpawn()
      {
        return mMaxSpawn;
      }

      public boolean getWhitelisted()
      {
        return mWhitelisted;
      }
    }
  }
}

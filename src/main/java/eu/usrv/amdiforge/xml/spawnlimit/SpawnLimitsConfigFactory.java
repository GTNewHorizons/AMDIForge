
package eu.usrv.amdiforge.xml.spawnlimit;

import net.minecraft.world.World;

import eu.usrv.amdiforge.xml.spawnlimit.SpawnLimitsConfig.LimitedEntity;
import eu.usrv.amdiforge.xml.spawnlimit.SpawnLimitsConfig.LimitedEntity.SpawnLimitWorld;

public class SpawnLimitsConfigFactory {

    public LimitedEntity createLimitedEntity(Class pEntityClass) {
        LimitedEntity tLE = new LimitedEntity();
        tLE.mClassName = pEntityClass.getCanonicalName();
        tLE.mExactNameMatch = true;

        return tLE;
    }

    public SpawnLimitWorld createLimitedWorld(World pDimension) {
        return createLimitedWorld(pDimension.provider.dimensionId);
    }

    public SpawnLimitWorld createLimitedWorld(int pDimensionID) {
        SpawnLimitWorld tLW = new SpawnLimitWorld();
        tLW.mDimID = pDimensionID;
        tLW.mMaxSpawn = -1;
        tLW.mWhitelisted = false;

        return tLW;
    }
}

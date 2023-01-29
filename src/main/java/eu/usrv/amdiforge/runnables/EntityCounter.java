
package eu.usrv.amdiforge.runnables;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import eu.usrv.amdiforge.AMDIForge;

public class EntityCounter implements Runnable {

    private static class EntityDef {

        public String EntityClassName;
        public List<WorldDef> WorldList;
        public int EntityID;

        public EntityDef(Entity pEntity) {
            EntityID = pEntity.getEntityId();
            EntityClassName = pEntity.getClass().getCanonicalName();
            WorldList = new ArrayList<WorldDef>();
        }
    }

    private static class WorldDef {

        public int DimensionID;
        public int EntityCount;

        public WorldDef(int pDimensionID, int pEntityCount) {
            DimensionID = pDimensionID;
            EntityCount = pEntityCount;
        }
    }

    private static EntityCounter _mInstance = null;
    private ReadWriteLock _mLock = new ReentrantReadWriteLock();
    private List<EntityDef> _mEntities;

    public static EntityCounter getInstance() {
        if (_mInstance == null) _mInstance = new EntityCounter();

        return _mInstance;
    }

    private EntityCounter() {
        _mEntities = new ArrayList<EntityDef>();
    }

    public int getEntityCountForWorld(String pEntityClassName, int pDimensionID) {
        int tReturnValue = 0;

        _mLock.readLock().lock();
        try {
            for (EntityDef tEntity : _mEntities) {
                if (tEntity.EntityClassName.equalsIgnoreCase(pEntityClassName)) {
                    for (WorldDef tWorld : tEntity.WorldList) {
                        if (tWorld.DimensionID == pDimensionID) {
                            tReturnValue = tWorld.EntityCount;
                            break;
                        }
                    }
                }
            }
        } finally {
            _mLock.readLock().unlock();
        }

        return tReturnValue;
    }

    @Override
    public void run() {
        List<EntityDef> tEntities = new ArrayList<EntityDef>();

        for (WorldServer ws : MinecraftServer.getServer().worldServers) {
            for (int j = 0; j < ws.loadedEntityList.size(); ++j) {
                Entity entity = (Entity) ws.loadedEntityList.get(j);

                String tCanonicalName = entity.getClass().getCanonicalName();
                if (AMDIForge.AMDICfg.DoDebugMessages) AMDIForge.Logger.info(
                        String.format("Found living entity %s in world %d", tCanonicalName, ws.provider.dimensionId));

                boolean tFoundEntity = false;
                for (EntityDef tEntity : tEntities) {
                    if (tEntity.EntityClassName.equalsIgnoreCase(tCanonicalName)) {
                        boolean tFoundWorld = false;
                        for (WorldDef tWorld : tEntity.WorldList) {
                            if (tWorld.DimensionID == ws.provider.dimensionId) {
                                tFoundWorld = true;
                                tWorld.EntityCount += 1;
                            }

                            if (!tFoundWorld) tEntity.WorldList.add(new WorldDef(ws.provider.dimensionId, 1));
                        }
                    }
                }
                if (!tFoundEntity) tEntities.add(new EntityDef(entity));
            }
        }

        _mLock.writeLock().lock();
        try {
            _mEntities = tEntities;
        } finally {
            _mLock.writeLock().unlock();
        }
    }

    public void trackSpawnEvent(World pTargetWorld, Entity pEntityToSpawn) {
        String tCanonicalName = pEntityToSpawn.getClass().getCanonicalName();
        int tTargetWorld = pTargetWorld.provider.dimensionId;

        _mLock.writeLock().lock();
        try {
            boolean tFoundEntity = false;
            for (EntityDef tEntity : _mEntities) {
                if (tEntity.EntityClassName.equalsIgnoreCase(tCanonicalName)) {
                    boolean tFoundWorld = false;
                    for (WorldDef tWorld : tEntity.WorldList) {
                        if (tWorld.DimensionID == tTargetWorld) {
                            tFoundWorld = true;
                            tWorld.EntityCount += 1;
                        }

                        if (!tFoundWorld) tEntity.WorldList.add(new WorldDef(tTargetWorld, 1));
                    }
                }
            }
            if (!tFoundEntity) _mEntities.add(new EntityDef(pEntityToSpawn));
        } finally {
            _mLock.writeLock().unlock();
        }
    }
}

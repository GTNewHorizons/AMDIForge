/*
 * Copyright 2016 Stefan 'Namikon' Thomanek <sthomanek at gmail dot com> This program is free software: you can
 * redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version. This program is distributed in
 * the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details. You should have received a
 * copy of the GNU General Public License along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package eu.usrv.amdiforge.core;

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Lists;

import eu.usrv.amdiforge.AMDIForge;
import eu.usrv.amdiforge.core.graveIO.GraveNBT;
import eu.usrv.yamcore.auxiliary.LogHelper;

public class GraveFileHandler {

    private LogHelper _mLogger = AMDIForge.Logger;
    private String _mConfigFileName;

    private boolean _mInitialized = false;

    public GraveFileHandler() {}

    /**
     * Creates a fake Array of ItemStacks for given LootGroupID This should only execute on the SERVER thread
     * 
     * @param pLootGroupID
     * @return
     */
    public ItemStack[] createFakeInventoryFromTagCompound(NBTTagCompound pGraveTag) {
        ItemStack[] tList = new ItemStack[108];

        if (pGraveTag == null) return tList;

        try {
            GraveNBT tGrave = GraveNBT.getGrave(pGraveTag);
            ItemStack[] tmpList = tGrave.getGraveInventory();
            for (int i = 0; i < tmpList.length; i++) {
                if (tmpList[i] != null) tList[i] = tmpList[i].copy();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // _mLogger.info(String.format("fakeInventory contains %d items", i));
        return tList;
    }

    /**
     * Creates a fake Array of ItemStacks for given LootGroupID This should only execute on the SERVER thread
     * 
     * @param pLootGroupID
     * @return
     */
    public ItemStack[] createFakeInventoryFromGrave(String pGraveFile) {
        ItemStack[] tList = new ItemStack[108];

        if (pGraveFile == "") return tList;

        try {
            GraveNBT tGrave = GraveNBT.getGrave(pGraveFile);
            ItemStack[] tmpList = tGrave.getGraveInventory();
            for (int i = 0; i < tmpList.length; i++) {
                if (tmpList[i] != null) tList[i] = tmpList[i].copy();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // _mLogger.info(String.format("fakeInventory contains %d items", i));
        return tList;
    }

    public static File getSaveFolder(World world) {
        File dummy = world.getSaveHandler().getMapFileFromName("dummy");
        return dummy.getParentFile();
    }

    private static final String PREFIX = "inventory-";

    public List<String> getMatchedDumps(World world, String prefix) {
        File saveFolder = getSaveFolder(world);
        final String actualPrefix = StringUtils.startsWithIgnoreCase(prefix, PREFIX) ? prefix : PREFIX + prefix;
        File[] files = saveFolder.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith(actualPrefix);
            }
        });

        List<String> result = Lists.newArrayList();
        int toCut = PREFIX.length();

        for (File f : files) {
            String name = f.getName();
            result.add(name.substring(toCut, name.length() - 4));
        }

        return result;
    }
}

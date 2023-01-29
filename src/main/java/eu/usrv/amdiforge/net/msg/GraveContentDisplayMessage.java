/*
 * Copyright 2016 Stefan 'Namikon' Thomanek <sthomanek at gmail dot com> This program is free software: you can
 * redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version. This program is distributed in
 * the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details. You should have received a
 * copy of the GNU General Public License along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package eu.usrv.amdiforge.net.msg;

import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import eu.usrv.amdiforge.AMDIForge;
import eu.usrv.amdiforge.GuiHandler;
import eu.usrv.yamcore.network.client.AbstractClientMessageHandler;
import io.netty.buffer.ByteBuf;

public class GraveContentDisplayMessage implements IMessage {

    protected NBTTagCompound _mPayload;

    public GraveContentDisplayMessage() {}

    public GraveContentDisplayMessage(NBTTagCompound pPayload) {
        _mPayload = pPayload;
    }

    @Override
    public void fromBytes(ByteBuf pBuffer) {
        _mPayload = ByteBufUtils.readTag(pBuffer);
    }

    @Override
    public void toBytes(ByteBuf pBuffer) {
        ByteBufUtils.writeTag(pBuffer, _mPayload);
    }

    public static class GraveContentDisplayMessageHandler
            extends AbstractClientMessageHandler<GraveContentDisplayMessage> {

        @Override
        public IMessage handleClientMessage(EntityPlayer pPlayer, GraveContentDisplayMessage pMessage,
                MessageContext pCtx) {
            EntityClientPlayerMP p = (EntityClientPlayerMP) pPlayer;
            GuiHandler.PendingGraveNBT = pMessage._mPayload;
            p.openGui(
                    AMDIForge.instance,
                    GuiHandler.GUI_GRAVEVIEW,
                    p.worldObj,
                    (int) p.posX,
                    (int) p.posY,
                    (int) p.posZ);
            return null;
        }
    }
}

package eu.usrv.amdiforge.net.msg;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import eu.usrv.amdiforge.AMDIForge;
import eu.usrv.amdiforge.GuiHandler;
import eu.usrv.yamcore.network.client.AbstractClientMessageHandler;

public class GraveContentDisplayMessage implements IMessage
{
  protected NBTTagCompound _mPayload;

  public GraveContentDisplayMessage()
  {
  }

  public GraveContentDisplayMessage( NBTTagCompound pPayload )
  {
    _mPayload = pPayload;
  }

  @Override
  public void fromBytes( ByteBuf pBuffer )
  {
    _mPayload = ByteBufUtils.readTag( pBuffer );
  }

  @Override
  public void toBytes( ByteBuf pBuffer )
  {
    ByteBufUtils.writeTag( pBuffer, _mPayload );
  }

  public static class GraveContentDisplayMessageHandler extends AbstractClientMessageHandler<GraveContentDisplayMessage>
  {
    @Override
    public IMessage handleClientMessage( EntityPlayer pPlayer, GraveContentDisplayMessage pMessage, MessageContext pCtx )
    {
      EntityClientPlayerMP p = (EntityClientPlayerMP) pPlayer;
      GuiHandler.PendingGraveNBT = pMessage._mPayload;
      p.openGui( AMDIForge.instance, GuiHandler.GUI_GRAVEVIEW, p.worldObj, (int) p.posX, (int) p.posY, (int) p.posZ );
      return null;
    }
  }
}
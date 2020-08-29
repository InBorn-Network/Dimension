package me.c1oky;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntityLevelChangeEvent;
import cn.nukkit.event.server.DataPacketSendEvent;
import cn.nukkit.level.Level;
import cn.nukkit.math.BlockVector3;
import cn.nukkit.network.protocol.*;
import cn.nukkit.plugin.PluginBase;

public class Dimension extends PluginBase implements Listener {

    @Override
    public void onEnable() {
        Server.getInstance().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onDataPacketSend(DataPacketSendEvent event) {
        DataPacket dataPacket = event.getPacket();
        if (dataPacket instanceof StartGamePacket) {
            Player player = event.getPlayer();
            ((StartGamePacket) dataPacket).dimension = (byte) (player.getLevel().getDimension() & 0xff);
        }
    }

    @EventHandler
    public void onEntityLevelChange(EntityLevelChangeEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Player) {
            Level origin = event.getOrigin();
            Level target = event.getTarget();
            if (target.getDimension() != origin.getDimension()) {
                Player player = (Player) entity;
                ChangeDimensionPacket changeDimensionPacket = new ChangeDimensionPacket();
                changeDimensionPacket.dimension = target.getDimension() & 0xff;
                changeDimensionPacket.x = (float) player.x;
                changeDimensionPacket.y = (float) player.y;
                changeDimensionPacket.z = (float) player.z;
                changeDimensionPacket.respawn = !player.isAlive();
                player.dataPacket(changeDimensionPacket);

                PlayStatusPacket playStatusPacket = new PlayStatusPacket();
                playStatusPacket.status = PlayStatusPacket.PLAYER_SPAWN;
                player.dataPacket(playStatusPacket);

                NetworkChunkPublisherUpdatePacket chunkPublisherUpdatePacket = new NetworkChunkPublisherUpdatePacket();
                chunkPublisherUpdatePacket.position = new BlockVector3((int) player.x, (int) player.y, (int) player.z);
                chunkPublisherUpdatePacket.radius = player.getViewDistance() << 4;
                player.dataPacket(chunkPublisherUpdatePacket);
            }
        }
    }
}
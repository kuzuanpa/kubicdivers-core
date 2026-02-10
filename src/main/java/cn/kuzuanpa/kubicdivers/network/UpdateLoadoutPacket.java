package cn.kuzuanpa.kubicdivers.network;

import cn.kuzuanpa.kubicdivers.KubicDiversMod;
import cn.kuzuanpa.kubicdivers.common.mission.PlayerManager;
import cn.kuzuanpa.kubicdivers.stratagem.common.stratagem.IStratagem;
import cn.kuzuanpa.kubicdivers.stratagem.common.stratagem.StratagemManager;
import cn.kuzuanpa.kubicdivers.stratagem.common.stratagem.types.EmptyStratagem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**It is a packet, but also many place use same structure records. To simplify, we just use this record everywhere.**/
public record UpdateLoadoutPacket(String playerName, ItemStack armor, ItemStack primaryWeapon, ItemStack secondaryWeapon,
                                  ItemStack enhance,
                                  List<IStratagem> stratagems, boolean isReady) {
    public static void encode(UpdateLoadoutPacket msg, FriendlyByteBuf buf) {
        ItemStack air = new ItemStack(Items.AIR,0);//Those dan friendly bufs don't accept null.
        buf.writeUtf(msg.playerName);
        buf.writeItem(msg.armor == null? air : msg.armor);
        buf.writeItem(msg.primaryWeapon == null? air : msg.primaryWeapon);
        buf.writeItem(msg.secondaryWeapon == null? air : msg.secondaryWeapon);
        buf.writeItem(msg.enhance == null? air : msg.enhance);
        buf.writeCollection(msg.stratagems.stream().map(s -> s == null? new EmptyStratagem() : s).map(IStratagem::getId).collect(Collectors.toList()), FriendlyByteBuf::writeUtf);
        buf.writeBoolean(msg.isReady);
    }

    public static UpdateLoadoutPacket decode(FriendlyByteBuf buf) {
        return new UpdateLoadoutPacket(buf.readUtf(),
                buf.readItem(), buf.readItem(), buf.readItem(), buf.readItem(),
                buf.readCollection(ArrayList::new, FriendlyByteBuf::readUtf).stream().map(StratagemManager::getStratagem).collect(Collectors.toList()),
                buf.readBoolean()

        );
    }

    public static void handle(UpdateLoadoutPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            PlayerManager.updateLoadout(msg.playerName, msg);
            ServerPlayer sender = ctx.get().getSender();
            if(sender != null){//Is server side
                KubicDiversMod.NETWORK_CHANNEL.send(PacketDistributor.ALL.noArg(), msg);
                PlayerManager.checkReady(sender);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
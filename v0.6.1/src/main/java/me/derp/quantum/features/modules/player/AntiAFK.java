package me.derp.quantum.features.modules.player;

import me.derp.quantum.features.modules.Module;
import me.derp.quantum.features.setting.Setting;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketClickWindow;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketTabComplete;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

import java.util.Random;
import java.util.UUID;

public
class AntiAFK extends Module {

    private final Random random;
    private final Setting < Boolean > swing = this.register ( new Setting <> ( "Swing" , true ) );
    private final Setting < Boolean > turn = this.register ( new Setting <> ( "Turn" , true ) );
    private final Setting < Boolean > jump = this.register ( new Setting <> ( "Jump" , true ) );
    private final Setting < Boolean > sneak = this.register ( new Setting <> ( "Sneak" , true ) );
    private final Setting < Boolean > interact = this.register ( new Setting <> ( "InteractBlock" , false ) );
    private final Setting < Boolean > tabcomplete = this.register ( new Setting <> ( "TabComplete" , true ) );
    private final Setting < Boolean > msgs = this.register ( new Setting <> ( "ChatMsgs" , true ) );
    private final Setting < Boolean > window = this.register ( new Setting <> ( "WindowClick" , true ) );
    private final Setting < Boolean > swap = this.register ( new Setting <> ( "ItemSwap" , true ) );
    private final Setting < Boolean > dig = this.register ( new Setting <> ( "HitBlock" , true ) );
    private final Setting < Boolean > move = this.register ( new Setting <> ( "Move" , true ) );

    public
    AntiAFK ( ) {
        super ( "AntiAFK" , "Stop servers attempting to kick u for being AFK." , Category.PLAYER , true , false , false );
        random = new Random ( );
    }

    @Override
    public
    void onUpdate ( ) {
        if ( AntiAFK.mc.player.ticksExisted % 45 == 0 && this.swing.getValue ( ) ) {
            AntiAFK.mc.player.swingArm ( EnumHand.MAIN_HAND );
        }
        if ( AntiAFK.mc.player.ticksExisted % 20 == 0 && this.turn.getValue ( ) ) {
            AntiAFK.mc.player.rotationYaw = (float) ( this.random.nextInt ( 360 ) - 180 );
        }
        if ( AntiAFK.mc.player.ticksExisted % 60 == 0 && this.jump.getValue ( ) && AntiAFK.mc.player.onGround ) {
            AntiAFK.mc.player.jump ( );
        }
        if ( AntiAFK.mc.player.ticksExisted % 50 == 0 && this.sneak.getValue ( ) && ! AntiAFK.mc.player.isSneaking ( ) ) {
            AntiAFK.mc.player.setSneaking ( true );
            AntiAFK.mc.player.setSneaking ( false );
        }
        if ( AntiAFK.mc.player.ticksExisted % 30 == 0 && this.interact.getValue ( ) ) {
            final BlockPos blockPos = AntiAFK.mc.objectMouseOver.getBlockPos ( );
            if ( ! AntiAFK.mc.world.isAirBlock ( blockPos ) ) {
                AntiAFK.mc.playerController.clickBlock ( blockPos , AntiAFK.mc.objectMouseOver.sideHit );
            }
        }
        if ( AntiAFK.mc.player.ticksExisted % 80 == 0 && this.tabcomplete.getValue ( ) && ! AntiAFK.mc.player.isDead ) {
            AntiAFK.mc.player.connection.sendPacket ( new CPacketTabComplete ( "/" + UUID.randomUUID ( ).toString ( ).replace ( '-' , 'v' ) , AntiAFK.mc.player.getPosition ( ) , false ) );
        }
        if ( AntiAFK.mc.player.ticksExisted % 200 == 0 && this.msgs.getValue ( ) && ! AntiAFK.mc.player.isDead ) {
            AntiAFK.mc.player.sendChatMessage ( "quantum continued owns me an all: https://discord.gg/vf6nxStNWk " + random.nextInt ( ) );
        }
        if ( AntiAFK.mc.player.ticksExisted % 125 == 0 && this.window.getValue ( ) && ! AntiAFK.mc.player.isDead ) {
            AntiAFK.mc.player.connection.sendPacket ( new CPacketClickWindow ( 1 , 1 , 1 , ClickType.CLONE , new ItemStack ( Blocks.OBSIDIAN ) , (short) 1 ) );
        }
        if ( AntiAFK.mc.player.ticksExisted % 70 == 0 && this.swap.getValue ( ) && ! AntiAFK.mc.player.isDead ) {
            AntiAFK.mc.player.connection.sendPacket ( new CPacketPlayerDigging ( CPacketPlayerDigging.Action.SWAP_HELD_ITEMS , AntiAFK.mc.player.getPosition ( ) , EnumFacing.DOWN ) );
        }
        if ( AntiAFK.mc.player.ticksExisted % 50 == 0 && this.dig.getValue ( ) ) {
            AntiAFK.mc.player.connection.sendPacket ( new CPacketPlayerDigging ( CPacketPlayerDigging.Action.START_DESTROY_BLOCK , AntiAFK.mc.player.getPosition ( ) , EnumFacing.DOWN ) );
        }
        if ( AntiAFK.mc.player.ticksExisted % 150 == 0 && this.move.getValue ( ) ) {
            mc.gameSettings.keyBindForward.pressed = mc.player.ticksExisted % 10 == 0;
            mc.gameSettings.keyBindBack.pressed = mc.player.ticksExisted % 15 == 0;
            mc.gameSettings.keyBindLeft.pressed = mc.player.ticksExisted % 20 == 0;
            mc.gameSettings.keyBindRight.pressed = mc.player.ticksExisted % 25 == 0;
        }
    }
}
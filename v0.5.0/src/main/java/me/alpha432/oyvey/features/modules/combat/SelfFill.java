package me.alpha432.oyvey.features.modules.combat;

import me.alpha432.oyvey.event.events.UpdateWalkingPlayerEvent;
import me.alpha432.oyvey.features.command.Command;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Setting;
import me.alpha432.oyvey.util.BlockUtil;
import me.alpha432.oyvey.util.InventoryUtil;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public
class SelfFill
        extends Module {
    private static SelfFill INSTANCE;

    private final Setting < Mode > mode = this.register ( new Setting <> ( "Mode" , Mode.OBSIDIAN ) );
    private final Setting < Boolean > smartTp = this.register ( new Setting <> ( "SmartTP" , false ) );
    private final Setting < Integer > tpMin = this.register ( new Setting <> ( "TPMin" , 2 , 2 , 10 , v -> this.smartTp.getValue ( ) ) );
    private final Setting < Integer > tpMax = this.register ( new Setting <> ( "TPMax" , 25 , 5 , 40 , v -> this.smartTp.getValue ( ) ) );
    private final Setting < Boolean > noVoid = this.register ( new Setting <> ( "NoVoid" , true , v -> this.smartTp.getValue ( ) ) );
    private final Setting < Integer > tpHeight = this.register ( new Setting <> ( "TPHeight" , 2 , - 100 , 100 , v -> ! this.smartTp.getValue ( ) ) );
    private final Setting < Boolean > keepInside = this.register ( new Setting <> ( "Center" , true ) );
    private final Setting < Boolean > rotate = this.register ( new Setting <> ( "Rotate" , false ) );
    private final Setting < Boolean > sneaking = this.register ( new Setting <> ( "Sneak" , false ) );
    private final Setting < Boolean > offground = this.register ( new Setting <> ( "Offground" , false ) );
    private final Setting < Boolean > chat = this.register ( new Setting <> ( "Chat Msgs" , true ) );
    private final Setting < Boolean > tpdebug = this.register ( new Setting <> ( "Debug" , false , v -> this.chat.getValue ( ) && this.smartTp.getValue ( ) ) );
    private BlockPos burrowPos;
    private int lastBlock;
    private int blockSlot;

    public
    SelfFill ( ) {
        super ( "SelfFill" , ":face_vomiting:" , Module.Category.COMBAT , true , false , false );
        INSTANCE = this;
    }

    public static
    SelfFill getInstance ( ) {
        if ( INSTANCE == null )
            INSTANCE = new SelfFill ( );
        return INSTANCE;
    }

    @Override
    public
    void onEnable ( ) {
        this.burrowPos = new BlockPos ( SelfFill.mc.player.posX , Math.ceil ( SelfFill.mc.player.posY ) , SelfFill.mc.player.posZ );
        this.blockSlot = this.findBlockSlot ( );
        this.lastBlock = SelfFill.mc.player.inventory.currentItem;
        if ( ! doChecks ( ) || this.blockSlot == - 1 ) {
            this.disable ( );
            return;
        }
        if ( this.keepInside.getValue ( ) ) {
            double x = SelfFill.mc.player.posX - Math.floor ( SelfFill.mc.player.posX );
            double z = SelfFill.mc.player.posZ - Math.floor ( SelfFill.mc.player.posZ );
            if ( x <= 0.3 || x >= 0.7 ) {
                x = ( x > 0.5 ? 0.69 : 0.31 );
            }
            if ( z < 0.3 || z > 0.7 ) {
                z = ( z > 0.5 ? 0.69 : 0.31 );
            }
            SelfFill.mc.player.connection.sendPacket ( new CPacketPlayer.Position ( Math.floor ( SelfFill.mc.player.posX ) + x , SelfFill.mc.player.posY , Math.floor ( SelfFill.mc.player.posZ ) + z , SelfFill.mc.player.onGround ) );
            SelfFill.mc.player.setPosition ( Math.floor ( SelfFill.mc.player.posX ) + x , SelfFill.mc.player.posY , Math.floor ( SelfFill.mc.player.posZ ) + z );
            // no fucking clue how this worked i made it drunk
        }
        SelfFill.mc.player.connection.sendPacket ( new CPacketPlayer.Position ( SelfFill.mc.player.posX , SelfFill.mc.player.posY + 0.41999998688698D , SelfFill.mc.player.posZ , ! this.offground.getValue ( ) ) );
        SelfFill.mc.player.connection.sendPacket ( new CPacketPlayer.Position ( SelfFill.mc.player.posX , SelfFill.mc.player.posY + 0.7531999805211997D , SelfFill.mc.player.posZ , ! this.offground.getValue ( ) ) );
        SelfFill.mc.player.connection.sendPacket ( new CPacketPlayer.Position ( SelfFill.mc.player.posX , SelfFill.mc.player.posY + 1.00133597911214D , SelfFill.mc.player.posZ , ! this.offground.getValue ( ) ) );
        SelfFill.mc.player.connection.sendPacket ( new CPacketPlayer.Position ( SelfFill.mc.player.posX , SelfFill.mc.player.posY + 1.16610926093821D , SelfFill.mc.player.posZ , ! this.offground.getValue ( ) ) );
    }

    @SubscribeEvent
    public
    void onUpdateWalkingPlayer ( UpdateWalkingPlayerEvent event ) {
        if ( event.getStage ( ) != 0 ) return;
        if ( this.sneaking.getValue ( ) && ! SelfFill.mc.player.isSneaking ( ) ) {
            SelfFill.mc.player.connection.sendPacket ( new CPacketEntityAction ( SelfFill.mc.player , CPacketEntityAction.Action.START_SNEAKING ) );
        }
        InventoryUtil.switchToHotbarSlot ( this.blockSlot , false );
        BlockUtil.placeBlock ( this.burrowPos , this.blockSlot == - 2 ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND , this.rotate.getValue ( ) , true , this.sneaking.getValue ( ) );
        InventoryUtil.switchToHotbarSlot ( this.lastBlock , false );
        SelfFill.mc.player.connection.sendPacket ( new CPacketPlayer.Position ( SelfFill.mc.player.posX , ( this.smartTp.getValue ( ) ? this.adaptiveTpHeight ( false ) : this.tpHeight.getValue ( ) + SelfFill.mc.player.posY ) , SelfFill.mc.player.posZ , ! this.offground.getValue ( ) ) );
        SelfFill.mc.player.connection.sendPacket ( new CPacketEntityAction ( SelfFill.mc.player , CPacketEntityAction.Action.STOP_SNEAKING ) );
        this.disable ( );
    }

    private
    int findBlockSlot ( ) {
        if ( this.mode.getValue ( ) != Mode.FALLING ) {
            Class block;
            String name;
            if ( this.mode.getValue ( ) == Mode.ECHEST ) {
                block = BlockEnderChest.class;
                name = "Ender Chests";
            } else {
                block = BlockObsidian.class;
                name = "Obsidian";
            }
            int slot = InventoryUtil.findHotbarBlock ( block );
            if ( slot == - 1 )  {
                if ( InventoryUtil.isBlock ( SelfFill.mc.player.getHeldItemOffhand ( ).getItem ( ) , block ) ) {
                    return - 2;
                } else {
                    if ( this.chat.getValue ( ) )
                        Command.sendMessage ( "<" + this.getDisplayName ( ) + "> " + "\u00a7c" + "No " + name + " to use." );
                }
            }
            return slot;
        } else { // gravity block mode
            for ( int i = 0; i < 9; i++ ) {
                ItemStack item = SelfFill.mc.player.inventory.getStackInSlot ( i );
                if ( ! ( item.getItem ( ) instanceof ItemBlock ) )
                    continue;
                Block block = Block.getBlockFromItem ( SelfFill.mc.player.inventory.getStackInSlot ( i ).getItem ( ) );
                if ( block instanceof BlockFalling )
                    return i;
            } // really don't think anyone's gonna fucking offhand sand or anvils so im not adding that
            if ( this.chat.getValue ( ) )
                Command.sendMessage ( "<" + this.getDisplayName ( ) + "> " + "\u00a7c" + "No Gravity Blocks to use." );
            return - 1;
        }
    }



    private
    int adaptiveTpHeight ( boolean first ) {
        int max = ( SelfFill.mc.player.dimension == - 1 && this.noVoid.getValue ( ) && this.tpMax.getValue ( ) + this.burrowPos.getY ( ) > 127 ? Math.abs ( this.burrowPos.getY ( ) - 127 ) : this.tpMax.getValue ( ) );
        int airblock = ( this.noVoid.getValue ( ) && this.tpMax.getValue ( ) * - 1 + this.burrowPos.getY ( ) < 0 ? this.burrowPos.getY ( ) * - 1 : this.tpMax.getValue ( ) * - 1 );
        while ( airblock < max ) {
            if ( Math.abs ( airblock ) < this.tpMin.getValue ( ) || ! SelfFill.mc.world.isAirBlock ( this.burrowPos.offset ( EnumFacing.UP , airblock ) ) || ! SelfFill.mc.world.isAirBlock ( this.burrowPos.offset ( EnumFacing.UP , airblock + 1 ) ) ) {
                airblock++;
            } else {
                if ( this.tpdebug.getValue ( ) && this.chat.getValue ( ) && ! first )
                    Command.sendMessage ( Integer.toString ( airblock ) );
                return this.burrowPos.getY ( ) + airblock;
            }
        }
        return 69420; // if there isn't any room
    }

    private
    boolean
    doChecks ( ) {
        if ( SelfFill.fullNullCheck ( ) ) return false;
        if ( BlockUtil.isPositionPlaceable ( this.burrowPos , false , false ) < 1 ) return false;
        if ( this.smartTp.getValue ( ) && this.adaptiveTpHeight ( true ) == 69420 ) {
            if ( this.chat.getValue ( ) )
                Command.sendMessage ( "<" + this.getDisplayName ( ) + "> " + "\u00a7c" + "Not enough room to rubberband." );
            return false;
        }
        if ( ! SelfFill.mc.world.isAirBlock ( this.burrowPos.offset( EnumFacing.UP , 2 ) ) ) {
            if ( this.chat.getValue ( ) )
                Command.sendMessage ( "<" + this.getDisplayName ( ) + "> " + "\u00a7c" + "Not enough room to jump." );
            return false;
        }
        for ( Entity entity : BlockUtil.mc.world.getEntitiesWithinAABB ( Entity.class , new AxisAlignedBB ( burrowPos ) ) ) {
            if ( entity instanceof EntityItem || entity instanceof EntityXPOrb || entity instanceof EntityArrow || entity instanceof EntityPlayer ) continue;
            return false;
        }
        return true;
    }

    private
    enum Mode {
        OBSIDIAN,
        ECHEST,
        FALLING
    }
}

package me.derp.quantum.features.modules.combat;

import me.derp.quantum.Quantum;
import me.derp.quantum.event.events.*;
import me.derp.quantum.features.command.Command;
import me.derp.quantum.features.modules.Module;
import me.derp.quantum.features.modules.misc.AutoGG;
import me.derp.quantum.features.setting.Setting;
import me.derp.quantum.util.Timer;
import me.derp.quantum.util.*;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.SPacketEntityStatus;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.network.play.server.SPacketSpawnObject;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.Explosion;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import me.derp.quantum.util.Multithread;

/* credits list bc why not
* oyvey : original autocrystla
* cosmos : yawstep
* bleachhack+ : sequential
*
* ozark : city predict
*/
public class TooBeeCrystalAura
        extends Module {
    private final Timer placeTimer = new Timer();
    private final Timer breakTimer = new Timer();
    private final Timer preditTimer = new Timer();
    private final Timer manualTimer = new Timer();
    private final Setting<Integer> attackFactor = this.register(new Setting<>("PredictDelay", 0, 0, 200));
    public Setting<Boolean> place = this.register(new Setting<>("Place", true));
    public Setting<Integer> placeDelay = this.register(new Setting<>("PlaceDelay", 0, 0, 500));
    public Setting<Float> placeRange = this.register(new Setting<>("PlaceRange", 4.0f, 0.1f, 7.0f));
    public Setting<Boolean> explode = this.register(new Setting<>("Break", true));
    public Setting<Boolean> packetBreak = this.register(new Setting<>("PacketBreak", true));
    public Setting<Boolean> sequential = this.register(new Setting<>("Sequential", true));
    public Setting<Boolean> predicts = this.register(new Setting<>("Predict", true));
    public Setting<Integer> amountOfThreads = this.register(new Setting<>("Threads", 1, 1, 10));
    public Setting<Boolean> rotate = this.register(new Setting<>("Rotate", true));
    public Setting<Boolean> yawStep = this.register(new Setting<>("YawStep", true, v->rotate.getValue()));
    public Setting<Integer> yawSteps = this.register(new Setting<>("Step", 7, 0, 32, v->rotate.getValue() && yawStep.getValue()));
    public Setting<Sync> sync = this.register(new Setting<>("Sync", Sync.Sound));
    public Setting<Boolean> cityPredict = this.register(new Setting<>("CityPredict", true));
    public Setting<Integer> breakDelay = this.register(new Setting<>("BreakDelay", 0, 0, 500));
    public Setting<Float> breakRange = this.register(new Setting<>("BreakRange", 4.0f, 0.1f, 7.0f));
    public Setting<Float> breakWallRange = this.register(new Setting<>("BreakWallRange", 4.0f, 0.1f, 7.0f));
    public Setting<Boolean> opPlace = this.register(new Setting<>("1.13 Place", true));
    Setting<InventoryUtil.Switch> switchMode = this.register(new Setting<>("Switch", InventoryUtil.Switch.NONE));
    public Setting<Boolean> suicide = this.register(new Setting<>("AntiSuicide", true));
    public Setting<Boolean> ignoreUseAmount = this.register(new Setting<>("IgnoreUseAmount", true));
    public Setting<Integer> wasteAmount = this.register(new Setting<>("UseAmount", 4, 1, 5));
    public Setting<Boolean> facePlaceSword = this.register(new Setting<>("FacePlaceSword", true));
    public Setting<Float> targetRange = this.register(new Setting<>("TargetRange", 4.0f, 1.0f, 12.0f));
    public Setting<Float> minDamage = this.register(new Setting<>("MinDamage", 4.0f, 0.1f, 20.0f));
    public Setting<Float> facePlace = this.register(new Setting<>("FacePlaceHP", 4.0f, 0.0f, 36.0f));
    public Setting<Float> breakMaxSelfDamage = this.register(new Setting<>("BreakMaxSelf", 4.0f, 0.1f, 12.0f));
    public Setting<Float> breakMinDmg = this.register(new Setting<>("BreakMinDmg", 4.0f, 0.1f, 7.0f));
    public Setting<Float> minArmor = this.register(new Setting<>("MinArmor", 4.0f, 0.1f, 80.0f));
    public Setting<SwingMode> swingMode = this.register(new Setting<>("Swing", SwingMode.MainHand));
    public Setting<Boolean> render = this.register(new Setting<>("Render", true));
    public Setting<Boolean> renderDmg = this.register(new Setting<>("RenderDmg", true));
    Setting<Boolean> fastPop = this.register(new Setting<>("FastPop", false));
    EntityEnderCrystal crystal;
    private final Map<EntityPlayer, Timer> totemPops = new ConcurrentHashMap<>();
    private EntityLivingBase target;
    private BlockPos pos;
    private int hotBarSlot;
    Multithread multiThread = new Multithread();
    private boolean armor;
    private boolean armorTarget;
    private int crystalCount;
    private int predictWait;
    private int predictPackets;
    private boolean packetCalc;
    private float yaw = 0.0f;
    private EntityLivingBase realTarget;
    private int predict;
    private float pitch = 0.0f;
    private boolean rotating = false;

    public TooBeeCrystalAura() {
        super("AutoCrystal2", "lol", Category.COMBAT, true, false, false);
    }

    public static List<BlockPos> getSphere(BlockPos loc, float r, int h, boolean hollow, boolean sphere, int plus_y) {
        ArrayList<BlockPos> circleblocks = new ArrayList<BlockPos>();
        int cx = loc.getX();
        int cy = loc.getY();
        int cz = loc.getZ();
        int x = cx - (int) r;
        while ((float) x <= (float) cx + r) {
            int z = cz - (int) r;
            while ((float) z <= (float) cz + r) {
                int y = sphere ? cy - (int) r : cy;
                while (true) {
                    float f;
                    f = sphere ? (float) cy + r : (float) (cy + h);
                    if (!((float) y < f)) break;
                    double dist = (cx - x) * (cx - x) + (cz - z) * (cz - z) + (sphere ? (cy - y) * (cy - y) : 0);
                    if (!(!(dist < (double) (r * r)) || hollow && dist < (double) ((r - 1.0f) * (r - 1.0f)))) {
                        BlockPos l = new BlockPos(x, y + plus_y, z);
                        circleblocks.add(l);
                    }
                    ++y;
                }
                ++z;
            }
            ++x;
        }
        return circleblocks;
    }
    @Override
    public void onEnable() {
        this.placeTimer.reset();
        this.breakTimer.reset();
        this.predictWait = 0;
        this.hotBarSlot = -1;
        this.pos = null;
        this.crystal = null;
        this.predict = 0;
        this.predictPackets = 1;
        this.target = null;
        this.packetCalc = false;
        this.realTarget = null;
        this.armor = false;
        this.armorTarget = false;
        totemPops.clear();
    }

    @Override
    public void onDisable() {
        this.rotating = false;
        totemPops.clear();
    }
    @SubscribeEvent
    public void onBlockEvent(final BlockEvent event) {
        if (cityPredict.getValue() && getTarget() != null) {
            if (event.pos == EntityUtil.is_cityable(getTarget(), opPlace.getValue()))      placeCrystalOnBlock(event.pos.down(), TooBeeCrystalAura.mc.player.getHeldItemOffhand().getItem() == Items.END_CRYSTAL ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, false, false, switchMode.getValue() == InventoryUtil.Switch.SILENT);
        }
    }
    /*
    @SubscribeEvent
    public void onPlayerWalkingUpdated(UpdateWalkingPlayerEvent event) {
        if (event.getStage() == 0) {
            if (rotate.getValue()) onCrystal();
            if (rotate.getValue() && this.crystal != null) {
                float[] angle = calculateAngles(this.crystal.getPositionVector());
                for (int i = 0; i <= yawSteps.getValue(); i++) {
                    Quantum.rotationManagerNew.setYaw(angle[0] / i);
                    Quantum.rotationManagerNew.setPitch(angle[1] / i);
                }
            }
        }
    }
     */

    private boolean isDoublePopable(EntityPlayer player, float damage) {
        double health = player.getHealth();
        if (fastPop.getValue() && health <= 1.0 && damage > health + 0.5 && damage <= 4.0) {
            Timer timer = totemPops.get(player);
            return timer == null || timer.passed((long)500.0);
        }
        return false;
    }
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void listenSentPackets(PacketEvent.Send event) {
        if (event.getPacket() instanceof CPacketPlayer && rotate.getValue() && yawStep.getValue() && this.pos != null) {
            // split up the rotation into multiple packets, NCP flags for quick rotations
            for (float step = yawSteps.getValue(); step > 0; step--) {
                Quantum.rotationManager.setYaw(this.yaw / step);
                Quantum.rotationManager.setPitch(this.pitch / step);
            }
        }

        if (event.getPacket() instanceof CPacketUseEntity && ((CPacketUseEntity) event.getPacket()).getAction() == CPacketUseEntity.Action.ATTACK && ((CPacketUseEntity) event.getPacket()).getEntityFromWorld(mc.world) instanceof EntityEnderCrystal) {
            if (sync.getValue() == Sync.Attack) Objects.requireNonNull(((CPacketUseEntity) event.getPacket()).getEntityFromWorld(mc.world)).setDead();
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (fullNullCheck()) return;
        if (!rotate.getValue() && event.phase == TickEvent.Phase.START) {
            pos = calculatePosition();
            crystal = calculateCrystal();
                for (int i = amountOfThreads.getValue(); i >= 1; i--) {
                    mc.addScheduledTask(() -> {
                        Thread thr = new Thread(this::onCrystal);
                        thr.setDaemon(true);
                        thr.start();
                    });
                }
        }
    }

    @Override
    public String getDisplayInfo() {
        if (this.realTarget != null) {
            return this.realTarget.getName();
        }
        return null;
    }

    public void onCrystal() {
        if (TooBeeCrystalAura.mc.world == null || TooBeeCrystalAura.mc.player == null) {
            return;
        }
        this.realTarget = null;
        this.manualBreaker();
        this.crystalCount = 0;
        if (!this.ignoreUseAmount.getValue()) {
            for (Entity crystal : TooBeeCrystalAura.mc.world.loadedEntityList) {
                if (!(crystal instanceof EntityEnderCrystal) || !this.IsValidCrystal(crystal)) continue;
                boolean count = false;
                double damage = this.calculateDamage((double) this.target.getPosition().getX() + 0.5, (double) this.target.getPosition().getY() + 1.0, (double) this.target.getPosition().getZ() + 0.5, this.target);
                if (damage >= (double) this.minDamage.getValue()) {
                    count = true;
                }
                if (!count) continue;
                ++this.crystalCount;
            }
        }
        this.hotBarSlot = -1;
        if (TooBeeCrystalAura.mc.player.getHeldItemOffhand().getItem() != Items.END_CRYSTAL) {
            int crystalSlot;
            crystalSlot = TooBeeCrystalAura.mc.player.getHeldItemMainhand().getItem() == Items.END_CRYSTAL ? TooBeeCrystalAura.mc.player.inventory.currentItem : -1;
            if (crystalSlot == -1) {
                for (int l = 0; l < 9; ++l) {
                    if (TooBeeCrystalAura.mc.player.inventory.getStackInSlot(l).getItem() != Items.END_CRYSTAL)
                        continue;
                    crystalSlot = l;
                    this.hotBarSlot = l;
                    break;
                }
            }
            if (crystalSlot == -1) {
                this.pos = null;
                this.target = null;
                return;
            }
        }
        if (TooBeeCrystalAura.mc.player.getHeldItemOffhand().getItem() == Items.GOLDEN_APPLE && TooBeeCrystalAura.mc.player.getHeldItemMainhand().getItem() != Items.END_CRYSTAL) {
            this.pos = null;
            this.target = null;
            return;
        }
        if (this.target == null) {
            this.target = this.getTarget();
        }
        if (this.target == null) {
            this.crystal = null;
            return;
        }
        if (this.target.getDistance(TooBeeCrystalAura.mc.player) > 12.0f) {
            this.crystal = null;
            this.target = null;
        }
        if (this.crystal != null && this.explode.getValue() && this.breakTimer.passedMs(this.breakDelay.getValue())) {
            this.breakTimer.reset();
            if (this.packetBreak.getValue()) {
                new Thread(() -> {
                    TooBeeCrystalAura.mc.player.connection.sendPacket(new CPacketUseEntity(this.crystal));
                }).start();
                if (sync.getValue() == Sync.Instant) this.crystal.setDead();
                if (sequential.getValue()) {
                    BlockPos crystalPos = new BlockPos(Math.floor(this.crystal.posX), Math.floor(this.crystal.posY - 1), Math.floor(this.crystal.posZ));
                    new Thread(() -> {
                        placeCrystalOnBlock(crystalPos, TooBeeCrystalAura.mc.player.getHeldItemOffhand().getItem() == Items.END_CRYSTAL ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, false, false, switchMode.getValue() == InventoryUtil.Switch.SILENT);
                    }).start();
                }
            } else {
                new Thread(() -> {
                    TooBeeCrystalAura.mc.playerController.attackEntity(TooBeeCrystalAura.mc.player, this.crystal);
                }).start();
                if (sync.getValue() == Sync.Instant) this.crystal.setDead();
                if (sequential.getValue()) {
                    BlockPos crystalPos = new BlockPos(Math.floor(this.crystal.posX), Math.floor(this.crystal.posY - 1), Math.floor(this.crystal.posZ));
                    new Thread(() -> {
                        placeCrystalOnBlock(crystalPos, TooBeeCrystalAura.mc.player.getHeldItemOffhand().getItem() == Items.END_CRYSTAL ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, false, false, switchMode.getValue() == InventoryUtil.Switch.SILENT);
                    }).start();
                }
            }
            if (this.swingMode.getValue() == SwingMode.MainHand) {
                TooBeeCrystalAura.mc.player.swingArm(EnumHand.MAIN_HAND);
            } else if (this.swingMode.getValue() == SwingMode.OffHand) {
                TooBeeCrystalAura.mc.player.swingArm(EnumHand.OFF_HAND);
            }

        }


        if (this.placeTimer.passedMs(this.placeDelay.getValue().longValue()) && this.place.getValue() && this.pos != null) {
            this.placeTimer.reset();
            if (!this.ignoreUseAmount.getValue()) {
                int crystalLimit = this.wasteAmount.getValue();
                if (this.crystalCount >= crystalLimit) {
                    return;
                }
                if (this.crystalCount < crystalLimit && this.pos != null) {
                    new Thread(() -> {
                        placeCrystalOnBlock(this.pos, TooBeeCrystalAura.mc.player.getHeldItemOffhand().getItem() == Items.END_CRYSTAL ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, false, false, switchMode.getValue() == InventoryUtil.Switch.SILENT);
                    }).start();
                }
            } else if (this.pos != null) {
                new Thread(() -> {
                    placeCrystalOnBlock(this.pos, TooBeeCrystalAura.mc.player.getHeldItemOffhand().getItem() == Items.END_CRYSTAL ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, false, false, switchMode.getValue() == InventoryUtil.Switch.SILENT);
                }).start();
            }
        }
    }
    public EntityEnderCrystal calculateCrystal() {
        EntityEnderCrystal idealCrystal = mc.world.getLoadedEntityList().stream()
                .filter(entity -> entity instanceof EntityEnderCrystal)
                .filter(entity -> mc.player.canEntityBeSeen(entity))
                .map(entity -> (EntityEnderCrystal) entity)
                .min(Comparator.comparing(c -> mc.player.getDistance(c) < breakRange.getValue()))
                .orElse(null);
        return idealCrystal;
    }
    public static
    void placeCrystalOnBlock ( BlockPos pos , EnumHand hand , boolean swing , boolean exactHand , boolean silent ) {
        RayTraceResult result = BlockUtil.mc.world.rayTraceBlocks ( new Vec3d ( BlockUtil.mc.player.posX , BlockUtil.mc.player.posY + (double) BlockUtil.mc.player.getEyeHeight ( ) , BlockUtil.mc.player.posZ ) , new Vec3d ( (double) pos.getX ( ) + 0.5 , (double) pos.getY ( ) - 0.5 , (double) pos.getZ ( ) + 0.5 ) );
        EnumFacing facing = result == null || result.sideHit == null ? EnumFacing.UP : result.sideHit;
        int old = BlockUtil.mc.player.inventory.currentItem;
        int crystal = InventoryUtil.getItemHotbar ( Items.END_CRYSTAL );
        if ( hand == EnumHand.MAIN_HAND && silent && crystal != - 1 && crystal != BlockUtil.mc.player.inventory.currentItem )
            BlockUtil.mc.player.connection.sendPacket ( new CPacketHeldItemChange ( crystal ) );
        BlockUtil.mc.player.connection.sendPacket ( new CPacketPlayerTryUseItemOnBlock ( pos , facing , hand , 0.0f , 0.0f , 0.0f ) );
        if ( hand == EnumHand.MAIN_HAND && silent && crystal != - 1 && crystal != BlockUtil.mc.player.inventory.currentItem )
            BlockUtil.mc.player.connection.sendPacket ( new CPacketHeldItemChange( old ) );
        if ( swing )
            BlockUtil.mc.player.connection.sendPacket ( new CPacketAnimation( exactHand ? hand : EnumHand.MAIN_HAND ) );
    }
    /*boolean rayTracePlaceCheck(final BlockPos pos, final boolean shouldCheck, final float height) {
        return !shouldCheck || mc.world.rayTraceBlocks(new Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ), new Vec3d(pos.getX(), pos.getY() + height, pos.getZ()), false, true, false) == null && (calculateAngles(new Vec3d(pos.getX(), pos.getY() + height, pos.getZ()))[0] <= 90 || calculateAngles(new Vec3d(pos.getX(), pos.getY() + height, pos.getZ()))[1] <= 90);
    }*/

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public void onPacketReceive(PacketEvent.Receive event) {
        SPacketSpawnObject packet;
        if (event.getPacket() instanceof SPacketSpawnObject && (packet = event.getPacket()).getType() == 51 && this.predicts.getValue() && this.preditTimer.passedMs(this.attackFactor.getValue().longValue()) && this.predicts.getValue() && this.explode.getValue() && this.packetBreak.getValue() && this.target != null) {
            CPacketUseEntity predict = new CPacketUseEntity();
            predict.entityId = packet.getEntityID();
            predict.action = CPacketUseEntity.Action.ATTACK;
            TooBeeCrystalAura.mc.player.connection.sendPacket(predict);
        }

        if (event.getPacket() instanceof SPacketSoundEffect) {
            if (((SPacketSoundEffect) event.getPacket()).getCategory() == SoundCategory.BLOCKS && ((SPacketSoundEffect) event.getPacket()).getSound() == SoundEvents.ENTITY_GENERIC_EXPLODE) {
                for (Entity crystal : mc.world.loadedEntityList) {
                    if (crystal instanceof EntityEnderCrystal)
                        if (crystal.getDistance(((SPacketSoundEffect) event.getPacket()).getX(), ((SPacketSoundEffect) event.getPacket()).getY(), ((SPacketSoundEffect) event.getPacket()).getZ()) <= 6) {
                            if (sync.getValue() == Sync.Sound)
                                crystal.setDead();
                        }
                }
            }
        }
        if (event.getPacket() instanceof SPacketEntityStatus) {
            SPacketEntityStatus packet5 = event.getPacket();
            if (packet5.getOpCode() == 35 && packet5.getEntity(AutoCrystal.mc.world) instanceof EntityPlayer) {
                this.totemPops.put((EntityPlayer) packet5.getEntity(AutoCrystal.mc.world), new me.derp.quantum.util.Timer().reset());
            }
        }
    }
    public BlockPos calculatePosition() {
        double damage = 0.5;
        for (BlockPos blockPos : this.placePostions(this.placeRange.getValue())) {
            double selfDmg;
            double targetRange;
            if (blockPos == null || this.target == null || !TooBeeCrystalAura.mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(blockPos)).isEmpty() || (targetRange = this.target.getDistance(blockPos.getX(), blockPos.getY(), blockPos.getZ())) > (double) this.targetRange.getValue() || this.target.isDead || this.target.getHealth() + this.target.getAbsorptionAmount() <= 0.0f)
                continue;
            double targetDmg = this.calculateDamage((double) blockPos.getX() + 0.5, (double) blockPos.getY() + 1.0, (double) blockPos.getZ() + 0.5, this.target);
            if (isDoublePopable((EntityPlayer) this.target, (float) targetDmg)) {
                this.pos = blockPos;
                damage = targetDmg;
            }
            this.armor = false;
            for (ItemStack is : this.target.getArmorInventoryList()) {
                float green = ((float) is.getMaxDamage() - (float) is.getItemDamage()) / (float) is.getMaxDamage();
                float red = 1.0f - green;
                int dmg = 100 - (int) (red * 100.0f);
                if (!((float) dmg <= this.minArmor.getValue())) continue;
                this.armor = true;
            }
            if (targetDmg < (double) this.minDamage.getValue() && (this.facePlaceSword.getValue() != false ? this.target.getAbsorptionAmount() + this.target.getHealth() > this.facePlace.getValue() : TooBeeCrystalAura.mc.player.getHeldItemMainhand().getItem() instanceof ItemSword || this.target.getAbsorptionAmount() + this.target.getHealth() > this.facePlace.getValue()) && (this.facePlaceSword.getValue() ? !this.armor : TooBeeCrystalAura.mc.player.getHeldItemMainhand().getItem() instanceof ItemSword || !this.armor) || (selfDmg = this.calculateDamage((double) blockPos.getX() + 0.5, (double) blockPos.getY() + 1.0, (double) blockPos.getZ() + 0.5, TooBeeCrystalAura.mc.player)) >= (double) (TooBeeCrystalAura.mc.player.getHealth() + TooBeeCrystalAura.mc.player.getAbsorptionAmount()) && selfDmg >= targetDmg && targetDmg < (double) (this.target.getHealth() + this.target.getAbsorptionAmount()) || !(damage < targetDmg))
                continue;
            this.pos = blockPos;
            damage = targetDmg;
        }
        if (damage == 0.5) {
            this.pos = null;
            this.target = null;
            this.realTarget = null;
        }
        return this.pos;

    }
    @Override
    public void onRender3D(Render3DEvent event) {
        if (this.pos != null && this.render.getValue().booleanValue() && this.target != null) {
            AxisAlignedBB bb = mc.world.getBlockState(pos).getSelectedBoundingBox(mc.world, pos);
            Vec3d interp = EntityUtil.interpolateEntity(RenderUtil.mc.player, mc.getRenderPartialTicks());
            for (EnumFacing face : EnumFacing.values()) {
                RenderUtil.drawGradientPlaneBB(bb.grow(0.002f).offset(-interp.x, -interp.y, -interp.z), face, new Color(ColorUtil.rainbow(50).getRed(), ColorUtil.rainbow(50).getGreen(), ColorUtil.rainbow(50).getBlue(), 127), ColorUtil.invert(new Color(ColorUtil.rainbow(50).getRed(), ColorUtil.rainbow(50).getGreen(), ColorUtil.rainbow(50).getBlue(), 127)), 2f);
            }
            RenderUtil.drawGradientBlockOutline(bb.grow(0.002f).offset(-interp.x, -interp.y, -interp.z), ColorUtil.invert(new Color(ColorUtil.rainbow(50).getRed(), ColorUtil.rainbow(50).getGreen(), ColorUtil.rainbow(50).getBlue(), 255)), new Color(ColorUtil.rainbow(50).getRed(), ColorUtil.rainbow(50).getGreen(), ColorUtil.rainbow(50).getBlue(), 255), 2f);
            if (this.renderDmg.getValue().booleanValue()) {
                double renderDamage = this.calculateDamage((double) this.pos.getX() + 0.5, (double) this.pos.getY() + 1.0, (double) this.pos.getZ() + 0.5, this.target);
                RenderUtil.drawText(this.pos, (Math.floor(renderDamage) == renderDamage ? Integer.valueOf((int) renderDamage) : String.format("%.1f", renderDamage)) + "");
            }
        }
    }
    public static float[] calculateAngles(Vec3d vector) {
        float yaw = (float) (Math.toDegrees(Math.atan2(vector.z, vector.x)) - 90);
        float pitch = (float) Math.toDegrees(-Math.atan2(vector.y, Math.hypot(vector.x, vector.z)));

        return new float[] {
                MathHelper.wrapDegrees(yaw), MathHelper.wrapDegrees(pitch)
        };
    }
    /*boolean isBlind(BlockPos pos) {
        return mc.world.rayTraceBlocks(new Vec3d(mc.player.posX, mc.player.posY + (double) mc.player.getEyeHeight(), mc.player.posZ), new Vec3d(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5), false, true, false) == null;
    }*/

    /*
    private boolean isPredicting(SPacketSpawnObject packet) {
        BlockPos packPos = new BlockPos(packet.getX(), packet.getY(), packet.getZ());
        if (TooBeeCrystalAura.mc.player.getDistance(packet.getX(), packet.getY(), packet.getZ()) > (double) this.breakRange.getValue()) {
            return false;
        }
        if (!this.canSeePos(packPos) && TooBeeCrystalAura.mc.player.getDistance(packet.getX(), packet.getY(), packet.getZ()) > (double) this.breakWallRange.getValue()) {
            return false;
        }
        double targetDmg = this.calculateDamage(packet.getX() + 0.5, packet.getY() + 1.0, packet.getZ() + 0.5, this.target);
        if (EntityUtil.isInHole(TooBeeCrystalAura.mc.player) && targetDmg >= 1.0) {
            return true;
        }
        double selfDmg = this.calculateDamage(packet.getX() + 0.5, packet.getY() + 1.0, packet.getZ() + 0.5, TooBeeCrystalAura.mc.player);
        if (!suicide.getValue()) {
            if (selfDmg < (double) (TooBeeCrystalAura.mc.player.getHealth() + TooBeeCrystalAura.mc.player.getAbsorptionAmount()) && targetDmg >= (double) (this.target.getAbsorptionAmount() + this.target.getHealth())) {
                return true;
            }
        }
        this.armorTarget = false;
        for (ItemStack is : this.target.getArmorInventoryList()) {
            float green = ((float) is.getMaxDamage() - (float) is.getItemDamage()) / (float) is.getMaxDamage();
            float red = 1.0f - green;
            int dmg = 100 - (int) (red * 100.0f);
            if (!((float) dmg <= this.minArmor.getValue())) continue;
            this.armorTarget = true;
        }
        if (targetDmg >= (double) this.breakMinDmg.getValue() && selfDmg <= (double) this.breakMaxSelfDamage.getValue()) {
            return true;
        }
        return EntityUtil.isInHole(this.target) && this.target.getHealth() + this.target.getAbsorptionAmount() <= this.facePlace.getValue();
     */

    private boolean IsValidCrystal(Entity p_Entity) {
        if (p_Entity == null) {
            return false;
        }
        if (!(p_Entity instanceof EntityEnderCrystal)) {
            return false;
        }
        if (this.target == null) {
            return false;
        }
        if (p_Entity.getDistance(TooBeeCrystalAura.mc.player) > this.breakRange.getValue()) {
            return false;
        }
        if (!TooBeeCrystalAura.mc.player.canEntityBeSeen(p_Entity) && p_Entity.getDistance(TooBeeCrystalAura.mc.player) > this.breakWallRange.getValue()) {
            return false;
        }
        if (this.target.isDead || this.target.getHealth() + this.target.getAbsorptionAmount() <= 0.0f) {
            return false;
        }
        double targetDmg = this.calculateDamage((double) p_Entity.getPosition().getX() + 0.5, (double) p_Entity.getPosition().getY() + 1.0, (double) p_Entity.getPosition().getZ() + 0.5, this.target);
        if (EntityUtil.isInHole(TooBeeCrystalAura.mc.player) && targetDmg >= 1.0) {
            return true;
        }
        double selfDmg = this.calculateDamage((double) p_Entity.getPosition().getX() + 0.5, (double) p_Entity.getPosition().getY() + 1.0, (double) p_Entity.getPosition().getZ() + 0.5, TooBeeCrystalAura.mc.player);
        if (!suicide.getValue()) {
            if (selfDmg < (double) (TooBeeCrystalAura.mc.player.getHealth() + TooBeeCrystalAura.mc.player.getAbsorptionAmount()) && targetDmg >= (double) (this.target.getAbsorptionAmount() + this.target.getHealth())) {
                return true;
            }
        }
        this.armorTarget = false;
        for (ItemStack is : this.target.getArmorInventoryList()) {
            float green = ((float) is.getMaxDamage() - (float) is.getItemDamage()) / (float) is.getMaxDamage();
            float red = 1.0f - green;
            int dmg = 100 - (int) (red * 100.0f);
            if (!((float) dmg <= this.minArmor.getValue())) continue;
            this.armorTarget = true;
        }
        if (targetDmg >= (double) this.breakMinDmg.getValue() && selfDmg <= (double) this.breakMaxSelfDamage.getValue()) {
            return true;
        }
        return EntityUtil.isInHole(this.target) && this.target.getHealth() + this.target.getAbsorptionAmount() <= this.facePlace.getValue();
    }

    EntityPlayer getTarget() {
        EntityPlayer closestPlayer = null;
        for (EntityPlayer entity : TooBeeCrystalAura.mc.world.playerEntities) {
            if (TooBeeCrystalAura.mc.player == null || TooBeeCrystalAura.mc.player.isDead || entity.isDead || entity == TooBeeCrystalAura.mc.player || Quantum.friendManager.isFriend(entity.getName()) || entity.getDistance(TooBeeCrystalAura.mc.player) > 12.0f)
                continue;
            this.armorTarget = false;
            for (ItemStack is : entity.getArmorInventoryList()) {
                float green = ((float) is.getMaxDamage() - (float) is.getItemDamage()) / (float) is.getMaxDamage();
                float red = 1.0f - green;
                int dmg = 100 - (int) (red * 100.0f);
                if (!((float) dmg <= this.minArmor.getValue())) continue;
                this.armorTarget = true;
            }
            if (EntityUtil.isInHole(entity) && entity.getAbsorptionAmount() + entity.getHealth() > this.facePlace.getValue() && !this.armorTarget && this.minDamage.getValue() > 2.2f)
                continue;
            if (closestPlayer == null) {
                closestPlayer = entity;
                continue;
            }
            if (!(closestPlayer.getDistance(TooBeeCrystalAura.mc.player) > entity.getDistance(TooBeeCrystalAura.mc.player)))
                continue;
            closestPlayer = entity;
        }
        return closestPlayer;
    }

    private void manualBreaker() {
        RayTraceResult result;
        if (this.manualTimer.passedMs(200L) && TooBeeCrystalAura.mc.gameSettings.keyBindUseItem.isKeyDown() && TooBeeCrystalAura.mc.player.getHeldItemOffhand().getItem() != Items.GOLDEN_APPLE && TooBeeCrystalAura.mc.player.inventory.getCurrentItem().getItem() != Items.GOLDEN_APPLE && TooBeeCrystalAura.mc.player.inventory.getCurrentItem().getItem() != Items.BOW && TooBeeCrystalAura.mc.player.inventory.getCurrentItem().getItem() != Items.EXPERIENCE_BOTTLE && (result = TooBeeCrystalAura.mc.objectMouseOver) != null) {
            if (result.typeOfHit.equals(RayTraceResult.Type.ENTITY)) {
                Entity entity = result.entityHit;
                if (entity instanceof EntityEnderCrystal) {
                    if (this.packetBreak.getValue()) {
                        TooBeeCrystalAura.mc.player.connection.sendPacket(new CPacketUseEntity(entity));
                    } else {
                        TooBeeCrystalAura.mc.playerController.attackEntity(TooBeeCrystalAura.mc.player, entity);
                    }
                    this.manualTimer.reset();
                }
            } else if (result.typeOfHit.equals(RayTraceResult.Type.BLOCK)) {
                BlockPos mousePos = new BlockPos(TooBeeCrystalAura.mc.objectMouseOver.getBlockPos().getX(), (double) TooBeeCrystalAura.mc.objectMouseOver.getBlockPos().getY() + 1.0, TooBeeCrystalAura.mc.objectMouseOver.getBlockPos().getZ());
                for (Entity target : TooBeeCrystalAura.mc.world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(mousePos))) {
                    if (!(target instanceof EntityEnderCrystal)) continue;
                    if (this.packetBreak.getValue()) {
                        TooBeeCrystalAura.mc.player.connection.sendPacket(new CPacketUseEntity(target));
                    } else {
                        TooBeeCrystalAura.mc.playerController.attackEntity(TooBeeCrystalAura.mc.player, target);
                    }
                    this.manualTimer.reset();
                }
            }
        }
    }

    private boolean canSeePos(BlockPos pos) {
        return TooBeeCrystalAura.mc.world.rayTraceBlocks(new Vec3d(TooBeeCrystalAura.mc.player.posX, TooBeeCrystalAura.mc.player.posY + (double) TooBeeCrystalAura.mc.player.getEyeHeight(), TooBeeCrystalAura.mc.player.posZ), new Vec3d(pos.getX(), pos.getY(), pos.getZ()), false, true, false) == null;
    }

    private NonNullList<BlockPos> placePostions(float placeRange) {
        NonNullList positions = NonNullList.create();
        positions.addAll(TooBeeCrystalAura.getSphere(new BlockPos(Math.floor(TooBeeCrystalAura.mc.player.posX), Math.floor(TooBeeCrystalAura.mc.player.posY), Math.floor(TooBeeCrystalAura.mc.player.posZ)), placeRange, (int) placeRange, false, true, 0).stream().filter(pos -> this.canPlaceCrystal(pos, true)).collect(Collectors.toList()));
        return positions;
    }

    private boolean canPlaceCrystal(BlockPos blockPos, boolean specialEntityCheck) {
        BlockPos boost = blockPos.add(0, 1, 0);
        BlockPos boost2 = blockPos.add(0, 2, 0);
        try {
            if (!this.opPlace.getValue()) {
                if (TooBeeCrystalAura.mc.world.getBlockState(blockPos).getBlock() != Blocks.BEDROCK && TooBeeCrystalAura.mc.world.getBlockState(blockPos).getBlock() != Blocks.OBSIDIAN) {
                    return false;
                }
                if (TooBeeCrystalAura.mc.world.getBlockState(boost).getBlock() != Blocks.AIR || TooBeeCrystalAura.mc.world.getBlockState(boost2).getBlock() != Blocks.AIR) {
                    return false;
                }
                if (!specialEntityCheck) {
                    return TooBeeCrystalAura.mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost)).isEmpty() && TooBeeCrystalAura.mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost2)).isEmpty();
                }
                for (Entity entity : TooBeeCrystalAura.mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost))) {
                    if (entity instanceof EntityEnderCrystal) continue;
                    return false;
                }
                for (Entity entity : TooBeeCrystalAura.mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost2))) {
                    if (entity instanceof EntityEnderCrystal) continue;
                    return false;
                }
            } else {
                if (TooBeeCrystalAura.mc.world.getBlockState(blockPos).getBlock() != Blocks.BEDROCK && TooBeeCrystalAura.mc.world.getBlockState(blockPos).getBlock() != Blocks.OBSIDIAN) {
                    return false;
                }
                if (TooBeeCrystalAura.mc.world.getBlockState(boost).getBlock() != Blocks.AIR) {
                    return false;
                }
                if (!specialEntityCheck) {
                    return TooBeeCrystalAura.mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost)).isEmpty();
                }
                for (Entity entity : TooBeeCrystalAura.mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost))) {
                    if (entity instanceof EntityEnderCrystal) continue;
                    return false;
                }
            }
        } catch (Exception ignored) {
            return false;
        }
        return true;
    }

    private float calculateDamage(double posX, double posY, double posZ, Entity entity) {
        float doubleExplosionSize = 12.0f;
        double distancedsize = entity.getDistance(posX, posY, posZ) / 12.0;
        Vec3d vec3d = new Vec3d(posX, posY, posZ);
        double blockDensity = 0.0;
        try {
            blockDensity = entity.world.getBlockDensity(vec3d, entity.getEntityBoundingBox());
        } catch (Exception exception) {
            // empty catch block
        }
        double v = (1.0 - distancedsize) * blockDensity;
        float damage = (int) ((v * v + v) / 2.0 * 7.0 * 12.0 + 1.0);
        double finald = 1.0;
        if (entity instanceof EntityLivingBase) {
            finald = this.getBlastReduction((EntityLivingBase) entity, this.getDamageMultiplied(damage), new Explosion(TooBeeCrystalAura.mc.world, null, posX, posY, posZ, 6.0f, false, true));
        }
        return (float) finald;
    }

    private float getBlastReduction(EntityLivingBase entity, float damageI, Explosion explosion) {
        float damage = damageI;
        if (entity instanceof EntityPlayer) {
            EntityPlayer ep = (EntityPlayer) entity;
            DamageSource ds = DamageSource.causeExplosionDamage(explosion);
            damage = CombatRules.getDamageAfterAbsorb(damage, (float) ep.getTotalArmorValue(), (float) ep.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue());
            int k = 0;
            try {
                k = EnchantmentHelper.getEnchantmentModifierDamage(ep.getArmorInventoryList(), ds);
            } catch (Exception exception) {
                // empty catch block
            }
            float f = MathHelper.clamp((float) k, 0.0f, 20.0f);
            damage *= 1.0f - f / 25.0f;
            if (entity.isPotionActive(MobEffects.RESISTANCE)) {
                damage -= damage / 4.0f;
            }
            damage = Math.max(damage, 0.0f);
            return damage;
        }
        damage = CombatRules.getDamageAfterAbsorb(damage, (float) entity.getTotalArmorValue(), (float) entity.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue());
        return damage;
    }

    private float getDamageMultiplied(float damage) {
        int diff = TooBeeCrystalAura.mc.world.getDifficulty().getId();
        return damage * (diff == 0 ? 0.0f : (diff == 2 ? 1.0f : (diff == 1 ? 0.5f : 1.5f)));
    }

    public enum SwingMode {
        MainHand,
        OffHand,
        None

    }

    public enum Sync {
        None,
        Instant,
        Attack,
        Sound
    }
}


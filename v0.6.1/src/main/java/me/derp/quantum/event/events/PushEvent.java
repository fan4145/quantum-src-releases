package me.derp.quantum.event.events;

import me.derp.quantum.event.EventStage;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

@Cancelable
public class PushEvent
        extends EventStage {
    public Entity entity;
    public double x;
    public double y;
    public double z;
    public boolean airbone;

    public PushEvent(Entity entity, double x, double y, double z, boolean airbone) {
        super(0);
        this.entity = entity;
        this.x = x;
        this.y = y;
        this.z = z;
        this.airbone = airbone;
    }

    public PushEvent(int stage) {
        super(stage);
    }

    public PushEvent(int stage, Entity entity) {
        super(stage);
        this.entity = entity;
    }
}
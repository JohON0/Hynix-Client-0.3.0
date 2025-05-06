package io.hynix.events.impl;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;

@Getter
@Setter
public class EventMoving {
	public Vector3d from, to, motion, collisionOffset;
    public boolean toGround;
    public AxisAlignedBB aabbFrom;
    public boolean ignoreHorizontal, ignoreVertical, collidedHorizontal, collidedVertical;

    public EventMoving(Vector3d from, Vector3d to, Vector3d motion, Vector3d collisionOffset, boolean toGround,
                       boolean isCollidedHorizontal, boolean isCollidedVertical, AxisAlignedBB aabbFrom) {
        this.from = from;
        this.to = to;
        this.motion = motion;
        this.toGround = toGround;
        this.collidedHorizontal = isCollidedHorizontal;
        this.collidedVertical = isCollidedVertical;
        this.aabbFrom = aabbFrom;
    }
    
	public Vector3d collisionOffset() {
		return this.collisionOffset;
	}

	public Vector3d to() {
		return this.to;
	}
}

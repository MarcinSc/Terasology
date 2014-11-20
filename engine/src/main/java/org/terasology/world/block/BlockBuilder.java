/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.world.block;

import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.linearmath.Transform;
import com.google.common.collect.Maps;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.math.AABB;
import org.terasology.math.Side;
import org.terasology.rendering.assets.mesh.Mesh;
import org.terasology.utilities.collection.EnumBooleanMap;
import org.terasology.world.block.family.BlockFamily;
import org.terasology.world.block.shapes.BlockMeshPart;

import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.util.Map;

public class BlockBuilder {
    private static final Map<BlockPart, Float> DIRECTION_LIT_LEVEL = Maps.newEnumMap(BlockPart.class);

    /**
     * Init. the LUTs.
     */
    static {
        DIRECTION_LIT_LEVEL.put(BlockPart.TOP, 0.9f);
        DIRECTION_LIT_LEVEL.put(BlockPart.BOTTOM, 0.9f);
        DIRECTION_LIT_LEVEL.put(BlockPart.FRONT, 1.0f);
        DIRECTION_LIT_LEVEL.put(BlockPart.BACK, 1.0f);
        DIRECTION_LIT_LEVEL.put(BlockPart.LEFT, 0.75f);
        DIRECTION_LIT_LEVEL.put(BlockPart.RIGHT, 0.75f);
        DIRECTION_LIT_LEVEL.put(BlockPart.CENTER, 0.8f);
    }

    private short id;
    private String displayName = "Untitled block";
    private BlockUri uri;
    private BlockFamily family;
    private Side direction = Side.FRONT;

    /* PROPERTIES */

    // Overall behavioural
    private boolean liquid;
    private boolean attachmentAllowed = true;
    private boolean replacementAllowed;
    private int hardness = 3;
    private boolean supportRequired;
    private EnumBooleanMap<Side> fullSide = new EnumBooleanMap<>(Side.class);
    private BlockSounds sounds = BlockSounds.NULL;

    // Special rendering flags (TODO: clean this up)
    private boolean water;
    private boolean lava;
    private boolean grass;
    private boolean ice;

    // Rendering related
    private boolean invisible;
    private boolean translucent;
    private boolean doubleSided;
    private boolean shadowCasting = true;
    private boolean waving;
    private byte luminance;
    private Vector3f tint = new Vector3f(0, 0, 0);
    private Map<BlockPart, BlockColorSource> colorSource = Maps.newEnumMap(BlockPart.class);
    private Map<BlockPart, Vector4f> colorOffsets = Maps.newEnumMap(BlockPart.class);

    // Collision related
    private boolean penetrable;
    private boolean targetable = true;
    private boolean climbable;

    // Physics
    private float mass = 10;
    private boolean debrisOnDestroy = true;

    // Entity integration
    private String prefab = "";
    private boolean keepActive;
    private EntityRef entity = EntityRef.NULL;
    private boolean lifecycleEventsRequired;

    // Inventory settings
    private boolean directPickup;
    private boolean stackable = true;

    /* Mesh */
    private Mesh mesh;
    private BlockAppearance primaryAppearance = new BlockAppearance();
    // TODO: Remove once liquids have nicer generation
    private Map<Side, BlockMeshPart> loweredLiquidMesh = Maps.newEnumMap(Side.class);

    /* Collision */
    private CollisionShape collisionShape;
    private Vector3f collisionOffset;
    private AABB bounds = AABB.createEmpty();

    public BlockBuilder() {
        for (BlockPart part : BlockPart.values()) {
            colorSource.put(part, DefaultColorSource.DEFAULT);
            colorOffsets.put(part, new Vector4f(1.0f, 1.0f, 1.0f, 1.0f));
        }
    }

    public Block build() {
        return new Block(displayName, uri, family, direction, liquid, attachmentAllowed, replacementAllowed,
                hardness, supportRequired, fullSide, sounds, water, lava, grass, ice, invisible, translucent,
                doubleSided, shadowCasting, waving, luminance, tint, colorSource, colorOffsets, penetrable,
                targetable, climbable, mass, debrisOnDestroy, prefab, keepActive, lifecycleEventsRequired,
                directPickup, stackable, primaryAppearance, loweredLiquidMesh, collisionShape, collisionOffset,
                bounds);
    }

    public BlockBuilder setTranslucent(boolean translucent) {
        this.translucent = translucent;
        return this;
    }

    public BlockBuilder setInvisible(boolean invisible) {
        this.invisible = invisible;
        return this;
    }

    public BlockBuilder setTargetable(boolean targetable) {
        this.targetable = targetable;
        return this;
    }

    public BlockBuilder setId(short id) {
        this.id = id;
        return this;
    }

    public BlockBuilder setDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    public String getDisplayName() {
        return displayName;
    }

    public BlockBuilder setUri(BlockUri uri) {
        this.uri = uri;
        return this;
    }

    public BlockBuilder setFamily(BlockFamily family) {
        this.family = family;
        return this;
    }

    public BlockBuilder setDirection(Side direction) {
        this.direction = direction;
        return this;
    }

    public BlockBuilder setLiquid(boolean liquid) {
        this.liquid = liquid;
        return this;
    }

    public boolean isLiquid() {
        return liquid;
    }

    public BlockBuilder setAttachmentAllowed(boolean attachmentAllowed) {
        this.attachmentAllowed = attachmentAllowed;
        return this;
    }

    public BlockBuilder setReplacementAllowed(boolean replacementAllowed) {
        this.replacementAllowed = replacementAllowed;
        return this;
    }

    public BlockBuilder setHardness(int hardness) {
        this.hardness = hardness;
        return this;
    }

    public BlockBuilder setSupportRequired(boolean supportRequired) {
        this.supportRequired = supportRequired;
        return this;
    }

    public BlockBuilder setPenetrable(boolean penetrable) {
        this.penetrable = penetrable;
        return this;
    }

    public BlockBuilder setShadowCasting(boolean shadowCasting) {
        this.shadowCasting = shadowCasting;
        return this;
    }

    public BlockBuilder setWater(boolean water) {
        this.water = water;
        return this;
    }

    public BlockBuilder setLava(boolean lava) {
        this.lava = lava;
        return this;
    }

    public BlockBuilder setGrass(boolean grass) {
        this.grass = grass;
        return this;
    }

    public BlockBuilder setIce(boolean ice) {
        this.ice = ice;
        return this;
    }

    public BlockBuilder setDoubleSided(boolean doubleSided) {
        this.doubleSided = doubleSided;
        return this;
    }

    public BlockBuilder setWaving(boolean waving) {
        this.waving = waving;
        return this;
    }

    public BlockBuilder setLuminance(byte luminance) {
        this.luminance = luminance;
        return this;
    }

    public BlockBuilder setTint(Vector3f tint) {
        this.tint = tint;
        return this;
    }

    public BlockBuilder setClimbable(boolean climbable) {
        this.climbable = climbable;
        return this;
    }

    public BlockBuilder setMass(float mass) {
        this.mass = mass;
        return this;
    }

    public BlockBuilder setDebrisOnDestroy(boolean debrisOnDestroy) {
        this.debrisOnDestroy = debrisOnDestroy;
        return this;
    }

    public BlockBuilder setPrefab(String prefab) {
        this.prefab = prefab;
        return this;
    }

    public BlockBuilder setKeepActive(boolean keepActive) {
        this.keepActive = keepActive;
        return this;
    }

    public BlockBuilder setDirectPickup(boolean directPickup) {
        this.directPickup = directPickup;
        return this;
    }

    public BlockBuilder setStackable(boolean stackable) {
        this.stackable = stackable;
        return this;
    }

    public BlockBuilder setSounds(BlockSounds sounds) {
        this.sounds = sounds;
        return this;
    }

    public BlockBuilder setPrimaryAppearance(BlockAppearance primaryAppearance) {
        this.primaryAppearance = primaryAppearance;
        return this;
    }

    public BlockBuilder setCollision(Vector3f offset, CollisionShape shape) {
        collisionShape = shape;
        collisionOffset = offset;
        Transform t = new Transform(new Matrix4f(new Quat4f(0, 0, 0, 1), offset, 1.0f));
        Vector3f min = new Vector3f();
        Vector3f max = new Vector3f();
        shape.getAabb(t, min, max);

        bounds = AABB.createMinMax(min, max);
        return this;
    }

    public BlockBuilder setFullSide(Side side, boolean full) {
        fullSide.put(side, full);
        return this;
    }

    public BlockBuilder setLoweredLiquidMesh(Side side, BlockMeshPart meshPart) {
        loweredLiquidMesh.put(side, meshPart);
        return this;
    }

    public BlockBuilder setColorSource(BlockPart part, BlockColorSource value) {
        this.colorSource.put(part, value);
        return this;
    }

    public BlockBuilder setColorOffset(BlockPart part, Vector4f color) {
        colorOffsets.put(part, color);
        return this;
    }
}

/*
 * Copyright 2013 MovingBlocks
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.asset.Assets;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.math.AABB;
import org.terasology.math.Side;
import org.terasology.math.TeraMath;
import org.terasology.math.Vector3i;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.assets.mesh.Mesh;
import org.terasology.rendering.assets.shader.ShaderProgramFeature;
import org.terasology.rendering.primitives.Tessellator;
import org.terasology.utilities.collection.EnumBooleanMap;
import org.terasology.world.biomes.Biome;
import org.terasology.world.block.family.BlockFamily;
import org.terasology.world.block.shapes.BlockMeshPart;
import org.terasology.world.chunks.ChunkConstants;

import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.util.Map;

/**
 * Stores all information for a specific block type.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 * @author Rasmus 'Cervator' Praestholm <cervator@gmail.com>
 */
// TODO: Make this immutable, add a block builder class?
public final class Block {
    private static final Logger logger = LoggerFactory.getLogger(Block.class);

    // TODO: Use directional light(s) when rendering instead of this
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

    // Mutable
    private short id;
    private EntityRef entity;
    private boolean keepActive;
    private boolean lifecycleEventsRequired;
    private Mesh mesh;

    // Immutable
    private final String displayName;
    private final BlockUri uri;
    private final BlockFamily family;
    private final Side direction;

    /* PROPERTIES */

    // Overall behavioural
    private final boolean liquid;
    private final boolean attachmentAllowed;
    private final boolean replacementAllowed;
    private final int hardness;
    private final boolean supportRequired;
    private final EnumBooleanMap<Side> fullSide;
    private final BlockSounds sounds;

    // Special rendering flags (TODO: clean this up)
    private final boolean water;
    private final boolean lava;
    private final boolean grass;
    private final boolean ice;

    // Rendering related
    private final boolean invisible;
    private final boolean translucent;
    private final boolean doubleSided;
    private final boolean shadowCasting;
    private final boolean waving;
    private final byte luminance;
    private final Vector3f tint;
    private final Map<BlockPart, BlockColorSource> colorSource;
    private final Map<BlockPart, Vector4f> colorOffsets;

    // Collision related
    private final boolean penetrable;
    private final boolean targetable;
    private final boolean climbable;

    // Physics
    private final float mass;
    private final boolean debrisOnDestroy;

    // Entity integration
    private final String prefab;

    // Inventory settings
    private final boolean directPickup;
    private final boolean stackable;

    private final BlockAppearance primaryAppearance;
    // TODO: Remove once liquids have nicer generation
    private final Map<Side, BlockMeshPart> loweredLiquidMesh;

    /* Collision */
    private final CollisionShape collisionShape;
    private final Vector3f collisionOffset;
    private final AABB bounds;

    public Block(String displayName, BlockUri uri, BlockFamily family, Side direction, boolean liquid,
                 boolean attachmentAllowed, boolean replacementAllowed, int hardness, boolean supportRequired,
                 EnumBooleanMap<Side> fullSide, BlockSounds sounds, boolean water, boolean lava, boolean grass,
                 boolean ice, boolean invisible, boolean translucent, boolean doubleSided, boolean shadowCasting,
                 boolean waving, byte luminance, Vector3f tint, Map<BlockPart, BlockColorSource> colorSource,
                 Map<BlockPart, Vector4f> colorOffsets, boolean penetrable, boolean targetable, boolean climbable,
                 float mass, boolean debrisOnDestroy, String prefab, boolean keepActive,
                 boolean lifecycleEventsRequired, boolean directPickup, boolean stackable,
                 BlockAppearance primaryAppearance, Map<Side, BlockMeshPart> loweredLiquidMesh,
                 CollisionShape collisionShape, Vector3f collisionOffset, AABB bounds) {
        this.displayName = displayName;
        this.uri = uri;
        this.family = family;
        this.direction = direction;
        this.liquid = liquid;
        this.attachmentAllowed = attachmentAllowed;
        this.replacementAllowed = replacementAllowed;
        this.hardness = hardness;
        this.supportRequired = supportRequired;
        this.fullSide = fullSide;
        this.sounds = sounds;
        this.water = water;
        this.lava = lava;
        this.grass = grass;
        this.ice = ice;
        this.invisible = invisible;
        this.translucent = translucent;
        this.doubleSided = doubleSided;
        this.shadowCasting = shadowCasting;
        this.waving = waving;
        this.luminance = luminance;
        this.tint = tint;
        this.colorSource = colorSource;
        this.colorOffsets = colorOffsets;
        this.penetrable = penetrable;
        this.targetable = targetable;
        this.climbable = climbable;
        this.mass = mass;
        this.debrisOnDestroy = debrisOnDestroy;
        this.prefab = prefab;
        this.keepActive = keepActive;
        this.lifecycleEventsRequired = lifecycleEventsRequired;
        this.directPickup = directPickup;
        this.stackable = stackable;
        this.primaryAppearance = primaryAppearance;
        this.loweredLiquidMesh = loweredLiquidMesh;
        this.collisionShape = collisionShape;
        this.collisionOffset = collisionOffset;
        this.bounds = bounds;
    }

    public short getId() {
        return id;
    }

    public void setId(short id) {
        this.id = id;
    }

    public BlockUri getURI() {
        return uri;
    }

    public String getDisplayName() {
        return displayName;
    }

    public BlockFamily getBlockFamily() {
        return family;
    }

    public Side getDirection() {
        return direction;
    }

    /**
     * @return Whether this block should be rendered Double Sided
     */
    public boolean isDoubleSided() {
        return doubleSided;
    }

    /**
     * A liquid has some special handling around shape
     *
     * @return Whether this block is a liquid
     */
    public boolean isLiquid() {
        return liquid;
    }

    public boolean isWater() {
        return water;
    }

    public boolean isLava() {
        return lava;
    }

    public boolean isGrass() {
        return grass;
    }

    public boolean isIce() {
        return ice;
    }

    /**
     * @return The sound set used by this block. Never null.
     */
    public BlockSounds getSounds() {
        return sounds;
    }

    /**
     * @return Whether this block is translucent/alpha masked
     */
    public boolean isTranslucent() {
        return translucent;
    }

    /**
     * @return Whether this block needs to be rendered at all
     */
    public boolean isInvisible() {
        return invisible;
    }

    /**
     * A block is penetrable if it does not block solid objects.
     *
     * @return Whether this block allows solid objects to pass through it.
     */
    public boolean isPenetrable() {
        return penetrable;
    }

    /**
     * @return Does this block create a slight shadow around it
     */
    // TODO: Remove this once SSAO is implemented?
    public boolean isShadowCasting() {
        return shadowCasting && luminance == 0;
    }

    /**
     * @return Can this block be targetted for interactions
     */
    public boolean isTargetable() {
        return targetable;
    }

    public boolean isClimbable() {
        return climbable;
    }

    /**
     * @return Whether this block waves in the wind
     */
    public boolean isWaving() {
        return waving;
    }

    /**
     * @return Whether this block can be replaced freely by other blocks
     */
    public boolean isReplacementAllowed() {
        return replacementAllowed;
    }

    /**
     * @return Whether blocks can be attached to this block
     */
    public boolean isAttachmentAllowed() {
        return attachmentAllowed;
    }

    public boolean canAttachTo(Side side) {
        return attachmentAllowed && fullSide.get(side);
    }

    /**
     * @return Whether this block should be destroyed when no longer attached
     */
    public boolean isSupportRequired() {
        return supportRequired;
    }

    /**
     * @return The entity prefab for this block
     */
    public String getPrefab() {
        return prefab;
    }

    public boolean isKeepActive() {
        return keepActive;
    }

    public void setKeepActive(boolean keepActive) {
        this.keepActive = keepActive;
    }

    public EntityRef getEntity() {
        return entity;
    }

    public boolean isLifecycleEventsRequired() {
        return lifecycleEventsRequired;
    }

    public void setLifecycleEventsRequired(boolean lifecycleEventsRequired) {
        this.lifecycleEventsRequired = lifecycleEventsRequired;
    }

    /**
     * @return Whether this block should go directly into a character's inventory when harvested
     */
    public boolean isDirectPickup() {
        return directPickup;
    }

    public boolean isStackable() {
        return stackable;
    }

    /**
     * @return How much damage it takes to destroy the block
     */
    public int getHardness() {
        return hardness;
    }

    public boolean isDestructible() {
        return getHardness() > 0;
    }

    /**
     * @return The light level produced by this block
     */
    public byte getLuminance() {
        return luminance;
    }

    public Vector3f getTint() {
        return tint;
    }

    /**
     * @return Whether physics debris of the block is created when the block is destroyed
     */
    public boolean isDebrisOnDestroy() {
        return debrisOnDestroy;
    }

    public float getMass() {
        return mass;
    }

    public BlockColorSource getColorSource(BlockPart part) {
        return colorSource.get(part);
    }

    public BlockAppearance getPrimaryAppearance() {
        return primaryAppearance;
    }

    public BlockAppearance getAppearance(Map<Side, Block> adjacentBlocks) {
        return primaryAppearance;
    }

    public Mesh getMesh() {
        if (mesh == null || mesh.isDisposed()) {
            generateMesh();
        }
        return mesh;
    }

    public BlockMeshPart getLoweredLiquidMesh(Side side) {
        return loweredLiquidMesh.get(side);
    }

    /**
     * @param side
     * @return Is the given side of the block "full" (a full square filling the side)
     */
    public boolean isFullSide(Side side) {
        return fullSide.get(side);
    }

    /**
     * Calculates the color offset for a given block type and a specific
     * side of the block.
     *
     * @param part  The block side
     * @param biome The block's biome
     * @return The color offset
     */
    public Vector4f calcColorOffsetFor(BlockPart part, Biome biome) {
        BlockColorSource source = getColorSource(part);
        Vector4f color = source.calcColor(biome);

        Vector4f colorOffset = colorOffsets.get(part);
        color.x *= colorOffset.x;
        color.y *= colorOffset.y;
        color.z *= colorOffset.z;
        color.w *= colorOffset.w;

        return color;
    }

    public CollisionShape getCollisionShape() {
        return collisionShape;
    }

    public Vector3f getCollisionOffset() {
        return collisionOffset;
    }

    public AABB getBounds(Vector3i pos) {
        return bounds.move(pos.toVector3f());
    }

    public AABB getBounds(Vector3f floatPos) {
        return getBounds(new Vector3i(floatPos, 0.5f));
    }

    public void renderWithLightValue(float sunlight, float blockLight) {
        if (isInvisible()) {
            return;
        }

        Material mat = Assets.getMaterial("engine:prog.block");
        mat.activateFeature(ShaderProgramFeature.FEATURE_USE_MATRIX_STACK);

        mat.enable();
        mat.setFloat("sunlight", sunlight);
        mat.setFloat("blockLight", blockLight);

        if (mesh == null || mesh.isDisposed()) {
            generateMesh();
        }

        mesh.render();

        mat.deactivateFeature(ShaderProgramFeature.FEATURE_USE_MATRIX_STACK);
    }

    private void generateMesh() {
        Tessellator tessellator = new Tessellator();
        for (BlockPart dir : BlockPart.values()) {
            BlockMeshPart part = primaryAppearance.getPart(dir);
            if (part != null) {
                if (doubleSided) {
                    tessellator.addMeshPartDoubleSided(part);
                } else {
                    tessellator.addMeshPart(part);
                }
            }
        }
        mesh = tessellator.generateMesh(new AssetUri(AssetType.MESH, uri.toString()));
    }

    public void setEntity(EntityRef entity) {
        this.entity = entity;
    }

    @Override
    public String toString() {
        return uri.toString();
    }
}

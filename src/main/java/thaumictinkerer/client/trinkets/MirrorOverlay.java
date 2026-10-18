package thaumictinkerer.client.trinkets;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import thaumcraft.client.platform.TcRenderTypes;
import thaumcraft.client.platform.TcRenderTypes.Kind;
import thaumictinkerer.Config;
import thaumictinkerer.ThaumicTinkerer;
import thaumictinkerer.client.geom.GhostBlockGeometry;
import thaumictinkerer.client.geom.GhostBlockGeometry.UvRect;
import thaumictinkerer.client.platform.ModelVertexSink;
import thaumictinkerer.kami.trinkets.WorldshaperGlassItem;

@EventBusSubscriber(modid = ThaumicTinkerer.MODID, value = Dist.CLIENT)
public final class MirrorOverlay {
    private static final int GHOST_COLOR = 0x99FFFFFF;
    private static final int FULL_LIGHT = 0xF000F0;

    private MirrorOverlay() {
    }

    @SubscribeEvent
    static void onSubmit(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS || !Config.placementMirrorBlocks()) {
            return;
        }
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        Vec3 camera = event.getCamera().getPosition();
        PoseStack poseStack = event.getPoseStack();
        for (Player player : level.players()) {
            ItemStack stack = player.getMainHandItem();
            if (!(stack.getItem() instanceof WorldshaperGlassItem)) {
                continue;
            }
            Item item = WorldshaperGlassItem.getItem(stack);
            if (!(item instanceof BlockItem blockItem)) {
                continue;
            }
            List<BlockPos> targets = WorldshaperGlassItem.getBlocksToPlace(stack, player);
            if (targets.isEmpty() || !WorldshaperGlassItem.hasBlocks(stack, player, targets)) {
                continue;
            }
            BlockState state = blockItem.getBlock().defaultBlockState();
            TextureAtlasSprite sprite = Minecraft.getInstance().getBlockRenderer().getBlockModel(state).getParticleIcon();
            UvRect uv = new UvRect(sprite.getU(0.0F), sprite.getV(0.0F), sprite.getU(1.0F), sprite.getV(1.0F));
            thaumcraft.client.platform.ItemGeometry.submit(Minecraft.getInstance().renderBuffers().bufferSource(),
                poseStack,
                TcRenderTypes.of(Kind.ENTITY_TRANSLUCENT, sprite.atlasLocation()),
                (pose, buffer) -> {
                    ModelVertexSink sink = new ModelVertexSink(pose, buffer, GHOST_COLOR, FULL_LIGHT);
                    for (BlockPos target : targets) {
                        GhostBlockGeometry.cube(
                            sink,
                            (float) (target.getX() - camera.x),
                            (float) (target.getY() - camera.y),
                            (float) (target.getZ() - camera.z),
                            uv
                        );
                    }
                }
            );
        }
        Minecraft.getInstance().renderBuffers().bufferSource().endBatch();
    }
}

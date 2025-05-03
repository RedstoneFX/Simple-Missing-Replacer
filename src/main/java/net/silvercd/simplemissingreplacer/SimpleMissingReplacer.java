package net.silvercd.simplemissingreplacer;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(SimpleMissingReplacer.MODID)
public class SimpleMissingReplacer {
    public static final String MODID = "simplemissingreplacer";
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredBlock<Block> PLACEHOLDER_BLOCK = BLOCKS.registerSimpleBlock("placeholder", BlockBehaviour.Properties.of().mapColor(MapColor.STONE));

    public SimpleMissingReplacer(IEventBus modEventBus, ModContainer modContainer) {
        BLOCKS.register(modEventBus);
        NeoForge.EVENT_BUS.addListener(SimpleMissingReplacer::onChunkLoaded);
    }


    private static void onChunkLoaded(ChunkEvent.Load event) {
        if (event.getLevel().isClientSide()) return;
        if (event.isNewChunk()) return;
        ChunkAccess chunk = event.getChunk();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(0, 0, 0);
        for (int iy = chunk.getMinBuildHeight(); iy <= chunk.getMaxBuildHeight(); ++iy) {
            for (int ix = 0; ix < 16; ++ix) {
                for (int iz = 0; iz < 16; ++iz) {
                    if (chunk.getBlockState(pos.set(ix, iy, iz)).getBlock().equals(PLACEHOLDER_BLOCK.value())) {
                        tryToReplacePlaceholder(pos, event.getChunk());
                    }
                }
            }
        }
    }

    private static void tryToReplacePlaceholder(BlockPos.MutableBlockPos pos, ChunkAccess chunk) {
        if (pos.getY() == chunk.getMinBuildHeight()) {
            chunk.setBlockState(pos, Blocks.BEDROCK.defaultBlockState(), false);
        } else {
            BlockState replacer = chunk.getBlockState(pos.move(0, -1, 0));
            if (replacer.hasBlockEntity()) replacer = Blocks.AIR.defaultBlockState();
            chunk.setBlockState(pos.move(0, 1, 0), replacer, false);
        }
    }
}

package com.animationcreator.world;

import net.minecraft.block.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Generates the Animation City Hub — a custom structure containing:
 *  - Animation workstations (enchanting tables used as workstations)
 *  - Tutorial signs
 *  - Animation display screens (note blocks)
 *  - A central plaza with paths
 *  - Quest-giver NPC markers
 */
public class CityHubFeature {

    private static final int WIDTH  = 31;
    private static final int DEPTH  = 31;
    private static final int HEIGHT = 15;

    public static void generate(ServerWorld world, BlockPos origin) {
        // Flatten the ground
        for (int x = -WIDTH/2; x <= WIDTH/2; x++) {
            for (int z = -DEPTH/2; z <= DEPTH/2; z++) {
                BlockPos base = origin.add(x, -1, z);
                world.setBlockState(base, Blocks.SMOOTH_STONE.getDefaultState());
                // Clear space above
                for (int y = 0; y < HEIGHT; y++) {
                    world.setBlockState(base.up(y + 1), Blocks.AIR.getDefaultState());
                }
            }
        }

        // Stone brick border walls
        buildBorderWalls(world, origin);

        // Central path (east-west)
        for (int x = -WIDTH/2; x <= WIDTH/2; x++) {
            world.setBlockState(origin.add(x, 0, 0), Blocks.STONE_BRICKS.getDefaultState());
        }
        // Central path (north-south)
        for (int z = -DEPTH/2; z <= DEPTH/2; z++) {
            world.setBlockState(origin.add(0, 0, z), Blocks.STONE_BRICKS.getDefaultState());
        }

        // Decorative gold blocks at path intersections
        world.setBlockState(origin.add(0, 0, 0), Blocks.GOLD_BLOCK.getDefaultState());

        // ── Workstations (4 corners of inner quad) ──────────────────────────
        placeWorkstation(world, origin.add(-8, 0, -8),  "WORKSTATION A");
        placeWorkstation(world, origin.add(8, 0, -8),   "WORKSTATION B");
        placeWorkstation(world, origin.add(-8, 0, 8),   "WORKSTATION C");
        placeWorkstation(world, origin.add(8, 0, 8),    "WORKSTATION D");

        // ── Tutorial signs ──────────────────────────────────────────────────
        placeTutorialSign(world, origin.add(-3, 1, -8),
                "Animation Creator", "Press 'K' to open GUI", "Type any prompt!", "e.g. zombie walking");
        placeTutorialSign(world, origin.add(3, 1, -8),
                "Commands:", "/animation list", "/animation play <id>", "/animation hub");
        placeTutorialSign(world, origin.add(-3, 1, 8),
                "Animation Library:", "Browse saved anims", "Import / Export JSON", "Assign to mobs");
        placeTutorialSign(world, origin.add(3, 1, 8),
                "AI Generation:", "Uses Pollinations.ai", "100% free, no key!", "Works offline too");

        // ── Display screens (note block pillars) ────────────────────────────
        buildDisplayScreen(world, origin.add(-14, 0, 0));
        buildDisplayScreen(world, origin.add(14, 0, 0));
        buildDisplayScreen(world, origin.add(0, 0, -14));
        buildDisplayScreen(world, origin.add(0, 0, 14));

        // ── Quest giver NPC marker ───────────────────────────────────────────
        placeQuestGiver(world, origin.add(0, 1, -12));

        // ── Lighting ────────────────────────────────────────────────────────
        placeLamps(world, origin);
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private static void buildBorderWalls(ServerWorld world, BlockPos origin) {
        int hx = WIDTH/2, hz = DEPTH/2;
        for (int x = -hx; x <= hx; x++) {
            for (int y = 0; y < 5; y++) {
                world.setBlockState(origin.add(x, y, -hz), Blocks.STONE_BRICK_WALL.getDefaultState());
                world.setBlockState(origin.add(x, y, hz),  Blocks.STONE_BRICK_WALL.getDefaultState());
            }
        }
        for (int z = -hz; z <= hz; z++) {
            for (int y = 0; y < 5; y++) {
                world.setBlockState(origin.add(-hx, y, z), Blocks.STONE_BRICK_WALL.getDefaultState());
                world.setBlockState(origin.add(hx, y, z),  Blocks.STONE_BRICK_WALL.getDefaultState());
            }
        }
    }

    private static void placeWorkstation(ServerWorld world, BlockPos pos, String label) {
        // 3×3 platform
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                world.setBlockState(pos.add(dx, -1, dz), Blocks.DARK_OAK_PLANKS.getDefaultState());
            }
        }
        // Enchanting table as workstation
        world.setBlockState(pos, Blocks.ENCHANTING_TABLE.getDefaultState());
        // Bookshelves for decoration
        world.setBlockState(pos.add(-1, 1, 0), Blocks.BOOKSHELF.getDefaultState());
        world.setBlockState(pos.add(1, 1, 0),  Blocks.BOOKSHELF.getDefaultState());
        world.setBlockState(pos.add(0, 1, -1), Blocks.BOOKSHELF.getDefaultState());
        // Torch
        world.setBlockState(pos.up(2), Blocks.GLOWSTONE.getDefaultState());
    }

    private static void placeTutorialSign(ServerWorld world, BlockPos pos, String line1, String line2, String line3, String line4) {
        // Oak sign on ground
        world.setBlockState(pos, Blocks.OAK_SIGN.getDefaultState());
        // Decorate base
        world.setBlockState(pos.down(), Blocks.OAK_FENCE.getDefaultState());
        world.setBlockState(pos.down(2), Blocks.SMOOTH_STONE.getDefaultState());

        if (world.getBlockEntity(pos) instanceof net.minecraft.block.entity.SignBlockEntity sign) {
            sign.setTextOnSide(true, net.minecraft.block.entity.SignText.DEFAULT
                    .withMessage(0, Text.literal(line1))
                    .withMessage(1, Text.literal(line2))
                    .withMessage(2, Text.literal(line3))
                    .withMessage(3, Text.literal(line4)));
        }
    }

    private static void buildDisplayScreen(ServerWorld world, BlockPos pos) {
        // 5-tall pillar of note blocks with glowstone top — acts as a "screen"
        for (int y = 0; y < 6; y++) {
            world.setBlockState(pos.up(y), Blocks.NOTE_BLOCK.getDefaultState());
        }
        world.setBlockState(pos.up(6), Blocks.GLOWSTONE.getDefaultState());

        // Side pillars
        world.setBlockState(pos.add(-1, 0, 0), Blocks.IRON_BARS.getDefaultState());
        world.setBlockState(pos.add(1, 0, 0),  Blocks.IRON_BARS.getDefaultState());
        world.setBlockState(pos.add(-1, 6, 0), Blocks.IRON_BARS.getDefaultState());
        world.setBlockState(pos.add(1, 6, 0),  Blocks.IRON_BARS.getDefaultState());
    }

    private static void placeQuestGiver(ServerWorld world, BlockPos pos) {
        // Gold block base + diamond block head = "NPC" visual marker
        world.setBlockState(pos, Blocks.GOLD_BLOCK.getDefaultState());
        world.setBlockState(pos.up(), Blocks.DIAMOND_BLOCK.getDefaultState());
        world.setBlockState(pos.up(2), Blocks.GLOWSTONE.getDefaultState());
        // Sign in front
        BlockPos signPos = pos.add(0, 1, 1);
        world.setBlockState(signPos, Blocks.OAK_SIGN.getDefaultState());
        if (world.getBlockEntity(signPos) instanceof net.minecraft.block.entity.SignBlockEntity sign) {
            sign.setTextOnSide(true, net.minecraft.block.entity.SignText.DEFAULT
                    .withMessage(0, Text.literal("QUEST GIVER"))
                    .withMessage(1, Text.literal("Create 5 anims"))
                    .withMessage(2, Text.literal("to unlock"))
                    .withMessage(3, Text.literal("DRAGON anim!")));
        }
    }

    private static void placeLamps(ServerWorld world, BlockPos origin) {
        int[][] positions = {{-10,6},{10,6},{-10,-6},{10,-6},{6,10},{-6,10},{6,-10},{-6,-10}};
        for (int[] p : positions) {
            BlockPos lamp = origin.add(p[0], 0, p[1]);
            world.setBlockState(lamp, Blocks.OAK_FENCE.getDefaultState());
            world.setBlockState(lamp.up(), Blocks.OAK_FENCE.getDefaultState());
            world.setBlockState(lamp.up(2), Blocks.SEA_LANTERN.getDefaultState());
        }
    }
}

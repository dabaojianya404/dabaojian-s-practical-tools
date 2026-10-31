package cn.autoforged.tree_clearing_tool_mod_1787384923.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * 光照更新工具：右键更新玩家周围的光照；Shift+右键循环调整更新范围。
 *
 * 修改要求对应的行为：
 *  - 普通右键：以玩家所在方块为中心，对 range x range x range 范围内的每个方块调用
 *    {@link net.minecraft.world.level.lighting.LevelLightEngine#checkBlock(BlockPos)}
 *    强制重算该方块的方块光与天空光，修复因各种原因残留的阴影/光照错误。
 *    checkBlock 对未加载区块是安全空操作（见 LightEngine 的 storingLightForSection 守卫），
 *    因此可以放心遍历一个较大的立方体。
 *  - Shift+右键：在预设档位间循环切换更新范围（只调范围、不执行更新），存于物品 NBT
 *    （键 light_range），并显示当前值。范围调节发生在服务端 use() 内，直接写服务端手持
 *    物品 NBT，无需网络包同步。
 *
 * 假设（需求未明示，采用最合理方案）：
 *  - range 作为立方体的半径（半边长），默认 16，档位 8/16/32/64 循环，最大 64。
 *  - 普通右键执行更新时不扣耐久逻辑不强制；这里只在执行更新时扣 1 点耐久（创造下无副作用），
 *    调节范围不扣耐久。
 */
public class LightUpdateToolItem extends Item {

	public static final String RANGE_KEY = "light_range";
	public static final int DEFAULT_RANGE = 16;
	public static final int MAX_RANGE = 64;
	private static final int[] PRESETS = { 8, 16, 32, 64 };

	public LightUpdateToolItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (level.isClientSide) {
			return InteractionResultHolder.sidedSuccess(stack, true);
		}

		int range = Math.max(1, Math.min(MAX_RANGE, AreaToolBaseItem.readInt(stack, RANGE_KEY, DEFAULT_RANGE)));
		if (player.isShiftKeyDown()) {
			int next = nextPreset(range);
			AreaToolBaseItem.setInt(stack, RANGE_KEY, next);
			player.displayClientMessage(net.minecraft.network.chat.Component.literal("光照更新范围: " + next), true);
		} else {
			updateLight(level, player.blockPosition(), range);
			player.displayClientMessage(net.minecraft.network.chat.Component.literal("已更新 " + range + " 范围内光照"), true);
			stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
		}
		return InteractionResultHolder.sidedSuccess(stack, true);
	}

	/** 以 center 为中心，遍历边长为 2*range 的立方体，逐块强制重算光照。 */
	private static void updateLight(Level level, BlockPos center, int range) {
		BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
		for (int dx = -range; dx <= range; dx++) {
			for (int dy = -range; dy <= range; dy++) {
				for (int dz = -range; dz <= range; dz++) {
					mutable.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
					level.getLightEngine().checkBlock(mutable);
				}
			}
		}
	}

	/** 返回当前范围之后的下一档范围；到达末档后回到首档（循环）。 */
	private static int nextPreset(int current) {
		for (int i = 0; i < PRESETS.length; i++) {
			if (current <= PRESETS[i]) {
				return PRESETS[(i + 1) % PRESETS.length];
			}
		}
		return PRESETS[0];
	}
}

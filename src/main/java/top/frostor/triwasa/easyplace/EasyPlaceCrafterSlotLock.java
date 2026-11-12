package top.frostor.triwasa.easyplace;

import fi.dy.masa.litematica.world.SchematicWorldHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CrafterBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CrafterBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import top.frostor.triwasa.config.TriWasaConfigs;

public final class EasyPlaceCrafterSlotLock
{
	private static final int SLOT_COUNT = 9;
	private static final ThreadLocal<Integer> PENDING_SLOTS = ThreadLocal.withInitial(() -> 0);

	private EasyPlaceCrafterSlotLock()
	{
	}

	public static void capture(BlockPos pos, BlockState state)
	{
		PENDING_SLOTS.set(0);
		if(!TriWasaConfigs.EASY_PLACE_CRAFTER_LOCK_SLOT.getBooleanValue()
				|| !(state.getBlock() instanceof CrafterBlock))
		{
			return;
		}

		Level schematicWorld = SchematicWorldHandler.getSchematicWorld();
		if(schematicWorld == null)
		{
			return;
		}

		BlockEntity blockEntity = schematicWorld.getBlockEntity(pos);
		if(!(blockEntity instanceof CrafterBlockEntity crafter))
		{
			return;
		}

		int disabledMask = 0;
		for(int slot = 0; slot < SLOT_COUNT; slot++)
		{
			if(crafter.isSlotDisabled(slot))
			{
				disabledMask |= 1 << slot;
			}
		}
		PENDING_SLOTS.set(disabledMask & EasyPlaceExtraProtocol.CRAFTER_SLOT_MASK);
	}

	public static int getPendingMask()
	{
		return PENDING_SLOTS.get();
	}

	public static void clear()
	{
		PENDING_SLOTS.remove();
	}

	static int[] decodeSlots(int disabledMask)
	{
		int[] slots = new int[Integer.bitCount(disabledMask & EasyPlaceExtraProtocol.CRAFTER_SLOT_MASK)];
		int index = 0;
		for(int slot = 0; slot < SLOT_COUNT; slot++)
		{
			if((disabledMask & (1 << slot)) != 0)
			{
				slots[index++] = slot;
			}
		}
		return slots;
	}

}

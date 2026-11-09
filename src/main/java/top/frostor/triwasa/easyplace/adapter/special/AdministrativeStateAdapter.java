package top.frostor.triwasa.easyplace.adapter.special;

import net.minecraft.core.FrontAndTop;
import net.minecraft.world.level.block.CommandBlock;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.StructureBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.StructureMode;
import top.frostor.triwasa.easyplace.EasyPlaceStateSupport;
import top.frostor.triwasa.easyplace.registry.EasyPlaceCodecContext;
import top.frostor.triwasa.easyplace.registry.EasyPlaceStateDecoder;
import top.frostor.triwasa.easyplace.registry.EasyPlaceStateEncoder;

public final class AdministrativeStateAdapter implements EasyPlaceStateEncoder, EasyPlaceStateDecoder
{
	public static boolean matches(EasyPlaceCodecContext context)
	{
		return context.block() instanceof CommandBlock
				|| context.block() instanceof JigsawBlock
				|| context.block() instanceof LightBlock
				|| context.block() instanceof NoteBlock
				|| context.block() instanceof StructureBlock;
	}

	@Override
	public int encode(EasyPlaceCodecContext context)
	{
		BlockState state = context.state();
		if(context.block() instanceof CommandBlock)
		{
			return EasyPlaceStateSupport.encodeGeneric(state)
					| (state.getValue(CommandBlock.CONDITIONAL) ? 0x10 : 0);
		}
		if(context.block() instanceof JigsawBlock)
		{
			return state.hasProperty(BlockStateProperties.ORIENTATION)
					? state.getValue(BlockStateProperties.ORIENTATION).ordinal() & 0xf
					: 0;
		}
		if(context.block() instanceof LightBlock)
		{
			return state.getValue(LightBlock.LEVEL) & 0xf;
		}
		if(context.block() instanceof NoteBlock)
		{
			return state.getValue(NoteBlock.NOTE) & 0x1f;
		}
		return state.getValue(StructureBlock.MODE).ordinal() & 0x3;
	}

	@Override
	public BlockState decode(EasyPlaceCodecContext context)
	{
		BlockState state = context.state();
		int rawValue = context.rawValue();
		if(context.block() instanceof CommandBlock)
		{
			BlockState generic = EasyPlaceStateSupport.applyGeneric(
					state,
					rawValue,
					context.placementContext()
			);
			return EasyPlaceStateSupport.replaceSlot(
					generic,
					CommandBlock.CONDITIONAL,
					(rawValue & 0x10) != 0
			);
		}
		if(context.block() instanceof JigsawBlock)
		{
			FrontAndTop[] values = FrontAndTop.values();
			return EasyPlaceStateSupport.replaceSlot(
					state,
					JigsawBlock.ORIENTATION,
					values[(rawValue & 0xf) % values.length]
			);
		}
		if(context.block() instanceof LightBlock)
		{
			return EasyPlaceStateSupport.replaceSlot(state, LightBlock.LEVEL, rawValue & 0xf);
		}
		if(context.block() instanceof NoteBlock)
		{
			return EasyPlaceStateSupport.replaceSlot(state, NoteBlock.NOTE, (rawValue & 0x1f) % 25);
		}
		return EasyPlaceStateSupport.replaceSlot(
				state,
				StructureBlock.MODE,
				StructureMode.values()[rawValue & 0x3]
		);
	}
}

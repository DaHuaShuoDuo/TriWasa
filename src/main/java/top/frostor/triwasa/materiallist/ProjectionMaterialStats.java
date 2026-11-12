package top.frostor.triwasa.materiallist;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.SegmentableBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

/**
 * Maps a schematic block state to the number of items one block actually consumes.
 *
 * <p>Litematica's {@code MaterialCache#overrideStackSize} only handles double slabs, snow
 * layers, turtle eggs, sea pickles, candles and the spreadable multiface blocks. Flower beds,
 * leaf litter and resin clumps fall through, and vines are not multiface blocks at all, so a
 * multi-segment block ends up counted as a single item even though it is built by placing one
 * item per segment or per attached face.</p>
 */
public final class ProjectionMaterialStats
{
	private static final BooleanProperty[] VINE_FACES = {
			VineBlock.UP,
			VineBlock.NORTH,
			VineBlock.SOUTH,
			VineBlock.WEST,
			VineBlock.EAST
	};

	private ProjectionMaterialStats()
	{
	}

	/**
	 * @return the number of items one block of this state consumes, or {@code 0} when this class
	 * does not take over the state and the upstream value should be left untouched.
	 */
	public static int itemsPerBlock(BlockState state)
	{
		Block block = state.getBlock();

		// Flower beds (pink petals, wildflowers) and leaf litter expose a 1..4 segment count.
		if(block instanceof SegmentableBlock segmentable)
		{
			IntegerProperty property = segmentable.getSegmentAmountProperty();

			if(property != null && state.hasProperty(property))
			{
				return state.getValue(property);
			}

			return 0;
		}

		// Glow lichen, sculk veins and resin clumps accumulate one item per attached face.
		if(block instanceof MultifaceBlock)
		{
			return MultifaceBlock.availableFaces(state).size();
		}

		// Vines are not MultifaceBlock subclasses, but they accumulate faces the same way.
		if(block instanceof VineBlock)
		{
			int faces = 0;

			for(BooleanProperty property : VINE_FACES)
			{
				if(state.getValue(property))
				{
					faces++;
				}
			}

			return faces;
		}

		return 0;
	}
}

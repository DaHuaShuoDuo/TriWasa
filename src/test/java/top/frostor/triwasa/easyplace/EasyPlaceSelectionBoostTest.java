package top.frostor.triwasa.easyplace;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class EasyPlaceSelectionBoostTest extends EasyPlaceTestSupport
{
	private EasyPlaceSelectionBoostTest()
	{
	}

	public static int run()
	{
		EasyPlaceSelectionBoostTest test = new EasyPlaceSelectionBoostTest();
		test.checkDisabledScopeKeepsShape();
		test.checkEmptyShapeStaysEmpty();
		test.checkIncompleteShapeBecomesBlock();
		test.checkFullBlockShapeStaysFullBlock();
		test.checkCorrectStateKeepsShape();
		test.checkLitematicaStateIdentity();
		return test.checks();
	}

	private void checkDisabledScopeKeepsShape()
	{
		VoxelShape torch = torchShape();
		check(
				SchematicSelectionBoost.expandShape(torch, false, false) == torch,
				"disabled boost must keep the real outline shape"
		);
	}

	private void checkEmptyShapeStaysEmpty()
	{
		VoxelShape expanded = SchematicSelectionBoost.expandShape(Shapes.empty(), true, false);
		check(
				expanded.isEmpty(),
				"positions without an outline must not become click targets"
		);
	}

	private void checkIncompleteShapeBecomesBlock()
	{
		VoxelShape torch = torchShape();
		VoxelShape expanded = SchematicSelectionBoost.expandShape(torch, true, false);
		check(
				expanded.min(Direction.Axis.X) == 0.0D && expanded.max(Direction.Axis.X) == 1.0D,
				"boosted shapes must span the whole X cell"
		);
		check(
				expanded.min(Direction.Axis.Y) == 0.0D && expanded.max(Direction.Axis.Y) == 1.0D,
				"boosted shapes must span the whole Y cell"
		);
		check(
				expanded.min(Direction.Axis.Z) == 0.0D && expanded.max(Direction.Axis.Z) == 1.0D,
				"boosted shapes must span the whole Z cell"
		);
	}

	private void checkFullBlockShapeStaysFullBlock()
	{
		VoxelShape expanded = SchematicSelectionBoost.expandShape(Shapes.block(), true, false);
		check(
				expanded == Shapes.block(),
				"full blocks must keep the plain full block shape"
		);
	}

	private void checkCorrectStateKeepsShape()
	{
		VoxelShape torch = torchShape();
		check(
				SchematicSelectionBoost.expandShape(torch, true, true) == torch,
				"blocks already matching the schematic must keep their real outline"
		);
	}

	private void checkLitematicaStateIdentity()
	{
		BlockState slab = Blocks.OAK_SLAB.defaultBlockState();
		BlockState waterlogged = slab.setValue(BlockStateProperties.WATERLOGGED, true);
		BlockState top = slab.setValue(BlockStateProperties.SLAB_TYPE, SlabType.TOP);

		check(
				SchematicSelectionBoost.isSameBlockState(slab, slab),
				"identical schematic and client states must count as correct"
		);
		check(
				SchematicSelectionBoost.isSameBlockState(waterlogged, slab) == false,
				"Litematica compares BlockState identity, so WATERLOGGED still needs a placement"
		);
		check(
				SchematicSelectionBoost.isSameBlockState(top, slab) == false,
				"a slab half mismatch must not count as correct"
		);
		check(
				SchematicSelectionBoost.isSameBlockState(
						Blocks.STONE.defaultBlockState(),
						slab
				) == false,
				"different blocks must not count as correct"
		);
		check(
				SchematicSelectionBoost.isSameBlockState(
						Blocks.AIR.defaultBlockState(),
						slab
				) == false,
				"a missing block must not count as correct"
		);
	}

	private static VoxelShape torchShape()
	{
		return Shapes.box(0.4D, 0.0D, 0.4D, 0.6D, 0.625D, 0.6D);
	}
}

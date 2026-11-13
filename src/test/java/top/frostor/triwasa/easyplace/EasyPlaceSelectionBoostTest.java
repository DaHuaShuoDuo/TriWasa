package top.frostor.triwasa.easyplace;

import net.minecraft.core.Direction;
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
		return test.checks();
	}

	private void checkDisabledScopeKeepsShape()
	{
		VoxelShape torch = torchShape();
		check(
				SchematicSelectionBoost.expandShape(torch, false) == torch,
				"disabled boost must keep the real outline shape"
		);
	}

	private void checkEmptyShapeStaysEmpty()
	{
		VoxelShape expanded = SchematicSelectionBoost.expandShape(Shapes.empty(), true);
		check(
				expanded.isEmpty(),
				"positions without an outline must not become click targets"
		);
	}

	private void checkIncompleteShapeBecomesBlock()
	{
		VoxelShape torch = torchShape();
		VoxelShape expanded = SchematicSelectionBoost.expandShape(torch, true);
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
		VoxelShape expanded = SchematicSelectionBoost.expandShape(Shapes.block(), true);
		check(
				expanded == Shapes.block(),
				"full blocks must keep the plain full block shape"
		);
	}

	private static VoxelShape torchShape()
	{
		return Shapes.box(0.4D, 0.0D, 0.4D, 0.6D, 0.625D, 0.6D);
	}
}

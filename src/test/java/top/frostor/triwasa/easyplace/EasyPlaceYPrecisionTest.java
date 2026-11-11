package top.frostor.triwasa.easyplace;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.phys.Vec3;
import top.frostor.triwasa.easyplace.adapter.directional.StairStateAdapter;
import top.frostor.triwasa.easyplace.adapter.directional.TrapDoorStateAdapter;
import top.frostor.triwasa.easyplace.registry.EasyPlaceCodecContext;

public final class EasyPlaceYPrecisionTest extends EasyPlaceTestSupport
{
	private EasyPlaceYPrecisionTest()
	{
	}

	public static int run()
	{
		EasyPlaceYPrecisionTest test = new EasyPlaceYPrecisionTest();
		test.checkQuantization();
		test.checkWireRoundTrip();
		test.checkRestoredStates();
		return test.checks();
	}

	private void checkQuantization()
	{
		for(int precisionValue = 0;
			precisionValue < EasyPlaceExtraProtocol.Y_PRECISION_SCALE;
			precisionValue++)
		{
			double restored = EasyPlaceExtraProtocol.restoreRelativeY(precisionValue);
			check(
					restored >= 0.0 && restored <= 1.0,
					"restored Y precision must stay inside the clicked block"
			);
			check(
					(restored > 0.5)
							== (precisionValue >= EasyPlaceExtraProtocol.Y_PRECISION_SCALE / 2),
					"restored Y precision must preserve the half boundary"
			);
		}

		double[] samples = {
				0.0,
				0.25,
				0.499,
				0.5,
				Math.nextUp(0.5),
				0.501,
				0.75,
				0.999,
				1.0
		};
		for(double sample : samples)
		{
			int precisionValue = EasyPlaceExtraProtocol.quantizeRelativeY(sample);
			double restored = EasyPlaceExtraProtocol.restoreRelativeY(precisionValue);
			check(
					(sample > 0.5) == (restored > 0.5),
					"eight-bit Y must preserve the placement threshold at " + sample
			);
		}
	}

	private void checkWireRoundTrip()
	{
		BlockPos pos = new BlockPos(-17, 70, 31);
		Vec3 original = new Vec3(2.5, 70.73, 6.5);
		Vec3 encoded = EasyPlaceExtraProtocol.encodeY(pos, original, 0x5a);
		int payload = EasyPlaceExtraProtocol.decodeYPayload(encoded);
		int precisionValue = payload & EasyPlaceExtraProtocol.Y_PRECISION_MASK;

		check(
				encoded.x == original.x && encoded.z == original.z,
				"Y encoding must preserve X and Z"
		);
		check(
				EasyPlaceExtraProtocol.rawValueFromYPayload(payload) == 0x5a,
				"raw must travel independently of the placement target"
		);
		check(
				precisionValue == EasyPlaceExtraProtocol.quantizeRelativeY(0.73),
				"Y precision value must round trip through the wire encoding"
		);
		check(
				Math.abs(
						EasyPlaceExtraProtocol.restoreRelativeY(precisionValue) - 0.73
				) < 1.0 / EasyPlaceExtraProtocol.Y_PRECISION_SCALE,
				"restored Y must stay within one eight-bit step"
		);
	}

	private void checkRestoredStates()
	{
		BlockState stairBottom = new StairStateAdapter().decode(codecContext(
				Blocks.STONE_STAIRS.defaultBlockState(),
				0
		));
		BlockState stairTop = new StairStateAdapter().decode(codecContext(
				Blocks.STONE_STAIRS.defaultBlockState(),
				0x4
		));
		check(
				stairBottom != null && stairBottom.getValue(StairBlock.HALF) == Half.BOTTOM,
				"raw stair state must keep the bottom half independent of Y precision"
		);
		check(
				stairTop != null && stairTop.getValue(StairBlock.HALF) == Half.TOP,
				"raw stair state must keep the top half independent of Y precision"
		);

		BlockState trapdoorTopOpen = new TrapDoorStateAdapter().decode(codecContext(
				Blocks.OAK_TRAPDOOR.defaultBlockState(),
				0xC
		));
		check(
				trapdoorTopOpen != null
						&& trapdoorTopOpen.getValue(TrapDoorBlock.HALF) == Half.TOP
						&& trapdoorTopOpen.getValue(TrapDoorBlock.OPEN),
				"raw trapdoor state must preserve half and open independent of Y precision"
		);
	}

	private EasyPlaceCodecContext codecContext(BlockState state, int rawValue)
	{
		return new EasyPlaceCodecContext(
				state,
				null,
				null,
				null,
				null,
				rawValue,
				0
		);
	}
}

package top.frostor.triwasa.easyplace;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ObserverBlock;
import net.minecraft.world.level.block.state.BlockState;
import top.frostor.triwasa.config.TriWasaConfigs;
import top.frostor.triwasa.easyplace.registry.EasyPlaceEncodedState;

public final class EasyPlaceObserverStateTest extends EasyPlaceTestSupport
{
	private EasyPlaceObserverStateTest()
	{
	}

	public static int run()
	{
		EasyPlaceObserverStateTest test = new EasyPlaceObserverStateTest();
		test.checkFacingRoundTrip();
		test.checkInvalidRawValues();
		test.checkDisabledToggleUsesVanillaPath();
		return test.checks();
	}

	private void checkFacingRoundTrip()
	{
		for(Direction direction : Direction.values())
		{
			BlockState projected = Blocks.OBSERVER.defaultBlockState()
					.setValue(ObserverBlock.FACING, direction)
					.setValue(ObserverBlock.POWERED, true);
			int encoded = EasyPlaceStateCodec.encode(projected);
			check(
					encoded == direction.get3DDataValue(),
					"observer encoding must preserve facing and ignore powered"
			);

			BlockState restored = EasyPlaceStateCodec.apply(
					Blocks.OBSERVER,
					Blocks.OBSERVER.defaultBlockState().setValue(ObserverBlock.POWERED, true),
					null,
					encoded
			);
			check(
					restored != null && restored.getValue(ObserverBlock.FACING) == direction,
					"observer facing must round trip"
			);
			check(
					restored != null && !restored.getValue(ObserverBlock.POWERED),
					"observer decoding must force powered false"
			);
		}
	}

	private void checkInvalidRawValues()
	{
		for(int rawValue = Direction.values().length; rawValue <= 0x7f; rawValue++)
		{
			check(
					EasyPlaceStateCodec.apply(
							Blocks.OBSERVER,
							Blocks.OBSERVER.defaultBlockState(),
							null,
							rawValue
					) == null,
					"observer raw value " + rawValue + " must be rejected"
			);
		}
	}

	private void checkDisabledToggleUsesVanillaPath()
	{
		boolean previous = TriWasaConfigs.EASY_PLACE_OBSERVER_PROTECTION.getBooleanValue();
		boolean rail = TriWasaConfigs.EASY_PLACE_RAIL_BLOCK_NO_SHAPE_UPDATE.getBooleanValue();
		boolean wall = TriWasaConfigs.EASY_PLACE_WALL_BLOCK_NO_SHAPE_UPDATE.getBooleanValue();
		try
		{
			TriWasaConfigs.EASY_PLACE_OBSERVER_PROTECTION.setBooleanValue(false);
			EasyPlaceEncodedState encoded = EasyPlaceStateCodec.encodeState(
					Blocks.OBSERVER.defaultBlockState().setValue(ObserverBlock.POWERED, true)
			);
			check(
					encoded.rawValue() == -1 && encoded.auxiliaryValue() == 0,
					"disabling observer protection must disable projected observer encoding"
			);
			check(
					EasyPlaceUpdateFlags.current() == EasyPlaceUpdateFlags.encode(rail, wall, false),
					"disabling observer protection must remove only the observer flag"
			);
		}
		finally
		{
			TriWasaConfigs.EASY_PLACE_OBSERVER_PROTECTION.setBooleanValue(previous);
		}
	}
}

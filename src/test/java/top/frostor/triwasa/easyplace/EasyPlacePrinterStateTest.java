package top.frostor.triwasa.easyplace;

import fi.dy.masa.litematica.util.EasyPlaceUtils;
import net.minecraft.world.level.block.Blocks;
import top.frostor.triwasa.betterprinter.BetterPrinterGuideRules;
import top.frostor.triwasa.config.TriWasaConfigs;

public final class EasyPlacePrinterStateTest extends EasyPlaceTestSupport
{
	private EasyPlacePrinterStateTest()
	{
	}

	public static int run()
	{
		EasyPlacePrinterStateTest test = new EasyPlacePrinterStateTest();
		test.checkForcedPlacementScope();
		test.checkPrinterMultifaceGuideMatching();
		return test.checks();
	}

	private void checkForcedPlacementScope()
	{
		boolean easyPlace = TriWasaConfigs.BETTER_EASY_PLACE.getBooleanValue();
		TriWasaConfigs.BETTER_EASY_PLACE.setBooleanValue(true);
		EasyPlaceUtils.setHandling(false);

		try
		{
			check(
					!CandleRules.shouldAllowUnsupportedPlacement(Blocks.CANDLE.defaultBlockState()),
					"normal candle placement must retain vanilla support checks"
			);
			check(
					!HangingSignRules.shouldAllowUnsupportedWallPlacement(
							Blocks.OAK_WALL_HANGING_SIGN.defaultBlockState()
					),
					"normal wall hanging sign placement must retain vanilla support checks"
			);
			check(
					!HangingSignRules.shouldSuppressEasyPlaceChaining(
							Blocks.OAK_WALL_HANGING_SIGN.defaultBlockState(),
							Blocks.OAK_HANGING_SIGN.asItem()
					),
					"normal wall hanging sign interactions must retain vanilla chaining"
			);

			EasyPlaceUtils.setHandling(true);

			check(
					CandleRules.shouldAllowUnsupportedPlacement(Blocks.CANDLE.defaultBlockState()),
					"easy-place and printer candle interactions must allow unsupported placement"
			);
			check(
					HangingSignRules.shouldAllowUnsupportedWallPlacement(
							Blocks.OAK_WALL_HANGING_SIGN.defaultBlockState()
					),
					"easy-place and printer wall sign interactions must allow unsupported placement"
			);
			check(
					HangingSignRules.shouldSuppressEasyPlaceChaining(
							Blocks.OAK_WALL_HANGING_SIGN.defaultBlockState(),
							Blocks.OAK_HANGING_SIGN.asItem()
					),
					"easy-place wall sign interactions must not chain another sign above"
			);
		}
		finally
		{
			EasyPlaceUtils.setHandling(false);
			TriWasaConfigs.BETTER_EASY_PLACE.setBooleanValue(easyPlace);
		}
	}

	private void checkPrinterMultifaceGuideMatching()
	{
		boolean easyPlace = TriWasaConfigs.BETTER_EASY_PLACE.getBooleanValue();
		boolean printer = TriWasaConfigs.BETTER_PRINTER.getBooleanValue();
		try
		{
			TriWasaConfigs.BETTER_EASY_PLACE.setBooleanValue(true);
			TriWasaConfigs.BETTER_PRINTER.setBooleanValue(true);
			Class<?>[] vineGuideBlocks = {
					net.minecraft.world.level.block.VineBlock.class,
					net.minecraft.world.level.block.GlowLichenBlock.class
			};
			Class<?>[] flowerBedGuideBlocks = {
					net.minecraft.world.level.block.FlowerBedBlock.class
			};
			check(
					BetterPrinterGuideRules.shouldMatchMultifaceGuide(
							Blocks.GLOW_LICHEN,
							vineGuideBlocks
					),
					"glow lichen must retain printer face guidance"
			);
			check(
					BetterPrinterGuideRules.shouldMatchMultifaceGuide(
							Blocks.SCULK_VEIN,
							vineGuideBlocks
					),
					"sculk veins must reuse printer multiface guidance"
			);
			check(
					BetterPrinterGuideRules.shouldMatchMultifaceGuide(
							Blocks.RESIN_CLUMP,
							vineGuideBlocks
					),
					"resin clumps must reuse printer multiface guidance"
			);
			check(
					BetterPrinterGuideRules.shouldMatchLeafLitterGuide(
							Blocks.LEAF_LITTER,
							flowerBedGuideBlocks
					),
					"leaf litter must reuse the printer flower-bed quantity guide"
			);
			TriWasaConfigs.BETTER_PRINTER.setBooleanValue(false);
			check(
					!BetterPrinterGuideRules.shouldMatchMultifaceGuide(
							Blocks.SCULK_VEIN,
							vineGuideBlocks
					) && !BetterPrinterGuideRules.shouldMatchMultifaceGuide(
							Blocks.RESIN_CLUMP,
							vineGuideBlocks
					),
					"printer multiface extension must be config-gated"
			);
			check(
					!BetterPrinterGuideRules.shouldMatchLeafLitterGuide(
							Blocks.LEAF_LITTER,
							flowerBedGuideBlocks
					),
					"printer leaf litter extension must be config-gated"
			);
			check(
					!BetterPrinterGuideRules.shouldMatchMultifaceGuide(
							Blocks.CANDLE,
							vineGuideBlocks
					),
					"non-multiface blocks must not reuse printer multiface guidance"
			);
		}
		finally
		{
			TriWasaConfigs.BETTER_EASY_PLACE.setBooleanValue(easyPlace);
			TriWasaConfigs.BETTER_PRINTER.setBooleanValue(printer);
		}
	}
}

package top.frostor.triwasa.betterprinter;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FlowerBedBlock;
import net.minecraft.world.level.block.GlowLichenBlock;
import net.minecraft.world.level.block.LeafLitterBlock;
import net.minecraft.world.level.block.MultifaceBlock;
import top.frostor.triwasa.easyplace.EasyPlaceRules;

public final class BetterPrinterGuideRules
{
	private BetterPrinterGuideRules()
	{
	}

	public static boolean shouldMatchMultifaceGuide(Block block, Class<?>[] supportedBlocks)
	{
		if(!EasyPlaceRules.isPrinterEnabled() || !(block instanceof MultifaceBlock))
		{
			return false;
		}

		for(Class<?> supportedBlock : supportedBlocks)
		{
			if(supportedBlock == GlowLichenBlock.class)
			{
				return true;
			}
		}
		return false;
	}

	public static boolean shouldMatchLeafLitterGuide(Block block, Class<?>[] supportedBlocks)
	{
		if(!EasyPlaceRules.isPrinterEnabled() || !(block instanceof LeafLitterBlock))
		{
			return false;
		}

		for(Class<?> supportedBlock : supportedBlocks)
		{
			if(supportedBlock == FlowerBedBlock.class)
			{
				return true;
			}
		}
		return false;
	}
}

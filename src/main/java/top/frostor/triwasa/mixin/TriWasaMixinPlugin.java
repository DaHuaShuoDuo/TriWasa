package top.frostor.triwasa.mixin;

import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public final class TriWasaMixinPlugin implements IMixinConfigPlugin
{
	@Override
	public void onLoad(String mixinPackage)
	{
	}

	@Override
	public String getRefMapperConfig()
	{
		return null;
	}

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName)
	{
		if(mixinClassName.endsWith(".LivingEntitySwingMixin26_2"))
		{
			return isMinecraftVersion("26.2");
		}
		if(mixinClassName.endsWith(".LivingEntitySwingMixin26_3"))
		{
			return isMinecraftVersion("26.3");
		}
		if(mixinClassName.endsWith(".LitematicaPrinterMixin"))
		{
			return FabricLoader.getInstance().isModLoaded("litematica-printer");
		}
		if(mixinClassName.endsWith(".rei.ReiCraftableFilterIconMixin")
				|| mixinClassName.endsWith(".rei.ReiGuiGraphicsMixin"))
		{
			return FabricLoader.getInstance().isModLoaded("roughlyenoughitems");
		}
		return true;
	}

	private static boolean isMinecraftVersion(String version)
	{
		return FabricLoader.getInstance()
				.getModContainer("minecraft")
				.map(container -> container.getMetadata().getVersion().getFriendlyString())
				.filter(version::equals)
				.isPresent();
	}

	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets)
	{
	}

	@Override
	public List<String> getMixins()
	{
		return null;
	}

	@Override
	public void preApply(
			String targetClassName,
			ClassNode targetClass,
			String mixinClassName,
			IMixinInfo mixinInfo
	)
	{
	}

	@Override
	public void postApply(
			String targetClassName,
			ClassNode targetClass,
			String mixinClassName,
			IMixinInfo mixinInfo
	)
	{
	}
}

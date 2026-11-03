package top.frostor.triwasa.mixin;

import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Allows the terminal to reposition the real player inventory slot objects
 * without replacing them, so vanilla hit-testing and server synchronisation
 * keep using the exact same Slot instances.
 */
@Mixin(Slot.class)
public interface SlotPositionAccessor
{
	@Accessor("x")
	@Mutable
	void triwasa$setX(int value);

	@Accessor("y")
	@Mutable
	void triwasa$setY(int value);
}

package top.frostor.triwasa.bigenderchest.menu;

import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import top.frostor.triwasa.bigenderchest.protocol.BigEnderChestProtocol;

/**
 * Client compatibility menu used when FCA itself is not installed on this client.
 */
public final class BigEnderChestTerminalMenu extends AbstractContainerMenu
{
	public static final int PLAYER_SLOT_COUNT = BigEnderChestProtocol.PLAYER_SLOT_COUNT;
	private static MenuType<BigEnderChestTerminalMenu> ownType;

	public BigEnderChestTerminalMenu(
			int syncId,
			Inventory inventory,
			MenuType<? extends AbstractContainerMenu> menuType
	)
	{
		super(menuType, syncId);
		addStandardInventorySlots(inventory, 8, 84);
	}

	public static synchronized MenuType<BigEnderChestTerminalMenu> createMenuType()
	{
		if(ownType != null)
		{
			return ownType;
		}
		ownType = Registry.register(
				BuiltInRegistries.MENU,
				BigEnderChestProtocol.TERMINAL_MENU_ID,
				new ExtendedMenuType<BigEnderChestTerminalMenu, Unit>(
						(syncId, inventory, ignored) ->
								new BigEnderChestTerminalMenu(
										syncId,
										inventory,
										ownType
								),
						StreamCodec.unit(Unit.INSTANCE)
				)
		);
		return ownType;
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index)
	{
		return ItemStack.EMPTY;
	}

	@Override
	public boolean stillValid(Player player)
	{
		return true;
	}
}

package top.frostor.triwasa.bigenderchest.client;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.frostor.triwasa.bigenderchest.protocol.BigEnderChestMessages;

final class BigEnderChestHoverResolver
{
	private BigEnderChestHoverResolver()
	{
	}

	static BigEnderChestMessages.EntryRecord entryAt(
			BigEnderChestViewController view,
			BigEnderChestLayout.Computed computed,
			double mouseX,
			double mouseY
	)
	{
		for(BigEnderChestLayout.SlotHitbox hitbox : computed.itemSlots())
		{
			if(hitbox.bounds().contains(mouseX, mouseY))
			{
				return view.entryAtSlot(hitbox.index());
			}
		}
		return null;
	}

	static String sideButtonAt(
			BigEnderChestLayout.Computed computed,
			double mouseX,
			double mouseY
	)
	{
		for(BigEnderChestLayout.SideButtonBounds button : computed.sideButtons())
		{
			if(button.bounds().contains(mouseX, mouseY))
			{
				return button.id();
			}
		}
		return null;
	}

	static boolean insideItemGrid(
			BigEnderChestLayout.Computed computed,
			double mouseX,
			double mouseY
	)
	{
		BigEnderChestLayout.Bounds itemSlot = computed.bounds("itemSlot");
		return mouseX >= itemSlot.x()
				&& mouseY >= itemSlot.y()
				&& mouseX < itemSlot.x() + 9 * 18
				&& mouseY < itemSlot.y() + computed.rowCount() * 18;
	}

	static boolean insideAnySearchPart(
			BigEnderChestLayout.Computed computed,
			double mouseX,
			double mouseY
	)
	{
		return computed.bounds("searchHead").contains(mouseX, mouseY)
				|| computed.bounds("searchMiddle").contains(mouseX, mouseY)
				|| computed.bounds("searchTail").contains(mouseX, mouseY);
	}

	static ItemStack equipmentAt(
			BigEnderChestLayout.Computed computed,
			Player player,
			double mouseX,
			double mouseY
	)
	{
		if(computed.bounds("armorHelmetSlot") == null)
		{
			return ItemStack.EMPTY;
		}
		if(computed.bounds("armorHelmetSlot").contains(mouseX, mouseY))
		{
			return player.getItemBySlot(EquipmentSlot.HEAD);
		}
		if(computed.bounds("armorChestSlot").contains(mouseX, mouseY))
		{
			return player.getItemBySlot(EquipmentSlot.CHEST);
		}
		if(computed.bounds("armorLegsSlot").contains(mouseX, mouseY))
		{
			return player.getItemBySlot(EquipmentSlot.LEGS);
		}
		if(computed.bounds("armorBootsSlot").contains(mouseX, mouseY))
		{
			return player.getItemBySlot(EquipmentSlot.FEET);
		}
		if(computed.bounds("offhandSlot").contains(mouseX, mouseY))
		{
			return player.getItemBySlot(EquipmentSlot.OFFHAND);
		}
		return ItemStack.EMPTY;
	}

	static String craftingRegionAt(
			BigEnderChestLayout.Computed computed,
			double mouseX,
			double mouseY
	)
	{
		if(computed.bounds("craftingMatrixSlots") == null)
		{
			return null;
		}
		if(computed.bounds("craftingMatrixSlots").contains(mouseX, mouseY))
		{
			return "craftingMatrixSlots";
		}
		if(computed.bounds("craftingResultSlot").contains(mouseX, mouseY))
		{
			return "craftingResultSlot";
		}
		return null;
	}
}

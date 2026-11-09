package top.frostor.triwasa.easyplace;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import top.frostor.triwasa.TriWasa;

public final class EasyPlaceClientEncoder
{
	private EasyPlaceClientEncoder()
	{
	}

	public static @Nullable Vec3 encode(BlockPos pos, BlockState state, Vec3 hitVec)
	{
		boolean active = EasyPlaceClientNetwork.isActive();
		TriWasa.LOGGER.debug(
				"[EasyPlace] Encoding client protocol at {} state={} active={}",
				pos,
				state,
				active
		);
		if(!active)
		{
			return null;
		}

		Minecraft client = Minecraft.getInstance();
		BlockPos protocolPos = client.level == null
				? pos
				: BellRules.resolveProtocolTarget(client.level, pos, state);
		BlockState currentState = client.level == null ? null : client.level.getBlockState(protocolPos);
		int rawValue = EasyPlaceStateCodec.encode(state, currentState, client.level, protocolPos);
		if(rawValue < 0 || rawValue > 0x7f)
		{
			return null;
		}
		TriWasa.LOGGER.debug("[EasyPlace] Encoded raw protocol 0x{} at {}", Integer.toHexString(rawValue), pos);

		EasyPlaceCrafterSlotLock.capture(protocolPos, state);
		Vec3 encodedHitVec = EasyPlaceExtraProtocol.encodeX(protocolPos, hitVec, rawValue);
		int crafterDisabledMask = EasyPlaceCrafterSlotLock.getPendingMask();
		if(crafterDisabledMask != 0)
		{
			encodedHitVec = EasyPlaceExtraProtocol.encodeZ(pos, encodedHitVec, crafterDisabledMask);
		}
		return encodedHitVec;
	}
}

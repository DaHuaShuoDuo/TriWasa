package top.frostor.triwasa.easyplace;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import fi.dy.masa.litematica.world.SchematicWorldHandler;
import top.frostor.triwasa.TriWasa;
import top.frostor.triwasa.config.TriWasaConfigs;
import top.frostor.triwasa.easyplace.registry.EasyPlaceEncodedState;

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
		EasyPlaceEncodedState encodedState = EasyPlaceStateCodec.encodeState(
				state,
				currentState,
				client.level,
				protocolPos
		);
		int rawValue = encodedState.rawValue();
		if(rawValue < 0 || rawValue > 0x7f)
		{
			return null;
		}
		TriWasa.LOGGER.debug("[EasyPlace] Encoded raw protocol 0x{} at {}", Integer.toHexString(rawValue), pos);

		EasyPlaceCrafterSlotLock.capture(protocolPos, state);
		// Wire values are always anchored at the block position that will be sent in
		// BlockHitResult. protocolPos is only used above to read projected/current state:
		// Litematica 0.28/0.29 and bell placement plans resolve it differently, and the
		// network codec stores the hit location as a float offset relative to blockPos.
		int crafterDisabledMask = EasyPlaceCrafterSlotLock.getPendingMask();
		int auxiliaryValue = encodedState.auxiliaryValue() | EasyPlaceUpdateFlags.current();
		if(TriWasaConfigs.EASY_PLACE_WALL_BLOCK_NO_SHAPE_UPDATE.getBooleanValue())
		{
			Level schematicWorld = SchematicWorldHandler.getSchematicWorld();
			int wallNeighborMatchMask = EasyPlaceWallProtection.neighborMatchMask(
					client.level == null ? null : client.level::getBlockState,
					schematicWorld == null ? null : schematicWorld::getBlockState,
					protocolPos
			);
			auxiliaryValue = EasyPlaceWallProtection.applyNeighborMatchMask(
					auxiliaryValue,
					wallNeighborMatchMask
			);
		}
		if(TriWasaConfigs.EASY_PLACE_RAIL_PROTECTION.getBooleanValue())
		{
			Level schematicWorld = SchematicWorldHandler.getSchematicWorld();
			int railNeighborMatchMask = EasyPlaceRailProtection.neighborMatchMask(
					client.level == null ? null : client.level::getBlockState,
					schematicWorld == null ? null : schematicWorld::getBlockState,
					protocolPos
			);
			if(railNeighborMatchMask != 0
					|| state.getBlock() instanceof net.minecraft.world.level.block.BaseRailBlock)
			{
				TriWasa.LOGGER.info(
						"[EasyPlace-Rail] client target={} state={} auxiliary=0x{} mask=0x{}",
						protocolPos,
						state,
						Integer.toHexString(auxiliaryValue),
						Integer.toHexString(railNeighborMatchMask)
				);
			}
			auxiliaryValue = EasyPlaceRailProtection.applyNeighborMatchMask(
					auxiliaryValue,
					railNeighborMatchMask
			);
		}
		if(crafterDisabledMask != 0)
		{
			auxiliaryValue = (auxiliaryValue & ~EasyPlaceExtraProtocol.CRAFTER_SLOT_MASK)
					| crafterDisabledMask;
		}
		int wireAuxiliaryValue = auxiliaryValue == 0
				? 0
				: auxiliaryValue | EasyPlaceExtraProtocol.AUXILIARY_SCHEMA;
		boolean relativeAux = EasyPlaceClientNetwork.supportsRelativeAux();
		int auxiliaryHigh = relativeAux
				? EasyPlaceExtraProtocol.splitAuxiliaryHigh(wireAuxiliaryValue)
				: 0;
		Vec3 encodedHitVec = EasyPlaceExtraProtocol.encodeX(
				pos,
				hitVec,
				rawValue,
				auxiliaryHigh
		);
		if(relativeAux)
		{
			if(wireAuxiliaryValue != 0)
			{
				encodedHitVec = EasyPlaceExtraProtocol.encodeRelativeAuxiliary(
						pos,
						encodedHitVec,
						wireAuxiliaryValue
				);
			}
		}
		else if(wireAuxiliaryValue != 0)
		{
			encodedHitVec = EasyPlaceExtraProtocol.encodeAuxiliary(
					encodedHitVec,
					wireAuxiliaryValue
			);
		}
		int pistonProtectionMask = 0;
		if(EasyPlaceClientNetwork.supportsPistonProtection()
				&& EasyPlacePistonProtection.isEnabled())
		{
			Level schematicWorld = SchematicWorldHandler.getSchematicWorld();
			pistonProtectionMask = EasyPlacePistonProtection.protectionMask(
					state,
					client.level == null ? null : client.level::getBlockState,
					schematicWorld == null ? null : schematicWorld::getBlockState,
					protocolPos
			);
		}
		if(EasyPlaceClientNetwork.supportsYPrecision())
		{
			encodedHitVec = EasyPlaceExtraProtocol.encodeY(
					pos,
					encodedHitVec,
					pistonProtectionMask
			);
		}
		return encodedHitVec;
	}
}

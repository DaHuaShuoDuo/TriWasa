package top.frostor.triwasa.verifier;

import fi.dy.masa.litematica.schematic.verifier.SchematicVerifier;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

import top.frostor.triwasa.mixin.SchematicVerifierPickerInvoker;

/**
 * Rebuilds the mismatch overlays of every live verifier after the position picker switch changed,
 * so disabling the switch drops pinned highlights immediately instead of on the next verifier
 * interaction.
 */
public final class VerifierOverlayRefresh
{
	private static final Set<SchematicVerifier> VERIFIERS =
			Collections.newSetFromMap(new WeakHashMap<>());

	private VerifierOverlayRefresh()
	{
	}

	public static void register(SchematicVerifier verifier)
	{
		VERIFIERS.add(verifier);
	}

	public static void refreshAll()
	{
		for(SchematicVerifier verifier : List.copyOf(VERIFIERS))
		{
			((SchematicVerifierPickerInvoker) verifier).triwasa$invokeUpdateMismatchOverlays();
		}
	}
}

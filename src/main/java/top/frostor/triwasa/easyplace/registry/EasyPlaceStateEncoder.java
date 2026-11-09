package top.frostor.triwasa.easyplace.registry;

@FunctionalInterface
public interface EasyPlaceStateEncoder
{
	int encode(EasyPlaceCodecContext context);

	default int encodeAuxiliary(EasyPlaceCodecContext context)
	{
		return 0;
	}
}

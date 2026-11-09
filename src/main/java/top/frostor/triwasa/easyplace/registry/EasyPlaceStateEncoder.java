package top.frostor.triwasa.easyplace.registry;

@FunctionalInterface
public interface EasyPlaceStateEncoder
{
	int encode(EasyPlaceCodecContext context);
}

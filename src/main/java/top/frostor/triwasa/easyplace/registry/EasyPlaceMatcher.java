package top.frostor.triwasa.easyplace.registry;

@FunctionalInterface
public interface EasyPlaceMatcher
{
	boolean matches(EasyPlaceCodecContext context);
}

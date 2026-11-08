package top.frostor.triwasa.betterprinter;

import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class BetterPrinterIncrementalPlacement
{
	private static final ThreadLocal<BlockPos> RETRY_POSITION = new ThreadLocal<>();
	private static final Method TASK_EVENT = findMethod(
			"me.aleksilassila.litematica.printer.handler.handlers.print.PrintPlacementResult",
			"taskEvent"
	);
	private static final Class<?> RESULT_CLASS = TASK_EVENT.getDeclaringClass();
	private static final Method DEFERRED_RESULT = findMethod(
			RESULT_CLASS,
			"deferred",
			boolean.class
	);
	private static final Method ON_RESULT = findMethod(
			"me.aleksilassila.litematica.printer.handler.handlers.print.PrintTargetScheduler",
			"onResult",
			BlockPos.class,
			RESULT_CLASS,
			boolean.class
	);

	private BetterPrinterIncrementalPlacement()
	{
	}

	public static void clear()
	{
		RETRY_POSITION.remove();
	}

	public static void mark(BlockPos pos)
	{
		RETRY_POSITION.set(pos.immutable());
	}

	public static @Nullable BlockPos take()
	{
		BlockPos pos = RETRY_POSITION.get();
		RETRY_POSITION.remove();
		return pos;
	}

	public static boolean isSuccess(Object result)
	{
		if(result == null)
		{
			return false;
		}

		try
		{
			Object taskEvent = TASK_EVENT.invoke(result);
			return taskEvent instanceof Enum<?> event && event.name().equals("SUCCESS");
		}
		catch(IllegalAccessException | InvocationTargetException exception)
		{
			throw new IllegalStateException("Litematica Printer placement result API changed", exception);
		}
	}

	public static void requeueThroughScheduler(Object scheduler, BlockPos pos)
	{
		try
		{
			Object deferredResult = DEFERRED_RESULT.invoke(null, false);
			ON_RESULT.invoke(scheduler, pos, deferredResult, false);
		}
		catch(IllegalAccessException | InvocationTargetException exception)
		{
			throw new IllegalStateException("Litematica Printer target scheduler API changed", exception);
		}
	}

	private static Method findMethod(String className, String methodName, Class<?>... parameterTypes)
	{
		try
		{
			return findMethod(Class.forName(className), methodName, parameterTypes);
		}
		catch(ClassNotFoundException exception)
		{
			throw new IllegalStateException("Litematica Printer API changed", exception);
		}
	}

	private static Method findMethod(Class<?> targetClass, String methodName, Class<?>... parameterTypes)
	{
		try
		{
			return targetClass.getMethod(methodName, parameterTypes);
		}
		catch(NoSuchMethodException exception)
		{
			throw new IllegalStateException("Litematica Printer API changed", exception);
		}
	}
}

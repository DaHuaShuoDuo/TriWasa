package top.frostor.triwasa.bigenderchest;

import top.frostor.triwasa.bigenderchest.network.BigEnderChestActionDispatcher;
import top.frostor.triwasa.bigenderchest.network.BigEnderChestSessionState;
import top.frostor.triwasa.bigenderchest.protocol.BigEnderChestCapabilities;
import top.frostor.triwasa.bigenderchest.protocol.BigEnderChestMessages;
import top.frostor.triwasa.bigenderchest.protocol.BigEnderChestProtocol;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class BigEnderChestActionQueueTest
{
	private static int checks;

	private BigEnderChestActionQueueTest()
	{
	}

	public static void run()
	{
		BigEnderChestSessionState state = readyState();
		List<BigEnderChestMessages.ActionRequest> sent = new ArrayList<>();
		List<String> resyncReasons = new ArrayList<>();
		BigEnderChestActionDispatcher dispatcher =
				new BigEnderChestActionDispatcher(
						state,
						sent::add,
						status -> {
						},
						resyncReasons::add
				);
		for(int index = 0; index < 10; index++)
		{
			check(
					dispatcher.enqueue(BigEnderChestActionDispatcher.ActionIntent.entry(
							BigEnderChestProtocol.ACTION_TAKE_STACK,
							index + 1,
							0
					)),
					"contract actions must enter the queue"
			);
		}
		dispatcher.tick();
		check(
				sent.size() == BigEnderChestActionDispatcher.MAX_ACTIONS_PER_TICK,
				"no more than eight actions may be sent in one client tick"
		);
		Set<Long> requestIds = new HashSet<>();
		long previousSequence = 0;
		for(BigEnderChestMessages.ActionRequest request : sent)
		{
			check(request.requestId() > 0, "request ids must be non-zero");
			check(requestIds.add(request.requestId()), "request ids must be unique");
			check(
					request.actionSequence() > previousSequence,
					"action sequence must be strictly increasing"
			);
			check(
					request.knownRevision() == state.revision(),
					"actions must send the current known revision"
			);
			previousSequence = request.actionSequence();
			dispatcher.onResult(new BigEnderChestMessages.ActionResult(
					request.sessionId(),
					request.requestId(),
					BigEnderChestProtocol.RESULT_OK,
					state.revision()
			));
		}
		dispatcher.tick();
		check(sent.size() == 10, "the remaining two actions must send on the next tick");

		BigEnderChestSessionState retryState = readyState();
		List<BigEnderChestMessages.ActionRequest> retried = new ArrayList<>();
		BigEnderChestActionDispatcher retryDispatcher =
				new BigEnderChestActionDispatcher(
						retryState,
						retried::add,
						status -> {
						},
						resyncReasons::add
				);
		retryDispatcher.enqueue(BigEnderChestActionDispatcher.ActionIntent.entry(
				BigEnderChestProtocol.ACTION_DROP_ONE,
				1,
				0
		));
		retryDispatcher.tick();
		BigEnderChestMessages.ActionRequest firstAttempt = retried.getFirst();
		retryDispatcher.onResult(new BigEnderChestMessages.ActionResult(
				firstAttempt.sessionId(),
				firstAttempt.requestId(),
				BigEnderChestProtocol.RESULT_RATE_LIMITED,
				retryState.revision()
		));
		retryDispatcher.tick();
		check(retried.size() == 1, "rate-limited actions must wait before retrying");
		retryDispatcher.tick();
		retryDispatcher.tick();
		check(retried.size() == 2, "rate-limited actions must eventually retry");
		check(
				retried.get(1).requestId() != firstAttempt.requestId(),
				"rate-limited retries must use a fresh request id"
		);

		BigEnderChestSessionState staleState = readyState();
		List<BigEnderChestMessages.ActionRequest> staleRequests = new ArrayList<>();
		List<String> staleResyncReasons = new ArrayList<>();
		BigEnderChestActionDispatcher staleDispatcher =
				new BigEnderChestActionDispatcher(
						staleState,
						staleRequests::add,
						status -> {
						},
						staleResyncReasons::add
				);
		staleDispatcher.enqueue(BigEnderChestActionDispatcher.ActionIntent.entry(
				BigEnderChestProtocol.ACTION_TAKE_STACK,
				1,
				0
		));
		staleDispatcher.tick();
		BigEnderChestMessages.ActionRequest staleRequest = staleRequests.getFirst();
		staleDispatcher.onResult(new BigEnderChestMessages.ActionResult(
				staleRequest.sessionId(),
				staleRequest.requestId(),
				BigEnderChestProtocol.RESULT_REVISION_STALE,
				staleState.revision()
		));
		check(
				staleDispatcher.pendingCount() == 0
						&& staleState.state()
						== BigEnderChestSessionState.State.RESYNCING
						&& !staleResyncReasons.isEmpty(),
				"revision-stale results must clear dependent work and request resync"
		);
		System.out.println("BigEnderChestActionQueueTest passed " + checks + " checks");
	}

	private static BigEnderChestSessionState readyState()
	{
		BigEnderChestSessionState state = new BigEnderChestSessionState();
		state.helloPending();
		state.acceptHello(new BigEnderChestMessages.HelloAck(
				BigEnderChestProtocol.VERSION,
				BigEnderChestCapabilities.REQUIRED,
				BigEnderChestProtocol.MAX_CHUNK_BYTES,
				true
		));
		state.openSession(new BigEnderChestMessages.SessionOpen(
				123,
				4,
				9,
				-1,
				1,
				1,
				false,
				BigEnderChestCapabilities.REQUIRED,
				4,
				0
		));
		if(!state.installSnapshot(123, 9, List.of()))
		{
			throw new AssertionError("failed to initialize session test state");
		}
		return state;
	}

	private static void check(boolean condition, String message)
	{
		checks++;
		if(!condition)
		{
			throw new AssertionError(message);
		}
	}
}

package top.frostor.triwasa.bigenderchest.network;

import top.frostor.triwasa.bigenderchest.protocol.BigEnderChestMessages;
import top.frostor.triwasa.bigenderchest.protocol.BigEnderChestProtocol;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * FIFO action queue with the protocol's eight actions per client tick limit.
 */
public final class BigEnderChestActionDispatcher
{
	public static final int MAX_ACTIONS_PER_TICK = 8;
	private static final int RATE_LIMIT_DELAY_TICKS = 2;

	private final BigEnderChestSessionState session;
	private final Consumer<BigEnderChestMessages.ActionRequest> sender;
	private final Consumer<Integer> resultListener;
	private final Consumer<String> resyncListener;
	private final Deque<QueuedAction> queue = new ArrayDeque<>();
	private final Map<Long, QueuedAction> inFlight = new HashMap<>();
	private int redirectDelayTicks;

	public BigEnderChestActionDispatcher(
			BigEnderChestSessionState session,
			Consumer<BigEnderChestMessages.ActionRequest> sender,
			Consumer<Integer> resultListener,
			Consumer<String> resyncListener
	)
	{
		this.session = session;
		this.sender = sender;
		this.resultListener = resultListener;
		this.resyncListener = resyncListener;
	}

	public boolean enqueue(ActionIntent intent)
	{
		if(intent == null
				|| this.queue.size() + this.inFlight.size() >= 256
				|| this.session.sessionId() == 0)
		{
			return false;
		}
		long requestId = this.session.allocateRequestId();
		this.queue.addLast(new QueuedAction(requestId, intent));
		return true;
	}

	public void tick()
	{
		if(this.redirectDelayTicks > 0)
		{
			this.redirectDelayTicks--;
			return;
		}
		if(this.session.state() != BigEnderChestSessionState.State.READY
				|| this.session.sessionId() == 0)
		{
			return;
		}

		int sent = 0;
		while(sent < MAX_ACTIONS_PER_TICK && !this.queue.isEmpty())
		{
			QueuedAction queued = this.queue.removeFirst();
			ActionIntent intent = queued.intent;
			BigEnderChestMessages.ActionRequest request =
					new BigEnderChestMessages.ActionRequest(
							this.session.sessionId(),
							queued.requestId,
							this.session.containerId(),
							this.session.revision(),
							intent.storagePage(),
							intent.slot(),
							intent.templateSerial(),
							intent.action(),
							intent.button(),
							intent.amount(),
							this.session.allocateActionSequence(),
							intent.playerSlots()
					);
			this.inFlight.put(queued.requestId, queued);
			this.sender.accept(request);
			sent++;
		}
	}

	public void onResult(BigEnderChestMessages.ActionResult result)
	{
		if(result.sessionId() != this.session.sessionId())
		{
			return;
		}
		QueuedAction completed = this.inFlight.remove(result.requestId());
		if(completed == null)
		{
			return;
		}
		int status = result.status();
		switch(status)
		{
			case BigEnderChestProtocol.RESULT_OK -> this.resultListener.accept(status);
			case BigEnderChestProtocol.RESULT_DUPLICATE_REQUEST -> {
				// The cached server result is authoritative; never resend the same request.
				this.resultListener.accept(status);
			}
			case BigEnderChestProtocol.RESULT_RATE_LIMITED -> {
				long retryId = this.session.allocateRequestId();
				this.queue.addFirst(new QueuedAction(retryId, completed.intent));
				this.redirectDelayTicks = RATE_LIMIT_DELAY_TICKS;
			}
			case BigEnderChestProtocol.RESULT_REVISION_STALE,
					BigEnderChestProtocol.RESULT_RESYNC_REQUIRED,
					BigEnderChestProtocol.RESULT_INDEX_INVALID -> {
				clear();
				this.session.state(BigEnderChestSessionState.State.RESYNCING);
				this.resyncListener.accept(
						"triwasa.big_ender_chest.error.revision_stale"
				);
			}
			case BigEnderChestProtocol.RESULT_SESSION_EXPIRED -> {
				clear();
				this.session.clearSession();
				this.resultListener.accept(status);
			}
			default -> this.resultListener.accept(status);
		}
	}

	public void clear()
	{
		this.queue.clear();
		this.inFlight.clear();
		this.redirectDelayTicks = 0;
	}

	public int pendingCount()
	{
		return this.queue.size() + this.inFlight.size();
	}

	public record ActionIntent(
			int action,
			long templateSerial,
			long amount,
			byte button,
			int storagePage,
			int slot,
			List<Integer> playerSlots
	)
	{
		public ActionIntent
		{
			playerSlots = playerSlots == null ? List.of() : List.copyOf(playerSlots);
		}

		public static ActionIntent entry(int action, long templateSerial, long amount)
		{
			return new ActionIntent(action, templateSerial, amount, (byte)0, -1, -1, List.of());
		}

		public static ActionIntent player(int action, int inventorySlot)
		{
			return new ActionIntent(
					action,
					0,
					0,
					(byte)inventorySlot,
					-1,
					-1,
					List.of()
			);
		}

		public static ActionIntent playerSlots(List<Integer> inventorySlots)
		{
			Set<Integer> unique = new HashSet<>(inventorySlots);
			if(unique.size() != inventorySlots.size())
			{
				throw new IllegalArgumentException("Player slots must be unique");
			}
			return new ActionIntent(
					BigEnderChestProtocol.ACTION_BATCH_PUT_PLAYER_SLOTS,
					0,
					0,
					(byte)0,
					-1,
					-1,
					unique.stream().sorted().toList()
			);
		}
	}

	private record QueuedAction(long requestId, ActionIntent intent)
	{
	}
}

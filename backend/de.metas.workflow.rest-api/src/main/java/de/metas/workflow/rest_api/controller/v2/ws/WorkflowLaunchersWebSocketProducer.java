package de.metas.workflow.rest_api.controller.v2.ws;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import de.metas.global_qrcodes.GlobalQRCode;
import de.metas.user.UserId;
import de.metas.user.api.IUserBL;
import de.metas.util.Services;
import de.metas.websocket.WebsocketSubscriptionId;
import de.metas.websocket.producers.WebSocketProducer;
import de.metas.workflow.rest_api.controller.v2.json.JsonOpts;
import de.metas.workflow.rest_api.controller.v2.json.JsonWorkflowLaunchersList;
import de.metas.workflow.rest_api.model.MobileApplicationId;
import de.metas.workflow.rest_api.model.WorkflowLaunchersList;
import de.metas.workflow.rest_api.model.WorkflowLaunchersQuery;
import de.metas.workflow.rest_api.model.facets.WorkflowLaunchersFacetId;
import de.metas.workflow.rest_api.service.WorkflowRestAPIService;
import lombok.NonNull;
import org.adempiere.service.ISysConfigBL;
import org.adempiere.util.lang.SynchronizedMutable;

import javax.annotation.Nullable;
import java.time.Duration;
import java.util.List;
import java.util.Set;

class WorkflowLaunchersWebSocketProducer implements WebSocketProducer, WebSocketProducer.ProduceEventsOnPollSupport
{
	private static final String SYSCONFIG_MaxStaleAcceptedInSeconds = "WorkflowLaunchersWebSocketProducer.maxStaleAcceptedInSeconds";
	private static final Duration DEFAULT_MaxStaleAccepted = Duration.ofSeconds(10);

	private final ISysConfigBL sysConfigBL = Services.get(ISysConfigBL.class);
	private final IUserBL userBL = Services.get(IUserBL.class);
	private final WorkflowRestAPIService workflowRestAPIService;

	@NonNull private final MobileApplicationId applicationId;
	@NonNull private final UserId userId;
	@Nullable private final GlobalQRCode filterByQRCode;
	@Nullable private final ImmutableSet<WorkflowLaunchersFacetId> facetIds;

	private final SynchronizedMutable<WorkflowLaunchersList> lastResultHolder = SynchronizedMutable.of(null);

	WorkflowLaunchersWebSocketProducer(
			final @NonNull WorkflowRestAPIService workflowRestAPIService,
			final @NonNull MobileApplicationId applicationId,
			final @NonNull UserId userId,
			final @Nullable GlobalQRCode filterByQRCode,
			final @Nullable Set<WorkflowLaunchersFacetId> facetIds)
	{
		this.workflowRestAPIService = workflowRestAPIService;
		this.applicationId = applicationId;
		this.userId = userId;
		this.filterByQRCode = filterByQRCode;
		this.facetIds = facetIds != null && !facetIds.isEmpty() ? ImmutableSet.copyOf(facetIds) : null;
	}

	@Override
	public void onNewSubscription(final @NonNull WebsocketSubscriptionId subscriptionId)
	{
		lastResultHolder.setValue(null);
	}

	@Override
	public List<?> produceEvents()
	{
		final WorkflowLaunchersList newResult = computeNewResult();
		final WorkflowLaunchersList previousResult = lastResultHolder.setValueAndReturnPrevious(newResult);

		if (previousResult != null && previousResult.equalsIgnoringTimestamp(newResult))
		{
			// nothing changed, don't flood the frontend with same information
			return ImmutableList.of();
		}
		else
		{
			return toJson(newResult);
		}
	}

	@NonNull
	private ImmutableList<JsonWorkflowLaunchersList> toJson(final WorkflowLaunchersList launchers)
	{
		final JsonOpts jsonOpts = newJsonOpts();

		return ImmutableList.of(JsonWorkflowLaunchersList.of(launchers, false, jsonOpts));
	}

	private JsonOpts newJsonOpts()
	{
		return JsonOpts.builder()
				.adLanguage(userBL.getUserLanguage(userId).getAD_Language())
				.build();
	}

	private WorkflowLaunchersList computeNewResult()
	{
		return workflowRestAPIService.getLaunchers(
				WorkflowLaunchersQuery.builder()
						.applicationId(applicationId)
						.userId(userId)
						.filterByQRCode(filterByQRCode)
						.facetIds(facetIds)
						.maxStaleAccepted(getMaxStaleAccepted())
						.build()
		);
	}

	private Duration getMaxStaleAccepted()
	{
		final int durationSec = sysConfigBL.getIntValue(SYSCONFIG_MaxStaleAcceptedInSeconds, (int)DEFAULT_MaxStaleAccepted.getSeconds());
		return Duration.ofSeconds(durationSec);
	}
}

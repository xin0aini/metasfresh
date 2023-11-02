/*
 * #%L
 * de.metas.picking.rest-api
 * %%
 * Copyright (C) 2021 metas GmbH
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */

package de.metas.picking.workflow.handlers;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import de.metas.common.util.time.SystemTime;
import de.metas.document.engine.IDocument;
import de.metas.handlingunits.picking.QtyRejectedReasonCode;
import de.metas.handlingunits.picking.job.model.PickingJob;
import de.metas.handlingunits.picking.job.model.PickingJobId;
import de.metas.handlingunits.picking.job.model.PickingJobLineId;
import de.metas.handlingunits.picking.job.model.PickingJobStepEvent;
import de.metas.handlingunits.picking.job.model.PickingJobStepEventType;
import de.metas.handlingunits.picking.job.model.PickingJobStepId;
import de.metas.handlingunits.picking.job.model.PickingJobStepPickFromKey;
import de.metas.handlingunits.qrcodes.model.HUQRCode;
import de.metas.i18n.AdMessageKey;
import de.metas.i18n.ImmutableTranslatableString;
import de.metas.i18n.TranslatableStrings;
import de.metas.picking.config.MobileUIPickingUserProfileRepository;
import de.metas.picking.rest_api.json.JsonPickingEventsList;
import de.metas.picking.rest_api.json.JsonPickingStepEvent;
import de.metas.picking.workflow.PickingJobRestService;
import de.metas.picking.workflow.PickingWFProcessStartParams;
import de.metas.picking.workflow.handlers.activity_handlers.ActualPickingWFActivityHandler;
import de.metas.picking.workflow.handlers.activity_handlers.CompletePickingWFActivityHandler;
import de.metas.picking.workflow.handlers.activity_handlers.RequestReviewWFActivityHandler;
import de.metas.picking.workflow.handlers.activity_handlers.SetPickingSlotWFActivityHandler;
import de.metas.user.UserId;
import de.metas.workflow.rest_api.model.CustomApplicationParameter;
import de.metas.workflow.rest_api.model.MobileApplicationId;
import de.metas.workflow.rest_api.model.MobileApplicationInfo;
import de.metas.workflow.rest_api.model.WFActivity;
import de.metas.workflow.rest_api.model.WFActivityId;
import de.metas.workflow.rest_api.model.WFProcess;
import de.metas.workflow.rest_api.model.WFProcessHeaderProperties;
import de.metas.workflow.rest_api.model.WFProcessHeaderProperty;
import de.metas.workflow.rest_api.model.WFProcessId;
import de.metas.workflow.rest_api.model.WorkflowLaunchersList;
import de.metas.workflow.rest_api.model.WorkflowLaunchersQuery;
import de.metas.workflow.rest_api.model.WorkplaceSettings;
import de.metas.workflow.rest_api.model.facets.WorkflowLaunchersFacetGroupList;
import de.metas.workflow.rest_api.service.WorkflowBasedMobileApplication;
import de.metas.workflow.rest_api.service.WorkflowStartRequest;
import de.metas.workplace.WorkplaceService;
import lombok.NonNull;
import org.adempiere.exceptions.AdempiereException;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.function.UnaryOperator;

import static de.metas.picking.workflow.handlers.activity_handlers.PickingWFActivityHelper.getPickingJob;

@Component
public class PickingMobileApplication implements WorkflowBasedMobileApplication
{
	@VisibleForTesting
	public static final MobileApplicationId APPLICATION_ID = MobileApplicationId.ofString("picking");

	private static final AdMessageKey MSG_Caption = AdMessageKey.of("mobileui.picking.appName");

	private final PickingJobRestService pickingJobRestService;
	private final PickingWorkflowLaunchersProvider wfLaunchersProvider;
	private final WorkplaceService workplaceService;

	public PickingMobileApplication(
			@NonNull final PickingJobRestService pickingJobRestService,
			@NonNull final MobileUIPickingUserProfileRepository mobileUIPickingUserProfileRepository,
			@NonNull final WorkplaceService workplaceService)
	{
		this.pickingJobRestService = pickingJobRestService;
		this.wfLaunchersProvider = new PickingWorkflowLaunchersProvider(pickingJobRestService, mobileUIPickingUserProfileRepository, workplaceService);
		this.workplaceService = workplaceService;
	}

	@Override
	public MobileApplicationId getApplicationId()
	{
		return APPLICATION_ID;
	}

	@Override
	public @NonNull MobileApplicationInfo getApplicationInfo(@NonNull final UserId loggedUserId)
	{
		final WorkplaceSettings workplaceSettings = WorkplaceSettings.builder()
				.assignedWorkplace(workplaceService.getWorkplaceByUserId(loggedUserId).orElse(null))
				.isWorkplaceAssignmentRequired(workplaceService.isAnyWorkplaceActive())
				.build();

		return MobileApplicationInfo.builder()
				.id(APPLICATION_ID)
				.caption(TranslatableStrings.adMessage(MSG_Caption))
				.showFilters(true)
				.applicationParameters(ImmutableMap.of(CustomApplicationParameter.WORKPLACE_SETTINGS, workplaceSettings))
				.build();
	}

	@Override
	public WorkflowLaunchersList provideLaunchers(@NonNull final WorkflowLaunchersQuery query)
	{
		if (query.getFilterByQRCode() != null)
		{
			throw new AdempiereException("Invalid QR Code: " + query.getFilterByQRCode());
		}

		return wfLaunchersProvider.provideLaunchers(query);
	}

	@Override
	public WorkflowLaunchersFacetGroupList getFacets(@NonNull final UserId userId)
	{
		return wfLaunchersProvider.getFacets(userId);
	}

	@NonNull
	private static PickingJobId toPickingJobId(final @NonNull WFProcessId wfProcessId)
	{
		return wfProcessId.getRepoId(PickingJobId::ofRepoId);
	}

	@Override
	public WFProcess getWFProcessById(@NonNull final WFProcessId wfProcessId)
	{
		final PickingJobId pickingJobId = toPickingJobId(wfProcessId);
		final PickingJob pickingJob = pickingJobRestService.getPickingJobById(pickingJobId);
		return toWFProcess(pickingJob);
	}

	@Override
	public WFProcess changeWFProcessById(
			@NonNull final WFProcessId wfProcessId,
			@NonNull final UnaryOperator<WFProcess> remappingFunction)
	{
		final WFProcess wfProcess = getWFProcessById(wfProcessId);
		return remappingFunction.apply(wfProcess);
	}

	@Override
	public WFProcessHeaderProperties getHeaderProperties(@NonNull final WFProcess wfProcess)
	{
		final PickingJob pickingJob = getPickingJob(wfProcess);

		return WFProcessHeaderProperties.builder()
				.entry(WFProcessHeaderProperty.builder()
							   .caption(TranslatableStrings.adElementOrMessage("DocumentNo"))
							   .value(pickingJob.getSalesOrderDocumentNo())
							   .build())
				.entry(WFProcessHeaderProperty.builder()
							   .caption(TranslatableStrings.adElementOrMessage("C_BPartner_Customer_ID"))
							   .value(pickingJob.getCustomerName())
							   .build())
				.entry(WFProcessHeaderProperty.builder()
							   .caption(TranslatableStrings.adElementOrMessage("PreparationDate"))
							   .value(pickingJob.getPreparationDate())
							   .build())
				.entry(WFProcessHeaderProperty.builder()
							   .caption(TranslatableStrings.adElementOrMessage("DeliveryToAddress"))
							   .value(pickingJob.getDeliveryRenderedAddress())
							   .build())
				.build();
	}

	@Override
	public WFProcess startWorkflow(@NonNull final WorkflowStartRequest request)
	{
		final UserId invokerId = request.getInvokerId();
		final PickingWFProcessStartParams params = PickingWFProcessStartParams.ofParams(request.getWfParameters());
		final PickingJob pickingJob = pickingJobRestService.createPickingJob(params, invokerId);

		wfLaunchersProvider.invalidateCacheByUserId(invokerId);

		return toWFProcess(pickingJob);
	}

	@Override
	public WFProcess continueWorkflow(@NonNull final WFProcessId wfProcessId, @NonNull final UserId callerId)
	{
		final PickingJobId pickingJobId = toPickingJobId(wfProcessId);
		final PickingJob pickingJob = pickingJobRestService.assignPickingJob(pickingJobId, callerId);
		return toWFProcess(pickingJob);
	}

	@Override
	public void abort(@NonNull final WFProcessId wfProcessId, @NonNull final UserId callerId)
	{
		final PickingJobId pickingJobId = toPickingJobId(wfProcessId);
		final PickingJob pickingJob = pickingJobRestService.getPickingJobById(pickingJobId);
		final WFProcess wfProcess = toWFProcess(pickingJob);
		abort(wfProcess, callerId);
	}

	private void abort(@NonNull final WFProcess wfProcess, final @NonNull UserId callerId)
	{
		wfProcess.assertHasAccess(callerId);
		pickingJobRestService.abort(getPickingJob(wfProcess));
		wfLaunchersProvider.invalidateCacheByUserId(callerId);
	}

	@Override
	public void abortAll(final UserId callerId)
	{
		pickingJobRestService.abortAllByUserId(callerId);
		wfLaunchersProvider.invalidateCacheByUserId(callerId);
	}

	private static WFProcess toWFProcess(final PickingJob pickingJob)
	{
		final UserId responsibleId = pickingJob.getLockedBy();

		final ImmutableList.Builder<WFActivity> activities = ImmutableList.builder();
		activities.add(WFActivity.builder()
				.id(WFActivityId.ofString("A10"))
				.caption(ImmutableTranslatableString.builder()
						.trl("de_DE", "Kommissionierplatz scannen")
						.trl("de_CH", "Kommissionierplatz scannen")
						.defaultValue("Scan picking slot")
						.build())
				.wfActivityType(SetPickingSlotWFActivityHandler.HANDLED_ACTIVITY_TYPE)
				.status(SetPickingSlotWFActivityHandler.computeActivityState(pickingJob))
				.build());
		activities.add(WFActivity.builder()
				.id(WFActivityId.ofString("A20"))
				.caption(TranslatableStrings.anyLanguage("Pick"))
				.wfActivityType(ActualPickingWFActivityHandler.HANDLED_ACTIVITY_TYPE)
				.status(ActualPickingWFActivityHandler.computeActivityState(pickingJob))
				.build());

		if (pickingJob.isPickingReviewRequired())
		{
			activities.add(WFActivity.builder()
					.id(WFActivityId.ofString("A80"))
					.caption(TranslatableStrings.adMessage(RequestReviewWFActivityHandler.MSG_Caption))
					.wfActivityType(RequestReviewWFActivityHandler.HANDLED_ACTIVITY_TYPE)
					.status(RequestReviewWFActivityHandler.computeActivityState(pickingJob))
					.build());
		}
		else
		{
			activities.add(WFActivity.builder()
					.id(WFActivityId.ofString("A90"))
					.caption(TranslatableStrings.adRefList(IDocument.ACTION_AD_Reference_ID, IDocument.ACTION_Complete))
					.wfActivityType(CompletePickingWFActivityHandler.HANDLED_ACTIVITY_TYPE)
					.status(CompletePickingWFActivityHandler.computeActivityState(pickingJob))
					.build());
		}

		return WFProcess.builder()
				.id(WFProcessId.ofIdPart(APPLICATION_ID, pickingJob.getId()))
				.responsibleId(responsibleId)
				.caption(PickingWFProcessUtils.workflowCaption()
						.salesOrderDocumentNo(pickingJob.getSalesOrderDocumentNo())
						.customerName(pickingJob.getCustomerName())
						.build())
				.document(pickingJob)
				.activities(activities.build())
				.build();
	}

	public WFProcess processStepEvent(
			@NonNull final JsonPickingStepEvent event,
			@NonNull final UserId callerId)
	{
		return processStepEvents(WFProcessId.ofString(event.getWfProcessId()), callerId, ImmutableSet.of(event));
	}

	public void processStepEvents(
			@NonNull final JsonPickingEventsList eventsList,
			@NonNull final UserId callerId)
	{
		final ImmutableListMultimap<WFProcessId, JsonPickingStepEvent> eventsByWFProcessId = eventsList
				.getEvents()
				.stream()
				.collect(ImmutableListMultimap.toImmutableListMultimap(
						event -> WFProcessId.ofString(event.getWfProcessId()),
						event -> event));

		eventsByWFProcessId
				.asMap()
				.forEach((wfProcessId, events) -> processStepEvents(wfProcessId, callerId, events));
	}

	public static PickingJobStepEventType fromJson(JsonPickingStepEvent.EventType json)
	{
		switch (json)
		{
			case PICK:
				return PickingJobStepEventType.PICK;
			case UNPICK:
				return PickingJobStepEventType.UNPICK;
			default:
				throw new AdempiereException("Unknown event type: " + json);
		}
	}

	private WFProcess processStepEvents(
			@NonNull final WFProcessId wfProcessId,
			@NonNull final UserId callerId,
			@NonNull final Collection<JsonPickingStepEvent> jsonEvents)
	{
		return changeWFProcessById(
				wfProcessId,
				wfProcess -> {
					wfProcess.assertHasAccess(callerId);
					assertPickingActivityType(jsonEvents, wfProcess);

					return wfProcess.<PickingJob>mapDocument(
							pickingJob -> processStepEvents(pickingJob, jsonEvents)
					);
				});
	}

	private PickingJob processStepEvents(@NonNull final PickingJob pickingJob, @NonNull final Collection<JsonPickingStepEvent> jsonEvents)
	{
		final ImmutableList<PickingJobStepEvent> events = jsonEvents.stream()
				.map(json -> fromJson(json, pickingJob))
				.collect(ImmutableList.toImmutableList());

		return pickingJobRestService.processStepEvents(pickingJob, events);
	}

	private static PickingJobStepEvent fromJson(@NonNull final JsonPickingStepEvent json, @NonNull final PickingJob pickingJob)
	{
		final HUQRCode qrCode = HUQRCode.fromGlobalQRCodeJsonString(json.getHuQRCode());

		final PickingJobLineId pickingLineId = PickingJobLineId.ofString(json.getPickingLineId());
		final PickingJobStepId pickingStepId = PickingJobStepId.ofNullableString(json.getPickingStepId());
		final PickingJobStepPickFromKey pickFromKey = pickingStepId != null
				? pickingJob.getStepById(pickingStepId).getPickFromByHUQRCode(qrCode).getPickFromKey()
				: null;

		return PickingJobStepEvent.builder()
				.timestamp(SystemTime.asInstant())
				.pickingLineId(pickingLineId)
				.pickingStepId(pickingStepId)
				.pickFromKey(pickFromKey)
				.eventType(fromJson(json.getType()))
				.huQRCode(qrCode)
				.qtyPicked(json.getQtyPicked())
				.qtyRejected(json.getQtyRejected())
				.qtyRejectedReasonCode(QtyRejectedReasonCode.ofNullableCode(json.getQtyRejectedReasonCode()).orElse(null))
				.catchWeight(json.getCatchWeight())
				.build();
	}

	private static void assertPickingActivityType(
			final @NonNull Collection<JsonPickingStepEvent> events,
			final @NonNull WFProcess wfProcess)
	{
		events.stream()
				.map(event -> WFActivityId.ofString(event.getWfActivityId()))
				.distinct()
				.map(wfProcess::getActivityById)
				.map(WFActivity::getWfActivityType)
				.forEach(ActualPickingWFActivityHandler.HANDLED_ACTIVITY_TYPE::assertActual);
	}

	@Override
	public void logout(final @NonNull UserId userId)
	{
		pickingJobRestService.unassignAllByUserId(userId);
		wfLaunchersProvider.invalidateCacheByUserId(userId);
	}
}

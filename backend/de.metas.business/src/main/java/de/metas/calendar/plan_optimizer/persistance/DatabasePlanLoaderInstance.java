package de.metas.calendar.plan_optimizer.persistance;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import de.metas.calendar.plan_optimizer.domain.Plan;
import de.metas.calendar.plan_optimizer.domain.Project;
import de.metas.calendar.plan_optimizer.domain.Resource;
import de.metas.calendar.plan_optimizer.domain.Step;
import de.metas.calendar.plan_optimizer.domain.StepId;
import de.metas.calendar.simulation.SimulationPlanId;
import de.metas.common.util.CoalesceUtil;
import de.metas.i18n.BooleanWithReason;
import de.metas.logging.LogManager;
import de.metas.organization.IOrgDAO;
import de.metas.product.ResourceId;
import de.metas.project.InternalPriority;
import de.metas.project.ProjectId;
import de.metas.project.workorder.calendar.WOProjectSimulationPlan;
import de.metas.project.workorder.calendar.WOProjectSimulationService;
import de.metas.project.workorder.project.WOProject;
import de.metas.project.workorder.project.WOProjectService;
import de.metas.project.workorder.resource.WOProjectResource;
import de.metas.project.workorder.resource.WOProjectResourcesCollection;
import de.metas.project.workorder.step.WOProjectStep;
import de.metas.project.workorder.step.WOProjectStepsCollection;
import de.metas.resource.HumanResourceTestGroup;
import de.metas.resource.HumanResourceTestGroupService;
import de.metas.resource.ResourceService;
import lombok.Builder;
import lombok.NonNull;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class DatabasePlanLoaderInstance
{
	//
	// Services
	private static final Logger logger = LogManager.getLogger(DatabasePlanLoaderInstance.class);
	private final IOrgDAO orgDAO;
	private final WOProjectService woProjectService;
	private final WOProjectSimulationService woProjectSimulationService;
	private final ResourceService resourceService;
	private final HumanResourceTestGroupService humanResourceTestGroupService;

	//
	// Params
	@NonNull final SimulationPlanId simulationId;

	//
	// State
	private WOProjectStepsCollection stepsByProjectId;
	private WOProjectResourcesCollection resources;
	private ZoneId timeZone;
	private WOProjectSimulationPlan simulationPlan;
	private final HashMap<ResourceId, Resource> optaPlannerResources = new HashMap<>();

	@Builder
	private DatabasePlanLoaderInstance(
			final @NonNull IOrgDAO orgDAO,
			final @NonNull WOProjectService woProjectService,
			final @NonNull WOProjectSimulationService woProjectSimulationService,
			final @NonNull ResourceService resourceService,
			final @NonNull HumanResourceTestGroupService humanResourceTestGroupService,
			//
			final @NonNull SimulationPlanId simulationId)
	{
		this.orgDAO = orgDAO;
		this.woProjectService = woProjectService;
		this.woProjectSimulationService = woProjectSimulationService;
		this.resourceService = resourceService;
		this.humanResourceTestGroupService = humanResourceTestGroupService;
		this.simulationId = simulationId;
	}

	@NonNull
	public Plan load()
	{
		final List<WOProject> woProjects = woProjectService.getAllActiveProjects();
		if (woProjects.isEmpty())
		{
			return new Plan();
		}

		final ImmutableSet<ProjectId> projectIds = woProjects.stream().map(WOProject::getProjectId).collect(ImmutableSet.toImmutableSet());
		this.stepsByProjectId = woProjectService.getStepsByProjectIds(projectIds);
		this.resources = woProjectService.getResourcesByProjectIds(projectIds);
		this.timeZone = orgDAO.getTimeZone(woProjects.get(0).getOrgId()); // use the time zone of the first project
		this.simulationPlan = woProjectSimulationService.getSimulationPlanById(simulationId);

		final List<Project> optaplannerProjects = woProjects.stream() //todo fp pull by priority
				.map(this::toOptaplannerProject)
				.collect(ImmutableList.toImmutableList());

		final List<Step> optaplannerSteps = optaplannerProjects.stream()
				.map(Project::getSteps)
				.flatMap(List::stream)
				.collect(ImmutableList.toImmutableList());

		final List<Resource> optaplannerResources = optaplannerSteps.stream()
				.map(Step::getResource)
				.collect(ImmutableList.toImmutableList());

		final Plan optaPlannerPlan = new Plan();
		optaPlannerPlan.setSimulationId(simulationId);
		optaPlannerPlan.setTimeZone(timeZone);
		optaPlannerPlan.setProjectList(optaplannerProjects);
		optaPlannerPlan.setStepsList(optaplannerSteps);
		optaPlannerPlan.setResourceList(optaplannerResources);

		return optaPlannerPlan;
	}

	@NonNull
	private Project toOptaplannerProject(@NonNull final WOProject woProject)
	{
		return Project.builder()
				.projectId(woProject.getProjectId())
				.projectPriority(CoalesceUtil.coalesceNotNull(woProject.getInternalPriority(), InternalPriority.MEDIUM))
				.steps(loadStepsFromWOProjectId(woProject))
				.build();
	}

	@NonNull
	private List<Step> loadStepsFromWOProjectId(@NonNull final WOProject woProject)
	{
		return stepsByProjectId.getByProjectId(woProject.getProjectId()).toOrderedList()
				.stream()
				.map(step -> toOptaplannerSteps(step, woProject))
				.flatMap(List::stream)
				.collect(ImmutableList.toImmutableList());
	}

	final List<Step> toOptaplannerSteps(@NonNull final WOProjectStep woStep, @NonNull final WOProject woProject)
	{
		final LocalDateTime startDateMin = Optional.ofNullable(woStep.getDeliveryDate())
				.map(this::toLocalDateTime)
				.orElse(extractProjectStartDate(woProject));

		if (startDateMin == null)
		{
			logger.warn("Ignore step because StartDateMin could not be determined: {}", woStep);
			return ImmutableList.of();
		}

		final LocalDateTime dueDate = CoalesceUtil.optionalOfFirstNonNull(woStep.getWoDueDate(), woProject.getDateFinish())
				.map(this::toLocalDateTime)
				.orElse(null);

		if (dueDate == null)
		{
			logger.warn("Ignore step because due date could not be determined: {}", woStep);
			return ImmutableList.of();
		}

		return resources.getByStepId(woStep.getWoProjectStepId())
				.stream()
				.map(woStepResourceOrig -> fromWOStepResource(woStep, woStepResourceOrig, startDateMin, dueDate))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(ImmutableList.toImmutableList());
	}

	@NonNull
	private LocalDateTime toLocalDateTime(final Instant date)
	{
		return date.atZone(timeZone).toLocalDateTime().truncatedTo(Plan.PLANNING_TIME_PRECISION);
	}

	@Nullable
	private LocalDateTime extractProjectStartDate(final WOProject woProject)
	{
		return CoalesceUtil.optionalOfFirstNonNull(woProject.getDateOfProvisionByBPartner(), woProject.getDateContract(), woProject.getWoProjectCreatedDate())
				.map(this::toLocalDateTime)
				.orElse(null);
	}

	@NonNull
	private Optional<Step> fromWOStepResource(
			@NonNull final WOProjectStep woStep,
			@NonNull final WOProjectResource woStepResourceOrig,
			@NonNull final LocalDateTime startDateMin,
			@NonNull final LocalDateTime dueDate)
	{
		Duration duration = woStepResourceOrig.getDuration();
		if (duration.toSeconds() <= 0)
		{
			duration = Duration.of(1, Plan.PLANNING_TIME_PRECISION);
			logger.info("Step/resource has invalid duration. Considering it {}: {}", duration, woStepResourceOrig);
		}

		final WOProjectResource woStepResource = simulationPlan.applyOn(woStepResourceOrig);

		final LocalDateTime startDate = woStepResource.getStartDate()
				.map(this::toLocalDateTime)
				.orElse(null);
		final LocalDateTime endDate = woStepResource.getEndDate()
				.map(this::toLocalDateTime)
				.orElse(null);

		boolean pinned = woStep.isManuallyLocked() || woStep.inTesting();
		if (pinned && (startDate == null || endDate == null))
		{
			logger.info("Cannot consider resource as locked because it has no start/end date: {}", woStepResource);
			pinned = false;
		}

		final Duration humanResourceTestGroupDuration = Optional.ofNullable(woStep.getWoPlannedPersonDurationHours())
				.map(Duration::ofHours)
				.orElse(Duration.ZERO);

		final Step step = Step.builder()
				.id(StepId.builder()
						.woProjectStepId(woStepResource.getWoProjectStepId())
						.woProjectResourceId(woStepResource.getWoProjectResourceId())
						.build())
				.resource(toOptaPlannerResource(woStepResource))
				.duration(duration)
				.dueDate(dueDate)
				.startDateMin(startDateMin)
				.pinned(pinned)
				.humanResourceTestGroupDuration(humanResourceTestGroupDuration)
				.seqNo(woStep.getSeqNo())
				.build();

		final BooleanWithReason valid = step.checkProblemFactsValid();
		if (valid.isFalse())
		{
			logger.info("Skip invalid woStep because `{}`: {}", valid.getReasonAsString(), woStep);
			return Optional.empty();
		}

		return Optional.of(step);
	}

	@NonNull
	private Resource toOptaPlannerResource(final WOProjectResource woStepResource)
	{
		return optaPlannerResources.computeIfAbsent(woStepResource.getResourceId(), this::createOptaPlannerResource);
	}

	private de.metas.calendar.plan_optimizer.domain.Resource createOptaPlannerResource(final ResourceId resourceId)
	{
		final de.metas.resource.Resource resource = resourceService.getResourceById(resourceId);

		final Duration hrWeeklyCapacityInHours = Optional.ofNullable(resource.getHumanResourceTestGroupId())
				.map(humanResourceTestGroupService::getById)
				.map(HumanResourceTestGroup::getWeeklyCapacity)
				.orElse(null);

		return new de.metas.calendar.plan_optimizer.domain.Resource(resource.getResourceId(), resource.getName().getDefaultValue(), hrWeeklyCapacityInHours);
	}

	public static int computeDelay(@NonNull final LocalDateTime lastStepEndDate, @Nullable final LocalDateTime thisStepStartDate)
	{
		return thisStepStartDate != null && thisStepStartDate.isAfter(lastStepEndDate)
				? (int)Plan.PLANNING_TIME_PRECISION.between(lastStepEndDate, thisStepStartDate)
				: 0;
	}

}

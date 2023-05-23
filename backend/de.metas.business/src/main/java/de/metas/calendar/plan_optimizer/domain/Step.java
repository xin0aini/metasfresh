package de.metas.calendar.plan_optimizer.domain;

import de.metas.calendar.plan_optimizer.solver.StartDateUpdatingVariableListener;
import de.metas.i18n.BooleanWithReason;
import de.metas.product.ResourceId;
import de.metas.project.ProjectId;
import de.metas.util.time.DurationUtils;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.entity.PlanningPin;
import org.optaplanner.core.api.domain.lookup.PlanningId;
import org.optaplanner.core.api.domain.valuerange.CountableValueRange;
import org.optaplanner.core.api.domain.valuerange.ValueRangeFactory;
import org.optaplanner.core.api.domain.variable.InverseRelationShadowVariable;
import org.optaplanner.core.api.domain.variable.NextElementShadowVariable;
import org.optaplanner.core.api.domain.variable.PreviousElementShadowVariable;
import org.optaplanner.core.api.domain.variable.ShadowVariable;

import javax.annotation.Nullable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@PlanningEntity
@Setter
@Getter
//@EqualsAndHashCode(doNotUseGetters = true) // IMPORTANT: do not use it beucase we have next/prev Step refs
public class Step
{
	@NonNull @PlanningId private StepId id;
	@NonNull private Resource resource;
	@NonNull private Duration duration;
	@NonNull private LocalDateTime startDateMin;
	@NonNull private LocalDateTime dueDate;
	@NonNull private Duration humanResourceTestGroupDuration;
	@NonNull private Integer seqNo;
	@Nullable private Step previousStep;
	@Nullable private Step nextStep;

	/**
	 * Delay it's the offset from previous step end.
	 * The delay is measured in {@link Plan#PLANNING_TIME_PRECISION}
	 */
	public static final String FIELD_startDate = "startDate";

	// Shadow variables
	@InverseRelationShadowVariable(sourceVariableName = "stepList")
	private Project project;
	@ShadowVariable(variableListenerClass = StartDateUpdatingVariableListener.class,
			sourceEntityClass = Project.class, sourceVariableName = "stepList")
	private LocalDateTime startDate; // in hours

	@PlanningPin
	boolean pinned;

	// No-arg constructor required for OptaPlanner
	public Step() {}

	@Builder
	private Step(
			@NonNull final StepId id,
			@NonNull final Resource resource,
			@NonNull final Duration duration,
			@NonNull final LocalDateTime startDateMin,
			@NonNull final LocalDateTime dueDate,
			@NonNull final Duration humanResourceTestGroupDuration,
			@NonNull final Integer seqNo,
			@Nullable final Step previousStep,
			@Nullable final Step nextStep,
			final boolean pinned)
	{
		this.id = id;
		this.resource = resource;
		this.duration = duration;
		this.dueDate = dueDate;
		this.startDateMin = startDateMin;
		this.pinned = pinned;
		this.humanResourceTestGroupDuration = humanResourceTestGroupDuration;
		this.seqNo = seqNo;
		this.previousStep = previousStep;
		this.nextStep = nextStep;
	}

	@Override
	public String toString()
	{
		// NOTE: keep it concise, important for optaplanner troubleshooting
		return getStartDate() + " -> " + getEndDate()
				+ " (" + duration + ")"
				+ ": dueDate=" + dueDate
				+ ", startDateMin=" + startDateMin
				+ ", " + resource
				+ ", " + getProjectId()
				+ ", humanResourceTestGroupDuration=" + getHumanResourceTestGroupDuration()
				+ ", ID=" + id.getWoProjectResourceId().getRepoId();
	}

	public BooleanWithReason checkProblemFactsValid()
	{
		if (!startDateMin.isBefore(dueDate))
		{
			return BooleanWithReason.falseBecause("StartDateMin shall be before DueDate");
		}
		if (duration.getSeconds() <= 0)
		{
			return BooleanWithReason.falseBecause("Duration must be set and must be positive");
		}

		final Duration durationMax = Duration.between(startDateMin, dueDate);
		if (duration.compareTo(durationMax) > 0)
		{
			return BooleanWithReason.falseBecause("Duration does not fit into StartDateMin/DueDate interval");
		}

		return BooleanWithReason.TRUE;
	}

	public CountableValueRange<Integer> getDelayRange()
	{
		final int delayMax = computeDelayMax();
		return ValueRangeFactory.createIntValueRange(0, delayMax);
	}

	private int computeDelayMax()
	{
		return DurationUtils.toInt(Duration.between(startDateMin, dueDate), Plan.PLANNING_TIME_PRECISION)
				- DurationUtils.toInt(duration, Plan.PLANNING_TIME_PRECISION);
	}

	public ProjectId getProjectId() {return getId().getProjectId();}

	public ResourceId getResourceId() {return resource.getId();}

	public YearWeek getStartDateYearWeek()
	{
		return YearWeek.from(getStartDate());
	}

	@NonNull
	public LocalDateTime getEndDate()
	{
		return Optional.ofNullable(startDate)
				.map(startDate -> startDate.plus(duration))
				.orElseGet(() -> getStartDateMin().plus(duration));
	}

	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public boolean isStartDateMinRespected() {return getDurationBeforeStartDateMin().isZero();}

	public Duration getDurationBeforeStartDateMin()
	{
		final LocalDateTime startDate = getStartDate();
		return startDate.isBefore(startDateMin) ? Duration.between(startDate, startDateMin) : Duration.ZERO;
	}

	public int getDurationBeforeStartDateMinAsInt()
	{
		return DurationUtils.toInt(getDurationBeforeStartDateMin(), Plan.PLANNING_TIME_PRECISION);
	}

	public Duration getDurationAfterDue()
	{
		final LocalDateTime endDate = getEndDate();
		return endDate.isAfter(dueDate) ? Duration.between(dueDate, endDate) : Duration.ZERO;
	}

	public int getDurationAfterDueAsInt()
	{
		return DurationUtils.toInt(getDurationAfterDue(), Plan.PLANNING_TIME_PRECISION);
	}

	public boolean isDueDateNotRespected() {return !isDueDateRespected();}

	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public boolean isDueDateRespected() {return getDurationAfterDue().isZero();}

	private Duration getDurationFromEndToDueDate() {return Duration.between(getEndDate(), dueDate);}

	public int getDurationFromEndToDueDateInHoursAbs() {return Math.abs((int)getDurationFromEndToDueDate().toHours());}

	@Nullable
	// @PreviousElementShadowVariable(sourceVariableName = "stepList")
	public Step getPreviousStep()
	{
		return previousStep;
	}

	@Nullable
	// @NextElementShadowVariable(sourceVariableName = "stepList")
	public Step getNextStep()
	{
		return nextStep;
	}
}

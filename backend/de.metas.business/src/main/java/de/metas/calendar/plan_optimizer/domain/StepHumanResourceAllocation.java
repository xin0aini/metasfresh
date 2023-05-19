package de.metas.calendar.plan_optimizer.domain;

import de.metas.util.Check;
import lombok.NonNull;
import lombok.Value;

import java.time.Duration;
import java.time.LocalDateTime;

@Value
public class StepHumanResourceAllocation
{
	@NonNull StepId stepId;
	@NonNull YearWeek yearWeek;
	@NonNull LocalDateTime startDate;
	@NonNull Duration requiredDuration;

	private StepHumanResourceAllocation(StepHumanResourceRequired stepReq, Step step)
	{
		Check.assumeEquals(stepReq.getStepId(), step.getId(), "StepId shall be the same: {}, {}", stepReq, step);

		this.yearWeek = step.getStartDateYearWeek();
		this.stepId = step.getId();
		this.startDate = step.getStartDate();
		this.requiredDuration = stepReq.getRequiredDuration();
	}

	public static StepHumanResourceAllocation of(StepHumanResourceRequired stepReq, Step step)
	{
		return new StepHumanResourceAllocation(stepReq, step);
	}

	public int getRequiredDurationInHours()
	{
		return (int)requiredDuration.toHours();
	}
}

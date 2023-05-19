package de.metas.calendar.plan_optimizer.domain;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import org.optaplanner.core.api.domain.lookup.PlanningId;

import java.time.Duration;

@Value
@Builder
public class StepHumanResourceRequired
{
	@PlanningId
	@NonNull StepId stepId;
	@NonNull Resource resource;
	@NonNull Duration requiredDuration;

	public boolean isFiniteHumanResourceWeeklyCapacity()
	{
		return resource.isFiniteHumanResourceWeeklyCapacity();
	}

	public int getRequiredDurationInHours()
	{
		return (int)requiredDuration.toHours();
	}
}

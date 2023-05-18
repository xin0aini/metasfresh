package de.metas.calendar.plan_optimizer.domain;

import lombok.NonNull;
import lombok.Value;

import java.time.Duration;

@Value
public class StepHumanResourceRequired
{
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

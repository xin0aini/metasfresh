package de.metas.calendar.plan_optimizer.domain;

import de.metas.product.ResourceId;
import de.metas.resource.HumanResourceTestGroupId;
import lombok.NonNull;
import lombok.Value;
import org.optaplanner.core.api.domain.lookup.PlanningId;

import javax.annotation.Nullable;
import java.time.Duration;

@Value
public class Resource
{
	@PlanningId @NonNull ResourceId id;
	@NonNull String name;

	@Nullable HumanResourceTestGroupId humanResourceTestGroupId;

	@Nullable Duration humanResourceWeeklyCapacity;

	@Override
	public String toString() {return name;}

	public boolean isFiniteHumanResourceWeeklyCapacity()
	{
		return humanResourceWeeklyCapacity != null;
	}

	public int getHumanResourceWeeklyCapacityInHours()
	{
		return humanResourceWeeklyCapacity != null
				? (int)humanResourceWeeklyCapacity.toHours()
				: Integer.MAX_VALUE;
	}
}

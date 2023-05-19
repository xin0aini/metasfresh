package de.metas.calendar.plan_optimizer.domain;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class StepHumanResourceAllocationsAggregate
{
	public static final StepHumanResourceAllocationsAggregate ZERO = new StepHumanResourceAllocationsAggregate(ImmutableMap.of());

	private final ImmutableMap<StepId, StepHumanResourceAllocation> map;

	private StepHumanResourceAllocationsAggregate(final Map<StepId, StepHumanResourceAllocation> map)
	{
		this.map = ImmutableMap.copyOf(map);
	}

	public static StepHumanResourceAllocationsAggregate of(StepHumanResourceRequired stepReq, Step step)
	{
		final StepHumanResourceAllocation alloc = StepHumanResourceAllocation.of(stepReq, step);
		return new StepHumanResourceAllocationsAggregate(ImmutableMap.of(alloc.getStepId(), alloc));
	}

	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(this)
				.add("requiredDuration", getRequiredDurationInHours())
				.toString();
	}

	public boolean isEmpty() {return map.isEmpty();}

	public StepHumanResourceAllocationsAggregate add(StepHumanResourceAllocationsAggregate other)
	{
		if (this.map.isEmpty())
		{
			return other;
		}
		else if (other.map.isEmpty())
		{
			return this;
		}
		else
		{
			return new StepHumanResourceAllocationsAggregate(
					ImmutableMap.<StepId, StepHumanResourceAllocation>builder()
							.putAll(this.map)
							.putAll(other.map)
							.build());
		}
	}

	public StepHumanResourceAllocationsAggregate subtract(StepHumanResourceAllocationsAggregate other)
	{
		if (this.isEmpty())
		{
			return this; // i.e. ZERO
		}
		else if (other.isEmpty())
		{
			return this;
		}
		else
		{
			final HashMap<StepId, StepHumanResourceAllocation> newMap = new HashMap<>(this.map);
			other.map.keySet().forEach(newMap::remove);
			if (newMap.size() == this.map.size())
			{
				return this;
			}
			else if (newMap.isEmpty())
			{
				return ZERO;
			}
			else
			{
				return new StepHumanResourceAllocationsAggregate(newMap);
			}
		}
	}

	public int getRequiredDurationInHours()
	{
		if (map.isEmpty())
		{
			return 0;
		}

		return map.values()
				.stream()
				.mapToInt(StepHumanResourceAllocation::getRequiredDurationInHours)
				.sum();
	}

	public boolean isWeeklyCapacityExceeded(final int weeklyCapacityInHours)
	{
		return getRequiredDurationInHours() > weeklyCapacityInHours;
	}

	public int computePenaltyWeight(final int weeklyCapacityInHours)
	{
		final ArrayList<StepHumanResourceAllocation> allocationsInOrder = new ArrayList<>(map.values());
		allocationsInOrder.sort(Comparator.comparing(StepHumanResourceAllocation::getStartDate)
				.thenComparing(StepHumanResourceAllocation::getStepId));

		int reservedCapacity = 0;
		int overReservedCapacity = 0;
		for (StepHumanResourceAllocation alloc : allocationsInOrder)
		{
			reservedCapacity += alloc.getRequiredDurationInHours();

			if (reservedCapacity > weeklyCapacityInHours)
			{
				final LocalDateTime startDate = alloc.getStartDate();
				final LocalDateTime nextAvailableDate = YearWeek.from(startDate).nextWeekMonday();
				overReservedCapacity += Plan.PLANNING_TIME_PRECISION.between(startDate, nextAvailableDate);
			}
		}

		System.out.println("** overReservedCapacity=" + overReservedCapacity);
		allocationsInOrder.forEach(alloc -> System.out.println("\t" + alloc));

		return overReservedCapacity;
	}

}

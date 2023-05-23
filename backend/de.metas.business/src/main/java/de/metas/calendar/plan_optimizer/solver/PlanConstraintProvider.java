package de.metas.calendar.plan_optimizer.solver;

import de.metas.calendar.plan_optimizer.domain.Plan;
import de.metas.calendar.plan_optimizer.domain.Project;
import de.metas.calendar.plan_optimizer.domain.Step;
import de.metas.calendar.plan_optimizer.domain.StepHumanResourceAllocationsAggregate;
import de.metas.calendar.plan_optimizer.domain.StepHumanResourceRequired;
import de.metas.project.InternalPriority;
import de.metas.util.Check;
import org.optaplanner.core.api.score.buildin.bendable.BendableScore;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintCollectors;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.ConstraintProvider;
import org.optaplanner.core.api.score.stream.Joiners;
import org.optaplanner.core.api.score.stream.bi.BiJoiner;
import org.optaplanner.core.impl.score.stream.JoinerSupport;

import java.time.LocalDateTime;

public class PlanConstraintProvider implements ConstraintProvider
{
	public static final int HARD_LEVELS_SIZE = 2;
	public static final int SOFT_LEVELS_SIZE = 3;

	private static final BendableScore ONE_HARD = BendableScore.of(new int[] { 1, 0 }, new int[] { 0, 0, 0 });
	private static final BendableScore ONE_HARD_2 = BendableScore.of(new int[] { 0, 1 }, new int[] { 0, 0, 0 });
	private static final BendableScore ONE_SOFT_1 = BendableScore.of(new int[] { 0, 0 }, new int[] { 1, 0, 0 });
	private static final BendableScore ONE_SOFT_2 = BendableScore.of(new int[] { 0, 0 }, new int[] { 0, 1, 0 });
	private static final BendableScore ONE_SOFT_3 = BendableScore.of(new int[] { 0, 0 }, new int[] { 0, 0, 1 });

	@Override
	public Constraint[] defineConstraints(final ConstraintFactory constraintFactory)
	{
		return new Constraint[] {
				// Hard:
				// resourceConflict(constraintFactory),
				// startDateMin(constraintFactory),
				// dueDate(constraintFactory),
				// humanResourceAvailableCapacity(constraintFactory),
				// Soft:
				stepsNotRespectingProjectPriority(constraintFactory),
				// delayIsMinimum(constraintFactory),
				// minDurationFromEndToDueDateIsMaximum(constraintFactory),
				// sumOfDurationFromEndToDueDateIsMaximum(constraintFactory),
		};
	}

	Constraint resourceConflict(final ConstraintFactory constraintFactory)
	{
		return constraintFactory.forEachUniquePair(
						Step.class,
						Joiners.equal(Step::getResource),
						stepsOverlapping())
				.penalize(ONE_HARD, PlanConstraintProvider::getOverlappingDuration)
				.asConstraint("Resource conflict");
	}

	private static BiJoiner<Step, Step> stepsOverlapping() {return Joiners.overlapping(Step::getStartDate, Step::getEndDate);}

	Constraint startDateMin(final ConstraintFactory constraintFactory)
	{
		return constraintFactory.forEach(Step.class)
				.filter(step -> !step.isStartDateMinRespected())
				.penalize(ONE_HARD, Step::getDurationBeforeStartDateMinAsInt)
				.asConstraint("StartDateMin not respected");
	}

	Constraint dueDate(final ConstraintFactory constraintFactory)
	{
		return constraintFactory.forEach(Step.class)
				.filter(Step::isDueDateNotRespected)
				.penalize(ONE_HARD, Step::getDurationAfterDueAsInt)
				.asConstraint("DueDate not respected");
	}

	Constraint humanResourceAvailableCapacity(final ConstraintFactory constraintFactory)
	{
		return constraintFactory.forEach(StepHumanResourceRequired.class)
				.filter(StepHumanResourceRequired::isFiniteHumanResourceWeeklyCapacity)
				.join(Step.class, Joiners.equal(StepHumanResourceRequired::getStepId, Step::getId))
				.groupBy(
						(stepReq, step) -> stepReq.getResource(),
						(stepReq, step) -> step.getStartDateYearWeek(),
						ConstraintCollectors.sum(
								StepHumanResourceAllocationsAggregate::of,
								StepHumanResourceAllocationsAggregate.ZERO,
								StepHumanResourceAllocationsAggregate::add,
								StepHumanResourceAllocationsAggregate::subtract
						)
				)
				.filter((resource, yearWeek, allocationsAgg) -> allocationsAgg.isWeeklyCapacityExceeded(resource.getHumanResourceWeeklyCapacityInHours()))
				.penalize(ONE_HARD_2,
						  (resource, yearWeek, allocationsAgg) -> allocationsAgg.computePenaltyWeight(resource.getHumanResourceWeeklyCapacityInHours()))
				.asConstraint("Available human resource test group capacity");
	}
	// Constraint delayIsMinimum(final ConstraintFactory constraintFactory)
	// {
	// 	return constraintFactory.forEach(Step.class)
	// 			.filter(step -> step.getDelay() > 0)
	// 			.penalize(ONE_SOFT_1, Step::getDelay)
	// 			.asConstraint("Delay is minimum");
	// }
	//
	// // select from the solution set S the solution s for which the minimum amount |tdi-tei| is maximum
	// Constraint minDurationFromEndToDueDateIsMaximum(final ConstraintFactory constraintFactory)
	// {
	// 	return constraintFactory.forEach(Step.class)
	// 			.groupBy(ConstraintCollectors.min(Step::getDurationFromEndToDueDateInHoursAbs))
	// 			.reward(ONE_SOFT_2, durationFromEndToDueDateInHours -> durationFromEndToDueDateInHours)
	// 			.asConstraint("solution for which the minimum of |tdi-tei| is maximum");
	// }
	//
	// // Choose from Z the solution where the sum of | tdi- tei | is maximum
	// Constraint sumOfDurationFromEndToDueDateIsMaximum(final ConstraintFactory constraintFactory)
	// {
	// 	return constraintFactory.forEach(Step.class)
	// 			.groupBy(ConstraintCollectors.sum(Step::getDurationFromEndToDueDateInHoursAbs))
	// 			.reward(ONE_SOFT_3, sum -> sum)
	// 			.asConstraint("solution for which sum of |tdi-tei| is maximum");
	// }

	Constraint stepsNotRespectingProjectPriority(final ConstraintFactory constraintFactory)
	{
		return constraintFactory.forEachUniquePair(
						Project.class,
						Joiners.equal(Project::getProjectId),
						stepsNotRespectingProjectPriority())
				.penalize(ONE_SOFT_1, PlanConstraintProvider::computePenaltyWeight_StepsNotRespectingProjectPriority)
				.asConstraint("Steps not respecting project priority");
	}
	//
	private static BiJoiner<Project, Project> stepsNotRespectingProjectPriority()
	{
		return JoinerSupport.getJoinerService().newBiJoiner(PlanConstraintProvider::stepsNotRespectingProjectPriority);
	}

	static boolean stepsNotRespectingProjectPriority(final Project p1, final Project p2)
	{
		return computePenaltyWeight_StepsNotRespectingProjectPriority(p1, p2) > 0;
	}

	static int computePenaltyWeight_StepsNotRespectingProjectPriority(final Project p1, final Project p2)
	{
		final InternalPriority prio1 = p1.getProjectPriority();
		final InternalPriority prio2 = p2.getProjectPriority();
		if (prio1.equals(prio2))
		{
			return 0;
		}
		else if (prio1.isHigherThan(prio2))
		{
			final LocalDateTime endDate1 = Check.assumeNotNull(LocalDateTime.MAX, "end date not null: {}", p1);
			final LocalDateTime startDate2 = Check.assumeNotNull(LocalDateTime.MAX, "start date not null: {}", p2);

			final int duration = (int)Plan.PLANNING_TIME_PRECISION.between(endDate1, startDate2);
			return duration < 0 ? -duration : 0;
		}
		else
		{
			final LocalDateTime endDate2 = Check.assumeNotNull(LocalDateTime.MIN, "end date not null: {}", p1);
			final LocalDateTime startDate1 = Check.assumeNotNull(LocalDateTime.MAX, "start date not null: {}", p2);

			final int duration = (int)Plan.PLANNING_TIME_PRECISION.between(endDate2, startDate1);
			return duration < 0 ? -duration : 0;
		}
	}

	private static int getOverlappingDuration(final Step step1, final Step step2)
	{
		final LocalDateTime step1Start = step1.getStartDate();
		final LocalDateTime step1End = step1.getEndDate();
		final LocalDateTime step2Start = step2.getStartDate();
		final LocalDateTime step2End = step2.getEndDate();
		final LocalDateTime overlappingStart = (step1Start.isAfter(step2Start)) ? step1Start : step2Start; // MAX
		final LocalDateTime overlappingEnd = (step1End.isBefore(step2End)) ? step1End : step2End; // MIN
		return (int)Plan.PLANNING_TIME_PRECISION.between(overlappingStart, overlappingEnd);
	}
}

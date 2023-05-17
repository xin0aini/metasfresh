package de.metas.calendar.plan_optimizer;

import ch.qos.logback.classic.Level;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Multimaps;
import de.metas.calendar.plan_optimizer.domain.Plan;
import de.metas.calendar.plan_optimizer.domain.Resource;
import de.metas.calendar.plan_optimizer.domain.Step;
import de.metas.calendar.plan_optimizer.domain.StepId;
import de.metas.calendar.simulation.SimulationPlanId;
import de.metas.common.util.time.SystemTime;
import de.metas.logging.LogManager;
import de.metas.product.ResourceId;
import de.metas.project.InternalPriority;
import de.metas.project.ProjectId;
import de.metas.project.workorder.resource.WOProjectResourceId;
import de.metas.project.workorder.step.WOProjectStepId;
import de.metas.resource.HumanResourceTestGroupId;
import de.metas.resource.HumanResourceTestGroupRepository;
import de.metas.resource.HumanResourceTestGroupService;
import lombok.NonNull;
import org.adempiere.model.InterfaceWrapperHelper;
import org.adempiere.test.AdempiereTestHelper;
import org.compiere.SpringContextHolder;
import org.compiere.model.I_S_HumanResourceTestGroup;
import org.junit.jupiter.api.Disabled;
import org.optaplanner.core.api.solver.SolverFactory;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@Disabled
public class ManualPOCTest
{
	public static final Duration TERMINATION_SPENT_LIMIT = Duration.ofMinutes(5);
	private static final ProjectId PROJECT_ID1 = ProjectId.ofRepoId(1);
	private static final ProjectId PROJECT_ID2 = ProjectId.ofRepoId(2);
	private static final AtomicInteger nextStepRepoId = new AtomicInteger(1);

	public static void main(String[] args)
	{
		LogManager.setLoggerLevel(SimulationOptimizerTask.class, Level.DEBUG);
		AdempiereTestHelper.get().init();
		SpringContextHolder.registerJUnitBean(new HumanResourceTestGroupService(new HumanResourceTestGroupRepository()));

		final SimulationPlanId simulationId = SimulationPlanId.ofRepoId(123);

		final SimulationOptimizerStatusDispatcher simulationOptimizerStatusDispatcher = new SimulationOptimizerStatusDispatcher();

		final InMemoryPlanLoaderAndSaver planLoaderAndSaver = new InMemoryPlanLoaderAndSaver();
		planLoaderAndSaver.saveSolution(generateProblem(simulationId));

		do
		{
			SimulationOptimizerTask.builder()
					.executorService(Executors.newSingleThreadExecutor())
					.solverFactory(createSolverFactory())
					.simulationOptimizerStatusDispatcher(simulationOptimizerStatusDispatcher)
					.planLoaderAndSaver(planLoaderAndSaver)
					.simulationId(simulationId)
					.onTaskComplete(() -> {}) // do nothing
					.build()
					.run();

			System.out.println("\n\n Press 'q' to quit, any key to run again...");
			final String key = new Scanner(System.in).next();
			if (key.length() > 0 && key.charAt(0) == 'q')
			{
				break;
			}
		}
		while (true);
	}

	private static SolverFactory<Plan> createSolverFactory()
	{
		return SolverFactory.create(SimulationOptimizerConfiguration.solverConfig(null, TERMINATION_SPENT_LIMIT));
	}

	private static Plan generateProblem(SimulationPlanId simulationId)
	{
		final HumanResourceTestGroupId groupId = createGroupId();
		final ArrayList<Step> stepsList = new ArrayList<>();

		// Project 1:
		for (int i = 1; i <= 10; i++)
		{
			stepsList.add(Step.builder()
					.id(StepId.builder()
							.woProjectStepId(WOProjectStepId.ofRepoId(PROJECT_ID1, nextStepRepoId.getAndIncrement()))
							.woProjectResourceId(WOProjectResourceId.ofRepoId(PROJECT_ID1, nextStepRepoId.get()))
							.build())
					.projectPriority(InternalPriority.MEDIUM)
					.resource(resource(i, groupId))
					.duration(Duration.ofHours(1))
					.dueDate(LocalDateTime.parse("2023-08-01T15:00"))
					.startDateMin(LocalDate.parse("2023-04-01").atStartOfDay())
					.humanResourceTestGroupDuration(Duration.ofHours(i))
					.build());
		}

		// Project 2:
		for (int i = 1; i <= 10; i++)
		{
			stepsList.add(Step.builder()
					.id(StepId.builder()
							.woProjectStepId(WOProjectStepId.ofRepoId(PROJECT_ID2, nextStepRepoId.getAndIncrement()))
							.woProjectResourceId(WOProjectResourceId.ofRepoId(PROJECT_ID2, nextStepRepoId.get()))
							.build())
					.projectPriority(InternalPriority.MEDIUM)
					.resource(resource(i, null))
					.duration(Duration.ofHours(1))
					.dueDate(LocalDateTime.parse("2023-05-01T15:00"))
					.startDateMin(LocalDate.parse("2023-04-01").atStartOfDay())
					.humanResourceTestGroupDuration(Duration.ZERO)
					.build());
		}

		// stepsList.stream()
		// 		.filter(step -> ProjectId.equals(step.getProjectId(), PROJECT_ID1)
		// 				&& ResourceId.equals(step.getResource().getId(), resourceId(1)))
		// 		.findFirst()
		// 		.ifPresent(step -> {
		// 			step.setStartDate(LocalDateTime.parse("2023-04-02T17:00"));
		// 			step.updateEndDate();
		// 			step.setPinned(true);
		// 		});

		updatePreviousAndNextStep(stepsList);

		final Plan plan = new Plan();
		plan.setSimulationId(simulationId);
		plan.setTimeZone(SystemTime.zoneId());
		plan.setStepsList(stepsList);

		return plan;
	}

	private static Resource resource(int index, HumanResourceTestGroupId groupId) {return new Resource(resourceId(index), "R" + index, groupId);}

	@NonNull
	private static ResourceId resourceId(final int index) {return ResourceId.ofRepoId(100 + index);}

	private static void updatePreviousAndNextStep(final List<Step> steps)
	{
		ImmutableListMultimap<ProjectId, Step> stepsByProjectId = Multimaps.index(steps, Step::getProjectId);

		for (final ProjectId projectId : stepsByProjectId.keySet())
		{
			final ImmutableList<Step> projectSteps = stepsByProjectId.get(projectId);

			for (int i = 0, lastIndex = projectSteps.size() - 1; i <= lastIndex; i++)
			{
				final Step step = projectSteps.get(i);
				step.setPreviousStep(i == 0 ? null : projectSteps.get(i - 1));
				step.setNextStep(i == lastIndex ? null : projectSteps.get(i + 1));
			}
		}
	}

	private static HumanResourceTestGroupId createGroupId()
	{
		final I_S_HumanResourceTestGroup record = InterfaceWrapperHelper.newInstance(I_S_HumanResourceTestGroup.class);

		record.setGroupIdentifier("testIdentifier");
		record.setName("test");
		record.setDepartment("test");
		record.setCapacityInHours(10);
		InterfaceWrapperHelper.saveRecord(record);

		return HumanResourceTestGroupId.ofRepoId(record.getS_HumanResourceTestGroup_ID());
	}
}

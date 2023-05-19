/*
 * #%L
 * de.metas.business
 * %%
 * Copyright (C) 2023 metas GmbH
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

package de.metas.calendar.plan_optimizer.solver;

import de.metas.calendar.plan_optimizer.domain.Plan;
import de.metas.calendar.plan_optimizer.domain.Project;
import de.metas.calendar.plan_optimizer.domain.Step;
import org.optaplanner.core.api.domain.variable.ListVariableListener;
import org.optaplanner.core.api.score.director.ScoreDirector;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class StartDateUpdatingVariableListener implements ListVariableListener<Plan, Project, Step>
{

	@Override
	public void afterListVariableElementUnassigned(final ScoreDirector<Plan> scoreDirector, final Step step)
	{
		throw new UnsupportedOperationException("This example does not support adding projects.");
	}

	@Override
	public void beforeListVariableChanged(final ScoreDirector<Plan> scoreDirector, final Project project, final int fromIndex, final int toIndex)
	{
		throw new UnsupportedOperationException("This example does not support adding projects.");
	}

	@Override
	public void beforeEntityAdded(final ScoreDirector<Plan> scoreDirector, final Project project)
	{
		throw new UnsupportedOperationException("This example does not support adding projects.");
	}

	@Override
	public void afterEntityAdded(final ScoreDirector<Plan> scoreDirector, final Project project)
	{
		throw new UnsupportedOperationException("This example does not support adding projects.");
	}

	@Override
	public void beforeEntityRemoved(final ScoreDirector<Plan> scoreDirector, final Project project)
	{
		throw new UnsupportedOperationException("This example does not support adding projects.");

	}

	@Override
	public void afterEntityRemoved(final ScoreDirector<Plan> scoreDirector, final Project project)
	{
		throw new UnsupportedOperationException("This example does not support adding projects.");
	}

	@Override
	public void afterListVariableChanged(final ScoreDirector<Plan> scoreDirector, final Project project, final int fromIndex, final int toIndex) //todo fp this one
	{
		computeStartDate(scoreDirector, project, fromIndex);
	}

	private void computeStartDate(final ScoreDirector<Plan> scoreDirector, final Project project, final int index) //todo fp
	{
		final List<Step> steps = project.getSteps();
		LocalDateTime previousEndTime = index == 0 ? null : steps.get(index - 1).getEndDate(); //todo fp

		for (final Step step : steps)
		{
			final LocalDateTime startTime = Optional.ofNullable(previousEndTime)
					.orElseGet(step::getEndDate);

			if (!Objects.equals(step.getStartDate(), startTime))
			{
				scoreDirector.beforeVariableChanged(step, "startTime");
				step.setStartDate(startTime);
				scoreDirector.afterVariableChanged(step, "startTime");
			}

			previousEndTime = step.getEndDate();
		}
	}
}

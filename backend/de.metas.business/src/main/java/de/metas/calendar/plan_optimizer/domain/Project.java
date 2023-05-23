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

package de.metas.calendar.plan_optimizer.domain;

import de.metas.project.InternalPriority;
import de.metas.project.ProjectId;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.Value;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.lookup.PlanningId;
import org.optaplanner.core.api.domain.variable.PlanningListVariable;

import java.util.List;

@PlanningEntity
@Setter
@Getter
public class Project
{
	@PlanningId @NonNull ProjectId projectId;
	@NonNull InternalPriority projectPriority;

	@PlanningListVariable
	@NonNull List<Step> stepList;

	// No-arg constructor required for OptaPlanner
	public Project() {}

	@Builder
	public Project(
			@NonNull final ProjectId projectId,
			@NonNull final InternalPriority projectPriority,
			@NonNull final List<Step> stepList)
	{
		this.projectId = projectId;
		this.projectPriority = projectPriority;
		this.stepList = stepList;
	}
}

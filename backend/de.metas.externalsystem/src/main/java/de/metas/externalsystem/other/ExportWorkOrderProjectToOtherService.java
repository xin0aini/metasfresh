/*
 * #%L
 * de.metas.externalsystem
 * %%
 * Copyright (C) 2022 metas GmbH
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

package de.metas.externalsystem.other;

import de.metas.audit.data.repository.DataExportAuditLogRepository;
import de.metas.audit.data.repository.DataExportAuditRepository;
import de.metas.common.externalsystem.ExternalSystemConstants;
import de.metas.externalsystem.ExternalSystemConfigRepo;
import de.metas.externalsystem.ExternalSystemConfigService;
import de.metas.externalsystem.ExternalSystemType;
import de.metas.externalsystem.IExternalSystemChildConfig;
import de.metas.externalsystem.export.workorder.ExportWorkOrderProjectToExternalSystem;
import de.metas.externalsystem.rabbitmq.ExternalSystemMessageSender;
import de.metas.project.ProjectId;
import de.metas.project.workorder.project.WOProjectRepository;
import lombok.NonNull;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ExportWorkOrderProjectToOtherService extends ExportWorkOrderProjectToExternalSystem
{
	private static final String EXTERNAL_SYSTEM_COMMAND_EXPORT_WORK_ORDER = "exportWorkOrderProject";

	protected ExportWorkOrderProjectToOtherService(
			final @NonNull ExternalSystemConfigRepo externalSystemConfigRepo,
			final @NonNull ExternalSystemMessageSender externalSystemMessageSender,
			final @NonNull DataExportAuditLogRepository dataExportAuditLogRepository,
			final @NonNull DataExportAuditRepository dataExportAuditRepository,
			final @NonNull ExternalSystemConfigService externalSystemConfigService,
			final @NonNull WOProjectRepository woProjectRepository)
	{
		super(externalSystemConfigRepo, externalSystemMessageSender, dataExportAuditLogRepository, dataExportAuditRepository, externalSystemConfigService, woProjectRepository);
	}

	@Override
	protected Map<String, String> buildParameters(
			final @NonNull IExternalSystemChildConfig childConfig,
			final @NonNull ProjectId projectId)
	{
		final ExternalSystemOtherConfig otherConfig = ExternalSystemOtherConfig.cast(childConfig);

		final Map<String, String> parameters = new HashMap<>();
		otherConfig.getParameters().forEach(parameter -> parameters.put(parameter.getName(), parameter.getValue()));

		parameters.put(ExternalSystemConstants.PARAM_PROJECT_ID, String.valueOf(projectId.getRepoId()));

		return parameters;
	}

	@Override
	protected ExternalSystemType getExternalSystemType()
	{
		return ExternalSystemType.Other;
	}

	@Override
	protected String getExternalCommand()
	{
		return EXTERNAL_SYSTEM_COMMAND_EXPORT_WORK_ORDER;
	}
}

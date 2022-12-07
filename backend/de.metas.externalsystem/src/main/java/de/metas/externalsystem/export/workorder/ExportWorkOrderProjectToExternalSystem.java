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

package de.metas.externalsystem.export.workorder;

import ch.qos.logback.classic.Level;
import de.metas.audit.data.repository.DataExportAuditLogRepository;
import de.metas.audit.data.repository.DataExportAuditRepository;
import de.metas.common.externalsystem.JsonExternalSystemName;
import de.metas.common.externalsystem.JsonExternalSystemRequest;
import de.metas.common.rest_api.common.JsonMetasfreshId;
import de.metas.externalsystem.ExternalSystemConfigRepo;
import de.metas.externalsystem.ExternalSystemConfigService;
import de.metas.externalsystem.ExternalSystemParentConfig;
import de.metas.externalsystem.IExternalSystemChildConfig;
import de.metas.externalsystem.IExternalSystemChildConfigId;
import de.metas.externalsystem.export.ExportToExternalSystemService;
import de.metas.externalsystem.rabbitmq.ExternalSystemMessageSender;
import de.metas.logging.LogManager;
import de.metas.process.PInstanceId;
import de.metas.project.ProjectId;
import de.metas.project.workorder.project.WOProject;
import de.metas.project.workorder.project.WOProjectRepository;
import de.metas.util.Loggables;
import lombok.NonNull;
import org.adempiere.util.lang.impl.TableRecordReference;
import org.compiere.model.I_C_Project;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public abstract class ExportWorkOrderProjectToExternalSystem extends ExportToExternalSystemService
{
	private static final Logger logger = LogManager.getLogger(ExportWorkOrderProjectToExternalSystem.class);

	private final ExternalSystemConfigService externalSystemConfigService;
	private final WOProjectRepository woProjectRepository;

	protected ExportWorkOrderProjectToExternalSystem(
			@NonNull final ExternalSystemConfigRepo externalSystemConfigRepo,
			@NonNull final ExternalSystemMessageSender externalSystemMessageSender,
			@NonNull final DataExportAuditLogRepository dataExportAuditLogRepository,
			@NonNull final DataExportAuditRepository dataExportAuditRepository,
			@NonNull final ExternalSystemConfigService externalSystemConfigService,
			@NonNull final WOProjectRepository woProjectRepository)
	{
		super(dataExportAuditRepository, dataExportAuditLogRepository, externalSystemConfigRepo, externalSystemMessageSender);

		this.externalSystemConfigService = externalSystemConfigService;
		this.woProjectRepository = woProjectRepository;
	}

	@Override
	protected Optional<JsonExternalSystemRequest> getExportExternalSystemRequest(
			@NonNull final IExternalSystemChildConfigId externalSystemChildConfigId,
			@NonNull final TableRecordReference recordReference,
			@NonNull final PInstanceId pInstanceId)
	{
		final ProjectId projectId = recordReference.getIdAssumingTableName(I_C_Project.Table_Name, ProjectId::ofRepoId);

		final ExternalSystemParentConfig config = externalSystemConfigRepo.getById(externalSystemChildConfigId);

		if (!config.isActive())
		{
			Loggables.withLogger(logger, Level.DEBUG).addLog("ExternalSystemParentConfig: {} isActive = false! No action is performed!", config.getId());
			return Optional.empty();
		}

		final WOProject project = woProjectRepository.getById(projectId);

		final String orgCode = orgDAO.getById(project.getOrgId()).getValue();

		return Optional.of(JsonExternalSystemRequest.builder()
								   .externalSystemName(JsonExternalSystemName.of(getExternalSystemType().getName()))
								   .externalSystemConfigId(JsonMetasfreshId.of(config.getId().getRepoId()))
								   .orgCode(orgCode)
								   .adPInstanceId(JsonMetasfreshId.ofOrNull(PInstanceId.toRepoId(pInstanceId)))
								   .command(getExternalCommand())
								   .parameters(buildParameters(config.getChildConfig(), projectId))
								   .traceId(externalSystemConfigService.getTraceId())
								   .externalSystemChildConfigValue(config.getChildConfig().getValue())
								   .writeAuditEndpoint(config.getAuditEndpointIfEnabled())
								   .build());
	}

	@Override
	protected void runPreExportHook(final TableRecordReference recordReferenceToExport) {}

	@Override
	public int getCurrentPendingItems()
	{
		return 0;
	}

	protected abstract Map<String, String> buildParameters(@NonNull final IExternalSystemChildConfig childConfig, @NonNull final ProjectId projectId);

	protected abstract String getExternalCommand();
}

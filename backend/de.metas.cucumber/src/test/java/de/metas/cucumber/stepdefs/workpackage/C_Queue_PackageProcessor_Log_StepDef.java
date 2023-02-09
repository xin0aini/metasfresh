/*
 * #%L
 * de.metas.cucumber
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

package de.metas.cucumber.stepdefs.workpackage;

import de.metas.async.model.I_C_Queue_PackageProcessor;
import de.metas.logging.LogManager;
import de.metas.util.Services;
import io.cucumber.java.en.And;
import org.adempiere.ad.dao.IQueryBL;
import org.adempiere.ad.migration.model.I_AD_MigrationScript;
import org.slf4j.Logger;

public class C_Queue_PackageProcessor_Log_StepDef
{
	private final IQueryBL queryBL = Services.get(IQueryBL.class);
	private final static Logger logger = LogManager.getLogger(C_Queue_PackageProcessor_Log_StepDef.class);

	@And("run package processor logs")
	public void runPackageProcessorLogs()
	{
		final StringBuilder migrationScriptLogs = new StringBuilder();

		queryBL.createQueryBuilder(I_AD_MigrationScript.class)
				.addStringLikeFilter(I_AD_MigrationScript.COLUMNNAME_Name, "5611420_sys_gh11947_entity_de_metas_salesorder", true)
				.setJoinOr()
				.addStringLikeFilter(I_AD_MigrationScript.COLUMNNAME_Name, "5611430_sys_gh11947_wp_CompleteShipAndInvoiceWorkpackageProcessor", true)
				.setJoinOr()
				.addStringLikeFilter(I_AD_MigrationScript.COLUMNNAME_Name, "5612270_sys_gh11947_wp_ProcessOLCandsWorkpackageProcessor", true)
				.setJoinOr()
				.addStringLikeFilter(I_AD_MigrationScript.COLUMNNAME_Name, "5611419_sys_gh11947_fix_folder_name", true)
				.stream()
				.forEach(record ->
								 migrationScriptLogs.append(I_AD_MigrationScript.COLUMNNAME_AD_MigrationScript_ID).append(" : ").append(record.getAD_MigrationScript_ID()).append(" ; ")
										 .append(I_AD_MigrationScript.COLUMNNAME_Name).append(" : ").append(record.getFileName()).append(" ; ")
										 .append(I_AD_MigrationScript.COLUMNNAME_CreatedBy).append(" : ").append(record.getCreatedBy()).append(" ; ")
										 .append(I_AD_MigrationScript.COLUMNNAME_Created).append(" : ").append(record.getCreated()).append(" ; ")
										 .append(I_AD_MigrationScript.COLUMNNAME_Updated).append(" : ").append(record.getUpdated()).append(" ; ")
										 .append("\n\n"));

		logger.error("******* AD_MigrationScript logs: \n" + migrationScriptLogs.toString());

		final StringBuilder queuePackageProcessorLogs = new StringBuilder();

		queryBL.createQueryBuilder(I_C_Queue_PackageProcessor.class)
				.addStringLikeFilter(I_C_Queue_PackageProcessor.COLUMNNAME_Classname, "CompleteShipAndInvoiceWorkpackageProcessor", true)
				.setJoinOr()
				.addStringLikeFilter(I_C_Queue_PackageProcessor.COLUMNNAME_Classname, "ProcessOLCandsWorkpackageProcessor", true)
				.stream()
				.forEach(record ->
								 queuePackageProcessorLogs.append(I_C_Queue_PackageProcessor.COLUMNNAME_C_Queue_PackageProcessor_ID).append(" : ").append(record.getC_Queue_PackageProcessor_ID()).append(" ; ")
										 .append(I_C_Queue_PackageProcessor.COLUMNNAME_Classname).append(" : ").append(record.getClassname()).append(" ; ")
										 .append(I_C_Queue_PackageProcessor.COLUMNNAME_CreatedBy).append(" : ").append(record.getCreatedBy()).append(" ; ")
										 .append(I_C_Queue_PackageProcessor.COLUMNNAME_Created).append(" : ").append(record.getCreated()).append(" ; ")
										 .append(I_C_Queue_PackageProcessor.COLUMNNAME_Updated).append(" : ").append(record.getUpdated()).append(" ; ")
										 .append("\n\n"));

		logger.error("******* C_Queue_PackageProcessor logs: \n" + queuePackageProcessorLogs.toString());
	}
}

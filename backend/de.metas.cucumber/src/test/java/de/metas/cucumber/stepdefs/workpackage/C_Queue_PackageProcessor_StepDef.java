/*
 * #%L
 * de.metas.cucumber
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

package de.metas.cucumber.stepdefs.workpackage;

import de.metas.async.model.I_C_Queue_PackageProcessor;
import de.metas.cucumber.stepdefs.DataTableUtil;
import de.metas.logging.LogManager;
import de.metas.util.Services;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import lombok.NonNull;
import org.adempiere.ad.dao.IQueryBL;
import org.adempiere.ad.migration.model.I_AD_MigrationScript;
import org.slf4j.Logger;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

public class C_Queue_PackageProcessor_StepDef
{
	private final IQueryBL queryBL = Services.get(IQueryBL.class);
	private final static Logger logger = LogManager.getLogger(C_Queue_PackageProcessor_StepDef.class);

	@NonNull
	private final C_Queue_PackageProcessor_StepDefData packageProcessorTable;

	public C_Queue_PackageProcessor_StepDef(final C_Queue_PackageProcessor_StepDefData packageProcessorTable)
	{
		this.packageProcessorTable = packageProcessorTable;
	}

	@And("load C_Queue_PackageProcessor by classname:")
	public void load_C_Queue_PackageProcessor(@NonNull final DataTable dataTable)
	{
		for (final Map<String, String> row : dataTable.asMaps())
		{
			final String classname = DataTableUtil.extractStringForColumnName(row, I_C_Queue_PackageProcessor.COLUMNNAME_Classname);

			assertThat(classname).isNotNull();

			final I_C_Queue_PackageProcessor packageProcessor = queryBL.createQueryBuilder(I_C_Queue_PackageProcessor.class)
					.addOnlyActiveRecordsFilter()
					.addEqualsFilter(I_C_Queue_PackageProcessor.COLUMNNAME_Classname, classname)
					.create()
					.firstOnlyNotNull(I_C_Queue_PackageProcessor.class);

			final String packageProcessIdentifier = DataTableUtil.extractStringForColumnName(row, I_C_Queue_PackageProcessor.COLUMNNAME_C_Queue_PackageProcessor_ID + ".Identifier");

			packageProcessorTable.putOrReplace(packageProcessIdentifier, packageProcessor);
		}
	}

	@And("log workpackage")
	public void logg()
	{
		final StringBuilder message = new StringBuilder();

		queryBL.createQueryBuilder(I_AD_MigrationScript.class)
				.addStringLikeFilter(I_AD_MigrationScript.COLUMNNAME_FileName, "5611420_sys_gh11947_entity_de_metas_salesorder.sql", true)
				.setJoinOr()
				.addStringLikeFilter(I_AD_MigrationScript.COLUMNNAME_FileName, "5611430_sys_gh11947_wp_CompleteShipAndInvoiceWorkpackageProcessor.sql", true)
				.setJoinOr()
				.addStringLikeFilter(I_AD_MigrationScript.COLUMNNAME_FileName, "5612270_sys_gh11947_wp_ProcessOLCandsWorkpackageProcessor.sql", true)
				.setJoinOr()
				.addStringLikeFilter(I_AD_MigrationScript.COLUMNNAME_FileName, "5611419_sys_gh11947_fix_folder_name.sql", true)
				.stream()
				.forEach(record ->
								 message.append(I_AD_MigrationScript.COLUMNNAME_AD_MigrationScript_ID).append(" : ").append(record.getAD_MigrationScript_ID()).append(" ; ")
										 .append(I_AD_MigrationScript.COLUMNNAME_FileName).append(" : ").append(record.getFileName()).append(" ; ")
										 .append(I_AD_MigrationScript.COLUMNNAME_CreatedBy).append(" : ").append(record.getCreatedBy()).append(" ; ")
										 .append(I_AD_MigrationScript.COLUMNNAME_Created).append(" : ").append(record.getCreated()).append(" ; ")
										 .append(I_AD_MigrationScript.COLUMNNAME_Updated).append(" : ").append(record.getUpdated()).append(" ; ")
										 .append("\n\n"));

		final StringBuilder messagePP = new StringBuilder();

		queryBL.createQueryBuilder(I_C_Queue_PackageProcessor.class)
				.addStringLikeFilter(I_C_Queue_PackageProcessor.COLUMNNAME_Classname, "CompleteShipAndInvoiceWorkpackageProcessor", true)
				.setJoinOr()
				.addStringLikeFilter(I_C_Queue_PackageProcessor.COLUMNNAME_Classname, "ProcessOLCandsWorkpackageProcessor", true)
				.stream()
				.forEach(record ->
								 messagePP.append(I_C_Queue_PackageProcessor.COLUMNNAME_C_Queue_PackageProcessor_ID).append(" : ").append(record.getC_Queue_PackageProcessor_ID()).append(" ; ")
										 .append(I_C_Queue_PackageProcessor.COLUMNNAME_Classname).append(" : ").append(record.getClassname()).append(" ; ")
										 .append(I_C_Queue_PackageProcessor.COLUMNNAME_CreatedBy).append(" : ").append(record.getCreatedBy()).append(" ; ")
										 .append(I_C_Queue_PackageProcessor.COLUMNNAME_Created).append(" : ").append(record.getCreated()).append(" ; ")
										 .append(I_C_Queue_PackageProcessor.COLUMNNAME_Updated).append(" : ").append(record.getUpdated()).append(" ; ")
										 .append("\n\n"));

		logger.error("******* AD_MigrationScript logs: \n" + message.toString());
		logger.error("******* C_Queue_PackageProcessor logs: \n" + messagePP.toString());
	}
}

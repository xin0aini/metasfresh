/*
 * #%L
 * de.metas.contracts
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

package de.metas.contracts.modular.impexp;

import de.metas.contracts.model.I_I_ModCntr_Log;
import de.metas.impexp.processing.IImportProcessFactory;
import org.springframework.context.annotation.Configuration;
import de.metas.util.Services;

@Configuration
public class ModularContractLogImportProcessConfiguration
{
	public ModularContractLogImportProcessConfiguration()
	{
		registerStandardImportProcesses();
	}

	private void registerStandardImportProcesses()
	{
		final IImportProcessFactory importProcessesFactory = Services.get(IImportProcessFactory.class);
		importProcessesFactory.registerImportProcess(I_I_ModCntr_Log.class, ModularContractLogImportProcess.class);
	}
}

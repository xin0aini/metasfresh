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

package de.metas.cucumber.stepdefs.foreignexchangecontract;

import de.metas.cucumber.stepdefs.C_Currency_StepDefData;
import de.metas.cucumber.stepdefs.C_Order_StepDefData;
import de.metas.cucumber.stepdefs.DataTableUtil;
import de.metas.cucumber.stepdefs.StepDefDocAction;
import de.metas.document.engine.IDocument;
import de.metas.document.engine.IDocumentBL;
import de.metas.forex.ForexContractId;
import de.metas.forex.ForexContractService;
import de.metas.i18n.BooleanWithReason;
import de.metas.order.OrderId;
import de.metas.util.Services;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import lombok.NonNull;
import org.adempiere.exceptions.AdempiereException;
import org.adempiere.model.InterfaceWrapperHelper;
import org.compiere.SpringContextHolder;
import org.compiere.model.I_C_ForeignExchangeContract;
import org.compiere.model.I_C_Order;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Map;

import static de.metas.cucumber.stepdefs.StepDefConstants.TABLECOLUMN_IDENTIFIER;
import static org.compiere.model.I_C_ForeignExchangeContract.COLUMNNAME_C_Currency_ID;
import static org.compiere.model.I_C_ForeignExchangeContract.COLUMNNAME_C_ForeignExchangeContract_ID;
import static org.compiere.model.I_C_ForeignExchangeContract.COLUMNNAME_FEC_Amount;
import static org.compiere.model.I_C_ForeignExchangeContract.COLUMNNAME_FEC_Amount_Alloc;
import static org.compiere.model.I_C_ForeignExchangeContract.COLUMNNAME_FEC_Amount_Open;
import static org.compiere.model.I_C_ForeignExchangeContract.COLUMNNAME_FEC_CurrencyRate;
import static org.compiere.model.I_C_ForeignExchangeContract.COLUMNNAME_FEC_MaturityDate;
import static org.compiere.model.I_C_ForeignExchangeContract.COLUMNNAME_FEC_ValidityDate;
import static org.compiere.model.I_C_ForeignExchangeContract.COLUMNNAME_To_Currency_ID;

public class C_ForeignExchangeContract_StepDef
{
	private final IDocumentBL documentBL = Services.get(IDocumentBL.class);
	private final ForexContractService forexContractService = SpringContextHolder.instance.getBean(ForexContractService.class);

	private final C_Currency_StepDefData currencyTable;
	private final C_Order_StepDefData orderTable;
	private final C_ForeignExchangeContract_StepDefData foreignExchangeContractTable;

	public C_ForeignExchangeContract_StepDef(
			@NonNull final C_Currency_StepDefData currencyTable,
			@NonNull final C_Order_StepDefData orderTable,
			@NonNull final C_ForeignExchangeContract_StepDefData foreignExchangeContractTable)
	{
		this.currencyTable = currencyTable;
		this.orderTable = orderTable;
		this.foreignExchangeContractTable = foreignExchangeContractTable;
	}

	@And("metasfresh contains C_ForeignExchangeContract")
	public void create_C_ForeignExchangeContract(@NonNull final DataTable dataTable)
	{
		for (final Map<String, String> row : dataTable.asMaps())
		{
			final String currencyIdentifier = DataTableUtil.extractStringForColumnName(row, COLUMNNAME_C_Currency_ID + "." + TABLECOLUMN_IDENTIFIER);
			final String toCurrencyIdentifier = DataTableUtil.extractStringForColumnName(row, COLUMNNAME_To_Currency_ID + "." + TABLECOLUMN_IDENTIFIER);
			final BigDecimal fecCurrencyRate = DataTableUtil.extractBigDecimalForColumnName(row, COLUMNNAME_FEC_CurrencyRate);
			final BigDecimal fecAmount = DataTableUtil.extractBigDecimalForColumnName(row, COLUMNNAME_FEC_Amount);
			final BigDecimal fecAmountAlloc = DataTableUtil.extractBigDecimalForColumnName(row, COLUMNNAME_FEC_Amount_Alloc);
			final BigDecimal fecAmountOpen = DataTableUtil.extractBigDecimalForColumnName(row, COLUMNNAME_FEC_Amount_Open);
			final Timestamp maturityDate = DataTableUtil.extractDateTimestampForColumnName(row, COLUMNNAME_FEC_MaturityDate);
			final Timestamp validityDate = DataTableUtil.extractDateTimestampForColumnName(row, COLUMNNAME_FEC_ValidityDate);

			final I_C_ForeignExchangeContract foreignExchangeContract = InterfaceWrapperHelper.newInstance(I_C_ForeignExchangeContract.class);
			foreignExchangeContract.setC_Currency_ID(currencyTable.get(currencyIdentifier).getC_Currency_ID());
			foreignExchangeContract.setTo_Currency_ID(currencyTable.get(toCurrencyIdentifier).getC_Currency_ID());
			foreignExchangeContract.setFEC_CurrencyRate(fecCurrencyRate);
			foreignExchangeContract.setFEC_MaturityDate(maturityDate);
			foreignExchangeContract.setFEC_ValidityDate(validityDate);
			foreignExchangeContract.setFEC_Amount(fecAmount);
			foreignExchangeContract.setFEC_Amount_Alloc(fecAmountAlloc);
			foreignExchangeContract.setFEC_Amount_Open(fecAmountOpen);

			InterfaceWrapperHelper.saveRecord(foreignExchangeContract);

			final String foreignExchangeContractIdentifier = DataTableUtil.extractStringForColumnName(row, COLUMNNAME_C_ForeignExchangeContract_ID + "." + TABLECOLUMN_IDENTIFIER);
			foreignExchangeContractTable.put(foreignExchangeContractIdentifier, foreignExchangeContract);
		}
	}

	@And("allocate C_ForeignExchangeContract to order")
	public void allocate_C_ForeignExchangeContract_to_order(@NonNull final DataTable dataTable)
	{
		for (final Map<String, String> row : dataTable.asMaps())
		{
			final String foreignExchangeContractIdentifier = DataTableUtil.extractStringForColumnName(row, COLUMNNAME_C_ForeignExchangeContract_ID + "." + TABLECOLUMN_IDENTIFIER);
			final String orderIdentifier = DataTableUtil.extractStringForColumnName(row, I_C_Order.COLUMNNAME_C_Order_ID + "." + TABLECOLUMN_IDENTIFIER);
			final BigDecimal fecAmountAlloc = DataTableUtil.extractBigDecimalForColumnName(row, COLUMNNAME_FEC_Amount_Alloc);

			final ForexContractId forexContractId = ForexContractId.ofRepoId(foreignExchangeContractTable.get(foreignExchangeContractIdentifier).getC_ForeignExchangeContract_ID());
			final OrderId orderId = OrderId.ofRepoId(orderTable.get(orderIdentifier).getC_Order_ID());

			final BooleanWithReason orderEligibleToAllocate = forexContractService.checkOrderEligibleToAllocate(orderId);

			if (orderEligibleToAllocate.isFalse())
			{
				throw new AdempiereException(orderEligibleToAllocate.getReason());
			}

			forexContractService.allocateOrder(forexContractId, orderId, fecAmountAlloc);
		}
	}

	@And("^the foreign exchange contract identified by (.*) is (reactivated|completed)$")
	public void order_action(@NonNull final String foreignExchangeContractIdentifier, @NonNull final String action)
	{
		final I_C_ForeignExchangeContract foreignExchangeContract = foreignExchangeContractTable.get(foreignExchangeContractIdentifier);

		switch (StepDefDocAction.valueOf(action))
		{
			case reactivated:
				foreignExchangeContract.setDocAction(IDocument.ACTION_Complete); // we need this because otherwise MOrder.completeIt() won't complete it
				documentBL.processEx(foreignExchangeContract, IDocument.ACTION_ReActivate, IDocument.STATUS_InProgress);
				break;
			case completed:
				foreignExchangeContract.setDocAction(IDocument.ACTION_Complete); // we need this because otherwise MOrder.completeIt() won't complete it
				documentBL.processEx(foreignExchangeContract, IDocument.ACTION_Complete, IDocument.STATUS_Completed);
				break;
			default:
				throw new AdempiereException("Unhandled C_Order action")
						.appendParametersToMessage()
						.setParameter("action:", action);
		}
	}
}

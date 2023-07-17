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

package de.metas.cucumber.stepdefs.deliveryplanning;

import de.metas.cucumber.stepdefs.C_BPartner_Location_StepDefData;
import de.metas.cucumber.stepdefs.C_BPartner_StepDefData;
import de.metas.cucumber.stepdefs.C_Currency_StepDefData;
import de.metas.cucumber.stepdefs.C_OrderLine_StepDefData;
import de.metas.cucumber.stepdefs.C_Order_StepDefData;
import de.metas.cucumber.stepdefs.DataTableUtil;
import de.metas.cucumber.stepdefs.M_Product_StepDefData;
import de.metas.cucumber.stepdefs.M_Shipper_StepDefData;
import de.metas.cucumber.stepdefs.StepDefDocAction;
import de.metas.cucumber.stepdefs.StepDefUtil;
import de.metas.cucumber.stepdefs.foreignexchangecontract.C_ForeignExchangeContract_StepDefData;
import de.metas.cucumber.stepdefs.warehouse.M_Warehouse_StepDefData;
import de.metas.deliveryplanning.DeliveryPlanningId;
import de.metas.deliveryplanning.DeliveryPlanningReceiptInfo;
import de.metas.deliveryplanning.DeliveryPlanningService;
import de.metas.deliveryplanning.DeliveryPlanningShipmentInfo;
import de.metas.forex.ForexContractId;
import de.metas.forex.ForexContractRef;
import de.metas.handlingunits.model.I_M_ReceiptSchedule;
import de.metas.handlingunits.model.I_M_ShipmentSchedule;
import de.metas.handlingunits.receiptschedule.IHUReceiptScheduleBL;
import de.metas.handlingunits.shipmentschedule.api.M_ShipmentSchedule_QuantityTypeToUse;
import de.metas.handlingunits.shipmentschedule.api.ShipmentScheduleEnqueuer;
import de.metas.i18n.IMsgBL;
import de.metas.inout.ShipmentScheduleId;
import de.metas.inoutcandidate.api.InOutGenerateResult;
import de.metas.inoutcandidate.api.impl.ReceiptMovementDateRule;
import de.metas.money.CurrencyId;
import de.metas.process.IADPInstanceDAO;
import de.metas.shipping.model.I_M_ShipperTransportation;
import de.metas.util.Check;
import de.metas.util.Services;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.When;
import lombok.NonNull;
import org.adempiere.ad.dao.IQueryBL;
import org.adempiere.ad.dao.IQueryBuilder;
import org.adempiere.ad.dao.IQueryFilter;
import org.adempiere.ad.trx.api.ITrx;
import org.adempiere.exceptions.AdempiereException;
import org.adempiere.model.InterfaceWrapperHelper;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.compiere.SpringContextHolder;
import org.compiere.model.I_C_BPartner;
import org.compiere.model.I_C_BPartner_Location;
import org.compiere.model.I_C_ForeignExchangeContract;
import org.compiere.model.I_C_Order;
import org.compiere.model.I_C_OrderLine;
import org.compiere.model.I_M_Delivery_Planning;
import org.compiere.model.I_M_Product;
import org.compiere.model.I_M_Shipper;
import org.compiere.model.I_M_Warehouse;
import org.compiere.util.Env;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import static de.metas.cucumber.stepdefs.StepDefConstants.TABLECOLUMN_IDENTIFIER;
import static org.adempiere.model.InterfaceWrapperHelper.saveRecord;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class M_Delivery_Planning_StepDef
{
	private final HashMap<DeliveryPlanningId, Optional<DeliveryPlanningReceiptInfo>> receiptInfos = new HashMap<>();

	private final DeliveryPlanningService deliveryPlanningService = SpringContextHolder.instance.getBean(DeliveryPlanningService.class);

	private final M_Delivery_Planning_StepDefData deliveryPlanningTable;
	private final C_Order_StepDefData orderTable;
	private final C_OrderLine_StepDefData orderLineTable;
	private final M_Product_StepDefData productTable;
	private final C_BPartner_StepDefData bpartnerTable;
	private final M_Shipper_StepDefData shipperTable;
	private final C_BPartner_Location_StepDefData bPartnerLocationTable;
	private final M_Warehouse_StepDefData warehouseTable;
	private final M_ShipperTransportation_StepDefData deliveryInstructionTable;
	private final C_ForeignExchangeContract_StepDefData foreignExchangeContractTable;
	private final C_Currency_StepDefData currencyTable;

	private final IQueryBL queryBL = Services.get(IQueryBL.class);
	private final IMsgBL msgBL = Services.get(IMsgBL.class);
	private final IADPInstanceDAO pinstanceDAO = Services.get(IADPInstanceDAO.class);
	private final IHUReceiptScheduleBL huReceiptScheduleBL = Services.get(IHUReceiptScheduleBL.class);

	public M_Delivery_Planning_StepDef(
			@NonNull final M_Delivery_Planning_StepDefData deliveryPlanningTable,
			@NonNull final C_Order_StepDefData orderTable,
			@NonNull final C_OrderLine_StepDefData orderLineTable,
			@NonNull final M_Product_StepDefData productTable,
			@NonNull final C_BPartner_StepDefData bpartnerTable,
			@NonNull final M_Shipper_StepDefData shipperTable,
			@NonNull final C_BPartner_Location_StepDefData bPartnerLocationTable,
			@NonNull final M_Warehouse_StepDefData warehouseTable,
			@NonNull final M_ShipperTransportation_StepDefData deliveryInstructionTable,
			@NonNull final C_ForeignExchangeContract_StepDefData foreignExchangeContractTable,
			@NonNull final C_Currency_StepDefData currencyTable)
	{
		this.deliveryPlanningTable = deliveryPlanningTable;
		this.orderTable = orderTable;
		this.orderLineTable = orderLineTable;
		this.productTable = productTable;
		this.bpartnerTable = bpartnerTable;
		this.shipperTable = shipperTable;
		this.bPartnerLocationTable = bPartnerLocationTable;
		this.warehouseTable = warehouseTable;
		this.deliveryInstructionTable = deliveryInstructionTable;
		this.foreignExchangeContractTable = foreignExchangeContractTable;
		this.currencyTable = currencyTable;
	}

	@And("^after not more than (.*)s, load created M_Delivery_Planning:$")
	public void load_created_M_Delivery_Planning(final int timeoutSec, @NonNull final DataTable dataTable) throws InterruptedException
	{
		for (final Map<String, String> row : dataTable.asMaps())
		{
			final String orderLineIdentifier = DataTableUtil.extractStringForColumnName(row, I_M_Delivery_Planning.COLUMNNAME_C_OrderLine_ID + "." + TABLECOLUMN_IDENTIFIER);
			final I_C_OrderLine orderLine = orderLineTable.get(orderLineIdentifier);
			assertThat(orderLine).isNotNull();

			final String deliveryPlanningIdentifiers = DataTableUtil.extractStringForColumnName(row, I_M_Delivery_Planning.COLUMNNAME_M_Delivery_Planning_ID + "." + TABLECOLUMN_IDENTIFIER + "s");
			final List<String> identifiers = StepDefUtil.splitIdentifiers(deliveryPlanningIdentifiers);
			final int numberOfRecordsToLoad = identifiers.size();

			final IQueryBuilder<I_M_Delivery_Planning> queryBuilder = queryBL.createQueryBuilder(I_M_Delivery_Planning.class)
					.addEqualsFilter(I_M_Delivery_Planning.COLUMNNAME_C_OrderLine_ID, orderLine.getC_OrderLine_ID());

			final Supplier<Boolean> isDeliveryPlanningFound = () ->
			{
				final int deliveryPlanningRecords = queryBuilder.create().count();

				if (deliveryPlanningRecords > numberOfRecordsToLoad)
				{
					throw new AdempiereException("Found more M_Delivery_Planning records than expected for C_OrderLine_ID=" + orderLine.getC_OrderLine_ID())
							.appendParametersToMessage()
							.setParameter("ExpectedCount", numberOfRecordsToLoad)
							.setParameter("ActualCount", deliveryPlanningRecords);
				}

				return deliveryPlanningRecords == numberOfRecordsToLoad;
			};

			StepDefUtil.tryAndWait(timeoutSec, 1000, isDeliveryPlanningFound);

			final List<I_M_Delivery_Planning> deliveryPlannings = queryBL.createQueryBuilder(I_M_Delivery_Planning.class)
					.addEqualsFilter(I_M_Delivery_Planning.COLUMNNAME_C_OrderLine_ID, orderLine.getC_OrderLine_ID())
					.orderBy(I_M_Delivery_Planning.COLUMNNAME_M_Delivery_Planning_ID)
					.create()
					.list();

			for (int rowIndex = 0; rowIndex < numberOfRecordsToLoad; rowIndex++)
			{
				final String deliveryPlanningIdentifier = identifiers.get(rowIndex);
				deliveryPlanningTable.putOrReplace(deliveryPlanningIdentifier, deliveryPlannings.get(rowIndex));
			}
		}
	}

	@And("^generate (.*) additional M_Delivery_Planning records for: (.*)$")
	public void generate_additional_M_Delivery_Planning(final int noAdditionalRecords, @NonNull final String deliveryPlanningIdentifier)
	{
		final I_M_Delivery_Planning deliveryPlanning = deliveryPlanningTable.get(deliveryPlanningIdentifier);
		assertThat(deliveryPlanning).isNotNull();

		final DeliveryPlanningId deliveryPlanningId = DeliveryPlanningId.ofRepoId(deliveryPlanning.getM_Delivery_Planning_ID());
		deliveryPlanningService.createAdditionalDeliveryPlannings(deliveryPlanningId, noAdditionalRecords);
	}

	@When("delete M_Delivery_Planning:")
	public void delete_M_Delivery_Planning(@NonNull final DataTable dataTable)
	{
		for (final Map<String, String> row : dataTable.asMaps())
		{
			final String deliveryPlanningIdentifier = DataTableUtil.extractStringForColumnName(row, I_M_Delivery_Planning.COLUMNNAME_M_Delivery_Planning_ID + "." + TABLECOLUMN_IDENTIFIER);
			final I_M_Delivery_Planning deliveryPlanning = deliveryPlanningTable.get(deliveryPlanningIdentifier);
			assertThat(deliveryPlanning).isNotNull();

			final String errorMsg = DataTableUtil.extractStringOrNullForColumnName(row, "OPT.ErrorMessage");
			try
			{
				deliveryPlanningService.validateDeletion(deliveryPlanning);
				if (Check.isNotBlank(errorMsg))
				{
					throw new RuntimeException("Was expecting operation to fail!");
				}
				InterfaceWrapperHelper.delete(deliveryPlanning);

			}
			catch (final AdempiereException e)
			{
				assertThat(e.getMessage()).contains(errorMsg);
			}
		}
	}

	@And("validate M_Delivery_Planning:")
	public void validate_M_Delivery_Planning(@NonNull final DataTable dataTable)
	{
		for (final Map<String, String> row : dataTable.asMaps())
		{
			final String deliveryPlanningIdentifier = DataTableUtil.extractStringForColumnName(row, I_M_Delivery_Planning.COLUMNNAME_M_Delivery_Planning_ID + "." + TABLECOLUMN_IDENTIFIER);
			final I_M_Delivery_Planning deliveryPlanning = deliveryPlanningTable.get(deliveryPlanningIdentifier);
			assertThat(deliveryPlanning).isNotNull();
			InterfaceWrapperHelper.refresh(deliveryPlanning);

			final SoftAssertions softly = new SoftAssertions();

			final BigDecimal qtyOrdered = DataTableUtil.extractBigDecimalForColumnName(row, I_M_Delivery_Planning.COLUMNNAME_QtyOrdered);
			softly.assertThat(deliveryPlanning.getQtyOrdered()).as(I_M_Delivery_Planning.COLUMNNAME_QtyOrdered).isEqualTo(qtyOrdered);

			final BigDecimal qtyTotalOpen = DataTableUtil.extractBigDecimalForColumnName(row, I_M_Delivery_Planning.COLUMNNAME_QtyTotalOpen);
			softly.assertThat(deliveryPlanning.getQtyTotalOpen()).as(I_M_Delivery_Planning.COLUMNNAME_QtyTotalOpen).isEqualTo(qtyTotalOpen);

			final String type = DataTableUtil.extractStringForColumnName(row, I_M_Delivery_Planning.COLUMNNAME_M_Delivery_Planning_Type);
			softly.assertThat(deliveryPlanning.getM_Delivery_Planning_Type()).as(I_M_Delivery_Planning.COLUMNNAME_M_Delivery_Planning_Type).isEqualTo(type);

			final Boolean isB2B = DataTableUtil.extractBooleanForColumnNameOr(row, I_M_Delivery_Planning.COLUMNNAME_IsB2B, null);
			if (isB2B != null)
			{
				softly.assertThat(deliveryPlanning.isB2B()).as(I_M_Delivery_Planning.COLUMNNAME_IsB2B).isEqualTo(isB2B);
			}

			final String productIdentifier = DataTableUtil.extractStringOrNullForColumnName(row, "OPT." + I_M_Delivery_Planning.COLUMNNAME_M_Product_ID + "." + TABLECOLUMN_IDENTIFIER);
			if (Check.isNotBlank(productIdentifier))
			{
				final I_M_Product product = productTable.get(productIdentifier);
				softly.assertThat(product).isNotNull();

				softly.assertThat(deliveryPlanning.getM_Product_ID()).as(I_M_Delivery_Planning.COLUMNNAME_M_Product_ID).isEqualTo(product.getM_Product_ID());
			}

			final String bpartnerIdentifier = DataTableUtil.extractStringOrNullForColumnName(row, "OPT." + I_M_Delivery_Planning.COLUMNNAME_C_BPartner_ID + "." + TABLECOLUMN_IDENTIFIER);
			if (Check.isNotBlank(bpartnerIdentifier))
			{
				final I_C_BPartner bPartner = bpartnerTable.get(bpartnerIdentifier);
				softly.assertThat(bPartner).isNotNull();
				softly.assertThat(deliveryPlanning.getC_BPartner_ID()).as(I_M_Delivery_Planning.COLUMNNAME_C_BPartner_ID).isEqualTo(bPartner.getC_BPartner_ID());
			}

			final String orderIdentifier = DataTableUtil.extractStringOrNullForColumnName(row, "OPT." + I_M_Delivery_Planning.COLUMNNAME_C_Order_ID + "." + TABLECOLUMN_IDENTIFIER);
			if (Check.isNotBlank(orderIdentifier))
			{
				final I_C_Order order = orderTable.get(orderIdentifier);
				softly.assertThat(order).isNotNull();
				softly.assertThat(deliveryPlanning.getC_Order_ID()).as(I_M_Delivery_Planning.COLUMNNAME_C_Order_ID).isEqualTo(order.getC_Order_ID());
			}

			final String orderLineIdentifier = DataTableUtil.extractStringOrNullForColumnName(row, "OPT." + I_M_Delivery_Planning.COLUMNNAME_C_OrderLine_ID + "." + TABLECOLUMN_IDENTIFIER);
			if (Check.isNotBlank(orderLineIdentifier))
			{
				final I_C_OrderLine orderLine = orderLineTable.get(orderLineIdentifier);
				softly.assertThat(orderLine).isNotNull();
				softly.assertThat(deliveryPlanning.getC_OrderLine_ID()).as(I_M_Delivery_Planning.COLUMNNAME_C_OrderLine_ID).isEqualTo(orderLine.getC_OrderLine_ID());
			}

			final String shipperIdentifier = DataTableUtil.extractStringOrNullForColumnName(row, "OPT." + I_M_Delivery_Planning.COLUMNNAME_M_Shipper_ID + "." + TABLECOLUMN_IDENTIFIER);
			if (Check.isNotBlank(shipperIdentifier))
			{
				final I_M_Shipper shipper = shipperTable.get(shipperIdentifier);
				softly.assertThat(shipper).isNotNull();
				softly.assertThat(deliveryPlanning.getM_Shipper_ID()).as(I_M_Delivery_Planning.COLUMNNAME_M_Shipper_ID).isEqualTo(shipper.getM_Shipper_ID());
			}

			final String bpartnerLocationIdentifier = DataTableUtil.extractStringOrNullForColumnName(row, "OPT." + I_M_Delivery_Planning.COLUMNNAME_C_BPartner_Location_ID + "." + TABLECOLUMN_IDENTIFIER);
			if (Check.isNotBlank(bpartnerLocationIdentifier))
			{
				final I_C_BPartner_Location bPartnerLocation = bPartnerLocationTable.get(bpartnerLocationIdentifier);
				softly.assertThat(bPartnerLocation).isNotNull();
				softly.assertThat(deliveryPlanning.getC_BPartner_Location_ID()).as(I_M_Delivery_Planning.COLUMNNAME_C_BPartner_Location_ID).isEqualTo(bPartnerLocation.getC_BPartner_Location_ID());
			}

			final String warehouseIdentifier = DataTableUtil.extractStringOrNullForColumnName(row, "OPT." + I_M_Delivery_Planning.COLUMNNAME_M_Warehouse_ID + "." + TABLECOLUMN_IDENTIFIER);
			if (Check.isNotBlank(warehouseIdentifier))
			{
				final I_M_Warehouse warehouse = warehouseTable.get(warehouseIdentifier);
				softly.assertThat(warehouse).isNotNull();
				softly.assertThat(deliveryPlanning.getM_Warehouse_ID()).as(I_M_Delivery_Planning.COLUMNNAME_M_Warehouse_ID).isEqualTo(warehouse.getM_Warehouse_ID());
			}

			final Timestamp plannedDeliveryDate = DataTableUtil.extractDateTimestampForColumnNameOrNull(row, "OPT." + I_M_Delivery_Planning.COLUMNNAME_PlannedDeliveryDate);
			if (plannedDeliveryDate != null)
			{
				softly.assertThat(deliveryPlanning.getPlannedDeliveryDate()).as(I_M_Delivery_Planning.COLUMNNAME_PlannedDeliveryDate).isEqualTo(plannedDeliveryDate);
			}

			final Timestamp plannedLoadingDate = DataTableUtil.extractDateTimestampForColumnNameOrNull(row, "OPT." + I_M_Delivery_Planning.COLUMNNAME_PlannedLoadingDate);
			if (plannedLoadingDate != null)
			{
				softly.assertThat(deliveryPlanning.getPlannedLoadingDate()).as(I_M_Delivery_Planning.COLUMNNAME_PlannedLoadingDate).isEqualTo(plannedLoadingDate);
			}

			final BigDecimal plannedLoadedQty = DataTableUtil.extractBigDecimalOrNullForColumnName(row, "OPT." + I_M_Delivery_Planning.COLUMNNAME_PlannedLoadedQuantity);
			if (plannedLoadedQty != null)
			{
				softly.assertThat(deliveryPlanning.getPlannedLoadedQuantity()).as(I_M_Delivery_Planning.COLUMNNAME_PlannedLoadedQuantity).isEqualTo(plannedLoadedQty);
			}

			final Boolean isClosed = DataTableUtil.extractBooleanForColumnNameOrNull(row, "OPT." + I_M_Delivery_Planning.COLUMNNAME_IsClosed);
			if (isClosed != null)
			{
				softly.assertThat(deliveryPlanning.isClosed()).as(I_M_Delivery_Planning.COLUMNNAME_IsClosed).isEqualTo(isClosed);
			}

			final Boolean isProcessed = DataTableUtil.extractBooleanForColumnNameOrNull(row, "OPT." + I_M_Delivery_Planning.COLUMNNAME_Processed);
			if (isProcessed != null)
			{
				softly.assertThat(deliveryPlanning.isProcessed()).as(I_M_Delivery_Planning.COLUMNNAME_Processed).isEqualTo(isProcessed);
			}

			final String orderStatus = DataTableUtil.extractStringOrNullForColumnName(row, "OPT." + I_M_Delivery_Planning.COLUMNNAME_OrderStatus);
			if (Check.isNotBlank(orderStatus))
			{
				softly.assertThat(deliveryPlanning.getOrderStatus()).as(I_M_Delivery_Planning.COLUMNNAME_OrderStatus).isEqualTo(orderStatus);
			}

			final String deliveryInstructionIdentifier = DataTableUtil.extractNullableStringForColumnName(row, "OPT." + I_M_Delivery_Planning.COLUMNNAME_M_ShipperTransportation_ID + "." + TABLECOLUMN_IDENTIFIER);
			if (Check.isNotBlank(deliveryInstructionIdentifier))
			{
				final String deliveryInstructionIdentifierValue = DataTableUtil.nullToken2Null(deliveryInstructionIdentifier);
				if (deliveryInstructionIdentifierValue != null)
				{
					final I_M_ShipperTransportation deliveryInstruction = deliveryInstructionTable.get(deliveryInstructionIdentifier);
					softly.assertThat(deliveryInstruction).isNotNull();
					softly.assertThat(deliveryPlanning.getM_ShipperTransportation_ID()).as(I_M_Delivery_Planning.COLUMNNAME_M_ShipperTransportation_ID).isEqualTo(deliveryInstruction.getM_ShipperTransportation_ID());
				}
				else
				{
					softly.assertThat(deliveryPlanning.getM_ShipperTransportation_ID()).as(I_M_Delivery_Planning.COLUMNNAME_M_ShipperTransportation_ID).isEqualTo(0);
				}
			}

			softly.assertAll();
		}
	}

	@And("^M_Delivery_Planning identified by (.*) is (closed|opened|canceled)$")
	public void delivery_Planning_action(@NonNull final String deliveryPlanningIdentifier, @NonNull final String action)
	{
		final I_M_Delivery_Planning deliveryPlanning = deliveryPlanningTable.get(deliveryPlanningIdentifier);
		assertThat(deliveryPlanning).isNotNull();

		switch (StepDefDocAction.valueOf(action))
		{
			case closed:
				deliveryPlanningService.closeSelectedDeliveryPlannings(getQueryFilterFor(deliveryPlanningIdentifier));
				break;
			case opened:
				deliveryPlanningService.reOpenSelectedDeliveryPlannings(getQueryFilterFor(deliveryPlanningIdentifier));
				break;
			case canceled:
				deliveryPlanningService.cancelDelivery(getQueryFilterFor(deliveryPlanningIdentifier));
				break;
			default:
				throw new AdempiereException("Unsupported action for M_Delivery_Planning!")
						.appendParametersToMessage()
						.setParameter(I_M_Delivery_Planning.COLUMNNAME_M_Delivery_Planning_ID, deliveryPlanning.getM_Delivery_Planning_ID());
		}
	}

	@And("update M_Delivery_Planning:")
	public void update_M_Delivery_Planning(@NonNull final DataTable dataTable)
	{
		for (final Map<String, String> row : dataTable.asMaps())
		{
			final String deliveryPlanningIdentifier = DataTableUtil.extractStringForColumnName(row, I_M_Delivery_Planning.COLUMNNAME_M_Delivery_Planning_ID + "." + TABLECOLUMN_IDENTIFIER);
			final I_M_Delivery_Planning deliveryPlanning = deliveryPlanningTable.get(deliveryPlanningIdentifier);
			assertThat(deliveryPlanning).isNotNull();

			final String shipperIdentifier = DataTableUtil.extractStringOrNullForColumnName(row, "OPT." + I_M_Delivery_Planning.COLUMNNAME_M_Shipper_ID + "." + TABLECOLUMN_IDENTIFIER);
			if (Check.isNotBlank(shipperIdentifier))
			{
				final I_M_Shipper shipper = shipperTable.get(shipperIdentifier);
				assertThat(shipper).isNotNull();

				deliveryPlanning.setM_Shipper_ID(shipper.getM_Shipper_ID());
			}

			saveRecord(deliveryPlanning);
		}
	}

	@And("generate goods issue:")
	public void generate_goods_issue(@NonNull final DataTable dataTable)
	{
		for (final Map<String, String> row : dataTable.asMaps())
		{
			final String deliveryPlanningIdentifier = DataTableUtil.extractStringForColumnName(row, I_M_Delivery_Planning.COLUMNNAME_M_Delivery_Planning_ID + "." + TABLECOLUMN_IDENTIFIER);

			final DeliveryPlanningId deliveryPlanningId = DeliveryPlanningId.ofRepoId(deliveryPlanningTable.get(deliveryPlanningIdentifier).getM_Delivery_Planning_ID());
			deliveryPlanningService.validateDeliveryPlanning(deliveryPlanningId);

			final DeliveryPlanningShipmentInfo shipmentInfo = deliveryPlanningService.getShipmentInfo(deliveryPlanningId);
			if (shipmentInfo.isShipped())
			{
				throw new AdempiereException("Already shipped");
			}

			final String foreignExchangeContractIdentifier = DataTableUtil.extractStringForColumnName(row, I_C_ForeignExchangeContract.COLUMNNAME_C_ForeignExchangeContract_ID + "." + TABLECOLUMN_IDENTIFIER);
			final ForexContractId forexContractId = ForexContractId.ofRepoId(foreignExchangeContractTable.get(foreignExchangeContractIdentifier).getC_ForeignExchangeContract_ID());

			final String orderCurrencyIdentifier = DataTableUtil.extractStringForColumnName(row, I_C_Order.COLUMNNAME_C_Order_ID + "." + I_C_Order.COLUMNNAME_C_Currency_ID + "." + TABLECOLUMN_IDENTIFIER);
			final CurrencyId orderCurrencyId = CurrencyId.ofRepoId(currencyTable.get(orderCurrencyIdentifier).getC_Currency_ID());

			final String fromCurrencyIdentifier = DataTableUtil.extractStringForColumnName(row, I_C_ForeignExchangeContract.COLUMNNAME_C_Currency_ID + "." + TABLECOLUMN_IDENTIFIER);
			final CurrencyId fromCurrencyId = CurrencyId.ofRepoId(currencyTable.get(fromCurrencyIdentifier).getC_Currency_ID());

			final String toCurrencyIdentifier = DataTableUtil.extractStringForColumnName(row, I_C_ForeignExchangeContract.COLUMNNAME_To_Currency_ID + "." + TABLECOLUMN_IDENTIFIER);
			final CurrencyId toCurrencyId = CurrencyId.ofRepoId(currencyTable.get(toCurrencyIdentifier).getC_Currency_ID());

			final BigDecimal currencyRate = DataTableUtil.extractBigDecimalForColumnName(row, I_C_ForeignExchangeContract.COLUMNNAME_FEC_CurrencyRate);

			final ShipmentScheduleId shipmentScheduleId = shipmentInfo.getShipmentScheduleId();
			// final BigDecimal qtyOnHand = getQtyOnHandByShipmentScheduleId(shipmentScheduleId).toBigDecimal();
			//
			// final DeliveryRule deliveryRule = getDeliveryRuleByShipmentScheduleId(shipmentScheduleId);
			// final InOutId b2bReceiptId = request.getB2bReceiptId();
			// if (request.getQtyToShipBD().compareTo(qtyOnHand) > 0 && !deliveryRule.isForce() && b2bReceiptId == null)
			// {
			// 	throw new AdempiereException(MSG_ERROR_GOODS_ISSUE_QUANTITY);
			// }

			final ForexContractRef forexContractRef = ForexContractRef.builder()
					.forexContractId(forexContractId)
					.orderCurrencyId(orderCurrencyId)
					.fromCurrencyId(fromCurrencyId)
					.toCurrencyId(toCurrencyId)
					.currencyRate(currencyRate)
					.build();

			final ShipmentScheduleEnqueuer.Result result = new ShipmentScheduleEnqueuer()
					.setContext(Env.getCtx(), ITrx.TRXNAME_ThreadInherited)
					.createWorkpackages(
							ShipmentScheduleEnqueuer.ShipmentScheduleWorkPackageParameters.builder()
									.adPInstanceId(pinstanceDAO.createSelectionId())
									.queryFilters(Services.get(IQueryBL.class).createCompositeQueryFilter(I_M_ShipmentSchedule.class)
														  .addEqualsFilter(I_M_ShipmentSchedule.COLUMNNAME_M_ShipmentSchedule_ID, shipmentScheduleId))
									.quantityType(M_ShipmentSchedule_QuantityTypeToUse.TYPE_QTY_TO_DELIVER)
									.completeShipments(true)
									// .fixedShipmentDate(deliveryDate)
									// .qtysToDeliverOverride(ImmutableMap.<ShipmentScheduleId, BigDecimal>builder()
									// 							   .put(shipmentScheduleId, request.getQtyToShipBD())
									// 							   .build())
									.forexContractRef(forexContractRef)
									.deliveryPlanningId(deliveryPlanningId)
									// .b2bReceiptId(b2bReceiptId)
									.build());

			Assertions.assertThat(result.getEnqueuedPackagesCount()).isEqualTo(1);
		}
	}

	@And("generate goods receipt:")
	public void generate_goods_receipt(@NonNull final DataTable dataTable)
	{
		for (final Map<String, String> row : dataTable.asMaps())
		{
			final String deliveryPlanningIdentifier = DataTableUtil.extractStringForColumnName(row, I_M_Delivery_Planning.COLUMNNAME_M_Delivery_Planning_ID + "." + TABLECOLUMN_IDENTIFIER);

			final DeliveryPlanningId deliveryPlanningId = DeliveryPlanningId.ofRepoId(deliveryPlanningTable.get(deliveryPlanningIdentifier).getM_Delivery_Planning_ID());
			deliveryPlanningService.validateDeliveryPlanning(deliveryPlanningId);

			final DeliveryPlanningReceiptInfo receiptInfo = getReceiptInfo(deliveryPlanningId);
			if (receiptInfo.isReceived())
			{
				throw new AdempiereException("Already received");
			}

			final I_M_ReceiptSchedule receiptSchedule = huReceiptScheduleBL.getById(receiptInfo.getReceiptScheduleId());
			// final Quantity qtyToReceive = Quantitys.create(request.getQtyToReceiveBD(), UomId.ofRepoId(receiptSchedule.getC_UOM_ID()));

			final String foreignExchangeContractIdentifier = DataTableUtil.extractStringForColumnName(row, I_C_ForeignExchangeContract.COLUMNNAME_C_ForeignExchangeContract_ID + "." + TABLECOLUMN_IDENTIFIER);
			final ForexContractId forexContractId = ForexContractId.ofRepoId(foreignExchangeContractTable.get(foreignExchangeContractIdentifier).getC_ForeignExchangeContract_ID());

			final String orderCurrencyIdentifier = DataTableUtil.extractStringForColumnName(row, I_C_Order.COLUMNNAME_C_Order_ID + "." + I_C_Order.COLUMNNAME_C_Currency_ID + "." + TABLECOLUMN_IDENTIFIER);
			final CurrencyId orderCurrencyId = CurrencyId.ofRepoId(currencyTable.get(orderCurrencyIdentifier).getC_Currency_ID());

			final String fromCurrencyIdentifier = DataTableUtil.extractStringForColumnName(row, I_C_ForeignExchangeContract.COLUMNNAME_C_ForeignExchangeContract_ID + "." + I_C_ForeignExchangeContract.COLUMNNAME_C_Currency_ID + "." + TABLECOLUMN_IDENTIFIER);
			final CurrencyId fromCurrencyId = CurrencyId.ofRepoId(currencyTable.get(fromCurrencyIdentifier).getC_Currency_ID());

			final String toCurrencyIdentifier = DataTableUtil.extractStringForColumnName(row, I_C_ForeignExchangeContract.COLUMNNAME_C_ForeignExchangeContract_ID + "." + I_C_ForeignExchangeContract.COLUMNNAME_To_Currency_ID + "." + TABLECOLUMN_IDENTIFIER);
			final CurrencyId toCurrencyId = CurrencyId.ofRepoId(currencyTable.get(toCurrencyIdentifier).getC_Currency_ID());

			final BigDecimal currencyRate = DataTableUtil.extractBigDecimalForColumnName(row, I_C_ForeignExchangeContract.COLUMNNAME_FEC_CurrencyRate);

			final ForexContractRef forexContractRef = ForexContractRef.builder()
					.forexContractId(forexContractId)
					.orderCurrencyId(orderCurrencyId)
					.fromCurrencyId(fromCurrencyId)
					.toCurrencyId(toCurrencyId)
					.currencyRate(currencyRate)
					.build();

			// final I_M_HU vhu = huReceiptScheduleBL.createPlanningVHU(receiptSchedule, qtyToReceive);
			// if (vhu == null)
			// {
			// 	throw new AdempiereException("Failed receiving"); // shall not happen
			// }
			// final HuId vhuId = HuId.ofRepoId(vhu.getM_HU_ID());

			final InOutGenerateResult result = huReceiptScheduleBL.processReceiptSchedules(
					IHUReceiptScheduleBL.CreateReceiptsParameters.builder()
							.commitEachReceiptIndividually(false)
							.movementDateRule(ReceiptMovementDateRule.CURRENT_DATE)
							.ctx(Env.getCtx())
							.destinationLocatorIdOrNull(null) // use receipt schedules' destination-warehouse settings
							.printReceiptLabels(true)
							.receiptSchedule(receiptSchedule)
							// .selectedHuId(vhuId)
							.forexContractRef(forexContractRef)
							.deliveryPlanningId(deliveryPlanningId)
							.build());

			Assertions.assertThat(result).isNotNull();
			Assertions.assertThat(result.getInOuts()).isNotEmpty();
			Assertions.assertThat(result.getInOuts().size()).isEqualTo(1);
		}
	}

	@NonNull
	private IQueryFilter<I_M_Delivery_Planning> getQueryFilterFor(@NonNull final String deliveryPlanningIdentifier)
	{
		final I_M_Delivery_Planning deliveryPlanning = deliveryPlanningTable.get(deliveryPlanningIdentifier);
		assertThat(deliveryPlanning).isNotNull();

		return queryBL.createCompositeQueryFilter(I_M_Delivery_Planning.class)
				.addEqualsFilter(I_M_Delivery_Planning.COLUMNNAME_M_Delivery_Planning_ID, deliveryPlanning.getM_Delivery_Planning_ID());
	}

	private DeliveryPlanningReceiptInfo getReceiptInfo(@NonNull final DeliveryPlanningId deliveryPlanningId)
	{
		return getReceiptInfoIfIncomingType(deliveryPlanningId)
				.orElseThrow(() -> new AdempiereException("Expected to be an incoming delivery planning"));
	}

	private Optional<DeliveryPlanningReceiptInfo> getReceiptInfoIfIncomingType(@NonNull final DeliveryPlanningId deliveryPlanningId)
	{
		return receiptInfos.computeIfAbsent(deliveryPlanningId, deliveryPlanningService::getReceiptInfoIfIncomingType);
	}
}

/*
 * #%L
 * de.metas.cucumber
 * %%
 * Copyright (C) 2021 metas GmbH
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

package de.metas.cucumber.stepdefs.shipment;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import de.metas.cucumber.stepdefs.C_BPartner_Location_StepDefData;
import de.metas.cucumber.stepdefs.C_BPartner_StepDefData;
import de.metas.cucumber.stepdefs.C_OrderLine_StepDefData;
import de.metas.cucumber.stepdefs.C_Order_StepDefData;
import de.metas.cucumber.stepdefs.DataTableUtil;
import de.metas.cucumber.stepdefs.M_ReceiptSchedule_StepDefData;
import de.metas.cucumber.stepdefs.StepDefConstants;
import de.metas.cucumber.stepdefs.StepDefDocAction;
import de.metas.cucumber.stepdefs.StepDefUtil;
import de.metas.cucumber.stepdefs.sectioncode.M_SectionCode_StepDefData;
import de.metas.cucumber.stepdefs.shipmentschedule.M_ShipmentSchedule_StepDefData;
import de.metas.cucumber.stepdefs.warehouse.M_Warehouse_StepDefData;
import de.metas.document.engine.IDocument;
import de.metas.document.engine.IDocumentBL;
import de.metas.handlingunits.inout.IHUInOutBL;
import de.metas.handlingunits.shipmentschedule.api.M_ShipmentSchedule_QuantityTypeToUse;
import de.metas.handlingunits.shipmentschedule.api.ShipmentScheduleEnqueuer;
import de.metas.impex.api.IInputDataSourceDAO;
import de.metas.impex.model.I_AD_InputDataSource;
import de.metas.inout.IInOutDAO;
import de.metas.inout.InOutId;
import de.metas.inout.InOutLineId;
import de.metas.inout.ShipmentScheduleId;
import de.metas.inout.model.I_M_InOutLine;
import de.metas.inoutcandidate.api.IShipmentScheduleAllocDAO;
import de.metas.inoutcandidate.model.I_M_ReceiptSchedule;
import de.metas.inoutcandidate.model.I_M_ReceiptSchedule_Alloc;
import de.metas.inoutcandidate.model.I_M_ShipmentSchedule;
import de.metas.inoutcandidate.model.I_M_ShipmentSchedule_QtyPicked;
import de.metas.process.IADPInstanceDAO;
import de.metas.util.Check;
import de.metas.util.Services;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import lombok.NonNull;
import org.adempiere.ad.dao.IQueryBL;
import org.adempiere.ad.dao.IQueryBuilder;
import org.adempiere.ad.dao.IQueryFilter;
import org.adempiere.exceptions.AdempiereException;
import org.adempiere.model.InterfaceWrapperHelper;
import org.compiere.model.I_C_BPartner;
import org.compiere.model.I_C_BPartner_Location;
import org.compiere.model.I_C_DocType;
import org.compiere.model.I_C_Order;
import org.compiere.model.I_C_OrderLine;
import org.compiere.model.I_M_InOut;
import org.compiere.model.I_M_SectionCode;
import org.compiere.util.Env;
import org.compiere.util.Trx;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static de.metas.cucumber.stepdefs.StepDefConstants.TABLECOLUMN_IDENTIFIER;
import static org.assertj.core.api.Assertions.*;
import static org.compiere.model.I_C_BPartner_Location.COLUMNNAME_C_BPartner_Location_ID;
import static org.compiere.model.I_C_DocType.COLUMNNAME_DocBaseType;
import static org.compiere.model.I_C_DocType.COLUMNNAME_Name;
import static org.compiere.model.I_M_InOut.COLUMNNAME_C_Order_ID;
import static org.compiere.model.I_M_InOut.COLUMNNAME_DocStatus;
import static org.compiere.model.I_M_InOut.COLUMNNAME_M_InOut_ID;

public class M_InOut_StepDef
{
	private final M_InOut_StepDefData shipmentTable;
	private final M_InOutLine_StepDefData shipmentLineTable;
	private final C_BPartner_StepDefData bpartnerTable;
	private final C_BPartner_Location_StepDefData bpartnerLocationTable;
	private final M_ShipmentSchedule_StepDefData shipmentScheduleTable;
	private final C_Order_StepDefData orderTable;
	private final C_OrderLine_StepDefData orderLineTable;
	private final M_Warehouse_StepDefData warehouseTable;
	private final M_SectionCode_StepDefData sectionCodeTable;
	private final M_ReceiptSchedule_StepDefData receiptScheduleTable;

	private final IInOutDAO inOutDAO = Services.get(IInOutDAO.class);
	private final IShipmentScheduleAllocDAO shipmentScheduleAllocDAO = Services.get(IShipmentScheduleAllocDAO.class);

	private final IQueryBL queryBL = Services.get(IQueryBL.class);
	private final IADPInstanceDAO pinstanceDAO = Services.get(IADPInstanceDAO.class);
	private final IDocumentBL documentBL = Services.get(IDocumentBL.class);
	private final IInputDataSourceDAO inputDataSourceDAO = Services.get(IInputDataSourceDAO.class);
	private final IHUInOutBL huInOutBL = Services.get(IHUInOutBL.class);

	public M_InOut_StepDef(
			@NonNull final M_InOut_StepDefData shipmentTable,
			@NonNull final M_InOutLine_StepDefData shipmentLineTable,
			@NonNull final M_ShipmentSchedule_StepDefData shipmentScheduleTable,
			@NonNull final C_BPartner_StepDefData bpartnerTable,
			@NonNull final C_BPartner_Location_StepDefData bpartnerLocationTable,
			@NonNull final C_Order_StepDefData orderTable,
			@NonNull final C_OrderLine_StepDefData orderLineTable,
			@NonNull final M_Warehouse_StepDefData warehouseTable,
			@NonNull final M_SectionCode_StepDefData sectionCodeTable,
			@NonNull final M_ReceiptSchedule_StepDefData receiptScheduleTable)
	{
		this.shipmentTable = shipmentTable;
		this.shipmentLineTable = shipmentLineTable;
		this.bpartnerTable = bpartnerTable;
		this.bpartnerLocationTable = bpartnerLocationTable;
		this.shipmentScheduleTable = shipmentScheduleTable;
		this.orderTable = orderTable;
		this.orderLineTable = orderLineTable;
		this.warehouseTable = warehouseTable;
		this.sectionCodeTable = sectionCodeTable;
		this.receiptScheduleTable = receiptScheduleTable;
	}

	@And("^validate the created (shipments|material receipt)$")
	public void validate_created_shipments(@NonNull final String inoutType, @NonNull final DataTable table)
	{
		final List<Map<String, String>> dataTable = table.asMaps();
		for (final Map<String, String> row : dataTable)
		{
			final String identifier = DataTableUtil.extractStringForColumnName(row, "M_InOut_ID.Identifier");
			final Timestamp dateOrdered = DataTableUtil.extractDateTimestampForColumnName(row, "dateordered");
			final String poReference = DataTableUtil.extractStringOrNullForColumnName(row, "OPT." + I_M_InOut.COLUMNNAME_POReference);
			final boolean processed = DataTableUtil.extractBooleanForColumnName(row, "processed");
			final String docStatus = DataTableUtil.extractStringForColumnName(row, "docStatus");

			final String bpartnerIdentifier = DataTableUtil.extractStringForColumnName(row, I_C_BPartner.COLUMNNAME_C_BPartner_ID + "." + TABLECOLUMN_IDENTIFIER);
			final Integer expectedBPartnerId = bpartnerTable.getOptional(bpartnerIdentifier)
					.map(I_C_BPartner::getC_BPartner_ID)
					.orElseGet(() -> Integer.parseInt(bpartnerIdentifier));

			final String bpartnerLocationIdentifier = DataTableUtil.extractStringForColumnName(row, COLUMNNAME_C_BPartner_Location_ID + "." + TABLECOLUMN_IDENTIFIER);
			final Integer expectedBPartnerLocationId = bpartnerLocationTable.getOptional(bpartnerLocationIdentifier)
					.map(I_C_BPartner_Location::getC_BPartner_Location_ID)
					.orElseGet(() -> Integer.parseInt(bpartnerLocationIdentifier));

			final I_M_InOut shipment = shipmentTable.get(identifier);

			assertThat(shipment.getC_BPartner_ID()).isEqualTo(expectedBPartnerId);
			assertThat(shipment.getC_BPartner_Location_ID()).isEqualTo(expectedBPartnerLocationId);
			assertThat(shipment.getDateOrdered()).isEqualTo(dateOrdered);

			if (Check.isNotBlank(poReference))
			{
				assertThat(shipment.getPOReference()).isEqualTo(poReference);
			}

			assertThat(shipment.isProcessed()).isEqualTo(processed);
			assertThat(shipment.getDocStatus()).isEqualTo(docStatus);

			final String internalName = DataTableUtil.extractStringOrNullForColumnName(row, "OPT." + I_M_InOut.COLUMNNAME_AD_InputDataSource_ID + "." + I_AD_InputDataSource.COLUMNNAME_InternalName);
			if (Check.isNotBlank(internalName))
			{
				final I_AD_InputDataSource dataSource = inputDataSourceDAO.retrieveInputDataSource(Env.getCtx(), internalName, true, Trx.TRXNAME_None);
				assertThat(shipment.getAD_InputDataSource_ID()).isEqualTo(dataSource.getAD_InputDataSource_ID());
			}

			final String docBaseType = DataTableUtil.extractStringOrNullForColumnName(row, "OPT." + I_C_DocType.Table_Name + "." + COLUMNNAME_DocBaseType);
			if (Check.isNotBlank(docBaseType))
			{
				final String name = DataTableUtil.extractStringForColumnName(row, "OPT." + I_C_DocType.Table_Name + "." + COLUMNNAME_Name);

				final I_C_DocType docType = queryBL.createQueryBuilder(I_C_DocType.class)
						.addEqualsFilter(COLUMNNAME_DocBaseType, docBaseType)
						.addEqualsFilter(COLUMNNAME_Name, name)
						.create()
						.firstOnlyNotNull(I_C_DocType.class);

				assertThat(shipment.getC_DocType_ID()).isEqualTo(docType.getC_DocType_ID());
			}

			final String sectionCodeIdentifier = DataTableUtil.extractStringOrNullForColumnName(row, "OPT." + I_M_InOut.COLUMNNAME_M_SectionCode_ID + "." + TABLECOLUMN_IDENTIFIER);
			if (Check.isNotBlank(sectionCodeIdentifier))
			{
				final I_M_SectionCode sectionCode = sectionCodeTable.get(sectionCodeIdentifier);
				assertThat(shipment.getM_SectionCode_ID()).isEqualTo(sectionCode.getM_SectionCode_ID());
			}
		}
	}

	@And("'generate shipments' process is invoked")
	public void invokeGenerateShipmentsProcess(@NonNull final DataTable table)
	{
		final List<Map<String, String>> dataTable = table.asMaps();
		for (final Map<String, String> tableRow : dataTable)
		{
			final String shipmentScheduleIdentifier = DataTableUtil.extractStringForColumnName(tableRow, I_M_ShipmentSchedule.COLUMNNAME_M_ShipmentSchedule_ID + "." + TABLECOLUMN_IDENTIFIER);

			final String quantityType = DataTableUtil.extractStringForColumnName(tableRow, ShipmentScheduleEnqueuer.ShipmentScheduleWorkPackageParameters.PARAM_QuantityType);
			final boolean isCompleteShipments = DataTableUtil.extractBooleanForColumnName(tableRow, ShipmentScheduleEnqueuer.ShipmentScheduleWorkPackageParameters.PARAM_IsCompleteShipments);
			final boolean isShipToday = DataTableUtil.extractBooleanForColumnName(tableRow, ShipmentScheduleEnqueuer.ShipmentScheduleWorkPackageParameters.PARAM_IsShipmentDateToday);
			final BigDecimal qtyToDeliverOverride = DataTableUtil.extractBigDecimalOrNullForColumnName(tableRow, ShipmentScheduleEnqueuer.ShipmentScheduleWorkPackageParameters.PARAM_PREFIX_QtyToDeliver_Override);

			final I_M_ShipmentSchedule shipmentSchedule = shipmentScheduleTable.get(shipmentScheduleIdentifier);

			final IQueryFilter<de.metas.handlingunits.model.I_M_ShipmentSchedule> queryFilter = queryBL.createCompositeQueryFilter(de.metas.handlingunits.model.I_M_ShipmentSchedule.class)
					.addOnlyActiveRecordsFilter()
					.addEqualsFilter(I_M_ShipmentSchedule.COLUMNNAME_M_ShipmentSchedule_ID, shipmentSchedule.getM_ShipmentSchedule_ID());

			final ShipmentScheduleEnqueuer.ShipmentScheduleWorkPackageParameters.ShipmentScheduleWorkPackageParametersBuilder workPackageParametersBuilder = ShipmentScheduleEnqueuer.ShipmentScheduleWorkPackageParameters.builder()
					.adPInstanceId(pinstanceDAO.createSelectionId())
					.queryFilters(queryFilter)
					.quantityType(M_ShipmentSchedule_QuantityTypeToUse.ofCode(quantityType))
					.completeShipments(isCompleteShipments)
					.isShipmentDateToday(isShipToday);

			if (qtyToDeliverOverride != null)
			{
				final ImmutableMap<ShipmentScheduleId, BigDecimal> qtysToDeliverOverride = ImmutableMap.of(
						ShipmentScheduleId.ofRepoId(shipmentSchedule.getM_ShipmentSchedule_ID()), qtyToDeliverOverride);
				workPackageParametersBuilder.qtysToDeliverOverride(qtysToDeliverOverride);
			}

			final ShipmentScheduleEnqueuer.Result result = new ShipmentScheduleEnqueuer()
					.setContext(Env.getCtx(), Trx.TRXNAME_None)
					.createWorkpackages(workPackageParametersBuilder.build());

			assertThat(result.getEnqueuedPackagesCount()).isEqualTo(1);
		}
	}

	@And("^after not more than (.*)s, M_InOut is found:$")
	public void shipmentIsFound(final int timeoutSec, @NonNull final DataTable dataTable) throws InterruptedException
	{
		final Map<String, String> tableRow = dataTable.asMaps().get(0);

		final String shipmentScheduleIdentifier = DataTableUtil.extractStringForColumnName(tableRow, I_M_ShipmentSchedule.COLUMNNAME_M_ShipmentSchedule_ID + "." + TABLECOLUMN_IDENTIFIER);
		final String shipmentIdentifier = DataTableUtil.extractStringForColumnName(tableRow, I_M_InOut.COLUMNNAME_M_InOut_ID + "." + TABLECOLUMN_IDENTIFIER);
		final Optional<String> docStatus = Optional.ofNullable(DataTableUtil.extractStringOrNullForColumnName(tableRow, "OPT." + I_M_InOut.COLUMNNAME_DocStatus));

		final I_M_ShipmentSchedule shipmentSchedule = shipmentScheduleTable.get(shipmentScheduleIdentifier);

		final Supplier<Boolean> isShipmentCreated = () -> {

			final List<I_M_ShipmentSchedule_QtyPicked> qtyPickedRecords = queryBL
					.createQueryBuilder(I_M_ShipmentSchedule_QtyPicked.class)
					.addOnlyActiveRecordsFilter()
					.addEqualsFilter(I_M_ShipmentSchedule_QtyPicked.COLUMNNAME_M_ShipmentSchedule_ID, shipmentSchedule.getM_ShipmentSchedule_ID())
					.addNotNull(I_M_ShipmentSchedule_QtyPicked.COLUMNNAME_M_InOutLine_ID)
					.create()
					.list(I_M_ShipmentSchedule_QtyPicked.class);

			if (qtyPickedRecords.isEmpty())
			{
				return false;
			}

			final Set<InOutLineId> shipmentLineIds = qtyPickedRecords.stream()
					.map(I_M_ShipmentSchedule_QtyPicked::getM_InOutLine_ID)
					.map(InOutLineId::ofRepoId)
					.collect(ImmutableSet.toImmutableSet());

			final Set<InOutId> inOutIds = queryBL
					.createQueryBuilder(I_M_InOutLine.class)
					.addOnlyActiveRecordsFilter()
					.addInArrayFilter(I_M_InOutLine.COLUMNNAME_M_InOutLine_ID, shipmentLineIds)
					.create()
					.stream()
					.map(I_M_InOutLine::getM_InOut_ID)
					.map(InOutId::ofRepoId)
					.collect(Collectors.toSet());

			if (inOutIds.size() > 1)
			{
				throw new AdempiereException("More than one M_InOut found for shipmentSchedule=" + shipmentSchedule.getM_ShipmentSchedule_ID());
			}

			final IQueryBuilder<I_M_InOut> shipmentQueryBuilder = queryBL
					.createQueryBuilder(I_M_InOut.class)
					.addOnlyActiveRecordsFilter();

			docStatus.map(status -> shipmentQueryBuilder.addEqualsFilter(I_M_InOut.COLUMNNAME_DocStatus, status));

			final I_M_InOut shipment = shipmentQueryBuilder
					.addEqualsFilter(I_M_InOut.COLUMNNAME_M_InOut_ID, inOutIds.iterator().next().getRepoId())
					.create()
					.firstOnly(I_M_InOut.class);

			if (shipment != null)
			{
				shipmentTable.put(shipmentIdentifier, shipment);
				return true;
			}

			return false;
		};

		StepDefUtil.tryAndWait(timeoutSec, 500, isShipmentCreated);
	}

	@And("^after not more than (.*)s, M_InOut is found for material receipt:$")
	public void receiptIsFound(final int timeoutSec, @NonNull final DataTable dataTable) throws InterruptedException
	{
		final Map<String, String> tableRow = dataTable.asMaps().get(0);

		final String receiptScheduleIdentifier = DataTableUtil.extractStringForColumnName(tableRow, I_M_ReceiptSchedule.COLUMNNAME_M_ReceiptSchedule_ID + "." + TABLECOLUMN_IDENTIFIER);
		final String receiptIdentifier = DataTableUtil.extractStringForColumnName(tableRow, COLUMNNAME_M_InOut_ID + "." + TABLECOLUMN_IDENTIFIER);
		final I_M_ReceiptSchedule receiptSchedule = receiptScheduleTable.get(receiptScheduleIdentifier);

		final Supplier<Boolean> isReceiptCreated = () -> {

			final List<I_M_ReceiptSchedule_Alloc> receiptAllocRecords = queryBL
					.createQueryBuilder(I_M_ReceiptSchedule_Alloc.class)
					.addOnlyActiveRecordsFilter()
					.addEqualsFilter(I_M_ReceiptSchedule_Alloc.COLUMNNAME_M_ReceiptSchedule_ID, receiptSchedule.getM_ReceiptSchedule_ID())
					.addNotNull(I_M_ReceiptSchedule_Alloc.COLUMNNAME_M_InOutLine_ID)
					.create()
					.list(I_M_ReceiptSchedule_Alloc.class);

			if (receiptAllocRecords.isEmpty())
			{
				return false;
			}

			final Set<InOutLineId> receiptLineIds = receiptAllocRecords.stream()
					.map(I_M_ReceiptSchedule_Alloc::getM_InOutLine_ID)
					.map(InOutLineId::ofRepoId)
					.collect(ImmutableSet.toImmutableSet());

			final Set<InOutId> inOutIds = queryBL
					.createQueryBuilder(I_M_InOutLine.class)
					.addOnlyActiveRecordsFilter()
					.addInArrayFilter(I_M_InOutLine.COLUMNNAME_M_InOutLine_ID, receiptLineIds)
					.create()
					.stream()
					.map(I_M_InOutLine::getM_InOut_ID)
					.map(InOutId::ofRepoId)
					.collect(Collectors.toSet());

			if (inOutIds.size() > 1)
			{
				throw new AdempiereException("More than one M_InOut found for receiptSchedule=" + receiptSchedule.getM_ReceiptSchedule_ID());
			}

			final IQueryBuilder<I_M_InOut> shipmentQueryBuilder = queryBL
					.createQueryBuilder(I_M_InOut.class)
					.addOnlyActiveRecordsFilter();

			final I_M_InOut shipment = shipmentQueryBuilder
					.addEqualsFilter(I_M_InOut.COLUMNNAME_M_InOut_ID, inOutIds.iterator().next().getRepoId())
					.create()
					.firstOnly(I_M_InOut.class);

			if (shipment != null)
			{
				shipmentTable.put(receiptIdentifier, shipment);
				return true;
			}

			return false;
		};

		StepDefUtil.tryAndWait(timeoutSec, 500, isReceiptCreated);


	}

		@And("^the (shipment|material receipt) identified by (.*) is (completed) and the following exception is thrown: (.*)")
	public void complete_inOut_expect_exception(
			@NonNull final String model_UNUSED,
			@NonNull final String shipmentIdentifier,
			@NonNull final String action,
			@NonNull final String exceptionMessage)
	{
		try
		{
			shipment_action(model_UNUSED, shipmentIdentifier, action);
			assertThat(1).as("An Exception should have been thrown !").isEqualTo(2);
		}
		catch (final AdempiereException exception)
		{
			assertThat(exception.getMessage()).contains(exceptionMessage);
		}
	}
	
	@And("^the (shipment|material receipt|return inOut) identified by (.*) is (completed|reactivated|reversed|voided|closed)$")
	public void shipment_action(@NonNull final String model_UNUSED, @NonNull final String shipmentIdentifier, @NonNull final String action)
	{
		final I_M_InOut shipment = shipmentTable.get(shipmentIdentifier);
		InterfaceWrapperHelper.refresh(shipment);

		switch (StepDefDocAction.valueOf(action))
		{
			case completed:
				shipment.setDocAction(IDocument.ACTION_Complete); // we need this because otherwise MInOut.completeIt() won't complete it
				documentBL.processEx(shipment, IDocument.ACTION_Complete, IDocument.STATUS_Completed);
				break;
			case reactivated:
				shipment.setDocAction(IDocument.ACTION_Complete); // we need this because otherwise MInOut.completeIt() won't complete it
				documentBL.processEx(shipment, IDocument.ACTION_ReActivate, IDocument.STATUS_InProgress);
				break;
			case reversed:
				shipment.setDocAction(IDocument.ACTION_Complete); // we need this because otherwise MInOut.completeIt() won't complete it
				documentBL.processEx(shipment, IDocument.ACTION_Reverse_Correct, IDocument.STATUS_Reversed);
				break;
			case voided:
				shipment.setDocAction(IDocument.ACTION_Complete); // we need this because otherwise MInOut.completeIt() won't complete it
				documentBL.processEx(shipment, IDocument.ACTION_Void, IDocument.STATUS_Voided);
				break;
			case closed:
				shipment.setDocAction(IDocument.ACTION_Complete); // we need this because otherwise MInOut.completeIt() won't complete it
				documentBL.processEx(shipment, IDocument.ACTION_Close, IDocument.STATUS_Closed);
				break;
			default:
				throw new AdempiereException("Unhandled M_InOut action")
						.appendParametersToMessage()
						.setParameter("action:", action);
		}
	}

	@And("load M_InOut:")
	public void loadM_InOut(@NonNull final DataTable dataTable)
	{
		for (final Map<String, String> row : dataTable.asMaps())
		{
			final BigDecimal qtyEntered = DataTableUtil.extractBigDecimalForColumnName(row, I_M_InOutLine.COLUMNNAME_QtyEntered);

			final IQueryBuilder<I_M_InOutLine> shipmentLineBuilder = queryBL.createQueryBuilder(I_M_InOutLine.class)
					.addEqualsFilter(I_M_InOutLine.COLUMNNAME_QtyEntered, qtyEntered);

			final String orderLineIdentifier = DataTableUtil.extractStringOrNullForColumnName(row, "OPT." + I_M_InOutLine.COLUMNNAME_C_OrderLine_ID + "." + TABLECOLUMN_IDENTIFIER);
			if (Check.isNotBlank(orderLineIdentifier))
			{
				final I_C_OrderLine orderLine = orderLineTable.get(orderLineIdentifier);
				shipmentLineBuilder.addEqualsFilter(I_M_InOutLine.COLUMNNAME_C_OrderLine_ID, orderLine.getC_OrderLine_ID());
			}

			final I_M_InOutLine shipmentLine = shipmentLineBuilder.create()
					.firstOnlyNotNull(I_M_InOutLine.class);

			final String shipmentLineIdentifier = DataTableUtil.extractStringForColumnName(row, I_M_InOutLine.COLUMNNAME_M_InOutLine_ID + "." + TABLECOLUMN_IDENTIFIER);
			shipmentLineTable.putOrReplace(shipmentLineIdentifier, shipmentLine);

			final I_M_InOut shipment = InterfaceWrapperHelper.load(shipmentLine.getM_InOut_ID(), I_M_InOut.class);
			assertThat(shipment).isNotNull();

			final String docStatus = DataTableUtil.extractStringForColumnName(row, I_M_InOut.COLUMNNAME_DocStatus);
			assertThat(shipment.getDocStatus()).isEqualTo(docStatus);

			final String orderIdentifier = DataTableUtil.extractStringOrNullForColumnName(row, "OPT." + COLUMNNAME_C_Order_ID + "." + TABLECOLUMN_IDENTIFIER);
			if (Check.isNotBlank(orderIdentifier))
			{
				final I_C_Order order = orderTable.get(orderIdentifier);
				assertThat(shipment.getC_Order_ID()).isEqualTo(order.getC_Order_ID());
			}

			final String shipmentIdentifier = DataTableUtil.extractStringForColumnName(row, I_M_InOut.COLUMNNAME_M_InOut_ID + "." + TABLECOLUMN_IDENTIFIER);
			shipmentTable.putOrReplace(shipmentIdentifier, shipment);
		}
	}

	@And("perform shipment document action")
	public void reverseShipment(@NonNull final DataTable table)
	{
		final List<Map<String, String>> dataTable = table.asMaps();
		for (final Map<String, String> row : dataTable)
		{
			final String shipmentIdentifier = DataTableUtil.extractStringForColumnName(row, I_M_InOut.COLUMNNAME_M_InOut_ID + "." + StepDefConstants.TABLECOLUMN_IDENTIFIER);

			final String docAction = DataTableUtil.extractStringForColumnName(row, I_M_InOut.COLUMNNAME_DocAction);

			final I_M_InOut shipment = shipmentTable.get(shipmentIdentifier);

			documentBL.processEx(shipment, docAction);
		}
	}

	@Then("locate M_InOut by shipment schedule Id")
	public void locate_shipment_by_scheduleId(@NonNull final DataTable table)
	{
		final List<Map<String, String>> dataTable = table.asMaps();
		for (final Map<String, String> row : dataTable)
		{
			locateShipmentByScheduleId(row);
		}
	}

	@And("validate M_In_Out status")
	public void validate_M_In_Out_status(@NonNull final DataTable table)
	{
		final List<Map<String, String>> dataTable = table.asMaps();
		for (final Map<String, String> row : dataTable)
		{
			final String shipmentIdentifier = DataTableUtil.extractStringForColumnName(row, COLUMNNAME_M_InOut_ID + "." + TABLECOLUMN_IDENTIFIER);
			final I_M_InOut shipment = shipmentTable.get(shipmentIdentifier);
			InterfaceWrapperHelper.refresh(shipment);

			final String docStatus = DataTableUtil.extractStringForColumnName(row, COLUMNNAME_DocStatus);
			assertThat(shipment.getDocStatus()).isEqualTo(docStatus);
		}
	}

	@And("^reset M_InOut packing lines for shipment (.*)$")
	public void reset_M_InOut_PackingLines(@NonNull final String shipmentIdentifier)
	{
		final I_M_InOut shipment = shipmentTable.get(shipmentIdentifier);
		assertThat(shipment).isNotNull();

		huInOutBL.recreatePackingMaterialLines(shipment);
	}

	@And("^validate no M_InOut found for C_Order identified by (.*)$")
	public void no_M_InOut_found(@NonNull final String orderIdentifier)
	{
		final I_C_Order order = orderTable.get(orderIdentifier);
		assertThat(order).isNotNull();

		final I_M_InOut inOut = queryBL.createQueryBuilder(I_M_InOut.class)
				.addOnlyActiveRecordsFilter()
				.addEqualsFilter(COLUMNNAME_C_Order_ID, order.getC_Order_ID())
				.create()
				.firstOnly(I_M_InOut.class);

		assertThat(inOut).isNull();
	}

	private void locateShipmentByScheduleId(@NonNull final Map<String, String> row)
	{
		final String shipmentScheduleIdentifier = DataTableUtil.extractStringForColumnName(row, I_M_ShipmentSchedule.COLUMNNAME_M_ShipmentSchedule_ID + ".Identifier");
		final I_M_ShipmentSchedule shipmentSchedule = shipmentScheduleTable.get(shipmentScheduleIdentifier);

		final List<I_M_ShipmentSchedule_QtyPicked> shipmentScheduleQtyPickedRecords = shipmentScheduleAllocDAO.retrieveAllQtyPickedRecords(shipmentSchedule, I_M_ShipmentSchedule_QtyPicked.class);
		final InOutLineId lineId = InOutLineId.ofRepoId(shipmentScheduleQtyPickedRecords.get(0).getM_InOutLine_ID());

		final I_M_InOut shipmentRecord = inOutDAO.retrieveInOutByLineIds(ImmutableSet.of(lineId)).get(lineId);

		final String shipmentIdentifier = DataTableUtil.extractStringForColumnName(row, I_M_InOut.COLUMNNAME_M_InOut_ID + ".Identifier");
		shipmentTable.put(shipmentIdentifier, shipmentRecord);
	}
}

package de.metas.handlingunits.picking.job.model;

import com.google.common.collect.ImmutableSet;
import de.metas.bpartner.BPartnerId;
import de.metas.inout.ShipmentScheduleId;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.time.ZonedDateTime;

@Value
@Builder
public class PickingJobReference
{
	@NonNull PickingJobId pickingJobId;
	@NonNull String salesOrderDocumentNo;
	@NonNull BPartnerId customerId;
	@NonNull String customerName;
	@NonNull ZonedDateTime deliveryDate;
	@NonNull ImmutableSet<ShipmentScheduleId> shipmentScheduleIds;
}

/*
 * #%L
 * ext-metasfresh
 * %%
 * Copyright (C) 2022 Adekia
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

package com.adekia.exchange.metasfresh.util;

import de.metas.common.bpartner.v2.request.JsonRequestLocation;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_23.AddressType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_23.LocationType;
import oasis.names.specification.ubl.schema.xsd.order_23.OrderType;
import org.springframework.util.CollectionUtils;

public class MetasfreshOrderBPartnerLocation {
    public JsonRequestLocation from(OrderType order) {
        JsonRequestLocation buyerLocation = new JsonRequestLocation();
        buyerLocation.setActive(true);
        buyerLocation.setBillTo(true);
        buyerLocation.setBillToDefault(true);
        buyerLocation.setShipTo(true);
        buyerLocation.setShipToDefault(true);

        if (!CollectionUtils.isEmpty(order.getDelivery())) {
            LocationType location = order.getDeliveryAtIndex(0).getDeliveryLocation();
            buyerLocation.setName(location.getNameValue());
            AddressType adr = location.getAddress();
            if (adr != null) {
                if (adr.getAddressLineCount() > 0)
                    buyerLocation.setAddress1(adr.getAddressLineAtIndex(0).getLineValue());
                if (adr.getAddressLineCount() > 1)
                    buyerLocation.setAddress2(adr.getAddressLineAtIndex(1).getLineValue());
                if (adr.getAddressLineCount() > 2)
                    buyerLocation.setAddress3(adr.getAddressLineAtIndex(2).getLineValue());
                buyerLocation.setPostal(adr.getPostalZoneValue());
                buyerLocation.setCity(adr.getCityNameValue());
                if (adr.getCountry() != null)
                    buyerLocation.setCountryCode(adr.getCountry().getIdentificationCodeValue());
            }
        }

        if (buyerLocation.getCountryCode() == null || buyerLocation.getCountryCode().length()==0)
            buyerLocation.setCountryCode("FR"); // todo

        return buyerLocation;
    }

}

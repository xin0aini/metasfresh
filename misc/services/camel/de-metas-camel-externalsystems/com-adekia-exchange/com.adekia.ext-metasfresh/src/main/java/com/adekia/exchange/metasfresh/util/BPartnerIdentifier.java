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

import com.adekia.exchange.metasfresh.constant.MetasfreshExternalConstant;
import com.helger.commons.string.StringHelper;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_23.PartyType;

public class BPartnerIdentifier {

    private PartyType party;

    public  BPartnerIdentifier(PartyType party)
    {
            this.party = party;
    }

    public String getPartnerIdentifier()
    {
        if (party == null || party.getContact() == null)
            raiseRequiredException("ElectronicMail");
        return ExternalIdentifierFormat.formatExternalId(party.getContact().getElectronicMailValue());

    }

    public String getPartnerContactIdentifier()
    {
        if (party == null || party.getContact() == null)
            raiseRequiredException("ElectronicMail");
        return ExternalIdentifierFormat.formatExternalId(party.getContact().getElectronicMailValue());

    }

    public String getPartnerLocationIdentifier()
    {
        if (party == null || party.getPostalAddress() == null || ! StringHelper.hasText(party.getPostalAddress().getIDValue()))
           raiseRequiredException("Postal Address");
        return ExternalIdentifierFormat.formatExternalId(party.getPostalAddress().getIDValue());
    }

    private void raiseRequiredException(String field) {
        throw new IllegalStateException("Partner " + field + " required");
    }
}

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

// Generated Model - DO NOT CHANGE
package de.metas.externalsystem.model;

import java.sql.ResultSet;
import java.util.Properties;
import javax.annotation.Nullable;

/** Generated Model for ExternalSystem_Config_Amazon
 *  @author metasfresh (generated) 
 */
@SuppressWarnings("unused")
public class X_ExternalSystem_Config_Amazon extends org.compiere.model.PO implements I_ExternalSystem_Config_Amazon, org.compiere.model.I_Persistent 
{

	private static final long serialVersionUID = -1618278417L;

    /** Standard Constructor */
    public X_ExternalSystem_Config_Amazon (final Properties ctx, final int ExternalSystem_Config_Amazon_ID, @Nullable final String trxName)
    {
      super (ctx, ExternalSystem_Config_Amazon_ID, trxName);
    }

    /** Load Constructor */
    public X_ExternalSystem_Config_Amazon (final Properties ctx, final ResultSet rs, @Nullable final String trxName)
    {
      super (ctx, rs, trxName);
    }


	/** Load Meta Data */
	@Override
	protected org.compiere.model.POInfo initPO(final Properties ctx)
	{
		return org.compiere.model.POInfo.getPOInfo(Table_Name);
	}

	@Override
	public void setAccessKeyId (final String AccessKeyId)
	{
		set_Value (COLUMNNAME_AccessKeyId, AccessKeyId);
	}

	@Override
	public String getAccessKeyId()
	{
		return get_ValueAsString(COLUMNNAME_AccessKeyId);
	}

	@Override
	public void setBasePath (final String BasePath)
	{
		set_Value (COLUMNNAME_BasePath, BasePath);
	}

	@Override
	public String getBasePath()
	{
		return get_ValueAsString(COLUMNNAME_BasePath);
	}

	@Override
	public void setClientID (final String ClientID)
	{
		set_Value (COLUMNNAME_ClientID, ClientID);
	}

	@Override
	public String getClientID()
	{
		return get_ValueAsString(COLUMNNAME_ClientID);
	}

	@Override
	public void setClientSecret (final String ClientSecret)
	{
		set_Value (COLUMNNAME_ClientSecret, ClientSecret);
	}

	@Override
	public String getClientSecret()
	{
		return get_ValueAsString(COLUMNNAME_ClientSecret);
	}

	@Override
	public void setExternalSystem_Config_Amazon_ID (final int ExternalSystem_Config_Amazon_ID)
	{
		if (ExternalSystem_Config_Amazon_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_ExternalSystem_Config_Amazon_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_ExternalSystem_Config_Amazon_ID, ExternalSystem_Config_Amazon_ID);
	}

	@Override
	public int getExternalSystem_Config_Amazon_ID() 
	{
		return get_ValueAsInt(COLUMNNAME_ExternalSystem_Config_Amazon_ID);
	}

	@Override
	public void setExternalSystem_Config_ID (final int ExternalSystem_Config_ID)
	{
		if (ExternalSystem_Config_ID < 1) 
			set_Value (COLUMNNAME_ExternalSystem_Config_ID, null);
		else 
			set_Value (COLUMNNAME_ExternalSystem_Config_ID, ExternalSystem_Config_ID);
	}

	@Override
	public int getExternalSystem_Config_ID() 
	{
		return get_ValueAsInt(COLUMNNAME_ExternalSystem_Config_ID);
	}

	@Override
	public void setExternalSystemValue (final String ExternalSystemValue)
	{
		set_Value (COLUMNNAME_ExternalSystemValue, ExternalSystemValue);
	}

	@Override
	public String getExternalSystemValue()
	{
		return get_ValueAsString(COLUMNNAME_ExternalSystemValue);
	}

	@Override
	public void setIsDebugProtocol (final boolean IsDebugProtocol)
	{
		set_Value (COLUMNNAME_IsDebugProtocol, IsDebugProtocol);
	}

	@Override
	public boolean isDebugProtocol() 
	{
		return get_ValueAsBoolean(COLUMNNAME_IsDebugProtocol);
	}

	@Override
	public void setLWAEndpoint (final String LWAEndpoint)
	{
		set_Value (COLUMNNAME_LWAEndpoint, LWAEndpoint);
	}

	@Override
	public String getLWAEndpoint()
	{
		return get_ValueAsString(COLUMNNAME_LWAEndpoint);
	}

	@Override
	public void setName (final String Name)
	{
		set_Value (COLUMNNAME_Name, Name);
	}

	@Override
	public String getName()
	{
		return get_ValueAsString(COLUMNNAME_Name);
	}

	@Override
	public void setRefreshToken (final @Nullable String RefreshToken)
	{
		set_Value (COLUMNNAME_RefreshToken, RefreshToken);
	}

	@Override
	public String getRefreshToken()
	{
		return get_ValueAsString(COLUMNNAME_RefreshToken);
	}

	@Override
	public void setRegionName (final @Nullable String RegionName)
	{
		set_Value (COLUMNNAME_RegionName, RegionName);
	}

	@Override
	public String getRegionName()
	{
		return get_ValueAsString(COLUMNNAME_RegionName);
	}

	@Override
	public void setRoleArn (final String RoleArn)
	{
		set_Value (COLUMNNAME_RoleArn, RoleArn);
	}

	@Override
	public String getRoleArn()
	{
		return get_ValueAsString(COLUMNNAME_RoleArn);
	}

	@Override
	public void setSecretKey (final @Nullable String SecretKey)
	{
		set_Value (COLUMNNAME_SecretKey, SecretKey);
	}

	@Override
	public String getSecretKey()
	{
		return get_ValueAsString(COLUMNNAME_SecretKey);
	}
}
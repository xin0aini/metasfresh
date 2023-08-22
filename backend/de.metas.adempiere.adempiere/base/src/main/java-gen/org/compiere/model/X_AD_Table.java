/** Generated Model - DO NOT CHANGE */
package org.compiere.model;

import java.sql.ResultSet;
import java.util.Properties;

/** Generated Model for AD_Table
 *  @author Adempiere (generated)
 */
@SuppressWarnings("javadoc")
public class X_AD_Table extends org.compiere.model.PO implements I_AD_Table, org.compiere.model.I_Persistent 
{

	private static final long serialVersionUID = -88752471L;

    /** Standard Constructor */
    public X_AD_Table (Properties ctx, int AD_Table_ID, String trxName)
    {
      super (ctx, AD_Table_ID, trxName);
      /** if (AD_Table_ID == 0)
        {
			setAccessLevel (null); // 4
			setAD_Table_ID (0);
			setEntityType (null); // U
			setIsAutocomplete (false); // N
			setIsChangeLog (false);
			setIsDeleteable (true); // Y
			setIsEnableRemoteCacheInvalidation (false); // N
			setIsHighVolume (false);
			setIsSecurityEnabled (false);
			setIsView (false); // N
			setName (null);
			setPersonalDataCategory (null); // NP
			setReplicationType (null); // L
			setTableName (null);
			setTooltipType (null); // DTI
        } */
    }

    /** Load Constructor */
    public X_AD_Table (Properties ctx, ResultSet rs, String trxName)
    {
      super (ctx, rs, trxName);
    }


    /** Load Meta Data */
    @Override
    protected org.compiere.model.POInfo initPO (Properties ctx)
    {
      org.compiere.model.POInfo poi = org.compiere.model.POInfo.getPOInfo (ctx, Table_Name, get_TrxName());
      return poi;
    }

	/** 
	 * AccessLevel AD_Reference_ID=5
	 * Reference name: AD_Table Access Levels
	 */
	public static final int ACCESSLEVEL_AD_Reference_ID=5;
	/** Organization = 1 */
	public static final String ACCESSLEVEL_Organization = "1";
	/** ClientPlusOrganization = 3 */
	public static final String ACCESSLEVEL_ClientPlusOrganization = "3";
	/** SystemOnly = 4 */
	public static final String ACCESSLEVEL_SystemOnly = "4";
	/** All = 7 */
	public static final String ACCESSLEVEL_All = "7";
	/** SystemPlusClient = 6 */
	public static final String ACCESSLEVEL_SystemPlusClient = "6";
	/** ClientOnly = 2 */
	public static final String ACCESSLEVEL_ClientOnly = "2";
	/** Set Berechtigungsstufe.
		@param AccessLevel
		Access Level required
	  */
	@Override
	public void setAccessLevel (java.lang.String AccessLevel)
	{

		set_Value (COLUMNNAME_AccessLevel, AccessLevel);
	}

	/** Get Berechtigungsstufe.
		@return Access Level required
	  */
	@Override
	public java.lang.String getAccessLevel ()
	{
		return (java.lang.String)get_Value(COLUMNNAME_AccessLevel);
	}

	/** Set Auto Complete Min Length.
		@param ACTriggerLength
		Identifier autocomplete trigger length
	  */
	@Override
	public void setACTriggerLength (int ACTriggerLength)
	{
		set_Value (COLUMNNAME_ACTriggerLength, Integer.valueOf(ACTriggerLength));
	}

	/** Get Auto Complete Min Length.
		@return Identifier autocomplete trigger length
	  */
	@Override
	public int getACTriggerLength ()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_ACTriggerLength);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set DB-Tabelle.
		@param AD_Table_ID
		Database Table information
	  */
	@Override
	public void setAD_Table_ID (int AD_Table_ID)
	{
		if (AD_Table_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_AD_Table_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_AD_Table_ID, Integer.valueOf(AD_Table_ID));
	}

	/** Get DB-Tabelle.
		@return Database Table information
	  */
	@Override
	public int getAD_Table_ID ()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_AD_Table_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	@Override
	public org.compiere.model.I_AD_Val_Rule getAD_Val_Rule()
	{
		return get_ValueAsPO(COLUMNNAME_AD_Val_Rule_ID, org.compiere.model.I_AD_Val_Rule.class);
	}

	@Override
	public void setAD_Val_Rule(org.compiere.model.I_AD_Val_Rule AD_Val_Rule)
	{
		set_ValueFromPO(COLUMNNAME_AD_Val_Rule_ID, org.compiere.model.I_AD_Val_Rule.class, AD_Val_Rule);
	}

	/** Set Dynamische Validierung.
		@param AD_Val_Rule_ID
		Dynamic Validation Rule
	  */
	@Override
	public void setAD_Val_Rule_ID (int AD_Val_Rule_ID)
	{
		if (AD_Val_Rule_ID < 1) 
			set_Value (COLUMNNAME_AD_Val_Rule_ID, null);
		else 
			set_Value (COLUMNNAME_AD_Val_Rule_ID, Integer.valueOf(AD_Val_Rule_ID));
	}

	/** Get Dynamische Validierung.
		@return Dynamic Validation Rule
	  */
	@Override
	public int getAD_Val_Rule_ID ()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_AD_Val_Rule_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	@Override
	public org.compiere.model.I_AD_Window getAD_Window()
	{
		return get_ValueAsPO(COLUMNNAME_AD_Window_ID, org.compiere.model.I_AD_Window.class);
	}

	@Override
	public void setAD_Window(org.compiere.model.I_AD_Window AD_Window)
	{
		set_ValueFromPO(COLUMNNAME_AD_Window_ID, org.compiere.model.I_AD_Window.class, AD_Window);
	}

	/** Set Fenster.
		@param AD_Window_ID
		Data entry or display window
	  */
	@Override
	public void setAD_Window_ID (int AD_Window_ID)
	{
		if (AD_Window_ID < 1) 
			set_Value (COLUMNNAME_AD_Window_ID, null);
		else 
			set_Value (COLUMNNAME_AD_Window_ID, Integer.valueOf(AD_Window_ID));
	}

	/** Get Fenster.
		@return Data entry or display window
	  */
	@Override
	public int getAD_Window_ID ()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_AD_Window_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/**
	 * CloningEnabled AD_Reference_ID=541757
	 * Reference name: AD_Table_CloningEnabled
	 */
	public static final int CLONINGENABLED_AD_Reference_ID=541757;
	/** Enabled = E */
	public static final String CLONINGENABLED_Enabled = "E";
	/** Disabled = D */
	public static final String CLONINGENABLED_Disabled = "D";
	/** Auto = A */
	public static final String CLONINGENABLED_Auto = "A";
	@Override
	public void setCloningEnabled (final java.lang.String CloningEnabled)
	{
		set_Value (COLUMNNAME_CloningEnabled, CloningEnabled);
	}

	@Override
	public java.lang.String getCloningEnabled()
	{
		return get_ValueAsString(COLUMNNAME_CloningEnabled);
	}

	@Override
	public void setCopyColumnsFromTable (java.lang.String CopyColumnsFromTable)
	{
		set_Value (COLUMNNAME_CopyColumnsFromTable, CopyColumnsFromTable);
	}

	/** Get Copy Columns From Table.
		@return Copy Columns From Table	  */
	@Override
	public java.lang.String getCopyColumnsFromTable ()
	{
		return (java.lang.String)get_Value(COLUMNNAME_CopyColumnsFromTable);
	}

	/** Set Beschreibung.
		@param Description Beschreibung	  */
	@Override
	public void setDescription (java.lang.String Description)
	{
		set_Value (COLUMNNAME_Description, Description);
	}

	/** Get Beschreibung.
		@return Beschreibung	  */
	@Override
	public java.lang.String getDescription ()
	{
		return (java.lang.String)get_Value(COLUMNNAME_Description);
	}

	/**
	 * DownlineCloningStrategy AD_Reference_ID=541755
	 * Reference name: AD_Table_DownlineCloningStrategy
	 */
	public static final int DOWNLINECLONINGSTRATEGY_AD_Reference_ID=541755;
	/** Skip = S */
	public static final String DOWNLINECLONINGSTRATEGY_Skip = "S";
	/** Auto = A */
	public static final String DOWNLINECLONINGSTRATEGY_Auto = "A";
	/** OnlyIncluded = I */
	public static final String DOWNLINECLONINGSTRATEGY_OnlyIncluded = "I";
	@Override
	public void setDownlineCloningStrategy (final java.lang.String DownlineCloningStrategy)
	{
		set_Value (COLUMNNAME_DownlineCloningStrategy, DownlineCloningStrategy);
	}

	@Override
	public java.lang.String getDownlineCloningStrategy()
	{
		return get_ValueAsString(COLUMNNAME_DownlineCloningStrategy);
	}

	/**
	 * EntityType AD_Reference_ID=389
	 * Reference name: _EntityTypeNew
	 */
	public static final int ENTITYTYPE_AD_Reference_ID=389;
	/** Set Entitäts-Art.
		@param EntityType
		Dictionary Entity Type; Determines ownership and synchronization
	  */
	@Override
	public void setEntityType (java.lang.String EntityType)
	{

		set_Value (COLUMNNAME_EntityType, EntityType);
	}

	/** Get Entitäts-Art.
		@return Dictionary Entity Type; Determines ownership and synchronization
	  */
	@Override
	public java.lang.String getEntityType ()
	{
		return (java.lang.String)get_Value(COLUMNNAME_EntityType);
	}

	/** Set Kommentar/Hilfe.
		@param Help
		Comment or Hint
	  */
	@Override
	public void setHelp (java.lang.String Help)
	{
		set_Value (COLUMNNAME_Help, Help);
	}

	/** Get Kommentar/Hilfe.
		@return Comment or Hint
	  */
	@Override
	public java.lang.String getHelp ()
	{
		return (java.lang.String)get_Value(COLUMNNAME_Help);
	}

	/** Set Import Table.
		@param ImportTable
		Import Table Columns from Database
	  */
	@Override
	public void setImportTable (java.lang.String ImportTable)
	{
		set_Value (COLUMNNAME_ImportTable, ImportTable);
	}

	/** Get Import Table.
		@return Import Table Columns from Database
	  */
	@Override
	public java.lang.String getImportTable ()
	{
		return (java.lang.String)get_Value(COLUMNNAME_ImportTable);
	}

	/** Set Autocomplete.
		@param IsAutocomplete
		Automatic completion for textfields
	  */
	@Override
	public void setIsAutocomplete (boolean IsAutocomplete)
	{
		set_Value (COLUMNNAME_IsAutocomplete, Boolean.valueOf(IsAutocomplete));
	}

	/** Get Autocomplete.
		@return Automatic completion for textfields
	  */
	@Override
	public boolean isAutocomplete ()
	{
		Object oo = get_Value(COLUMNNAME_IsAutocomplete);
		if (oo != null)
		{
			 if (oo instanceof Boolean)
				 return ((Boolean)oo).booleanValue();
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Änderungen protokollieren.
		@param IsChangeLog
		Maintain a log of changes
	  */
	@Override
	public void setIsChangeLog (boolean IsChangeLog)
	{
		set_Value (COLUMNNAME_IsChangeLog, Boolean.valueOf(IsChangeLog));
	}

	/** Get Änderungen protokollieren.
		@return Maintain a log of changes
	  */
	@Override
	public boolean isChangeLog ()
	{
		Object oo = get_Value(COLUMNNAME_IsChangeLog);
		if (oo != null)
		{
			 if (oo instanceof Boolean)
				 return ((Boolean)oo).booleanValue();
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Records deleteable.
		@param IsDeleteable
		Indicates if records can be deleted from the database
	  */
	@Override
	public void setIsDeleteable (boolean IsDeleteable)
	{
		set_Value (COLUMNNAME_IsDeleteable, Boolean.valueOf(IsDeleteable));
	}

	/** Get Records deleteable.
		@return Indicates if records can be deleted from the database
	  */
	@Override
	public boolean isDeleteable ()
	{
		Object oo = get_Value(COLUMNNAME_IsDeleteable);
		if (oo != null)
		{
			 if (oo instanceof Boolean)
				 return ((Boolean)oo).booleanValue();
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Enable remote cache invalidation.
		@param IsEnableRemoteCacheInvalidation Enable remote cache invalidation	  */
	@Override
	public void setIsEnableRemoteCacheInvalidation (boolean IsEnableRemoteCacheInvalidation)
	{
		set_Value (COLUMNNAME_IsEnableRemoteCacheInvalidation, Boolean.valueOf(IsEnableRemoteCacheInvalidation));
	}

	/** Get Enable remote cache invalidation.
		@return Enable remote cache invalidation	  */
	@Override
	public boolean isEnableRemoteCacheInvalidation ()
	{
		Object oo = get_Value(COLUMNNAME_IsEnableRemoteCacheInvalidation);
		if (oo != null)
		{
			 if (oo instanceof Boolean)
				 return ((Boolean)oo).booleanValue();
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set High Volume.
		@param IsHighVolume
		Use Search instead of Pick list
	  */
	@Override
	public void setIsHighVolume (boolean IsHighVolume)
	{
		set_Value (COLUMNNAME_IsHighVolume, Boolean.valueOf(IsHighVolume));
	}

	/** Get High Volume.
		@return Use Search instead of Pick list
	  */
	@Override
	public boolean isHighVolume ()
	{
		Object oo = get_Value(COLUMNNAME_IsHighVolume);
		if (oo != null)
		{
			 if (oo instanceof Boolean)
				 return ((Boolean)oo).booleanValue();
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Security enabled.
		@param IsSecurityEnabled
		If security is enabled, user access to data can be restricted via Roles
	  */
	@Override
	public void setIsSecurityEnabled (boolean IsSecurityEnabled)
	{
		set_Value (COLUMNNAME_IsSecurityEnabled, Boolean.valueOf(IsSecurityEnabled));
	}

	/** Get Security enabled.
		@return If security is enabled, user access to data can be restricted via Roles
	  */
	@Override
	public boolean isSecurityEnabled ()
	{
		Object oo = get_Value(COLUMNNAME_IsSecurityEnabled);
		if (oo != null)
		{
			 if (oo instanceof Boolean)
				 return ((Boolean)oo).booleanValue();
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Ansicht.
		@param IsView
		This is a view
	  */
	@Override
	public void setIsView (boolean IsView)
	{
		set_Value (COLUMNNAME_IsView, Boolean.valueOf(IsView));
	}

	/** Get Ansicht.
		@return This is a view
	  */
	@Override
	public boolean isView ()
	{
		Object oo = get_Value(COLUMNNAME_IsView);
		if (oo != null)
		{
			 if (oo instanceof Boolean)
				 return ((Boolean)oo).booleanValue();
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Reihenfolge.
		@param LoadSeq Reihenfolge	  */
	@Override
	public void setLoadSeq (int LoadSeq)
	{
		set_ValueNoCheck (COLUMNNAME_LoadSeq, Integer.valueOf(LoadSeq));
	}

	/** Get Reihenfolge.
		@return Reihenfolge	  */
	@Override
	public int getLoadSeq ()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_LoadSeq);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Name.
		@param Name Name	  */
	@Override
	public void setName (java.lang.String Name)
	{
		set_Value (COLUMNNAME_Name, Name);
	}

	/** Get Name.
		@return Name	  */
	@Override
	public java.lang.String getName ()
	{
		return (java.lang.String)get_Value(COLUMNNAME_Name);
	}

	/** 
	 * PersonalDataCategory AD_Reference_ID=540857
	 * Reference name: PersonalDataCategory
	 */
	public static final int PERSONALDATACATEGORY_AD_Reference_ID=540857;
	/** NotPersonal = NP */
	public static final String PERSONALDATACATEGORY_NotPersonal = "NP";
	/** Personal = P */
	public static final String PERSONALDATACATEGORY_Personal = "P";
	/** SensitivePersonal = SP */
	public static final String PERSONALDATACATEGORY_SensitivePersonal = "SP";
	/** Set Datenschutz-Kategorie.
		@param PersonalDataCategory Datenschutz-Kategorie	  */
	@Override
	public void setPersonalDataCategory (java.lang.String PersonalDataCategory)
	{

		set_Value (COLUMNNAME_PersonalDataCategory, PersonalDataCategory);
	}

	/** Get Datenschutz-Kategorie.
		@return Datenschutz-Kategorie	  */
	@Override
	public java.lang.String getPersonalDataCategory ()
	{
		return (java.lang.String)get_Value(COLUMNNAME_PersonalDataCategory);
	}

	@Override
	public org.compiere.model.I_AD_Window getPO_Window()
	{
		return get_ValueAsPO(COLUMNNAME_PO_Window_ID, org.compiere.model.I_AD_Window.class);
	}

	@Override
	public void setPO_Window(org.compiere.model.I_AD_Window PO_Window)
	{
		set_ValueFromPO(COLUMNNAME_PO_Window_ID, org.compiere.model.I_AD_Window.class, PO_Window);
	}

	/** Set PO Window.
		@param PO_Window_ID
		Purchase Order Window
	  */
	@Override
	public void setPO_Window_ID (int PO_Window_ID)
	{
		if (PO_Window_ID < 1) 
			set_Value (COLUMNNAME_PO_Window_ID, null);
		else 
			set_Value (COLUMNNAME_PO_Window_ID, Integer.valueOf(PO_Window_ID));
	}

	/** Get PO Window.
		@return Purchase Order Window
	  */
	@Override
	public int getPO_Window_ID ()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_PO_Window_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** 
	 * ReplicationType AD_Reference_ID=126
	 * Reference name: AD_Table Replication Type
	 */
	public static final int REPLICATIONTYPE_AD_Reference_ID=126;
	/** Local = L */
	public static final String REPLICATIONTYPE_Local = "L";
	/** Merge = M */
	public static final String REPLICATIONTYPE_Merge = "M";
	/** Reference = R */
	public static final String REPLICATIONTYPE_Reference = "R";
	/** Set Replication Type.
		@param ReplicationType
		Type of Data Replication
	  */
	@Override
	public void setReplicationType (java.lang.String ReplicationType)
	{

		set_Value (COLUMNNAME_ReplicationType, ReplicationType);
	}

	/** Get Replication Type.
		@return Type of Data Replication
	  */
	@Override
	public java.lang.String getReplicationType ()
	{
		return (java.lang.String)get_Value(COLUMNNAME_ReplicationType);
	}

	/** Set Name der DB-Tabelle.
		@param TableName Name der DB-Tabelle	  */
	@Override
	public void setTableName (java.lang.String TableName)
	{
		set_Value (COLUMNNAME_TableName, TableName);
	}

	/** Get Name der DB-Tabelle.
		@return Name der DB-Tabelle	  */
	@Override
	public java.lang.String getTableName ()
	{
		return (java.lang.String)get_Value(COLUMNNAME_TableName);
	}

	/** Set Technical note.
		@param TechnicalNote
		A note that is not indended for the user documentation, but for developers, customizers etc
	  */
	@Override
	public void setTechnicalNote (java.lang.String TechnicalNote)
	{
		set_Value (COLUMNNAME_TechnicalNote, TechnicalNote);
	}

	/** Get Technical note.
		@return A note that is not indended for the user documentation, but for developers, customizers etc
	  */
	@Override
	public java.lang.String getTechnicalNote ()
	{
		return (java.lang.String)get_Value(COLUMNNAME_TechnicalNote);
	}

	/** 
	 * TooltipType AD_Reference_ID=541141
	 * Reference name: TooltipType
	 */
	public static final int TOOLTIPTYPE_AD_Reference_ID=541141;
	/** DescriptionFallbackToTableIdentifier = DTI */
	public static final String TOOLTIPTYPE_DescriptionFallbackToTableIdentifier = "DTI";
	/** TableIdentifier = T */
	public static final String TOOLTIPTYPE_TableIdentifier = "T";
	/** Description = D */
	public static final String TOOLTIPTYPE_Description = "D";
	/** Set Tooltip Type.
		@param TooltipType Tooltip Type	  */
	@Override
	public void setTooltipType (java.lang.String TooltipType)
	{

		set_Value (COLUMNNAME_TooltipType, TooltipType);
	}

	/** Get Tooltip Type.
		@return Tooltip Type	  */
	@Override
	public java.lang.String getTooltipType ()
	{
		return (java.lang.String)get_Value(COLUMNNAME_TooltipType);
	}

	/**
	 * WhenChildCloningStrategy AD_Reference_ID=541756
	 * Reference name: AD_Table_CloningStrategy
	 */
	public static final int WHENCHILDCLONINGSTRATEGY_AD_Reference_ID=541756;
	/** Skip = S */
	public static final String WHENCHILDCLONINGSTRATEGY_Skip = "S";
	/** AllowCloning = A */
	public static final String WHENCHILDCLONINGSTRATEGY_AllowCloning = "A";
	/** AlwaysInclude = I */
	public static final String WHENCHILDCLONINGSTRATEGY_AlwaysInclude = "I";
	@Override
	public void setWhenChildCloningStrategy (final java.lang.String WhenChildCloningStrategy)
	{
		set_Value (COLUMNNAME_WhenChildCloningStrategy, WhenChildCloningStrategy);
	}

	@Override
	public java.lang.String getWhenChildCloningStrategy()
	{
		return get_ValueAsString(COLUMNNAME_WhenChildCloningStrategy);
	}
}
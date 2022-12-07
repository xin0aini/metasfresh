-- 2022-12-07T10:43:29.294Z
INSERT INTO AD_Process (AccessLevel,AD_Client_ID,AD_Org_ID,AD_Process_ID,AllowProcessReRun,Classname,CopyFromProcess,Created,CreatedBy,EntityType,IsActive,IsApplySecuritySettings,IsBetaFunctionality,IsDirectPrint,IsFormatExcelFile,IsNotifyUserAfterExecution,IsOneInstanceOnly,IsReport,IsTranslateExcelHeaders,IsUpdateExportDate,IsUseBPartnerLanguage,LockWaitTimeout,Name,PostgrestResponseFormat,RefreshAllAfterExecution,ShowHelp,SpreadsheetFormat,Type,Updated,UpdatedBy,Value) VALUES ('3',0,0,585160,'Y','de.metas.externalsystem.other.export.project.workorder.C_Project_SyncTo_Other','N',TO_TIMESTAMP('2022-12-07 12:43:29','YYYY-MM-DD HH24:MI:SS'),100,'de.metas.externalsystem','Y','N','N','N','Y','N','N','N','Y','N','Y',0,'An Other senden','json','N','N','xls','Java',TO_TIMESTAMP('2022-12-07 12:43:29','YYYY-MM-DD HH24:MI:SS'),100,'C_Project_SyncTo_Other')
;

-- 2022-12-07T10:43:29.302Z
INSERT INTO AD_Process_Trl (AD_Language,AD_Process_ID, Description,Help,Name, IsTranslated,AD_Client_ID,AD_Org_ID,Created,Createdby,Updated,UpdatedBy,IsActive) SELECT l.AD_Language, t.AD_Process_ID, t.Description,t.Help,t.Name, 'N',t.AD_Client_ID,t.AD_Org_ID,t.Created,t.Createdby,t.Updated,t.UpdatedBy,'Y' FROM AD_Language l, AD_Process t WHERE l.IsActive='Y'AND (l.IsSystemLanguage='Y') AND t.AD_Process_ID=585160 AND NOT EXISTS (SELECT 1 FROM AD_Process_Trl tt WHERE tt.AD_Language=l.AD_Language AND tt.AD_Process_ID=t.AD_Process_ID)
;

-- Name: Other_ExternalSystem_Config
-- 2022-12-07T10:46:13.599Z
INSERT INTO AD_Reference (AD_Client_ID,AD_Org_ID,AD_Reference_ID,Created,CreatedBy,EntityType,IsActive,IsOrderByValue,Name,Updated,UpdatedBy,ValidationType) VALUES (0,0,541695,TO_TIMESTAMP('2022-12-07 12:46:13','YYYY-MM-DD HH24:MI:SS'),100,'de.metas.externalsystem','Y','N','Other_ExternalSystem_Config',TO_TIMESTAMP('2022-12-07 12:46:13','YYYY-MM-DD HH24:MI:SS'),100,'T')
;

-- 2022-12-07T10:46:13.602Z
INSERT INTO AD_Reference_Trl (AD_Language,AD_Reference_ID, Description,Help,Name, IsTranslated,AD_Client_ID,AD_Org_ID,Created,Createdby,Updated,UpdatedBy,IsActive) SELECT l.AD_Language, t.AD_Reference_ID, t.Description,t.Help,t.Name, 'N',t.AD_Client_ID,t.AD_Org_ID,t.Created,t.Createdby,t.Updated,t.UpdatedBy,'Y' FROM AD_Language l, AD_Reference t WHERE l.IsActive='Y'AND (l.IsSystemLanguage='Y') AND t.AD_Reference_ID=541695 AND NOT EXISTS (SELECT 1 FROM AD_Reference_Trl tt WHERE tt.AD_Language=l.AD_Language AND tt.AD_Reference_ID=t.AD_Reference_ID)
;

-- Reference: Other_ExternalSystem_Config
-- Table: ExternalSystem_Config
-- Key: ExternalSystem_Config.ExternalSystem_Config_ID
-- 2022-12-07T10:47:06.325Z
INSERT INTO AD_Ref_Table (AD_Client_ID,AD_Key,AD_Org_ID,AD_Reference_ID,AD_Table_ID,Created,CreatedBy,EntityType,IsActive,IsValueDisplayed,ShowInactiveValues,Updated,UpdatedBy,WhereClause) VALUES (0,572724,0,541695,541576,TO_TIMESTAMP('2022-12-07 12:47:06','YYYY-MM-DD HH24:MI:SS'),100,'de.metas.externalsystem','Y','N','N',TO_TIMESTAMP('2022-12-07 12:47:06','YYYY-MM-DD HH24:MI:SS'),100,'Type=''Other''')
;

-- 2022-12-07T10:47:33.820Z
INSERT INTO AD_Process_Para (AD_Client_ID,AD_Element_ID,AD_Org_ID,AD_Process_ID,AD_Process_Para_ID,AD_Reference_ID,AD_Reference_Value_ID,ColumnName,Created,CreatedBy,EntityType,FieldLength,IsActive,IsAutocomplete,IsCentrallyMaintained,IsEncrypted,IsMandatory,IsRange,Name,SeqNo,Updated,UpdatedBy) VALUES (0,578728,0,585160,542413,18,541695,'ExternalSystem_Config_ID',TO_TIMESTAMP('2022-12-07 12:47:33','YYYY-MM-DD HH24:MI:SS'),100,'de.metas.externalsystem',0,'Y','N','Y','N','N','N','External System Config',10,TO_TIMESTAMP('2022-12-07 12:47:33','YYYY-MM-DD HH24:MI:SS'),100)
;

-- 2022-12-07T10:47:33.822Z
INSERT INTO AD_Process_Para_Trl (AD_Language,AD_Process_Para_ID, Description,Help,Name, IsTranslated,AD_Client_ID,AD_Org_ID,Created,Createdby,Updated,UpdatedBy,IsActive) SELECT l.AD_Language, t.AD_Process_Para_ID, t.Description,t.Help,t.Name, 'N',t.AD_Client_ID,t.AD_Org_ID,t.Created,t.Createdby,t.Updated,t.UpdatedBy,'Y' FROM AD_Language l, AD_Process_Para t WHERE l.IsActive='Y'AND (l.IsSystemLanguage='Y') AND t.AD_Process_Para_ID=542413 AND NOT EXISTS (SELECT 1 FROM AD_Process_Para_Trl tt WHERE tt.AD_Language=l.AD_Language AND tt.AD_Process_Para_ID=t.AD_Process_Para_ID)
;

-- 2022-12-07T10:48:28.341Z
INSERT INTO AD_Table_Process (AD_Client_ID,AD_Org_ID,AD_Process_ID,AD_Table_ID,AD_Table_Process_ID,Created,CreatedBy,EntityType,IsActive,Updated,UpdatedBy,WEBUI_DocumentAction,WEBUI_IncludedTabTopAction,WEBUI_ViewAction,WEBUI_ViewQuickAction,WEBUI_ViewQuickAction_Default) VALUES (0,0,585160,203,541314,TO_TIMESTAMP('2022-12-07 12:48:28','YYYY-MM-DD HH24:MI:SS'),100,'de.metas.externalsystem','Y',TO_TIMESTAMP('2022-12-07 12:48:28','YYYY-MM-DD HH24:MI:SS'),100,'Y','N','Y','N','N')
;

-- 2022-12-07T10:54:02.773Z
UPDATE AD_Table_Process SET WEBUI_ViewQuickAction='Y',Updated=TO_TIMESTAMP('2022-12-07 12:54:02','YYYY-MM-DD HH24:MI:SS'),UpdatedBy=100 WHERE AD_Table_Process_ID=541314
;

-- 2022-12-07T10:55:27.214Z
UPDATE AD_Table_Process SET EntityType='D',Updated=TO_TIMESTAMP('2022-12-07 12:55:27','YYYY-MM-DD HH24:MI:SS'),UpdatedBy=100 WHERE AD_Table_Process_ID=541314
;

-- 2022-12-07T10:55:32.303Z
UPDATE AD_Process SET EntityType='D',Updated=TO_TIMESTAMP('2022-12-07 12:55:32','YYYY-MM-DD HH24:MI:SS'),UpdatedBy=100 WHERE AD_Process_ID=585160
;

-- Name: Other_ExternalSystem_Config
-- 2022-12-07T10:58:02.366Z
UPDATE AD_Reference SET EntityType='D',Updated=TO_TIMESTAMP('2022-12-07 12:58:02','YYYY-MM-DD HH24:MI:SS'),UpdatedBy=100 WHERE AD_Reference_ID=541695
;

-- Reference: Other_ExternalSystem_Config
-- Table: ExternalSystem_Config
-- Key: ExternalSystem_Config.ExternalSystem_Config_ID
-- 2022-12-07T10:58:11.218Z
UPDATE AD_Ref_Table SET EntityType='D',Updated=TO_TIMESTAMP('2022-12-07 12:58:11','YYYY-MM-DD HH24:MI:SS'),UpdatedBy=100 WHERE AD_Reference_ID=541695
;


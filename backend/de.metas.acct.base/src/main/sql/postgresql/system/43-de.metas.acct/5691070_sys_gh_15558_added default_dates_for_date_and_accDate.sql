-- UI Element: Purchase Invoice(541621,de.metas.ab182) -> Invoice(546648,de.metas.ab182) -> main -> 10 -> payment_modalities.Payment Term
-- Column: C_Invoice.C_PaymentTerm_ID
-- 2023-06-12T14:59:45.420Z
INSERT INTO AD_UI_Element (AD_Client_ID,AD_Field_ID,AD_Org_ID,AD_Tab_ID,AD_UI_Element_ID,AD_UI_ElementGroup_ID,AD_UI_ElementType,Created,CreatedBy,Description,Help,IsActive,IsAdvancedField,IsAllowFiltering,IsDisplayed,IsDisplayed_SideList,IsDisplayedGrid,IsMultiLine,MultiLine_LinesCount,Name,SeqNo,SeqNo_SideList,SeqNoGrid,Updated,UpdatedBy) VALUES (0,707488,0,546648,617983,549985,'F',TO_TIMESTAMP('2023-06-12 17:59:45','YYYY-MM-DD HH24:MI:SS'),100,'The terms of Payment (timing, discount)','Payment Terms identify the method and timing of payment.','Y','N','N','Y','N','N','N',0,'Payment Term',60,0,0,TO_TIMESTAMP('2023-06-12 17:59:45','YYYY-MM-DD HH24:MI:SS'),100)
;

-- UI Element: Purchase Invoice(541621,de.metas.ab182) -> Invoice(546648,de.metas.ab182) -> advanced edit -> 10 -> advanced edit.Zahlungsbedigung
-- Column: C_Invoice.C_PaymentTerm_ID
-- 2023-06-12T15:01:20.206Z
UPDATE AD_UI_Element SET IsDisplayedGrid='Y', SeqNoGrid=180,Updated=TO_TIMESTAMP('2023-06-12 18:01:20','YYYY-MM-DD HH24:MI:SS'),UpdatedBy=100 WHERE AD_UI_Element_ID=613156
;

-- UI Element: Purchase Invoice(541621,de.metas.ab182) -> Invoice(546648,de.metas.ab182) -> advanced edit -> 10 -> advanced edit.Zahlungsbedigung
-- Column: C_Invoice.C_PaymentTerm_ID
-- 2023-06-12T15:02:26.651Z
UPDATE AD_UI_Element SET IsDisplayedGrid='N', SeqNoGrid=0,Updated=TO_TIMESTAMP('2023-06-12 18:02:26','YYYY-MM-DD HH24:MI:SS'),UpdatedBy=100 WHERE AD_UI_Element_ID=613156
;

-- UI Element: Purchase Invoice(541621,de.metas.ab182) -> Invoice(546648,de.metas.ab182) -> advanced edit -> 10 -> advanced edit.Zahlungsbedigung
-- Column: C_Invoice.C_PaymentTerm_ID
-- 2023-06-12T15:07:16.197Z
DELETE FROM AD_UI_Element WHERE AD_UI_Element_ID=613156
;

-- Process: ab182_C_Invoice_Candidate_EnqueueSelectionForInvoicing_Purchase(de.metas.invoicecandidate.process.C_Invoice_Candidate_EnqueueSelectionForInvoicing)
-- ParameterName: DateInvoiced
-- 2023-06-12T15:11:50.017Z
UPDATE AD_Process_Para SET DefaultValue='@Date@',Updated=TO_TIMESTAMP('2023-06-12 18:11:50','YYYY-MM-DD HH24:MI:SS'),UpdatedBy=100 WHERE AD_Process_Para_ID=542355
;

-- Process: ab182_C_Invoice_Candidate_EnqueueSelectionForInvoicing_Purchase(de.metas.invoicecandidate.process.C_Invoice_Candidate_EnqueueSelectionForInvoicing)
-- ParameterName: DateInvoiced
-- 2023-06-12T15:12:32.056Z
UPDATE AD_Process_Para SET DefaultValue='@#Date@',Updated=TO_TIMESTAMP('2023-06-12 18:12:32','YYYY-MM-DD HH24:MI:SS'),UpdatedBy=100 WHERE AD_Process_Para_ID=542355
;

-- Process: ab182_C_Invoice_Candidate_EnqueueSelectionForInvoicing_Purchase(de.metas.invoicecandidate.process.C_Invoice_Candidate_EnqueueSelectionForInvoicing)
-- ParameterName: DateAcct
-- 2023-06-12T15:13:32.761Z
UPDATE AD_Process_Para SET DefaultValue='@#Date#',Updated=TO_TIMESTAMP('2023-06-12 18:13:32','YYYY-MM-DD HH24:MI:SS'),UpdatedBy=100 WHERE AD_Process_Para_ID=542356
;

-- Process: ab182_C_Invoice_Candidate_EnqueueSelectionForInvoicing_Purchase(de.metas.invoicecandidate.process.C_Invoice_Candidate_EnqueueSelectionForInvoicing)
-- ParameterName: DateAcct
-- 2023-06-12T15:14:36.209Z
UPDATE AD_Process_Para SET DefaultValue='@#Date@',Updated=TO_TIMESTAMP('2023-06-12 18:14:36','YYYY-MM-DD HH24:MI:SS'),UpdatedBy=100 WHERE AD_Process_Para_ID=542356
;


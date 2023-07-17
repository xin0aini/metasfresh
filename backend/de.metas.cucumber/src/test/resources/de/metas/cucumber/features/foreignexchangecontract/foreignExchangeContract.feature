@from:cucumber
Feature: foreign exchange contract

  Background:
    Given infrastructure and metasfresh are running
    And the existing user with login 'metasfresh' receives a random a API token for the existing role with name 'WebUI'
    And set sys config boolean value true for sys config SKIP_WP_PROCESSOR_FOR_AUTOMATION
    And metasfresh has date and time 2023-07-13T13:30:13+01:00[Europe/Berlin]

    And load M_Warehouse:
      | M_Warehouse_ID.Identifier | Value        |
      | warehouseStd              | StdWarehouse |
    And load C_Currency:
      | C_Currency_ID.Identifier | OPT.C_Currency_ID |
      | eur                      | 102               |
      | chf                      | 318               |
    And load C_AcctSchema:
      | C_AcctSchema_ID.Identifier | OPT.Name              |
      | acctSchema_1               | metas fresh UN/34 CHF |
    And update C_AcctSchema:
      | C_AcctSchema_ID.Identifier | OPT.C_Currency_ID.Identifier |
      | acctSchema_1               | chf                          |
    And metasfresh contains M_Products:
      | Identifier | Name                    |
      | p_1        | d_salesProduct_13072023 |
    And metasfresh contains M_PricingSystems
      | Identifier | Name                           | Value                           | OPT.IsActive |
      | ps_1       | d_pricing_system_name_13072023 | d_pricing_system_value_13072023 | true         |
    And metasfresh contains M_PriceLists
      | Identifier | M_PricingSystem_ID.Identifier | OPT.C_Country.CountryCode | C_Currency.ISO_Code | Name                          | OPT.Description | SOTrx | IsTaxIncluded | PricePrecision | OPT.IsActive |
      | pl_1       | ps_1                          | DE                        | EUR                 | d_price_list_name_SO_13072023 | null            | true  | false         | 2              | true         |
      | pl_2       | ps_1                          | DE                        | EUR                 | d_price_list_name_PO_13072023 | null            | false | false         | 2              | true         |
    And metasfresh contains M_PriceList_Versions
      | Identifier | M_PriceList_ID.Identifier | Name                         | ValidFrom  |
      | plv_1      | pl_1                      | d_salesOrder-PLV_13072023    | 2021-04-01 |
      | plv_1      | pl_2                      | d_purchaseOrder-PLV_13072023 | 2021-04-01 |
    And metasfresh contains M_ProductPrices
      | Identifier | M_PriceList_Version_ID.Identifier | M_Product_ID.Identifier | PriceStd | C_UOM_ID.X12DE355 | C_TaxCategory_ID.InternalName |
      | pp_1       | plv_1                             | p_1                     | 10.0     | PCE               | Normal                        |
      | pp_1       | plv_2                             | p_1                     | 10.0     | PCE               | Normal                        |
    And metasfresh contains C_BPartners without locations:
      | Identifier    | Name                  | OPT.IsVendor | OPT.IsCustomer | M_PricingSystem_ID.Identifier | OPT.InvoiceRule |
      | endcustomer_1 | Endcustomer2_13072023 | N            | Y              | ps_1                          | D               |
      | endvendor_1   | Endvendor2_13072023   | Y            | N              | ps_2                          | D               |
    And metasfresh contains C_BPartner_Locations:
      | Identifier | GLN           | C_BPartner_ID.Identifier | OPT.IsShipToDefault | OPT.IsBillToDefault |
      | l_1        | 0121456739015 | endcustomer_1            | Y                   | Y                   |
      | l_2        | 0121456439016 | endvendor_1              | Y                   | Y                   |
    And load C_Tax:
      | C_Tax_ID.Identifier | OPT.C_Tax_ID |
      | tax_1               | 1000023      |
    And metasfresh contains C_Conversion_Rate:
      | C_Conversion_Rate_ID.Identifier | C_Currency_ID.Identifier | C_Currency_ID_To.Identifier | ValidFrom  | ValidTo    | MultiplyRate | DivideRate    | C_ConversionType_ID |
      | conversion_rate_eur_chf         | eur                      | chf                         | 2023-07-01 | 2023-07-29 | 1.0728       | 0.95672173557 | 201                 |
    Then metasfresh contains C_ForeignExchangeContract
      | C_ForeignExchangeContract_ID.Identifier | C_Currency_ID.Identifier | To_Currency_ID.Identifier | FEC_CurrencyRate | FEC_Amount | FEC_Amount_Alloc | FEC_Amount_Open | FEC_MaturityDate | FEC_ValidityDate |
      | foreignExchangeContract_07132023        | eur                      | chf                       | 2                | 10         | 0                | 10              | 2023-07-15       | 2023-07-14       |
    And the foreign exchange contract identified by foreignExchangeContract_07132023 is completed

  @from:cucumber
  Scenario: foreign exchange contract with sales order
    And metasfresh contains C_Orders:
      | Identifier | IsSOTrx | C_BPartner_ID.Identifier | DateOrdered |
      | o_1        | true    | endcustomer_1            | 2023-07-13  |
    And metasfresh contains C_OrderLines:
      | Identifier | C_Order_ID.Identifier | M_Product_ID.Identifier | QtyEntered | OPT.C_Tax_ID.Identifier |
      | ol_1       | o_1                   | p_1                     | 10         | tax_1                   |
    When the order identified by o_1 is completed
    And after not more than 30s, load created M_Delivery_Planning:
      | M_Delivery_Planning_ID.Identifiers | C_OrderLine_ID.Identifier |
      | deliveryPlanning_SO                | ol_1                      |
    And allocate C_ForeignExchangeContract to order
      | C_ForeignExchangeContract_ID.Identifier | C_Order_ID.Identifier | FEC_Amount_Alloc |
      | foreignExchangeContract_07132023        | o_1                   | 10               |
    When generate goods issue:
      | M_Delivery_Planning_ID.Identifier | C_ForeignExchangeContract_ID.Identifier | C_Currency_ID.Identifier | To_Currency_ID.Identifier | C_Order_ID.C_Currency_ID.Identifier | FEC_CurrencyRate |
      | deliveryPlanning_SO               | foreignExchangeContract_07132023        | eur                      | chf                       | eur                                 | 2                |
    And after not more than 30s, M_ShipmentSchedules are found:
      | Identifier | C_OrderLine_ID.Identifier | IsToRecompute |
      | s_s_1      | ol_1                      | N             |
    Then after not more than 30s, M_InOut is found:
      | M_ShipmentSchedule_ID.Identifier | M_InOut_ID.Identifier |
      | s_s_1                            | shipment_1            |
    And after not more than 30s, Fact_Acct are found
      | TableName | Record_ID.Identifier | OPT.CurrencyRate |
      | M_InOut   | shipment_1           | 0                |
    And after not more than 30s locate invoice candidates by order line:
      | C_Invoice_Candidate_ID.Identifier | C_OrderLine_ID.Identifier |
      | invoice_candidate_1               | ol_1                      |
    And process invoice candidates and wait 30s for C_Invoice_Candidate to be processed
      | C_Invoice_Candidate_ID.Identifier | OPT.C_ForeignExchangeContract_ID.Identifier | OPT.C_Currency_ID.Identifier | OPT.To_Currency_ID.Identifier | OPT.C_Order_ID.C_Currency_ID.Identifier | OPT.FEC_CurrencyRate | OPT.WaitForProcessedCandidate |
      | invoice_candidate_1               | foreignExchangeContract_07132023            | eur                          | chf                           | eur                                     | 2                    | N                             |
    And after not more than 30s, C_Invoice are found:
      | C_Invoice_ID.Identifier | C_Invoice_Candidate_ID.Identifier |
      | invoice_1               | invoice_candidate_1               |
    And after not more than 30s, Fact_Acct are found
      | TableName | Record_ID.Identifier | OPT.CurrencyRate |
      | C_Invoice | invoice_1            | 2                |

  @from:cucumber
  Scenario: foreign exchange contract with purchase order
    And metasfresh contains C_Orders:
      | Identifier | IsSOTrx | OPT.DocBaseType | C_BPartner_ID.Identifier | DateOrdered |
      | o_1        | false   | POO             | endvendor_1              | 2023-07-13  |
    And metasfresh contains C_OrderLines:
      | Identifier | C_Order_ID.Identifier | M_Product_ID.Identifier | QtyEntered | OPT.C_Tax_ID.Identifier |
      | ol_1       | o_1                   | p_1                     | 10         | tax_1                   |
    When the order identified by o_1 is completed
    And after not more than 30s, load created M_Delivery_Planning:
      | M_Delivery_Planning_ID.Identifiers | C_OrderLine_ID.Identifier |
      | deliveryPlanning_PO                | ol_1                      |
    And allocate C_ForeignExchangeContract to order
      | C_ForeignExchangeContract_ID.Identifier | C_Order_ID.Identifier | FEC_Amount_Alloc |
      | foreignExchangeContract_07132023        | o_1                   | 10               |
    When generate goods receipt:
      | M_Delivery_Planning_ID.Identifier | C_ForeignExchangeContract_ID.Identifier | C_Currency_ID.Identifier | To_Currency_ID.Identifier | C_Order_ID.C_Currency_ID.Identifier | FEC_CurrencyRate |
      | deliveryPlanning_PO               | foreignExchangeContract_07132023        | eur                      | chf                       | eur                                 | 2                |
    And after not more than 30s, M_ShipmentSchedules are found:
      | Identifier | C_OrderLine_ID.Identifier | IsToRecompute |
      | s_s_1      | ol_1                      | N             |
    And after not more than 30s, M_ReceiptSchedule are found:
      | M_ReceiptSchedule_ID.Identifier | C_Order_ID.Identifier | C_OrderLine_ID.Identifier | C_BPartner_ID.Identifier | C_BPartner_Location_ID.Identifier | M_Product_ID.Identifier | QtyOrdered | M_Warehouse_ID.Identifier |
      | receiptSchedule_PO              | o_1                   | ol_1                      | endvendor_1              | l_2                               | p_1                     | 10         | warehouseStd              |

    Then after not more than 30s, M_InOut is found for material receipt:
      | M_ShipmentSchedule_ID.Identifier | M_InOut_ID.Identifier |
      | s_s_1                            | receipt_1             |
    And after not more than 30s, Fact_Acct are found
      | TableName | Record_ID.Identifier | OPT.CurrencyRate |
      | M_InOut   | receipt_1            | 2                |
    And after not more than 30s locate invoice candidates by order line:
      | C_Invoice_Candidate_ID.Identifier | C_OrderLine_ID.Identifier |
      | invoice_candidate_1               | ol_1                      |
    And process invoice candidates and wait 30s for C_Invoice_Candidate to be processed
      | C_Invoice_Candidate_ID.Identifier | OPT.C_ForeignExchangeContract_ID.Identifier | OPT.C_Currency_ID.Identifier | OPT.To_Currency_ID.Identifier | OPT.C_Order_ID.C_Currency_ID.Identifier | OPT.FEC_CurrencyRate | OPT.WaitForProcessedCandidate |
      | invoice_candidate_1               | foreignExchangeContract_07132023            | eur                          | chf                           | eur                                     | 2                    | N                             |
    And after not more than 30s, C_Invoice are found:
      | C_Invoice_ID.Identifier | C_Invoice_Candidate_ID.Identifier |
      | invoice_1               | invoice_candidate_1               |
    And after not more than 30s, Fact_Acct are found
      | TableName | Record_ID.Identifier | OPT.CurrencyRate |
      | C_Invoice | invoice_1            | 2                |

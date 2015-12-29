SELECT 
  CAST (CASE WHEN RTRADM.SALES_GEOGRAPHY_REF.THTR_OPS = 'NA' THEN 'CORP' ELSE RTRADM.SALES_GEOGRAPHY_REF.THTR_OPS END AS VARCHAR(20)), 
  RTRADM.RTR_REPORT_DATA.SOURCE_BUCKET, 
  RTRADM.RTR_REPORT_DATA.CREATION_DATE, 
  SUM (RTRADM.RTR_REPORT_DATA.REVENUE * RTRADM.SALES_CREDITS.PERCENT / 100), 
  RTRADM.RTR_REPORT_DATA.RPTG_YR_QTR_ABBRV, 
  RTRADM.RTR_REPORT_DATA.RPTG_PRD_TYPE 
FROM 
  RTRADM.SALES_GEOGRAPHY_REF, 
  RTRADM.RTR_REPORT_DATA, 
  RTRADM.SALES_CREDITS 
WHERE 
  (RTRADM.SALES_GEOGRAPHY_REF.DSTRCT_ID = RTRADM.SALES_CREDITS.DISTRICT_ID) 
  AND 
  (RTRADM.SALES_CREDITS.SO_NUMBER = RTRADM.RTR_REPORT_DATA.ORD_NUM) 
  AND 
  (RTRADM.RTR_REPORT_DATA.RECORD_STATUS = 'ACTIVE') 
  AND 
  (RTRADM.SALES_CREDITS.EFFECTIVE_END_DATE IS NULL) 
  AND 
  (RTRADM.RTR_REPORT_DATA.RPTG_YR_QTR_ABBRV = '2014-Q4' AND RTRADM.RTR_REPORT_DATA.SOURCE_BUCKET = 'QTD' AND RTRADM.RTR_REPORT_DATA.BUCKET IN ('BOOKINGS','CONTRACT_BOOKINGS')) 
GROUP BY 
  CAST (CASE WHEN RTRADM.SALES_GEOGRAPHY_REF.THTR_OPS = 'NA' THEN 'CORP' ELSE RTRADM.SALES_GEOGRAPHY_REF.THTR_OPS END AS VARCHAR(20)), 
  RTRADM.RTR_REPORT_DATA.SOURCE_BUCKET, 
  RTRADM.RTR_REPORT_DATA.CREATION_DATE, 
  RTRADM.RTR_REPORT_DATA.RPTG_YR_QTR_ABBRV, 
  RTRADM.RTR_REPORT_DATA.RPTG_PRD_TYPE;
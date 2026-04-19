import { createSlice, PayloadAction } from "@reduxjs/toolkit";

export interface ComplianceState {
  kycStatus: "UNVERIFIED" | "PENDING" | "VERIFIED" | "SUSPENDED";
  amlRiskLevel: "LOW" | "MEDIUM" | "HIGH" | "CRITICAL";
  auditLogs: AuditLogEntry[];
  complianceAlerts: ComplianceAlert[];
}

export interface AuditLogEntry {
  id: string;
  timestamp: string;
  action: string;
  ipAddress: string;
  details: string;
}

export interface ComplianceAlert {
  id: string;
  timestamp: string;
  severity: "INFO" | "WARNING" | "CRITICAL";
  message: string;
  resolved: boolean;
}

const initialState: ComplianceState = {
  kycStatus: "UNVERIFIED",
  amlRiskLevel: "LOW",
  auditLogs: [],
  complianceAlerts: [],
};

const complianceSlice = createSlice({
  name: "compliance",
  initialState,
  reducers: {
    hydrateCompliance(state, action: PayloadAction<ComplianceState>) {
      return action.payload;
    },
    updateKycStatus(state, action: PayloadAction<ComplianceState["kycStatus"]>) {
      state.kycStatus = action.payload;
    },
    updateAmlRisk(state, action: PayloadAction<ComplianceState["amlRiskLevel"]>) {
      state.amlRiskLevel = action.payload;
    },
    logAuditAction(state, action: PayloadAction<AuditLogEntry>) {
      state.auditLogs.unshift(action.payload);
      if (state.auditLogs.length > 500) {
        state.auditLogs.pop(); // keep last 500 for UI, backend keeps all
      }
    },
    triggerComplianceAlert(state, action: PayloadAction<ComplianceAlert>) {
      state.complianceAlerts.unshift(action.payload);
    },
    resolveComplianceAlert(state, action: PayloadAction<string>) {
      const alert = state.complianceAlerts.find(a => a.id === action.payload);
      if (alert) {
        alert.resolved = true;
      }
    }
  },
});

export const { hydrateCompliance, updateKycStatus, updateAmlRisk, logAuditAction, triggerComplianceAlert, resolveComplianceAlert } = complianceSlice.actions;
export default complianceSlice.reducer;


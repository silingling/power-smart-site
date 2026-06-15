package com.powersmart.common.constant;

public interface ApiConstant {
    String HEADER_TRACE_ID = "X-Trace-Id";
    String HEADER_PROJECT_ID = "X-Project-Id";

    interface WorkerStatus {
        int ON_JOB = 1;
        int OFF_JOB = 0;
    }

    interface DeviceStatus {
        int NORMAL = 1;
        int RUNNING = 2;
        int MAINTENANCE = 3;
        int RETIRED = 4;
    }

    interface HazardLevel {
        int GENERAL = 1;
        int MAJOR = 2;
        int CRITICAL = 3;
    }

    interface HazardStatus {
        int PENDING = 1;
        int IN_PROGRESS = 2;
        int VERIFIED = 3;
        int ARCHIVED = 4;
    }

    interface WorkOrderStatus {
        int PENDING = 1;
        int RECTIFIED = 2;
        int PASSED = 3;
        int REJECTED = 4;
    }

    interface ProgressTaskStatus {
        int NOT_STARTED = 0;
        int IN_PROGRESS = 1;
        int COMPLETED = 2;
        int DELAYED = 3;
    }

    interface ReportType {
        int AI = 1;
        int MANUAL = 2;
    }
}

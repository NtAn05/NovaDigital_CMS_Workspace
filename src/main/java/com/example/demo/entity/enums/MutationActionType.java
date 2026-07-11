package com.example.demo.entity.enums;

/**
 * Classifies the type of data mutation that was performed on a Milestone.
 * Used in MilestoneMutationLog for Audit Trail categorization.
 */
public enum MutationActionType {
    CREATE,         // Cột mốc mới được tạo
    SYNC_UPDATE,    // Cập nhật qua Sync Endpoint (do PM kéo thanh tiến độ)
    MANUAL_UPDATE,  // Chỉnh sửa thủ công trên Admin UI
    DELETE          // Cột mốc bị xóa
}

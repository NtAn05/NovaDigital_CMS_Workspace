package com.example.demo.entity.enums;

/**
 * F_38 — Recruitment pipeline status for a CandidateApplication.
 * HR transitions an application through these stages:
 *
 *   PENDING → INTERVIEW → APPROVED
 *                       ↘ REJECTED
 *
 * Any stage can also be moved directly to REJECTED.
 */
public enum ApplicationStatus {

    /** Newly submitted – awaiting HR review (default). */
    PENDING,

    /** HR has viewed the candidate application / CV. */
    VIEWED,

    /** HR has scheduled / is planning an interview with this candidate. */
    INTERVIEW,

    /** Candidate passed evaluation and is approved for hire. */
    APPROVED,

    /** Candidate did not pass; application closed. */
    REJECTED
}

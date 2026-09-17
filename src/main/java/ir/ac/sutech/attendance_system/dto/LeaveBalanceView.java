package ir.ac.sutech.attendance_system.dto;

public class LeaveBalanceView {

    private final int year;
    private final int annualAllowanceDays;
    private final long usedDays;
    private final long remainingDays;

    public LeaveBalanceView(
            int year,
            int annualAllowanceDays,
            long usedDays
    ) {
        this.year = year;
        this.annualAllowanceDays = annualAllowanceDays;
        this.usedDays = Math.max(usedDays, 0);
        this.remainingDays = Math.max(
                (long) annualAllowanceDays - this.usedDays,
                0
        );
    }

    public int getYear() {
        return year;
    }

    public int getAnnualAllowanceDays() {
        return annualAllowanceDays;
    }

    public long getUsedDays() {
        return usedDays;
    }

    public long getRemainingDays() {
        return remainingDays;
    }
}

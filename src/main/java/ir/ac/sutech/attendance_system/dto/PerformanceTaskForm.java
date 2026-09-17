package ir.ac.sutech.attendance_system.dto;

public class PerformanceTaskForm {

    private Long employeeId;
    private Integer year;
    private Integer quarter;
    private String title;
    private String description;
    private Integer weightPercent;

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getQuarter() {
        return quarter;
    }

    public void setQuarter(Integer quarter) {
        this.quarter = quarter;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getWeightPercent() {
        return weightPercent;
    }

    public void setWeightPercent(Integer weightPercent) {
        this.weightPercent = weightPercent;
    }
}

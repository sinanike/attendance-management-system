package ir.ac.sutech.attendance_system.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "official_holidays",
        uniqueConstraints = {
                @UniqueConstraint(
                        name =
                                "uk_official_holiday_date",
                        columnNames =
                                "holiday_date"
                )
        },
        indexes = {
                @Index(
                        name =
                                "idx_official_holiday_date",
                        columnList =
                                "holiday_date"
                ),
                @Index(
                        name =
                                "idx_official_holiday_active",
                        columnList =
                                "active"
                )
        }
)
public class OfficialHoliday {

    @Id
    @GeneratedValue(
            strategy =
                    GenerationType.IDENTITY
    )
    private Long id;


    @Column(
            name = "holiday_date",
            nullable = false
    )
    private LocalDate holidayDate;


    @Column(
            nullable = false,
            length = 200
    )
    private String title;


    @Column(
            length = 1000
    )
    private String description;


    @Column(
            nullable = false
    )
    private boolean active = true;


    @Column(
            name = "created_at",
            nullable = false
    )
    private LocalDateTime createdAt;


    protected OfficialHoliday() {
    }


    public OfficialHoliday(
            LocalDate holidayDate,
            String title
    ) {
        this.holidayDate =
                holidayDate;

        this.title =
                title;
    }


    @PrePersist
    public void onCreate() {

        createdAt =
                LocalDateTime.now();
    }


    public Long getId() {
        return id;
    }


    public LocalDate getHolidayDate() {
        return holidayDate;
    }

    public void setHolidayDate(
            LocalDate holidayDate
    ) {
        this.holidayDate =
                holidayDate;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(
            String title
    ) {
        this.title = title;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(
            String description
    ) {
        this.description =
                description;
    }


    public boolean isActive() {
        return active;
    }

    public void setActive(
            boolean active
    ) {
        this.active = active;
    }


    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
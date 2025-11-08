package dev.dsa.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfile extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "page_size")
    @Builder.Default
    private Integer pageSize = 11; // Default page size

    @Column(name = "timezone", length = 50)
    @Builder.Default
    private String timezone = "UTC";

    @Column(name = "language", length = 10)
    @Builder.Default
    private String language = "en";

    @Column(name = "date_format", length = 20)
    @Builder.Default
    private String dateFormat = "yyyy-MM-dd";

    @Column(name = "time_format", length = 20)
    @Builder.Default
    private String timeFormat = "HH:mm:ss";

    @Column(name = "theme", length = 20)
    @Builder.Default
    private String theme = "light";

    @Override
    public String toString() {
        return "UserProfile{" +
                "id=" + id +
                ", userId=" + (user != null ? user.getId() : null) +
                ", pageSize=" + pageSize +
                ", timezone='" + timezone + '\'' +
                ", language='" + language + '\'' +
                '}';
    }
}

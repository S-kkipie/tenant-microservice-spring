package unsa.sistemas.tenantservice.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;


    private String name;
    private String description;
    private Modality modality;
    private String ownerId;

    @ManyToOne
    @JoinColumn(name = "type_id")
    private Type type;

    @Column(unique = true, nullable = false)
    private String code;



    private Boolean enabled;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

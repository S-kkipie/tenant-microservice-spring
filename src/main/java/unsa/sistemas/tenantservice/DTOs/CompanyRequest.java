package unsa.sistemas.tenantservice.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import unsa.sistemas.tenantservice.Models.Modality;

import java.io.Serial;
import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CompanyRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 5926468583005150707L;

    private String name;
    private String description;
    private String code;
    private Long typeId;
    private Modality modality;
}

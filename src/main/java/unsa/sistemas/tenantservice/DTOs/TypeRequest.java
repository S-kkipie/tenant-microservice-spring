package unsa.sistemas.tenantservice.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TypeRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String name;
}

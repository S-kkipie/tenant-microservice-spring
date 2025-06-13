package unsa.sistemas.tenantservice.DTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;


@AllArgsConstructor
@Builder
@Data
public class CreateDataBaseEvent {
    private String encryptedPayload;
}
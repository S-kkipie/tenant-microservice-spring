package unsa.sistemas.tenantservice.Services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import unsa.sistemas.tenantservice.DTOs.CreateTypeRequest;
import unsa.sistemas.tenantservice.Models.Type;
import unsa.sistemas.tenantservice.Repositories.TypeRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class TypeService {
    private final TypeRepository typeRepository;

    //TODO check if postgres manage collisions with name
    public Type createType(CreateTypeRequest createTypeRequest) {
        return typeRepository.save(Type.builder()
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .enabled(true)
                .name(createTypeRequest.getName())
                .build());
    }

    public List<Type> getAllTypes() {
        return typeRepository.findAll();
    }
}

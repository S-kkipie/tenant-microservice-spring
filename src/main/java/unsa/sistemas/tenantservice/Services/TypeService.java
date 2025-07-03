package unsa.sistemas.tenantservice.Services;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import unsa.sistemas.tenantservice.DTOs.TypeRequest;
import unsa.sistemas.tenantservice.Models.Type;
import unsa.sistemas.tenantservice.Repositories.TypeRepository;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class TypeService {
    private final TypeRepository typeRepository;

    //TODO check if postgres manage collisions with name
    public Type createType(TypeRequest typeRequest) {
        return typeRepository.save(Type.builder()
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .enabled(true)
                .name(typeRequest.getName())
                .build());
    }

    public Page<Type> findTypes(int pageNumber, int size, String search) {
        Pageable page = PageRequest.of(pageNumber, size, Sort.sort(Type.class).by(Type::getUsageCount).descending());
        return typeRepository.findByNameContainingIgnoreCase(search, page);
    }

    public Type getTypeById(Long id) {
        return typeRepository.findById(id).orElseThrow(() -> new RuntimeException("Type not found"));
    }

    public void deleteType(Long id) {
        Type type = typeRepository.findById(id).orElseThrow(() -> new RuntimeException("Type not found"));
        typeRepository.delete(type);
    }
}

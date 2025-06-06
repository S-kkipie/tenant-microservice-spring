package unsa.sistemas.tenantservice.Services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import unsa.sistemas.tenantservice.DTOs.CreateCompanyRequest;
import unsa.sistemas.tenantservice.Models.Company;
import unsa.sistemas.tenantservice.Models.Type;
import unsa.sistemas.tenantservice.Repositories.CompanyRepository;
import unsa.sistemas.tenantservice.Repositories.TypeRepository;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class CompanyService {
    private final CompanyRepository companyRepository;
    private final TypeRepository typeRepository;

    //TODO check if postgres manage collisions with code
    public Company createCompany(CreateCompanyRequest request, String ownerId) {
        Type type = typeRepository.findById(request.getTypeId()).orElseThrow();
        return companyRepository.save(Company.builder()
                .createdAt(LocalDateTime.now())
                .code(request.getCode())
                .type(type)
                .description(request.getDescription())
                .modality(request.getModality())
                .enabled(true)
                .name(request.getName())
                .updatedAt(LocalDateTime.now())
                .ownerId(ownerId)
                .build());
    }

}

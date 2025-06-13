package unsa.sistemas.tenantservice.Services;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import unsa.sistemas.tenantservice.DTOs.CreateCompanyRequest;
import unsa.sistemas.tenantservice.DTOs.CreateDataBaseEvent;
import unsa.sistemas.tenantservice.Messaging.TenantEventProducer;
import unsa.sistemas.tenantservice.Models.Company;
import unsa.sistemas.tenantservice.Models.Type;
import unsa.sistemas.tenantservice.Repositories.CompanyRepository;
import unsa.sistemas.tenantservice.Repositories.TypeRepository;
import unsa.sistemas.tenantservice.Utils.EncryptionUtil;
import unsa.sistemas.tenantservice.Utils.RandomPasswordGenerator;
import unsa.sistemas.tenantservice.Utils.URLUtil;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CompanyService {
    private final CompanyRepository companyRepository;
    private final TypeRepository typeRepository;
    private final DockerTenantService dockerTenantService;
    private final TenantEventProducer tenantEventProducer;
    private final EncryptionUtil encryptionUtil;

    @Value("${app.base-url}")
    String URL_BASE;

    //TODO check if postgres manage collisions with code
    public Company createCompany(CreateCompanyRequest request, String ownerId) throws Exception {
        Company existingCompany = companyRepository.findCompanyByCode(request.getCode()).orElse(null);

        if (existingCompany != null) {
            throw new DuplicateKeyException("Company already exists");
        }

        Type type = typeRepository.findById(request.getTypeId()).orElseThrow(() -> new NotFoundException("Type not found"));

        String password = RandomPasswordGenerator.generateRandomPassword(20);

        Company company = companyRepository.save(Company.builder()
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

        Company createdCompany = dockerTenantService.createContainer(company, password);

        createdCompany.setDataBasePassword(password);

        //TODO Abstract send message for every microservices
        String dbName = company.getCode() + "_identity_db";

        Map<String, String> payload = Map.of(
                "url", URLUtil.generateUrl(URL_BASE, createdCompany.getDataBasePort(), dbName),
                "username", company.getOwnerId(),
                "password", password,
                "dbName", dbName,
                "orgCode", company.getCode()
        );

        String json = new ObjectMapper().writeValueAsString(payload);

        String encrypted = encryptionUtil.encrypt(json);


        tenantEventProducer.sendDatabaseCreatedEvent(new CreateDataBaseEvent(encrypted));

        return createdCompany;
    }

    public Company findCompanyByCode(String code) {
        return companyRepository.findCompanyByCode(code).orElseThrow(() -> new NotFoundException("Company not found"));
    }

}

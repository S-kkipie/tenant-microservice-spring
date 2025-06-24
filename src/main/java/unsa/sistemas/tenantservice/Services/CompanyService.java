package unsa.sistemas.tenantservice.Services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import unsa.sistemas.tenantservice.Config.UserContext;
import unsa.sistemas.tenantservice.Config.UserContextHolder;
import unsa.sistemas.tenantservice.DTOs.CompanyRequest;
import unsa.sistemas.tenantservice.DTOs.CreateDataBaseEvent;
import unsa.sistemas.tenantservice.Messaging.TenantEventProducer;
import unsa.sistemas.tenantservice.Models.Company;
import unsa.sistemas.tenantservice.Models.Role;
import unsa.sistemas.tenantservice.Models.Type;
import unsa.sistemas.tenantservice.Repositories.CompanyRepository;
import unsa.sistemas.tenantservice.Repositories.TypeRepository;
import unsa.sistemas.tenantservice.Utils.EncryptionUtil;
import unsa.sistemas.tenantservice.Utils.RandomPasswordGenerator;
import unsa.sistemas.tenantservice.Utils.URLUtil;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

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
    public Company createCompany(CompanyRequest request, String username) throws Exception {
        Company existingCompany = companyRepository.findCompanyByCode(request.getCode()).orElse(null);

        if (existingCompany != null) {
            throw new DuplicateKeyException("Company already exists");
        }

        Type type = typeRepository.findById(request.getTypeId()).orElseThrow(() -> new IllegalArgumentException("Type not found"));

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
                .username(username)
                .build());

        Company createdCompany = dockerTenantService.createContainer(company, password);


        Map<String, String> payload = Map.of(
                "url", URLUtil.generateUrl(URL_BASE, createdCompany.getDataBasePort()),
                "username", company.getUsername(),
                "password", password,
                "orgCode", company.getCode()
        );

        String json = new ObjectMapper().writeValueAsString(payload);

        String encrypted = encryptionUtil.encrypt(json);


        tenantEventProducer.sendDatabaseCreatedEvent(new CreateDataBaseEvent(encrypted));

        return createdCompany;
    }

    public void deployAllCompanies() {
        java.util.List<Company> companies = companyRepository.findAll();
        for (Company company : companies) {

            Map<String, String> payload = Map.of(
                    "url", URLUtil.generateUrl(URL_BASE, company.getDataBasePort()),
                    "username", company.getUsername(),
                    "password", company.getDataBasePassword(),
                    "orgCode", company.getCode(),
                    "physicalExists", "true"
            );

            try {
                String json = new ObjectMapper().writeValueAsString(payload);
                String encrypted = encryptionUtil.encrypt(json);
                tenantEventProducer.sendDatabaseCreatedEvent(new CreateDataBaseEvent(encrypted));
            } catch (Exception e) {
                throw new RuntimeException("Error while sending database created event for company: " + company.getCode(), e);
            }
        }
    }


    public Company findCompanyByCode(String code) {
        UserContext context = UserContextHolder.get();
        Role role = context.getRole();

        Company company = companyRepository.findCompanyByCode(code).orElseThrow(() -> new IllegalArgumentException("Company not found"));

        if (!(role == Role.ROLE_PRINCIPAL_ADMIN || Objects.equals(context.getUsername(), company.getUsername()))) {
            throw new IllegalArgumentException("You don't have access to this company");
        }
        return company;
    }

    public java.util.List<Company> getAllCompanies() {
        return companyRepository.findAll();
    }

    public Company updateCompany(String code, CompanyRequest updatedCompany) {
        UserContext context = UserContextHolder.get();
        Role role = context.getRole();

        Company existingCompany = companyRepository.findCompanyByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));

        if (!(role == Role.ROLE_PRINCIPAL_ADMIN || Objects.equals(context.getUsername(), existingCompany.getUsername()))) {
            throw new IllegalArgumentException("You don't have access to this company");
        }



        Type type = typeRepository.findById(updatedCompany.getTypeId()).orElseThrow(() -> new IllegalArgumentException("Type not found"));

        existingCompany.setName(updatedCompany.getName());
        existingCompany.setDescription(updatedCompany.getDescription());
        existingCompany.setModality(updatedCompany.getModality());
        existingCompany.setType(type);
        existingCompany.setUpdatedAt(java.time.LocalDateTime.now());
        return companyRepository.save(existingCompany);
    }

    public void deleteCompany(String code) {
        UserContext context = UserContextHolder.get();
        Role role = context.getRole();

        Company company = companyRepository.findCompanyByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));

        if (!(role == Role.ROLE_PRINCIPAL_ADMIN || Objects.equals(context.getUsername(), company.getUsername()))) {
            throw new IllegalArgumentException("You don't have access to this company");
        }

        dockerTenantService.stopOrganizationContainer(code);
        dockerTenantService.deleteOrganizationContainer(code);
        companyRepository.delete(company);
    }

}

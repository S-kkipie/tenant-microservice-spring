package unsa.sistemas.tenantservice.Controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import unsa.sistemas.tenantservice.Config.UserContext;
import unsa.sistemas.tenantservice.Config.UserContextHolder;
import unsa.sistemas.tenantservice.Models.Company;
import unsa.sistemas.tenantservice.Models.Role;
import unsa.sistemas.tenantservice.Services.CompanyService;
import unsa.sistemas.tenantservice.Utils.ResponseHandler;
import unsa.sistemas.tenantservice.Utils.ResponseWrapper;
import unsa.sistemas.tenantservice.DTOs.CompanyRequest;

import java.util.List;

@RestController
@RequestMapping("/company")
@RequiredArgsConstructor
public class CompanyController {
    private final CompanyService companyService;

    @GetMapping
    public ResponseEntity<ResponseWrapper<List<Company>>> getAllCompanies() {
        UserContext context = UserContextHolder.get();
        Role role = context.getRole();

        if (role != Role.ROLE_PRINCIPAL_ADMIN) {
            return ResponseHandler.generateResponse("Unauthorized access", HttpStatus.FORBIDDEN, null);
        }

        return ResponseHandler.generateResponse("All companies found", HttpStatus.OK, companyService.getAllCompanies());
    }

    @GetMapping("/deploy")
    public ResponseEntity<ResponseWrapper<Object>> deployAllCompanies() {
        try {
            UserContext context = UserContextHolder.get();
            if (context.getRole() != Role.ROLE_PRINCIPAL_ADMIN) {
                return ResponseHandler.generateResponse("Unauthorized access", HttpStatus.FORBIDDEN, null);
            }
            companyService.deployAllCompanies();
            return ResponseHandler.generateResponse("All companies deployed successfully", HttpStatus.OK, null);
        } catch (Exception e) {
            return ResponseHandler.generateResponse("Failed to deploy companies", HttpStatus.INTERNAL_SERVER_ERROR, null);
        }
    }


    @GetMapping("/{code}")
    public ResponseEntity<ResponseWrapper<Company>> getCompany(@PathVariable String code) {
        try {
            return ResponseHandler.generateResponse("Company found", HttpStatus.OK, companyService.findCompanyByCode(code));
        } catch (IllegalArgumentException e) {
            return ResponseHandler.generateResponse("Unauthorized access", HttpStatus.FORBIDDEN, null);
        } catch (Exception e) {
            return ResponseHandler.generateResponse("Failed to get company", HttpStatus.NOT_FOUND, null);
        }
    }

    @PostMapping
    public ResponseEntity<ResponseWrapper<Object>> createCompany(@Valid @RequestBody CompanyRequest request) {
        try {
            UserContext context = UserContextHolder.get();
            if (context.getRole() != Role.ROLE_PRINCIPAL_USER && context.getRole() != Role.ROLE_PRINCIPAL_ADMIN) {
                return ResponseHandler.generateResponse("Unauthorized access", HttpStatus.FORBIDDEN, null);
            }
            return ResponseHandler.generateResponse("Company created successfully", HttpStatus.CREATED, companyService.createCompany(request, context.getUsername()));
        } catch (DuplicateKeyException e) {
            return ResponseHandler.generateResponse("Failed to create a company", HttpStatus.BAD_REQUEST, "Code is already used");
        } catch (Exception e) {
            return ResponseHandler.generateResponse("Failed to create a company", HttpStatus.BAD_REQUEST, "An error occurred while creating a company");
        }
    }

    @PutMapping("/{code}")
    public ResponseEntity<ResponseWrapper<Company>> updateCompany(@PathVariable String code, @RequestBody CompanyRequest updatedCompany) {
        try {
            return ResponseHandler.generateResponse("Company updated successfully", HttpStatus.OK, companyService.updateCompany(code, updatedCompany));
        } catch (IllegalArgumentException e) {
            return ResponseHandler.generateResponse("Unauthorized access", HttpStatus.FORBIDDEN, null);
        } catch (Exception e) {
            return ResponseHandler.generateResponse("Failed to get company", HttpStatus.NOT_FOUND, null);
        }
    }

    @DeleteMapping("/{code}")
    public ResponseEntity<ResponseWrapper<Object>> deleteCompany(@PathVariable String code) {
        try {
            companyService.deleteCompany(code);
            return ResponseHandler.generateResponse("Company deleted successfully", HttpStatus.OK, null);
        } catch (IllegalArgumentException e) {
            return ResponseHandler.generateResponse("Unauthorized access", HttpStatus.FORBIDDEN, null);
        } catch (Exception e) {
            return ResponseHandler.generateResponse("Failed to get company", HttpStatus.NOT_FOUND, null);
        }
    }
}

package unsa.sistemas.tenantservice.Controllers;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import unsa.sistemas.tenantservice.DTOs.CreateCompanyRequest;
import unsa.sistemas.tenantservice.DTOs.CreateTypeRequest;
import unsa.sistemas.tenantservice.Models.Type;
import unsa.sistemas.tenantservice.Services.CompanyService;
import unsa.sistemas.tenantservice.Services.TypeService;
import unsa.sistemas.tenantservice.Utils.ResponseHandler;
import unsa.sistemas.tenantservice.Utils.ResponseWrapper;

import java.util.List;

@Slf4j
@AllArgsConstructor
@RestController
public class TenantController {
    private final CompanyService companyService;
    private final TypeService typeService;

    @PostMapping("/company")
    public ResponseEntity<ResponseWrapper<Object>> createCompany(@Validated @RequestBody CreateCompanyRequest request, @RequestHeader("User-Id") String ownerId) {
        try {
            return ResponseHandler.generateResponse("Company created successfully", HttpStatus.CREATED, companyService.createCompany(request, ownerId));
        } catch (DuplicateKeyException e) {
            log.error(e.getMessage());
            return ResponseHandler.generateResponse("Failed to create a company", HttpStatus.BAD_REQUEST, "Code is already used");
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseHandler.generateResponse("Failed to create a company", HttpStatus.BAD_REQUEST, "An error occurred while creating a company");
        }
    }

    @GetMapping("/company/{code}")
    public ResponseEntity<ResponseWrapper<Object>> getCompany(@PathVariable String code) {
        try {
            return ResponseHandler.generateResponse("Company found", HttpStatus.OK, companyService.findCompanyByCode(code));
        } catch (Exception e) {
            return ResponseHandler.generateResponse("Failed to get company", HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/type")
    public ResponseEntity<ResponseWrapper<List<Type>>> getAllTypes() {
        return ResponseHandler.generateResponse("Retrieving all types found", HttpStatus.OK, typeService.getAllTypes());
    }


    @PostMapping("/type")
    public ResponseEntity<ResponseWrapper<Object>> createType(@Validated @RequestBody CreateTypeRequest request) {
        try {
            return ResponseHandler.generateResponse("Type created successfully", HttpStatus.CREATED, typeService.createType(request));
        } catch (Exception e) {
            return ResponseHandler.generateResponse("Failed to create a type", HttpStatus.BAD_REQUEST, "An error occurred while creating a type");
        }
    }
}

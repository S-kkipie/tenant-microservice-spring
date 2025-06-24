package unsa.sistemas.tenantservice.Controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import unsa.sistemas.tenantservice.Config.UserContext;
import unsa.sistemas.tenantservice.Config.UserContextHolder;
import unsa.sistemas.tenantservice.DTOs.TypeRequest;
import unsa.sistemas.tenantservice.Models.Role;
import unsa.sistemas.tenantservice.Models.Type;
import unsa.sistemas.tenantservice.Services.TypeService;
import unsa.sistemas.tenantservice.Utils.ResponseHandler;
import unsa.sistemas.tenantservice.Utils.ResponseWrapper;

import java.util.List;

@RestController
@RequestMapping("/type")
@RequiredArgsConstructor
public class TypeController {
    private final TypeService typeService;

    @PostMapping
    public ResponseEntity<ResponseWrapper<Type>> createType(@RequestBody TypeRequest typeRequest) {
        try {
            Type type = typeService.createType(typeRequest);
            return ResponseHandler.generateResponse("Type created successfully", HttpStatus.CREATED, type);
        } catch (Exception e) {
            return ResponseHandler.generateResponse("Failed to create type", HttpStatus.BAD_REQUEST, null);
        }
    }

    @GetMapping
    public ResponseEntity<ResponseWrapper<List<Type>>> getAllTypes() {
        return ResponseHandler.generateResponse("All types found", HttpStatus.OK, typeService.getAllTypes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseWrapper<Type>> getTypeById(@PathVariable Long id) {
        try {
            Type type = typeService.getTypeById(id);
            return ResponseHandler.generateResponse("Type found", HttpStatus.OK, type);
        } catch (Exception e) {
            return ResponseHandler.generateResponse("Type not found", HttpStatus.NOT_FOUND, null);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseWrapper<Object>> deleteType(@PathVariable Long id) {
        try {
            UserContext context = UserContextHolder.get();
            Role role = context.getRole();

            if (role != Role.ROLE_PRINCIPAL_ADMIN) {
                return ResponseHandler.generateResponse("Unauthorized access", HttpStatus.FORBIDDEN, null);
            }

            typeService.deleteType(id);
            return ResponseHandler.generateResponse("Type deleted successfully", HttpStatus.OK, null);
        } catch (Exception e) {
            return ResponseHandler.generateResponse("Failed to delete type", HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
}

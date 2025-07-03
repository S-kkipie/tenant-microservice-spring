package unsa.sistemas.tenantservice.Repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import unsa.sistemas.tenantservice.Models.Company;

import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, String> {
    Optional<Company> findCompanyByCode(String code);
    Page<Company> findByCodeContainingIgnoreCaseOrNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String code, String name, String description, Pageable page);
}

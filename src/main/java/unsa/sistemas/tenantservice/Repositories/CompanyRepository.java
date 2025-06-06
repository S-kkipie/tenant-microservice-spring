package unsa.sistemas.tenantservice.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import unsa.sistemas.tenantservice.Models.Company;

@Repository
public interface CompanyRepository extends JpaRepository<Company, String> {
}

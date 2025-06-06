package unsa.sistemas.tenantservice.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import unsa.sistemas.tenantservice.Models.Type;

public interface TypeRepository extends JpaRepository<Type, Long> {
}

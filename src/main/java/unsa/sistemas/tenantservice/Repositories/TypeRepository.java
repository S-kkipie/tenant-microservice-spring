package unsa.sistemas.tenantservice.Repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import unsa.sistemas.tenantservice.Models.Type;

public interface TypeRepository extends JpaRepository<Type, Long> {
    Page<Type> findByNameContainingIgnoreCase(String name, Pageable page);
}

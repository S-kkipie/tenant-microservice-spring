package unsa.sistemas.tenantservice.Config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import unsa.sistemas.tenantservice.Models.Role;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserContext {
    private String username;
    private Role role;
}

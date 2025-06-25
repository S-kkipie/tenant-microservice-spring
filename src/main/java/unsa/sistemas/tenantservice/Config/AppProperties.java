package unsa.sistemas.tenantservice.Config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
public class AppProperties {
    @Value("${app.properties.page-size}")
    private int pageSize;
}

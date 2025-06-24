package unsa.sistemas.tenantservice.Services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import unsa.sistemas.tenantservice.Config.TenantProperties;
import unsa.sistemas.tenantservice.Models.Company;
import unsa.sistemas.tenantservice.Repositories.CompanyRepository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@AllArgsConstructor
public class DockerTenantService {
    private final CompanyRepository companyRepository;
    private final TenantProperties tenantProperties;

    public Company createContainer(Company company, String securePassword) {
        try {
            int port = getNextPortAvailable();
            return createPostgresContainer(company, port, securePassword);

        } catch (Exception e) {
            log.error("An error occurred trying to create the container for the org  {}: {}", company.getCode(), e.getMessage());
            throw new RuntimeException("Failed to create container's org  " + company.getCode(), e);
        }
    }

    private Company createPostgresContainer(Company company, int port, String password) throws IOException, InterruptedException {
        String containerName = getContainerName(company.getCode());
        String dbName = company.getCode() + "_identity_db";


        if (containerExists(containerName)) {
            log.info("Container {} already exists, skipping...", containerName);
            throw new RuntimeException("Container " + containerName + " already exists");
        }

        log.info("Creating PostgresSQL container for org: {}, port: {}, dbName: {}, username: {}", company.getCode(), port, dbName, company.getUsername());

        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(
                "docker", "run", "-d",
                "--name", containerName,
                "-p", port + ":5432",
                "-e", "POSTGRES_USER=" + company.getUsername(),
                "-e", "POSTGRES_PASSWORD=" + password,
                "-e", "POSTGRES_DB=" + dbName,
                "-v", "tenant-" + company.getCode() + ":/var/lib/postgresql/data",
                "postgres:16"
        );

        company.setDataBasePassword(password);

        Process process = processBuilder.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

        String line;
        while ((line = reader.readLine()) != null) {
            log.info("Docker output: {}", line);
        }

        while ((line = errorReader.readLine()) != null) {
            log.error("Docker error: {}", line);
        }

        int exitCode = process.waitFor();

        if (exitCode == 0) {
            log.info("PostgresSQL container created: {} in port {} with database default: {}", containerName, port, dbName);
            waitContainerBeAlready(containerName, company.getUsername());
            createExtraDatabases(company.getCode(), containerName, company.getUsername());
            return saveContainerInfo(company, containerName, port, dbName);
        } else {
            log.error("An error occurred trying to create the container {}, output code: {}", containerName, exitCode);
            throw new RuntimeException("PostgresSQL container creation failed");
        }
    }

    private void waitContainerBeAlready(String containerName, String tenantId) {
        log.info("Waiting for the container {} be already...", containerName);

        int intents = 0;

        while (intents < 30) {
            try {
                ProcessBuilder pb = new ProcessBuilder("docker", "exec", containerName,
                        "pg_isready", "-U", tenantId);
                Process process = pb.start();
                int exitCode = process.waitFor();

                if (exitCode == 0) {
                    log.info("Container {} is already", containerName);
                    return;
                }

                Thread.sleep(1000);
                intents++;

            } catch (Exception e) {
                log.warn("An error occurred verifying the status of the container: {}", e.getMessage());
                intents++;
            }
        }

        log.warn("Timeout waiting for the container: {}", containerName);
    }

    private void createExtraDatabases(String orgCode, String containerName, String tenantId) {
        List<String> extraDatabases = getExtraDatabasesForOrganization(orgCode);
        for (String dataBaseName : extraDatabases) {
            createDataBase(containerName, dataBaseName, tenantId);
        }
    }

    private List<String> getExtraDatabasesForOrganization(String orgCode) {
        return List.of(
                orgCode + "_inventory_db"
        );
    }

    private void createDataBase(String containerName, String dataBaseName, String tenantId) {
        try {
            log.info("Creating database: {} in container: {}", dataBaseName, containerName);

            ProcessBuilder pb = new ProcessBuilder("docker", "exec", containerName,
                    "psql", "-U", tenantId, "-d", "postgres", "-c", "CREATE DATABASE \"" + dataBaseName + "\";");

            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                log.debug("PostgresSQL output: {}", line);
            }

            while ((line = errorReader.readLine()) != null) {
                if (!line.contains("already exists")) {
                    log.warn("PostgresSQL warning: {}", line);
                }
            }

            int exitCode = process.waitFor();

            if (exitCode == 0) {
                log.info("Data Base {} successfully created", dataBaseName);
            } else {
                log.warn("Maybe an error occurred trying to create the database {}, code: {}", dataBaseName, exitCode);
            }

        } catch (Exception e) {
            log.error("Error creating database {}: {}", dataBaseName, e.getMessage());
        }
    }


    private boolean containerExists(String containerName) {
        try {
            ProcessBuilder pb = new ProcessBuilder("docker", "ps", "-a", "--format", "{{.Names}}");
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                log.info("PostgresSQL output: {}", line);
                if (line.trim().equals(containerName)) {
                    return true;
                }
            }
            process.waitFor();
            return false;

        } catch (Exception e) {
            log.error("An error occurred trying to verify if container exists: {}", e.getMessage());
            return false;
        }
    }

    private int getNextPortAvailable() {
        try {
            ProcessBuilder pb = new ProcessBuilder("docker", "ps", "--format", "{{.Ports}}");
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            Set<Integer> usedPorts = new HashSet<>();

            String line;
            while ((line = reader.readLine()) != null) {
                Pattern pattern = Pattern.compile("0\\.0\\.0\\.0:(\\d+)->5432/tcp");
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    usedPorts.add(Integer.parseInt(matcher.group(1)));
                }
            }

            process.waitFor();

            for (int i = 0; i < 1000; i++) {
                int puerto = tenantProperties.getBasePort() + i;
                if (!usedPorts.contains(puerto)) {
                    return puerto;
                }
            }

            throw new RuntimeException("There isn't enough port available");

        } catch (Exception e) {
            log.error("An error occurred trying to find an available port: {}", e.getMessage());
            return tenantProperties.getBasePort() + (int) (System.currentTimeMillis() % 1000);
        }
    }

    private Company saveContainerInfo(Company company, String containerName, int port, String dbName) {
        log.info("Saving info - Org: {}, Container: {}, Puerto: {}, DB: {}",
                company.getCode(), containerName, port, dbName);
        company.setDataBasePort(port);
        company.setContainerName(containerName);
        try {
            return companyRepository.save(company);
        } catch (Exception e) {
            throw new RuntimeException("An error occurred trying to save the container info");
        }
    }

    public void stopOrganizationContainer(String orgCode) {
        try {
            String containerName = getContainerName(orgCode);

            ProcessBuilder pb = new ProcessBuilder("docker", "stop", containerName);
            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                log.info("Container {} stopped", containerName);
            }

        } catch (Exception e) {
            log.error("An error occurred trying to stop the container for org {}: {}", orgCode, e.getMessage());
        }
    }

    public void deleteOrganizationContainer(String orgCode) {
        try {
            String containerName = getContainerName(orgCode);

            ProcessBuilder stopPb = new ProcessBuilder("docker", "stop", containerName);
            stopPb.start().waitFor();

            ProcessBuilder volumermPb = new ProcessBuilder("docker", "volume" , "rm", containerName);
            volumermPb.start().waitFor();

            ProcessBuilder rmPb = new ProcessBuilder("docker", "rm", containerName);
            Process process = rmPb.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                log.info("Container {} deleted", containerName);
            }

        } catch (Exception e) {
            log.error("An error occurred trying to delete container for org {}: {}", orgCode, e.getMessage());
        }
    }

//    public List<ContainerInfo> listarContenedoresActivos() {
//        List<ContainerInfo> contenedores = new ArrayList<>();
//
//        try {
//            ProcessBuilder pb = new ProcessBuilder("docker", "ps",
//                    "--filter", "name=postgres-tenant-",
//                    "--format", "{{.Names}}:{{.Ports}}");
//
//            Process process = pb.start();
//            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
//
//            String line;
//            while ((line = reader.readLine()) != null) {
//                String[] parts = line.split(":");
//                if (parts.length >= 2) {
//                    String name = parts[0];
//                    String ports = parts[1];
//
//                    // Extraer código de organización del nombre
//                    String codigoOrg = name.replace("postgres-tenant-", "");
//
//                    // Extraer puerto
//                    Pattern pattern = Pattern.compile("0\\.0\\.0\\.0:(\\d+)->5432/tcp");
//                    Matcher matcher = pattern.matcher(ports);
//                    if (matcher.find()) {
//                        int puerto = Integer.parseInt(matcher.group(1));
//                        contenedores.add(new ContainerInfo(codigoOrg, name, puerto));
//                    }
//                }
//            }
//
//            process.waitFor();
//
//        } catch (Exception e) {
//            log.error("Error listando contenedores: {}", e.getMessage());
//        }
//
//        return contenedores;
//    }
//
//    // Clase interna para info de contenedores
//    public static class ContainerInfo {
//        private String codigoOrganizacion;
//        private String nombreContenedor;
//        private int puerto;
//
//        public ContainerInfo(String codigoOrganizacion, String nombreContenedor, int puerto) {
//            this.codigoOrganizacion = codigoOrganizacion;
//            this.nombreContenedor = nombreContenedor;
//            this.puerto = puerto;
//        }
//
//        // Getters y setters
//        public String getCodigoOrganizacion() { return codigoOrganizacion; }
//        public void setCodigoOrganizacion(String codigoOrganizacion) { this.codigoOrganizacion = codigoOrganizacion; }
//
//        public String getNombreContenedor() { return nombreContenedor; }
//        public void setNombreContenedor(String nombreContenedor) { this.nombreContenedor = nombreContenedor; }
//
//        public int getPuerto() { return puerto; }
//        public void setPuerto(int puerto) { this.puerto = puerto; }
//    }

    private String getContainerName(String orgCode) {
        return "tenant-" + orgCode;
    }
}
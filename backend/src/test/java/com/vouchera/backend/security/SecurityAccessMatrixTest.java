package com.vouchera.backend.security;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.vouchera.backend.entity.Company;
import com.vouchera.backend.entity.User;
import com.vouchera.backend.enums.CompanyStatus;
import com.vouchera.backend.enums.Role;
import com.vouchera.backend.repository.CampaignRepository;
import com.vouchera.backend.repository.CompanyRepository;
import com.vouchera.backend.repository.RedemptionRepository;
import com.vouchera.backend.repository.UserRepository;
import com.vouchera.backend.repository.VoucherTypeRepository;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = "cors.allowed-origins=http://localhost:5173")
class SecurityAccessMatrixTest {

    private MockMvc mockMvc;

    private UUID activeCompanyId;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private VoucherTypeRepository voucherTypeRepository;

    @Autowired
    private RedemptionRepository redemptionRepository;

    @BeforeEach
    void setUp() {
        redemptionRepository.deleteAll();
        voucherTypeRepository.deleteAll();
        campaignRepository.deleteAll();
        userRepository.deleteAll();
        companyRepository.deleteAll();

        Company company = new Company("Acme Security Test");
        company.setCompanyStatus(CompanyStatus.ACTIVE);
        company = companyRepository.save(company);
        activeCompanyId = company.getId();

        userRepository.save(new User("admin@example.com", "password123", Role.ADMIN, null));
        userRepository.save(new User("customer@example.com", "password123", Role.CUSTOMER, null));
        userRepository.save(new User("marketing@example.com", "password123", Role.MARKETING, company));

        mockMvc = MockMvcBuilders.webAppContextSetup(context)
            .apply(SecurityMockMvcConfigurers.springSecurity())
            .build();
    }

    @Test
    void anonymousCanReadPublicCompanies() throws Exception {
        mockMvc.perform(get("/api/companies"))
            .andExpect(status().isOk());
    }

    @Test
    void anonymousCannotReadUsers() throws Exception {
        mockMvc.perform(get("/api/users"))
            .andExpect(status().isForbidden());
    }

    @Test
    void customerCannotReadUsers() throws Exception {
        mockMvc.perform(get("/api/users")
                .with(user("customer@example.com").roles("CUSTOMER")))
            .andExpect(status().isForbidden());
    }

    @Test
    void adminCanReadUsers() throws Exception {
        mockMvc.perform(get("/api/users")
                .with(user("admin@example.com").roles("ADMIN")))
            .andExpect(status().isOk());
    }

    @Test
    void customerCannotCreateCampaign() throws Exception {
        mockMvc.perform(post("/api/campaigns")
                .with(user("customer@example.com").roles("CUSTOMER"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(validCampaignBody(activeCompanyId)))
            .andExpect(status().isForbidden());
    }

    @Test
    void marketingCanCreateCampaign() throws Exception {
        mockMvc.perform(post("/api/campaigns")
                .with(user("marketing@example.com").roles("MARKETING"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(validCampaignBody(activeCompanyId)))
            .andExpect(status().isOk());
    }

    private String validCampaignBody(UUID companyId) {
        return "{" +
            "\"companyId\":\"" + companyId + "\"," +
            "\"name\":\"Summer Blast\"," +
            "\"description\":\"Discount campaign\"," +
            "\"startTime\":\"2030-01-01T10:00:00\"," +
            "\"endTime\":\"2030-01-10T10:00:00\"" +
            "}";
    }
}

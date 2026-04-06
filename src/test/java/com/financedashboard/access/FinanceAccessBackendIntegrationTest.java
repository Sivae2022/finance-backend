package com.financedashboard.access;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class FinanceAccessBackendIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void viewerCanSeeDashboardButNotRecordListing() throws Exception {
        mockMvc.perform(get("/api/dashboard/overview")
                        .with(httpBasic("viewer.meera", "Viewer@123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIncome").value(8600.00))
                .andExpect(jsonPath("$.totalExpenses").value(2736.55))
                .andExpect(jsonPath("$.netBalance").value(5863.45));

        mockMvc.perform(get("/api/records")
                        .with(httpBasic("viewer.meera", "Viewer@123")))
                .andExpect(status().isForbidden());
    }

    @Test
    void analystCanReadRecordsButCannotCreateThem() throws Exception {
        mockMvc.perform(get("/api/records")
                        .with(httpBasic("analyst.rahul", "Analyst@123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(8));

        mockMvc.perform(post("/api/records")
                        .with(httpBasic("analyst.rahul", "Analyst@123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 510.00,
                                  "type": "EXPENSE",
                                  "category": "Hardware",
                                  "recordDate": "2026-04-01",
                                  "description": "Replacement keyboard"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanCreateRecordAndManageUsers() throws Exception {
        mockMvc.perform(post("/api/records")
                        .with(httpBasic("admin.priya", "Admin@123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 510.00,
                                  "type": "EXPENSE",
                                  "category": "Hardware",
                                  "recordDate": "2026-04-01",
                                  "description": "Replacement keyboard"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.category").value("Hardware"))
                .andExpect(jsonPath("$.amount").value(510.00));

        mockMvc.perform(post("/api/users")
                        .with(httpBasic("admin.priya", "Admin@123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "admin.sonal",
                                  "fullName": "Sonal Kapur",
                                  "password": "Sonal@123",
                                  "role": "ADMIN",
                                  "status": "ACTIVE"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("admin.sonal"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void recordFilteringWorksForAnalyst() throws Exception {
        mockMvc.perform(get("/api/records")
                        .with(httpBasic("analyst.rahul", "Analyst@123"))
                        .param("type", "EXPENSE")
                        .param("category", "Rent")
                        .param("minAmount", "1000")
                        .param("maxAmount", "2000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].category").value("Rent"))
                .andExpect(jsonPath("$[0].amount").value(1800.00));
    }

    @Test
    void trendsEndpointReturnsRequestedMonthWindow() throws Exception {
        mockMvc.perform(get("/api/dashboard/trends")
                        .with(httpBasic("viewer.meera", "Viewer@123"))
                        .param("months", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4));
    }

    @Test
    void inactiveUserCannotAuthenticate() throws Exception {
        mockMvc.perform(get("/api/dashboard/overview")
                        .with(httpBasic("archived.aisha", "Inactive@123")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void invalidRecordPayloadReturnsValidationError() throws Exception {
        mockMvc.perform(post("/api/records")
                        .with(httpBasic("admin.priya", "Admin@123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 0,
                                  "type": "INCOME",
                                  "category": "",
                                  "recordDate": "2026-04-01"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.amount").exists())
                .andExpect(jsonPath("$.validationErrors.category").exists());
    }

    @Test
    void adminCanUpdateExistingUserStatus() throws Exception {
        mockMvc.perform(put("/api/users/2")
                        .with(httpBasic("admin.priya", "Admin@123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fullName": "Rahul Verma",
                                  "status": "INACTIVE"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INACTIVE"));
    }
}

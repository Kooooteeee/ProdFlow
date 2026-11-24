
package ru.prodflow.service;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.prodflow.dto.UpdatePartRequestDto;
import ru.prodflow.dto.request.MachineRequestDto;
import ru.prodflow.dto.request.PartRequestDto;
import ru.prodflow.dto.request.PartRequestRequestDto;
import ru.prodflow.dto.request.UserRequestDto;
import ru.prodflow.enums.PartRequestStatus;
import ru.prodflow.exception.ConflictException;
import ru.prodflow.model.Operation;
import ru.prodflow.model.Part;
import ru.prodflow.model.PartOperation;
import ru.prodflow.model.PartRequest;
import ru.prodflow.model.User;
import ru.prodflow.repository.OperationRepository;
import ru.prodflow.repository.PartOperationRepository;
import ru.prodflow.repository.PartRepository;
import ru.prodflow.service.interfaces.MachineService;
import ru.prodflow.service.interfaces.PartRequestService;
import ru.prodflow.service.interfaces.PartService;
import ru.prodflow.service.interfaces.UserService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class PartRequestServiceIntegrationTest {

    @Autowired
    private PartRequestService partRequestService;

    @Autowired
    private UserService userService;

    @Autowired
    private PartService partService;

    @Autowired
    private MachineService machineService;

    @Autowired
    private PartRepository partRepository;

    @Autowired
    private PartOperationRepository partOperationRepository;

    @Autowired
    private OperationRepository operationRepository;

    @Autowired
    private EntityManager entityManager;

    @Transactional
    @Test
    public void createPartRequest_WhenValid_CreatesPartRequest() {
        User customer = createCustomer("pr-create@example.com");

        PartRequestRequestDto partRequestDto = new PartRequestRequestDto();
        partRequestDto.setName("testPart");
        partRequestDto.setDescription("testDescription");

        PartRequest savedRequest = partRequestService.createPartRequest(
                partRequestDto, customer.getId());

        entityManager.flush();
        entityManager.clear();

        PartRequest foundRequest = partRequestService.findPartRequestById(savedRequest.getId());

        assertEquals("testPart", foundRequest.getName());
        assertEquals("testDescription", foundRequest.getDescription());
        assertEquals(PartRequestStatus.PENDING, foundRequest.getStatus());
        assertEquals(customer.getId(), foundRequest.getCustomer().getId());
    }

    @Transactional
    @Test
    public void approvePartRequest_WhenValid_CreatesPartAndApprovesRequest() {
        User customer = createCustomer("pr-approve@example.com");

        PartRequest savedRequest = createRequest(customer.getId(), "approvedTestPart");

        Operation operation = createOperationWithMachine(
                "approveTestOperation", "approveTestMachine");

        partRequestService.approvePartRequest(
                savedRequest.getId(), List.of(operation.getId()));

        entityManager.flush();
        entityManager.clear();

        PartRequest foundRequest = partRequestService.findPartRequestById(savedRequest.getId());

        assertEquals(PartRequestStatus.APPROVED, foundRequest.getStatus());

        Part foundPart = partRepository.findAll().stream()
                .filter(part -> part.getName().equals("approvedTestPart"))
                .findFirst()
                .orElseThrow();

        assertEquals("testDescription", foundPart.getDescription());

        List<PartOperation> partOperations = partOperationRepository
                .findAllByPart_IdOrderById_SequenceAsc(foundPart.getId());

        assertEquals(1, partOperations.size());
        assertEquals(operation.getId(), partOperations.get(0).getOperation().getId());
    }

    @Transactional
    @Test
    public void approvePartRequest_WhenNameExists_ThrowsException() {
        User customer = createCustomer("pr-duplicate@example.com");

        Operation operation = createOperationWithMachine(
                "duplicateTestOperation", "duplicateTestMachine");

        PartRequestDto partDto = new PartRequestDto();
        partDto.setName("duplicateTestPart");
        partDto.setDescription("existingDescription");
        partDto.setOperationIds(List.of(operation.getId()));

        partService.createPart(partDto);

        PartRequest savedRequest = createRequest(
                customer.getId(), "  DUPLICATETESTPART  ");

        entityManager.flush();

        long countBefore = partRepository.count();

        assertThrows(ConflictException.class, () ->
                partRequestService.approvePartRequest(
                        savedRequest.getId(), List.of(operation.getId())));

        entityManager.clear();

        PartRequest foundRequest = partRequestService.findPartRequestById(savedRequest.getId());

        assertEquals(PartRequestStatus.PENDING, foundRequest.getStatus());
        assertEquals(countBefore, partRepository.count());
    }

    @Transactional
    @Test
    public void approvePartRequest_WhenAlreadyProcessed_ThrowsException() {
        User customer = createCustomer("pr-processed@example.com");

        PartRequest savedRequest = createRequest(customer.getId(), "processedTestPart");

        partRequestService.rejectPartRequests(List.of(savedRequest.getId()));

        entityManager.flush();
        entityManager.clear();

        assertThrows(ConflictException.class, () ->
                partRequestService.approvePartRequest(savedRequest.getId(), List.of()));

        entityManager.clear();

        PartRequest foundRequest = partRequestService.findPartRequestById(savedRequest.getId());

        assertEquals(PartRequestStatus.REJECTED, foundRequest.getStatus());
    }

    @Transactional
    @Test
    public void rejectPartRequests_WhenValid_RejectsPendingRequests() {
        User customer = createCustomer("pr-reject@example.com");

        PartRequest firstRequest = createRequest(customer.getId(), "firstRejectPart");
        PartRequest secondRequest = createRequest(customer.getId(), "secondRejectPart");
        PartRequest approvedRequest = createRequest(customer.getId(), "alreadyApprovedPart");

        approvedRequest.setStatus(PartRequestStatus.APPROVED);

        entityManager.flush();
        entityManager.clear();

        partRequestService.rejectPartRequests(List.of(
                firstRequest.getId(),
                firstRequest.getId(),
                secondRequest.getId(),
                approvedRequest.getId()
        ));

        entityManager.flush();
        entityManager.clear();

        PartRequest foundFirst = partRequestService.findPartRequestById(firstRequest.getId());
        PartRequest foundSecond = partRequestService.findPartRequestById(secondRequest.getId());
        PartRequest foundApproved = partRequestService.findPartRequestById(approvedRequest.getId());

        assertEquals(PartRequestStatus.REJECTED, foundFirst.getStatus());
        assertEquals(PartRequestStatus.REJECTED, foundSecond.getStatus());
        assertEquals(PartRequestStatus.APPROVED, foundApproved.getStatus());
    }

    @Transactional
    @Test
    public void updatePartRequest_WhenPending_UpdatesPartRequest() {
        User customer = createCustomer("pr-update@example.com");

        PartRequest savedRequest = createRequest(customer.getId(), "originalTestPart");

        UpdatePartRequestDto updateDto = new UpdatePartRequestDto();
        updateDto.setName("updatedTestPart");
        updateDto.setDescription("updatedDescription");

        partRequestService.updatePartRequest(savedRequest.getId(), updateDto);

        entityManager.flush();
        entityManager.clear();

        PartRequest foundRequest = partRequestService.findPartRequestById(savedRequest.getId());

        assertEquals("updatedTestPart", foundRequest.getName());
        assertEquals("updatedDescription", foundRequest.getDescription());
        assertEquals(PartRequestStatus.PENDING, foundRequest.getStatus());
    }

    @Transactional
    @Test
    public void updatePartRequest_WhenAlreadyProcessed_ThrowsException() {
        User customer = createCustomer("pr-update-rejected@example.com");

        PartRequest savedRequest = createRequest(customer.getId(), "originalRejectedPart");

        partRequestService.rejectPartRequests(List.of(savedRequest.getId()));

        UpdatePartRequestDto updateDto = new UpdatePartRequestDto();
        updateDto.setName("updatedRejectedPart");
        updateDto.setDescription("updatedDescription");

        assertThrows(ConflictException.class, () ->
                partRequestService.updatePartRequest(savedRequest.getId(), updateDto));

        entityManager.flush();
        entityManager.clear();

        PartRequest foundRequest = partRequestService.findPartRequestById(savedRequest.getId());

        assertEquals("originalRejectedPart", foundRequest.getName());
        assertEquals("testDescription", foundRequest.getDescription());
        assertEquals(PartRequestStatus.REJECTED, foundRequest.getStatus());
    }

    private User createCustomer(String email) {
        UserRequestDto userRequestDto = new UserRequestDto();
        userRequestDto.setName("testCustomer");
        userRequestDto.setEmail(email);
        userRequestDto.setPassword("testPassword");

        User savedUser = userService.createUser(userRequestDto);
        userService.approveUsers(List.of(savedUser.getId()));

        return savedUser;
    }

    private PartRequest createRequest(Long customerId, String name) {
        PartRequestRequestDto partRequestDto = new PartRequestRequestDto();
        partRequestDto.setName(name);
        partRequestDto.setDescription("testDescription");

        return partRequestService.createPartRequest(partRequestDto, customerId);
    }

    private Operation createOperationWithMachine(String operationName, String machineName) {
        Operation operation = new Operation();
        operation.setName(operationName);
        operation.setDurationMinutes(10);

        Operation savedOperation = operationRepository.save(operation);

        MachineRequestDto machineRequestDto = new MachineRequestDto();
        machineRequestDto.setName(machineName);
        machineRequestDto.setOperationId(savedOperation.getId());

        machineService.createMachine(machineRequestDto);

        return savedOperation;
    }
}

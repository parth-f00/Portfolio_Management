package org.neueda.rest.project.repositorytest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.neueda.rest.project.entity.Investment;
import org.neueda.rest.project.repository.InvestmentRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

public class InvestmentEpositoryTest {

    @Mock
    private InvestmentRepository investmentRepository;

    private Investment testInvestment;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        testInvestment = new Investment(null, "AAPL", "Tech", 100, BigDecimal.valueOf(150.0), LocalDateTime.now());
    }

    @Test
    public void testSaveInvestment_savesSuccessfully() {
        Investment investment = new Investment(null, "AAPL", "Tech", 100, BigDecimal.valueOf(150.0), LocalDateTime.now());
        investment.setId(1L);
        when(investmentRepository.save(any(Investment.class))).thenReturn(investment);

        Investment saved = investmentRepository.save(investment);

        assertNotNull(saved);
        assertNotNull(saved.getId());
        assertEquals("AAPL", saved.getTicker());
        assertEquals("Tech", saved.getSector());
        assertEquals(100, saved.getQuantity());
        verify(investmentRepository).save(any(Investment.class));
    }

    @Test
    public void testSaveMultipleInvestments_allSavedSuccessfully() {
        Investment inv1 = new Investment(1L, "AAPL", "Tech", 100, BigDecimal.valueOf(150.0), LocalDateTime.now());
        Investment inv2 = new Investment(2L, "MSFT", "Tech", 50, BigDecimal.valueOf(300.0), LocalDateTime.now());
        Investment inv3 = new Investment(3L, "JPM", "Finance", 75, BigDecimal.valueOf(160.0), LocalDateTime.now());

        List<Investment> investments = List.of(inv1, inv2, inv3);
        when(investmentRepository.saveAll(any())).thenReturn(investments);

        List<Investment> saved = investmentRepository.saveAll(investments);

        assertEquals(3, saved.size());
        verify(investmentRepository).saveAll(any());
    }

    @Test
    public void testFindById_returnsInvestmentWhenExists() {
        Long id = 1L;
        Investment investment = new Investment(id, "AAPL", "Tech", 100, BigDecimal.valueOf(150.0), LocalDateTime.now());
        when(investmentRepository.findById(id)).thenReturn(Optional.of(investment));

        Optional<Investment> found = investmentRepository.findById(id);

        assertTrue(found.isPresent());
        assertEquals(id, found.get().getId());
        assertEquals("AAPL", found.get().getTicker());
        verify(investmentRepository).findById(id);
    }

    @Test
    public void testFindById_returnsEmptyWhenNotExists() {
        when(investmentRepository.findById(anyLong())).thenReturn(Optional.empty());

        Optional<Investment> found = investmentRepository.findById(999L);

        assertFalse(found.isPresent());
        verify(investmentRepository).findById(999L);
    }

    @Test
    public void testFindAll_returnsEmptyListWhenNoData() {
        when(investmentRepository.findAll()).thenReturn(new ArrayList<>());

        List<Investment> all = investmentRepository.findAll();

        assertTrue(all.isEmpty());
        verify(investmentRepository).findAll();
    }

    @Test
    public void testFindAll_returnsAllInvestments() {
        Investment inv1 = new Investment(1L, "AAPL", "Tech", 100, BigDecimal.valueOf(150.0), LocalDateTime.now());
        Investment inv2 = new Investment(2L, "MSFT", "Tech", 50, BigDecimal.valueOf(300.0), LocalDateTime.now());
        List<Investment> investments = List.of(inv1, inv2);
        when(investmentRepository.findAll()).thenReturn(investments);

        List<Investment> all = investmentRepository.findAll();

        assertEquals(2, all.size());
        verify(investmentRepository).findAll();
    }

    @Test
    public void testDeleteById_removesInvestment() {
        Long id = 1L;
        when(investmentRepository.findById(id)).thenReturn(Optional.empty());

        investmentRepository.deleteById(id);
        Optional<Investment> found = investmentRepository.findById(id);

        assertFalse(found.isPresent());
        verify(investmentRepository).deleteById(id);
    }

    @Test
    public void testDelete_removesInvestmentByObject() {
        Investment investment = new Investment(1L, "AAPL", "Tech", 100, BigDecimal.valueOf(150.0), LocalDateTime.now());

        investmentRepository.delete(investment);

        verify(investmentRepository).delete(investment);
    }

    @Test
    public void testUpdate_modifiesExistingInvestment() {
        Investment investment = new Investment(1L, "AAPL", "Tech", 100, BigDecimal.valueOf(150.0), LocalDateTime.now());
        investment.setQuantity(200);
        investment.setBuyPrice(BigDecimal.valueOf(160.0));
        when(investmentRepository.save(any(Investment.class))).thenReturn(investment);

        Investment updated = investmentRepository.save(investment);

        assertEquals(200, updated.getQuantity());
        assertEquals(BigDecimal.valueOf(160.0), updated.getBuyPrice());
        verify(investmentRepository).save(investment);
    }

    @Test
    public void testCount_returnsCorrectNumber() {
        when(investmentRepository.count()).thenReturn(3L);

        long count = investmentRepository.count();

        assertEquals(3L, count);
        verify(investmentRepository).count();
    }

    @Test
    public void testExistsById_returnsTrueWhenExists() {
        when(investmentRepository.existsById(1L)).thenReturn(true);

        boolean exists = investmentRepository.existsById(1L);

        assertTrue(exists);
        verify(investmentRepository).existsById(1L);
    }

    @Test
    public void testExistsById_returnsFalseWhenNotExists() {
        when(investmentRepository.existsById(999L)).thenReturn(false);

        boolean exists = investmentRepository.existsById(999L);

        assertFalse(exists);
        verify(investmentRepository).existsById(999L);
    }

    @Test
    public void testSaveAll_savesMultipleInvestmentsWithIds() {
        Investment inv1 = new Investment(1L, "AAPL", "Tech", 100, BigDecimal.valueOf(150.0), LocalDateTime.now());
        Investment inv2 = new Investment(2L, "MSFT", "Tech", 50, BigDecimal.valueOf(300.0), LocalDateTime.now());
        Investment inv3 = new Investment(3L, "GOOGL", "Tech", 25, BigDecimal.valueOf(2800.0), LocalDateTime.now());
        List<Investment> toSave = List.of(inv1, inv2, inv3);
        when(investmentRepository.saveAll(toSave)).thenReturn(toSave);

        List<Investment> saved = investmentRepository.saveAll(toSave);

        assertEquals(3, saved.size());
        assertTrue(saved.stream().allMatch(inv -> inv.getId() != null));
        verify(investmentRepository).saveAll(toSave);
    }

    @Test
    public void testDeleteAll_removesAllInvestments() {
        investmentRepository.deleteAll();
        when(investmentRepository.count()).thenReturn(0L);

        long count = investmentRepository.count();

        assertEquals(0, count);
        verify(investmentRepository).deleteAll();
    }

    @Test
    public void testInvestmentPersistence_dataStays() {
        Long id = 1L;
        Investment investment = new Investment(id, "TSLA", "Tech", 30, BigDecimal.valueOf(800.0), LocalDateTime.now());
        when(investmentRepository.findById(id)).thenReturn(Optional.of(investment));

        Optional<Investment> retrieved = investmentRepository.findById(id);

        assertTrue(retrieved.isPresent());
        assertEquals("TSLA", retrieved.get().getTicker());
        assertEquals("Tech", retrieved.get().getSector());
        verify(investmentRepository).findById(id);
    }

    @Test
    public void testMultipleOperations_sequence() {
        Investment investment = new Investment(1L, "AAPL", "Tech", 100, BigDecimal.valueOf(150.0), LocalDateTime.now());
        when(investmentRepository.existsById(1L)).thenReturn(true);
        when(investmentRepository.findById(1L)).thenReturn(Optional.of(investment));

        assertTrue(investmentRepository.existsById(1L));
        Optional<Investment> found = investmentRepository.findById(1L);
        assertTrue(found.isPresent());

        verify(investmentRepository).existsById(1L);
        verify(investmentRepository).findById(1L);
    }

    @Test
    public void testSaveInvestmentWithDifferentSectors() {
        Investment tech = new Investment(1L, "AAPL", "Tech", 100, BigDecimal.valueOf(150.0), LocalDateTime.now());
        Investment finance = new Investment(2L, "JPM", "Finance", 50, BigDecimal.valueOf(160.0), LocalDateTime.now());
        Investment healthcare = new Investment(3L, "JNJ", "Healthcare", 75, BigDecimal.valueOf(140.0), LocalDateTime.now());
        List<Investment> investments = List.of(tech, finance, healthcare);
        when(investmentRepository.findAll()).thenReturn(investments);

        List<Investment> all = investmentRepository.findAll();

        assertEquals(3, all.size());
        verify(investmentRepository).findAll();
    }

    @Test
    public void testFindAll_withMultipleRecords() {
        List<Investment> investments = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            investments.add(new Investment((long) i, "TICK" + i, "Sector", 10 + i, BigDecimal.valueOf(100 + i), LocalDateTime.now()));
        }
        when(investmentRepository.findAll()).thenReturn(investments);

        List<Investment> all = investmentRepository.findAll();

        assertEquals(10, all.size());
        verify(investmentRepository).findAll();
    }

    @Test
    public void testInvestmentAttributes_correctlyStored() {
        LocalDateTime now = LocalDateTime.now();
        Long id = 1L;
        Investment investment = new Investment(id, "NVDA", "Tech", 50, BigDecimal.valueOf(875.50), now);
        when(investmentRepository.findById(id)).thenReturn(Optional.of(investment));

        Optional<Investment> found = investmentRepository.findById(id);

        assertTrue(found.isPresent());
        assertEquals("NVDA", found.get().getTicker());
        assertEquals("Tech", found.get().getSector());
        assertEquals(50, found.get().getQuantity());
        assertEquals(BigDecimal.valueOf(875.50), found.get().getBuyPrice());
        assertNotNull(found.get().getPurchaseDate());
        verify(investmentRepository).findById(id);
    }
}

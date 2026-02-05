package org.neueda.rest.project.servicetest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.neueda.rest.project.service.FinnhubService;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class FinnhubServiceTest {

    private FinnhubService service;

    @BeforeEach
    public void setUp() {
        service = new FinnhubService();
    }

    @Test
    public void getStockPrice_returnsCachedValue_whenCacheHit() throws Exception {
        // create CachedItem<BigDecimal> via reflection
        Class<?> cachedClass = Class.forName("org.neueda.rest.project.service.FinnhubService$CachedItem");
        Constructor<?> ctor = cachedClass.getDeclaredConstructor(Object.class);
        ctor.setAccessible(true);
        Object cachedItem = ctor.newInstance(BigDecimal.valueOf(123.45));

        Field priceCacheField = FinnhubService.class.getDeclaredField("priceCache");
        priceCacheField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, Object> priceCache = (Map<String, Object>) priceCacheField.get(service);

        priceCache.put("AAPL", cachedItem);

        BigDecimal price = service.getStockPrice("AAPL");

        assertEquals(0, price.compareTo(BigDecimal.valueOf(123.45)));
    }

    @Test
    public void getCompanyHistory_returnsCachedMap_whenCacheHit() throws Exception {
        Class<?> cachedClass = Class.forName("org.neueda.rest.project.service.FinnhubService$CachedItem");
        Constructor<?> ctor = cachedClass.getDeclaredConstructor(Object.class);
        ctor.setAccessible(true);

        Map<String, Object> sample = Map.of(
                "s", "ok",
                "c", List.of(1.0, 2.0),
                "t", List.of(1000L, 2000L)
        );

        Object cachedItem = ctor.newInstance(sample);

        Field historyCacheField = FinnhubService.class.getDeclaredField("historyCache");
        historyCacheField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, Object> historyCache = (Map<String, Object>) historyCacheField.get(service);

        historyCache.put("FAKE", cachedItem);

        Map<String, Object> result = service.getCompanyHistory("FAKE");

        assertNotNull(result);
        assertEquals("ok", result.get("s"));
        assertTrue(((List<?>) result.get("c")).size() >= 2);
    }

    @Test
    public void getStockPrice_returnsZero_onRestFailure() throws Exception {
        // set apiUrl to an unreachable port to trigger exception quickly
        Field apiUrlField = FinnhubService.class.getDeclaredField("apiUrl");
        apiUrlField.setAccessible(true);
        apiUrlField.set(service, "http://127.0.0.1:1/quote?symbol=");

        Field apiKeyField = FinnhubService.class.getDeclaredField("apiKey");
        apiKeyField.setAccessible(true);
        apiKeyField.set(service, "badkey");

        BigDecimal price = service.getStockPrice("NOSUCH");

        assertEquals(0, price.compareTo(BigDecimal.ZERO));
    }

    @Test
    public void getCompanyHistory_returnsNoData_onRestFailure() throws Exception {
        // call with a ticker that will likely fail network call and return error map
        Map<String, Object> res = service.getCompanyHistory("INVALID_TICKER_TEST");

        assertNotNull(res);
        assertTrue(res.containsKey("s"));
        assertEquals("no_data", res.get("s"));
    }
}
 

package com.example.myproduct.user.table;

import org.junit.jupiter.api.Test;
import com.example.myproduct.model.MyProductRelease;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TableBuilderIntegrationTest {
    @Test
    public void testTableBuilding() {
        List<MyProductRelease> testData = Arrays.asList(
                new MyProductRelease("1.0", "https://example.com/release1"),
                new MyProductRelease("2.0", "https://example.com/release2"));

        assertEquals(Arrays.asList(
                        Arrays.asList("1.0", "https://example.com/release1"),
                        Arrays.asList("2.0", "https://example.com/release2")),
                TableBuilder.build(testData));
    }

}
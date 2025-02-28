package io.agrest.converter.jsonvalue;

import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.FloatNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.TextNode;
import io.agrest.AgException;
import io.agrest.converter.jsonvalue.FloatConverter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FloatConverterTest {

    @Test
    public void testConverter_Zero() {
        Float value = 0f;
        assertEquals(value, convert(value));
    }

    @Test
    public void testConverter_Integer_Zero() {
        Float value = 0f;
        assertEquals(value, convert(0));
    }

    @Test
    public void testConverter_MinValue() {
        Float value = Float.MIN_VALUE;
        assertEquals(value, convert(value));
    }

    @Test
    public void testConverter_NegativeMinValue() {
        Float value = -Float.MIN_VALUE;
        assertEquals(value, convert(value));
    }

    @Test
    public void testConverter_MaxValue() {
        Float value = Float.MAX_VALUE;
        assertEquals(value, convert(value));
    }

    @Test
    public void testConverter_NegativeMaxValue() {
        Float value = -Float.MAX_VALUE;
        assertEquals(value, convert(value));
    }

    @Test
    public void testConverter_NaN() {
        assertEquals(Float.valueOf(Float.NaN), convert("NaN"));
    }

    @Test
    public void testConverter_PositiveInfinity() {
        assertEquals(Float.valueOf(Float.POSITIVE_INFINITY), convert("Infinity"));
        assertEquals(Float.valueOf(Float.POSITIVE_INFINITY), convert("+Infinity"));
    }

    @Test
    public void testConverter_NegativeInfinity() {
        assertEquals(Float.valueOf(Float.NEGATIVE_INFINITY), convert("-Infinity"));
    }

    @Test
    public void testConverter_TooLarge_Positive() {
        assertThrows(AgException.class, () -> FloatConverter.converter().value(new DoubleNode(Float.MAX_VALUE * 1.1d)));
    }

    @Test
    public void testConverter_TooSmall_Positive() {
        assertThrows(AgException.class, () -> FloatConverter.converter().value(new DoubleNode(Float.MIN_VALUE * 0.9d)));
    }

    @Test
    public void testConverter_TooLarge_Negative() {
        assertThrows(AgException.class, () -> FloatConverter.converter().value(new DoubleNode(Float.MAX_VALUE * -1.1d)));
    }

    @Test
    public void testConverter_TooSmall_Negative() {
        assertThrows(AgException.class, () -> FloatConverter.converter().value(new DoubleNode(Float.MIN_VALUE * -0.9d)));
    }

    private Float convert(Float value) {
        return (Float) FloatConverter.converter().value(new FloatNode(value));
    }

    private Float convert(Integer value) {
        return (Float) FloatConverter.converter().value(new IntNode(value));
    }

    private Float convert(String value) {
        return (Float) FloatConverter.converter().value(new TextNode(value));
    }
}

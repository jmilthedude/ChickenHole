package net.ninjadev.chickenhole.client.screen;

import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

public class ChickenSlider extends SliderWidget {

    public enum Type {
        INTEGER,
        DOUBLE,
        BOOLEAN
    }

    private static class RangeConversion {
        final double min;
        final double max;
        final double range;

        RangeConversion(double min, double max) {
            this.min = min;
            this.max = max;
            this.range = max - min;
        }

        double normalize(double actualValue) {
            return (actualValue - this.min) / this.range;
        }

        double denormalized(double normalized) {
            return normalized * range + min;
        }
    }

    private final RangeConversion valueConversion;
    private final Text label;
    private final Type type;

    public ChickenSlider(int x, int y, int width, int height, Text text, double min, double max, double initial, Type type) {
        super(x, y, width, height, text, 0D);
        this.label = text;
        this.valueConversion = new RangeConversion(min, max);
        this.value = valueConversion.normalize(initial);
        this.type = type;
        this.applyValue();
        this.updateMessage();
    }

    @Override
    protected void updateMessage() {
        this.setMessage(this.label.copy().append(": " + this.getFormattedValue()));
    }

    public String getFormattedValue() {
        double val = this.valueConversion.denormalized(this.value);
        return switch (this.type) {
            case INTEGER -> String.format("%d", Math.round(val));
            case DOUBLE -> String.format("%.2f", val);
            case BOOLEAN -> val < 0.5 ? "False" : "True";
        };
    }

    public double getValue() {
        return this.valueConversion.denormalized(this.value);
    }

    @Override
    protected void applyValue() {

    }
}

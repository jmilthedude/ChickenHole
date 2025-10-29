package net.ninjadev.chickenhole.client.screen;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.input.MouseInput;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.ninjadev.chickenhole.network.DigPayload;
import net.ninjadev.chickenhole.util.HoleType;

import java.util.ArrayList;
import java.util.List;

public class ChickenScreen extends Screen {

    private final BlockPos origin;

    private ChickenSlider sizeSlider;
    private ChickenSlider speedSlider;
    private CheckboxWidget dropsCheckbox;

    private final List<CheckboxWidget> holeTypes = new ArrayList<>();
    private boolean suppressGroupCallbacks = false;

    public ChickenScreen(BlockPos origin) {
        super(Text.of("Chicken!"));
        this.origin = origin;
    }

    @Override
    protected void init() {
        int rowCount = 5;
        int padding = 2;
        int y = (this.height / 2) - ((rowCount * (20 + padding)) / 2);
        this.sizeSlider = new ChickenSlider(
                this.width / 2 - 100, y,
                200, 20,
                Text.of("Size"),
                2, 64, 8,
                ChickenSlider.Type.INTEGER
        );
        y += 20 + padding;
        this.speedSlider = new ChickenSlider(
                this.width / 2 - 100, y,
                200, 20,
                Text.of("Movement Speed"),
                1.05D, 3.0D, 1.05D,
                ChickenSlider.Type.DOUBLE
        );
        y += 20 + padding;
        this.dropsCheckbox = CheckboxWidget.builder(Text.of("Enable Drops"), this.textRenderer)
                .checked(false)
                .maxWidth(98)
                .pos(this.width / 2 - 100, y)
                .build();


        int x;
        for (int i = 0; i < HoleType.values().length; i++) {
            String type = switch (HoleType.values()[i]) {
                case HoleType.SEMISPHERE -> "SemiSphere";
                case HoleType.CYLINDER -> "Cylinder";
                case HoleType.CUBE -> "Cube";
            };
            if (i % 2 == 0) {
                x = this.width / 2 + 2;
            } else {
                x = this.width / 2 - 100;
            }
            CheckboxWidget holeTypeCheckbox = CheckboxWidget.builder(Text.of(type), this.textRenderer)
                    .checked(i == 0)
                    .maxWidth(98)
                    .pos(x, y)
                    .callback(this::uncheckOthers)
                    .build();
            if (i % 2 == 0) y += 20 + padding;
            this.holeTypes.add(holeTypeCheckbox);
        }
        this.addDrawableChild(sizeSlider);
        this.addDrawableChild(speedSlider);
        this.addDrawableChild(dropsCheckbox);
        for (CheckboxWidget holeType : this.holeTypes) {
            this.addDrawableChild(holeType);
        }
        y += 20 + padding;
        this.addDrawableChild(
                ButtonWidget.builder(Text.of("Dig!"), this::startDigging)
                        .dimensions(this.width / 2 - 100, y, 200, 20)
                        .build()
        );
    }

    private void startDigging(ButtonWidget buttonWidget) {
        ClientPlayNetworking.send(new DigPayload(this.origin, (int) this.sizeSlider.getValue(), this.getSelectedHoleType(), this.speedSlider.getValue(), this.dropsCheckbox.isChecked()));
        this.close();
    }

    private HoleType getSelectedHoleType() {
        for (int i = 0; i < holeTypes.size(); i++) {
            if (holeTypes.get(i).isChecked()) {
                return HoleType.values()[i];
            }
        }
        return HoleType.SEMISPHERE;
    }

    private void uncheckOthers(CheckboxWidget self, boolean isChecked) {
        if (suppressGroupCallbacks) return;

        if (isChecked) {
            suppressGroupCallbacks = true;
            try {
                for (CheckboxWidget other : holeTypes) {
                    if (other != self && other.isChecked()) {
                        other.onPress(new MouseInput(0, InputUtil.GLFW_RELEASE));
                    }
                }
            } finally {
                suppressGroupCallbacks = false;
            }
        } else {
            if (holeTypes.stream().noneMatch(CheckboxWidget::isChecked)) {
                suppressGroupCallbacks = true;
                try {
                    self.onPress(new MouseInput(0, InputUtil.GLFW_RELEASE));
                } finally {
                    suppressGroupCallbacks = false;
                }
            }
        }
    }
}

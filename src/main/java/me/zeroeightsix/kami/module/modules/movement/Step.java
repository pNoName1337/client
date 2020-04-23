package me.zeroeightsix.kami.module.modules.movement;

import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;

/**
 * @author dominikaaaa (Mode.VANILLA)
 * @author fr1kin (Mode.PACKET)
 *
 * See Forgehax for Mode.PACKET
 * TODO: implement packet mode
 * https://github.com/fr1kin/ForgeHax/blob/2011740/src/main/java/com/matt/forgehax/mods/StepMod.java
 */
@Module.Info(
        name = "Step",
        description = "Changes the vanilla behavior for stepping up blocks",
        category = Module.Category.MOVEMENT
)
public class Step extends Module {
    private Setting<Mode> mode = register(Settings.e("Mode", Mode.VANILLA));
    private Setting<Integer> height = register(Settings.integerBuilder("Height").withMinimum(1).withMaximum(100).withValue(40).withVisibility(v -> mode.getValue().equals(Mode.VANILLA)).build());

    private enum Mode { VANILLA, PACKET }

    public void onUpdate() {
        if (mode.getValue().equals(Mode.VANILLA)) {
            if (mc.player.collidedHorizontally
                    && mc.player.onGround
                    && !mc.player.isOnLadder()
                    && !mc.player.isInWater()
                    && !mc.player.isInLava()) {
                mc.player.motionY = height.getValue() / 100D;
            }
        }
    }
}

package me.zeroeightsix.kami.module.modules.player

import baritone.api.BaritoneAPI
import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.event.events.PacketEvent.Receive
import me.zeroeightsix.kami.gui.kami.DisplayGuiScreen
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.BaritoneUtils.pause
import me.zeroeightsix.kami.util.BaritoneUtils.unpause
import me.zeroeightsix.kami.util.MathsUtils
import me.zeroeightsix.kami.util.MessageSendHelper
import me.zeroeightsix.kami.util.WebHelper
import me.zeroeightsix.kami.util.Wrapper
import net.minecraft.client.gui.GuiChat

/**
 * @author dominikaaaa
 * Thanks Brady and cooker and leij for helping me not be completely retarded
 *
 * Updated by dominikaaaa on 19/04/20
 */
@Module.Info(
        name = "LagNotifier",
        description = "Displays a warning when the server is lagging",
        category = Module.Category.PLAYER
)
class LagNotifier : Module() {
    var pauseDuringLag: Setting<Boolean> = register(Settings.b("Pause Baritone", true))
    private val feedback = register(Settings.booleanBuilder("Pause Feedback").withValue(true).withVisibility { pauseDuringLag.value }.build())
    private val timeout = register(Settings.doubleBuilder().withName("Timeout").withValue(2.0).withMinimum(0.0).withMaximum(10.0).build())

    private var serverLastUpdated: Long = 0
    var hasUnpaused = true
    var text = "Server Not Responding! "

    override fun onRender() {
        if ((mc.currentScreen != null && mc.currentScreen !is GuiChat) || mc.isIntegratedServerRunning) return
        if (1000L *  timeout.value.toDouble() > System.currentTimeMillis() - serverLastUpdated) {
            if (!hasUnpaused && pauseDuringLag.value) {
                hasUnpaused = true
                if (feedback.value) MessageSendHelper.sendBaritoneMessage("Unpaused!")
                unpause()
            }
            return
        }

        if (shouldPing()) {
            WebHelper.run()
            text = if (WebHelper.isInternetDown) {
                "Your internet is offline! "
            } else {
                "Server Not Responding! "
            }
            if (hasUnpaused && pauseDuringLag.value && BaritoneAPI.getProvider().primaryBaritone.customGoalProcess.goal != null) {
                if (feedback.value) MessageSendHelper.sendBaritoneMessage("Paused due to lag!")
                pause()
                hasUnpaused = false
            }
        }
        text = text.replace("! .*".toRegex(), "! " + timeDifference() + "s")
        val renderer = Wrapper.getFontRenderer()
        val divider = DisplayGuiScreen.getScale()

        /* 217 is the offset to make it go high, bigger = higher, with 0 being center */
        renderer.drawStringWithShadow(mc.displayWidth / divider / 2 - renderer.getStringWidth(text) / 2, mc.displayHeight / divider / 2 - 217, 255, 85, 85, text)
    }

    @EventHandler
    private val receiveListener = Listener(EventHook { event: Receive? -> serverLastUpdated = System.currentTimeMillis() })

    private fun timeDifference(): Double {
        return MathsUtils.round((System.currentTimeMillis() - serverLastUpdated) / 1000.0, 1)
    }

    private fun shouldPing(): Boolean {
        if (startTime == 0L) startTime = System.currentTimeMillis()
        if (startTime + 1000 <= System.currentTimeMillis()) { // 1 second
            startTime = System.currentTimeMillis()
            return true
        }
        return false
    }

    companion object {
        private var startTime: Long = 0
    }
}
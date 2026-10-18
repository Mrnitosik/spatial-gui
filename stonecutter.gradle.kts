plugins {
    id("dev.kikugie.stonecutter")
}
stonecutter active "26.1.2-fabric"

stonecutter parameters {
    constants.match(node.metadata.project.substringAfterLast('-'), "fabric", "neoforge", "forge")
    // 1.21.4 is built with the 1.21.1 version tag (legacy GUI pipeline); this constant marks the places
    // where 1.21.2-1.21.4 differ from 1.21.1 (entity render states, CoreShaders, applyInput, ...)
    constants.put("mc1214", node.metadata.project.startsWith("1.21.4"))

    replacements {
        string(current.parsed < "26.2") {
            replace("gameRenderer.mainCamera()", "gameRenderer.getMainCamera()")
            replace("= client.gui.screen()", "= client.screen")
        }

        string(current.parsed < "26.1.2") {
            replace("g.outline", "g.renderOutline")
            replace("g.horizontalLine", "g.hLine")
            replace("g.verticalLine", "g.vLine")
        }

        string(current.parsed < "1.21.1") {
            replace("modelView.pushMatrix()", "modelView.pushPose()")
            replace("modelView.popMatrix()", "modelView.popPose()")
        }

        string(current.parsed < "1.21.11") {
            replace("camera.yRot()", "camera.getYRot()")
            replace("camera.xRot()", "camera.getXRot()")
            replace(".grabOrReleaseMouse(mc.getWindow(), ", ".grabOrReleaseMouse(mc.getWindow().getWindow(), ")
            replace("invertMouseY()", "invertYMouse()")
        }

        string(current.parsed < "1.21.11") {
            replace(
                "me.shedaniel.autoconfig.AutoConfigClient.getConfigScreen",
                "me.shedaniel.autoconfig.AutoConfig.getConfigScreen"
            )
        }
    }
}
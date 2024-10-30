// Import any classes you need here
import net.minecraft.server.v1_8_R3.EntityLiving
import gg.mineral.server.combat.KnockbackProtocol

// Knockback values
friction = 3.5
horizontal = 0.325
horizontalExtra = 0.5
vertical = 0.5
verticalExtra = 0.0
verticalLimit = 0.36

protocol = new KnockbackProtocol() {

    @Override
    void firstStage(EntityLiving attacker, EntityLiving victim) {
        if (friction > 0) {
            victim.motX /= friction
            victim.motY /= friction
            victim.motZ /= friction
        } else {
            victim.motX = 0
            victim.motY = 0
            victim.motZ = 0
        }

        distanceX = attacker.locX - victim.locX
        distanceZ = attacker.locZ - victim.locZ

        magnitude = Math.sqrt(distanceX * distanceX + distanceZ * distanceZ)

        victim.motX -= distanceX / magnitude * horizontal
        victim.motY += vertical
        victim.motZ -= distanceZ / magnitude * horizontal

        if (victim.motY > verticalLimit) {
            victim.motY = verticalLimit
        }
    }

    @Override
    void secondStage(EntityLiving attacker, EntityLiving victim, int knockbackEnchantLevel) {
        int extraKBMult = knockbackEnchantLevel
        if (attacker.isSprinting()) {
            extraKBMult += 1
        }

        if (extraKBMult > 0) {
            yaw = Math.toRadians(attacker.yaw)
            sin = -Math.sin(yaw)
            cos = Math.cos(yaw)
            victim.motX += sin * horizontalExtra * extraKBMult
            victim.motY += verticalExtra
            victim.motZ += cos * horizontalExtra * extraKBMult
            attacker.motX *= 0.6D
            attacker.motZ *= 0.6D
            attacker.setSprinting(false)
        }
    }

}

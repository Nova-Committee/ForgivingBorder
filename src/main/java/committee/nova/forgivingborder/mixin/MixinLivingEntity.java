package committee.nova.forgivingborder.mixin;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.levelgen.Heightmap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity {
    @Shadow
    public abstract boolean isAlive();

    public MixinLivingEntity(EntityType<?> t, Level l) {
        super(t, l);
    }

    @Inject(method = "baseTick", at = @At("HEAD"), cancellable = true)
    private void inject$baseTick(CallbackInfo ci) {
        if (!isAlive()) return;
        if (!((LivingEntity) (Object) this instanceof Player player)) return;
        final WorldBorder border = level.getWorldBorder();
        if (border.isWithinBounds(getBoundingBox())) return;
        ci.cancel();
        if (level.isClientSide) return;
        final double targetX = Mth.clamp(position().x, border.getMinX() + .5, border.getMaxX() - .5);
        final double targetZ = Mth.clamp(position().z, border.getMinZ() + .5, border.getMaxZ() - .5);
        final double targetY = level.getHeight(Heightmap.Types.MOTION_BLOCKING,
                (int) Math.floor(targetX), (int) Math.floor(targetZ)) + 1.0;
        teleportTo(targetX, targetY, targetZ);
        player.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
    }
}

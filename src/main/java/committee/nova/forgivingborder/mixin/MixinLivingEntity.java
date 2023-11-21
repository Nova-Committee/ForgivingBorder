package committee.nova.forgivingborder.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.gen.Heightmap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity {
    public MixinLivingEntity(EntityType<?> t, World w) {
        super(t, w);
    }

    @Inject(method = "baseTick", at = @At("HEAD"), cancellable = true)
    private void inject$baseTick(CallbackInfo ci) {
        if (!isAlive()) return;
        if (!((LivingEntity) (Object) this instanceof PlayerEntity)) return;
        final WorldBorder border = level.getWorldBorder();
        if (border.isWithinBounds(getBoundingBox())) return;
        ci.cancel();
        if (level.isClientSide) return;
        final double targetX = MathHelper.clamp(position().x, border.getMinX() + .5, border.getMaxX() - .5);
        final double targetZ = MathHelper.clamp(position().z, border.getMinZ() + .5, border.getMaxZ() - .5);
        final double targetY = level.getHeight(Heightmap.Type.MOTION_BLOCKING,
                (int) Math.floor(targetX), (int) Math.floor(targetZ)) + 1.0;
        teleportTo(targetX, targetY, targetZ);
        playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
    }
}

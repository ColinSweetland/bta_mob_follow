package csweetla.mob_follow.mixins;

import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.EntityLiving;
import net.minecraft.core.entity.EntityPathfinder;
import net.minecraft.core.entity.animal.EntityAnimal;
import net.minecraft.core.entity.animal.EntityWolf;
import net.minecraft.core.entity.player.EntityPlayer;
import net.minecraft.core.item.Item;
import net.minecraft.core.world.World;

import org.objectweb.asm.Opcodes;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static csweetla.mob_follow.MobFollow.lead_item_map;

@Mixin(value = EntityPathfinder.class, remap = false)
public abstract class EntityPathfinderMixin extends EntityLiving {

	public EntityPathfinderMixin(World world) {
		super(world);
	}

	@Unique
	float mob_follow_radius_sqr = 144.0F;

	@Unique
	float player_personal_space_sqr = 8.0F;

	@Unique
	private boolean isHoldingItem(EntityPlayer p, Item i) {
		return p.getHeldItem() != null && p.getHeldItem().itemID == i.id;
	}

	@Inject(method = "updatePlayerActionState", at = @At("HEAD"))
	protected void updatePlayerActionState(CallbackInfo ci) {
		EntityPathfinder thisAs = (EntityPathfinder) (Object) this;

		if (!(thisAs instanceof EntityAnimal) || thisAs instanceof EntityWolf)
			return;

		Item lead_item = lead_item_map.get(thisAs.getClass());
		if (lead_item == null)
			return;

		// if we are currently following a player,
		// just make sure they are still holding wheat
		if (thisAs.getTarget() != null) {
			Entity t = thisAs.getTarget();
			if (!(t instanceof EntityPlayer))
				return;
			if (!isHoldingItem((EntityPlayer) t,lead_item)) {
				thisAs.setTarget(null);
			}
			return;
		}

		// else: try to find a player to follow
		double closestDistance = -1.0;
		EntityPlayer closest_player = null;

		// find the closest player who is holding wheat and can be seen
		for (int i = 0; i < thisAs.world.players.size(); ++i) {
			EntityPlayer p = thisAs.world.players.get(i);

			if (!isHoldingItem(p,lead_item) || !thisAs.canEntityBeSeen(p))
				continue;

			double distancesqr = p.distanceToSqr(thisAs);
			if ((distancesqr > mob_follow_radius_sqr) || (closestDistance != -1.0 && (distancesqr >= closestDistance)))
				continue;

			closestDistance = distancesqr;
			closest_player = p;
		}

		if (closest_player != null)
			thisAs.setTarget(closest_player);
	}

	// makes animals stop moving when they are a short distance away, so they don't push the player who is leading them
	@Redirect(method = "updatePlayerActionState", at = @At(value = "FIELD", target = "Lnet/minecraft/core/entity/EntityPathfinder;moveSpeed:F", opcode = Opcodes.GETFIELD))
	private float getMoveSpeed(EntityPathfinder instance) {
		EntityPathfinder thisAs = (EntityPathfinder) (Object) this;
		if (thisAs instanceof EntityAnimal && thisAs.getTarget() != null && thisAs.getTarget() instanceof EntityPlayer) {
			if(thisAs.getTarget().distanceToSqr(thisAs) < player_personal_space_sqr) {
				return 0;
			}
		}
		return this.moveSpeed;
	}
}


